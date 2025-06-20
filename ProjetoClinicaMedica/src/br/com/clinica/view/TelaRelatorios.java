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
 * A interface é dinâmica, exibindo os filtros apropriados para o tipo de
 * relatório selecionado pelo usuário, e permite interatividade na tabela de resultados.
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

    // Componentes dos painéis de filtro
    private JComboBox<Medico> cbMedicoFiltro;
    private JComboBox<String> cbMesFiltro;
    private JComboBox<Integer> cbAnoFiltro;
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
        getContentPane().add(criarPainelTiposRelatorio(), BorderLayout.WEST);
        getContentPane().add(criarPainelCentral(), BorderLayout.CENTER);
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
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(CLEAN_WHITE);
        headerPanel.add(lblTitulo, BorderLayout.WEST);
        return headerPanel;
    }
    
    /**
     * Cria o painel lateral com JRadioButtons para seleção do tipo de relatório.
     * Este método utiliza Expressões Lambda para definir o comportamento de clique
     * de cada botão, que consiste em limpar os resultados e alternar o painel
     * de filtros visível através do CardLayout.
     * @return O painel de seleção de relatórios.
     */
    private JPanel criarPainelTiposRelatorio() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 5)); // Layout compacto
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Tipos de Relatório"),
            new EmptyBorder(5, 10, 5, 10)
        ));
        
        grupoRelatorios = new ButtonGroup();
        String[] nomesRelatorios = {
            "Consultas por Médico", "Consultas Canceladas", "Histórico do Paciente",
            "Pacientes Inativos (1 ano)", "Distribuição de Consultas"
        };

        for (String nome : nomesRelatorios) {
            JRadioButton radio = new JRadioButton(nome);
            radio.setActionCommand(nome);
            radio.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            
            radio.addActionListener(e -> {
                limparResultados();
                cardLayoutFiltros.show(painelContainerFiltros, e.getActionCommand());
            });
            
            grupoRelatorios.add(radio);
            panel.add(radio);
        }
        
        return panel;
    }

    private JPanel criarPainelCentral() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(criarPainelContainerFiltros(), BorderLayout.NORTH);
        panel.add(criarPainelResultados(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel criarPainelContainerFiltros() {
        cardLayoutFiltros = new CardLayout();
        painelContainerFiltros = new JPanel(cardLayoutFiltros);
        
        JPanel filtroVazio = new JPanel();
        filtroVazio.setBorder(BorderFactory.createTitledBorder("Filtros"));
        filtroVazio.add(new JLabel("Nenhum filtro adicional necessário para este relatório."));
        
        painelContainerFiltros.add(criarPainelFiltroCompleto(), "Consultas por Médico");
        painelContainerFiltros.add(criarPainelFiltroCompleto(), "Consultas Canceladas");
        painelContainerFiltros.add(criarPainelFiltroCompleto(), "Distribuição de Consultas");
        painelContainerFiltros.add(criarPainelFiltroPaciente(), "Histórico do Paciente");
        painelContainerFiltros.add(filtroVazio, "Pacientes Inativos (1 ano)");
        
        painelContainerFiltros.add(new JPanel(), "DEFAULT");
        cardLayoutFiltros.show(painelContainerFiltros, "DEFAULT");
        return painelContainerFiltros;
    }

    private JPanel criarPainelFiltroCompleto() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Filtros"));
        
        panel.add(new JLabel("Médico:"));
        cbMedicoFiltro = new JComboBox<>();
        panel.add(cbMedicoFiltro);
        
        panel.add(new JLabel("Mês:"));
        String[] meses = {"Todos", "Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"};
        cbMesFiltro = new JComboBox<>(meses);
        panel.add(cbMesFiltro);

        panel.add(new JLabel("Ano:"));
        cbAnoFiltro = new JComboBox<>();
        panel.add(cbAnoFiltro);
        
        return panel;
    }
    
    private JPanel criarPainelFiltroPaciente() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Filtros"));
        panel.add(new JLabel("Nome ou CPF do Paciente:"));
        txtPacienteFiltro = new JTextField(30);
        panel.add(txtPacienteFiltro);
        return panel;
    }
    
    private JPanel criarPainelResultados() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        table = new JTable();
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(28);
        
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    abrirTelaDeDetalhes();
                }
            }
        });
        
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        txtResumo = new JTextArea(5, 0);
        txtResumo.setFont(new Font("Monospaced", Font.PLAIN, 14));
        txtResumo.setEditable(false);
        panel.add(new JScrollPane(txtResumo), BorderLayout.SOUTH);
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
            cbMedicoFiltro.addItem(null);
            medicoDAO.findAll().forEach(cbMedicoFiltro::addItem);
            cbMedicoFiltro.setRenderer(new DefaultListCellRenderer() {
                public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                    super.getListCellRendererComponent(l,v,i,s,f);
                    setText(v instanceof Medico ? ((Medico)v).getNome() : "Todos");
                    return this;
                }
            });

            cbAnoFiltro.addItem(null); // Opção para "Todos"
            int anoAtual = LocalDate.now().getYear();
            for (int i = anoAtual; i > anoAtual - 5; i--) {
                cbAnoFiltro.addItem(i);
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
            int mes = cbMesFiltro.getSelectedIndex(); // 0 = Todos, 1 = Jan, ...
            int ano = cbAnoFiltro.getSelectedItem() != null ? (Integer) cbAnoFiltro.getSelectedItem() : 0;

            switch (tipoRelatorio) {
                case "Consultas por Médico":
                    resultado = relatorioService.gerarRelatorioConsultasPorMedico((Medico) cbMedicoFiltro.getSelectedItem(), mes, ano);
                    break;
                case "Consultas Canceladas":
                    resultado = relatorioService.gerarRelatorioConsultasCanceladas(mes, ano);
                    break;
                case "Histórico do Paciente":
                    resultado = relatorioService.gerarRelatorioHistoricoPaciente(txtPacienteFiltro.getText());
                    break;
                case "Pacientes Inativos (1 ano)":
                    resultado = relatorioService.gerarRelatorioPacientesInativos();
                    break;
                case "Distribuição de Consultas":
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
                new TelaPacientes().setVisible(true); // Funcionalidade de focar no paciente precisaria ser adicionada
            } else if (nomeColuna.equals("Médico")) {
                String crm = (String) model.getValueAt(linha, getColunaPorNome("CRM Médico"));
                new TelaMedicos().setVisible(true); // Funcionalidade de focar no médico precisaria ser adicionada
            } else {
                Long idConsulta = (Long) model.getValueAt(linha, getColunaPorNome("ID Consulta"));
                new TelaGerenciarConsultas(idConsulta).setVisible(true);
            }
        } catch (IllegalArgumentException ex) {
            // Ignora cliques em colunas sem ID (ex: relatórios de pacientes inativos)
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
                // A coluna não existe neste relatório, o que é normal.
            }
        }
    }

    private void limparResultados() {
        table.setModel(new DefaultTableModel());
        txtResumo.setText("");
    }
    
    private RoundedButton criarBotaoAcao(String texto, Color cor, ActionListener acao) {
        RoundedButton botao = new RoundedButton(texto);
        botao.setFont(new Font("Segoe UI", Font.BOLD, 14));
        botao.setForeground(CLEAN_WHITE);
        botao.setBackground(cor);
        botao.setPreferredSize(new Dimension(180, 50));
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