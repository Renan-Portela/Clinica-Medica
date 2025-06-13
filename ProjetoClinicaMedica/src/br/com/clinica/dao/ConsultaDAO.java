package br.com.clinica.dao;

import br.com.clinica.config.DatabaseConnection;
import br.com.clinica.model.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ConsultaDAO {
    private Connection connection;
    private MedicoDAO medicoDAO;
    private PacienteDAO pacienteDAO;
    
    public ConsultaDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
        this.medicoDAO = new MedicoDAO();
        this.pacienteDAO = new PacienteDAO();
    }
    
    public void save(Consulta consulta) throws SQLException {
        String sql = "INSERT INTO consultas (medico_crm, paciente_cpf, data_horario, observacoes, status) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, consulta.getMedico().getCrm());
            stmt.setString(2, consulta.getPaciente().getCpf());
            stmt.setTimestamp(3, Timestamp.valueOf(consulta.getDataHorario()));
            stmt.setString(4, consulta.getObservacoes());
            stmt.setString(5, consulta.getStatus().name());
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    consulta.setId(generatedKeys.getLong(1));
                }
            }
        }
    }
    
    public void update(Consulta consulta) throws SQLException {
        String sql = "UPDATE consultas SET medico_crm = ?, paciente_cpf = ?, data_horario = ?, " +
                    "observacoes = ?, status = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, consulta.getMedico().getCrm());
            stmt.setString(2, consulta.getPaciente().getCpf());
            stmt.setTimestamp(3, Timestamp.valueOf(consulta.getDataHorario()));
            stmt.setString(4, consulta.getObservacoes());
            stmt.setString(5, consulta.getStatus().name());
            stmt.setLong(6, consulta.getId());
            
            stmt.executeUpdate();
        }
    }
    
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM consultas WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }
    
    public List<Consulta> findAll() throws SQLException {
        String sql = "SELECT * FROM consultas ORDER BY data_horario DESC";
        List<Consulta> consultas = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                consultas.add(mapResultSet(rs));
            }
        }
        
        return consultas;
    }
    
    public List<Consulta> findByMedico(String crmMedico) throws SQLException {
        String sql = "SELECT * FROM consultas WHERE medico_crm = ? ORDER BY data_horario DESC";
        List<Consulta> consultas = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, crmMedico);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    consultas.add(mapResultSet(rs));
                }
            }
        }
        
        return consultas;
    }
    
    public List<Consulta> findByPaciente(String cpfPaciente) throws SQLException {
        String sql = "SELECT * FROM consultas WHERE paciente_cpf = ? ORDER BY data_horario DESC";
        List<Consulta> consultas = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cpfPaciente);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    consultas.add(mapResultSet(rs));
                }
            }
        }
        
        return consultas;
    }
    
    private Consulta mapResultSet(ResultSet rs) throws SQLException {
        Consulta consulta = new Consulta();
        consulta.setId(rs.getLong("id"));
        consulta.setDataHorario(rs.getTimestamp("data_horario").toLocalDateTime());
        consulta.setObservacoes(rs.getString("observacoes"));
        consulta.setStatus(Consulta.StatusConsulta.valueOf(rs.getString("status")));
        
        // Buscar médico e paciente
        try {
            Medico medico = medicoDAO.findById(rs.getString("medico_crm"));
            Paciente paciente = pacienteDAO.findById(rs.getString("paciente_cpf"));
            
            consulta.setMedico(medico);
            consulta.setPaciente(paciente);
        } catch (SQLException e) {
            System.err.println("Erro buscar medico/paciente: " + e.getMessage());
        }
        
        return consulta;
    }
}
