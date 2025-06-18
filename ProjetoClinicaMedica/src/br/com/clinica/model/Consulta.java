package br.com.clinica.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Consulta {
    private Long id;
    private Medico medico;
    private Paciente paciente;
    private LocalDateTime dataHorario;
    private String observacoes;
    private StatusConsulta status;
    
    public enum StatusConsulta {
        AGENDADA("Agendada", "A"),
        REALIZADA("Realizada", "R"), 
        CANCELADA("Cancelada", "C"),
        NAO_COMPARECEU("Não Compareceu", "N"); // Adicionado sigla 'N'
        
        private final String descricao;
        private final String sigla; // Novo campo para a sigla
        
        StatusConsulta(String descricao, String sigla) { // Construtor atualizado
            this.descricao = descricao;
            this.sigla = sigla;
        }
        
        public String getDescricao() { return descricao; }
        public String getSigla() { return sigla; } // Novo método para obter a sigla
    }
    
    public Consulta() {
        this.status = StatusConsulta.AGENDADA;
        this.observacoes = "";
    }
    
    public Consulta(Medico medico, Paciente paciente, LocalDateTime dataHorario) {
        this.medico = medico;
        this.paciente = paciente;
        this.dataHorario = dataHorario;
        this.status = StatusConsulta.AGENDADA;
        this.observacoes = "";
    }
    
    public String getDataHorarioFormatado() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return dataHorario.format(formatter);
    }
    
    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Medico getMedico() { return medico; }
    public void setMedico(Medico medico) { this.medico = medico; }
    
    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }
    
    public LocalDateTime getDataHorario() { return dataHorario; }
    public void setDataHorario(LocalDateTime dataHorario) { this.dataHorario = dataHorario; }
    
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    
    public StatusConsulta getStatus() { return status; }
    public void setStatus(StatusConsulta status) { this.status = status; }
    
    @Override
    public String toString() {
        return "Consulta: " + paciente.getNome() + " com " + medico.getNome() + 
               " em " + getDataHorarioFormatado() + " - " + status.getDescricao();
    }
}