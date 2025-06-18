package br.com.clinica.view;

import br.com.clinica.dao.*;
import br.com.clinica.model.*;
import br.com.clinica.service.EmailService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ActionEvent; // Importacao explicita
import java.awt.event.ActionListener; // Importacao explicita
import java.awt.event.KeyAdapter; // Importacao explicita
import java.awt.event.KeyEvent; // Importacao explicita
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

/**
 * Tela de agendamento de consultas com interface moderna reorganizada.
 * Sistema completo para agendar consultas medicas com validacoes e controles avancados.
 * Layout otimizado em linhas logicas para melhor organizacao visual.
 * Interage com MedicoDAO, PacienteDAO, ConsultaDAO e EmailService.
 *
 * Estrutura do formulario:
 * - Linha 1: Medico, Paciente, Botao Historico
 * - Linha 2: Data, Horario, Checkbox Email
 * - Linha 3: Observacoes (campo amplo)
 *
 * LAYOUT EXPANDIDO:
 * - Formulario se expande verticalmente ate os botoes.
 * - Aproveitamento maximo do espaco disponivel.
 * - Campo de observacoes com mais altura.
 *
 * Funcionalidades principais:
 * - Selecao de medico e paciente com carregamento automatico.
 * - Verificacao de horarios disponiveis em tempo real.
 * - Sistema de historico do paciente com edicao de observacoes.
 * - Validacoes de data, horario e campos obrigatorios.
 * - Sistema de email de confirmacao integrado.
 * - Layout em linhas: dados basicos + agendamento + observacoes.
 * - Interface moderna seguindo padrao medico do sistema.
 */
public class TelaAgendamento extends JFrame {

    private static final long serialVersionUID = 1L;

    // Paleta de cores medica profissional
    private static final Color PRIMARY_BLUE = new Color(52, 144, 220);
    private static final Color MEDICAL_GREEN = new Color(76, 175, 80);
    private static final Color CLEAN_WHITE = new Color(255, 255, 255);
    private static final Color LIGHT_GRAY = new Color(248, 249, 250);
    private static final Color DARK_TEXT = new Color(52, 58, 64);
    private static final Color ACCENT_RED = new Color(220, 53, 69);
    private static final Color SUCCESS_GREEN = new Color(40, 167, 69);
    private static final Color WARNING_ORANGE = new Color(255, 193, 7);

    // Componentes principais da interface
    private JPanel contentPane;
    private JPanel headerPanel;
    private JPanel formPanel;
    private JPanel buttonPanel;

    // Campos do formulario organizados
    private JComboBox<Medico> cbMedicos;
    private JComboBox<Paciente> cbPacientes;
    private JButton btnHistorico;
    private JFormattedTextField txtData;
    private JComboBox<String> cbHorarios;
    private JCheckBox chkEnviarEmail;
    private JTextArea txtObservacoes;

    // Botoes de acao no rodape
    private JButton btnConfirmar;
    private JButton btnLimpar;
    private JButton btnFechar; // Renomeado de btnCancelar
    private JButton btnVerCalendario; // Novo botao de navegacao

    // DAOs para acesso aos dados
    private MedicoDAO medicoDAO;
    private PacienteDAO pacienteDAO;
    private ConsultaDAO consultaDAO;

    /**
     * Construtor principal da TelaAgendamento.
     * Inicializa os DAOs para interacao com o banco de dados e
     * configura a interface visual da tela.
     * Carrega os dados iniciais (medicos e pacientes) nos ComboBoxes.
     *
     * Comunicacao com Outras Classes:
     * - MedicoDAO: Para carregar a lista de medicos.
     * - PacienteDAO: Para carregar a lista de pacientes.
     * - ConsultaDAO: Para realizar operacoes de agendamento e buscar consultas existentes.
     */
    public TelaAgendamento() {
        this.medicoDAO = new MedicoDAO();
        this.pacienteDAO = new PacienteDAO();
        this.consultaDAO = new ConsultaDAO();

        inicializarInterface();
        carregarDados();
    }

    /**
     * Configura a interface principal com layout moderno e reorganizado.
     * Define as propriedades da janela, cria os componentes principais,
     * organiza-os no layout e aplica os estilos visuais.
     */
    private void inicializarInterface() {
        configurarJanela();
        criarComponentesPrincipais();
        organizarLayout();
        aplicarEstilosVisuais();
    }

    /**
     * Configura as propriedades basicas da janela (JFrame).
     * Define o titulo, estado de tela cheia, posicao inicial e operacao de fechamento.
     */
    private void configurarJanela() {
        setTitle("Agendamento de Consultas - Sistema Clinica");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Maximiza a janela na tela
        setLocationRelativeTo(null); // Centraliza a janela
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Fecha apenas esta janela

        contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(LIGHT_GRAY);
        setContentPane(contentPane);
    }

    /**
     * Cria e inicializa todos os principais componentes visuais da interface.
     * Inclui o painel de cabecalho, o formulario de agendamento e o painel de botoes de acao.
     */
    private void criarComponentesPrincipais() {
        criarHeader();
        criarFormularioReorganizado();
        criarBotoesAcao();
    }

    /**
     * Cria o painel de cabecalho da tela.
     * Exibe o titulo principal da secao e um subtitulo informativo,
     * seguindo o padrao visual estabelecido para o sistema.
     */
    private void criarHeader() {
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_BLUE);
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel lblTitulo = new JLabel("AGENDAMENTO DE CONSULTAS");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(CLEAN_WHITE);

        JLabel lblSubtitulo = new JLabel("Agende consultas medicas com verificacao de disponibilidade");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblSubtitulo.setForeground(new Color(255, 255, 255, 200));

        JPanel headerLeft = new JPanel();
        headerLeft.setLayout(new BoxLayout(headerLeft, BoxLayout.Y_AXIS));
        headerLeft.setOpaque(false);
        headerLeft.add(lblTitulo);
        headerLeft.add(Box.createVerticalStrut(5));
        headerLeft.add(lblSubtitulo);

        headerPanel.add(headerLeft, BorderLayout.WEST);
    }

    /**
     * Cria o formulario de agendamento reorganizado em linhas logicas,
     * utilizando GridBagLayout para um controle preciso de posicionamento e redimensionamento.
     * O formulario se expande verticalmente para otimizar o espaco, especialmente para o campo de observacoes.
     */
    private void criarFormularioReorganizado() {
        formPanel = new JPanel(new BorderLayout());
        formPanel.setBackground(CLEAN_WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(PRIMARY_BLUE, 2),
                        "Dados do Agendamento",
                        0, 0,
                        new Font("Segoe UI", Font.BOLD, 18),
                        PRIMARY_BLUE
                ),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));

        JPanel camposPanel = new JPanel(new GridBagLayout());
        camposPanel.setBackground(CLEAN_WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 25, 0); // Espacamento vertical entre as "linhas"
        gbc.anchor = GridBagConstraints.WEST; // Alinha os componentes a esquerda
        gbc.fill = GridBagConstraints.HORIZONTAL; // Permite que os componentes preencham o espaco horizontal

        // Linha 1: Medico, Paciente, Botao Historico
        gbc.gridy = 0; // Primeira linha
        gbc.weightx = 0.33; // Divide o espaco horizontal
        gbc.gridx = 0;
        adicionarCampoMedico(camposPanel, gbc);

        gbc.gridx = 1;
        adicionarCampoPaciente(camposPanel, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0; // Nao expande este componente horizontalmente
        adicionarBotaoHistorico(camposPanel, gbc);
        
        // Espacador para empurrar o Historico para o canto direito, se houver espaco
        gbc.gridx = 3;
        gbc.weightx = 1.0;
        camposPanel.add(Box.createHorizontalGlue(), gbc); // Empurra para a direita

        // Linha 2: Data, Horario, Checkbox Email
        gbc.gridy = 1; // Segunda linha
        gbc.weightx = 0.0; // Reset weightx para os proximos campos
        gbc.gridx = 0;
        adicionarCampoData(camposPanel, gbc);

        gbc.gridx = 1;
        adicionarCampoHorario(camposPanel, gbc);

        gbc.gridx = 2;
        gbc.gridwidth = 2; // Ocupa duas colunas para o checkbox e o espacamento
        gbc.weightx = 1.0; // Permite que o checkbox expanda
        adicionarCheckboxEmail(camposPanel, gbc);
        gbc.gridwidth = 1; // Reset gridwidth

        // Linha 3: Observacoes (campo amplo)
        gbc.gridy = 2; // Terceira linha
        gbc.gridx = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER; // Ocupa o restante da linha
        gbc.weightx = 1.0; // Expande horizontalmente
        gbc.weighty = 1.0; // Expande verticalmente, crucial para JTextArea
        gbc.fill = GridBagConstraints.BOTH; // Preenche tanto horizontal quanto vertical
        gbc.insets = new Insets(0, 0, 0, 0); // Remove insets da ultima linha
        adicionarCampoObservacoes(camposPanel, gbc);


        formPanel.add(camposPanel, BorderLayout.CENTER);
    }

    /**
     * Adiciona o campo de selecao de Medico ao painel do formulario.
     * O ComboBox e preenchido automaticamente com a lista de medicos cadastrados.
     * @param parentPanel O JPanel pai onde o componente sera adicionado (camposPanel).
     * @param gbc As restricoes de GridBagLayout para posicionamento.
     *
     * Comunicacao com Outras Classes:
     * - MedicoDAO: Para carregar a lista de medicos.
     */
    private void adicionarCampoMedico(JPanel parentPanel, GridBagConstraints gbc) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(CLEAN_WHITE);
        container.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20)); // Espacamento a direita

        JLabel lblMedico = new JLabel("Medico:");
        lblMedico.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblMedico.setForeground(DARK_TEXT);
        lblMedico.setAlignmentX(Component.LEFT_ALIGNMENT);

        cbMedicos = new JComboBox<>();
        cbMedicos.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        cbMedicos.setPreferredSize(new Dimension(300, 40)); // Tamanho preferencial
        cbMedicos.setMaximumSize(new Dimension(300, 40));   // Tamanho maximo
        cbMedicos.setBackground(CLEAN_WHITE);
        cbMedicos.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Listener para atualizar horarios disponiveis quando o medico mudar
        cbMedicos.addActionListener(new ActionListener() { // Substituicao de lambda
            @Override
            public void actionPerformed(ActionEvent e) {
                carregarHorariosDisponiveis();
            }
        });

        container.add(lblMedico);
        container.add(Box.createVerticalStrut(8));
        container.add(cbMedicos);
        parentPanel.add(container, gbc);
    }

    /**
     * Adiciona o campo de selecao de Paciente ao painel do formulario.
     * O ComboBox e preenchido automaticamente com a lista de pacientes cadastrados.
     * @param parentPanel O JPanel pai onde o componente sera adicionado (camposPanel).
     * @param gbc As restricoes de GridBagLayout para posicionamento.
     *
     * Comunicacao com Outras Classes:
     * - PacienteDAO: Para carregar a lista de pacientes.
     */
    private void adicionarCampoPaciente(JPanel parentPanel, GridBagConstraints gbc) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(CLEAN_WHITE);
        container.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20)); // Espacamento a direita

        JLabel lblPaciente = new JLabel("Paciente:");
        lblPaciente.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblPaciente.setForeground(DARK_TEXT);
        lblPaciente.setAlignmentX(Component.LEFT_ALIGNMENT);

        cbPacientes = new JComboBox<>();
        cbPacientes.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        cbPacientes.setPreferredSize(new Dimension(300, 40));
        cbPacientes.setMaximumSize(new Dimension(300, 40));
        cbPacientes.setBackground(CLEAN_WHITE);
        cbPacientes.setAlignmentX(Component.LEFT_ALIGNMENT);

        container.add(lblPaciente);
        container.add(Box.createVerticalStrut(8));
        container.add(cbPacientes);
        parentPanel.add(container, gbc);
    }

    /**
     * Adiciona o botao "Historico" ao painel do formulario.
     * Ao clicar, abre um popup com o historico de consultas do paciente selecionado,
     * permitindo visualizar e editar observacoes.
     * @param parentPanel O JPanel pai onde o componente sera adicionado (camposPanel).
     * @param gbc As restricoes de GridBagLayout para posicionamento.
     *
     * Comunicacao com Outras Classes:
     * - ConsultaDAO: Para buscar o historico de consultas do paciente.
     */
    private void adicionarBotaoHistorico(JPanel parentPanel, GridBagConstraints gbc) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(CLEAN_WHITE);
        
        // Espacador vertical para alinhar o botao com os campos de texto
        container.add(Box.createVerticalStrut(24)); 

        btnHistorico = new JButton("Historico");
        btnHistorico.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnHistorico.setForeground(CLEAN_WHITE);
        btnHistorico.setBackground(WARNING_ORANGE);
        btnHistorico.setPreferredSize(new Dimension(140, 40));
        btnHistorico.setMaximumSize(new Dimension(140, 40));
        btnHistorico.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btnHistorico.setFocusPainted(false);
        btnHistorico.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnHistorico.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        btnHistorico.addActionListener(new ActionListener() { // Substituicao de lambda
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirHistoricoPaciente();
            }
        });
        
        btnHistorico.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnHistorico.setBackground(WARNING_ORANGE.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnHistorico.setBackground(WARNING_ORANGE);
            }
        });

        container.add(btnHistorico);
        parentPanel.add(container, gbc);
    }

    /**
     * Adiciona o campo de entrada de Data da Consulta ao painel do formulario.
     * Utiliza uma mascara para formatacao dd/MM/yyyy e valida automaticamente o preenchimento.
     * @param parentPanel O JPanel pai onde o componente sera adicionado (camposPanel).
     * @param gbc As restricoes de GridBagLayout para posicionamento.
     */
    private void adicionarCampoData(JPanel parentPanel, GridBagConstraints gbc) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(CLEAN_WHITE);
        container.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20)); // Espacamento a direita

        JLabel lblData = new JLabel("Data da Consulta:");
        lblData.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblData.setForeground(DARK_TEXT);
        lblData.setAlignmentX(Component.LEFT_ALIGNMENT);

        try {
            MaskFormatter dataMask = new MaskFormatter("##/##/####");
            dataMask.setValidCharacters("0123456789");
            dataMask.setPlaceholderCharacter('_');
            txtData = new JFormattedTextField(dataMask);
        } catch (ParseException e) {
            txtData = new JFormattedTextField(); // Garante que e um JFormattedTextField
        }

        txtData.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtData.setPreferredSize(new Dimension(160, 40));
        txtData.setMaximumSize(new Dimension(160, 40));
        txtData.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        txtData.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtData.setToolTipText("Formato: dd/MM/yyyy");

        // Listener para atualizar horarios disponiveis quando a data for totalmente digitada
        txtData.addKeyListener(new KeyAdapter() { // Substituicao de lambda
            @Override
            public void keyReleased(KeyEvent evt) {
                if (txtData.getText().replaceAll("[_/ ]", "").length() == 8) { // Considera a mascara completa
                    carregarHorariosDisponiveis();
                }
            }
        });

        container.add(lblData);
        container.add(Box.createVerticalStrut(8));
        container.add(txtData);
        parentPanel.add(container, gbc);
    }

    /**
     * Adiciona o campo de selecao de Horario ao painel do formulario.
     * O ComboBox e preenchido com horaios disponiveis baseados no medico e data selecionados.
     * @param parentPanel O JPanel pai onde o componente sera adicionado (camposPanel).
     * @param gbc As restricoes de GridBagLayout para posicionamento.
     */
    private void adicionarCampoHorario(JPanel parentPanel, GridBagConstraints gbc) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(CLEAN_WHITE);
        container.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20)); // Espacamento a direita

        JLabel lblHorario = new JLabel("Horario:");
        lblHorario.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblHorario.setForeground(DARK_TEXT);
        lblHorario.setAlignmentX(Component.LEFT_ALIGNMENT);

        cbHorarios = new JComboBox<>();
        cbHorarios.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        cbHorarios.setPreferredSize(new Dimension(120, 40));
        cbHorarios.setMaximumSize(new Dimension(120, 40));
        cbHorarios.setBackground(CLEAN_WHITE);
        cbHorarios.setAlignmentX(Component.LEFT_ALIGNMENT);

        container.add(lblHorario);
        container.add(Box.createVerticalStrut(8));
        container.add(cbHorarios);
        parentPanel.add(container, gbc);
    }

    /**
     * Adiciona o checkbox para "Enviar email de confirmacao" ao painel do formulario.
     * Permite ao usuario escolher se um email de confirmacao sera enviado apos o agendamento.
     * @param parentPanel O JPanel pai onde o componente sera adicionado (camposPanel).
     * @param gbc As restricoes de GridBagLayout para posicionamento.
     */
    private void adicionarCheckboxEmail(JPanel parentPanel, GridBagConstraints gbc) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(CLEAN_WHITE);

        // Espacador vertical para alinhar o checkbox com os campos de texto
        container.add(Box.createVerticalStrut(24)); 

        chkEnviarEmail = new JCheckBox("Enviar email de confirmacao");
        chkEnviarEmail.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        chkEnviarEmail.setBackground(CLEAN_WHITE);
        chkEnviarEmail.setForeground(DARK_TEXT);
        chkEnviarEmail.setSelected(true);
        chkEnviarEmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        chkEnviarEmail.setPreferredSize(new Dimension(280, 40)); // Ajustado para 40px de altura

        container.add(chkEnviarEmail);
        parentPanel.add(container, gbc);
    }

    /**
     * Adiciona o campo de Observacoes (JTextArea) ao painel do formulario.
     * Este campo e amplo e permite anotacoes adicionais sobre a consulta.
     * Ele se expande verticalmente para aproveitar o espaco disponivel.
     * @param parentPanel O JPanel pai onde o componente sera adicionado (camposPanel).
     * @param gbc As restricoes de GridBagLayout para posicionamento.
     */
    private void adicionarCampoObservacoes(JPanel parentPanel, GridBagConstraints gbc) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(CLEAN_WHITE);
        
        JLabel lblObservacoes = new JLabel("Observacoes (opcional):");
        lblObservacoes.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblObservacoes.setForeground(DARK_TEXT);
        lblObservacoes.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtObservacoes = new JTextArea();
        txtObservacoes.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtObservacoes.setLineWrap(true);
        txtObservacoes.setWrapStyleWord(true);
        txtObservacoes.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JScrollPane scrollObs = new JScrollPane(txtObservacoes);
        scrollObs.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        scrollObs.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        // Nao define preferred/maximum size fixo, o GridBagLayout com weighty=1.0 fara ele expandir
        scrollObs.setAlignmentX(Component.LEFT_ALIGNMENT);
        

        container.add(lblObservacoes);
        container.add(Box.createVerticalStrut(8));
        container.add(scrollObs);
        parentPanel.add(container, gbc);
    }

    /**
     * Cria e configura o painel de botoes de acao no rodape da tela.
     * Inclui botoes para confirmar o agendamento, limpar campos,
     * ver no calendario, e fechar a tela (retornando ao menu principal).
     */
    private void criarBotoesAcao() {
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonPanel.setBackground(LIGHT_GRAY);
        buttonPanel.setPreferredSize(new Dimension(0, 80));

        btnConfirmar = criarBotaoAcao("Confirmar Agendamento", SUCCESS_GREEN, new ActionListener() { // Substituicao de lambda
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmarAgendamento();
            }
        });

        btnLimpar = criarBotaoAcao("Limpar Campos", PRIMARY_BLUE, new ActionListener() { // Substituicao de lambda
            @Override
            public void actionPerformed(ActionEvent e) {
                limparCampos();
            }
        });

        btnVerCalendario = criarBotaoAcao("Ver no Calendario", PRIMARY_BLUE, new ActionListener() { // Substituicao de lambda
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirTelaAgendaCalendario();
            }
        });
        
        btnFechar = criarBotaoAcao("Fechar", ACCENT_RED, new ActionListener() { // Substituicao de lambda
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Fecha a janela atual
                trazerTelaPrincipalParaFrente(); // Retorna para a TelaPrincipal existente
            }
        });

        buttonPanel.add(btnConfirmar);
        buttonPanel.add(btnLimpar);
        buttonPanel.add(btnVerCalendario);
        buttonPanel.add(btnFechar); // O botao "Voltar ao Menu" foi removido e sua funcao integrada ao "Fechar"
    }

    /**
     * Cria um botao de acao com estilo moderno.
     * Inclui configuracoes de fonte, cor, tamanho preferencial, borda e efeito de hover.
     * @param texto Texto visivel no botao.
     * @param cor Cor de fundo do botao.
     * @param acao O ActionListener a ser executado quando o botao e clicado.
     * @return JButton configurado.
     */
    private JButton criarBotaoAcao(String texto, Color cor, ActionListener acao) { // Adicionado ActionListener como parametro
        JButton botao = new JButton(texto);
        botao.setFont(new Font("Segoe UI", Font.BOLD, 16));
        botao.setForeground(CLEAN_WHITE);
        botao.setBackground(cor);
        botao.setPreferredSize(new Dimension(220, 50));
        botao.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        botao.setFocusPainted(false);
        botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
        botao.addActionListener(acao); // Adicionado o ActionListener

        botao.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                botao.setBackground(cor.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                botao.setBackground(cor);
            }
        });

        return botao;
    }

    /**
     * Organiza o layout final dos paineis na janela principal (contentPane).
     * O headerPanel e posicionado ao norte, o formPanel expande no centro,
     * e o buttonPanel fica na parte inferior.
     */
    private void organizarLayout() {
        contentPane.add(headerPanel, BorderLayout.NORTH);
        contentPane.add(formPanel, BorderLayout.CENTER);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Aplica estilos visuais finais ao painel de cabecalho.
     * Adiciona bordas e efeitos para aprimorar a estetica da interface.
     */
    private void aplicarEstilosVisuais() {
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0, 0, 0, 30)),
                BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
    }

    /**
     * Carrega medicos e pacientes nos ComboBoxes na inicializacao da tela.
     * Tambem carrega os horarios padrao de funcionamento da clinica.
     *
     * Comunicacao com Outras Classes:
     * - MedicoDAO: Para buscar todos os medicos.
     * - PacienteDAO: Para buscar todos os pacientes.
     */
    private void carregarDados() {
        try {
            cbMedicos.removeAllItems();
            List<Medico> medicos = medicoDAO.findAll();
            for (Medico medico : medicos) {
                cbMedicos.addItem(medico);
            }
            
            cbPacientes.removeAllItems();
            List<Paciente> pacientes = pacienteDAO.findAll();
            for (Paciente paciente : pacientes) {
                cbPacientes.addItem(paciente);
            }
            
            carregarHorarios();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao carregar dados: " + e.getMessage(),
                    "Erro de Carregamento",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Carrega os horarios padrao de atendimento da clinica (08:00 as 17:00) no ComboBox de horarios.
     */
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

    /**
     * Carrega apenas os horarios disponiveis para agendamento,
     * com base no medico e data selecionados. Remove os horarios ja ocupados.
     * Esta e uma funcionalidade critica para impedir agendamentos em conflito.
     *
     * Comunicacao com Outras Classes:
     * - Medico: Para obter o CRM do medico selecionado.
     * - ConsultaDAO: Para buscar consultas ja agendadas e realizadas.
     * - Consulta.StatusConsulta: Para verificar o status das consultas.
     */
    private void carregarHorariosDisponiveis() {
        cbHorarios.removeAllItems();

        String[] todosHorarios = {
                "08:00", "08:30", "09:00", "09:30", "10:00", "10:30",
                "11:00", "11:30", "14:00", "14:30", "15:00", "15:30",
                "16:00", "16:30", "17:00"
        };

        // Se nao tem medico selecionado ou a data esta incompleta, mostra todos os horarios
        if (cbMedicos.getSelectedItem() == null || txtData.getText().replaceAll("[_/ ]", "").length() < 8) {
            for (String horario : todosHorarios) {
                cbHorarios.addItem(horario);
            }
            return;
        }

        try {
            Medico medico = (Medico) cbMedicos.getSelectedItem();
            LocalDate data = LocalDate.parse(txtData.getText().trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            
            List<Consulta> consultas = consultaDAO.findAll();
            List<String> horariosOcupados = new ArrayList<>();
            
            for (Consulta consulta : consultas) {
                // Considera consultas AGENDADAS ou REALIZADAS como horaios ocupados para prevencao de conflito
                if (consulta.getMedico().getCrm().equals(medico.getCrm()) && 
                    consulta.getDataHorario().toLocalDate().equals(data) &&
                    (consulta.getStatus() == Consulta.StatusConsulta.AGENDADA ||
                     consulta.getStatus() == Consulta.StatusConsulta.REALIZADA)) {
                    String horarioOcupado = consulta.getDataHorario().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
                    horariosOcupados.add(horarioOcupado);
                }
            }
            
            for (String horario : todosHorarios) {
                if (!horariosOcupados.contains(horario)) {
                    cbHorarios.addItem(horario);
                }
            }
            
        } catch (Exception e) {
            // Em caso de erro (ex: data invalida), carrega todos os horarios para nao travar a tela
            for (String horario : todosHorarios) {
                cbHorarios.addItem(horario);
            }
            JOptionPane.showMessageDialog(this,
                "Erro ao carregar horaios disponiveis: " + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Abre um popup com o historico de consultas do paciente selecionado.
     * Permite ao usuario visualizar e editar as observacoes de consultas passadas.
     * @param pacienteSelecionado O Paciente cujo historico sera exibido.
     *
     * Comunicacao com Outras Classes:
     * - Paciente: Para obter o CPF do paciente.
     * - ConsultaDAO: Para buscar o historico de consultas do paciente.
     * - Consulta: Para exibir detalhes da consulta no historico.
     */
    private void abrirHistoricoPaciente() {
        Paciente pacienteSelecionado = (Paciente) cbPacientes.getSelectedItem();
        
        if (pacienteSelecionado == null) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um paciente primeiro!",
                    "Selecao Necessaria",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            List<Consulta> todasConsultas = consultaDAO.findAll();
            List<Consulta> consultasPaciente = new ArrayList<>();
            
            for (Consulta consulta : todasConsultas) {
                if (consulta.getPaciente().getCpf().equals(pacienteSelecionado.getCpf())) {
                    consultasPaciente.add(consulta);
                }
            }
            
            if (consultasPaciente.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Nenhuma consulta encontrada para: " + pacienteSelecionado.getNome(),
                        "Historico Vazio",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            mostrarHistoricoPopup(pacienteSelecionado, consultasPaciente);
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao carregar historico: " + ex.getMessage(),
                    "Erro de Sistema",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Exibe um dialogo (popup) contendo uma tabela com o historico de consultas do paciente.
     * As observacoes das consultas podem ser editadas diretamente na tabela.
     * Possui botoes para salvar modificacoes e fechar o dialogo.
     * @param paciente O Paciente cujo historico esta sendo exibido.
     * @param consultas A lista de Consulta do historico do paciente.
     *
     * Comunicacao com Outras Classes:
     * - ConsultaDAO: Para atualizar observacoes apos edicao.
     * - TelaAgenda: Pode ser aberta a partir do historico.
     */
    private void mostrarHistoricoPopup(Paciente paciente, List<Consulta> consultas) {
        JDialog dialog = new JDialog(this, "Historico - " + paciente.getNome(), true);
        dialog.setSize(900, 600);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout());

        JLabel lblHeader = new JLabel("Historico de Consultas - " + paciente.getNome());
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblHeader.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblInfo = new JLabel("Dica: Clique duas vezes na coluna 'Observacoes' para editar");
        lblInfo.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblInfo.setForeground(Color.GRAY);
        lblInfo.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 10));

        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.add(lblHeader, BorderLayout.NORTH);
        infoPanel.add(lblInfo, BorderLayout.SOUTH);
        panel.add(infoPanel, BorderLayout.NORTH);

        String[] colunas = {"ID", "Data/Hora", "Medico", "Status", "Observacoes"};
        DefaultTableModel modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Apenas coluna de Observacoes e editavel
            }
        };
        
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
        tabela.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        tabela.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(150);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(150);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(100);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(300);
        
        JScrollPane scroll = new JScrollPane(tabela);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton btnSalvar = new JButton("Salvar Modificacoes");
        btnSalvar.setBackground(MEDICAL_GREEN);
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSalvar.addActionListener(new ActionListener() { // Substituicao de lambda
            @Override
            public void actionPerformed(ActionEvent e) {
                salvarModificacoesHistorico(dialog, tabela, modeloTabela, consultas);
            }
        });
        buttonPanel.add(btnSalvar);

        JButton btnVerAgenda = new JButton("Ver Agenda Completa");
        btnVerAgenda.setBackground(PRIMARY_BLUE);
        btnVerAgenda.setForeground(Color.WHITE);
        btnVerAgenda.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnVerAgenda.addActionListener(new ActionListener() { // Substituicao de lambda
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
                TelaAgenda telaAgenda = new TelaAgenda();
                telaAgenda.setVisible(true);
                telaAgenda.toFront();
            }
        });
        buttonPanel.add(btnVerAgenda);

        JButton btnFechar = new JButton("Fechar");
        btnFechar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnFechar.addActionListener(new ActionListener() { // Substituicao de lambda
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
        buttonPanel.add(btnFechar);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.getContentPane().add(panel);
        dialog.setVisible(true);
    }

    /**
     * Salva as modificacoes realizadas no historico de consultas do paciente.
     * Percorre a tabela do popup de historico, verifica quais observacoes foram editadas
     * e atualiza as consultas correspondentes no banco de dados.
     * @param dialog O JDialog do popup de historico.
     * @param tabela A JTable do historico.
     * @param modelo O DefaultTableModel da tabela do historico.
     * @param consultas A lista original de Consulta do paciente.
     *
     * Comunicacao com Outras Classes:
     * - ConsultaDAO: Para atualizar consultas.
     */
    private void salvarModificacoesHistorico(JDialog dialog, JTable tabela, DefaultTableModel modelo, List<Consulta> consultas) {
        int opcao = JOptionPane.showConfirmDialog(
                dialog,
                "Deseja realmente salvar todas as modificacoes realizadas?",
                "Confirmar Alteracoes",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (opcao != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            boolean algumaMudanca = false;
            
            for (int i = 0; i < modelo.getRowCount(); i++) {
                Long idConsulta = (Long) modelo.getValueAt(i, 0);
                String novasObservacoes = (String) modelo.getValueAt(i, 4);
                
                for (Consulta consulta : consultas) {
                    if (consulta.getId().equals(idConsulta)) {
                        String observacoesAntigas = consulta.getObservacoes();
                        
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
                        "Modificacoes salvas com sucesso!",
                        "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(dialog,
                        "Nenhuma modificacao foi detectada.",
                        "Informacao",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(dialog,
                    "Erro ao salvar modificacoes: " + ex.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Confirma e realiza o agendamento de uma nova consulta.
     * Executa validacoes completas dos campos, salva a consulta no banco de dados,
     * e opcionalmente envia um email de confirmacao para o paciente.
     *
     * Comunicacao com Outras Classes:
     * - Medico, Paciente, Consulta: Objetos de modelo para criar a consulta.
     * - ConsultaDAO: Para salvar a nova consulta no banco.
     * - EmailService: Para enviar o email de confirmacao.
     */
    private void confirmarAgendamento() {
        try {
            // Validacoes obrigatorias
            if (cbMedicos.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this,
                        "Selecione um medico!",
                        "Erro de Validacao",
                        JOptionPane.ERROR_MESSAGE);
                cbMedicos.requestFocus();
                return;
            }

            if (cbPacientes.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this,
                        "Selecione um paciente!",
                        "Erro de Validacao",
                        JOptionPane.ERROR_MESSAGE);
                cbPacientes.requestFocus();
                return;
            }

            if (txtData.getText().replaceAll("[_/ ]", "").length() < 8) { // Verifica se a mascara esta completa
                JOptionPane.showMessageDialog(this,
                        "Informe a data da consulta no formato dd/MM/yyyy!",
                        "Erro de Validacao",
                        JOptionPane.ERROR_MESSAGE);
                txtData.requestFocus();
                return;
            }

            if (cbHorarios.getSelectedItem() == null) {
                 JOptionPane.showMessageDialog(this,
                        "Selecione um horario disponivel!",
                        "Erro de Validacao",
                        JOptionPane.ERROR_MESSAGE);
                cbHorarios.requestFocus();
                return;
            }

            // Validacao e parsing da data
            LocalDate data;
            try {
                String dataTexto = txtData.getText().trim();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                data = LocalDate.parse(dataTexto, formatter);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Data invalida! Use formato dd/MM/yyyy",
                        "Erro de Validacao",
                        JOptionPane.ERROR_MESSAGE);
                txtData.requestFocus();
                return;
            }

            // Parsing do horario
            LocalTime horario = LocalTime.parse((String) cbHorarios.getSelectedItem());
            LocalDateTime dataHorario = LocalDateTime.of(data, horario);

            // Verificar se data nao e no passado
            if (dataHorario.isBefore(LocalDateTime.now())) {
                JOptionPane.showMessageDialog(this,
                        "Nao e possivel agendar consulta no passado!",
                        "Erro de Validacao",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Criar e salvar consulta
            Medico medico = (Medico) cbMedicos.getSelectedItem();
            Paciente paciente = (Paciente) cbPacientes.getSelectedItem();

            Consulta consulta = new Consulta(medico, paciente, dataHorario);
            consulta.setObservacoes(txtObservacoes.getText().trim());

            consultaDAO.save(consulta);

            // Envio de email de confirmacao (opcional)
            if (chkEnviarEmail.isSelected()) {
                String emailPaciente = JOptionPane.showInputDialog(this,
                        "Digite o email do paciente para confirmacao:",
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
                                "Consulta agendada com sucesso!\n Email de confirmacao enviado para: " + emailPaciente,
                                "Sucesso",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Consulta agendada com sucesso!\n Porem houve erro ao enviar email. Verifique o console para detalhes.",
                                "Aviso",
                                JOptionPane.WARNING_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Consulta agendada com sucesso!\n Email nao enviado (endereco vazio).",
                            "Sucesso",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Consulta agendada com sucesso!",
                        "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            // Limpar campos apos sucesso
            limparCampos();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao agendar consulta: " + e.getMessage(),
                    "Erro de Sistema",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Limpa todos os campos do formulario, restaurando-os ao estado inicial
     * para permitir um novo agendamento.
     */
    private void limparCampos() {
        // Redefine a selecao dos comboboxes ou o primeiro item
        if (cbMedicos.getItemCount() > 0) cbMedicos.setSelectedIndex(0);
        else cbMedicos.setSelectedIndex(-1);

        if (cbPacientes.getItemCount() > 0) cbPacientes.setSelectedIndex(0);
        else cbPacientes.setSelectedIndex(-1);

        txtData.setText("");
        txtObservacoes.setText("");
        chkEnviarEmail.setSelected(true);
        carregarHorarios(); // Restaurar horarios padrao e atualizar disponibilidade
    }

    /**
     * Abre a TelaAgendaCalendario.
     * Esta tela permite a visualizacao em calendario dos agendamentos.
     *
     * Comunicacao com Outras Classes:
     * - TelaAgendaCalendario: Abre uma nova instancia e a torna visivel.
     */
    private void abrirTelaAgendaCalendario() {
        TelaAgendaCalendario tela = new TelaAgendaCalendario();
        tela.setVisible(true);
        tela.toFront(); // Traz a janela para a frente
    }

    /**
     * Traz para a frente a instancia existente da TelaPrincipal,
     * ou cria uma nova se nenhuma for encontrada.
     * Garante que o usuario retorne ao menu principal sem abrir multiplas janelas.
     *
     * Comunicacao com Outras Classes:
     * - TelaPrincipal: Busca por uma instancia existente ou cria uma nova.
     */
    private void trazerTelaPrincipalParaFrente() {
        TelaPrincipal principal = null;
        // Percorre todas as janelas abertas para encontrar a TelaPrincipal
        for (Window window : Window.getWindows()) {
            if (window instanceof TelaPrincipal) {
                principal = (TelaPrincipal) window;
                break;
            }
        }

        if (principal != null) {
            principal.setVisible(true);
            principal.toFront();
            principal.requestFocus();
        } else {
            // Se por alguma razao a TelaPrincipal nao estiver aberta, cria uma nova
            // (Isso nao deveria acontecer se a TelaPrincipal e o ponto de entrada)
            new TelaPrincipal().setVisible(true);
        }
    }
}