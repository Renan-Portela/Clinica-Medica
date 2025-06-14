package br.com.clinica.view;

import br.com.clinica.config.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TelaPrincipal extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    // Teste conexao
                    DatabaseConnection.getInstance().getConnection();
                    
                    TelaPrincipal frame = new TelaPrincipal();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Erro ao iniciar sistema: " + e.getMessage());
                }
            }
        });
    }

    public TelaPrincipal() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 800, 600);
        setTitle("Sistema de Gestao Clinica Medica");
        setLocationRelativeTo(null);
        
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setBackground(new Color(240, 248, 255));
        setContentPane(contentPane);
        contentPane.setLayout(null);
        
        // Titulo
        JLabel lblTitulo = new JLabel("SISTEMA DE GESTAO CLINICA MEDICA");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(0, 102, 153));
        lblTitulo.setBounds(200, 30, 500, 40);
        contentPane.add(lblTitulo);
        
        JLabel lblSubtitulo = new JLabel("Gerencie consultas, pacientes e medicos");
        lblSubtitulo.setFont(new Font("Arial", Font.ITALIC, 14));
        lblSubtitulo.setForeground(new Color(100, 100, 100));
        lblSubtitulo.setBounds(280, 70, 300, 25);
        contentPane.add(lblSubtitulo);
        
        // Botoes principais
        JButton btnMedicos = new JButton("Gerenciar Medicos");
        btnMedicos.setBounds(558, 169, 200, 50);
        btnMedicos.setBackground(new Color(156, 39, 176));
        btnMedicos.setForeground(Color.WHITE);
        btnMedicos.setFont(new Font("Arial", Font.BOLD, 14));
        btnMedicos.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new TelaMedicos().setVisible(true);
            }
        });
        contentPane.add(btnMedicos);
        
        JButton btnPacientes = new JButton("Gerenciar Pacientes");
        btnPacientes.setBounds(558, 293, 200, 50);
        btnPacientes.setBackground(new Color(255, 152, 0));
        btnPacientes.setForeground(Color.WHITE);
        btnPacientes.setFont(new Font("Arial", Font.BOLD, 14));
        btnPacientes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new TelaPacientes().setVisible(true);
            }
        });
        contentPane.add(btnPacientes);
        
        JButton btnConsultas = new JButton("Agendar Consulta");
        btnConsultas.setBounds(558, 107, 200, 50);
        btnConsultas.setBackground(new Color(76, 175, 80));
        btnConsultas.setForeground(Color.WHITE);
        btnConsultas.setFont(new Font("Arial", Font.BOLD, 14));
        btnConsultas.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new TelaAgendamento().setVisible(true);
            }
        });
        contentPane.add(btnConsultas);
        
        JButton btnAgenda = new JButton("Visualizar Agenda");
        btnAgenda.setBounds(558, 231, 200, 50);
        btnAgenda.setBackground(new Color(33, 150, 243));
        btnAgenda.setForeground(Color.WHITE);
        btnAgenda.setFont(new Font("Arial", Font.BOLD, 14));
        btnAgenda.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	new TelaAgendaCalendario().setVisible(true);
            }
        });
        contentPane.add(btnAgenda);
        
        JButton btnRelatorios = new JButton("Relatorios");
        btnRelatorios.setBounds(558, 355, 200, 50);
        btnRelatorios.setBackground(new Color(96, 125, 139));
        btnRelatorios.setForeground(Color.WHITE);
        btnRelatorios.setFont(new Font("Arial", Font.BOLD, 14));
        btnRelatorios.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new TelaRelatorios().setVisible(true);
            }
        });
        contentPane.add(btnRelatorios);
        
        JButton btnSair = new JButton("Sair");
        btnSair.setBounds(558, 417, 200, 50);
        btnSair.setBackground(new Color(244, 67, 54));
        btnSair.setForeground(Color.WHITE);
        btnSair.setFont(new Font("Arial", Font.BOLD, 14));
        btnSair.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int opcao = JOptionPane.showConfirmDialog(
                    TelaPrincipal.this,
                    "Deseja realmente sair do sistema?",
                    "Confirmar Saida",
                    JOptionPane.YES_NO_OPTION
                );
                
                if (opcao == JOptionPane.YES_OPTION) {
                    DatabaseConnection.getInstance().closeConnection();
                    System.exit(0);
                }
            }
        });
        contentPane.add(btnSair);
        
        // Status
        JLabel lblStatus = new JLabel("Sistema conectado ao banco de dados MySQL");
        lblStatus.setFont(new Font("Arial", Font.PLAIN, 12));
        lblStatus.setForeground(new Color(76, 175, 80));
        lblStatus.setBounds(50, 520, 300, 20);
        contentPane.add(lblStatus);
        
        JLabel lblRodape = new JLabel("Sistema desenvolvido para Projeto Academico - Versao 1.0");
        lblRodape.setFont(new Font("Arial", Font.ITALIC, 12));
        lblRodape.setForeground(new Color(120, 120, 120));
        lblRodape.setBounds(250, 550, 400, 20);
        contentPane.add(lblRodape);
        
        JLabel lblImagem = new JLabel("");
        lblImagem.setIcon(new ImageIcon("/home/renan/Downloads/TelaInicial.PNG"));
        lblImagem.setBounds(82, 99, 410, 392);
        contentPane.add(lblImagem);
    }
}
