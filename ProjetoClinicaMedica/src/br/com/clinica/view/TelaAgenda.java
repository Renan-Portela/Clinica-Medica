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
    
    public TelaAgenda() {
        this.consultaDAO = new ConsultaDAO();
        initComponents();
        carregarConsultas();
    }
    
    private void initComponents() {
        setTitle("Agenda de Consultas");
        setSize(1000, 600);
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
        
        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Botoes
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