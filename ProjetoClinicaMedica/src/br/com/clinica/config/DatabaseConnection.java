package br.com.clinica.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/clinica_medica";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root";
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    
    private static DatabaseConnection instance;
    private Connection connection;

    /**
     * Construtor privado para implementar o padrão Singleton.
     * Tenta estabelecer a conexão inicial com o banco de dados.
     */
    private DatabaseConnection() {
        try {
            Class.forName(DRIVER);
            this.connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Conexao estabelecida!");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver MySQL nao encontrado: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Erro conexao banco: " + e.getMessage());
        }
    }

    /**
     * Fornece o ponto de acesso global para a única instância da classe.
     * Interage com as classes: DatabaseConnection.
     */
    // Lógica: Implementa o padrão Singleton. Se a instância ainda não foi criada,
    // invoca o construtor privado para inicializá-la, garantindo que exista apenas um objeto de conexão.
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Retorna a instância ativa da conexão com o banco de dados.
     * Interage com as classes: Connection.
     */
    // Lógica: Verifica se a conexão é nula ou foi fechada. Se uma dessas condições for verdadeira,
    // tenta restabelecer a conexão antes de retorná-la, garantindo sua disponibilidade.
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            }
        } catch (SQLException e) {
            System.err.println("Erro verificar conexao: " + e.getMessage());
        }
        return connection;
    }

    /**
     * Fecha a conexão com o banco de dados se ela estiver aberta.
     * Interage com as classes: Connection.
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Conexao fechada!");
            }
        } catch (SQLException e) {
            System.err.println("Erro fechar conexao: " + e.getMessage());
        }
    }
}