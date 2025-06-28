package com.safeentry.Visits.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.safeentry.Visits.model.AgendamentoStatus;
import com.safeentry.Visits.model.VisitanteInfo;

import java.time.LocalDateTime;
import java.util.UUID;

// DTO para a resposta de agendamento
public class AgendamentoResponse {
    private UUID id;
    private UUID moradorId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataHoraVisita;
    private VisitanteInfo visitante;
    private String qrToken;
    private Boolean usado;
    private AgendamentoStatus status;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime criadoEm;

    // Construtor para mapear de Agendamento para AgendamentoResponse
    public AgendamentoResponse(UUID id, UUID moradorId, LocalDateTime dataHoraVisita, VisitanteInfo visitante,
                               String qrToken, Boolean usado, AgendamentoStatus status, LocalDateTime criadoEm) {
        this.id = id;
        this.moradorId = moradorId;
        this.dataHoraVisita = dataHoraVisita;
        this.visitante = visitante;
        this.qrToken = qrToken;
        this.usado = usado;
        this.status = status;
        this.criadoEm = criadoEm;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public UUID getMoradorId() {
        return moradorId;
    }

    public LocalDateTime getDataHoraVisita() {
        return dataHoraVisita;
    }

    public VisitanteInfo getVisitante() {
        return visitante;
    }

    public String getQrToken() {
        return qrToken;
    }

    public Boolean getUsado() {
        return usado;
    }

    public AgendamentoStatus getStatus() {
        return status;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}