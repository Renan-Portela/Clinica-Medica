package br.com.clinica.dao;

import br.com.clinica.config.DatabaseConnection;
import br.com.clinica.model.Paciente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PacienteDAO {
    private Connection connection;

    /**
     * Construtor. Inicializa a conexão com o banco de dados.
     * Interage com as classes: DatabaseConnection.
     */
    public PacienteDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Salva um novo paciente no banco de dados.
     * Interage com as classes: Paciente, DatabaseConnection.
     */
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

    /**
     * Atualiza os dados de um paciente existente no banco de dados.
     * Interage com as classes: Paciente, DatabaseConnection.
     */
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

    /**
     * Remove um paciente do banco de dados com base no seu CPF.
     * Interage com as classes: DatabaseConnection.
     */
    public void delete(String cpf) throws SQLException {
        String sql = "DELETE FROM pacientes WHERE cpf = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cpf);
            stmt.executeUpdate();
        }
    }

    /**
     * Busca e retorna um paciente específico pelo seu CPF.
     * Interage com as classes: Paciente, DatabaseConnection.
     */
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

    /**
     * Busca e retorna uma lista de todos os pacientes cadastrados, ordenados por nome.
     * Interage com as classes: Paciente, DatabaseConnection.
     */
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

    /**
     * Mapeia uma linha do ResultSet para um objeto do tipo Paciente.
     * Interage com as classes: ResultSet, Paciente.
     */
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