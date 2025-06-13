package br.com.clinica.view;

import br.com.clinica.dao.MedicoDAO;
import br.com.clinica.model.Medico;
import br.com.clinica.util.ValidadorCRM;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TelaMedicos extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private JTable table;
    private DefaultTableModel tableModel;
    private MedicoDAO medicoDAO;
    
    // Campos do formulario
    private JTextField txtCrm;
    private JTextField txtNome;
    private JComboBox<String> cbEspecialidade;
    private JTextField txtSala;
    private JSpinner spinnerInicio;
    private JSpinner spinnerFim;
    private JCheckBox[] checkDias;
    
    private Medico medicoSelecionado = null;
    
    public TelaMedicos() {
        this.medicoDAO = new MedicoDAO();
        initComponents();
        carregarMedicos();
    }
    
    private void initComponents() {
        setTitle("Gerenciamento de Medicos");
        setSize(900, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        add(mainPanel);
        
        // Painel do formulario
        JPanel formPanel = createFormPanel();
        mainPanel.add(formPanel, BorderLayout.NORTH);
        
        // Painel da tabela
        JPanel tablePanel = createTablePanel();
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        
        // Painel dos botoes
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setPreferredSize(new Dimension(0, 250));
        panel.setBorder(BorderFactory.createTitledBorder("Dados do Medico"));
        
        // CRM
        JLabel lblCrm = new JLabel("CRM:");
        lblCrm.setBounds(20, 30, 80, 25);
        panel.add(lblCrm);
        
        txtCrm = new JTextField();
        txtCrm.setBounds(100, 30, 150, 25);
        panel.add(txtCrm);
        
        // Nome
        JLabel lblNome = new JLabel("Nome:");
        lblNome.setBounds(280, 30, 80, 25);
        panel.add(lblNome);
        
        txtNome = new JTextField();
        txtNome.setBounds(360, 30, 200, 25);
        panel.add(txtNome);
        
        // Especialidade
        JLabel lblEspecialidade = new JLabel("Especialidade:");
        lblEspecialidade.setBounds(580, 30, 100, 25);
        panel.add(lblEspecialidade);
        
        String[] especialidades = {"Cardiologia", "Pediatria", "Ortopedia", "Dermatologia", 
                                  "Ginecologia", "Neurologia", "Oftalmologia"};
        cbEspecialidade = new JComboBox<>(especialidades);
        cbEspecialidade.setBounds(680, 30, 150, 25);
        cbEspecialidade.setEditable(true);
        panel.add(cbEspecialidade);
        
        // Sala
        JLabel lblSala = new JLabel("Sala:");
        lblSala.setBounds(20, 70, 80, 25);
        panel.add(lblSala);
        
        txtSala = new JTextField();
        txtSala.setBounds(100, 70, 100, 25);
        panel.add(txtSala);
        
        // Horarios
        JLabel lblInicio = new JLabel("Inicio:");
        lblInicio.setBounds(230, 70, 80, 25);
        panel.add(lblInicio);
        
        SpinnerDateModel modelInicio = new SpinnerDateModel();
        spinnerInicio = new JSpinner(modelInicio);
        JSpinner.DateEditor editorInicio = new JSpinner.DateEditor(spinnerInicio, "HH:mm");
        spinnerInicio.setEditor(editorInicio);
        spinnerInicio.setBounds(280, 70, 80, 25);
        spinnerInicio.setValue(java.sql.Time.valueOf(LocalTime.of(8, 0)));
        panel.add(spinnerInicio);
        
        JLabel lblFim = new JLabel("Fim:");
        lblFim.setBounds(380, 70, 80, 25);
        panel.add(lblFim);
        
        SpinnerDateModel modelFim = new SpinnerDateModel();
        spinnerFim = new JSpinner(modelFim);
        JSpinner.DateEditor editorFim = new JSpinner.DateEditor(spinnerFim, "HH:mm");
        spinnerFim.setEditor(editorFim);
        spinnerFim.setBounds(420, 70, 80, 25);
        spinnerFim.setValue(java.sql.Time.valueOf(LocalTime.of(17, 0)));
        panel.add(spinnerFim);
        
        // Dias da semana
        JLabel lblDias = new JLabel("Dias de Atendimento:");
        lblDias.setBounds(20, 110, 150, 25);
        panel.add(lblDias);
        
        String[] dias = {"Seg", "Ter", "Qua", "Qui", "Sex", "Sab", "Dom"};
        checkDias = new JCheckBox[7];
        
        for (int i = 0; i < dias.length; i++) {
            checkDias[i] = new JCheckBox(dias[i]);
            checkDias[i].setBounds(20 + (i * 80), 140, 70, 25);
            if (i < 5) checkDias[i].setSelected(true); // Seg a Sex marcados
            panel.add(checkDias[i]);
        }
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Lista de Medicos"));
        
        String[] colunas = {"CRM", "Nome", "Especialidade", "Horario", "Sala"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Nao editavel
            }
        };
        
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                carregarMedicoSelecionado();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        
        JButton btnNovo = new JButton("Novo");
        btnNovo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                limparFormulario();
            }
        });
        panel.add(btnNovo);
        
        JButton btnSalvar = new JButton("Salvar");
        btnSalvar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                salvarMedico();
            }
        });
        panel.add(btnSalvar);
        
        JButton btnExcluir = new JButton("Excluir");
        btnExcluir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                excluirMedico();
            }
        });
        panel.add(btnExcluir);
        
        JButton btnFechar = new JButton("Fechar");
        btnFechar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        panel.add(btnFechar);
        
        return panel;
    }
    
    private void carregarMedicos() {
        try {
            List<Medico> medicos = medicoDAO.findAll();
            tableModel.setRowCount(0);
            
            for (Medico medico : medicos) {
                String horario = medico.getHorarioInicio() + " as " + medico.getHorarioFim();
                Object[] row = {
                    medico.getCrm(),
                    medico.getNome(),
                    medico.getEspecialidade(),
                    horario,
                    medico.getSalaAtendimento()
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar medicos: " + e.getMessage());
        }
    }
    
    private void carregarMedicoSelecionado() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            String crm = (String) tableModel.getValueAt(selectedRow, 0);
            try {
                medicoSelecionado = medicoDAO.findById(crm);
                if (medicoSelecionado != null) {
                    preencherFormulario(medicoSelecionado);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao carregar medico: " + e.getMessage());
            }
        }
    }
    
    private void preencherFormulario(Medico medico) {
        txtCrm.setText(medico.getCrm());
        txtNome.setText(medico.getNome());
        cbEspecialidade.setSelectedItem(medico.getEspecialidade());
        txtSala.setText(medico.getSalaAtendimento());
        
        spinnerInicio.setValue(java.sql.Time.valueOf(medico.getHorarioInicio()));
        spinnerFim.setValue(java.sql.Time.valueOf(medico.getHorarioFim()));
        
        // Limpar checkboxes
        for (JCheckBox check : checkDias) {
            check.setSelected(false);
        }
        
        // Marcar dias correspondentes
        String[] diasCodigo = {"seg", "ter", "qua", "qui", "sex", "sab", "dom"};
        for (int i = 0; i < diasCodigo.length; i++) {
            if (medico.getDiasAtendimento().contains(diasCodigo[i])) {
                checkDias[i].setSelected(true);
            }
        }
    }
    
    private void limparFormulario() {
        txtCrm.setText("");
        txtNome.setText("");
        cbEspecialidade.setSelectedIndex(0);
        txtSala.setText("");
        spinnerInicio.setValue(java.sql.Time.valueOf(LocalTime.of(8, 0)));
        spinnerFim.setValue(java.sql.Time.valueOf(LocalTime.of(17, 0)));
        
        for (int i = 0; i < checkDias.length; i++) {
            checkDias[i].setSelected(i < 5);
        }
        
        medicoSelecionado = null;
        table.clearSelection();
    }
    
    private void salvarMedico() {
        try {
            // Validacoes
            if (txtCrm.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "CRM e obrigatorio!");
                return;
            }
            
            if (!ValidadorCRM.validar(txtCrm.getText().trim())) {
                JOptionPane.showMessageDialog(this, "CRM deve ter formato CRM seguido de numeros!");
                return;
            }
            
            if (txtNome.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nome e obrigatorio!");
                return;
            }
            
            if (txtSala.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Sala e obrigatoria!");
                return;
            }
            
            // Dias selecionados
            List<String> diasSelecionados = new ArrayList<>();
            String[] diasCodigo = {"seg", "ter", "qua", "qui", "sex", "sab", "dom"};
            
            for (int i = 0; i < checkDias.length; i++) {
                if (checkDias[i].isSelected()) {
                    diasSelecionados.add(diasCodigo[i]);
                }
            }
            
            if (diasSelecionados.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Selecione pelo menos um dia!");
                return;
            }
            
            // Horarios
            java.util.Date horaInicio = (java.util.Date) spinnerInicio.getValue();
            java.util.Date horaFim = (java.util.Date) spinnerFim.getValue();
            LocalTime inicio = LocalTime.ofNanoOfDay(horaInicio.getTime() * 1_000_000);
            LocalTime fim = LocalTime.ofNanoOfDay(horaFim.getTime() * 1_000_000);
            
            if (!inicio.isBefore(fim)) {
                JOptionPane.showMessageDialog(this, "Horario inicio deve ser menor que fim!");
                return;
            }
            
            // Criar ou atualizar medico
            Medico medico = new Medico();
            medico.setCrm(txtCrm.getText().trim().toUpperCase());
            medico.setNome(txtNome.getText().trim());
            medico.setEspecialidade((String) cbEspecialidade.getSelectedItem());
            medico.setSalaAtendimento(txtSala.getText().trim());
            medico.setHorarioInicio(inicio);
            medico.setHorarioFim(fim);
            medico.setDiasAtendimento(diasSelecionados);
            
            if (medicoSelecionado == null) {
                medicoDAO.save(medico);
                JOptionPane.showMessageDialog(this, "Medico cadastrado com sucesso!");
            } else {
                medicoDAO.update(medico);
                JOptionPane.showMessageDialog(this, "Medico atualizado com sucesso!");
            }
            
            carregarMedicos();
            limparFormulario();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar medico: " + e.getMessage());
        }
    }
    
    private void excluirMedico() {
        if (medicoSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um medico para excluir!");
            return;
        }
        
        int opcao = JOptionPane.showConfirmDialog(
            this,
            "Deseja realmente excluir o medico " + medicoSelecionado.getNome() + "?",
            "Confirmar Exclusao",
            JOptionPane.YES_NO_OPTION
        );
        
        if (opcao == JOptionPane.YES_OPTION) {
            try {
                medicoDAO.delete(medicoSelecionado.getCrm());
                JOptionPane.showMessageDialog(this, "Medico excluido com sucesso!");
                carregarMedicos();
                limparFormulario();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir medico: " + e.getMessage());
            }
        }
    }
}
