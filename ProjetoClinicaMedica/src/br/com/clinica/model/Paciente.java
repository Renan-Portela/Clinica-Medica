package br.com.clinica.model;

import java.time.LocalDate;
import java.time.Period;

/**
 * Representa um paciente no sistema da clínica.
 * Esta classe armazena os dados pessoais e de contato do paciente, como o identificador
 * único (cpf), nome, data de nascimento (dataNascimento), endereço, telefone,
 * e um campo para o histórico médico (historicoMedico).
 */
public class Paciente {
    private String cpf;
    private String nome;
    private LocalDate dataNascimento;
    private String endereco;
    private String telefone;
    private String historicoMedico;

    /**
     * Construtor padrão.
     */
    public Paciente() {}

    /**
     * Construtor para criar um novo paciente com todos os dados.
     * @param cpf O CPF do paciente.
     * @param nome O nome completo do paciente.
     * @param dataNascimento A data de nascimento do paciente.
     * @param endereco O endereço residencial do paciente.
     * @param telefone O número de telefone de contato do paciente.
     * @param historicoMedico O histórico de condições médicas do paciente.
     */
    public Paciente(String cpf, String nome, LocalDate dataNascimento, 
                    String endereco, String telefone, String historicoMedico) {
        this.cpf = cpf;
        this.nome = nome;
        this.dataNascimento = dataNascimento;
        this.endereco = endereco;
        this.telefone = telefone;
        this.historicoMedico = historicoMedico;
    }

    /**
     * Calcula e retorna a idade atual do paciente com base na data de nascimento.
     * @return A idade do paciente em anos, ou 0 se a data de nascimento for nula.
     */
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