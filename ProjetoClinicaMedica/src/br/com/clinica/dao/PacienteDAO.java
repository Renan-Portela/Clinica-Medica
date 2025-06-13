package br.com.clinica.dao;

import br.com.clinica.config.DatabaseConnection;
import br.com.clinica.model.Paciente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PacienteDAO {
    private Connection connection;
    
    public PacienteDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }
    
    public void save(Paciente paciente) throws SQLException {
        String sql = "INSERT INTO pacientes (cpf, nome, data_nascimento, endereco, telefone, historico_medico) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, paciente.getCpf());
            stmt.setString(2, paciente.getNome());
            stmt.setDate(3, Date.valueOf(paciente.getDataNascimento()));
            stmt.setString(4, paciente.getEndereco());
            stmt.setString(5, paciente.getTelefone());
            stmt.setString(6, paciente.getHistoricoMedico());
            
            stmt.executeUpdate();
        }
    }
    
    public void update(Paciente paciente) throws SQLException {
        String sql = "UPDATE pacientes SET nome = ?, data_nascimento = ?, endereco = ?, " +
                    "telefone = ?, historico_medico = ? WHERE cpf = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, paciente.getNome());
            stmt.setDate(2, Date.valueOf(paciente.getDataNascimento()));
            stmt.setString(3, paciente.getEndereco());
            stmt.setString(4, paciente.getTelefone());
            stmt.setString(5, paciente.getHistoricoMedico());
            stmt.setString(6, paciente.getCpf());
            
            stmt.executeUpdate();
        }
    }
    
    public void delete(String cpf) throws SQLException {
        String sql = "DELETE FROM pacientes WHERE cpf = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cpf);
            stmt.executeUpdate();
        }
    }
    
    public Paciente findById(String cpf) throws SQLException {
        String sql = "SELECT * FROM pacientes WHERE cpf = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cpf);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
                return null;
            }
        }
    }
    
    public List<Paciente> findAll() throws SQLException {
        String sql = "SELECT * FROM pacientes ORDER BY nome";
        List<Paciente> pacientes = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                pacientes.add(mapResultSet(rs));
            }
        }
        
        return pacientes;
    }
    
    private Paciente mapResultSet(ResultSet rs) throws SQLException {
        Paciente paciente = new Paciente();
        paciente.setCpf(rs.getString("cpf"));
        paciente.setNome(rs.getString("nome"));
        paciente.setDataNascimento(rs.getDate("data_nascimento").toLocalDate());
        paciente.setEndereco(rs.getString("endereco"));
        paciente.setTelefone(rs.getString("telefone"));
        paciente.setHistoricoMedico(rs.getString("historico_medico"));
        
        return paciente;
    }
}