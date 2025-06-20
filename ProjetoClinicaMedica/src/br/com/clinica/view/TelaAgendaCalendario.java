package br.com.clinica.view;

import br.com.clinica.dao.ConsultaDAO;
import br.com.clinica.dao.MedicoDAO;
import br.com.clinica.dao.PacienteDAO;
import br.com.clinica.model.Consulta;
import br.com.clinica.model.Medico;
import br.com.clinica.model.Paciente;
import br.com.clinica.service.ConsultaService;
import br.com.clinica.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Exibe a agenda de consultas em um formato de calendário visual semanal.
 * Permite a navegação entre semanas e meses, a filtragem por médico ou paciente
 * e a alteração rápida do status das consultas com cores indicativas.
 * 
 * FUNCIONALIDADES PRINCIPAIS:
 * - Mini-calendário centralizado no painel lateral esquerdo
 * - Menu de contexto completo no clique direito (consultas e células vazias)
 * - Cores automáticas por status: Azul=Agendada, Verde=Realizada, Vermelho=Cancelada, Cinza=Não Compareceu
 * - Clique duplo para abrir detalhes da consulta
 * - Tooltips informativos em todas as células
 * 
 * NOTA SOBRE EXPRESSÕES LAMBDA:
 * Esta classe utiliza extensivamente Expressões Lambda (sintaxe 'e -> ação'),
 * uma funcionalidade do Java 8+ que permite escrever código mais conciso.
 * Lambdas substituem classes anônimas para interfaces funcionais (como ActionListener),
 * tornando o código mais legível e reduzindo significativamente a verbosidade.
 * 
 * Interage com as classes: ConsultaService, MedicoDAO, PacienteDAO, UITheme.
 */
public class TelaAgendaCalendario extends JFrame implements UITheme {
    
    private static final long serialVersionUID = 1L;
    
    private JTable tabelaAgenda; 
    private DefaultTableModel modeloTabela;
    private JLabel lblMesAno;
    private JLabel lblDataAtual; // Nova variável para o display de data elegante
    private JPanel diasPanel;
    private JSpinner spinnerData; 
    private JComboBox<String> cbTipoFiltro;
    private JComboBox<Object> cbFiltroItem;
    private JLabel lblSemanaAtual;
    
    private ConsultaDAO consultaDAO;
    private MedicoDAO medicoDAO; 
    private PacienteDAO pacienteDAO; 
    private ConsultaService consultaService;
    
    private Calendar calendarioAtual;
    private Map<String, Consulta> mapaConsultas;

    /**
     * Construtor da tela. Inicializa as dependências e a interface.
     */
    public TelaAgendaCalendario() {
        this.consultaDAO = new ConsultaDAO();
        this.medicoDAO = new MedicoDAO();
        this.pacienteDAO = new PacienteDAO();
        this.consultaService = new ConsultaService();
        this.calendarioAtual = Calendar.getInstance();
        this.mapaConsultas = new HashMap<>();

        initialize(); 
        atualizarVisualizacao();
    }
    
    /**
     * Ponto de entrada para a construção da interface gráfica da tela.
     * Mantém a estrutura original mas reorganiza o painel lateral esquerdo
     * para melhor distribuição visual dos componentes.
     */
    private void initialize() {
        configurarJanela();
        
        // Painel superior combinado (header + filtros)
        JPanel painelSuperior = new JPanel(new BorderLayout());
        painelSuperior.add(criarHeader(), BorderLayout.NORTH);
        painelSuperior.add(criarBarraFiltros(), BorderLayout.SOUTH);
        
        // Mantém a estrutura original com painel lateral esquerdo reorganizado
        JPanel lateralPanel = criarPainelLateralReorganizado();
        JPanel centralPanel = criarPainelCentral();

        getContentPane().add(painelSuperior, BorderLayout.NORTH);
        getContentPane().add(lateralPanel, BorderLayout.WEST);
        getContentPane().add(centralPanel, BorderLayout.CENTER);

        criarTabelaAgendamento(); 
        configurarInteracoesTabela(); 
    }

    /**
     * Configura as propriedades principais da janela (JFrame).
     */
    private void configurarJanela() {
        setTitle("Agenda Visual - Calendário Semanal");
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
        headerPanel.setPreferredSize(new Dimension(0, 70));
        headerPanel.setBorder(new EmptyBorder(12, 20, 12, 20));
        
        JLabel lblTitulo = new JLabel("AGENDA VISUAL DA CLÍNICA");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(CLEAN_WHITE);
        
        JPanel botoesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botoesPanel.setOpaque(false);
        
        /**
         * Exemplos de uso de Expressões Lambda como ActionListeners nos botões do header.
         * Cada 'e -> new TelaNome().setVisible(true)' cria e exibe uma nova tela quando
         * o botão é clicado. As lambdas tornam estas ações muito mais concisas
         * comparadas à sintaxe tradicional de ActionListener com classes anônimas.
         */
        botoesPanel.add(criarBotaoAcao("Novo Agendamento", SUCCESS_GREEN, e -> new TelaNovoAgendamento().setVisible(true)));
        botoesPanel.add(criarBotaoAcao("Gerenciar Consultas", MEDICAL_GREEN, e -> new TelaGerenciarConsultas().setVisible(true)));
        botoesPanel.add(criarBotaoAcao("Fechar", ACCENT_RED, e -> dispose()));
        
        headerPanel.add(lblTitulo, BorderLayout.WEST);
        headerPanel.add(botoesPanel, BorderLayout.EAST);
        return headerPanel;
    }
    
    /**
     * Cria o painel lateral esquerdo com layout reorganizado para melhor distribuição visual.
     * O mini calendário agora fica centralizado verticalmente no painel, proporcionando
     * uma interface mais equilibrada. Utiliza BoxLayout para controle preciso do posicionamento.
     * @return O painel lateral configurado com componentes centralizados.
     */
    private JPanel criarPainelLateralReorganizado() {
        JPanel lateralPanel = new JPanel(new BorderLayout(0, 0));
        lateralPanel.setBackground(CLEAN_WHITE);
        lateralPanel.setPreferredSize(new Dimension(280, 0));
        lateralPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(200, 200, 200)),
            new EmptyBorder(20, 15, 20, 15)
        ));

        // Container principal que permite centralização vertical dos componentes
        JPanel containerPrincipal = new JPanel();
        containerPrincipal.setLayout(new BoxLayout(containerPrincipal, BoxLayout.Y_AXIS));
        containerPrincipal.setOpaque(false);
        
        // Seletor de data no topo
        JPanel seletorPanel = criarSeletorDeData();
        seletorPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        containerPrincipal.add(seletorPanel);
        
        // Espaço flexível que empurra o calendário para o centro
        containerPrincipal.add(Box.createVerticalGlue());
        
        // Mini calendário centralizado verticalmente
        JPanel miniCalendario = criarMiniCalendario();
        miniCalendario.setAlignmentX(Component.CENTER_ALIGNMENT);
        containerPrincipal.add(miniCalendario);
        
        // Outro espaço flexível que mantém o calendário centralizado
        containerPrincipal.add(Box.createVerticalGlue());
        
        lateralPanel.add(containerPrincipal, BorderLayout.CENTER);
        return lateralPanel;
    }
    
    /**
     * Cria uma barra horizontal de filtros na parte superior da tela.
     * @return O painel da barra de filtros.
     */
    private JPanel criarBarraFiltros() {
        JPanel barraPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        barraPanel.setBackground(new Color(245, 248, 250));
        barraPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)),
            new EmptyBorder(5, 15, 5, 15)
        ));
        
        // Label de título para os filtros
        JLabel lblFiltros = new JLabel("Filtros:");
        lblFiltros.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblFiltros.setForeground(PRIMARY_BLUE);
        barraPanel.add(lblFiltros);
        
        // ComboBox de tipo de filtro
        JLabel lblTipo = new JLabel("Tipo:");
        lblTipo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        barraPanel.add(lblTipo);
        
        cbTipoFiltro = new JComboBox<>(new String[]{"Todos", "Por Médico", "Por Paciente"});
        cbTipoFiltro.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbTipoFiltro.setPreferredSize(new Dimension(120, 25));
        /**
         * Expressões Lambda para os ActionListeners dos ComboBoxes.
         * Estas lambdas são executadas quando o usuário seleciona um item diferente.
         * 'e -> carregarItensFiltro()' substitui a necessidade de criar uma classe
         * ActionListener completa, tornando o código mais limpo e legível.
         */
        cbTipoFiltro.addActionListener(e -> carregarItensFiltro());
        barraPanel.add(cbTipoFiltro);
        
        // Separador visual
        JLabel separador1 = new JLabel("|");
        separador1.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        separador1.setForeground(new Color(180, 180, 180));
        barraPanel.add(separador1);
        
        // ComboBox de item específico
        JLabel lblItem = new JLabel("Seleção:");
        lblItem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        barraPanel.add(lblItem);
        
        cbFiltroItem = new JComboBox<>();
        cbFiltroItem.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbFiltroItem.setPreferredSize(new Dimension(200, 25));
        cbFiltroItem.addActionListener(e -> aplicarFiltro());
        barraPanel.add(cbFiltroItem);
        
        // Separador visual
        JLabel separador2 = new JLabel("|");
        separador2.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        separador2.setForeground(new Color(180, 180, 180));
        barraPanel.add(separador2);
        
        // Botão para limpar filtros
        JButton btnLimparFiltros = new JButton("Limpar");
        btnLimparFiltros.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLimparFiltros.setForeground(ACCENT_RED);
        btnLimparFiltros.setBackground(CLEAN_WHITE);
        btnLimparFiltros.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_RED, 1),
            BorderFactory.createEmptyBorder(3, 10, 3, 10)
        ));
        btnLimparFiltros.setFocusable(false);
        btnLimparFiltros.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLimparFiltros.addActionListener(e -> limparFiltros());
        barraPanel.add(btnLimparFiltros);
        
        return barraPanel;
    }
    
    /**
     * Limpa todos os filtros aplicados e volta à visualização completa.
     */
    private void limparFiltros() {
        cbTipoFiltro.setSelectedIndex(0); // "Todos"
        cbFiltroItem.removeAllItems();
        cbFiltroItem.addItem("Selecione...");
        aplicarFiltro();
    }
    
    /**
     * Cria um painel de navegação de data mais elegante e funcional.
     * Substitui o JSpinner genérico por botões de navegação intuitivos
     * e display de data formatado de forma profissional.
     * @return O painel de navegação de data estilizado.
     */
    private JPanel criarSeletorDeData() {
        JPanel seletorDataPanel = new JPanel();
        seletorDataPanel.setLayout(new BoxLayout(seletorDataPanel, BoxLayout.Y_AXIS));
        seletorDataPanel.setOpaque(false);
        seletorDataPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_BLUE, 1),
                "Navegação Rápida",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 12),
                PRIMARY_BLUE
            ),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        // Painel com display da data atual de forma elegante
        JPanel displayDataPanel = criarDisplayDataAtual();
        displayDataPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        seletorDataPanel.add(displayDataPanel);
        
        seletorDataPanel.add(Box.createVerticalStrut(10));
        
        // Painel com botões de navegação entre semanas
        JPanel navegacaoSemanaPanel = criarNavegacaoSemana();
        navegacaoSemanaPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        seletorDataPanel.add(navegacaoSemanaPanel);
        
        seletorDataPanel.add(Box.createVerticalStrut(8));
        
        // Painel com botões de ação rápida
        JPanel acoesRapidasPanel = criarAcoesRapidas();
        acoesRapidasPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        seletorDataPanel.add(acoesRapidasPanel);
        
        return seletorDataPanel;
    }
    
    /**
     * Cria um display elegante para mostrar a data/semana atual selecionada.
     * Utiliza formatação profissional e cores que harmonizam com o sistema.
     * @return Painel com o display da data atual.
     */
    private JPanel criarDisplayDataAtual() {
        JPanel displayPanel = new JPanel(new BorderLayout());
        displayPanel.setOpaque(true);
        displayPanel.setBackground(new Color(248, 250, 252));
        displayPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 210, 220), 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        
        // Label principal com a data atual (agora como variável de instância)
        lblDataAtual = new JLabel();
        lblDataAtual.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblDataAtual.setForeground(DARK_TEXT);
        lblDataAtual.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Atualizar o texto inicial
        SimpleDateFormat formatoDisplay = new SimpleDateFormat("dd 'de' MMMM, yyyy", new Locale("pt", "BR"));
        lblDataAtual.setText(formatoDisplay.format(calendarioAtual.getTime()));
        
        // Manter JSpinner oculto para compatibilidade
        spinnerData = new JSpinner(new SpinnerDateModel());
        spinnerData.setValue(calendarioAtual.getTime());
        spinnerData.setVisible(false);
        
        displayPanel.add(lblDataAtual, BorderLayout.CENTER);
        
        return displayPanel;
    }
    
    /**
     * Cria botões de navegação entre semanas com design profissional.
     * Permite navegar rapidamente sem precisar usar o mini calendário.
     * @return Painel com botões de navegação semanal.
     */
    private JPanel criarNavegacaoSemana() {
        JPanel navegacaoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        navegacaoPanel.setOpaque(false);
        
        JButton btnSemanaAnterior = criarBotaoNavegacaoRapida("< Semana");
        /**
         * Expressão Lambda para navegação de semana anterior.
         * A sintaxe 'e -> { ... }' permite executar múltiplas operações quando o botão é clicado.
         * Neste caso, retrocedemos 7 dias no calendário e atualizamos toda a visualização.
         * Lambda é ideal aqui porque temos uma ação simples e direta para um ActionListener.
         */
        btnSemanaAnterior.addActionListener(e -> {
            calendarioAtual.add(Calendar.DAY_OF_YEAR, -7);
            atualizarVisualizacao();
        });
        
        JButton btnProximaSemana = criarBotaoNavegacaoRapida("Semana >");
        /**
         * Expressão Lambda similar para próxima semana.
         * Avança 7 dias e atualiza a visualização. O padrão Lambda torna
         * o código mais legível que a alternativa com ActionListener tradicional.
         */
        btnProximaSemana.addActionListener(e -> {
            calendarioAtual.add(Calendar.DAY_OF_YEAR, 7);
            atualizarVisualizacao();
        });
        
        navegacaoPanel.add(btnSemanaAnterior);
        navegacaoPanel.add(btnProximaSemana);
        
        return navegacaoPanel;
    }
    
    /**
     * Cria botões de ação rápida para navegação comum (Hoje, etc.).
     * Facilita o acesso às operações mais frequentes sem múltiplos cliques.
     * @return Painel com botões de ação rápida.
     */
    private JPanel criarAcoesRapidas() {
        JPanel acoesPanel = new JPanel();
        acoesPanel.setLayout(new BoxLayout(acoesPanel, BoxLayout.Y_AXIS));
        acoesPanel.setOpaque(false);
        
        JButton btnHoje = criarBotaoAcaoRapida("Hoje");
        btnHoje.setAlignmentX(Component.CENTER_ALIGNMENT);
        /**
         * Expressão Lambda para voltar à data atual.
         * Calendar.getInstance() cria um novo calendário com a data/hora atual do sistema.
         * Esta lambda substitui a necessidade de criar uma classe ActionListener completa
         * para uma operação simples e comum.
         */
        btnHoje.addActionListener(e -> {
            calendarioAtual = Calendar.getInstance();
            atualizarVisualizacao();
        });
        
        acoesPanel.add(btnHoje);
        
        return acoesPanel;
    }
    
    /**
     * Cria botões estilizados para navegação rápida entre semanas.
     * Mantém consistência visual com o resto da aplicação.
     * @param texto O texto a ser exibido no botão.
     * @return JButton estilizado para navegação.
     */
    private JButton criarBotaoNavegacaoRapida(String texto) {
        JButton botao = new JButton(texto);
        botao.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        botao.setForeground(PRIMARY_BLUE);
        botao.setBackground(CLEAN_WHITE);
        botao.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_BLUE, 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        botao.setFocusable(false);
        botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
        botao.setPreferredSize(new Dimension(80, 28));
        
        /**
         * MouseAdapter para efeito hover nos botões de navegação rápida.
         * Como precisamos implementar múltiplos métodos do MouseListener (mouseEntered e mouseExited),
         * não podemos usar lambda aqui. MouseAdapter nos permite sobrescrever apenas os métodos
         * que realmente precisamos, ao invés de implementar todos os métodos da interface MouseListener.
         */
        botao.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                botao.setBackground(PRIMARY_BLUE);
                botao.setForeground(CLEAN_WHITE);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                botao.setBackground(CLEAN_WHITE);
                botao.setForeground(PRIMARY_BLUE);
            }
        });
        
        return botao;
    }
    
    /**
     * Cria botões de ação rápida com estilo diferenciado.
     * Usado para ações importantes como "Hoje".
     * @param texto O texto do botão.
     * @return JButton estilizado para ações rápidas.
     */
    private JButton criarBotaoAcaoRapida(String texto) {
        JButton botao = new JButton(texto);
        botao.setFont(new Font("Segoe UI", Font.BOLD, 12));
        botao.setForeground(CLEAN_WHITE);
        botao.setBackground(SUCCESS_GREEN);
        botao.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        botao.setFocusable(false);
        botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
        botao.setPreferredSize(new Dimension(100, 32));
        botao.setMaximumSize(new Dimension(120, 32));
        
        /**
         * MouseAdapter para efeito hover no botão de ação.
         * Similar aos outros botões, mas com cores diferentes para destacar
         * que este é um botão de ação principal.
         */
        botao.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                botao.setBackground(SUCCESS_GREEN.brighter());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                botao.setBackground(SUCCESS_GREEN);
            }
        });
        
        return botao;
    }

    /**
     * Cria o componente visual do mini-calendário com layout otimizado e centralizado.
     * Agora utiliza um grid 7x7 fixo para maior consistência visual e estabilidade
     * do layout independente do mês visualizado. O calendário mantém sempre o mesmo
     * tamanho, evitando mudanças bruscas na interface.
     * @return O painel do mini-calendário configurado.
     */
    private JPanel criarMiniCalendario() {
        JPanel miniCalendarioPanel = new JPanel(new BorderLayout(0, 10));
        miniCalendarioPanel.setOpaque(false);
        miniCalendarioPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_BLUE, 2),
                "Navegacao Mensal",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 14),
                PRIMARY_BLUE
            ),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        JPanel cabecalhoMini = new JPanel(new BorderLayout(5, 0));
        cabecalhoMini.setOpaque(false);
        
        JButton btnMesAnterior = criarBotaoNavegacao("<");
        /**
         * Expressão Lambda para navegação do mês anterior.
         * A sintaxe 'e -> navegarMes(-1)' substitui a criação de uma classe ActionListener completa.
         * 
         * Equivale ao código tradicional:
         * btnMesAnterior.addActionListener(new ActionListener() {
         *     public void actionPerformed(ActionEvent e) {
         *         navegarMes(-1);
         *     }
         * });
         * 
         * A lambda captura o parâmetro 'e' (ActionEvent) e executa 'navegarMes(-1)'.
         * Como não utilizamos o parâmetro 'e', poderíamos usar '() -> navegarMes(-1)',
         * mas mantemos 'e ->' por consistência com outras implementações.
         */
        btnMesAnterior.addActionListener(e -> navegarMes(-1));
        
        JButton btnProximoMes = criarBotaoNavegacao(">");
        /**
         * Expressão Lambda idêntica para navegação do próximo mês.
         * Demonstra como lambdas mantêm o código conciso quando a ação é simples.
         * O valor positivo (1) indica avanço de um mês, enquanto o anterior usa (-1).
         */
        btnProximoMes.addActionListener(e -> navegarMes(1));
        
        lblMesAno = new JLabel("", SwingConstants.CENTER);
        lblMesAno.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblMesAno.setForeground(DARK_TEXT);
        
        cabecalhoMini.add(btnMesAnterior, BorderLayout.WEST);
        cabecalhoMini.add(lblMesAno, BorderLayout.CENTER);
        cabecalhoMini.add(btnProximoMes, BorderLayout.EAST);
        
        // Grid FIXO 7x7 para garantir layout consistente entre diferentes meses
        // GridLayout(7, 7, 2, 2) cria 7 linhas e 7 colunas com espaçamento de 2px
        diasPanel = new JPanel(new GridLayout(7, 7, 2, 2));
        diasPanel.setBackground(CLEAN_WHITE);
        diasPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        
        // Tamanho fixo que previne redimensionamento indesejado
        Dimension tamanhoCalendario = new Dimension(220, 180);
        diasPanel.setPreferredSize(tamanhoCalendario);
        diasPanel.setMinimumSize(tamanhoCalendario);
        diasPanel.setMaximumSize(tamanhoCalendario);
        
        miniCalendarioPanel.add(cabecalhoMini, BorderLayout.NORTH);
        miniCalendarioPanel.add(diasPanel, BorderLayout.CENTER);
        return miniCalendarioPanel;
    }
    
    /**
     * Cria botões de navegação estilizados para o cabeçalho do mini-calendário.
     * Os botões usam símbolos simples ("<" e ">") e possuem efeito hover
     * implementado através de MouseListener anônimo.
     * @param texto O texto do botão ("<" ou ">").
     * @return Um JButton estilizado para navegação entre meses.
     */
    private JButton criarBotaoNavegacao(String texto) {
        JButton botao = new JButton(texto);
        botao.setFont(new Font("Segoe UI", Font.BOLD, 14));
        botao.setForeground(PRIMARY_BLUE);
        botao.setBackground(CLEAN_WHITE);
        botao.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_BLUE, 2),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        botao.setFocusable(false);
        botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
        botao.setPreferredSize(new Dimension(40, 32));
        
        /**
         * MouseAdapter com métodos sobrescritos para criar efeito hover.
         * Esta é uma classe anônima que implementa apenas os métodos necessários
         * do MouseListener. Aqui não usamos lambda porque MouseListener possui
         * múltiplos métodos (mouseEntered, mouseExited, etc.).
         * 
         * Lambdas só funcionam com interfaces funcionais (que têm apenas um método).
         * Como MouseListener tem vários métodos, usamos MouseAdapter que nos permite
         * sobrescrever apenas os métodos que precisamos.
         */
        botao.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                botao.setBackground(PRIMARY_BLUE);
                botao.setForeground(CLEAN_WHITE);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                botao.setBackground(CLEAN_WHITE);
                botao.setForeground(PRIMARY_BLUE);
            }
        });
        
        return botao;
    }

    /**
     * Cria o painel central, que contém a grade principal da agenda.
     * @return O painel central.
     */
    private JPanel criarPainelCentral() {
        JPanel centralPanel = new JPanel(new BorderLayout(0, 10));
        centralPanel.setBackground(CLEAN_WHITE);
        centralPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JPanel headerCentral = new JPanel(new BorderLayout());
        headerCentral.setOpaque(false);
        
        lblSemanaAtual = new JLabel("Agenda da Semana");
        lblSemanaAtual.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblSemanaAtual.setForeground(DARK_TEXT);
        
        headerCentral.add(lblSemanaAtual, BorderLayout.WEST);
        headerCentral.add(criarLegendaCores(), BorderLayout.EAST);
        
        centralPanel.add(headerCentral, BorderLayout.NORTH);
        
        tabelaAgenda = new JTable();
        JScrollPane scrollPaneTabela = new JScrollPane(tabelaAgenda); 
        scrollPaneTabela.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        centralPanel.add(scrollPaneTabela, BorderLayout.CENTER);
        
        return centralPanel;
    }
    
    /**
     * Cria a legenda de cores que indica o status de cada consulta.
     * @return O painel da legenda.
     */
    private JPanel criarLegendaCores() {
        JPanel legendaPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        legendaPanel.setOpaque(false);
        legendaPanel.add(criarIndicadorLegenda("Agendada", COR_AGENDADA));
        legendaPanel.add(criarIndicadorLegenda("Realizada", COR_REALIZADA));
        legendaPanel.add(criarIndicadorLegenda("Cancelada", COR_CANCELADA));
        legendaPanel.add(criarIndicadorLegenda("Não Compareceu", COR_NAO_COMPARECEU));
        return legendaPanel;
    }
    
    /**
     * Cria e configura a estrutura da tabela principal da agenda com cores funcionais.
     */
    private void criarTabelaAgendamento() {
        String[] colunas = new String[8];
        colunas[0] = "Horário";
        
        modeloTabela = new DefaultTableModel(null, colunas) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        tabelaAgenda.setModel(modeloTabela); 
        tabelaAgenda.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tabelaAgenda.setRowHeight(32);
        tabelaAgenda.setGridColor(new Color(220, 220, 220));
        tabelaAgenda.setShowGrid(true);
        
        // Configurar cabeçalho
        tabelaAgenda.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabelaAgenda.getTableHeader().setBackground(LIGHT_GRAY);
        tabelaAgenda.getTableHeader().setForeground(DARK_TEXT);
        
        // Configurar largura da coluna de horário
        tabelaAgenda.getColumnModel().getColumn(0).setPreferredWidth(70);
        tabelaAgenda.getColumnModel().getColumn(0).setMinWidth(70);
        tabelaAgenda.getColumnModel().getColumn(0).setMaxWidth(70);
        
        // Aplicar renderizadores com cores corrigidas
        aplicarRenderizadoresComCores();
    }
    
    /**
     * Aplica os renderizadores customizados com cores funcionais para cada coluna.
     */
    private void aplicarRenderizadoresComCores() {
        // Renderizador para coluna de horário
        tabelaAgenda.getColumnModel().getColumn(0).setCellRenderer(new HorarioRenderer());
        
        // Renderizador para colunas de agendamento (com cores)
        AgendamentoRenderer agendamentoRenderer = new AgendamentoRenderer();
        for (int i = 1; i < tabelaAgenda.getColumnCount(); i++) {
            tabelaAgenda.getColumnModel().getColumn(i).setCellRenderer(agendamentoRenderer);
        }
    }
    
    /**
     * Configura as interações da tabela incluindo clique duplo e menu de contexto.
     * CORRIGIDO: Menu de contexto (clique direito) funcionando adequadamente.
     */
    private void configurarInteracoesTabela() { 
        tabelaAgenda.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int linha = tabelaAgenda.rowAtPoint(e.getPoint());
                int coluna = tabelaAgenda.columnAtPoint(e.getPoint());
                
                if (linha < 0 || coluna < 1) return;
                
                String chave = linha + "," + coluna;
                Consulta consulta = mapaConsultas.get(chave);
                
                if (consulta != null) {
                    // Menu de contexto (clique direito)
                    if (SwingUtilities.isRightMouseButton(e)) {
                        mostrarMenuDeAcoesCompleto(e, consulta);
                    } 
                    // Clique duplo (esquerdo)
                    else if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                        new TelaGerenciarConsultas(consulta.getId()).setVisible(true);
                    }
                } else {
                    // Clique direito em célula vazia - menu para novo agendamento
                    if (SwingUtilities.isRightMouseButton(e)) {
                        mostrarMenuCelulaVazia(e, linha, coluna);
                    }
                }
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                // Garantir que o menu apareça no mousePressed também (compatibilidade)
                if (e.isPopupTrigger()) {
                    mouseClicked(e);
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                // Garantir que o menu apareça no mouseReleased também (compatibilidade)
                if (e.isPopupTrigger()) {
                    mouseClicked(e);
                }
            }
        });
    }

    /**
     * Exibe um menu de contexto completo com várias opções para consultas existentes.
     * @param e O evento de mouse.
     * @param consulta A consulta selecionada.
     */
    private void mostrarMenuDeAcoesCompleto(MouseEvent e, Consulta consulta) {
        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createLineBorder(PRIMARY_BLUE, 1));
        
        // Cabeçalho do menu com informações da consulta
        JMenuItem cabecalho = new JMenuItem(String.format("Consulta: %s", consulta.getPaciente().getNome()));
        cabecalho.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cabecalho.setForeground(PRIMARY_BLUE);
        cabecalho.setEnabled(false);
        popup.add(cabecalho);
        
        JMenuItem infoMedico = new JMenuItem(String.format("Médico: %s", consulta.getMedico().getNome()));
        infoMedico.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        infoMedico.setEnabled(false);
        popup.add(infoMedico);
        
        JMenuItem infoStatus = new JMenuItem(String.format("Status: %s", consulta.getStatus().getDescricao()));
        infoStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        infoStatus.setEnabled(false);
        popup.add(infoStatus);
        
        popup.addSeparator();
        
        // Opções de ação baseadas no status
        if (consulta.getStatus() == Consulta.StatusConsulta.AGENDADA) {
            JMenuItem itemRealizada = new JMenuItem("Marcar como Realizada");
            itemRealizada.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            /**
             * Expressão Lambda para ActionListener de MenuItem.
             * A sintaxe 'ev -> alterarStatusConsulta(consulta, StatusAcao.REALIZADA)'
             * cria um listener que executa a ação quando o item do menu é clicado.
             * Note que usamos 'ev' ao invés de 'e' para evitar conflito com o parâmetro
             * do método externo (MouseEvent e).
             */
            itemRealizada.addActionListener(ev -> alterarStatusConsulta(consulta, StatusAcao.REALIZADA));
            popup.add(itemRealizada);
            
            JMenuItem itemCancelada = new JMenuItem("Cancelar Consulta");
            itemCancelada.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            /**
             * Outra Expressão Lambda similar para cancelamento de consulta.
             * Demonstra como lambdas mantêm referências ao escopo externo ('consulta').
             */
            itemCancelada.addActionListener(ev -> alterarStatusConsulta(consulta, StatusAcao.CANCELADA));
            popup.add(itemCancelada);
            
            popup.addSeparator();
        }
        
        // Opções sempre disponíveis
        JMenuItem itemDetalhes = new JMenuItem("Ver Detalhes Completos");
        itemDetalhes.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        itemDetalhes.addActionListener(ev -> new TelaGerenciarConsultas(consulta.getId()).setVisible(true));
        popup.add(itemDetalhes);
        
        JMenuItem itemPaciente = new JMenuItem("Editar Paciente");
        itemPaciente.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        itemPaciente.addActionListener(ev -> new TelaPacientes().setVisible(true));
        popup.add(itemPaciente);
        
        // Exibir menu
        popup.show(tabelaAgenda, e.getX(), e.getY());
    }
    
    /**
     * Exibe menu para células vazias permitindo novo agendamento.
     * @param e O evento de mouse.
     * @param linha A linha clicada.
     * @param coluna A coluna clicada.
     */
    private void mostrarMenuCelulaVazia(MouseEvent e, int linha, int coluna) {
        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createLineBorder(SUCCESS_GREEN, 1));
        
        // Calcular horário da célula
        int hora = 8 + (linha / 2);
        int minuto = (linha % 2) * 30;
        String horarioStr = String.format("%02d:%02d", hora, minuto);
        
        JMenuItem cabecalho = new JMenuItem(String.format("Horário: %s", horarioStr));
        cabecalho.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cabecalho.setForeground(SUCCESS_GREEN);
        cabecalho.setEnabled(false);
        popup.add(cabecalho);
        
        popup.addSeparator();
        
        JMenuItem itemNovoAgendamento = new JMenuItem("Novo Agendamento");
        itemNovoAgendamento.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        itemNovoAgendamento.addActionListener(ev -> new TelaNovoAgendamento().setVisible(true));
        popup.add(itemNovoAgendamento);
        
        // Exibir menu
        popup.show(tabelaAgenda, e.getX(), e.getY());
    }
    
    /**
     * Altera o status de uma consulta utilizando o ConsultaService.
     * @param consulta A consulta a ser alterada.
     * @param acao O novo status desejado.
     */
    private void alterarStatusConsulta(Consulta consulta, StatusAcao acao) {
        try {
            switch (acao) {
                case REALIZADA:
                    String obs = JOptionPane.showInputDialog(this, "Observações da consulta:", "Consulta Realizada", JOptionPane.PLAIN_MESSAGE);
                    if (obs != null) {
                        consultaService.marcarComoRealizada(consulta, obs);
                        JOptionPane.showMessageDialog(this, "Status alterado para: Realizada", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    }
                    break;
                case CANCELADA:
                    int confirm = JOptionPane.showConfirmDialog(this, "Deseja realmente cancelar esta consulta?", "Confirmar", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        consultaService.cancelarConsulta(consulta);
                        JOptionPane.showMessageDialog(this, "Consulta cancelada com sucesso.", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    }
                    break;
            }
            atualizarVisualizacao();
        } catch (IllegalStateException | SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao alterar status: " + ex.getMessage(), "Erro de Operação", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Atualiza toda a visualização da agenda incluindo o display de data elegante.
     */
    private void atualizarVisualizacao() {
        // Atualizar o JSpinner oculto para manter compatibilidade
        spinnerData.setValue(calendarioAtual.getTime());
        
        // Atualizar o display de data elegante usando a variável de instância
        SimpleDateFormat formatoDisplay = new SimpleDateFormat("dd 'de' MMMM, yyyy", new Locale("pt", "BR"));
        lblDataAtual.setText(formatoDisplay.format(calendarioAtual.getTime()));
        
        atualizarMiniCalendario();
        atualizarCabecalhosTabela();
        aplicarFiltro();
    }
    
    /**
     * Atualiza o mini-calendário mantendo um layout FIXO que não muda entre meses.
     * Utiliza sempre exatamente 7x7 grid (49 células) para consistência visual.
     * A primeira linha contém os cabeçalhos dos dias da semana, e as 6 linhas
     * seguintes contêm os dias do mês atual e espaços vazios conforme necessário.
     */
    private void atualizarMiniCalendario() {
        SimpleDateFormat formatoMesAno = new SimpleDateFormat("MMMM, yyyy", new Locale("pt", "BR"));
        lblMesAno.setText(formatoMesAno.format(calendarioAtual.getTime()));
        
        diasPanel.removeAll();
        
        // PRIMEIRA LINHA: Cabeçalhos dos dias da semana (sempre 7 células)
        String[] diasSemana = {"D", "S", "T", "Q", "Q", "S", "S"};
        for (String dia : diasSemana) {
            JLabel lblDia = new JLabel(dia, SwingConstants.CENTER);
            lblDia.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblDia.setForeground(PRIMARY_BLUE);
            lblDia.setPreferredSize(new Dimension(30, 25));
            lblDia.setOpaque(true);
            lblDia.setBackground(LIGHT_GRAY);
            diasPanel.add(lblDia);
        }
        
        Calendar temp = (Calendar) calendarioAtual.clone();
        temp.set(Calendar.DAY_OF_MONTH, 1);
        int primeiroDiaDaSemana = temp.get(Calendar.DAY_OF_WEEK);
        int diasNoMes = temp.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        // Array para armazenar todos os 42 botões/labels (6 linhas x 7 colunas após cabeçalho)
        JComponent[] celulas = new JComponent[42];
        int indiceCelula = 0;
        
        // Espaços vazios antes do primeiro dia do mês
        for (int i = 1; i < primeiroDiaDaSemana; i++) {
            celulas[indiceCelula] = criarCelulaVazia();
            indiceCelula++;
        }
        
        // Botões dos dias do mês atual
        for (int dia = 1; dia <= diasNoMes; dia++) {
            celulas[indiceCelula] = criarBotaoQuadradoDia(dia);
            indiceCelula++;
        }
        
        // Completar as células restantes com espaços vazios
        while (indiceCelula < 42) {
            celulas[indiceCelula] = criarCelulaVazia();
            indiceCelula++;
        }
        
        // Adicionar todas as 42 células ao painel
        for (JComponent celula : celulas) {
            diasPanel.add(celula);
        }
        
        diasPanel.revalidate();
        diasPanel.repaint();
    }

    /**
     * Cria uma célula vazia para manter o layout do calendário consistente.
     * Essencial para que o grid 7x7 sempre tenha o mesmo número de elementos,
     * independente de quantos dias tem o mês ou em que dia da semana começa.
     * @return JLabel vazio com tamanho fixo.
     */
    private JLabel criarCelulaVazia() {
        JLabel espacoVazio = new JLabel("");
        espacoVazio.setPreferredSize(new Dimension(30, 30));
        espacoVazio.setMinimumSize(new Dimension(30, 30));
        espacoVazio.setMaximumSize(new Dimension(30, 30));
        espacoVazio.setOpaque(true);
        espacoVazio.setBackground(CLEAN_WHITE);
        return espacoVazio;
    }

    /**
     * Cria um botão quadrado para um dia específico no mini-calendário.
     * Cada botão possui tamanho fixo, efeito hover e ação de clique que
     * navega para o dia selecionado.
     * @param dia O número do dia a ser exibido no botão.
     * @return Um JButton quadrado estilizado.
     */
    private JButton criarBotaoQuadradoDia(int dia) {
        JButton btnDia = new JButton(String.valueOf(dia));
        btnDia.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnDia.setMargin(new Insets(0, 0, 0, 0));
        btnDia.setFocusable(false);
        btnDia.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));
        btnDia.setBackground(CLEAN_WHITE);
        btnDia.setForeground(DARK_TEXT);
        
        // Tamanho FIXO para manter consistência visual
        Dimension tamanhoQuadrado = new Dimension(30, 30);
        btnDia.setPreferredSize(tamanhoQuadrado);
        btnDia.setMinimumSize(tamanhoQuadrado);
        btnDia.setMaximumSize(tamanhoQuadrado);

        // Destacar o dia atual com cor diferente
        if (dia == calendarioAtual.get(Calendar.DAY_OF_MONTH)) {
            btnDia.setBackground(PRIMARY_BLUE);
            btnDia.setForeground(CLEAN_WHITE);
            btnDia.setBorder(BorderFactory.createLineBorder(PRIMARY_BLUE.darker(), 1));
        }

        /**
         * MouseAdapter para efeito hover nos botões de dia.
         * Similar ao botão de navegação, usa MouseAdapter porque precisamos
         * de múltiplos métodos (mouseEntered e mouseExited).
         * 
         * O efeito hover melhora a usabilidade indicando visualmente
         * que o elemento é clicável e qual será selecionado.
         */
        btnDia.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btnDia.getBackground() != PRIMARY_BLUE) {
                    btnDia.setBackground(new Color(240, 248, 255));
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                if (btnDia.getBackground() != PRIMARY_BLUE) {
                    btnDia.setBackground(CLEAN_WHITE);
                }
            }
        });

        /**
         * Expressão Lambda para navegação ao clicar em um dia específico.
         * A variável 'dia' precisa ser declarada como 'final' (ou ser efetivamente final)
         * para ser usada dentro da lambda. Isso acontece porque lambdas capturam variáveis
         * do escopo externo por valor, não por referência.
         * 
         * Como estamos dentro de um loop for, a variável 'dia' muda a cada iteração.
         * Por isso, criamos uma cópia final 'diaFinal' que mantém o valor específico
         * para cada botão criado.
         * 
         * A sintaxe 'e -> { ... }' permite executar múltiplas instruções quando o evento ocorre.
         * Neste caso, ajustamos o calendário para o dia clicado e atualizamos a visualização.
         */
        final int diaFinal = dia;
        btnDia.addActionListener(e -> {
            calendarioAtual.set(Calendar.DAY_OF_MONTH, diaFinal);
            atualizarVisualizacao();
        });
        
        return btnDia;
    }

    /**
     * Atualiza os cabeçalhos da tabela principal com os dias da semana correta.
     */
    private void atualizarCabecalhosTabela() { 
        Calendar inicioSemana = (Calendar) calendarioAtual.clone();
        inicioSemana.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        
        SimpleDateFormat formatoHeader = new SimpleDateFormat("E dd/MM", new Locale("pt", "BR"));
        
        String dataInicio = new SimpleDateFormat("dd/MM/yyyy").format(inicioSemana.getTime());
        Calendar fimSemana = (Calendar) inicioSemana.clone();
        fimSemana.add(Calendar.DAY_OF_YEAR, 6);
        String dataFim = new SimpleDateFormat("dd/MM/yyyy").format(fimSemana.getTime());
        lblSemanaAtual.setText(String.format("Semana de %s a %s", dataInicio, dataFim));
        
        for (int i = 1; i <= 7; i++) {
            tabelaAgenda.getColumnModel().getColumn(i).setHeaderValue(formatoHeader.format(inicioSemana.getTime()));
            inicioSemana.add(Calendar.DAY_OF_YEAR, 1);
        }
        tabelaAgenda.getTableHeader().repaint();
    }
    
    /**
     * Carrega os itens (médicos ou pacientes) no combobox de filtro.
     */
    private void carregarItensFiltro() {
        String tipo = (String) cbTipoFiltro.getSelectedItem();
        cbFiltroItem.removeAllItems();
        cbFiltroItem.addItem("Selecione...");
        try {
            if ("Por Médico".equals(tipo)) {
                /**
                 * Uso de Method Reference com forEach.
                 * 'medicoDAO.findAll().forEach(cbFiltroItem::addItem)' é equivalente a:
                 * 'medicoDAO.findAll().forEach(medico -> cbFiltroItem.addItem(medico))'
                 * O '::' é uma sintaxe especial para referenciar métodos existentes,
                 * tornando o código ainda mais conciso quando a lambda apenas chama um método.
                 */
                medicoDAO.findAll().forEach(cbFiltroItem::addItem);
            } else if ("Por Paciente".equals(tipo)) {
                /**
                 * Mesmo padrão de Method Reference para carregar pacientes.
                 * Esta sintaxe é preferível quando a lambda faria apenas uma chamada de método.
                 */
                pacienteDAO.findAll().forEach(cbFiltroItem::addItem);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar filtros: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Aplica o filtro selecionado e recarrega a grade de consultas.
     */
    private void aplicarFiltro() {
        try {
            List<Consulta> todasConsultas = consultaDAO.findAll();
            
            Object itemSelecionado = cbFiltroItem.getSelectedItem();
            if (itemSelecionado instanceof Medico) {
                /**
                 * Uso de Stream API com Expressões Lambda para filtragem.
                 * 'todasConsultas.stream().filter(c -> c.getMedico().getCrm().equals(...))'
                 * utiliza uma lambda como Predicate para testar cada elemento.
                 * 'c -> expressão_booleana' define a condição de filtragem, onde 'c'
                 * representa cada consulta na lista. Apenas consultas que retornam 'true'
                 * são mantidas no resultado final via '.collect(Collectors.toList())'.
                 */
                todasConsultas = todasConsultas.stream()
                    .filter(c -> c.getMedico().getCrm().equals(((Medico)itemSelecionado).getCrm()))
                    .collect(Collectors.toList());
            } else if (itemSelecionado instanceof Paciente) {
                /**
                 * Lambda similar para filtragem por paciente.
                 * Demonstra como a mesma estrutura de Stream + Lambda pode ser reutilizada
                 * para diferentes critérios de filtragem, mantendo o código consistente.
                 */
                 todasConsultas = todasConsultas.stream()
                    .filter(c -> c.getPaciente().getCpf().equals(((Paciente)itemSelecionado).getCpf()))
                    .collect(Collectors.toList());
            }

            popularTabela(todasConsultas);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao aplicar filtro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Popula a tabela principal com os dados das consultas na grade semanal.
     * Inclui o mapeamento correto para aplicação das cores.
     */
    private void popularTabela(List<Consulta> consultas) {
        String[] colunas = {"Horário", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom"};
        String[][] dados = new String[22][8];
        
        // Preencher coluna de horários
        for (int i = 0; i < 22; i++) {
            dados[i][0] = String.format("%02d:%02d", 8 + (i / 2), (i % 2) * 30);
        }
        
        modeloTabela.setDataVector(dados, colunas);
        tabelaAgenda.getColumnModel().getColumn(0).setPreferredWidth(70);
        
        // Limpar mapeamento anterior
        mapaConsultas.clear();
        
        // Calcular limites da semana
        Calendar inicioSemana = (Calendar) calendarioAtual.clone();
        inicioSemana.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        inicioSemana.set(Calendar.HOUR_OF_DAY, 0);
        inicioSemana.set(Calendar.MINUTE, 0);
        inicioSemana.set(Calendar.SECOND, 0);

        Calendar fimSemana = (Calendar) inicioSemana.clone();
        fimSemana.add(Calendar.DAY_OF_YEAR, 7);
        
        // Filtrar e mapear consultas da semana
        /**
         * Complexa cadeia de Stream API com múltiplas operações usando lambdas:
         * 1. '.filter(c -> { ... })' - Lambda que filtra consultas dentro do período da semana
         * 2. '.forEach(c -> { ... })' - Lambda que processa cada consulta filtrada
         * Esta estrutura demonstra como lambdas podem encadear operações complexas
         * de forma fluida e legível, substituindo loops tradicionais.
         */
        consultas.stream()
            .filter(c -> {
                Date dataConsulta = Date.from(c.getDataHorario().atZone(ZoneId.systemDefault()).toInstant());
                return !dataConsulta.before(inicioSemana.getTime()) && dataConsulta.before(fimSemana.getTime());
            })
            .forEach(c -> {
                // Calcular posição na tabela
                int linha = (c.getDataHorario().getHour() - 8) * 2 + (c.getDataHorario().getMinute() / 30);
                
                Calendar cal = Calendar.getInstance();
                cal.setTime(Date.from(c.getDataHorario().atZone(ZoneId.systemDefault()).toInstant()));
                int coluna = (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) ? 7 : cal.get(Calendar.DAY_OF_WEEK) - 1;

                if (linha >= 0 && linha < 22 && coluna >= 1 && coluna <= 7) {
                    String sigla = c.getStatus().getSigla();
                    String textoConsulta = String.format("[%s] %s", sigla, c.getPaciente().getNome());
                    modeloTabela.setValueAt(textoConsulta, linha, coluna);
                    
                    // Mapear consulta para aplicação de cores
                    String chave = linha + "," + coluna;
                    mapaConsultas.put(chave, c);
                }
            });
        
        // Reaplicar renderizadores após popular tabela
        aplicarRenderizadoresComCores();
        
        // Forçar repaint da tabela
        tabelaAgenda.revalidate();
        tabelaAgenda.repaint();
    }
    
    private void navegarMes(int incremento) {
        calendarioAtual.add(Calendar.MONTH, incremento);
        atualizarVisualizacao();
    }
    
    /**
     * Cria botões de ação estilizados para a interface.
     * @param texto O texto do botão.
     * @param cor A cor de fundo do botão.
     * @param acao ActionListener (geralmente uma expressão lambda) para o clique.
     * @return Um RoundedButton configurado.
     */
    private RoundedButton criarBotaoAcao(String texto, Color cor, ActionListener acao) {
        RoundedButton botao = new RoundedButton(texto);
        botao.setFont(new Font("Segoe UI", Font.BOLD, 12));
        botao.setForeground(CLEAN_WHITE);
        botao.setBackground(cor);
        botao.setPreferredSize(new Dimension(180, 40));
        botao.addActionListener(acao);
        return botao;
    }

    private JPanel criarIndicadorLegenda(String texto, Color cor) {
        JPanel indicador = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        indicador.setOpaque(false);
        JPanel quadrado = new JPanel();
        quadrado.setBackground(cor);
        quadrado.setPreferredSize(new Dimension(12, 12));
        indicador.add(quadrado);
        indicador.add(new JLabel(texto));
        return indicador;
    }
    
    private enum StatusAcao { REALIZADA, CANCELADA }
    
    /**
     * Renderizador para a coluna de horários.
     */
    private class HorarioRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
                boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBackground(LIGHT_GRAY);
            setForeground(DARK_TEXT);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            return this;
        }
    }
    
    /**
     * Renderizador customizado para as células de agendamento com cores funcionais.
     */
    private class AgendamentoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
                boolean hasFocus, int row, int column) {
            
            // Configuração básica do componente
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            // Garantir que a célula seja opaca para mostrar cores
            setOpaque(true);
            
            // Verificar se existe consulta nesta posição
            String chave = row + "," + column;
            Consulta consulta = mapaConsultas.get(chave);
            
            if (consulta != null) {
                // Aplicar cor baseada no status da consulta
                Color corFundo;
                switch(consulta.getStatus()) {
                    case AGENDADA:
                        corFundo = new Color(52, 144, 220, 180); // Azul semi-transparente
                        break;
                    case REALIZADA:
                        corFundo = new Color(76, 175, 80, 180); // Verde semi-transparente
                        break;
                    case CANCELADA:
                        corFundo = new Color(244, 67, 54, 180); // Vermelho semi-transparente
                        break;
                    case NAO_COMPARECEU:
                        corFundo = new Color(108, 117, 125, 180); // Cinza semi-transparente
                        break;
                    default:
                        corFundo = CLEAN_WHITE;
                        break;
                }
                
                setBackground(corFundo);
                setForeground(DARK_TEXT);
                setFont(new Font("Segoe UI", Font.BOLD, 10));
                
                // Tooltip informativo
                setToolTipText(String.format("<html><b>Paciente:</b> %s<br><b>Médico:</b> %s<br><b>Status:</b> %s<br><i>Clique direito para opções</i></html>", 
                    consulta.getPaciente().getNome(), 
                    consulta.getMedico().getNome(),
                    consulta.getStatus().getDescricao()));
            } else {
                // Célula vazia
                setBackground(CLEAN_WHITE);
                setForeground(DARK_TEXT);
                setFont(new Font("Segoe UI", Font.PLAIN, 10));
                setToolTipText("Clique direito para novo agendamento");
            }
            
            return this;
        }
    }

    /**
     * Classe interna para criar botões com cantos arredondados.
     */
    private class RoundedButton extends JButton {
        public RoundedButton(String text) { 
            super(text); 
            setContentAreaFilled(false); 
            setFocusPainted(false); 
            setBorder(new EmptyBorder(5, 10, 5, 10)); 
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (getModel().isPressed()) {
                g2.setColor(getBackground().darker());
            } else if (getModel().isRollover()) {
                g2.setColor(getBackground().brighter());
            } else {
                g2.setColor(getBackground());
            }
            
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
            super.paintComponent(g2);
            g2.dispose();
        }
    }
}