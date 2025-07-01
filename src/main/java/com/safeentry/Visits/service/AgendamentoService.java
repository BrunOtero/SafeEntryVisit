package com.safeentry.Visits.service;

import com.safeentry.Visits.model.Agendamento;
import com.safeentry.Visits.model.AgendamentoStatus;
import com.safeentry.Visits.dto.AgendamentoRequest;
import com.safeentry.Visits.repository.AgendamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.kafka.core.KafkaTemplate;
import com.safeentry.Visits.dto.AgendamentoResponse; // Import AgendamentoResponse DTO

@Service
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public AgendamentoService(AgendamentoRepository agendamentoRepository,
                              KafkaTemplate<String, Object> kafkaTemplate) {
        this.agendamentoRepository = agendamentoRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    private String generateQrToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[16];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    @Transactional
    public Agendamento createAgendamento(AgendamentoRequest request, UUID moradorId) {
        Agendamento agendamento = new Agendamento();
        agendamento.setMoradorId(moradorId);
        agendamento.setDataHoraVisita(request.getDataHoraVisita());
        agendamento.setVisitanteJson(request.getVisitante());
        agendamento.setQrToken(generateQrToken());
        agendamento.setStatus(AgendamentoStatus.pendente);

        Agendamento savedAgendamento = agendamentoRepository.save(agendamento);

        // Convert Agendamento to AgendamentoResponse DTO before sending to Kafka
        // The Gate service expects VisitServiceAgendamentoResponse, which should match this DTO's structure.
        AgendamentoResponse agendamentoResponseForKafka = new AgendamentoResponse(
                savedAgendamento.getId(),
                savedAgendamento.getMoradorId(),
                savedAgendamento.getDataHoraVisita(),
                savedAgendamento.getVisitanteJson(),
                savedAgendamento.getQrToken(),
                savedAgendamento.getUsado(),
                savedAgendamento.getStatus(),
                savedAgendamento.getCriadoEm()
        );

        try {
            kafkaTemplate.send("agendamentos-criados", savedAgendamento.getId().toString(), agendamentoResponseForKafka);
            System.out.println("Evento de agendamento criado enviado para Kafka: " + savedAgendamento.getId()); //
        } catch (Exception e) {
            System.err.println("Erro ao enviar evento para Kafka: " + e.getMessage());
        }

        return savedAgendamento;
    }

    public Optional<Agendamento> getAgendamentoById(UUID id) {
        return agendamentoRepository.findById(id);
    }

    public List<Agendamento> getAgendamentosByMorador(UUID moradorId) {
        return agendamentoRepository.findByMoradorIdOrderByDataHoraVisitaDesc(moradorId);
    }

    public Optional<Agendamento> getAgendamentoByQrToken(String qrToken) {
        return agendamentoRepository.findByQrToken(qrToken);
    }

    @Transactional
    public Agendamento updateAgendamentoStatus(UUID agendamentoId, AgendamentoStatus newStatus) {
        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado com o ID: " + agendamentoId));

        agendamento.setStatus(newStatus);
        if (newStatus == AgendamentoStatus.usado) {
            agendamento.setUsado(true);
        }

        return agendamentoRepository.save(agendamento);
    }

    @Transactional
    public Agendamento markAgendamentoAsUsed(String qrToken) {
        Agendamento agendamento = agendamentoRepository.findByQrToken(qrToken)
                .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado para o token: " + qrToken));

        if (agendamento.getUsado() || agendamento.getStatus() != AgendamentoStatus.pendente) {
            throw new IllegalStateException("Agendamento já foi usado ou está em status inválido.");
        }

        agendamento.setUsado(true);
        agendamento.setStatus(AgendamentoStatus.usado);
        return agendamentoRepository.save(agendamento);
    }

    @Transactional
    public Agendamento cancelAgendamento(UUID agendamentoId, UUID moradorId) {
        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado com o ID: " + agendamentoId));

        if (!agendamento.getMoradorId().equals(moradorId)) {
            throw new IllegalStateException("Você não tem permissão para cancelar este agendamento.");
        }

        if (agendamento.getStatus() != AgendamentoStatus.pendente) {
            throw new IllegalStateException("Não é possível cancelar um agendamento que não esteja pendente. Status atual: " + agendamento.getStatus());
        }

        agendamento.setStatus(AgendamentoStatus.cancelado);
        return agendamentoRepository.save(agendamento);
    }
}