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
 * e a alteração rápida do status das consultas.
 * Interage com as classes: ConsultaService, MedicoDAO, PacienteDAO, UITheme.
 */
public class TelaAgendaCalendario extends JFrame implements UITheme {
    
    private static final long serialVersionUID = 1L;
    
    private JTable tabelaAgenda; 
    private DefaultTableModel modeloTabela;
    private JLabel lblMesAno;
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
     */
    private void initialize() {
        configurarJanela();
        
        JPanel headerPanel = criarHeader();
        JPanel lateralPanel = criarPainelLateral();
        JPanel centralPanel = criarPainelCentral();

        getContentPane().add(headerPanel, BorderLayout.NORTH);
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
        botoesPanel.add(criarBotaoAcao("Novo Agendamento", SUCCESS_GREEN, e -> new TelaNovoAgendamento().setVisible(true)));
        botoesPanel.add(criarBotaoAcao("Gerenciar Consultas", MEDICAL_GREEN, e -> new TelaGerenciarConsultas().setVisible(true)));
        botoesPanel.add(criarBotaoAcao("Fechar", ACCENT_RED, e -> dispose()));
        
        headerPanel.add(lblTitulo, BorderLayout.WEST);
        headerPanel.add(botoesPanel, BorderLayout.EAST);
        return headerPanel;
    }
    
    /**
     * Cria o painel lateral com seletor de data, mini-calendário e filtros.
     * @return O painel lateral.
     */
    private JPanel criarPainelLateral() {
        JPanel lateralPanel = new JPanel(new BorderLayout(0, 20));
        lateralPanel.setBackground(CLEAN_WHITE);
        lateralPanel.setPreferredSize(new Dimension(280, 0));
        lateralPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(200, 200, 200)),
            new EmptyBorder(15, 15, 15, 15)
        ));

        lateralPanel.add(criarSeletorDeData(), BorderLayout.NORTH);
        lateralPanel.add(criarMiniCalendario(), BorderLayout.CENTER);
        lateralPanel.add(criarPainelFiltros(), BorderLayout.SOUTH);

        return lateralPanel;
    }
    
    /**
     * Cria o seletor de data (JSpinner) na parte superior do painel lateral.
     * @return O painel do seletor de data.
     */
    private JPanel criarSeletorDeData() {
        JPanel seletorDataPanel = new JPanel(new BorderLayout(0, 5));
        seletorDataPanel.setOpaque(false);
        
        JLabel lblSelecao = new JLabel("Seleção de Data");
        lblSelecao.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblSelecao.setForeground(DARK_TEXT);
        
        spinnerData = new JSpinner(new SpinnerDateModel());
        spinnerData.setEditor(new JSpinner.DateEditor(spinnerData, "dd/MM/yyyy"));
        spinnerData.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        spinnerData.addChangeListener(e -> {
            Date dataSelecionada = (Date) spinnerData.getValue();
            calendarioAtual.setTime(dataSelecionada);
            atualizarVisualizacao();
        });
        
        seletorDataPanel.add(lblSelecao, BorderLayout.NORTH);
        seletorDataPanel.add(spinnerData, BorderLayout.CENTER);
        return seletorDataPanel;
    }

    /**
     * Cria o componente visual do mini-calendário para navegação mensal.
     * @return O painel do mini-calendário.
     */
    private JPanel criarMiniCalendario() {
        JPanel miniCalendarioPanel = new JPanel(new BorderLayout());
        miniCalendarioPanel.setOpaque(false);
        miniCalendarioPanel.setBorder(BorderFactory.createTitledBorder("Navegação Mensal"));
        
        JPanel cabecalhoMini = new JPanel(new BorderLayout());
        cabecalhoMini.setBackground(LIGHT_GRAY);
        
        JButton btnMesAnterior = new JButton("<");
        btnMesAnterior.addActionListener(e -> navegarMes(-1));
        JButton btnProximoMes = new JButton(">");
        btnProximoMes.addActionListener(e -> navegarMes(1));
        
        lblMesAno = new JLabel("", SwingConstants.CENTER);
        lblMesAno.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        cabecalhoMini.add(btnMesAnterior, BorderLayout.WEST);
        cabecalhoMini.add(lblMesAno, BorderLayout.CENTER);
        cabecalhoMini.add(btnProximoMes, BorderLayout.EAST);
        
        diasPanel = new JPanel(new GridLayout(0, 7, 3, 3));
        diasPanel.setBackground(CLEAN_WHITE);
        
        miniCalendarioPanel.add(cabecalhoMini, BorderLayout.NORTH);
        miniCalendarioPanel.add(diasPanel, BorderLayout.CENTER);
        return miniCalendarioPanel;
    }
    
    /**
     * Cria o painel de filtros por médico ou paciente.
     * @return O painel de filtros.
     */
    private JPanel criarPainelFiltros() {
        JPanel filtrosPanel = new JPanel(); 
        filtrosPanel.setLayout(new BoxLayout(filtrosPanel, BoxLayout.Y_AXIS));
        filtrosPanel.setOpaque(false);
        filtrosPanel.setBorder(BorderFactory.createTitledBorder("Filtros de Visualização"));
        
        cbTipoFiltro = new JComboBox<>(new String[]{"Todos", "Por Médico", "Por Paciente"});
        cbFiltroItem = new JComboBox<>();
        
        cbTipoFiltro.addActionListener(e -> carregarItensFiltro());
        cbFiltroItem.addActionListener(e -> aplicarFiltro());
        
        filtrosPanel.add(new JLabel("Filtrar por:"));
        filtrosPanel.add(cbTipoFiltro);
        filtrosPanel.add(Box.createVerticalStrut(10));
        filtrosPanel.add(new JLabel("Selecionar:"));
        filtrosPanel.add(cbFiltroItem);
        
        return filtrosPanel;
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
     * Cria e configura a estrutura da tabela principal da agenda.
     */
    private void criarTabelaAgendamento() {
        String[] colunas = new String[8];
        colunas[0] = "Horário";
        
        modeloTabela = new DefaultTableModel(null, colunas) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        tabelaAgenda.setModel(modeloTabela); 
        tabelaAgenda.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabelaAgenda.setRowHeight(35);
        tabelaAgenda.setGridColor(new Color(220, 220, 220));
        
        tabelaAgenda.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabelaAgenda.getTableHeader().setBackground(LIGHT_GRAY);
        tabelaAgenda.getTableHeader().setForeground(DARK_TEXT);
        
        tabelaAgenda.getColumnModel().getColumn(0).setPreferredWidth(70);
        
        for (int i = 0; i < tabelaAgenda.getColumnCount(); i++) {
            if (i == 0) {
                tabelaAgenda.getColumnModel().getColumn(i).setCellRenderer(new HorarioRenderer());
            } else {
                tabelaAgenda.getColumnModel().getColumn(i).setCellRenderer(new AgendamentoRenderer());
            }
        }
    }
    
    /**
     * Adiciona os listeners de mouse à tabela para interações de clique,
     * incluindo a nova funcionalidade de abrir a consulta no gerenciador.
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
                
                if (consulta == null) return;

                if (SwingUtilities.isRightMouseButton(e)) {
                    mostrarMenuDeAcoes(e, consulta);
                } else if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                    // FUNCIONALIDADE ADICIONADA: Abre a consulta específica com duplo-clique
                    new TelaGerenciarConsultas(consulta.getId()).setVisible(true);
                }
            }
        });
    }

    /**
     * Exibe um menu de contexto com ações para a consulta selecionada.
     * @param e O evento de mouse.
     * @param consulta A consulta selecionada.
     */
    private void mostrarMenuDeAcoes(MouseEvent e, Consulta consulta) {
        JPopupMenu popup = new JPopupMenu();
        
        if (consulta.getStatus() == Consulta.StatusConsulta.AGENDADA) {
            JMenuItem itemRealizada = new JMenuItem("Marcar como Realizada");
            itemRealizada.addActionListener(ev -> alterarStatusConsulta(consulta, StatusAcao.REALIZADA));
            popup.add(itemRealizada);
            
            JMenuItem itemCancelada = new JMenuItem("Cancelar Consulta");
            itemCancelada.addActionListener(ev -> alterarStatusConsulta(consulta, StatusAcao.CANCELADA));
            popup.add(itemCancelada);
        }
        
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
     * Atualiza toda a visualização da agenda.
     */
    private void atualizarVisualizacao() {
        spinnerData.setValue(calendarioAtual.getTime());
        atualizarMiniCalendario();
        atualizarCabecalhosTabela();
        aplicarFiltro();
    }
    
    /**
     * Atualiza o mini-calendário com um layout flexível e botões quadrados.
     */
    private void atualizarMiniCalendario() {
        SimpleDateFormat formatoMesAno = new SimpleDateFormat("MMMM, yyyy", new Locale("pt", "BR"));
        lblMesAno.setText(formatoMesAno.format(calendarioAtual.getTime()));
        
        diasPanel.removeAll();
        
        String[] diasSemana = {"D", "S", "T", "Q", "Q", "S", "S"};
        for (String dia : diasSemana) {
            JLabel lblDia = new JLabel(dia, SwingConstants.CENTER);
            lblDia.setFont(new Font("Segoe UI", Font.BOLD, 12));
            diasPanel.add(lblDia);
        }
        
        Calendar temp = (Calendar) calendarioAtual.clone();
        temp.set(Calendar.DAY_OF_MONTH, 1);
        int primeiroDiaDaSemana = temp.get(Calendar.DAY_OF_WEEK);
        
        for (int i = 1; i < primeiroDiaDaSemana; i++) {
            diasPanel.add(new JLabel(""));
        }
        
        int diasNoMes = temp.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int dia = 1; dia <= diasNoMes; dia++) {
            diasPanel.add(criarBotaoDia(dia));
        }
        
        diasPanel.revalidate();
        diasPanel.repaint();
    }
    
    /**
     * Cria um botão quadrado para um dia específico no mini-calendário.
     * @param dia O número do dia.
     * @return Um JButton estilizado.
     */
    private JButton criarBotaoDia(int dia) {
        JButton btnDia = new JButton(String.valueOf(dia));
        btnDia.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnDia.setMargin(new Insets(2, 2, 2, 2));
        btnDia.setFocusable(false);
        btnDia.setBackground(CLEAN_WHITE);
        btnDia.setPreferredSize(new Dimension(35, 35));

        if (dia == calendarioAtual.get(Calendar.DAY_OF_MONTH)) {
            btnDia.setBackground(PRIMARY_BLUE);
            btnDia.setForeground(CLEAN_WHITE);
        }

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
                medicoDAO.findAll().forEach(cbFiltroItem::addItem);
            } else if ("Por Paciente".equals(tipo)) {
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
                todasConsultas = todasConsultas.stream()
                    .filter(c -> c.getMedico().getCrm().equals(((Medico)itemSelecionado).getCrm()))
                    .collect(Collectors.toList());
            } else if (itemSelecionado instanceof Paciente) {
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
     * @param consultas A lista de consultas a ser exibida.
     */
    private void popularTabela(List<Consulta> consultas) {
        String[] colunas = {"Horário", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom"};
        String[][] dados = new String[22][8];
        for (int i = 0; i < 22; i++) {
            dados[i][0] = String.format("%02d:%02d", 8 + (i / 2), (i % 2) * 30);
        }
        modeloTabela.setDataVector(dados, colunas);
        tabelaAgenda.getColumnModel().getColumn(0).setPreferredWidth(70);
        
        mapaConsultas.clear();
        
        Calendar inicioSemana = (Calendar) calendarioAtual.clone();
        inicioSemana.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        inicioSemana.set(Calendar.HOUR_OF_DAY, 0);

        Calendar fimSemana = (Calendar) inicioSemana.clone();
        fimSemana.add(Calendar.DAY_OF_YEAR, 7);
        
        consultas.stream()
            .filter(c -> {
                Date dataConsulta = Date.from(c.getDataHorario().atZone(ZoneId.systemDefault()).toInstant());
                return !dataConsulta.before(inicioSemana.getTime()) && dataConsulta.before(fimSemana.getTime());
            })
            .forEach(c -> {
                int linha = (c.getDataHorario().getHour() - 8) * 2 + (c.getDataHorario().getMinute() / 30);
                
                Calendar cal = Calendar.getInstance();
                cal.setTime(Date.from(c.getDataHorario().atZone(ZoneId.systemDefault()).toInstant()));
                int coluna = (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) ? 7 : cal.get(Calendar.DAY_OF_WEEK) - 1;

                if (linha >= 0 && linha < 22 && coluna >= 1 && coluna <= 7) {
                    String sigla = c.getStatus().getSigla();
                    modeloTabela.setValueAt(String.format("[%s] %s", sigla, c.getPaciente().getNome()), linha, coluna);
                    mapaConsultas.put(linha + "," + coluna, c);
                }
            });
    }
    
    private void navegarMes(int incremento) {
        calendarioAtual.add(Calendar.MONTH, incremento);
        atualizarVisualizacao();
    }
    
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
    
    private class HorarioRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBackground(LIGHT_GRAY);
            setForeground(DARK_TEXT);
            setHorizontalAlignment(SwingConstants.CENTER);
            return this;
        }
    }
    
    /**
     * Renderer customizado para as células de agendamento, que define a cor
     * de fundo com base no status da consulta.
     */
    private class AgendamentoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            // CORREÇÃO: Garante que a célula pode ser pintada
            if (c instanceof JComponent) {
                ((JComponent) c).setOpaque(true);
            }

            Consulta consulta = mapaConsultas.get(row + "," + column);
            if (consulta != null) {
                switch(consulta.getStatus()) {
                    case AGENDADA: c.setBackground(COR_AGENDADA); break;
                    case REALIZADA: c.setBackground(COR_REALIZADA); break;
                    case CANCELADA: c.setBackground(COR_CANCELADA); break;
                    case NAO_COMPARECEU: c.setBackground(COR_NAO_COMPARECEU); break;
                    default: c.setBackground(CLEAN_WHITE); break;
                }
                c.setForeground(CLEAN_WHITE);
                setToolTipText(consulta.getPaciente().getNome() + " com " + consulta.getMedico().getNome());
            } else {
                c.setBackground(CLEAN_WHITE);
                c.setForeground(DARK_TEXT);
                setToolTipText(null);
            }
            return c;
        }
    }

    /**
     * Classe interna para criar botões com cantos arredondados.
     */
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