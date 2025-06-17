package br.com.clinica.view;

import br.com.clinica.dao.ConsultaDAO;
import br.com.clinica.model.Consulta;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TelaAgenda extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private JTable table;
    private DefaultTableModel tableModel;
    private ConsultaDAO consultaDAO;
    private JTextArea txtObservacoes; // NOVO: Campo para observações
    private JButton btnSalvarObs; // NOVO: Botão salvar observações
    private Consulta consultaSelecionada; // NOVO: Consulta atualmente selecionada
    
    public TelaAgenda() {
        this.consultaDAO = new ConsultaDAO();
        initComponents();
        carregarConsultas();
    }
    
    private void initComponents() {
        setTitle("Agenda de Consultas");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Tela cheia
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        add(mainPanel);
        
        // Titulo
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(0, 102, 153));
        headerPanel.setPreferredSize(new Dimension(0, 50));
        
        JLabel lblTitulo = new JLabel("AGENDA DE CONSULTAS");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitulo.setForeground(Color.WHITE);
        headerPanel.add(lblTitulo);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Painel principal dividido
        JPanel painelCentral = new JPanel();
        painelCentral.setLayout(new BorderLayout());
        mainPanel.add(painelCentral, BorderLayout.CENTER);
        
        // Tabela
        String[] colunas = {"ID", "Data/Hora", "Medico", "Paciente", "Status", "Observacoes"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Nao editavel
            }
        };
        
        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // NOVO: Listener para carregar observações quando selecionar consulta
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                carregarConsultaSelecionada();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        painelCentral.add(scrollPane, BorderLayout.CENTER);
        
        // NOVO: Painel de observações (lado direito)
        JPanel painelObservacoes = new JPanel();
        painelObservacoes.setLayout(new BorderLayout());
        painelObservacoes.setPreferredSize(new Dimension(400, 0));
        painelObservacoes.setBorder(BorderFactory.createTitledBorder("Observações da Consulta"));
        
        JLabel lblInfo = new JLabel("Selecione uma consulta para editar observações");
        lblInfo.setFont(new Font("Arial", Font.ITALIC, 12));
        lblInfo.setHorizontalAlignment(SwingConstants.CENTER);
        painelObservacoes.add(lblInfo, BorderLayout.NORTH);
        
        txtObservacoes = new JTextArea();
        txtObservacoes.setLineWrap(true);
        txtObservacoes.setWrapStyleWord(true);
        txtObservacoes.setFont(new Font("Arial", Font.PLAIN, 12));
        txtObservacoes.setEnabled(false); // Desabilitado até selecionar consulta
        
        JScrollPane scrollObs = new JScrollPane(txtObservacoes);
        scrollObs.setPreferredSize(new Dimension(0, 200));
        painelObservacoes.add(scrollObs, BorderLayout.CENTER);
        
        // Botão salvar observações
        JPanel painelBotaoObs = new JPanel();
        btnSalvarObs = new JButton("Salvar Observações");
        btnSalvarObs.setBackground(new Color(0, 102, 153));
        btnSalvarObs.setForeground(Color.WHITE);
        btnSalvarObs.setEnabled(false); // Desabilitado até selecionar consulta
        btnSalvarObs.addActionListener(e -> salvarObservacoes());
        painelBotaoObs.add(btnSalvarObs);
        
        painelObservacoes.add(painelBotaoObs, BorderLayout.SOUTH);
        
        painelCentral.add(painelObservacoes, BorderLayout.EAST);
        
        // Botoes principais
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        
        JButton btnAtualizar = new JButton("Atualizar");
        btnAtualizar.addActionListener(e -> carregarConsultas());
        buttonPanel.add(btnAtualizar);
        
        JButton btnCancelar = new JButton("Cancelar Consulta");
        btnCancelar.setBackground(new Color(244, 67, 54));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.addActionListener(e -> cancelarConsulta());
        buttonPanel.add(btnCancelar);
        
        JButton btnRealizar = new JButton("Marcar Realizada");
        btnRealizar.setBackground(new Color(76, 175, 80));
        btnRealizar.setForeground(Color.WHITE);
        btnRealizar.addActionListener(e -> marcarRealizada());
        buttonPanel.add(btnRealizar);
        
        JButton btnFechar = new JButton("Fechar");
        btnFechar.addActionListener(e -> dispose());
        buttonPanel.add(btnFechar);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    // NOVO: Método para carregar consulta selecionada
    private void carregarConsultaSelecionada() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            try {
                Long id = (Long) tableModel.getValueAt(selectedRow, 0);
                
                // Buscar consulta completa
                List<Consulta> todasConsultas = consultaDAO.findAll();
                for (Consulta consulta : todasConsultas) {
                    if (consulta.getId().equals(id)) {
                        consultaSelecionada = consulta;
                        
                        // Carregar observações no campo de texto
                        String obs = consulta.getObservacoes();
                        txtObservacoes.setText(obs != null ? obs : "");
                        
                        // Habilitar edição
                        txtObservacoes.setEnabled(true);
                        btnSalvarObs.setEnabled(true);
                        break;
                    }
                }
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao carregar consulta: " + e.getMessage());
            }
        } else {
            // Nenhuma consulta selecionada
            consultaSelecionada = null;
            txtObservacoes.setText("");
            txtObservacoes.setEnabled(false);
            btnSalvarObs.setEnabled(false);
        }
    }
    
    // NOVO: Método para salvar observações
    private void salvarObservacoes() {
        if (consultaSelecionada == null) {
            JOptionPane.showMessageDialog(this, "Nenhuma consulta selecionada!");
            return;
        }
        
        try {
            // Atualizar observações da consulta
            consultaSelecionada.setObservacoes(txtObservacoes.getText().trim());
            
            // Salvar no banco
            consultaDAO.update(consultaSelecionada);
            
            JOptionPane.showMessageDialog(this, "Observações salvas com sucesso!");
            
            // Atualizar tabela
            carregarConsultas();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar observações: " + e.getMessage());
        }
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
                
                Object[] row = {
                    consulta.getId(),
                    consulta.getDataHorarioFormatado(),
                    consulta.getMedico().getNome(),
                    consulta.getPaciente().getNome(),
                    consulta.getStatus().getDescricao(),
                    observacoes != null ? observacoes : ""
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar consultas: " + e.getMessage());
        }
    }
    
    private void cancelarConsulta() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma consulta!");
            return;
        }
        
        try {
            Long id = (Long) tableModel.getValueAt(selectedRow, 0);
            String status = (String) tableModel.getValueAt(selectedRow, 4);
            
            if (!"Agendada".equals(status)) {
                JOptionPane.showMessageDialog(this, "Apenas consultas agendadas podem ser canceladas!");
                return;
            }
            
            int opcao = JOptionPane.showConfirmDialog(
                this,
                "Deseja realmente cancelar esta consulta?",
                "Confirmar Cancelamento",
                JOptionPane.YES_NO_OPTION
            );
            
            if (opcao == JOptionPane.YES_OPTION) {
                // Buscar consulta e atualizar status
                List<Consulta> todasConsultas = consultaDAO.findAll();
                for (Consulta consulta : todasConsultas) {
                    if (consulta.getId().equals(id)) {
                        consulta.setStatus(Consulta.StatusConsulta.CANCELADA);
                        consultaDAO.update(consulta);
                        break;
                    }
                }
                
                JOptionPane.showMessageDialog(this, "Consulta cancelada com sucesso!");
                carregarConsultas();
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao cancelar consulta: " + e.getMessage());
        }
    }
    
    private void marcarRealizada() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma consulta!");
            return;
        }
        
        try {
            Long id = (Long) tableModel.getValueAt(selectedRow, 0);
            String status = (String) tableModel.getValueAt(selectedRow, 4);
            
            if (!"Agendada".equals(status)) {
                JOptionPane.showMessageDialog(this, "Apenas consultas agendadas podem ser marcadas como realizadas!");
                return;
            }
            
            String observacoes = JOptionPane.showInputDialog(
                this,
                "Digite as observacoes da consulta:",
                "Observacoes da Consulta",
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (observacoes != null) {
                // Buscar consulta e atualizar
                List<Consulta> todasConsultas = consultaDAO.findAll();
                for (Consulta consulta : todasConsultas) {
                    if (consulta.getId().equals(id)) {
                        consulta.setStatus(Consulta.StatusConsulta.REALIZADA);
                        consulta.setObservacoes(observacoes);
                        consultaDAO.update(consulta);
                        break;
                    }
                }
                
                JOptionPane.showMessageDialog(this, "Consulta marcada como realizada!");
                carregarConsultas();
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao marcar consulta: " + e.getMessage());
        }
    }
}