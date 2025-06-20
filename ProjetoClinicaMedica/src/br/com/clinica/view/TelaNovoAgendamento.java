package br.com.clinica.view;

import br.com.clinica.dao.*;
import br.com.clinica.model.*;
import br.com.clinica.service.ConsultaService;
import br.com.clinica.service.EmailService;
import br.com.clinica.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Tela para a criação de um novo agendamento de consulta.
 * Esta tela é um formulário que permite ao usuário selecionar um médico, um paciente,
 * uma data e um horário disponível para agendar uma nova consulta.
 * Interage com as camadas de DAO para carregar dados iniciais (médicos e pacientes)
 * e com a camada de Serviço (ConsultaService) para executar a lógica de negócio.
 */
public class TelaNovoAgendamento extends JFrame implements UITheme {

    private static final long serialVersionUID = 1L;

    // Componentes da interface
    private JComboBox<Medico> cbMedicos;
    private JComboBox<Paciente> cbPacientes;
    private JFormattedTextField txtData;
    private JComboBox<String> cbHorarios;
    private JCheckBox chkEnviarEmail;
    private JTextArea txtObservacoes;

    // Camadas de dados e serviço
    private MedicoDAO medicoDAO;
    private PacienteDAO pacienteDAO;
    private ConsultaDAO consultaDAO;
    private ConsultaService consultaService;

    /**
     * Construtor da tela. Inicializa as dependências e a interface.
     */
    public TelaNovoAgendamento() {
        this.medicoDAO = new MedicoDAO();
        this.pacienteDAO = new PacienteDAO();
        this.consultaDAO = new ConsultaDAO();
        this.consultaService = new ConsultaService();

        inicializarInterface();
        carregarDadosIniciais();
    }

    /**
     * Ponto de entrada para a construção da interface gráfica da tela.
     */
    private void inicializarInterface() {
        configurarJanela();
        
        JPanel headerPanel = criarHeader();
        JPanel formPanel = criarFormulario();
        JPanel buttonPanel = criarPainelDeBotoes();

        getContentPane().add(headerPanel, BorderLayout.NORTH);
        getContentPane().add(formPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Configura as propriedades principais da janela (JFrame).
     */
    private void configurarJanela() {
        setTitle("Novo Agendamento de Consulta - Sistema Clínica");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(LIGHT_GRAY);
        setContentPane(contentPane);
    }

    /**
     * Cria o cabeçalho da tela.
     * @return O painel do cabeçalho.
     */
    private JPanel criarHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_BLUE);
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel lblTitulo = new JLabel("NOVO AGENDAMENTO DE CONSULTA");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(CLEAN_WHITE);

        headerPanel.add(lblTitulo, BorderLayout.WEST);
        return headerPanel;
    }

    /**
     * Cria o painel do formulário com todos os campos de entrada de dados.
     * @return O painel do formulário.
     */
    private JPanel criarFormulario() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CLEAN_WHITE);
        formPanel.setBorder(new EmptyBorder(20, 40, 20, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Linha 1: Médico e Paciente
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0.5;
        formPanel.add(criarCampo("Médico:", cbMedicos = new JComboBox<>()), gbc);

        gbc.gridx = 1;
        formPanel.add(criarCampo("Paciente:", cbPacientes = new JComboBox<>()), gbc);

        // Linha 2: Data e Horário
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.weightx = 0; // Não esticar
        formPanel.add(criarCampo("Data da Consulta:", txtData = criarCampoTextoFormatado("##/##/####")), gbc);
        
        gbc.gridx = 1;
        formPanel.add(criarCampo("Horário:", cbHorarios = new JComboBox<>()), gbc);
        
        // Linha 3: Observações
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0; // Permitir que cresça verticalmente
        gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(criarCampo("Observações (opcional):", txtObservacoes = new JTextArea(5, 30)), gbc);
        
        // Linha 4: Checkbox de Email
        gbc.gridy = 3;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        chkEnviarEmail = new JCheckBox("Enviar e-mail de confirmação para o paciente");
        chkEnviarEmail.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chkEnviarEmail.setOpaque(false);
        chkEnviarEmail.setSelected(true);
        formPanel.add(chkEnviarEmail, gbc);

        // Listeners para atualizar horários
        cbMedicos.addActionListener(e -> carregarHorariosDisponiveis());
        txtData.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                if (txtData.getText().replaceAll("[_/]", "").length() == 8) {
                    carregarHorariosDisponiveis();
                }
            }
        });

        return formPanel;
    }

    /**
     * Cria o painel inferior com os botões de ação principais.
     * @return O painel de botões.
     */
    private JPanel criarPainelDeBotoes() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonPanel.setBackground(LIGHT_GRAY);

        buttonPanel.add(criarBotaoAcao("Confirmar Agendamento", SUCCESS_GREEN, e -> confirmarAgendamento()));
        buttonPanel.add(criarBotaoAcao("Limpar Campos", PRIMARY_BLUE, e -> limparCampos()));
        buttonPanel.add(criarBotaoAcao("Fechar", DARK_TEXT, e -> dispose()));
        
        return buttonPanel;
    }

    /**
     * Carrega os dados iniciais nos comboboxes de médicos e pacientes.
     */
    private void carregarDadosIniciais() {
        try {
            List<Medico> medicos = medicoDAO.findAll();
            for (Medico medico : medicos) {
                cbMedicos.addItem(medico);
            }
            
            List<Paciente> pacientes = pacienteDAO.findAll();
            for (Paciente paciente : pacientes) {
                cbPacientes.addItem(paciente);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar dados iniciais: " + e.getMessage(), "Erro de Sistema", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Carrega os horários disponíveis no combobox, consultando o ConsultaService.
     */
    private void carregarHorariosDisponiveis() {
        cbHorarios.removeAllItems();

        Medico medico = (Medico) cbMedicos.getSelectedItem();
        if (medico == null) return;

        try {
            LocalDate data = LocalDate.parse(txtData.getText().trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            List<String> horarios = consultaService.getHorariosDisponiveis(medico, data);
            
            if (horarios.isEmpty()) {
                cbHorarios.addItem("Sem horários");
            } else {
                for (String horario : horarios) {
                    cbHorarios.addItem(horario);
                }
            }
        } catch (DateTimeParseException ex) {
            // Data ainda não está completa ou é inválida, não faz nada
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar horários: " + e.getMessage(), "Erro de Sistema", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Confirma e salva o agendamento, utilizando a camada de serviço.
     */
    private void confirmarAgendamento() {
        try {
            Medico medico = (Medico) cbMedicos.getSelectedItem();
            Paciente paciente = (Paciente) cbPacientes.getSelectedItem();
            String horarioStr = (String) cbHorarios.getSelectedItem();

            if (medico == null || paciente == null || horarioStr == null || horarioStr.equals("Sem horários")) {
                JOptionPane.showMessageDialog(this, "Todos os campos (Médico, Paciente, Data e Horário) devem ser preenchidos corretamente.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
                return;
            }

            LocalDate data = LocalDate.parse(txtData.getText().trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            LocalTime horario = LocalTime.parse(horarioStr);
            LocalDateTime dataHorario = LocalDateTime.of(data, horario);

            Consulta novaConsulta = consultaService.agendarNovaConsulta(medico, paciente, dataHorario, txtObservacoes.getText());
            
            String msgSucesso = "Consulta agendada com sucesso para " + novaConsulta.getPaciente().getNome() + "!";
            
            if (chkEnviarEmail.isSelected()) {
                String email = JOptionPane.showInputDialog(this, "Digite o e-mail do paciente para a confirmação:");
                if (email != null && !email.trim().isEmpty()) {
                    boolean enviado = EmailService.enviarConfirmacaoConsulta(email, paciente.getNome(), medico.getNome(), novaConsulta.getDataHorarioFormatado());
                    if (!enviado) {
                        msgSucesso += "\n(Aviso: Falha ao enviar o e-mail de confirmação.)";
                    }
                }
            }
            
            JOptionPane.showMessageDialog(this, msgSucesso, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            limparCampos();

        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Formato de data inválido. Use dd/MM/yyyy.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalStateException | SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao agendar consulta: " + e.getMessage(), "Erro de Negócio", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Limpa todos os campos do formulário para um novo preenchimento.
     */
    private void limparCampos() {
        cbMedicos.setSelectedIndex(0);
        cbPacientes.setSelectedIndex(0);
        txtData.setText("");
        cbHorarios.removeAllItems();
        txtObservacoes.setText("");
        chkEnviarEmail.setSelected(true);
    }

    /**
     * Helper para criar um painel com rótulo e componente.
     */
    private JPanel criarCampo(String label, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(lbl, BorderLayout.NORTH);
        component.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Helper para criar um campo de texto formatado.
     */
    private JFormattedTextField criarCampoTextoFormatado(String mask) {
        try {
            MaskFormatter formatter = new MaskFormatter(mask);
            formatter.setPlaceholderCharacter('_');
            return new JFormattedTextField(formatter);
        } catch (ParseException e) {
            return new JFormattedTextField();
        }
    }

    /**
     * Fábrica de botões para criar instâncias de RoundedButton com estilo padrão.
     */
    private RoundedButton criarBotaoAcao(String texto, Color cor, ActionListener acao) {
        RoundedButton botao = new RoundedButton(texto);
        botao.setFont(new Font("Segoe UI", Font.BOLD, 14));
        botao.setForeground(CLEAN_WHITE);
        botao.setBackground(cor);
        botao.setPreferredSize(new Dimension(220, 50));
        botao.addActionListener(acao);
        return botao;
    }
    
    /**
     * Classe interna para criar botões com cantos arredondados.
     */
    private class RoundedButton extends JButton {
        private static final long serialVersionUID = 1L;
        private Color hoverBackgroundColor;
        private Color pressedBackgroundColor;

        public RoundedButton(String text) {
            super(text);
            super.setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        public void setBackground(Color bg) {
            super.setBackground(bg);
            if (bg != null) {
                hoverBackgroundColor = bg.brighter();
                pressedBackgroundColor = bg.darker();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (getModel().isPressed()) {
                g2.setColor(pressedBackgroundColor);
            } else if (getModel().isRollover()) {
                g2.setColor(hoverBackgroundColor);
            } else {
                g2.setColor(getBackground());
            }

            int arc = 25;
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), arc, arc));

            super.paintComponent(g2);
            g2.dispose();
        }
    }
}