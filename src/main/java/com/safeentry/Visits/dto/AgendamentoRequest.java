package com.safeentry.Visits.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.safeentry.Visits.model.VisitanteInfo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

// DTO para a requisição de criação de agendamento
public class AgendamentoRequest {

    @NotNull(message = "A data e hora da visita são obrigatórias")
    @FutureOrPresent(message = "A data e hora da visita não pode ser no passado")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime dataHoraVisita;

    @NotNull(message = "As informações do visitante são obrigatórias")
    @Valid
    private VisitanteInfo visitante; // Mapeia para o JSONB

    public LocalDateTime getDataHoraVisita() {
        return dataHoraVisita;
    }

    public void setDataHoraVisita(LocalDateTime dataHoraVisita) {
        this.dataHoraVisita = dataHoraVisita;
    }

    public VisitanteInfo getVisitante() {
        return visitante;
    }

    public void setVisitante(VisitanteInfo visitante) {
        this.visitante = visitante;
    }
}