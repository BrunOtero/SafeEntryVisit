package com.safeentry.Visits.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.safeentry.Visits.model.VisitanteInfo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

// DTO para a requisição de criação de agendamento
public class AgendamentoRequest {

    // O moradorId virá do token JWT do usuário autenticado, então não é necessário no payload da requisição,
    // mas pode ser útil para validações futuras ou se a API permitir um admin agendar para outro morador.
    // Por enquanto, não vamos incluir aqui, pois será extraído do token JWT.

    @NotNull(message = "A data e hora da visita são obrigatórias")
    @FutureOrPresent(message = "A data e hora da visita não pode ser no passado")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS") // AGORA COM MILISSEGUNDOS
    private LocalDateTime dataHoraVisita;

    @NotNull(message = "As informações do visitante são obrigatórias")
    @Valid // Valida também os campos dentro de VisitanteInfo
    private VisitanteInfo visitante; // Mapeia para o JSONB

    // Getters e Setters
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