package br.com.clinica.view;

import br.com.clinica.dao.MedicoDAO;
import br.com.clinica.model.Medico;
import br.com.clinica.util.ValidadorCRM;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ActionEvent; // Importacao especifica para ActionEvent
import java.awt.event.ActionListener; // Importacao especifica para ActionListener
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Tela de gerenciamento de medicos com interface moderna e reorganizada.
 * Permite o CRUD (Criar, Ler, Atualizar, Excluir) completo para o cadastro e gestao de medicos.
 * O layout e otimizado em linhas logicas para melhor organizacao visual.
 * Interage com MedicoDAO e ValidadorCRM.
 *
 * Estrutura do formulario:
 * - Linha 1: CRM, Nome, Especialidade
 * - Linha 2: Sala, Horario Inicio, Horario Fim, Dias de Atendimento
 *
 * Funcionalidades principais:
 * - Cadastro de novos medicos com validacao de CRM.
 * - Edicao de dados de medicos existentes.
 * - Exclusao de registros de medicos.
 * - Listagem de todos os medicos em uma tabela interativa.
 * - Selecao de especialidades, horarios e dias de atendimento.
 * - Validacoes em tempo real e feedback visual ao usuario.
 */
public class TelaMedicos extends JFrame {
    
    private static final long serialVersionUID = 1L;
    
    // Paleta de cores medica profissional
    private static final Color PRIMARY_BLUE = new Color(52, 144, 220);
    private static final Color MEDICAL_GREEN = new Color(76, 175, 80);
    private static final Color CLEAN_WHITE = new Color(255, 255, 255);
    private static final Color LIGHT_GRAY = new Color(248, 249, 250);
    private static final Color DARK_TEXT = new Color(52, 58, 64);
    private static final Color ACCENT_RED = new Color(220, 53, 69);
    private static final Color SUCCESS_GREEN = new Color(40, 167, 69);
    private static final Color ORANGE_ACCENT = new Color(255, 152, 0);
    private static final Color PURPLE_ACCENT = new Color(156, 39, 176);
    private static final Color GRAY_ACCENT = new Color(96, 125, 139);
    
    // Componentes principais da interface
    private JPanel contentPane;
    private JPanel headerPanel;
    private JPanel formPanel;
    private JPanel tablePanel;
    private JPanel buttonPanel;
    
    // Componentes da tabela de medicos
    private JTable table;
    private DefaultTableModel tableModel;
    
    // Campos do formulario organizados
    private JTextField txtCrm;
    private JTextField txtNome;
    private JComboBox<String> cbEspecialidade;
    private JTextField txtSala;
    private JSpinner spinnerInicio;
    private JSpinner spinnerFim;
    private JCheckBox[] checkDias;
    
    // Botoes de acao (CRUD e Navegacao)
    private JButton btnNovo;
    private JButton btnSalvar;
    private JButton btnExcluir;
    private JButton btnFechar; // Botao para fechar a tela e voltar ao menu principal
    private JButton btnVoltarMenuPrincipalHeader; // Botao de navegacao no header
    private JButton btnIrParaPacientes; // Novo botao de navegacao
    private JButton btnIrParaRelatorios; // Novo botao de navegacao
    private JButton btnIrParaAgendaCalendario; // Novo botao de navegacao
    
    // Controle de estado para edicao de medico
    private Medico medicoSelecionado = null;
    
    // DAO para comunicacao com o banco de dados
    private MedicoDAO medicoDAO;
    
    /**
     * Construtor principal da TelaMedicos.
     * Inicializa o MedicoDAO e configura a interface visual da tela.
     * Carrega os dados de medicos existentes na tabela.
     *
     * Comunicacao com Outras Classes:
     * - MedicoDAO: Para todas as operacoes de persistencia de medicos.
     */
    public TelaMedicos() {
        this.medicoDAO = new MedicoDAO();
        inicializarInterface();
        carregarMedicos();
    }
    
    /**
     * Configura a interface visual principal da tela.
     * Define as propriedades da janela, cria os componentes principais
     * (cabecalho, formulario, tabela, botoes), organiza-os no layout
     * e aplica os estilos visuais finais.
     */
    private void inicializarInterface() {
        configurarJanela();
        criarComponentesPrincipais();
        organizarLayout();
        aplicarEstilosVisuais();
    }
    
    /**
     * Configura as propriedades basicas da janela (JFrame) da TelaMedicos.
     * Define o titulo, estado de tela cheia, posicao inicial e operacao de fechamento.
     */
    private void configurarJanela() {
        setTitle("Gerenciamento de Medicos - Sistema Clinica");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Maximiza a janela
        setLocationRelativeTo(null); // Centraliza a janela na tela
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Fecha apenas esta janela
        
        contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(LIGHT_GRAY);
        setContentPane(contentPane);
    }
    
    /**
     * Cria e inicializa todos os principais componentes visuais da interface.
     * Inclui o painel de cabecalho, o formulario de dados do medico,
     * a tabela de listagem de medicos e o painel de botoes de acao.
     */
    private void criarComponentesPrincipais() {
        criarHeader();
        criarFormularioReorganizado();
        criarTabelaMedicos();
        criarBotoesAcao();
    }
    
    /**
     * Cria o painel de cabecalho da tela.
     * Exibe o titulo principal da secao, um subtitulo informativo,
     * e um botao para retornar ao menu principal.
     *
     * Comunicacao com Outras Classes:
     * - TelaPrincipal: A tela para a qual o usuario retornara.
     */
    private void criarHeader() {
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_BLUE);
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        JPanel headerLeft = new JPanel();
        headerLeft.setLayout(new BoxLayout(headerLeft, BoxLayout.Y_AXIS));
        headerLeft.setOpaque(false);
        
        JLabel lblTitulo = new JLabel("GERENCIAMENTO DE MEDICOS");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(CLEAN_WHITE);
        
        JLabel lblSubtitulo = new JLabel("Cadastro, edicao e controle de profissionais medicos");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblSubtitulo.setForeground(new Color(255, 255, 255, 200));
        
        headerLeft.add(lblTitulo);
        headerLeft.add(Box.createVerticalStrut(5));
        headerLeft.add(lblSubtitulo);
        
        // Botao para voltar ao menu principal no header
        btnVoltarMenuPrincipalHeader = criarBotaoHeaderNav("Voltar ao Menu", CLEAN_WHITE, DARK_TEXT, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                trazerTelaPrincipalParaFrente();
            }
        });

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        headerRight.setOpaque(false);
        headerRight.add(btnVoltarMenuPrincipalHeader);
        
        headerPanel.add(headerLeft, BorderLayout.WEST);
        headerPanel.add(headerRight, BorderLayout.EAST);
    }

    /**
     * Cria um botao padronizado para uso no cabecalho para navegacao.
     * Inclui estilos de fonte, cor, tamanho e um efeito de hover visual.
     * @param texto O texto a ser exibido no botao.
     * @param corTexto A cor do texto do botao.
     * @param corFundo A cor de fundo padrao do botao.
     * @param acao O ActionListener a ser executado quando o botao e clicado.
     * @return Um JButton configurado com o estilo definido.
     */
    private JButton criarBotaoHeaderNav(String texto, Color corTexto, Color corFundo, ActionListener acao) {
        JButton botao = new JButton(texto);
        botao.setFont(new Font("Segoe UI", Font.BOLD, 14));
        botao.setForeground(corTexto);
        botao.setBackground(corFundo);
        botao.setPreferredSize(new Dimension(160, 35)); // Tamanho padrao para botoes de header
        botao.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        botao.setFocusPainted(false);
        botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
        botao.addActionListener(acao);
        
        botao.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                botao.setBackground(corFundo.brighter());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                botao.setBackground(corFundo);
            }
        });
        return botao;
    }
    
    /**
     * Cria o formulario de cadastro e edicao de medicos, reorganizado em linhas logicas.
     * Utiliza GridBagLayout para um controle preciso de posicionamento e redimensionamento.
     * A altura do formulario e ajustada para melhor visibilidade dos campos.
     *
     * AJUSTES DE LAYOUT:
     * - Altura do formulario aumentada para 280px.
     * - Espacamento entre linhas aumentado para 30px.
     */
    private void criarFormularioReorganizado() {
        formPanel = new JPanel(new BorderLayout());
        formPanel.setBackground(CLEAN_WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_BLUE, 2),
                "Dados do Medico",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 18),
                PRIMARY_BLUE
            ),
            BorderFactory.createEmptyBorder(30, 30, 30, 30) // Padding aumentado
        ));
        formPanel.setPreferredSize(new Dimension(0, 280)); // Altura ajustada
        
        JPanel camposPanel = new JPanel(new GridBagLayout()); // Usando GridBagLayout
        camposPanel.setBackground(CLEAN_WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 30, 0); // Espacamento vertical entre as "linhas" (ajustado para 30px)
        gbc.anchor = GridBagConstraints.WEST; // Alinha os componentes a esquerda
        gbc.fill = GridBagConstraints.HORIZONTAL; // Permite que os componentes preencham o espaco horizontal
        
        // Linha 1: CRM, Nome, Especialidade
        gbc.gridy = 0; // Primeira linha
        gbc.weightx = 0.0; // Resetar peso horizontal
        gbc.gridx = 0;
        adicionarCampoCRM(camposPanel, gbc);

        gbc.gridx = 1;
        adicionarCampoNome(camposPanel, gbc);
        
        gbc.gridx = 2;
        gbc.gridwidth = GridBagConstraints.REMAINDER; // Ocupa o restante da linha
        adicionarCampoEspecialidade(camposPanel, gbc);
        
        // Linha 2: Sala, Horario Inicio, Horario Fim, Dias de Atendimento
        gbc.gridy = 1; // Segunda linha
        gbc.insets = new Insets(0, 0, 0, 0); // Ultima linha nao precisa de espacamento vertical abaixo
        gbc.gridwidth = 1; // Reset gridwidth
        gbc.weightx = 0.0; // Reset weightx
        gbc.gridx = 0;
        adicionarCampoSala(camposPanel, gbc);

        gbc.gridx = 1;
        adicionarCampoHorarioInicio(camposPanel, gbc);

        gbc.gridx = 2;
        adicionarCampoHorarioFim(camposPanel, gbc);

        gbc.gridx = 3;
        gbc.gridwidth = GridBagConstraints.REMAINDER; // Ocupa o restante da linha
        gbc.weightx = 1.0; // Permite que os checkboxes se expandam horizontalmente
        gbc.fill = GridBagConstraints.BOTH; // Preenche horizontalmente e verticalmente
        adicionarCamposDiasAtendimento(camposPanel, gbc);
        
        formPanel.add(camposPanel, BorderLayout.CENTER);
    }
    
    /**
     * Adiciona o campo de entrada de CRM ao painel do formulario.
     * Utiliza uma mascara para formatacao "CRM######" e possui validacao de formato.
     * @param parentPanel O JPanel pai onde o componente sera adicionado (camposPanel).
     * @param gbc As restricoes de GridBagLayout para posicionamento.
     *
     * AJUSTE: Altura dos campos aumentada para 40px.
     */
    private void adicionarCampoCRM(JPanel parentPanel, GridBagConstraints gbc) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(CLEAN_WHITE);
        container.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20)); // Espacamento a direita
        
        JLabel lblCrm = new JLabel("CRM:");
        lblCrm.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblCrm.setForeground(DARK_TEXT);
        lblCrm.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        try {
            MaskFormatter crmMask = new MaskFormatter("CRM######");
            crmMask.setPlaceholderCharacter('_');
            txtCrm = new JFormattedTextField(crmMask);
        } catch (ParseException e) {
            txtCrm = new JTextField(); // Fallback para JTextField
        }
        
        txtCrm.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtCrm.setPreferredSize(new Dimension(180, 40)); // Altura ajustada
        txtCrm.setMaximumSize(new Dimension(180, 40));
        txtCrm.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        txtCrm.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        container.add(lblCrm);
        container.add(Box.createVerticalStrut(8));
        container.add(txtCrm);
        parentPanel.add(container, gbc);
    }
    
    /**
     * Adiciona o campo de entrada de Nome Completo ao painel do formulario.
     * @param parentPanel O JPanel pai onde o componente sera adicionado (camposPanel).
     * @param gbc As restricoes de GridBagLayout para posicionamento.
     *
     * AJUSTE: Altura dos campos aumentada para 40px.
     */
    private void adicionarCampoNome(JPanel parentPanel, GridBagConstraints gbc) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(CLEAN_WHITE);
        container.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20)); // Espacamento a direita
        
        JLabel lblNome = new JLabel("Nome Completo:");
        lblNome.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblNome.setForeground(DARK_TEXT);
        lblNome.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        txtNome = new JTextField();
        // Nome Completo;
        txtNome.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtNome.setPreferredSize(new Dimension(300, 40)); // Altura ajustada
        txtNome.setMaximumSize(new Dimension(300, 40));
        txtNome.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        txtNome.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        container.add(lblNome);
        container.add(Box.createVerticalStrut(8));
        container.add(txtNome);
        parentPanel.add(container, gbc);
    }
    
    /**
     * Adiciona o campo de selecao de Especialidade ao painel do formulario.
     * O ComboBox e preenchido com especialidades pre-definidas e e editavel.
     * @param parentPanel O JPanel pai onde o componente sera adicionado (camposPanel).
     * @param gbc As restricoes de GridBagLayout para posicionamento.
     *
     * AJUSTE: Altura dos campos aumentada para 40px.
     */
    private void adicionarCampoEspecialidade(JPanel parentPanel, GridBagConstraints gbc) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(CLEAN_WHITE);
        
        JLabel lblEspecialidade = new JLabel("Especialidade:");
        lblEspecialidade.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblEspecialidade.setForeground(DARK_TEXT);
        lblEspecialidade.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        String[] especialidades = {
            "Cardiologia", "Pediatria", "Ortopedia", "Dermatologia", 
            "Ginecologia", "Neurologia", "Oftalmologia", "Psiquiatria",
            "Urologia", "Endocrinologia"
        };
        
        cbEspecialidade = new JComboBox<>(especialidades);
        cbEspecialidade.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        cbEspecialidade.setPreferredSize(new Dimension(200, 40)); // Altura ajustada
        cbEspecialidade.setMaximumSize(new Dimension(200, 40));
        cbEspecialidade.setEditable(true); // Permite digitar novas especialidades
        cbEspecialidade.setBackground(CLEAN_WHITE);
        cbEspecialidade.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        container.add(lblEspecialidade);
        container.add(Box.createVerticalStrut(8));
        container.add(cbEspecialidade);
        parentPanel.add(container, gbc);
    }
    
    /**
     * Adiciona o campo de entrada de Sala de Atendimento ao painel do formulario.
     * @param parentPanel O JPanel pai onde o componente sera adicionado (camposPanel).
     * @param gbc As restricoes de GridBagLayout para posicionamento.
     *
     * AJUSTE: Altura dos campos aumentada para 40px.
     */
    private void adicionarCampoSala(JPanel parentPanel, GridBagConstraints gbc) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(CLEAN_WHITE);
        container.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20)); // Espacamento a direita
        
        JLabel lblSala = new JLabel("Sala:");
        lblSala.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblSala.setForeground(DARK_TEXT);
        lblSala.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        txtSala = new JTextField();
        txtSala.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtSala.setPreferredSize(new Dimension(120, 40)); // Altura ajustada
        txtSala.setMaximumSize(new Dimension(120, 40));
        txtSala.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        txtSala.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        container.add(lblSala);
        container.add(Box.createVerticalStrut(8));
        container.add(txtSala);
        parentPanel.add(container, gbc);
    }
    
    /**
     * Adiciona o campo de selecao de Horario de Inicio ao painel do formulario.
     * Utiliza um JSpinner com formato HH:mm e valor padrao 08:00.
     * @param parentPanel O JPanel pai onde o componente sera adicionado (camposPanel).
     * @param gbc As restricoes de GridBagLayout para posicionamento.
     *
     * AJUSTE: Altura dos campos aumentada para 40px.
     */
    private void adicionarCampoHorarioInicio(JPanel parentPanel, GridBagConstraints gbc) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(CLEAN_WHITE);
        container.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20)); // Espacamento a direita
        
        JLabel lblInicio = new JLabel("Horario Inicio:");
        lblInicio.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblInicio.setForeground(DARK_TEXT);
        lblInicio.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        SpinnerDateModel modelInicio = new SpinnerDateModel();
        spinnerInicio = new JSpinner(modelInicio);
        JSpinner.DateEditor editorInicio = new JSpinner.DateEditor(spinnerInicio, "HH:mm");
        spinnerInicio.setEditor(editorInicio);
        spinnerInicio.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        spinnerInicio.setPreferredSize(new Dimension(100, 40)); // Altura ajustada
        spinnerInicio.setMaximumSize(new Dimension(100, 40));
        spinnerInicio.setValue(java.sql.Time.valueOf(LocalTime.of(8, 0))); // Define valor padrao
        spinnerInicio.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        container.add(lblInicio);
        container.add(Box.createVerticalStrut(8));
        container.add(spinnerInicio);
        parentPanel.add(container, gbc);
    }
    
    /**
     * Adiciona o campo de selecao de Horario de Fim ao painel do formulario.
     * Utiliza um JSpinner com formato HH:mm e valor padrao 17:00.
     * @param parentPanel O JPanel pai onde o componente sera adicionado (camposPanel).
     * @param gbc As restricoes de GridBagLayout para posicionamento.
     *
     * AJUSTE: Altura dos campos aumentada para 40px.
     */
    private void adicionarCampoHorarioFim(JPanel parentPanel, GridBagConstraints gbc) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(CLEAN_WHITE);
        container.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20)); // Espacamento a direita
        
        JLabel lblFim = new JLabel("Horario Fim:");
        lblFim.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblFim.setForeground(DARK_TEXT);
        lblFim.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        SpinnerDateModel modelFim = new SpinnerDateModel();
        spinnerFim = new JSpinner(modelFim);
        JSpinner.DateEditor editorFim = new JSpinner.DateEditor(spinnerFim, "HH:mm");
        spinnerFim.setEditor(editorFim);
        spinnerFim.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        spinnerFim.setPreferredSize(new Dimension(100, 40)); // Altura ajustada
        spinnerFim.setMaximumSize(new Dimension(100, 40));
        spinnerFim.setValue(java.sql.Time.valueOf(LocalTime.of(17, 0))); // Define valor padrao
        spinnerFim.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        container.add(lblFim);
        container.add(Box.createVerticalStrut(8));
        container.add(spinnerFim);
        parentPanel.add(container, gbc);
    }
    
    /**
     * Adiciona os checkboxes para selecao dos Dias de Atendimento ao painel do formulario.
     * Os dias sao dispostos em uma unica linha horizontal, com segunda a sexta marcados por padrao.
     * @param parentPanel O JPanel pai onde o componente sera adicionado (camposPanel).
     * @param gbc As restricoes de GridBagLayout para posicionamento.
     *
     * AJUSTE: Altura dos checkboxes ajustada para 30px.
     */
    private void adicionarCamposDiasAtendimento(JPanel parentPanel, GridBagConstraints gbc) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(CLEAN_WHITE);
        
        JLabel lblDias = new JLabel("Dias de Atendimento:");
        lblDias.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblDias.setForeground(DARK_TEXT);
        lblDias.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel diasPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        diasPanel.setBackground(CLEAN_WHITE);
        diasPanel.setMaximumSize(new Dimension(500, 40)); // Altura ajustada
        diasPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        String[] dias = {"Seg", "Ter", "Qua", "Qui", "Sex", "Sab", "Dom"};
        checkDias = new JCheckBox[7];
        
        for (int i = 0; i < dias.length; i++) {
            checkDias[i] = new JCheckBox(dias[i]);
            checkDias[i].setFont(new Font("Segoe UI", Font.PLAIN, 14));
            checkDias[i].setBackground(CLEAN_WHITE);
            checkDias[i].setPreferredSize(new Dimension(60, 30)); // Altura ajustada
            
            if (i < 5) checkDias[i].setSelected(true); // Segunda a sexta por padrao
            
            diasPanel.add(checkDias[i]);
        }
        
        container.add(lblDias);
        container.add(Box.createVerticalStrut(8));
        container.add(diasPanel);
        parentPanel.add(container, gbc);
    }
    
    /**
     * Cria e configura a tabela de listagem de medicos.
     * Define o modelo da tabela, suas colunas, estilos de fonte e linha,
     * e um listener para carregar os dados do medico selecionado no formulario.
     */
    private void criarTabelaMedicos() {
        tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(CLEAN_WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_BLUE, 2),
                "Lista de Medicos Cadastrados",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 18),
                PRIMARY_BLUE
            ),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        String[] colunas = {"CRM", "Nome", "Especialidade", "Horario", "Sala"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabela somente leitura
            }
        };
        
        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        table.setRowHeight(30);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Permite apenas uma selecao
        table.setSelectionBackground(new Color(52, 144, 220, 30)); // Cor de selecao sutil
        table.setSelectionForeground(DARK_TEXT);
        table.setGridColor(new Color(220, 220, 220)); // Cor das linhas da grade
        
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));
        table.getTableHeader().setBackground(LIGHT_GRAY);
        table.getTableHeader().setForeground(DARK_TEXT);
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        
        // Listener para carregar medico selecionado no formulario
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) { // Evita multiplos eventos
                carregarMedicoSelecionado();
            }
        });
        
        // Configuracao de largura preferencial das colunas
        table.getColumnModel().getColumn(0).setPreferredWidth(120);
        table.getColumnModel().getColumn(1).setPreferredWidth(250);
        table.getColumnModel().getColumn(2).setPreferredWidth(180);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        tablePanel.add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * Cria e configura o painel de botoes de acao (CRUD e navegacao) no rodape da tela.
     * Inclui botoes para novo, salvar, excluir, gerenciar pacientes,
     * ver relatorios, ver agenda e fechar a tela (retornando ao menu principal).
     */
    private void criarBotoesAcao() {
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonPanel.setBackground(LIGHT_GRAY);
        buttonPanel.setPreferredSize(new Dimension(0, 80));
        
        btnNovo = criarBotaoAcao("Novo Medico", SUCCESS_GREEN, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                limparFormulario();
            }
        });
        
        btnSalvar = criarBotaoAcao("Salvar", PRIMARY_BLUE, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                salvarMedico();
            }
        });
        
        btnExcluir = criarBotaoAcao("Excluir", ACCENT_RED, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                excluirMedico();
            }
        });

        // Botoes de navegacao adicionais
        btnIrParaPacientes = criarBotaoAcao("Gerenciar Pacientes", ORANGE_ACCENT, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirTelaPacientes();
            }
        });

        btnIrParaRelatorios = criarBotaoAcao("Ver Relatorios", GRAY_ACCENT, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirTelaRelatorios();
            }
        });

        btnIrParaAgendaCalendario = criarBotaoAcao("Ver Agenda", PRIMARY_BLUE, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirTelaAgendaCalendario();
            }
        });
        
        // Botao para fechar a tela e retornar ao menu principal
        btnFechar = criarBotaoAcao("Fechar", DARK_TEXT, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Fecha a janela atual
                trazerTelaPrincipalParaFrente(); // Retorna para a TelaPrincipal existente
            }
        });
        
        // Adicionar botoes ao painel em ordem logica
        buttonPanel.add(btnNovo);
        buttonPanel.add(btnSalvar);
        buttonPanel.add(btnExcluir);
        buttonPanel.add(btnIrParaPacientes);
        buttonPanel.add(btnIrParaRelatorios);
        buttonPanel.add(btnIrParaAgendaCalendario);
        buttonPanel.add(btnFechar);
    }
    
    /**
     * Cria um botao de acao padronizado com estilo moderno para uso nos paineis de acao.
     * Inclui configuracoes de fonte, cor, tamanho preferencial, borda e efeito de hover.
     * @param texto Texto visivel no botao.
     * @param cor Cor de fundo do botao.
     * @param acao O ActionListener a ser executado quando o botao e clicado.
     * @return JButton configurado.
     */
    private JButton criarBotaoAcao(String texto, Color cor, ActionListener acao) {
        JButton botao = new JButton(texto);
        botao.setFont(new Font("Segoe UI", Font.BOLD, 16));
        botao.setForeground(CLEAN_WHITE);
        botao.setBackground(cor);
        botao.setPreferredSize(new Dimension(200, 50)); // Tamanho padrao para botoes de acao
        botao.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        botao.setFocusPainted(false);
        botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
        botao.addActionListener(acao);
        
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
     * O headerPanel e o formPanel sao agrupados no topo (BorderLayout.NORTH),
     * a tablePanel ocupa o centro, e o buttonPanel fica na parte inferior.
     */
    private void organizarLayout() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(formPanel, BorderLayout.CENTER);
        
        contentPane.add(topPanel, BorderLayout.NORTH);
        contentPane.add(tablePanel, BorderLayout.CENTER);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Aplica estilos visuais finais aos paineis principais.
     * Adiciona bordas e efeitos para aprimorar a estetica da interface.
     */
    private void aplicarEstilosVisuais() {
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0, 0, 0, 30)),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
        
        // A borda do formPanel ja e definida em criarFormularioReorganizado().
    }
    
    // ========== METODOS DE FUNCIONALIDADE (MANTIDOS E APRIMORADOS) ==========
    
    /**
     * Carrega todos os medicos do banco de dados e os exibe na tabela de medicos.
     * Limpa o modelo da tabela e o preenche com os dados mais recentes.
     *
     * Comunicacao com Outras Classes:
     * - MedicoDAO: Para buscar todos os medicos.
     */
    private void carregarMedicos() {
        try {
            List<Medico> medicos = medicoDAO.findAll();
            tableModel.setRowCount(0); // Limpa todas as linhas existentes
            
            for (Medico medico : medicos) {
                String horario = medico.getHorarioInicio().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) + 
                                 " as " + 
                                 medico.getHorarioFim().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
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
            JOptionPane.showMessageDialog(this, 
                "Erro ao carregar medicos: " + e.getMessage(),
                "Erro de Carregamento", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Carrega os dados do medico selecionado na tabela para o formulario de edicao.
     * Habilita a edicao dos campos com as informacoes do medico.
     *
     * Comunicacao com Outras Classes:
     * - MedicoDAO: Para buscar o medico pelo CRM.
     */
    private void carregarMedicoSelecionado() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            String crm = (String) tableModel.getValueAt(selectedRow, 0);
            try {
                medicoSelecionado = medicoDAO.findById(crm); // Busca o medico completo pelo CRM
                if (medicoSelecionado != null) {
                    preencherFormulario(medicoSelecionado);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Erro ao carregar medico: " + e.getMessage(),
                    "Erro de Carregamento", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Preenche os campos do formulario com os dados de um medico.
     * Utilizado para exibir os dados de um medico selecionado na tabela para edicao.
     * @param medico O objeto Medico cujos dados serao exibidos no formulario.
     */
    private void preencherFormulario(Medico medico) {
        txtCrm.setText(medico.getCrm());
        txtNome.setText(medico.getNome());
        cbEspecialidade.setSelectedItem(medico.getEspecialidade());
        txtSala.setText(medico.getSalaAtendimento());
        
        // Converte LocalTime para java.util.Date para JSpinner
        spinnerInicio.setValue(java.sql.Time.valueOf(medico.getHorarioInicio()));
        spinnerFim.setValue(java.sql.Time.valueOf(medico.getHorarioFim()));
        
        // Desmarca todos os checkboxes de dias antes de marcar os corretos
        for (JCheckBox check : checkDias) {
            check.setSelected(false);
        }
        
        String[] diasCodigo = {"seg", "ter", "qua", "qui", "sex", "sab", "dom"};
        for (int i = 0; i < diasCodigo.length; i++) {
            if (medico.getDiasAtendimento().contains(diasCodigo[i])) {
                checkDias[i].setSelected(true);
            }
        }
    }
    
    /**
     * Limpa todos os campos do formulario, restaurando-os ao estado inicial
     * para permitir o cadastro de um novo medico.
     */
    private void limparFormulario() {
        txtCrm.setText("");
        txtNome.setText("");
        cbEspecialidade.setSelectedIndex(0); // Volta para a primeira especialidade
        txtSala.setText("");
        spinnerInicio.setValue(java.sql.Time.valueOf(LocalTime.of(8, 0))); // Horario padrao
        spinnerFim.setValue(java.sql.Time.valueOf(LocalTime.of(17, 0))); // Horario padrao
        
        // Marca segunda a sexta por padrao
        for (int i = 0; i < checkDias.length; i++) {
            checkDias[i].setSelected(i < 5);
        }
        
        medicoSelecionado = null; // Reseta o medico selecionado
        table.clearSelection(); // Limpa a selecao da tabela
    }
    
    /**
     * Salva um novo medico ou atualiza um medico existente no banco de dados.
     * Realiza validacoes dos campos do formulario antes de persistir os dados.
     *
     * Comunicacao com Outras Classes:
     * - MedicoDAO: Para salvar ou atualizar o medico.
     * - ValidadorCRM: Para validar o formato do CRM.
     * - Medico: Cria e atualiza o objeto Medico.
     */
    private void salvarMedico() {
        try {
            // Validacoes de campos obrigatorios
            if (txtCrm.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "CRM e obrigatorio!", 
                    "Erro de Validacao", JOptionPane.ERROR_MESSAGE);
                txtCrm.requestFocus();
                return;
            }
            
            if (!ValidadorCRM.validar(txtCrm.getText().trim())) {
                JOptionPane.showMessageDialog(this, "CRM deve ter formato CRM seguido de numeros!", 
                    "Erro de Validacao", JOptionPane.ERROR_MESSAGE);
                txtCrm.requestFocus();
                return;
            }
            
            if (txtNome.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nome e obrigatorio!", 
                    "Erro de Validacao", JOptionPane.ERROR_MESSAGE);
                txtNome.requestFocus();
                return;
            }
            
            if (txtSala.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Sala e obrigatoria!", 
                    "Erro de Validacao", JOptionPane.ERROR_MESSAGE);
                txtSala.requestFocus();
                return;
            }
            
            List<String> diasSelecionados = new ArrayList<>();
            String[] diasCodigo = {"seg", "ter", "qua", "qui", "sex", "sab", "dom"};
            
            // Coleta os dias de atendimento selecionados
            for (int i = 0; i < checkDias.length; i++) {
                if (checkDias[i].isSelected()) {
                    diasSelecionados.add(diasCodigo[i]);
                }
            }
            
            if (diasSelecionados.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Selecione pelo menos um dia de atendimento!", 
                    "Erro de Validacao", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Obtem e valida os horarios de inicio e fim
            java.util.Date horaInicio = (java.util.Date) spinnerInicio.getValue();
            java.util.Date horaFim = (java.util.Date) spinnerFim.getValue();
            LocalTime inicio = LocalTime.of(horaInicio.getHours(), horaInicio.getMinutes());
            LocalTime fim = LocalTime.of(horaFim.getHours(), horaFim.getMinutes());
            
            if (!inicio.isBefore(fim)) {
                JOptionPane.showMessageDialog(this, "Horario de inicio deve ser menor que horario de fim!", 
                    "Erro de Validacao", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Medico medico = new Medico();
            String crmTexto = txtCrm.getText();
            String crm = crmTexto != null ? crmTexto.trim().toUpperCase() : "";
            medico.setCrm(crm);
            medico.setNome(txtNome.getText().trim());
            medico.setEspecialidade((String) cbEspecialidade.getSelectedItem());
            medico.setSalaAtendimento(txtSala.getText().trim());
            medico.setHorarioInicio(inicio);
            medico.setHorarioFim(fim);
            medico.setDiasAtendimento(diasSelecionados);
            
            // Decide entre salvar (novo) ou atualizar (existente)
            if (medicoSelecionado == null) {
                medicoDAO.save(medico);
                JOptionPane.showMessageDialog(this, "Medico cadastrado com sucesso!", 
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                int opcao = JOptionPane.showConfirmDialog(
                    this,
                    "Deseja realmente atualizar os dados do medico " + medicoSelecionado.getNome() + "?",
                    "Confirmar Alteracao",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );
                
                if (opcao == JOptionPane.YES_OPTION) {
                    medicoDAO.update(medico);
                    JOptionPane.showMessageDialog(this, "Medico atualizado com sucesso!", 
                        "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    return; // Usuario cancelou a atualizacao
                }
            }
            
            carregarMedicos(); // Recarrega a tabela para exibir as mudancas
            limparFormulario(); // Limpa o formulario para um novo cadastro/edicao
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar medico: " + e.getMessage(), 
                "Erro de Sistema", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Exclui o medico selecionado na tabela do banco de dados.
     * Solicita confirmacao do usuario antes de realizar a exclusao, pois e uma acao irreversivel.
     *
     * Comunicacao com Outras Classes:
     * - MedicoDAO: Para excluir o medico.
     */
    private void excluirMedico() {
        if (medicoSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um medico para excluir!", 
                "Selecao Necessaria", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int opcao = JOptionPane.showConfirmDialog(
            this,
            "Deseja realmente excluir o medico " + medicoSelecionado.getNome() + "?\n" +
            "Esta acao nao pode ser desfeita!",
            "Confirmar Exclusao",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (opcao == JOptionPane.YES_OPTION) {
            try {
                medicoDAO.delete(medicoSelecionado.getCrm());
                JOptionPane.showMessageDialog(this, "Medico excluido com sucesso!", 
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                
                carregarMedicos(); // Recarrega a tabela apos a exclusao
                limparFormulario(); // Limpa o formulario
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir medico: " + e.getMessage(), 
                    "Erro de Sistema", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ========== METODOS DE NAVEGACAO (Novos) ==========

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

    /**
     * Abre a TelaPacientes para gerenciar os dados dos pacientes.
     *
     * Comunicacao com Outras Classes:
     * - TelaPacientes: Abre uma nova instancia e a torna visivel.
     */
    private void abrirTelaPacientes() {
        TelaPacientes tela = new TelaPacientes();
        tela.setVisible(true);
        tela.toFront();
    }

    /**
     * Abre a TelaRelatorios para visualizar relatorios do sistema.
     *
     * Comunicacao com Outras Classes:
     * - TelaRelatorios: Abre uma nova instancia e a torna visivel.
     */
    private void abrirTelaRelatorios() {
        TelaRelatorios tela = new TelaRelatorios();
        tela.setVisible(true);
        tela.toFront();
    }

    /**
     * Abre a TelaAgendaCalendario para visualizar a agenda de consultas em formato de calendario.
     *
     * Comunicacao com Outras Classes:
     * - TelaAgendaCalendario: Abre uma nova instancia e a torna visivel.
     */
    private void abrirTelaAgendaCalendario() {
        TelaAgendaCalendario tela = new TelaAgendaCalendario();
        tela.setVisible(true);
        tela.toFront();
    }
}