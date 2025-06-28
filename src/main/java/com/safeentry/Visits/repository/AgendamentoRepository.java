package com.safeentry.Visits.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.safeentry.Visits.model.Agendamento;
import com.safeentry.Visits.model.AgendamentoStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, UUID> {
    // Buscar agendamentos por morador_id
    List<Agendamento> findByMoradorIdOrderByDataHoraVisitaDesc(UUID moradorId);

    // Buscar agendamentos por qr_token
    Optional<Agendamento> findByQrToken(String qrToken);

    // Buscar agendamentos pendentes ou futuros
    List<Agendamento> findByStatusAndDataHoraVisitaAfterOrderByDataHoraVisitaAsc(AgendamentoStatus status, LocalDateTime now);

    // Buscar agendamentos pendentes para um morador
    List<Agendamento> findByMoradorIdAndStatusOrderByDataHoraVisitaAsc(UUID moradorId, AgendamentoStatus status);
}