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
    
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
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