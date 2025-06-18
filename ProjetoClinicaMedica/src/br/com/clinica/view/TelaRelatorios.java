package br.com.clinica.view;

import br.com.clinica.dao.ConsultaDAO;
import br.com.clinica.dao.MedicoDAO;
import br.com.clinica.dao.PacienteDAO;
import br.com.clinica.model.Consulta;
import br.com.clinica.model.Medico;
import br.com.clinica.model.Paciente;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

/**
 * Tela de relatórios gerenciais com interface moderna reorganizada.
 * Dashboard analítico completo para análise de dados da clínica médica.
 * Layout horizontal otimizado para máximo aproveitamento do espaço.
 *
 * Estrutura da interface:
 * - Header: Título da seção e botões de navegação.
 * - Filtros: Organizados em uma linha horizontal bem distribuída.
 * • Tipo de Relatório | Médico | Mês | Ano | Botão Gerar.
 * - Área de Resultados: Dimensionamento otimizado.
 * • Tabela: Expansível para acomodar dados.
 * • Resumo: Altura adequada para estatísticas.
 *
 * Layout horizontal aproveitando toda largura:
 * - Filtros distribuídos uniformemente na horizontal.
 * - Campos dimensionados para total visibilidade.
 * - Espaçamento adequado entre componentes.
 * - Aproveitamento máximo do espaço disponível.
 *
 * Tipos de relatórios disponíveis:
 * - Consultas por Médico, Pacientes por Especialidade.
 * - Consultas Canceladas, Histórico do Paciente.
 * - Distribuição de Consultas, Pacientes sem Consulta.
 *
 * Comunicação com Outras Classes:
 * - ConsultaDAO: Para acesso a dados de consultas.
 * - MedicoDAO: Para acesso a dados de médicos (filtros e relatórios).
 * - PacienteDAO: Para acesso a dados de pacientes (filtros e relatórios).
 * - TelaPrincipal: Para retornar ao menu principal.
 * - TelaMedicos: Para navegar para a tela de gerenciamento de médicos.
 * - TelaPacientes: Para navegar para a tela de gerenciamento de pacientes.
 * - TelaAgendamento: Para navegar para a tela de agendamento de consultas.
 * - TelaAgendaCalendario: Para navegar para a tela da agenda em formato de calendário.
 */
public class TelaRelatorios extends JFrame {

    private static final long serialVersionUID = 1L;

    // Paleta de cores médica profissional
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

    // Componentes principais
    private JPanel contentPane;
    private JPanel headerPanel;
    private JPanel filtrosPanel;
    private JPanel resultadosPanel;
    private JPanel resumoPanel;

    // Componentes de filtros organizados horizontalmente
    private JComboBox<String> cbTipoRelatorio;
    private JComboBox<Medico> cbMedico;
    private JComboBox<String> cbMes;
    private JComboBox<Integer> cbAno;
    private JButton btnGerar;

    // Componentes de resultados
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextArea txtResumo;

    // DAOs para acesso aos dados
    private ConsultaDAO consultaDAO;
    private MedicoDAO medicoDAO;
    private PacienteDAO pacienteDAO;

    // Constantes para relatórios
    private final String[] TIPOS_RELATORIO = {
        "Consultas por Médico",
        "Pacientes por Especialidade",
        "Consultas Canceladas",
        "Histórico do Paciente",
        "Distribuição de Consultas",
        "Pacientes sem Consulta (1 ano)"
    };

    private final String[] MESES = {
        "Todos", "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
        "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    };

    // Botões de navegação adicionais
    private JButton btnVoltarMenuPrincipalHeader;
    private JButton btnIrParaMedicos;
    private JButton btnIrParaPacientes;
    private JButton btnIrParaAgendamento;
    private JButton btnIrParaAgendaCalendario;
    private JButton btnFechar;

    /**
     * Construtor principal - inicializa DAOs e interface.
     * Carrega filtros disponíveis na inicialização.
     */
    public TelaRelatorios() {
        this.consultaDAO = new ConsultaDAO();
        this.medicoDAO = new MedicoDAO();
        this.pacienteDAO = new PacienteDAO();

        inicializarInterface();
        carregarFiltros();
    }

    /**
     * Configura a interface principal com layout horizontal otimizado.
     * Aproveitamento máximo do espaço horizontal para filtros.
     */
    private void inicializarInterface() {
        configurarJanela();
        criarComponentesPrincipais();
        organizarLayout();
        aplicarEstilosVisuais();
    }

    /**
     * Configura propriedades básicas da janela.
     * Tela cheia, título e comportamento de fechamento.
     */
    private void configurarJanela() {
        setTitle("Relatórios Gerenciais - Sistema Clínica");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Painel principal com background moderno
        contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(LIGHT_GRAY);
        setContentPane(contentPane);
    }

    /**
     * Cria todos os componentes principais da interface.
     * Header, filtros horizontais, tabela de resultados e resumo.
     */
    private void criarComponentesPrincipais() {
        criarHeader();
        criarFiltrosHorizontais();
        criarTabelaResultados();
        criarPainelResumo();
        criarBotoesNavegacaoInferiores(); // Botões de navegação na parte inferior
    }

    /**
     * Cria o header com título da seção e botões de navegação.
     * Visual consistente com o restante do sistema.
     *
     * Comunicação com Outras Classes:
     * - TelaPrincipal: A tela para a qual o usuário retornará.
     */
    private void criarHeader() {
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_BLUE);
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JPanel headerLeft = new JPanel();
        headerLeft.setLayout(new BoxLayout(headerLeft, BoxLayout.Y_AXIS));
        headerLeft.setOpaque(false);

        JLabel lblTitulo = new JLabel("RELATÓRIOS GERENCIAIS");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(CLEAN_WHITE);

        JLabel lblSubtitulo = new JLabel("Dashboard analítico com dados em tempo real da clínica");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblSubtitulo.setForeground(new Color(255, 255, 255, 200));

        headerLeft.add(lblTitulo);
        headerLeft.add(Box.createVerticalStrut(5));
        headerLeft.add(lblSubtitulo);

        // Botão para voltar ao menu principal no header
        btnVoltarMenuPrincipalHeader = criarBotaoHeaderNav("Voltar ao Menu Principal", CLEAN_WHITE, DARK_TEXT, new ActionListener() {
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
     * Cria um botão padronizado para uso no cabeçalho para navegação.
     * Inclui estilos de fonte, cor, tamanho e um efeito de hover visual.
     * @param texto O texto a ser exibido no botão.
     * @param corTexto A cor do texto do botão.
     * @param corFundo A cor de fundo padrão do botão.
     * @param acao O ActionListener a ser executado quando o botão é clicado.
     * @return Um JButton configurado com o estilo definido.
     */
    private JButton criarBotaoHeaderNav(String texto, Color corTexto, Color corFundo, ActionListener acao) {
        JButton botao = new JButton(texto);
        botao.setFont(new Font("Segoe UI", Font.BOLD, 14));
        botao.setForeground(corTexto);
        botao.setBackground(corFundo);
        botao.setPreferredSize(new Dimension(200, 35));
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
     * Cria filtros organizados em UMA linha horizontal.
     * TODOS os campos visíveis: Tipo | Médico | Mês | Ano | Botão Gerar.
     * Layout FlowLayout para garantir visibilidade total.
     */
    private void criarFiltrosHorizontais() {
        filtrosPanel = new JPanel(new BorderLayout());
        filtrosPanel.setBackground(CLEAN_WHITE);
        filtrosPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_BLUE, 2),
                "Configuração do Relatório",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 18),
                PRIMARY_BLUE
            ),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        filtrosPanel.setPreferredSize(new Dimension(0, 150));

        JPanel linhaHorizontal = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        linhaHorizontal.setBackground(CLEAN_WHITE);

        // Adicionando todos os campos de forma visível
        linhaHorizontal.add(criarCampoTipoRelatorio());
        linhaHorizontal.add(criarCampoMedico());
        linhaHorizontal.add(criarCampoMes());
        linhaHorizontal.add(criarCampoAno());
        linhaHorizontal.add(criarBotaoGerar());

        filtrosPanel.add(linhaHorizontal, BorderLayout.CENTER);
    }

    /**
     * Cria campo Tipo de Relatório.
     * Campo principal para seleção do tipo de relatório.
     */
    private JPanel criarCampoTipoRelatorio() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(CLEAN_WHITE);
        container.setPreferredSize(new Dimension(300, 90));

        JLabel lblTipo = new JLabel("Tipo de Relatório:");
        lblTipo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTipo.setForeground(DARK_TEXT);
        lblTipo.setAlignmentX(Component.LEFT_ALIGNMENT);

        cbTipoRelatorio = new JComboBox<>(TIPOS_RELATORIO);
        cbTipoRelatorio.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        cbTipoRelatorio.setPreferredSize(new Dimension(280, 35));
        cbTipoRelatorio.setMaximumSize(new Dimension(280, 35));
        cbTipoRelatorio.setBackground(CLEAN_WHITE);
        cbTipoRelatorio.setAlignmentX(Component.LEFT_ALIGNMENT);

        container.add(lblTipo);
        container.add(Box.createVerticalStrut(5));
        container.add(cbTipoRelatorio);

        return container;
    }

    /**
     * Cria campo Médico.
     * ComboBox com médicos cadastrados + opção "Todos".
     */
    private JPanel criarCampoMedico() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(CLEAN_WHITE);
        container.setPreferredSize(new Dimension(240, 90));

        JLabel lblMedico = new JLabel("Médico:");
        lblMedico.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblMedico.setForeground(DARK_TEXT);
        lblMedico.setAlignmentX(Component.LEFT_ALIGNMENT);

        cbMedico = new JComboBox<>();
        cbMedico.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        cbMedico.setPreferredSize(new Dimension(220, 35));
        cbMedico.setMaximumSize(new Dimension(220, 35));
        cbMedico.setBackground(CLEAN_WHITE);
        cbMedico.setAlignmentX(Component.LEFT_ALIGNMENT);

        container.add(lblMedico);
        container.add(Box.createVerticalStrut(5));
        container.add(cbMedico);

        return container;
    }

    /**
     * Cria campo Mês.
     * ComboBox com meses do ano + opção "Todos".
     */
    private JPanel criarCampoMes() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(CLEAN_WHITE);
        container.setPreferredSize(new Dimension(150, 90));

        JLabel lblMes = new JLabel("Mês:");
        lblMes.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblMes.setForeground(DARK_TEXT);
        lblMes.setAlignmentX(Component.LEFT_ALIGNMENT);

        cbMes = new JComboBox<>(MESES);
        cbMes.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        cbMes.setPreferredSize(new Dimension(130, 35));
        cbMes.setMaximumSize(new Dimension(130, 35));
        cbMes.setBackground(CLEAN_WHITE);
        cbMes.setAlignmentX(Component.LEFT_ALIGNMENT);

        container.add(lblMes);
        container.add(Box.createVerticalStrut(5));
        container.add(cbMes);

        return container;
    }

    /**
     * Cria campo Ano.
     * ComboBox com anos (atual ± 5 anos) + opção "Todos".
     */
    private JPanel criarCampoAno() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(CLEAN_WHITE);
        container.setPreferredSize(new Dimension(120, 90));

        JLabel lblAno = new JLabel("Ano:");
        lblAno.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblAno.setForeground(DARK_TEXT);
        lblAno.setAlignmentX(Component.LEFT_ALIGNMENT);

        cbAno = new JComboBox<>();
        cbAno.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        cbAno.setPreferredSize(new Dimension(100, 35));
        cbAno.setMaximumSize(new Dimension(100, 35));
        cbAno.setBackground(CLEAN_WHITE);
        cbAno.setAlignmentX(Component.LEFT_ALIGNMENT);

        container.add(lblAno);
        container.add(Box.createVerticalStrut(5));
        container.add(cbAno);

        return container;
    }

    /**
     * Cria botão gerar relatório.
     * Botão de destaque com hover effect.
     */
    private JPanel criarBotaoGerar() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(CLEAN_WHITE);
        container.setPreferredSize(new Dimension(140, 90));

        // Label vazio para alinhamento vertical
        JLabel lblEspaco = new JLabel(" ");
        lblEspaco.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblEspaco.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnGerar = new JButton("Gerar");
        btnGerar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnGerar.setForeground(CLEAN_WHITE);
        btnGerar.setBackground(SUCCESS_GREEN);
        btnGerar.setPreferredSize(new Dimension(120, 35));
        btnGerar.setMaximumSize(new Dimension(120, 35));
        btnGerar.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btnGerar.setFocusPainted(false);
        btnGerar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGerar.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnGerar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gerarRelatorio();
            }
        });

        btnGerar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnGerar.setBackground(SUCCESS_GREEN.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnGerar.setBackground(SUCCESS_GREEN);
            }
        });

        container.add(lblEspaco);
        container.add(Box.createVerticalStrut(5));
        container.add(btnGerar);

        return container;
    }

    /**
     * Cria tabela de resultados com formatação moderna.
     * Tabela responsiva para exibição dos dados do relatório.
     */
    private void criarTabelaResultados() {
        resultadosPanel = new JPanel(new BorderLayout());
        resultadosPanel.setBackground(CLEAN_WHITE);
        resultadosPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_BLUE, 2),
                "Resultados do Relatório",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 18),
                PRIMARY_BLUE
            ),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Inicializar modelo da tabela
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabela somente leitura
            }
        };

        // Configurar tabela
        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(52, 144, 220, 30));
        table.setSelectionForeground(DARK_TEXT);
        table.setGridColor(new Color(220, 220, 220));
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);

        // Configurar header da tabela
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));
        table.getTableHeader().setBackground(LIGHT_GRAY);
        table.getTableHeader().setForeground(DARK_TEXT);
        table.getTableHeader().setPreferredSize(new Dimension(0, 35));

        // ScrollPane para a tabela
        JScrollPane scrollTable = new JScrollPane(table);
        scrollTable.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        scrollTable.setBackground(CLEAN_WHITE);

        resultadosPanel.add(scrollTable, BorderLayout.CENTER);
    }

    /**
     * Cria painel de resumo com estatísticas do relatório.
     * Área otimizada para exibição de métricas.
     */
    private void criarPainelResumo() {
        resumoPanel = new JPanel(new BorderLayout());
        resumoPanel.setBackground(CLEAN_WHITE);
        resumoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(23, 162, 184), 2),
                "Resumo e Estatísticas",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 18),
                new Color(23, 162, 184)
            ),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        resumoPanel.setPreferredSize(new Dimension(0, 180));

        // Área de texto para o resumo
        txtResumo = new JTextArea();
        txtResumo.setEditable(false);
        txtResumo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtResumo.setBackground(new Color(248, 249, 250));
        txtResumo.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        txtResumo.setLineWrap(true);
        txtResumo.setWrapStyleWord(true);

        // ScrollPane para o resumo
        JScrollPane scrollResumo = new JScrollPane(txtResumo);
        scrollResumo.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        scrollResumo.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        resumoPanel.add(scrollResumo, BorderLayout.CENTER);
    }

    /**
     * Cria e configura o painel de botões de navegação no rodapé da tela.
     * Inclui botões para navegar entre as principais telas do sistema e fechar.
     */
    private void criarBotoesNavegacaoInferiores() {
        JPanel navigationButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        navigationButtonPanel.setBackground(LIGHT_GRAY);
        navigationButtonPanel.setPreferredSize(new Dimension(0, 80));

        btnIrParaAgendamento = criarBotaoAcao("Agendar Consulta", MEDICAL_GREEN, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirTelaAgendamento();
            }
        });

        btnIrParaAgendaCalendario = criarBotaoAcao("Visualizar Agenda", PRIMARY_BLUE, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirTelaAgendaCalendario();
            }
        });

        btnIrParaMedicos = criarBotaoAcao("Gerenciar Médicos", PURPLE_ACCENT, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirTelaMedicos();
            }
        });

        btnIrParaPacientes = criarBotaoAcao("Gerenciar Pacientes", ORANGE_ACCENT, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirTelaPacientes();
            }
        });

        btnFechar = criarBotaoAcao("Fechar", DARK_TEXT, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Fecha a janela atual
                trazerTelaPrincipalParaFrente(); // Retorna para a TelaPrincipal existente
            }
        });

        navigationButtonPanel.add(btnIrParaAgendamento);
        navigationButtonPanel.add(btnIrParaAgendaCalendario);
        navigationButtonPanel.add(btnIrParaMedicos);
        navigationButtonPanel.add(btnIrParaPacientes);
        navigationButtonPanel.add(btnFechar);

        // Adicionar o painel de botões de navegação ao contentPane
        contentPane.add(navigationButtonPanel, BorderLayout.SOUTH);
    }

    /**
     * Cria um botão de ação padronizado com estilo moderno para uso nos painéis de ação.
     * Inclui configurações de fonte, cor, tamanho preferencial, borda e efeito de hover.
     * @param texto Texto visível no botão.
     * @param cor Cor de fundo do botão.
     * @param acao O ActionListener a ser executado quando o botão é clicado.
     * @return JButton configurado.
     */
    private JButton criarBotaoAcao(String texto, Color cor, ActionListener acao) {
        JButton botao = new JButton(texto);
        botao.setFont(new Font("Segoe UI", Font.BOLD, 16));
        botao.setForeground(CLEAN_WHITE);
        botao.setBackground(cor);
        botao.setPreferredSize(new Dimension(200, 50));
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
     * Organiza o layout final dos painéis na janela principal (contentPane).
     * O headerPanel e o filtrosPanel são agrupados no topo (BorderLayout.NORTH),
     * a resultadosPanel ocupa o centro, e o resumoPanel fica na parte inferior,
     * juntamente com o navigationButtonPanel.
     */
    private void organizarLayout() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(filtrosPanel, BorderLayout.CENTER); // Filtros abaixo do header

        contentPane.add(topPanel, BorderLayout.NORTH); // Topo da janela

        JPanel centerAndBottomPanel = new JPanel(new BorderLayout());
        centerAndBottomPanel.add(resultadosPanel, BorderLayout.CENTER); // Tabela no centro
        centerAndBottomPanel.add(resumoPanel, BorderLayout.SOUTH); // Resumo abaixo da tabela

        contentPane.add(centerAndBottomPanel, BorderLayout.CENTER); // Centro da janela
        // Botões de navegação já adicionados em criarBotoesNavegacaoInferiores() para BorderLayout.SOUTH do contentPane
    }

    /**
     * Aplica estilos visuais finais.
     * Adiciona bordas e efeitos para aprimorar a estética da interface.
     */
    private void aplicarEstilosVisuais() {
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0, 0, 0, 30)),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
    }

    // ========== MÉTODOS DE FUNCIONALIDADE (MANTIDOS) ==========

    /**
     * Carrega filtros disponíveis nos ComboBoxes.
     * Médicos cadastrados e range de anos para seleção.
     */
    private void carregarFiltros() {
        try {
            // Carregar médicos
            cbMedico.removeAllItems();
            cbMedico.addItem(null);

            List<Medico> medicos = medicoDAO.findAll();
            for (Medico medico : medicos) {
                cbMedico.addItem(medico);
            }

            // Renderer personalizado para exibir "Todos" quando null
            cbMedico.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value,
                        int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value == null) {
                        setText("Todos");
                    }
                    return this;
                }
            });

            // Carregar anos (atual ± 5 anos)
            cbAno.removeAllItems();
            cbAno.addItem(null);

            int anoAtual = LocalDateTime.now().getYear();
            for (int ano = anoAtual - 5; ano <= anoAtual + 2; ano++) {
                cbAno.addItem(ano);
            }

            // Renderer personalizado para exibir "Todos" quando null
            cbAno.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value,
                        int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value == null) {
                        setText("Todos");
                    }
                    return this;
                }
            });

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erro ao carregar filtros: " + e.getMessage(),
                "Erro de Carregamento",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Filtra consultas baseado nos critérios selecionados.
     * @param consultas Lista de todas as consultas a serem filtradas.
     * @return Lista de consultas filtradas.
     */
    private List<Consulta> filtrarConsultas(List<Consulta> consultas) {
        List<Consulta> consultasFiltradas = new ArrayList<>();

        Medico medicoSelecionado = (Medico) cbMedico.getSelectedItem();
        String mesSelecionado = (String) cbMes.getSelectedItem();
        Integer anoSelecionado = (Integer) cbAno.getSelectedItem();

        for (Consulta consulta : consultas) {
            boolean incluir = true;

            if (medicoSelecionado != null) {
                if (!consulta.getMedico().getCrm().equals(medicoSelecionado.getCrm())) {
                    incluir = false;
                }
            }

            if (!"Todos".equals(mesSelecionado)) {
                int mesConsulta = consulta.getDataHorario().getMonthValue();
                int mesIndice = Arrays.asList(MESES).indexOf(mesSelecionado);
                if (mesIndice != mesConsulta) {
                    incluir = false;
                }
            }

            if (anoSelecionado != null) {
                int anoConsulta = consulta.getDataHorario().getYear();
                if (anoConsulta != anoSelecionado) {
                    incluir = false;
                }
            }

            if (incluir) {
                consultasFiltradas.add(consulta);
            }
        }

        return consultasFiltradas;
    }

    /**
     * Gera relatório baseado no tipo selecionado.
     */
    private void gerarRelatorio() {
        String tipo = (String) cbTipoRelatorio.getSelectedItem();

        try {
            switch (tipo) {
                case "Consultas por Médico":
                    gerarConsultasPorMedico();
                    break;
                case "Pacientes por Especialidade":
                    gerarPacientesPorEspecialidade();
                    break;
                case "Consultas Canceladas":
                    gerarConsultasCanceladas();
                    break;
                case "Histórico do Paciente":
                    gerarHistoricoPaciente();
                    break;
                case "Distribuicao de Consultas":
                    gerarDistribuicaoConsultas();
                    break;
                case "Pacientes sem Consulta (1 ano)":
                    gerarPacientesSemConsulta();
                    break;
                default:
                    JOptionPane.showMessageDialog(this,
                        "Relatório não implementado: " + tipo,
                        "Aviso",
                        JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erro ao gerar relatório: " + e.getMessage(),
                "Erro de Sistema",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Gera o relatório de consultas por médico.
     * Exibe a lista de consultas e um resumo estatístico.
     * @throws Exception Se ocorrer um erro ao acessar o banco de dados.
     */
    private void gerarConsultasPorMedico() throws Exception {
        List<Consulta> todasConsultas = consultaDAO.findAll();
        List<Consulta> consultas = filtrarConsultas(todasConsultas);

        tableModel.setColumnIdentifiers(new String[]{"Data", "Médico", "Paciente", "Status"});
        tableModel.setRowCount(0);

        for (Consulta consulta : consultas) {
            tableModel.addRow(new Object[]{
                consulta.getDataHorarioFormatado(),
                consulta.getMedico().getNome(),
                consulta.getPaciente().getNome(),
                consulta.getStatus().getDescricao()
            });
        }

        Map<String, Integer> contadores = new HashMap<>();
        for (Consulta consulta : consultas) {
            String medico = consulta.getMedico().getNome();
            contadores.put(medico, contadores.getOrDefault(medico, 0) + 1);
        }

        String resumo = String.format(
            "RELATÓRIO DE CONSULTAS POR MÉDICO\n\n" +
            "• Total de consultas: %d\n" +
            "• Número de médicos: %d\n" +
            "• Média por médico: %.1f consultas",
            consultas.size(), contadores.size(),
            consultas.size() > 0 ? (double) consultas.size() / contadores.size() : 0.0
        );

        txtResumo.setText(resumo);
    }

    /**
     * Gera o relatório de pacientes por especialidade.
     * Exibe a lista de consultas e um resumo estatístico por especialidade.
     * @throws Exception Se ocorrer um erro ao acessar o banco de dados.
     */
    private void gerarPacientesPorEspecialidade() throws Exception {
        List<Consulta> todasConsultas = consultaDAO.findAll();
        List<Consulta> consultas = filtrarConsultas(todasConsultas);

        tableModel.setColumnIdentifiers(new String[]{"Data", "Especialidade", "Paciente", "Médico"});
        tableModel.setRowCount(0);

        for (Consulta consulta : consultas) {
            tableModel.addRow(new Object[]{
                consulta.getDataHorarioFormatado(),
                consulta.getMedico().getEspecialidade(),
                consulta.getPaciente().getNome(),
                consulta.getMedico().getNome()
            });
        }

        Map<String, Set<String>> pacientesPorEsp = new HashMap<>();
        for (Consulta consulta : consultas) {
            String especialidade = consulta.getMedico().getEspecialidade();
            String paciente = consulta.getPaciente().getNome();
            pacientesPorEsp.computeIfAbsent(especialidade, k -> new HashSet<>()).add(paciente);
        }

        int totalPacientes = pacientesPorEsp.values().stream().mapToInt(Set::size).sum();

        String resumo = String.format(
            "PACIENTES POR ESPECIALIDADE\n\n" +
            "• Total de consultas: %d\n" +
            "• Pacientes únicos: %d\n" +
            "• Especialidades: %d",
            consultas.size(), totalPacientes, pacientesPorEsp.size()
        );

        txtResumo.setText(resumo);
    }

    /**
     * Gera o relatório de consultas canceladas.
     * Exibe a lista de consultas canceladas e a taxa de cancelamento.
     * @throws Exception Se ocorrer um erro ao acessar o banco de dados.
     */
    private void gerarConsultasCanceladas() throws Exception {
        List<Consulta> todasConsultas = consultaDAO.findAll();
        List<Consulta> consultas = filtrarConsultas(todasConsultas);

        List<Consulta> canceladas = new ArrayList<>();
        for (Consulta consulta : consultas) {
            if (consulta.getStatus() == Consulta.StatusConsulta.CANCELADA) {
                canceladas.add(consulta);
            }
        }

        tableModel.setColumnIdentifiers(new String[]{"Data", "Médico", "Paciente", "Observações"});
        tableModel.setRowCount(0);

        for (Consulta consulta : canceladas) {
            tableModel.addRow(new Object[]{
                consulta.getDataHorarioFormatado(),
                consulta.getMedico().getNome(),
                consulta.getPaciente().getNome(),
                consulta.getObservacoes()
            });
        }

        double taxaCancelamento = consultas.size() > 0 ?
            (canceladas.size() * 100.0 / consultas.size()) : 0.0;

        String resumo = String.format(
            "CONSULTAS CANCELADAS\n\n" +
            "• Total canceladas: %d\n" +
            "• Taxa de cancelamento: %.1f%%",
            canceladas.size(), taxaCancelamento
        );

        txtResumo.setText(resumo);
    }

    /**
     * Gera o histórico de consultas de um paciente específico.
     * Solicita o nome do paciente e exibe suas consultas e observações.
     * @throws Exception Se ocorrer um erro ao acessar o banco de dados.
     */
    private void gerarHistoricoPaciente() throws Exception {
        String nomePaciente = JOptionPane.showInputDialog(this, "Digite o nome do paciente:");
        if (nomePaciente == null || nomePaciente.trim().isEmpty()) {
            return;
        }

        List<Consulta> todasConsultas = consultaDAO.findAll();
        List<Consulta> consultas = filtrarConsultas(todasConsultas);

        List<Consulta> historico = new ArrayList<>();
        for (Consulta consulta : consultas) {
            if (consulta.getPaciente().getNome().toLowerCase().contains(nomePaciente.toLowerCase())) {
                historico.add(consulta);
            }
        }

        tableModel.setColumnIdentifiers(new String[]{"Data", "Médico", "Status", "Observações"});
        tableModel.setRowCount(0);

        for (Consulta consulta : historico) {
            tableModel.addRow(new Object[]{
                consulta.getDataHorarioFormatado(),
                consulta.getMedico().getNome(),
                consulta.getStatus().getDescricao(),
                consulta.getObservacoes()
            });
        }

        String resumo = String.format(
            "HISTÓRICO DO PACIENTE: %s\n\n" +
            "• Total de consultas: %d",
            nomePaciente.toUpperCase(), historico.size()
        );

        txtResumo.setText(resumo);
    }

    /**
     * Gera o relatório de distribuição de consultas por dia da semana.
     * Exibe as consultas e um resumo da distribuição.
     * @throws Exception Se ocorrer um erro ao acessar o banco de dados.
     */
    private void gerarDistribuicaoConsultas() throws Exception {
        List<Consulta> todasConsultas = consultaDAO.findAll();
        List<Consulta> consultas = filtrarConsultas(todasConsultas);

        tableModel.setColumnIdentifiers(new String[]{"Data", "Dia da Semana", "Médico", "Paciente"});
        tableModel.setRowCount(0);

        for (Consulta consulta : consultas) {
            String dia = consulta.getDataHorario().getDayOfWeek().toString();
            String diaPortugues = converterDiaSemana(dia);

            tableModel.addRow(new Object[]{
                consulta.getDataHorarioFormatado(),
                diaPortugues,
                consulta.getMedico().getNome(),
                consulta.getPaciente().getNome()
            });
        }

        // Resumo - distribuição por dia
        Map<String, Integer> distribuicao = new LinkedHashMap<>();
        distribuicao.put("Segunda", 0);
        distribuicao.put("Terça", 0);
        distribuicao.put("Quarta", 0);
        distribuicao.put("Quinta", 0);
        distribuicao.put("Sexta", 0);
        distribuicao.put("Sábado", 0);
        distribuicao.put("Domingo", 0);

        for (Consulta consulta : consultas) {
            String dia = consulta.getDataHorario().getDayOfWeek().toString();
            String diaPortugues = converterDiaSemana(dia);
            distribuicao.put(diaPortugues, distribuicao.get(diaPortugues) + 1);
        }

        StringBuilder resumoBuilder = new StringBuilder();
        resumoBuilder.append("DISTRIBUIÇÃO POR DIA DA SEMANA\n\n");
        resumoBuilder.append(String.format("• Total de consultas: %d\n", consultas.size()));
        resumoBuilder.append(String.format("• Média por dia: %.1f\n\n", consultas.size() / 7.0));
        resumoBuilder.append("Detalhamento por Dia:\n");
        for (Map.Entry<String, Integer> entry : distribuicao.entrySet()) {
            resumoBuilder.append(String.format("• %s: %d consultas\n", entry.getKey(), entry.getValue()));
        }

        txtResumo.setText(resumoBuilder.toString());
    }

    /**
     * Gera o relatório de pacientes sem consulta no último ano.
     * @throws Exception Se ocorrer um erro ao acessar o banco de dados.
     */
    private void gerarPacientesSemConsulta() throws Exception {
        List<Paciente> todosPacientes = pacienteDAO.findAll();
        List<Consulta> todasConsultas = consultaDAO.findAll();
        List<Consulta> consultas = filtrarConsultas(todasConsultas);

        Set<String> pacientesComConsulta = new HashSet<>();
        for (Consulta consulta : consultas) {
            // Verifica se a consulta ocorreu no último ano
            if (consulta.getDataHorario().isAfter(LocalDateTime.now().minusYears(1))) {
                pacientesComConsulta.add(consulta.getPaciente().getCpf());
            }
        }

        List<Paciente> pacientesSemConsulta = new ArrayList<>();
        for (Paciente paciente : todosPacientes) {
            if (!pacientesComConsulta.contains(paciente.getCpf())) {
                pacientesSemConsulta.add(paciente);
            }
        }

        tableModel.setColumnIdentifiers(new String[]{"Nome", "CPF", "Idade", "Telefone"});
        tableModel.setRowCount(0);

        for (Paciente paciente : pacientesSemConsulta) {
            tableModel.addRow(new Object[]{
                paciente.getNome(),
                paciente.getCpf(),
                paciente.getIdade() + " anos",
                paciente.getTelefone()
            });
        }

        String resumo = String.format(
            "PACIENTES SEM CONSULTA NO ÚLTIMO ANO\n\n" +
            "• Total sem consulta (último ano): %d\n" +
            "• Total de pacientes cadastrados: %d\n" +
            "• Percentual inativo (último ano): %.1f%%",
            pacientesSemConsulta.size(), todosPacientes.size(),
            todosPacientes.size() > 0 ? (pacientesSemConsulta.size() * 100.0 / todosPacientes.size()) : 0.0
        );

        txtResumo.setText(resumo);
    }

    /**
     * Converte o nome do dia da semana de inglês para português.
     * @param diaIngles O nome do dia da semana em inglês (ex: MONDAY).
     * @return O nome do dia da semana em português (ex: Segunda).
     */
    private String converterDiaSemana(String diaIngles) {
        switch (diaIngles) {
            case "MONDAY": return "Segunda";
            case "TUESDAY": return "Terça";
            case "WEDNESDAY": return "Quarta";
            case "THURSDAY": return "Quinta";
            case "FRIDAY": return "Sexta";
            case "SATURDAY": return "Sábado";
            case "SUNDAY": return "Domingo";
            default: return "Desconhecido";
        }
    }

    // ========== MÉTODOS DE NAVEGAÇÃO ==========

    /**
     * Traz para a frente a instância existente da TelaPrincipal,
     * ou cria uma nova se nenhuma for encontrada.
     * Garante que o usuário retorne ao menu principal sem abrir múltiplas janelas.
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
            // Se por alguma razão a TelaPrincipal não estiver aberta, cria uma nova
            new TelaPrincipal().setVisible(true);
        }
    }

    /**
     * Abre a TelaMedicos para gerenciar os dados dos médicos.
     * Cria uma nova instância e a torna visível.
     */
    private void abrirTelaMedicos() {
        TelaMedicos tela = new TelaMedicos();
        tela.setVisible(true);
        tela.toFront();
    }

    /**
     * Abre a TelaPacientes para gerenciar os dados dos pacientes.
     * Cria uma nova instância e a torna visível.
     */
    private void abrirTelaPacientes() {
        TelaPacientes tela = new TelaPacientes();
        tela.setVisible(true);
        tela.toFront();
    }

    /**
     * Abre a TelaAgendamento para agendar novas consultas.
     * Cria uma nova instância e a torna visível.
     */
    private void abrirTelaAgendamento() {
        TelaAgendamento tela = new TelaAgendamento();
        tela.setVisible(true);
        tela.toFront();
    }

    /**
     * Abre a TelaAgendaCalendario para visualizar a agenda de consultas em formato de calendário.
     * Cria uma nova instância e a torna visível.
     */
    private void abrirTelaAgendaCalendario() {
        TelaAgendaCalendario tela = new TelaAgendaCalendario();
        tela.setVisible(true);
        tela.toFront();
    }
}