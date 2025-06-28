package com.safeentry.Visits.model;

import com.fasterxml.jackson.annotation.JsonProperty;

// Esta classe representa a estrutura JSON que você armazena no campo 'visitante_json'
public class VisitanteInfo {
    @JsonProperty("nome")
    private String nome;
    @JsonProperty("documento")
    private String documento;
    @JsonProperty("veiculo")
    private String veiculo;

    public VisitanteInfo() {}

    public VisitanteInfo(String nome, String documento, String veiculo) {
        this.nome = nome;
        this.documento = documento;
        this.veiculo = veiculo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getVeiculo() {
        return veiculo;
    }

    public void setVeiculo(String veiculo) {
        this.veiculo = veiculo;
    }

    @Override
    public String toString() {
        return "VisitanteInfo{" +
               "nome='" + nome + '\'' +
               ", documento='" + documento + '\'' +
               ", veiculo='" + veiculo + '\'' +
               '}';
    }
}