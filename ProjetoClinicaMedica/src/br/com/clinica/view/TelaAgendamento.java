package br.com.clinica.view;

import br.com.clinica.dao.*;
import br.com.clinica.model.*;
import br.com.clinica.service.EmailService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
        setSize(600, 500);
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
        mainPanel.add(cbMedicos);
        
        // Paciente
        JLabel lblPaciente = new JLabel("Paciente:");
        lblPaciente.setBounds(50, 110, 80, 25);
        lblPaciente.setFont(new Font("Arial", Font.BOLD, 12));
        mainPanel.add(lblPaciente);
        
        cbPacientes = new JComboBox<>();
        cbPacientes.setBounds(130, 110, 400, 25);
        mainPanel.add(cbPacientes);
        
        // Data
        JLabel lblData = new JLabel("Data:");
        lblData.setBounds(50, 150, 80, 25);
        lblData.setFont(new Font("Arial", Font.BOLD, 12));
        mainPanel.add(lblData);
        
        txtData = new JTextField();
        try {
            MaskFormatter dataMask = new MaskFormatter("##/##/####");
            dataMask.setPlaceholderCharacter('_');
            txtData = new JFormattedTextField(dataMask);
        } catch (ParseException e) {
            txtData = new JTextField(); // fallback
        }
        txtData.setBounds(130, 150, 120, 25);
        txtData.setToolTipText("dd/MM/yyyy");
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
    
    private void carregarHorarios() {
        String[] horarios = {
            "08:00", "08:30", "09:00", "09:30", "10:00", "10:30",
            "11:00", "11:30", "14:00", "14:30", "15:00", "15:30",
            "16:00", "16:30", "17:00"
        };
        
        for (String horario : horarios) {
            cbHorarios.addItem(horario);
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