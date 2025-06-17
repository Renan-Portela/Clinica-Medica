package br.com.clinica.view;

import br.com.clinica.dao.*;
import br.com.clinica.model.*;
import br.com.clinica.service.EmailService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import javax.swing.text.MaskFormatter;
import java.text.ParseException;

public class TelaAgendamento extends JFrame {
    
    private static final long serialVersionUID = 1L;
    
    private JComboBox<Medico> cbMedicos;
    private JComboBox<Paciente> cbPacientes;
    private JTextField txtData;
    private JComboBox<String> cbHorarios;
    private JTextArea txtObservacoes;
    private JCheckBox chkEnviarEmail;
    
    private MedicoDAO medicoDAO;
    private PacienteDAO pacienteDAO;
    private ConsultaDAO consultaDAO;
    
    public TelaAgendamento() {
        this.medicoDAO = new MedicoDAO();
        this.pacienteDAO = new PacienteDAO();
        this.consultaDAO = new ConsultaDAO();
        
        initComponents();
        carregarDados();
    }
    
    private void initComponents() {
        setTitle("Agendamento de Consulta");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Tela cheia
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setBackground(new Color(250, 250, 250));
        add(mainPanel);
        
        // Titulo
        JLabel lblTitulo = new JLabel("NOVO AGENDAMENTO");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setForeground(new Color(0, 102, 153));
        lblTitulo.setBounds(200, 20, 300, 25);
        mainPanel.add(lblTitulo);
        
        // Medico
        JLabel lblMedico = new JLabel("Medico:");
        lblMedico.setBounds(50, 70, 80, 25);
        lblMedico.setFont(new Font("Arial", Font.BOLD, 12));
        mainPanel.add(lblMedico);
        
        cbMedicos = new JComboBox<>();
        cbMedicos.setBounds(130, 70, 400, 25);
        cbMedicos.addActionListener(e -> carregarHorariosDisponiveis());
        mainPanel.add(cbMedicos);
        
        // Paciente
        JLabel lblPaciente = new JLabel("Paciente:");
        lblPaciente.setBounds(50, 110, 80, 25);
        lblPaciente.setFont(new Font("Arial", Font.BOLD, 12));
        mainPanel.add(lblPaciente);
        
        cbPacientes = new JComboBox<>();
        cbPacientes.setBounds(130, 110, 400, 25);
        mainPanel.add(cbPacientes);
        
        // Botão Histórico
        JButton btnHistorico = new JButton("Histórico");
        btnHistorico.setBounds(540, 110, 90, 25);
        btnHistorico.setBackground(new Color(0, 102, 153));
        btnHistorico.setForeground(Color.WHITE);
        btnHistorico.setFont(new Font("Arial", Font.BOLD, 10));
        btnHistorico.addActionListener(e -> abrirHistoricoPaciente());
        mainPanel.add(btnHistorico);
        
        // Data
        JLabel lblData = new JLabel("Data:");
        lblData.setBounds(50, 150, 80, 25);
        lblData.setFont(new Font("Arial", Font.BOLD, 12));
        mainPanel.add(lblData);
        
        try {
            MaskFormatter dataMask = new MaskFormatter("##/##/####");
            dataMask.setValidCharacters("0123456789");
            txtData = new JFormattedTextField(dataMask);
        } catch (ParseException e) {
            txtData = new JTextField();
        }
        
        txtData.setBounds(130, 150, 120, 25);
        txtData.setForeground(Color.BLACK);
        txtData.setBackground(Color.WHITE);
        txtData.setFont(new Font("Arial", Font.PLAIN, 12));
        txtData.setToolTipText("dd/MM/yyyy");
        // Listener para atualizar horários quando data for digitada
        txtData.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                if (txtData.getText().length() == 10) { // dd/MM/yyyy completo
                    carregarHorariosDisponiveis();
                }
            }
        });
        mainPanel.add(txtData);
        
        // Horario
        JLabel lblHorario = new JLabel("Horario:");
        lblHorario.setBounds(270, 150, 80, 25);
        lblHorario.setFont(new Font("Arial", Font.BOLD, 12));
        mainPanel.add(lblHorario);
        
        cbHorarios = new JComboBox<>();
        cbHorarios.setBounds(350, 150, 100, 25);
        carregarHorarios();
        mainPanel.add(cbHorarios);
        
        // Observacoes
        JLabel lblObs = new JLabel("Observacoes:");
        lblObs.setBounds(50, 190, 100, 25);
        lblObs.setFont(new Font("Arial", Font.BOLD, 12));
        mainPanel.add(lblObs);
        
        txtObservacoes = new JTextArea();
        txtObservacoes.setLineWrap(true);
        JScrollPane scrollObs = new JScrollPane(txtObservacoes);
        scrollObs.setBounds(50, 220, 480, 80);
        mainPanel.add(scrollObs);
        
        // Checkbox email
        chkEnviarEmail = new JCheckBox("Enviar email de confirmacao");
        chkEnviarEmail.setBounds(50, 320, 250, 25);
        chkEnviarEmail.setSelected(true);
        mainPanel.add(chkEnviarEmail);
        
        // Botoes
        JButton btnConfirmar = new JButton("Confirmar Agendamento");
        btnConfirmar.setBounds(150, 370, 180, 35);
        btnConfirmar.setBackground(new Color(76, 175, 80));
        btnConfirmar.setForeground(Color.WHITE);
        btnConfirmar.setFont(new Font("Arial", Font.BOLD, 12));
        btnConfirmar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmarAgendamento();
            }
        });
        mainPanel.add(btnConfirmar);
        
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setBounds(350, 370, 100, 35);
        btnCancelar.setBackground(new Color(244, 67, 54));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.addActionListener(e -> dispose());
        mainPanel.add(btnCancelar);
    }
    
    private void carregarDados() {
        try {
            // Carregar medicos
            List<Medico> medicos = medicoDAO.findAll();
            for (Medico medico : medicos) {
                cbMedicos.addItem(medico);
            }
            
            // Carregar pacientes
            List<Paciente> pacientes = pacienteDAO.findAll();
            for (Paciente paciente : pacientes) {
                cbPacientes.addItem(paciente);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar dados: " + e.getMessage());
        }
    }
    
    // MÉTODO ORIGINAL MANTIDO
    private void carregarHorarios() {
        String[] horarios = {
            "08:00", "08:30", "09:00", "09:30", "10:00", "10:30",
            "11:00", "11:30", "14:00", "14:30", "15:00", "15:30",
            "16:00", "16:30", "17:00"
        };
        
        cbHorarios.removeAllItems();
        for (String horario : horarios) {
            cbHorarios.addItem(horario);
        }
    }
    
    // Horários ocupados SOMEM do combobox
    private void carregarHorariosDisponiveis() {
        cbHorarios.removeAllItems();
        
        // Horários base
        String[] todosHorarios = {
            "08:00", "08:30", "09:00", "09:30", "10:00", "10:30",
            "11:00", "11:30", "14:00", "14:30", "15:00", "15:30",
            "16:00", "16:30", "17:00"
        };
        
        // Se não tem médico ou data, mostrar todos
        if (cbMedicos.getSelectedItem() == null || txtData.getText().trim().isEmpty()) {
            for (String horario : todosHorarios) {
                cbHorarios.addItem(horario);
            }
            return;
        }
        
        try {
            // Pegar médico e data selecionados
            Medico medico = (Medico) cbMedicos.getSelectedItem();
            LocalDate data = LocalDate.parse(txtData.getText().trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            
            // Buscar consultas agendadas
            List<Consulta> consultas = consultaDAO.findAll();
            List<String> horariosOcupados = new ArrayList<>();
            
            for (Consulta consulta : consultas) {
                if (consulta.getMedico().getCrm().equals(medico.getCrm()) && 
                    consulta.getDataHorario().toLocalDate().equals(data)) {
                    String horarioOcupado = consulta.getDataHorario().toLocalTime().toString();
                    horariosOcupados.add(horarioOcupado);
                }
            }
            
            // Adicionar apenas horários livres - HORÁRIOS OCUPADOS SOMEM!
            for (String horario : todosHorarios) {
                if (!horariosOcupados.contains(horario)) {
                    cbHorarios.addItem(horario);
                }
            }
            
        } catch (Exception e) {
            // Se der erro, mostrar todos os horários
            for (String horario : todosHorarios) {
                cbHorarios.addItem(horario);
            }
        }
    }
    
    // Método para abrir histórico do paciente
    private void abrirHistoricoPaciente() {
        Paciente pacienteSelecionado = (Paciente) cbPacientes.getSelectedItem();
        
        if (pacienteSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um paciente primeiro!");
            return;
        }
        
        try {
            // Buscar consultas do paciente
            List<Consulta> todasConsultas = consultaDAO.findAll();
            List<Consulta> consultasPaciente = new ArrayList<>();
            
            for (Consulta consulta : todasConsultas) {
                if (consulta.getPaciente().getCpf().equals(pacienteSelecionado.getCpf())) {
                    consultasPaciente.add(consulta);
                }
            }
            
            if (consultasPaciente.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Nenhuma consulta encontrada para: " + pacienteSelecionado.getNome());
                return;
            }
            
            // Criar popup com histórico
            mostrarHistoricoPopup(pacienteSelecionado, consultasPaciente);
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar histórico: " + ex.getMessage());
        }
    }
    
    // MODIFICADO: Popup com histórico do paciente - AGORA COM EDIÇÃO
    private void mostrarHistoricoPopup(Paciente paciente, List<Consulta> consultas) {
        JDialog dialog = new JDialog(this, "Histórico - " + paciente.getNome(), true);
        dialog.setSize(900, 600);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout());
        
        // Header
        JLabel lblHeader = new JLabel("Histórico de Consultas - " + paciente.getNome());
        lblHeader.setFont(new Font("Arial", Font.BOLD, 16));
        lblHeader.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(lblHeader, BorderLayout.NORTH);
        
        // Info sobre edição
        JLabel lblInfo = new JLabel("Dica: Clique duas vezes na coluna 'Observações' para editar");
        lblInfo.setFont(new Font("Arial", Font.ITALIC, 11));
        lblInfo.setForeground(Color.GRAY);
        lblInfo.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 10));
        
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.add(lblHeader, BorderLayout.NORTH);
        infoPanel.add(lblInfo, BorderLayout.SOUTH);
        panel.add(infoPanel, BorderLayout.NORTH);
        
        // Tabela EDITÁVEL
        String[] colunas = {"ID", "Data/Hora", "Médico", "Status", "Observações"};
        DefaultTableModel modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Apenas coluna de Observações é editável
            }
        };
        
        // Preencher dados
        for (Consulta consulta : consultas) {
            Object[] linha = {
                consulta.getId(),
                consulta.getDataHorarioFormatado(),
                consulta.getMedico().getNome(),
                consulta.getStatus().getDescricao(),
                consulta.getObservacoes() != null ? consulta.getObservacoes() : ""
            };
            modeloTabela.addRow(linha);
        }
        
        JTable tabela = new JTable(modeloTabela);
        tabela.setRowHeight(30);
        tabela.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Configurar larguras das colunas
        tabela.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        tabela.getColumnModel().getColumn(1).setPreferredWidth(150); // Data
        tabela.getColumnModel().getColumn(2).setPreferredWidth(150); // Médico
        tabela.getColumnModel().getColumn(3).setPreferredWidth(100); // Status
        tabela.getColumnModel().getColumn(4).setPreferredWidth(300); // Observações
        
        JScrollPane scroll = new JScrollPane(tabela);
        panel.add(scroll, BorderLayout.CENTER);
        
        // Botões
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        // NOVO: Botão Salvar Modificações
        JButton btnSalvar = new JButton("Salvar Modificações");
        btnSalvar.setBackground(new Color(76, 175, 80));
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.setFont(new Font("Arial", Font.BOLD, 12));
        btnSalvar.addActionListener(e -> {
            salvarModificacoesHistorico(dialog, tabela, modeloTabela, consultas);
        });
        buttonPanel.add(btnSalvar);
        
        JButton btnVerAgenda = new JButton("Ver Agenda Completa");
        btnVerAgenda.setBackground(new Color(0, 102, 153));
        btnVerAgenda.setForeground(Color.WHITE);
        btnVerAgenda.addActionListener(e -> {
            dialog.dispose();
            TelaAgenda telaAgenda = new TelaAgenda();
            telaAgenda.setVisible(true);
        });
        buttonPanel.add(btnVerAgenda);
        
        JButton btnFechar = new JButton("Fechar");
        btnFechar.addActionListener(e -> dialog.dispose());
        buttonPanel.add(btnFechar);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    // NOVO: Método para salvar modificações do histórico
    private void salvarModificacoesHistorico(JDialog dialog, JTable tabela, DefaultTableModel modelo, List<Consulta> consultas) {
        // Confirmação antes de salvar
        int opcao = JOptionPane.showConfirmDialog(
            dialog,
            "Deseja realmente salvar todas as modificações realizadas?",
            "Confirmar Alterações",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (opcao != JOptionPane.YES_OPTION) {
            return;
        }
        
        try {
            boolean algumaMudanca = false;
            
            // Percorrer todas as linhas da tabela
            for (int i = 0; i < modelo.getRowCount(); i++) {
                Long idConsulta = (Long) modelo.getValueAt(i, 0);
                String novasObservacoes = (String) modelo.getValueAt(i, 4);
                
                // Encontrar a consulta correspondente
                for (Consulta consulta : consultas) {
                    if (consulta.getId().equals(idConsulta)) {
                        String observacoesAntigas = consulta.getObservacoes();
                        
                        // Verificar se houve mudança
                        boolean mudou = false;
                        if (observacoesAntigas == null && novasObservacoes != null && !novasObservacoes.trim().isEmpty()) {
                            mudou = true;
                        } else if (observacoesAntigas != null && !observacoesAntigas.equals(novasObservacoes)) {
                            mudou = true;
                        }
                        
                        if (mudou) {
                            consulta.setObservacoes(novasObservacoes != null ? novasObservacoes.trim() : "");
                            consultaDAO.update(consulta);
                            algumaMudanca = true;
                        }
                        break;
                    }
                }
            }
            
            if (algumaMudanca) {
                JOptionPane.showMessageDialog(dialog, 
                    "Modificações salvas com sucesso!",
                    "Sucesso", 
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(dialog, 
                    "Nenhuma modificação foi detectada.",
                    "Informação", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(dialog, 
                "Erro ao salvar modificações: " + ex.getMessage(),
                "Erro", 
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    private void confirmarAgendamento() {
        try {
            // Validacoes
            if (cbMedicos.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Selecione um medico!");
                return;
            }
            
            if (cbPacientes.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Selecione um paciente!");
                return;
            }
            
            if (txtData.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Informe a data!");
                return;
            }
            
            // Parsear data e horario
            LocalDate data;
            try {
                String dataTexto = txtData.getText();
                String dataLimpa = dataTexto != null ? dataTexto.trim() : "";
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                data = LocalDate.parse(dataLimpa, formatter);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Data invalida! Use formato dd/MM/yyyy");
                return;
            }
            
            LocalTime horario = LocalTime.parse((String) cbHorarios.getSelectedItem());
            LocalDateTime dataHorario = LocalDateTime.of(data, horario);
            
            // Verificar se data nao e no passado
            if (dataHorario.isBefore(LocalDateTime.now())) {
                JOptionPane.showMessageDialog(this, "Nao e possivel agendar no passado!");
                return;
            }
            
            // Criar consulta
            Medico medico = (Medico) cbMedicos.getSelectedItem();
            Paciente paciente = (Paciente) cbPacientes.getSelectedItem();
            
            Consulta consulta = new Consulta(medico, paciente, dataHorario);
            consulta.setObservacoes(txtObservacoes.getText().trim());
            
            // Salvar consulta
            consultaDAO.save(consulta);
            
            // Enviar email se marcado
            if (chkEnviarEmail.isSelected()) {
                String emailPaciente = JOptionPane.showInputDialog(this, 
                    "Digite o email do paciente:", 
                    "Email para Confirmacao", 
                    JOptionPane.QUESTION_MESSAGE);
                
                if (emailPaciente != null && !emailPaciente.trim().isEmpty()) {
                    boolean emailEnviado = EmailService.enviarConfirmacaoConsulta(
                        emailPaciente,
                        paciente.getNome(),
                        medico.getNome(),
                        consulta.getDataHorarioFormatado()
                    );
                    
                    if (emailEnviado) {
                        JOptionPane.showMessageDialog(this, 
                            "Consulta agendada com sucesso!\nEmail de confirmacao enviado para: " + emailPaciente,
                            "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, 
                            "Consulta agendada com sucesso!\nPorem houve erro ao enviar email.",
                            "Aviso", JOptionPane.WARNING_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Consulta agendada com sucesso!\nEmail nao enviado (email vazio).",
                        "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Consulta agendada com sucesso!",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            }
            
            // Limpar campos apos sucesso
            limparCampos();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Erro ao agendar consulta: " + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void limparCampos() {
        cbMedicos.setSelectedIndex(-1);
        cbPacientes.setSelectedIndex(-1);
        txtData.setText("");
        cbHorarios.setSelectedIndex(0);
        txtObservacoes.setText("");
        chkEnviarEmail.setSelected(true);
    }
}