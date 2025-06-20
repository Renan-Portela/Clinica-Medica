package br.com.clinica.dao;

import br.com.clinica.config.DatabaseConnection;
import br.com.clinica.model.Medico;

import java.sql.*;
import java.time.LocalTime;
import java.util.*;

public class MedicoDAO {
    private Connection connection;

    /**
     * Construtor. Inicializa a conexão com o banco de dados.
     * Interage com as classes: DatabaseConnection.
     */
    public MedicoDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Salva um novo médico no banco de dados.
     * Interage com as classes: Medico, DatabaseConnection.
     */
    public void save(Medico medico) throws SQLException {
        String sql = "INSERT INTO medicos (crm, nome, especialidade, dias_atendimento, " +
                    "horario_inicio, horario_fim, sala_atendimento) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, medico.getCrm());
            stmt.setString(2, medico.getNome());
            stmt.setString(3, medico.getEspecialidade());
            stmt.setString(4, String.join(",", medico.getDiasAtendimento()));
            stmt.setTime(5, Time.valueOf(medico.getHorarioInicio()));
            stmt.setTime(6, Time.valueOf(medico.getHorarioFim()));
            stmt.setString(7, medico.getSalaAtendimento());
            
            stmt.executeUpdate();
        }
    }

    /**
     * Atualiza os dados de um médico existente no banco de dados.
     * Interage com as classes: Medico, DatabaseConnection.
     */
    public void update(Medico medico) throws SQLException {
        String sql = "UPDATE medicos SET nome = ?, especialidade = ?, dias_atendimento = ?, " +
                    "horario_inicio = ?, horario_fim = ?, sala_atendimento = ? WHERE crm = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, medico.getNome());
            stmt.setString(2, medico.getEspecialidade());
            stmt.setString(3, String.join(",", medico.getDiasAtendimento()));
            stmt.setTime(4, Time.valueOf(medico.getHorarioInicio()));
            stmt.setTime(5, Time.valueOf(medico.getHorarioFim()));
            stmt.setString(6, medico.getSalaAtendimento());
            stmt.setString(7, medico.getCrm());
            
            stmt.executeUpdate();
        }
    }

    /**
     * Remove um médico do banco de dados com base no seu CRM.
     * Interage com as classes: DatabaseConnection.
     */
    public void delete(String crm) throws SQLException {
        String sql = "DELETE FROM medicos WHERE crm = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, crm);
            stmt.executeUpdate();
        }
    }

    /**
     * Busca e retorna um médico específico pelo seu CRM.
     * Interage com as classes: Medico, DatabaseConnection.
     */
    public Medico findById(String crm) throws SQLException {
        String sql = "SELECT * FROM medicos WHERE crm = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, crm);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
                return null;
            }
        }
    }

    /**
     * Busca e retorna uma lista de todos os médicos cadastrados, ordenados por nome.
     * Interage com as classes: Medico, DatabaseConnection.
     */
    public List<Medico> findAll() throws SQLException {
        String sql = "SELECT * FROM medicos ORDER BY nome";
        List<Medico> medicos = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                medicos.add(mapResultSet(rs));
            }
        }
        
        return medicos;
    }

    /**
     * Mapeia uma linha do ResultSet para um objeto do tipo Medico.
     * Interage com as classes: ResultSet, Medico.
     */
    // Lógica: Converte os dados de uma linha da tabela 'medicos' em um objeto Medico.
    // O campo 'dias_atendimento', armazenado como texto, é dividido para formar a lista de dias.
    private Medico mapResultSet(ResultSet rs) throws SQLException {
        Medico medico = new Medico();
        medico.setCrm(rs.getString("crm"));
        medico.setNome(rs.getString("nome"));
        medico.setEspecialidade(rs.getString("especialidade"));
        
        String dias = rs.getString("dias_atendimento");
        if (dias != null && !dias.isEmpty()) {
            medico.setDiasAtendimento(Arrays.asList(dias.split(",")));
        }
        
        medico.setHorarioInicio(rs.getTime("horario_inicio").toLocalTime());
        medico.setHorarioFim(rs.getTime("horario_fim").toLocalTime());
        medico.setSalaAtendimento(rs.getString("sala_atendimento"));
        
        return medico;
    }
}