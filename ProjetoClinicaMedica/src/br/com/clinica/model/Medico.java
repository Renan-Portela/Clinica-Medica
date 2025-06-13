package br.com.clinica.model;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Medico {
    private String crm;
    private String nome;
    private String especialidade;
    private List<String> diasAtendimento;
    private LocalTime horarioInicio;
    private LocalTime horarioFim;
    private String salaAtendimento;
    
    public Medico() {
        this.diasAtendimento = new ArrayList<>();
    }
    
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