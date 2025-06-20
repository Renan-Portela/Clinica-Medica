package br.com.clinica.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Representa uma consulta médica no sistema.
 * Esta classe encapsula todas as informações pertinentes a um agendamento,
 * incluindo o médico responsável (Medico), o paciente (Paciente), a data/hora (dataHorario),
 * o status atual (StatusConsulta) e quaisquer observações clínicas (observacoes).
 * O identificador único da consulta é representado pelo atributo 'id'.
 */
public class Consulta {
    private Long id;
    private Medico medico;
    private Paciente paciente;
    private LocalDateTime dataHorario;
    private String observacoes;
    private StatusConsulta status;

    /**
     * Enum que define os possíveis estados de uma consulta dentro do sistema.
     */
    public enum StatusConsulta {
        AGENDADA("Agendada", "A"),
        REALIZADA("Realizada", "R"), 
        CANCELADA("Cancelada", "C"),
        NAO_COMPARECEU("Não Compareceu", "N");
        
        private final String descricao;
        private final String sigla;
        
        StatusConsulta(String descricao, String sigla) {
            this.descricao = descricao;
            this.sigla = sigla;
        }
        
        public String getDescricao() { return descricao; }
        public String getSigla() { return sigla; }
    }

    /**
     * Construtor padrão. Inicializa a consulta com o status AGENDADA e observações vazias.
     */
    public Consulta() {
        this.status = StatusConsulta.AGENDADA;
        this.observacoes = "";
    }

    /**
     * Construtor para criar uma nova consulta com os dados essenciais.
     * O status inicial é definido como AGENDADA.
     * @param medico O objeto Medico associado à consulta.
     * @param paciente O objeto Paciente associado à consulta.
     * @param dataHorario A data e hora em que a consulta ocorrerá.
     */
    public Consulta(Medico medico, Paciente paciente, LocalDateTime dataHorario) {
        this.medico = medico;
        this.paciente = paciente;
        this.dataHorario = dataHorario;
        this.status = StatusConsulta.AGENDADA;
        this.observacoes = "";
    }

    /**
     * Formata a data e o horário da consulta para uma representação textual (dd/MM/yyyy HH:mm).
     * @return Uma String com a data e o horário formatados.
     */
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