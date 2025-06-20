package br.com.clinica.model;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa um profissional médico no sistema.
 * Esta classe encapsula os dados cadastrais de um médico, incluindo seu identificador
 * profissional (crm), nome, especialidade, a lista de dias em que atende (diasAtendimento),
 * o horário de início e fim da jornada (horarioInicio, horarioFim) e sua sala 
 * de atendimento (salaAtendimento).
 */
public class Medico {
    private String crm;
    private String nome;
    private String especialidade;
    private List<String> diasAtendimento;
    private LocalTime horarioInicio;
    private LocalTime horarioFim;
    private String salaAtendimento;

    /**
     * Construtor padrão. Inicializa a lista de dias de atendimento como vazia.
     */
    public Medico() {
        this.diasAtendimento = new ArrayList<>();
    }

    /**
     * Construtor para criar um novo médico com todos os dados.
     * @param crm O CRM do médico.
     * @param nome O nome completo do médico.
     * @param especialidade A especialidade do médico.
     * @param diasAtendimento A lista de dias da semana em que o médico atende.
     * @param horarioInicio O horário de início do atendimento.
     * @param horarioFim O horário de fim do atendimento.
     * @param salaAtendimento A sala onde o médico realiza os atendimentos.
     */
    public Medico(String crm, String nome, String especialidade, List<String> diasAtendimento, 
                  LocalTime horarioInicio, LocalTime horarioFim, String salaAtendimento) {
        this.crm = crm;
        this.nome = nome;
        this.especialidade = especialidade;
        this.diasAtendimento = diasAtendimento != null ? diasAtendimento : new ArrayList<>();
        this.horarioInicio = horarioInicio;
        this.horarioFim = horarioFim;
        this.salaAtendimento = salaAtendimento;
    }
    
    // Getters e Setters
    public String getCrm() { return crm; }
    public void setCrm(String crm) { this.crm = crm; }
    
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    
    public String getEspecialidade() { return especialidade; }
    public void setEspecialidade(String especialidade) { this.especialidade = especialidade; }
    
    public List<String> getDiasAtendimento() { return diasAtendimento; }
    public void setDiasAtendimento(List<String> diasAtendimento) { this.diasAtendimento = diasAtendimento; }
    
    public LocalTime getHorarioInicio() { return horarioInicio; }
    public void setHorarioInicio(LocalTime horarioInicio) { this.horarioInicio = horarioInicio; }
    
    public LocalTime getHorarioFim() { return horarioFim; }
    public void setHorarioFim(LocalTime horarioFim) { this.horarioFim = horarioFim; }
    
    public String getSalaAtendimento() { return salaAtendimento; }
    public void setSalaAtendimento(String salaAtendimento) { this.salaAtendimento = salaAtendimento; }
    
    @Override
    public String toString() {
        return "Dr(a). " + nome + " - " + especialidade + " (CRM: " + crm + ")";
    }
}