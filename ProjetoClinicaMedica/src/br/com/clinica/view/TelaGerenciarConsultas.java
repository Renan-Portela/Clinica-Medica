package br.com.clinica.view;

import br.com.clinica.dao.ConsultaDAO;
import br.com.clinica.model.Consulta;
import br.com.clinica.service.ConsultaService;
import br.com.clinica.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.Optional;

/**
 * Tela para gerenciamento de consultas existentes.
 * Permite a visualização de todas as consultas em uma tabela, a edição de
 * observações e a alteração de status (ex: marcar como realizada ou cancelar).
 * Esta classe utiliza um ConsultaService para centralizar as regras de negócio.
 * Interage com as classes: ConsultaDAO, ConsultaService, Consulta, UITheme.
 */
public class TelaGerenciarConsultas extends JFrame implements UITheme {
    
    private static final long serialVersionUID = 1L;
    
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextArea txtObservacoes;
    private JButton btnSalvarObs;
    private JLabel lblInfoObservacoes;
    
    private ConsultaDAO consultaDAO;
    private ConsultaService consultaService;
    private Consulta consultaSelecionada;
    
    /**
     * Construtor padrão. Inicializa a tela para visualização geral.
     */
    public TelaGerenciarConsultas() {
        this.consultaDAO = new ConsultaDAO();
        this.consultaService = new ConsultaService();
        this.consultaSelecionada = null;
        
        inicializarInterface();
        carregarConsultas();
    }

    /**
     * CORREÇÃO: Construtor que recebe um ID para focar em uma consulta específica.
     * @param consultaId O ID da consulta a ser destacada.
     */
    public TelaGerenciarConsultas(Long consultaId) {
        this(); // Chama o construtor padrão para montar a interface

        // Tenta selecionar a consulta na tabela após ela ser carregada
        SwingUtilities.invokeLater(() -> {
            try {
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    if (tableModel.getValueAt(i, 0).equals(consultaId)) {
                        table.setRowSelectionInterval(i, i);
                        table.scrollRectToVisible(table.getCellRect(i, 0, true)); // Garante que a linha esteja visível
                        carregarConsultaSelecionada();
                        break;
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Erro ao carregar consulta específica: " + e.getMessage(),
                    "Erro de Sistema", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    private void inicializarInterface() {
        configurarJanela();
        
        JPanel headerPanel = criarHeader();
        JPanel centralPanel = criarPainelCentral();
        JPanel botoesPanel = criarPainelDeBotoes();
        
        getContentPane().add(headerPanel, BorderLayout.NORTH);
        getContentPane().add(centralPanel, BorderLayout.CENTER);
        getContentPane().add(botoesPanel, BorderLayout.SOUTH);
    }
    
    private void configurarJanela() {
        setTitle("Gerenciamento de Consultas - Sistema Clinica Medica");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(LIGHT_GRAY);
        setContentPane(contentPane);
    }
    
    private JPanel criarHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_BLUE);
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        JLabel lblTitulo = new JLabel("GERENCIAMENTO DE CONSULTAS");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(CLEAN_WHITE);
        
        headerPanel.add(lblTitulo, BorderLayout.WEST);
        return headerPanel;
    }

    private JPanel criarPainelCentral() {
        JPanel centralPanel = new JPanel(new BorderLayout());
        centralPanel.setBackground(LIGHT_GRAY);
        centralPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        centralPanel.add(criarTabelaConsultas(), BorderLayout.CENTER);
        centralPanel.add(criarPainelObservacoes(), BorderLayout.EAST);
        
        return centralPanel;
    }

    private JScrollPane criarTabelaConsultas() {
        String[] colunas = {"ID", "Data/Hora", "Médico", "Paciente", "Status", "Observações"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                carregarConsultaSelecionada();
            }
        });
        
        return new JScrollPane(table);
    }
    
    private JPanel criarPainelObservacoes() {
        JPanel observacoesPanel = new JPanel(new BorderLayout(10, 10));
        observacoesPanel.setBackground(CLEAN_WHITE);
        observacoesPanel.setPreferredSize(new Dimension(400, 0));
        observacoesPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Observações da Consulta"),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        lblInfoObservacoes = new JLabel("Selecione uma consulta para ver os detalhes", SwingConstants.CENTER);
        lblInfoObservacoes.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        
        txtObservacoes = new JTextArea();
        txtObservacoes.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtObservacoes.setLineWrap(true);
        txtObservacoes.setWrapStyleWord(true);
        txtObservacoes.setEnabled(false);
        
        btnSalvarObs = criarBotaoAcao("Salvar Observações", SUCCESS_GREEN, e -> salvarObservacoes());
        btnSalvarObs.setEnabled(false);
        
        observacoesPanel.add(lblInfoObservacoes, BorderLayout.NORTH);
        observacoesPanel.add(new JScrollPane(txtObservacoes), BorderLayout.CENTER);
        observacoesPanel.add(btnSalvarObs, BorderLayout.SOUTH);
        
        return observacoesPanel;
    }
    
    private JPanel criarPainelDeBotoes() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonPanel.setBackground(LIGHT_GRAY);

        buttonPanel.add(criarBotaoAcao("Novo Agendamento", SUCCESS_GREEN, e -> new TelaNovoAgendamento().setVisible(true)));
        buttonPanel.add(criarBotaoAcao("Ver Calendário", PRIMARY_BLUE, e -> new TelaAgendaCalendario().setVisible(true)));
        buttonPanel.add(criarBotaoAcao("Atualizar Lista", PRIMARY_BLUE, e -> carregarConsultas()));
        buttonPanel.add(criarBotaoAcao("Marcar Realizada", MEDICAL_GREEN, e -> marcarRealizada()));
        buttonPanel.add(criarBotaoAcao("Cancelar Consulta", ACCENT_RED, e -> cancelarConsulta()));
        buttonPanel.add(criarBotaoAcao("Fechar", DARK_TEXT, e -> dispose()));
        
        return buttonPanel;
    }
    
    private void carregarConsultas() {
        try {
            List<Consulta> consultas = consultaDAO.findAll();
            tableModel.setRowCount(0);
            
            for (Consulta consulta : consultas) {
                String observacoes = consulta.getObservacoes();
                if (observacoes != null && observacoes.length() > 50) {
                    observacoes = observacoes.substring(0, 47) + "...";
                }
                
                tableModel.addRow(new Object[]{
                    consulta.getId(),
                    consulta.getDataHorarioFormatado(),
                    consulta.getMedico().getNome(),
                    consulta.getPaciente().getNome(),
                    consulta.getStatus().getDescricao(),
                    observacoes != null ? observacoes : ""
                });
            }
            resetPainelObservacoes();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar consultas: " + e.getMessage(), "Erro de Sistema", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void carregarConsultaSelecionada() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            try {
                Long id = (Long) tableModel.getValueAt(selectedRow, 0);
                
                Optional<Consulta> optConsulta = consultaDAO.findAll().stream()
                    .filter(c -> c.getId().equals(id))
                    .findFirst();
                
                if (optConsulta.isPresent()) {
                    consultaSelecionada = optConsulta.get();
                    String obs = consultaSelecionada.getObservacoes();
                    txtObservacoes.setText(obs != null ? obs : "");
                    txtObservacoes.setEnabled(true);
                    btnSalvarObs.setEnabled(true);
                    lblInfoObservacoes.setText("Editando observações...");
                } else {
                    resetPainelObservacoes();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao carregar consulta: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                resetPainelObservacoes();
            }
        } else {
            resetPainelObservacoes();
        }
    }

    private void resetPainelObservacoes() {
        consultaSelecionada = null;
        txtObservacoes.setText("");
        txtObservacoes.setEnabled(false);
        btnSalvarObs.setEnabled(false);
        lblInfoObservacoes.setText("Selecione uma consulta para ver os detalhes");
    }
    
    private void salvarObservacoes() {
        if (consultaSelecionada == null) return;
        
        try {
            consultaSelecionada.setObservacoes(txtObservacoes.getText().trim());
            consultaDAO.update(consultaSelecionada);
            JOptionPane.showMessageDialog(this, "Observações salvas com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            carregarConsultas();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar observações: " + e.getMessage(), "Erro de Sistema", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void cancelarConsulta() {
        if (consultaSelecionada == null) {
            JOptionPane.showMessageDialog(this, "Selecione uma consulta para cancelar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Deseja realmente cancelar esta consulta?", "Confirmar Cancelamento", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                consultaService.cancelarConsulta(consultaSelecionada);
                JOptionPane.showMessageDialog(this, "Consulta cancelada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                carregarConsultas();
            } catch (IllegalStateException | HeadlessException | java.sql.SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro ao cancelar consulta: " + e.getMessage(), "Erro de Operação", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void marcarRealizada() {
        if (consultaSelecionada == null) {
            JOptionPane.showMessageDialog(this, "Selecione uma consulta para marcar como realizada.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String observacoes = JOptionPane.showInputDialog(this, "Digite as observações da consulta realizada:", "Observações da Consulta", JOptionPane.QUESTION_MESSAGE);
        
        if (observacoes != null) {
            try {
                consultaService.marcarComoRealizada(consultaSelecionada, observacoes);
                JOptionPane.showMessageDialog(this, "Consulta marcada como realizada!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                carregarConsultas();
            } catch (IllegalStateException | HeadlessException | java.sql.SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro ao marcar consulta: " + e.getMessage(), "Erro de Operação", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private RoundedButton criarBotaoAcao(String texto, Color cor, ActionListener acao) {
        RoundedButton botao = new RoundedButton(texto);
        botao.setFont(new Font("Segoe UI", Font.BOLD, 14));
        botao.setForeground(CLEAN_WHITE);
        botao.setBackground(cor);
        botao.setPreferredSize(new Dimension(180, 50));
        botao.addActionListener(acao);
        return botao;
    }
    
    private class RoundedButton extends JButton {
        public RoundedButton(String text) { super(text); setContentAreaFilled(false); setFocusPainted(false); setBorder(new EmptyBorder(5, 10, 5, 10)); }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (getModel().isPressed()) g2.setColor(getBackground().darker());
            else if (getModel().isRollover()) g2.setColor(getBackground().brighter());
            else g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
            super.paintComponent(g2);
            g2.dispose();
        }
    }
}