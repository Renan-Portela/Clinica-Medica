package br.com.clinica.view;

import br.com.clinica.dao.PacienteDAO;
import br.com.clinica.model.Paciente;
import br.com.clinica.util.ValidadorCPF;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Tela de gerenciamento de pacientes com interface moderna
 * CRUD completo para cadastro, edição, exclusão e listagem de pacientes
 * Interface otimizada para apresentações com fontes grandes e layout responsivo
 * 
 * Funcionalidades principais:
 * - Cadastro de pacientes com validação de CPF
 * - Máscaras para CPF, telefone e data de nascimento
 * - Histórico médico com área de texto expandida
 * - Tabela interativa com dados formatados
 * - Cálculo automático de idade
 * - Validações em tempo real e feedback visual
 */
public class TelaPacientes extends JFrame {
    
    private static final long serialVersionUID = 1L;
    
    // Paleta de cores médica profissional - consistente com o sistema
    private static final Color PRIMARY_BLUE = new Color(52, 144, 220);
    private static final Color MEDICAL_GREEN = new Color(76, 175, 80);
    private static final Color CLEAN_WHITE = new Color(255, 255, 255);
    private static final Color LIGHT_GRAY = new Color(248, 249, 250);
    private static final Color DARK_TEXT = new Color(52, 58, 64);
    private static final Color ACCENT_RED = new Color(220, 53, 69);
    private static final Color ORANGE_ACCENT = new Color(255, 152, 0);
    private static final Color SUCCESS_GREEN = new Color(40, 167, 69);
    
    // Componentes principais
    private JPanel contentPane;
    private JPanel headerPanel;
    private JPanel formPanel;
    private JPanel tablePanel;
    private JPanel buttonPanel;
    
    // Componentes da tabela
    private JTable table;
    private DefaultTableModel tableModel;
    private PacienteDAO pacienteDAO;
    
    // Campos do formulário com fontes grandes para apresentação
    private JTextField txtCpf;
    private JTextField txtNome;
    private JTextField txtDataNascimento;
    private JTextField txtEndereco;
    private JTextField txtTelefone;
    private JTextArea txtHistorico;
    
    // Botões de ação
    private JButton btnNovo;
    private JButton btnSalvar;
    private JButton btnExcluir;
    private JButton btnFechar;
    
    // Controle de estado
    private Paciente pacienteSelecionado = null;
    
    /**
     * Construtor principal - inicializa DAO e interface
     * Carrega dados existentes na inicialização
     */
    public TelaPacientes() {
        this.pacienteDAO = new PacienteDAO();
        inicializarInterface();
        carregarPacientes();
    }
    
    /**
     * Configura a interface principal com layout moderno
     * Substitui setBounds() por layouts responsivos
     * Aplica fontes grandes para facilitar apresentações
     */
    private void inicializarInterface() {
        configurarJanela();
        criarComponentesPrincipais();
        organizarLayout();
        aplicarEstilosVisuais();
    }
    
    /**
     * Configura propriedades básicas da janela
     * Tela cheia, título e comportamento de fechamento
     */
    private void configurarJanela() {
        setTitle("Gerenciamento de Pacientes - Sistema Clinica");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Painel principal com background moderno
        contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(LIGHT_GRAY);
        setContentPane(contentPane);
    }
    
    /**
     * Cria todos os componentes principais da interface
     * Header, formulário, tabela e botões de ação
     */
    private void criarComponentesPrincipais() {
        criarHeader();
        criarFormulario();
        criarTabelaPacientes();
        criarBotoesAcao();
    }
    
    /**
     * Cria o header com título da seção
     * Visual consistente com o restante do sistema
     */
    private void criarHeader() {
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_BLUE);
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        // Título da seção com fonte grande para apresentação
        JLabel lblTitulo = new JLabel("GERENCIAMENTO DE PACIENTES");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(CLEAN_WHITE);
        
        // Subtítulo informativo
        JLabel lblSubtitulo = new JLabel("Cadastro, edição e controle de dados pessoais e histórico médico");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblSubtitulo.setForeground(new Color(255, 255, 255, 200));
        
        // Organizar header
        JPanel headerLeft = new JPanel();
        headerLeft.setLayout(new BoxLayout(headerLeft, BoxLayout.Y_AXIS));
        headerLeft.setOpaque(false);
        headerLeft.add(lblTitulo);
        headerLeft.add(Box.createVerticalStrut(5));
        headerLeft.add(lblSubtitulo);
        
        headerPanel.add(headerLeft, BorderLayout.WEST);
    }
    
    /**
     * Cria o formulário de dados do paciente
     * Layout organizado em linhas lógicas com histórico ao lado
     * Mantém todas as validações e máscaras originais
     */
    private void criarFormulario() {
        formPanel = new JPanel(new BorderLayout());
        formPanel.setBackground(CLEAN_WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_BLUE, 2),
                "Dados do Paciente",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 18),
                PRIMARY_BLUE
            ),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        formPanel.setPreferredSize(new Dimension(0, 280));
        
        // Painel interno organizado em linhas
        JPanel camposPanel = new JPanel();
        camposPanel.setLayout(new BoxLayout(camposPanel, BoxLayout.Y_AXIS));
        camposPanel.setBackground(CLEAN_WHITE);
        
        // Linha 1: CPF, Nome, Data Nascimento
        JPanel linha1 = criarLinhaFormulario();
        adicionarCampoCPF(linha1);
        adicionarCampoNome(linha1);
        adicionarCampoDataNascimento(linha1);
        camposPanel.add(linha1);
        camposPanel.add(Box.createVerticalStrut(20));
        
        // Linha 2: Endereço, Telefone e Histórico Médico (lado a lado)
        JPanel linha2 = criarLinhaFormulario();
        adicionarCampoEndereco(linha2);
        adicionarCampoTelefone(linha2);
        adicionarCampoHistorico(linha2);
        camposPanel.add(linha2);
        
        formPanel.add(camposPanel, BorderLayout.CENTER);
    }
    
    /**
     * Cria uma linha do formulário com layout horizontal
     * @return JPanel configurado para receber campos lado a lado
     */
    private JPanel criarLinhaFormulario() {
        JPanel linha = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        linha.setBackground(CLEAN_WHITE);
        linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120)); // Altura maior para histórico
        return linha;
    }
    
    /**
     * Adiciona campo CPF à linha do formulário
     * Formato ###.###.###-## com validação automática
     */
    private void adicionarCampoCPF(JPanel linha) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(CLEAN_WHITE);
        
        JLabel lblCpf = new JLabel("CPF:");
        lblCpf.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblCpf.setForeground(DARK_TEXT);
        lblCpf.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        try {
            MaskFormatter cpfMask = new MaskFormatter("###.###.###-##");
            cpfMask.setPlaceholderCharacter('_');
            txtCpf = new JFormattedTextField(cpfMask);
        } catch (ParseException e) {
            txtCpf = new JTextField();
        }
        
        txtCpf.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtCpf.setPreferredSize(new Dimension(180, 35));
        txtCpf.setMaximumSize(new Dimension(180, 35));
        txtCpf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        txtCpf.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        container.add(lblCpf);
        container.add(Box.createVerticalStrut(5));
        container.add(txtCpf);
        linha.add(container);
    }
    
    /**
     * Adiciona campo Nome à linha do formulário
     * Campo amplo para nomes completos
     */
    private void adicionarCampoNome(JPanel linha) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(CLEAN_WHITE);
        
        JLabel lblNome = new JLabel("Nome Completo:");
        lblNome.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblNome.setForeground(DARK_TEXT);
        lblNome.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        txtNome = new JTextField();
        txtNome.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtNome.setPreferredSize(new Dimension(300, 35));
        txtNome.setMaximumSize(new Dimension(300, 35));
        txtNome.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        txtNome.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        container.add(lblNome);
        container.add(Box.createVerticalStrut(5));
        container.add(txtNome);
        linha.add(container);
    }
    
    /**
     * Adiciona campo Data de Nascimento à linha do formulário
     * Formato dd/MM/yyyy com validação
     */
    private void adicionarCampoDataNascimento(JPanel linha) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(CLEAN_WHITE);
        
        JLabel lblData = new JLabel("Data Nascimento:");
        lblData.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblData.setForeground(DARK_TEXT);
        lblData.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        try {
            MaskFormatter dataMask = new MaskFormatter("##/##/####");
            dataMask.setPlaceholderCharacter('_');
            txtDataNascimento = new JFormattedTextField(dataMask);
        } catch (ParseException e) {
            txtDataNascimento = new JTextField();
        }
        
        txtDataNascimento.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtDataNascimento.setPreferredSize(new Dimension(140, 35));
        txtDataNascimento.setMaximumSize(new Dimension(140, 35));
        txtDataNascimento.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        txtDataNascimento.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtDataNascimento.setToolTipText("dd/MM/yyyy");
        
        container.add(lblData);
        container.add(Box.createVerticalStrut(5));
        container.add(txtDataNascimento);
        linha.add(container);
    }
    
    /**
     * Adiciona campo Endereço à linha do formulário
     * Campo amplo para endereços completos
     */
    private void adicionarCampoEndereco(JPanel linha) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(CLEAN_WHITE);
        
        JLabel lblEndereco = new JLabel("Endereço:");
        lblEndereco.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblEndereco.setForeground(DARK_TEXT);
        lblEndereco.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        txtEndereco = new JTextField();
        txtEndereco.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtEndereco.setPreferredSize(new Dimension(350, 35));
        txtEndereco.setMaximumSize(new Dimension(350, 35));
        txtEndereco.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        txtEndereco.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        container.add(lblEndereco);
        container.add(Box.createVerticalStrut(5));
        container.add(txtEndereco);
        linha.add(container);
    }
    
    /**
     * Adiciona campo Telefone à linha do formulário
     * Formato (##) #####-#### para celulares
     */
    private void adicionarCampoTelefone(JPanel linha) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(CLEAN_WHITE);
        
        JLabel lblTelefone = new JLabel("Telefone:");
        lblTelefone.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTelefone.setForeground(DARK_TEXT);
        lblTelefone.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        try {
            MaskFormatter telMask = new MaskFormatter("(##) #####-####");
            telMask.setPlaceholderCharacter('_');
            txtTelefone = new JFormattedTextField(telMask);
        } catch (ParseException e) {
            txtTelefone = new JTextField();
        }
        
        txtTelefone.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtTelefone.setPreferredSize(new Dimension(180, 35));
        txtTelefone.setMaximumSize(new Dimension(180, 35));
        txtTelefone.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        txtTelefone.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtTelefone.setToolTipText("(11) 99999-9999");
        
        container.add(lblTelefone);
        container.add(Box.createVerticalStrut(5));
        container.add(txtTelefone);
        linha.add(container);
    }
    
    /**
     * Adiciona campo Histórico Médico à linha do formulário
     * Fica ao lado dos outros campos com largura adequada
     */
    private void adicionarCampoHistorico(JPanel linha) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(CLEAN_WHITE);
        
        JLabel lblHistorico = new JLabel("Histórico Médico:");
        lblHistorico.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblHistorico.setForeground(DARK_TEXT);
        lblHistorico.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        txtHistorico = new JTextArea(4, 30); // 4 linhas, 30 colunas
        txtHistorico.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtHistorico.setLineWrap(true);
        txtHistorico.setWrapStyleWord(true);
        txtHistorico.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        txtHistorico.setBackground(CLEAN_WHITE);
        
        JScrollPane scrollHistorico = new JScrollPane(txtHistorico);
        scrollHistorico.setPreferredSize(new Dimension(300, 80)); // Largura menor para ficar ao lado
        scrollHistorico.setMaximumSize(new Dimension(300, 80));
        scrollHistorico.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        scrollHistorico.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollHistorico.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        container.add(lblHistorico);
        container.add(Box.createVerticalStrut(5));
        container.add(scrollHistorico);
        linha.add(container);
    }
    
    /**
     * Cria tabela de pacientes com formatação moderna
     * Colunas: CPF formatado, Nome, Idade calculada, Telefone, Endereço
     * Fonte grande e altura de linha otimizada para apresentações
     */
    private void criarTabelaPacientes() {
        tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(CLEAN_WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_BLUE, 2),
                "Lista de Pacientes Cadastrados",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 18),
                PRIMARY_BLUE
            ),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Modelo da tabela com colunas não editáveis
        String[] colunas = {"CPF", "Nome", "Idade", "Telefone", "Endereço"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabela somente leitura
            }
        };
        
        // Configurar tabela com fonte grande para apresentação
        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 16)); // Fonte grande para apresentação
        table.setRowHeight(30); // Altura aumentada para melhor legibilidade
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(52, 144, 220, 30));
        table.setSelectionForeground(DARK_TEXT);
        table.setGridColor(new Color(220, 220, 220));
        
        // Header da tabela com destaque
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));
        table.getTableHeader().setBackground(LIGHT_GRAY);
        table.getTableHeader().setForeground(DARK_TEXT);
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        
        // Listener para seleção na tabela
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                carregarPacienteSelecionado();
            }
        });
        
        // Configurar larguras das colunas
        table.getColumnModel().getColumn(0).setPreferredWidth(150); // CPF
        table.getColumnModel().getColumn(1).setPreferredWidth(300); // Nome
        table.getColumnModel().getColumn(2).setPreferredWidth(80);  // Idade
        table.getColumnModel().getColumn(3).setPreferredWidth(150); // Telefone
        table.getColumnModel().getColumn(4).setPreferredWidth(350); // Endereço
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        tablePanel.add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * Cria botões de ação CRUD com design moderno
     * Cores semânticas e fontes grandes para apresentações
     * Hover effects para feedback visual
     */
    private void criarBotoesAcao() {
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonPanel.setBackground(LIGHT_GRAY);
        buttonPanel.setPreferredSize(new Dimension(0, 80));
        
        // Botão Novo - limpa formulário
        btnNovo = criarBotaoAcao("Novo Paciente", SUCCESS_GREEN);
        btnNovo.addActionListener(e -> limparFormulario());
        
        // Botão Salvar - persiste dados
        btnSalvar = criarBotaoAcao("Salvar", PRIMARY_BLUE);
        btnSalvar.addActionListener(e -> salvarPaciente());
        
        // Botão Excluir - remove paciente selecionado
        btnExcluir = criarBotaoAcao("Excluir", ACCENT_RED);
        btnExcluir.addActionListener(e -> excluirPaciente());
        
        // Botão Fechar - fecha janela
        btnFechar = criarBotaoAcao("Fechar", new Color(108, 117, 125));
        btnFechar.addActionListener(e -> dispose());
        
        buttonPanel.add(btnNovo);
        buttonPanel.add(btnSalvar);
        buttonPanel.add(btnExcluir);
        buttonPanel.add(btnFechar);
    }
    
    /**
     * Cria um botão de ação com estilo moderno
     * Fonte grande, hover effect e cores semânticas
     * 
     * @param texto Texto do botão
     * @param cor Cor de fundo
     * @return JButton estilizado
     */
    private JButton criarBotaoAcao(String texto, Color cor) {
        JButton botao = new JButton(texto);
        botao.setFont(new Font("Segoe UI", Font.BOLD, 16));
        botao.setForeground(CLEAN_WHITE);
        botao.setBackground(cor);
        botao.setPreferredSize(new Dimension(160, 50));
        botao.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        botao.setFocusPainted(false);
        botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect para feedback visual
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
     * Organiza o layout final da interface
     * Header, formulário, tabela e botões em BorderLayout
     */
    private void organizarLayout() {
        // Painel superior combinando header + formulário
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(formPanel, BorderLayout.CENTER);
        
        contentPane.add(topPanel, BorderLayout.NORTH);
        contentPane.add(tablePanel, BorderLayout.CENTER);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Aplica estilos visuais finais
     * Bordas, sombras e efeitos de profundidade
     */
    private void aplicarEstilosVisuais() {
        // Sombra no header
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0, 0, 0, 30)),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
        
        // Espaçamento entre formulário e tabela
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            formPanel.getBorder(),
            BorderFactory.createEmptyBorder(0, 0, 10, 0)
        ));
    }
    
    /**
     * Carrega todos os pacientes do banco de dados
     * Preenche tabela com dados formatados e idade calculada
     * Tratamento robusto de dados nulos e exceções
     */
    private void carregarPacientes() {
        try {
            List<Paciente> pacientes = pacienteDAO.findAll();
            tableModel.setRowCount(0); // Limpar tabela existente
            
            if (pacientes != null && !pacientes.isEmpty()) {
                for (Paciente paciente : pacientes) {
                    // Verificar e formatar dados com proteção contra nulos
                    String cpfFormatado = paciente.getCpf() != null ? 
                        ValidadorCPF.formatar(paciente.getCpf()) : "N/A";
                    String nome = paciente.getNome() != null ? paciente.getNome() : "N/A";
                    String idade = paciente.getIdade() + " anos";
                    String telefone = paciente.getTelefone() != null ? paciente.getTelefone() : "N/A";
                    String endereco = paciente.getEndereco() != null ? paciente.getEndereco() : "N/A";
                    
                    Object[] row = {cpfFormatado, nome, idade, telefone, endereco};
                    tableModel.addRow(row);
                }
                System.out.println("Carregados " + pacientes.size() + " pacientes");
            } else {
                System.out.println("Nenhum paciente encontrado");
            }
            
        } catch (Exception e) {
            System.err.println("Erro detalhado ao carregar pacientes: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Erro ao carregar pacientes: " + e.getMessage() + 
                "\nVerifique se o banco de dados está conectado.",
                "Erro de Carregamento", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Carrega dados do paciente selecionado na tabela
     * Preenche formulário com informações para edição
     * Remove formatação do CPF antes de buscar no banco
     */
    private void carregarPacienteSelecionado() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            try {
                // Extrair CPF da tabela removendo formatação
                String cpfFormatado = (String) tableModel.getValueAt(selectedRow, 0);
                String cpf = cpfFormatado.replaceAll("[^0-9]", "");
                
                pacienteSelecionado = pacienteDAO.findById(cpf);
                if (pacienteSelecionado != null) {
                    preencherFormulario(pacienteSelecionado);
                }
            } catch (Exception e) {
                System.err.println("Erro ao carregar paciente selecionado: " + e.getMessage());
                JOptionPane.showMessageDialog(this, 
                    "Erro ao carregar paciente: " + e.getMessage(),
                    "Erro de Carregamento", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Preenche formulário com dados do paciente
     * Converte dados do banco para componentes visuais
     * Aplica formatações e trata valores nulos
     * 
     * @param paciente Objeto paciente com dados a serem exibidos
     */
    private void preencherFormulario(Paciente paciente) {
        txtCpf.setText(ValidadorCPF.formatar(paciente.getCpf()));
        txtNome.setText(paciente.getNome());
        txtDataNascimento.setText(paciente.getDataNascimento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        txtEndereco.setText(paciente.getEndereco() != null ? paciente.getEndereco() : "");
        txtTelefone.setText(paciente.getTelefone() != null ? paciente.getTelefone() : "");
        txtHistorico.setText(paciente.getHistoricoMedico() != null ? paciente.getHistoricoMedico() : "");
    }
    
    /**
     * Limpa todos os campos do formulário
     * Reseta para valores padrão (novo cadastro)
     * Desmarca seleção da tabela
     */
    private void limparFormulario() {
        txtCpf.setText("");
        txtNome.setText("");
        txtDataNascimento.setText("");
        txtEndereco.setText("");
        txtTelefone.setText("");
        txtHistorico.setText("");
        
        pacienteSelecionado = null;
        table.clearSelection();
    }
    
    /**
     * Salva paciente no banco de dados
     * Executa validações completas antes de persistir
     * Diferencia entre inserção (novo) e atualização (edição)
     * Feedback visual de sucesso ou erro com foco em campos inválidos
     */
    private void salvarPaciente() {
        try {
            // Validação: CPF obrigatório
            if (txtCpf.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "CPF é obrigatório!", 
                    "Erro de Validação", JOptionPane.ERROR_MESSAGE);
                txtCpf.requestFocus();
                return;
            }
            
            // Validação: formato e algoritmo do CPF
            String cpfTexto = txtCpf.getText();
            String cpf = cpfTexto != null ? cpfTexto.replaceAll("[^0-9]", "") : "";
            if (!ValidadorCPF.validar(cpf)) {
                JOptionPane.showMessageDialog(this, "CPF inválido! Verifique os dígitos.", 
                    "Erro de Validação", JOptionPane.ERROR_MESSAGE);
                txtCpf.requestFocus();
                return;
            }
            
            // Validação: nome obrigatório
            if (txtNome.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nome é obrigatório!", 
                    "Erro de Validação", JOptionPane.ERROR_MESSAGE);
                txtNome.requestFocus();
                return;
            }
            
            // Validação: data de nascimento obrigatória
            if (txtDataNascimento.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Data de nascimento é obrigatória!", 
                    "Erro de Validação", JOptionPane.ERROR_MESSAGE);
                txtDataNascimento.requestFocus();
                return;
            }
            
            // Validação e conversão da data
            LocalDate dataNascimento;
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                dataNascimento = LocalDate.parse(txtDataNascimento.getText().trim(), formatter);
                
                // Validar se não é data futura
                if (dataNascimento.isAfter(LocalDate.now())) {
                    JOptionPane.showMessageDialog(this, "Data de nascimento não pode ser futura!", 
                        "Erro de Validação", JOptionPane.ERROR_MESSAGE);
                    txtDataNascimento.requestFocus();
                    return;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Data inválida! Use formato dd/MM/yyyy", 
                    "Erro de Validação", JOptionPane.ERROR_MESSAGE);
                txtDataNascimento.requestFocus();
                return;
            }
            
            // Criar objeto paciente com dados validados
            Paciente paciente = new Paciente();
            paciente.setCpf(cpf);
            paciente.setNome(txtNome.getText().trim());
            paciente.setDataNascimento(dataNascimento);
            paciente.setEndereco(txtEndereco.getText().trim());
            paciente.setTelefone(txtTelefone.getText().trim());
            paciente.setHistoricoMedico(txtHistorico.getText().trim());
            
            // Decidir entre inserção ou atualização
            if (pacienteSelecionado == null) {
                // Novo paciente
                pacienteDAO.save(paciente);
                JOptionPane.showMessageDialog(this, "Paciente cadastrado com sucesso!", 
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Atualização - confirmar antes de alterar
                int opcao = JOptionPane.showConfirmDialog(
                    this,
                    "Deseja realmente atualizar os dados do paciente " + pacienteSelecionado.getNome() + "?",
                    "Confirmar Alteração",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );
                
                if (opcao == JOptionPane.YES_OPTION) {
                    pacienteDAO.update(paciente);
                    JOptionPane.showMessageDialog(this, "Paciente atualizado com sucesso!", 
                        "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    return; // Cancelar operação
                }
            }
            
            // Atualizar interface após salvar
            carregarPacientes();
            limparFormulario();
            
        } catch (Exception e) {
            System.err.println("Erro ao salvar paciente: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao salvar paciente: " + e.getMessage(), 
                "Erro de Sistema", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Exclui paciente selecionado do banco de dados
     * Solicita confirmação antes de excluir
     * Remove registro permanentemente
     */
    private void excluirPaciente() {
        if (pacienteSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um paciente para excluir!", 
                "Seleção Necessária", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Confirmação de exclusão com aviso claro
        int opcao = JOptionPane.showConfirmDialog(
            this,
            "Deseja realmente excluir o paciente " + pacienteSelecionado.getNome() + "?\n" +
            "Esta ação não pode ser desfeita!",
            "Confirmar Exclusão",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (opcao == JOptionPane.YES_OPTION) {
            try {
                pacienteDAO.delete(pacienteSelecionado.getCpf());
                JOptionPane.showMessageDialog(this, "Paciente excluído com sucesso!", 
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                
                // Atualizar interface após exclusão
                carregarPacientes();
                limparFormulario();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir paciente: " + e.getMessage(), 
                    "Erro de Sistema", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}