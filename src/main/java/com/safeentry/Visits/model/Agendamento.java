package com.safeentry.Visits.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "agendamentos")
public class Agendamento {

    @Id
    @GeneratedValue(generator = "UUID")
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "morador_id", nullable = false)
    private UUID moradorId; // ID do morador que fez o agendamento (vem do serviço de autenticação)

    @Column(name = "data_hora_visita", nullable = false)
    private LocalDateTime dataHoraVisita;

    // Mapeamento para o tipo JSONB do PostgreSQL
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "visitante_json", columnDefinition = "jsonb", nullable = false)
    private VisitanteInfo visitanteJson; // Usar a classe VisitanteInfo para mapear o JSONB

    @Column(name = "qr_token", nullable = false, unique = true)
    private String qrToken; // Token para o QR Code

    @Column(name = "usado")
    private Boolean usado = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AgendamentoStatus status = AgendamentoStatus.pendente;

    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    // Construtor padrão
    public Agendamento() {}

    // Getters e Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getMoradorId() {
        return moradorId;
    }

    public void setMoradorId(UUID moradorId) {
        this.moradorId = moradorId;
    }

    public LocalDateTime getDataHoraVisita() {
        return dataHoraVisita;
    }

    public void setDataHoraVisita(LocalDateTime dataHoraVisita) {
        this.dataHoraVisita = dataHoraVisita;
    }

    public VisitanteInfo getVisitanteJson() {
        return visitanteJson;
    }

    public void setVisitanteJson(VisitanteInfo visitanteJson) {
        this.visitanteJson = visitanteJson;
    }

    public String getQrToken() {
        return qrToken;
    }

    public void setQrToken(String qrToken) {
        this.qrToken = qrToken;
    }

    public Boolean getUsado() {
        return usado;
    }

    public void setUsado(Boolean usado) {
        this.usado = usado;
    }

    public AgendamentoStatus getStatus() {
        return status;
    }

    public void setStatus(AgendamentoStatus status) {
        this.status = status;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    @Override
    public String toString() {
        return "Agendamento{" +
               "id=" + id +
               ", moradorId=" + moradorId +
               ", dataHoraVisita=" + dataHoraVisita +
               ", visitanteJson=" + visitanteJson +
               ", qrToken='" + qrToken + '\'' +
               ", usado=" + usado +
               ", status=" + status +
               ", criadoEm=" + criadoEm +
               '}';
    }
}