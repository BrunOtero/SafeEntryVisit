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

// (Opcional) Se for usar Kafka para notificação
// import org.springframework.kafka.core.KafkaTemplate;
// import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    // private final KafkaTemplate<String, String> kafkaTemplate; // Descomente se for usar Kafka
    // private final ObjectMapper objectMapper; // Descomente se for usar Kafka e precisar serializar JSON

    public AgendamentoService(AgendamentoRepository agendamentoRepository
                              /*, KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper*/) { // Descomente
        this.agendamentoRepository = agendamentoRepository;
        // this.kafkaTemplate = kafkaTemplate; // Descomente
        // this.objectMapper = objectMapper; // Descomente
    }

    // Método para gerar um QR Token único e seguro
    private String generateQrToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[16]; // 16 bytes = 128 bits, bom para um token único
        secureRandom.nextBytes(tokenBytes);
        // Codifica para Base64 URL-safe para evitar caracteres problemáticos em URLs ou QR Codes
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    @Transactional
    public Agendamento createAgendamento(AgendamentoRequest request, UUID moradorId) {
        Agendamento agendamento = new Agendamento();
        agendamento.setMoradorId(moradorId); // Define o ID do morador extraído do token
        agendamento.setDataHoraVisita(request.getDataHoraVisita());
        agendamento.setVisitanteJson(request.getVisitante());
        agendamento.setQrToken(generateQrToken()); // Gera um QR Token único
        agendamento.setStatus(AgendamentoStatus.PENDENTE); // Define o status inicial

        Agendamento savedAgendamento = agendamentoRepository.save(agendamento);

        // (Opcional) Publicar evento para o serviço de Portaria via Kafka
        /*
        try {
            String agendamentoJson = objectMapper.writeValueAsString(savedAgendamento);
            kafkaTemplate.send("agendamentos-criados", savedAgendamento.getId().toString(), agendamentoJson);
            System.out.println("Evento de agendamento criado enviado para Kafka: " + savedAgendamento.getId());
        } catch (Exception e) {
            System.err.println("Erro ao enviar evento para Kafka: " + e.getMessage());
            // Lidar com o erro de forma apropriada (log, retry, etc.)
        }
        */

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

        // Adicione lógica de validação aqui se necessário (ex: não pode mudar de USADO para PENDENTE)
        agendamento.setStatus(newStatus);
        // Se o status for USADO, marque como usado = true
        if (newStatus == AgendamentoStatus.USADO) {
            agendamento.setUsado(true);
        }

        return agendamentoRepository.save(agendamento);
    }

    // Método para marcar um agendamento como usado (inválido após o uso)
    @Transactional
    public Agendamento markAgendamentoAsUsed(String qrToken) {
        Agendamento agendamento = agendamentoRepository.findByQrToken(qrToken)
                .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado para o token: " + qrToken));

        if (agendamento.getUsado() || agendamento.getStatus() != AgendamentoStatus.PENDENTE) {
            throw new IllegalStateException("Agendamento já foi usado ou está em status inválido.");
        }

        agendamento.setUsado(true);
        agendamento.setStatus(AgendamentoStatus.USADO);
        return agendamentoRepository.save(agendamento);
    }
}