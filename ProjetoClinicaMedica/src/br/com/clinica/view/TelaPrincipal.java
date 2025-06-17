package br.com.clinica.view;

import br.com.clinica.config.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class TelaPrincipal extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JLabel lblTitulo, lblSubtitulo, lblStatus, lblRodape, lblImagem;
    private JButton btnMedicos, btnPacientes, btnConsultas, btnAgenda, btnRelatorios, btnSair;

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
        setTitle("Sistema de Gestao Clinica Medica");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Tela cheia
        setLocationRelativeTo(null);
        
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setBackground(new Color(240, 248, 255));
        setContentPane(contentPane);
        contentPane.setLayout(null);
        
        // Titulo
        lblTitulo = new JLabel("SISTEMA DE GESTAO CLINICA MEDICA");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(0, 102, 153));
        contentPane.add(lblTitulo);
        
        lblSubtitulo = new JLabel("Gerencie consultas, pacientes e medicos");
        lblSubtitulo.setFont(new Font("Arial", Font.ITALIC, 14));
        lblSubtitulo.setForeground(new Color(100, 100, 100));
        contentPane.add(lblSubtitulo);
        
        // Botoes principais
        btnMedicos = new JButton("Gerenciar Medicos");
        btnMedicos.setBackground(new Color(156, 39, 176));
        btnMedicos.setForeground(Color.WHITE);
        btnMedicos.setFont(new Font("Arial", Font.BOLD, 14));
        btnMedicos.addActionListener(e -> new TelaMedicos().setVisible(true));
        contentPane.add(btnMedicos);
        
        btnPacientes = new JButton("Gerenciar Pacientes");
        btnPacientes.setBackground(new Color(255, 152, 0));
        btnPacientes.setForeground(Color.WHITE);
        btnPacientes.setFont(new Font("Arial", Font.BOLD, 14));
        btnPacientes.addActionListener(e -> new TelaPacientes().setVisible(true));
        contentPane.add(btnPacientes);
        
        btnConsultas = new JButton("Agendar Consulta");
        btnConsultas.setBackground(new Color(76, 175, 80));
        btnConsultas.setForeground(Color.WHITE);
        btnConsultas.setFont(new Font("Arial", Font.BOLD, 14));
        btnConsultas.addActionListener(e -> new TelaAgendamento().setVisible(true));
        contentPane.add(btnConsultas);
        
        btnAgenda = new JButton("Visualizar Agenda");
        btnAgenda.setBackground(new Color(33, 150, 243));
        btnAgenda.setForeground(Color.WHITE);
        btnAgenda.setFont(new Font("Arial", Font.BOLD, 14));
        btnAgenda.addActionListener(e -> new TelaAgendaCalendario().setVisible(true));
        contentPane.add(btnAgenda);
        
        btnRelatorios = new JButton("Relatorios");
        btnRelatorios.setBackground(new Color(96, 125, 139));
        btnRelatorios.setForeground(Color.WHITE);
        btnRelatorios.setFont(new Font("Arial", Font.BOLD, 14));
        btnRelatorios.addActionListener(e -> new TelaRelatorios().setVisible(true));
        contentPane.add(btnRelatorios);
        
        btnSair = new JButton("Sair");
        btnSair.setBackground(new Color(244, 67, 54));
        btnSair.setForeground(Color.WHITE);
        btnSair.setFont(new Font("Arial", Font.BOLD, 14));
        btnSair.addActionListener(e -> {
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
        });
        contentPane.add(btnSair);
        
        // Status
        lblStatus = new JLabel("Sistema conectado ao banco de dados MySQL");
        lblStatus.setFont(new Font("Arial", Font.PLAIN, 12));
        lblStatus.setForeground(new Color(76, 175, 80));
        contentPane.add(lblStatus);
        
        lblRodape = new JLabel("Sistema desenvolvido para Projeto Academico - Versao 1.0");
        lblRodape.setFont(new Font("Arial", Font.ITALIC, 12));
        lblRodape.setForeground(new Color(120, 120, 120));
        contentPane.add(lblRodape);
        
        lblImagem = new JLabel("");
        contentPane.add(lblImagem);
        
        // Listener para responsividade - reposiciona componentes quando janela muda de tamanho
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent e) {
                reposicionarComponentes();
            }
        });
        
        // Posicionamento inicial após janela criada
        SwingUtilities.invokeLater(() -> reposicionarComponentes());
    }

    // Método para reposicionar todos os componentes responsivamente
    private void reposicionarComponentes() {
        int width = getWidth();
        int height = getHeight();
        
        // Títulos centralizados
        lblTitulo.setBounds(width/4, 30, width/2, 40);
        lblSubtitulo.setBounds(width/3, 80, width/3, 25);
        
        // Botões lado direito
        btnConsultas.setBounds(width-250, 120, 200, 50);
        btnMedicos.setBounds(width-250, 180, 200, 50);
        btnAgenda.setBounds(width-250, 240, 200, 50);
        btnPacientes.setBounds(width-250, 300, 200, 50);
        btnRelatorios.setBounds(width-250, 360, 200, 50);
        btnSair.setBounds(width-250, 420, 200, 50);
        
        // Status rodapé
        lblStatus.setBounds(50, height-80, 300, 20);
        lblRodape.setBounds(width/3, height-50, 400, 20);
        
        // Área imagem
        lblImagem.setBounds(50, 120, width-350, height-200);
        
        // Forçar repaint
        repaint();
    }
}