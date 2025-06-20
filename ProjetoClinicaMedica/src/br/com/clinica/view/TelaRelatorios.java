package br.com.clinica.view;

import br.com.clinica.dao.MedicoDAO;
import br.com.clinica.model.Medico;
import br.com.clinica.service.RelatorioService;
import br.com.clinica.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Map;
import java.util.List;

/**
 * Tela para geração de relatórios gerenciais sobre as operações da clínica.
 * A interface é organizada com seleção de relatórios no canto superior esquerdo,
 * filtros na área superior central e resultados na área principal.
 * Permite interatividade na tabela de resultados para navegação detalhada.
 * Interage com as classes: RelatorioService, MedicoDAO, UITheme.
 */
public class TelaRelatorios extends JFrame implements UITheme {

    private static final long serialVersionUID = 1L;

    private JTable table;
    private JTextArea txtResumo;
    private CardLayout cardLayoutFiltros;
    private JPanel painelContainerFiltros;
    private ButtonGroup grupoRelatorios;
    private RelatorioService relatorioService;
    private MedicoDAO medicoDAO;

    // Componentes dos painéis de filtro - Consultas por Médico
    private JComboBox<Medico> cbMedicoFiltro;
    private JComboBox<String> cbMesFiltro;
    private JComboBox<Integer> cbAnoFiltro;

    // Componentes dos painéis de filtro - Consultas Canceladas
    private JComboBox<String> cbMesCanceladas;
    private JComboBox<Integer> cbAnoCanceladas;

    // Componentes dos painéis de filtro - Distribuição de Consultas
    private JComboBox<String> cbMesDistribuicao;
    private JComboBox<Integer> cbAnoDistribuicao;

    // Componente do painel de filtro - Histórico do Paciente
    private JTextField txtPacienteFiltro;

    public TelaRelatorios() {
        this.relatorioService = new RelatorioService();
        this.medicoDAO = new MedicoDAO();
        inicializarInterface();
        carregarDadosFiltros();
    }

    private void inicializarInterface() {
        configurarJanela();
        getContentPane().add(criarHeader(), BorderLayout.NORTH);
        getContentPane().add(criarPainelPrincipal(), BorderLayout.CENTER);
        getContentPane().add(criarPainelDeBotoes(), BorderLayout.SOUTH);
    }
    
    private void configurarJanela() {
        setTitle("Relatórios Gerenciais - Sistema Clínica");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(LIGHT_GRAY);
        setContentPane(contentPane);
    }

    private JPanel criarHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_BLUE);
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        JLabel lblTitulo = new JLabel("RELATÓRIOS GERENCIAIS");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitulo.setForeground(CLEAN_WHITE);
        headerPanel.add(lblTitulo, BorderLayout.WEST);
        return headerPanel;
    }
    
    /**
     * Cria o painel principal reorganizado com melhor distribuição de espaço.
     * @return O painel principal configurado.
     */
    private JPanel criarPainelPrincipal() {
        JPanel painelPrincipal = new JPanel(new BorderLayout(10, 10));
        painelPrincipal.setBorder(new EmptyBorder(20, 20, 20, 20));
        painelPrincipal.setBackground(LIGHT_GRAY);
        
        JPanel painelSuperior = new JPanel(new BorderLayout(15, 0));
        painelSuperior.setOpaque(false);
        
        painelSuperior.add(criarPainelTiposRelatorioCompacto(), BorderLayout.WEST);
        painelSuperior.add(criarPainelContainerFiltros(), BorderLayout.CENTER);
        
        JPanel painelResultados = criarPainelResultadosCompleto();
        
        painelPrincipal.add(painelSuperior, BorderLayout.NORTH);
        painelPrincipal.add(painelResultados, BorderLayout.CENTER);
        
        return painelPrincipal;
    }
    
    /**
     * Cria o painel de seleção de tipos de relatório em formato compacto.
     * @return O painel de radio buttons configurado.
     */
    private JPanel criarPainelTiposRelatorioCompacto() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_BLUE, 1),
                "Tipo de Relatório", 
                0, 0, 
                new Font("Segoe UI", Font.BOLD, 16), 
                PRIMARY_BLUE
            ),
            new EmptyBorder(10, 15, 15, 15)
        ));
        panel.setBackground(CLEAN_WHITE);
        panel.setPreferredSize(new Dimension(320, 250));
        
        grupoRelatorios = new ButtonGroup();
        String[] nomesRelatorios = {
            "Consultas por Médico", 
            "Consultas Canceladas", 
            "Histórico do Paciente",
            "Pacientes Inativos", 
            "Distribuição de Consultas"
        };

        for (String nome : nomesRelatorios) {
            JRadioButton radio = new JRadioButton(nome);
            radio.setActionCommand(nome);
            radio.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            radio.setOpaque(false);
            radio.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            radio.addActionListener(e -> {
                limparResultados();
                cardLayoutFiltros.show(painelContainerFiltros, e.getActionCommand());
            });
            
            grupoRelatorios.add(radio);
            panel.add(radio);
            panel.add(Box.createVerticalStrut(10));
        }
        
        return panel;
    }

    /**
     * Cria o container de filtros com painéis específicos para cada tipo de relatório.
     * @return O painel de filtros configurado.
     */
    private JPanel criarPainelContainerFiltros() {
        cardLayoutFiltros = new CardLayout();
        painelContainerFiltros = new JPanel(cardLayoutFiltros);
        painelContainerFiltros.setPreferredSize(new Dimension(0, 120));
        
        JPanel filtroVazio = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtroVazio.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                "Filtros",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 14),
                DARK_TEXT
            ),
            new EmptyBorder(10, 10, 10, 10)
        ));
        filtroVazio.setBackground(CLEAN_WHITE);
        JLabel lblInstrucao = new JLabel("Selecione um tipo de relatório para exibir os filtros disponíveis.");
        lblInstrucao.setFont(new Font("Dialog", Font.PLAIN, 18));
        filtroVazio.add(lblInstrucao);
        
        // Painéis específicos para cada tipo de relatório - NOMES CORRIGIDOS
        painelContainerFiltros.add(criarPainelFiltroComMedico(), "Consultas por Médico");
        painelContainerFiltros.add(criarPainelFiltroConsultasCanceladas(), "Consultas Canceladas");
        painelContainerFiltros.add(criarPainelFiltroPaciente(), "Histórico do Paciente");
        painelContainerFiltros.add(filtroVazio, "Pacientes Inativos");
        painelContainerFiltros.add(criarPainelFiltroDistribuicaoConsultas(), "Distribuição de Consultas");
        painelContainerFiltros.add(filtroVazio, "DEFAULT");
        
        cardLayoutFiltros.show(painelContainerFiltros, "DEFAULT");
        return painelContainerFiltros;
    }

    /**
     * Cria a área de resultados com tabela e resumo organizados verticalmente.
     * @return O painel de resultados configurado.
     */
    private JPanel criarPainelResultadosCompleto() {
        JPanel painelResultados = new JPanel(new BorderLayout(0, 10));
        painelResultados.setOpaque(false);
        
        table = new JTable();
        table.setFont(new Font("Dialog", Font.PLAIN, 18));
        table.setRowHeight(35);
        
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    abrirTelaDeDetalhes();
                }
            }
        });
        
        JScrollPane scrollTable = new JScrollPane(table);
        scrollTable.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            "Resultados",
            0, 0,
            new Font("Segoe UI", Font.BOLD, 16),
            DARK_TEXT
        ));
        
        txtResumo = new JTextArea(4, 0);
        txtResumo.setFont(new Font("Monospaced", Font.PLAIN, 16));
        txtResumo.setEditable(false);
        txtResumo.setBackground(LIGHT_GRAY);
        
        JScrollPane scrollResumo = new JScrollPane(txtResumo);
        scrollResumo.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            "Resumo",
            0, 0,
            new Font("Segoe UI", Font.BOLD, 16),
            DARK_TEXT
        ));
        scrollResumo.setPreferredSize(new Dimension(0, 140));
        
        painelResultados.add(scrollTable, BorderLayout.CENTER);
        painelResultados.add(scrollResumo, BorderLayout.SOUTH);
        
        return painelResultados;
    }

    /**
     * Cria painel de filtro completo (com médico, mês e ano).
     * @return O painel de filtros configurado.
     */
    private JPanel criarPainelFiltroComMedico() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                "Filtros Disponíveis",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 14),
                DARK_TEXT
            ),
            new EmptyBorder(5, 10, 5, 10)
        ));
        panel.setBackground(CLEAN_WHITE);
        
        JLabel lblMedico = new JLabel("Médico:");
        lblMedico.setFont(new Font("Dialog", Font.BOLD, 18));
        panel.add(lblMedico);
        cbMedicoFiltro = new JComboBox<>();
        cbMedicoFiltro.setFont(new Font("Dialog", Font.PLAIN, 16));
        cbMedicoFiltro.setPreferredSize(new Dimension(250, 30));
        panel.add(cbMedicoFiltro);
        
        JLabel lblMes = new JLabel("Mês:");
        lblMes.setFont(new Font("Dialog", Font.BOLD, 18));
        panel.add(lblMes);
        String[] meses = {"Todos", "Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"};
        cbMesFiltro = new JComboBox<>(meses);
        cbMesFiltro.setFont(new Font("Dialog", Font.PLAIN, 16));
        panel.add(cbMesFiltro);

        JLabel lblAno = new JLabel("Ano:");
        lblAno.setFont(new Font("Dialog", Font.BOLD, 18));
        panel.add(lblAno);
        cbAnoFiltro = new JComboBox<>();
        cbAnoFiltro.setFont(new Font("Dialog", Font.PLAIN, 16));
        panel.add(cbAnoFiltro);
        
        return panel;
    }

    /**
     * Cria painel de filtro específico para Consultas Canceladas.
     * @return O painel de filtros configurado.
     */
    private JPanel criarPainelFiltroConsultasCanceladas() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                "Filtros Disponíveis",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 14),
                DARK_TEXT
            ),
            new EmptyBorder(5, 10, 5, 10)
        ));
        panel.setBackground(CLEAN_WHITE);
        
        JLabel lblMes = new JLabel("Mês:");
        lblMes.setFont(new Font("Dialog", Font.BOLD, 18));
        panel.add(lblMes);
        String[] meses = {"Todos", "Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"};
        cbMesCanceladas = new JComboBox<>(meses);
        cbMesCanceladas.setFont(new Font("Dialog", Font.PLAIN, 16));
        panel.add(cbMesCanceladas);

        JLabel lblAno = new JLabel("Ano:");
        lblAno.setFont(new Font("Dialog", Font.BOLD, 18));
        panel.add(lblAno);
        cbAnoCanceladas = new JComboBox<>();
        cbAnoCanceladas.setFont(new Font("Dialog", Font.PLAIN, 16));
        panel.add(cbAnoCanceladas);
        
        return panel;
    }

    /**
     * Cria painel de filtro específico para Distribuição de Consultas.
     * @return O painel de filtros configurado.
     */
    private JPanel criarPainelFiltroDistribuicaoConsultas() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                "Filtros Disponíveis",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 14),
                DARK_TEXT
            ),
            new EmptyBorder(5, 10, 5, 10)
        ));
        panel.setBackground(CLEAN_WHITE);
        
        JLabel lblMes = new JLabel("Mês:");
        lblMes.setFont(new Font("Dialog", Font.BOLD, 18));
        panel.add(lblMes);
        String[] meses = {"Todos", "Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"};
        cbMesDistribuicao = new JComboBox<>(meses);
        cbMesDistribuicao.setFont(new Font("Dialog", Font.PLAIN, 16));
        panel.add(cbMesDistribuicao);

        JLabel lblAno = new JLabel("Ano:");
        lblAno.setFont(new Font("Dialog", Font.BOLD, 18));
        panel.add(lblAno);
        cbAnoDistribuicao = new JComboBox<>();
        cbAnoDistribuicao.setFont(new Font("Dialog", Font.PLAIN, 16));
        panel.add(cbAnoDistribuicao);
        
        return panel;
    }
    
    private JPanel criarPainelFiltroPaciente() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                "Filtros Disponíveis",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 14),
                DARK_TEXT
            ),
            new EmptyBorder(5, 10, 5, 10)
        ));
        panel.setBackground(CLEAN_WHITE);
        
        JLabel lblPaciente = new JLabel("Nome ou CPF do Paciente:");
        lblPaciente.setFont(new Font("Dialog", Font.BOLD, 18));
        panel.add(lblPaciente);
        txtPacienteFiltro = new JTextField(25);
        txtPacienteFiltro.setFont(new Font("Dialog", Font.PLAIN, 16));
        panel.add(txtPacienteFiltro);
        
        return panel;
    }

    private JPanel criarPainelDeBotoes() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonPanel.setBackground(LIGHT_GRAY);
        buttonPanel.add(criarBotaoAcao("Gerar Relatório", SUCCESS_GREEN, e -> gerarRelatorio()));
        buttonPanel.add(criarBotaoAcao("Fechar", DARK_TEXT, e -> dispose()));
        return buttonPanel;
    }
    
    private void carregarDadosFiltros() {
        try {
            // Carregar dados para filtro com médico
            cbMedicoFiltro.addItem(null);
            medicoDAO.findAll().forEach(cbMedicoFiltro::addItem);
            cbMedicoFiltro.setRenderer(new DefaultListCellRenderer() {
                public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                    super.getListCellRendererComponent(l,v,i,s,f);
                    setText(v instanceof Medico ? ((Medico)v).getNome() : "Todos");
                    return this;
                }
            });

            // Carregar anos para todos os filtros de ano
            cbAnoFiltro.addItem(null);
            cbAnoCanceladas.addItem(null);
            cbAnoDistribuicao.addItem(null);
            
            int anoAtual = LocalDate.now().getYear();
            for (int i = anoAtual; i > anoAtual - 5; i--) {
                cbAnoFiltro.addItem(i);
                cbAnoCanceladas.addItem(i);
                cbAnoDistribuicao.addItem(i);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar filtros: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void gerarRelatorio() {
        ButtonModel selectedButton = grupoRelatorios.getSelection();
        if (selectedButton == null) {
            JOptionPane.showMessageDialog(this, "Selecione um tipo de relatório.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String tipoRelatorio = selectedButton.getActionCommand();
        Map<String, Object> resultado;

        try {
            int mes, ano;
            
            switch (tipoRelatorio) {
                case "Consultas por Médico":
                    mes = cbMesFiltro.getSelectedIndex();
                    ano = cbAnoFiltro.getSelectedItem() != null ? (Integer) cbAnoFiltro.getSelectedItem() : 0;
                    resultado = relatorioService.gerarRelatorioConsultasPorMedico((Medico) cbMedicoFiltro.getSelectedItem(), mes, ano);
                    break;
                case "Consultas Canceladas":
                    mes = cbMesCanceladas.getSelectedIndex();
                    ano = cbAnoCanceladas.getSelectedItem() != null ? (Integer) cbAnoCanceladas.getSelectedItem() : 0;
                    resultado = relatorioService.gerarRelatorioConsultasCanceladas(mes, ano);
                    break;
                case "Histórico do Paciente":
                    resultado = relatorioService.gerarRelatorioHistoricoPaciente(txtPacienteFiltro.getText());
                    break;
                case "Pacientes Inativos":
                    resultado = relatorioService.gerarRelatorioPacientesInativos();
                    break;
                case "Distribuição de Consultas":
                    mes = cbMesDistribuicao.getSelectedIndex();
                    ano = cbAnoDistribuicao.getSelectedItem() != null ? (Integer) cbAnoDistribuicao.getSelectedItem() : 0;
                    resultado = relatorioService.gerarRelatorioDistribuicaoConsultas(mes, ano);
                    break;
                default:
                    JOptionPane.showMessageDialog(this, "Relatório não implementado.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
            }

            table.setModel((DefaultTableModel) resultado.get("tableModel"));
            esconderColunasDeID();
            txtResumo.setText((String) resultado.get("summaryText"));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao gerar relatório: " + e.getMessage(), "Erro de Sistema", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void abrirTelaDeDetalhes() {
        int linha = table.getSelectedRow();
        int coluna = table.getSelectedColumn();
        if (linha == -1) return;

        String nomeColuna = table.getColumnName(coluna);
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        
        try {
            if (nomeColuna.equals("Paciente")) {
                String cpf = (String) model.getValueAt(linha, getColunaPorNome("CPF Paciente"));
                new TelaPacientes().setVisible(true);
            } else if (nomeColuna.equals("Médico")) {
                String crm = (String) model.getValueAt(linha, getColunaPorNome("CRM Médico"));
                new TelaMedicos().setVisible(true);
            } else {
                Long idConsulta = (Long) model.getValueAt(linha, getColunaPorNome("ID Consulta"));
                new TelaGerenciarConsultas(idConsulta).setVisible(true);
            }
        } catch (IllegalArgumentException ex) {
            // Ignora cliques em colunas sem ID
        }
    }

    private int getColunaPorNome(String nome) {
        for (int i=0; i < table.getColumnCount(); i++) {
            if (table.getColumnModel().getColumn(i).getHeaderValue().equals(nome)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Coluna não encontrada: " + nome);
    }

    private void esconderColunasDeID() {
        String[] colunasParaEsconder = {"ID Consulta", "CRM Médico", "CPF Paciente"};
        for (String nomeColuna : colunasParaEsconder) {
            try {
                int index = getColunaPorNome(nomeColuna);
                table.getColumnModel().getColumn(index).setMinWidth(0);
                table.getColumnModel().getColumn(index).setMaxWidth(0);
                table.getColumnModel().getColumn(index).setPreferredWidth(0);
            } catch (IllegalArgumentException e) {
                // A coluna não existe neste relatório
            }
        }
    }

    private void limparResultados() {
        table.setModel(new DefaultTableModel());
        txtResumo.setText("");
    }
    
    private RoundedButton criarBotaoAcao(String texto, Color cor, ActionListener acao) {
        RoundedButton botao = new RoundedButton(texto);
        botao.setFont(new Font("Segoe UI", Font.BOLD, 16));
        botao.setForeground(CLEAN_WHITE);
        botao.setBackground(cor);
        botao.setPreferredSize(new Dimension(200, 55));
        botao.addActionListener(acao);
        return botao;
    }
    
    private class RoundedButton extends JButton {
        public RoundedButton(String text) { super(text); setContentAreaFilled(false); setFocusPainted(false); setBorder(new EmptyBorder(5, 10, 5, 10)); }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (getModel().isPressed()) g2.setColor(getBackground().darker());
            else if (getModel().isRollover()) g2.setColor(getBackground().brighter());
            else g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
            super.paintComponent(g2);
            g2.dispose();
        }
    }
}