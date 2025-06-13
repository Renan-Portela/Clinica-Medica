package br.com.clinica.model;

import java.time.LocalDate;
import java.time.Period;

public class Paciente {
    private String cpf;
    private String nome;
    private LocalDate dataNascimento;
    private String endereco;
    private String telefone;
    private String historicoMedico;
    
    public Paciente() {}
    
    public Paciente(String cpf, String nome, LocalDate dataNascimento, 
                    String endereco, String telefone, String historicoMedico) {
        this.cpf = cpf;
        this.nome = nome;
        this.dataNascimento = dataNascimento;
        this.endereco = endereco;
        this.telefone = telefone;
        this.historicoMedico = historicoMedico;
    }
    
    public int getIdade() {
        if (dataNascimento == null) return 0;
        return Period.between(dataNascimento, LocalDate.now()).getYears();
    }
    
    // Getters e Setters
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    
    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    
    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    
    public String getHistoricoMedico() { return historicoMedico; }
    public void setHistoricoMedico(String historicoMedico) { this.historicoMedico = historicoMedico; }
    
    @Override
    public String toString() {
        return nome + " (CPF: " + cpf + ") - " + getIdade() + " anos";
    }
}