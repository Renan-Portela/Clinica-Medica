package br.com.clinica.view;

import br.com.clinica.dao.*;
import br.com.clinica.model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent; 
import java.awt.event.ActionListener; 
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.event.ChangeEvent; 
import javax.swing.event.ChangeListener; 
import java.util.*;
import java.util.List;
import java.time.LocalDateTime;
import java.text.SimpleDateFormat;
import java.sql.SQLException;
import java.time.ZoneId; // Necessário para LocalDateTime/Calendar
import java.util.Locale; // Necessário para SimpleDateFormat com Locale

/**
 * Tela de agenda em formato calendario visual semanal.
 * Esta interface permite a visualizacao e gestao de consultas medicas
 * de forma temporal em uma grade semanal, complementada por um mini-calendario e filtros.
 * Interage com ConsultaDAO, MedicoDAO, PacienteDAO.
 *
 * Estrutura da interface:
 * - Header: Contem o titulo principal e botoes de navegacao rapida (Fechar, Abrir Lista de Consultas, Novo Agendamento).
 * - Layout Principal: Dividido em dois paineis:
 * - Painel Lateral (Esquerda): Inclui um mini-calendario para navegacao mensal e painel de filtros.
 * - Painel Central (Direita): Exibe a grade semanal de horarios com as consultas.
 *
 * Funcionalidades do calendario:
 * - Visualizacao semanal em formato de grade (Horario x Dias da Semana).
 * - Mini-calendario interativo para selecao de semana e navegacao de mes/ano.
 * - Informacoes de consulta exibidas diretamente na celula com prefixo de status (Ex: [A] Paciente | Medico).
 * - Filtragem de consultas por medico ou paciente.
 *
 * Interacoes avancadas:
 * - Clique Esquerdo na celula da tabela: Abre a TelaAgenda (lista de consultas) com a consulta selecionada pre-carregada.
 * - Clique Direito na celula da tabela: Exibe um menu de contexto para alterar o status da consulta (Realizada, Cancelada, Reagendar, Nao Compareceu) ou ver detalhes.
 * - Selecao de dia no mini-calendario: Atualiza a visualizacao da semana correspondente.
 */
public class TelaAgendaCalendario extends JFrame {
    
    private static final long serialVersionUID = 1L;
    
    // Paleta de cores medica profissional
    private static final Color PRIMARY_BLUE = new Color(52, 144, 220);
    private static final Color MEDICAL_GREEN = new Color(76, 175, 80);
    private static final Color CLEAN_WHITE = new Color(255, 255, 255);
    private static final Color LIGHT_GRAY = new Color(248, 249, 250);
    private static final Color DARK_TEXT = new Color(52, 58, 64);
    private static final Color ACCENT_RED = new Color(220, 53, 69);
    private static final Color SUCCESS_GREEN = new Color(40, 167, 69);
    
    // Cores especificas por status de consulta (usadas no AgendamentoRenderer)
    private static final Color COR_AGENDADA = new Color(52, 144, 220);
    private static final Color COR_REALIZADA = new Color(76, 175, 80);
    private static final Color COR_CANCELADA = new Color(244, 67, 54);
    private static final Color COR_NAO_COMPARECEU = new Color(108, 117, 125); // Cinza para "Nao Compareceu"
    
    // Componentes principais da interface
    private JPanel contentPane;
    private JPanel headerPanel;
    private JPanel lateralPanel;
    private JPanel centralPanel;
    
    // Componentes do mini calendario
    private JPanel miniCalendarioPanel;
    private JPanel diasPanel;
    private JLabel lblMesAno;
    private JButton btnMesAnterior;
    private JButton btnProximoMes;
    private JSpinner spinnerData; 
    
    // Componentes de filtros
    private JPanel filtrosPanel;
    private JComboBox<String> cbTipoFiltro;
    private JComboBox<Object> cbFiltroItem;
    
    // Componentes da grade principal da agenda
    private JTable tabelaAgenda; 
    private DefaultTableModel modeloTabela;
    private JScrollPane scrollPaneTabela;
    private JLabel lblSemanaAtual;
    
    // Componentes de navegacao no header
    private JButton btnFecharTelaCalendario;
    private JButton btnNovoAgendamento;
    private JButton btnAbrirListaConsultas;
    
    // DAOs para comunicacao com o banco de dados
    private ConsultaDAO consultaDAO;
    private MedicoDAO medicoDAO; 
    private PacienteDAO pacienteDAO; 
    
    // Controle de estado do calendario e mapeamento de consultas
    private Calendar calendarioAtual;
    private Map<String, Consulta> mapaConsultas; // Mapeia posicao na tabela para objeto Consulta

    /**
     * Construtor da TelaAgendaCalendario.
     * Inicializa os DAOs para interacao com o banco de dados,
     * configura o calendario atual para a data do sistema,
     * e prepara o mapa para armazenar as consultas na grade visual.
     * Apos a inicializacao, a interface e construida e os dados iniciais sao carregados.
     *
     * Comunicacao com Outras Classes:
     * - ConsultaDAO: Para operacoes de busca e atualizacao de consultas.
     * - MedicoDAO: Para buscar informacoes de medicos (para filtros).
     * - PacienteDAO: Para buscar informacoes de pacientes (para filtros).
     */
    public TelaAgendaCalendario() {
        this.consultaDAO = new ConsultaDAO();
        this.medicoDAO = new MedicoDAO();
        this.pacienteDAO = new PacienteDAO();
        this.calendarioAtual = Calendar.getInstance();
        this.mapaConsultas = new HashMap<>();

        initialize(); 
        atualizarVisualizacao(); // Chamado apos toda a inicializacao da UI para carregar dados
    }
    
    /**
     * Metodo principal de inicializacao da interface da tela.
     * Cria e configura todos os paineis, componentes e listeners.
     */
    private void initialize() {
        // Configuracao da janela principal (JFrame)
        setTitle("Agenda Visual - Calendario Semanal | Sistema Clinica Medica");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(LIGHT_GRAY);
        setContentPane(contentPane);

        criarHeader(); 
        
        lateralPanel = new JPanel(new BorderLayout());
        lateralPanel.setBackground(CLEAN_WHITE);
        lateralPanel.setPreferredSize(new Dimension(250, 0)); // REDUZIDO: 300 -> 250
        lateralPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15) // REDUZIDO: 20 -> 15
        ));

        // Seletor de data (JSpinner) no topo do painel lateral
        JPanel seletorDataPanel = new JPanel(new BorderLayout());
        seletorDataPanel.setOpaque(false);
        seletorDataPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0)); // REDUZIDO: 15 -> 10
        JLabel lblSelecao = new JLabel("Selecao de Data");
        lblSelecao.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblSelecao.setForeground(DARK_TEXT);
        spinnerData = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinnerData, "dd/MM/yyyy");
        spinnerData.setEditor(editor);
        spinnerData.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        spinnerData.setPreferredSize(new Dimension(0, 35));
        spinnerData.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Date dataSelecionada = (Date) spinnerData.getValue();
                calendarioAtual.setTime(dataSelecionada);
                atualizarVisualizacao(); // Reativa a chamada para o metodo
            }
        });
        seletorDataPanel.add(lblSelecao, BorderLayout.NORTH);
        seletorDataPanel.add(Box.createVerticalStrut(5), BorderLayout.CENTER);
        seletorDataPanel.add(spinnerData, BorderLayout.SOUTH);
        lateralPanel.add(seletorDataPanel, BorderLayout.NORTH);

        // Mini-calendario (Navegacao Mensal) no centro do painel lateral
        miniCalendarioPanel = new JPanel(new BorderLayout());
        miniCalendarioPanel.setOpaque(false);
        miniCalendarioPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_BLUE, 1),
            "Navegacao Mensal",
            0, 0,
            new Font("Segoe UI", Font.BOLD, 14),
            PRIMARY_BLUE
        ));
        JPanel cabecalhoMini = new JPanel(new BorderLayout());
        cabecalhoMini.setBackground(LIGHT_GRAY);
        cabecalhoMini.setPreferredSize(new Dimension(0, 35));
        btnMesAnterior = new JButton("<-");
        btnMesAnterior.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnMesAnterior.setPreferredSize(new Dimension(30, 30));
        btnMesAnterior.setBackground(LIGHT_GRAY);
        btnMesAnterior.setBorder(null);
        btnMesAnterior.setFocusPainted(false);
        btnMesAnterior.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnMesAnterior.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                navegarMes(-1); // Reativa a chamada para o metodo
            }
        });
        btnProximoMes = new JButton("->");
        btnProximoMes.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnProximoMes.setPreferredSize(new Dimension(30, 30));
        btnProximoMes.setBackground(LIGHT_GRAY);
        btnProximoMes.setBorder(null);
        btnProximoMes.setFocusPainted(false);
        btnProximoMes.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnProximoMes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                navegarMes(1); // Reativa a chamada para o metodo
            }
        });
        lblMesAno = new JLabel("", SwingConstants.CENTER);
        lblMesAno.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblMesAno.setForeground(DARK_TEXT);
        cabecalhoMini.add(btnMesAnterior, BorderLayout.WEST);
        cabecalhoMini.add(lblMesAno, BorderLayout.CENTER);
        cabecalhoMini.add(btnProximoMes, BorderLayout.EAST);
        diasPanel = new JPanel(new GridLayout(7, 7, 0, 0)); // ORIGINAL: Layout 7x7 = 49 células
        diasPanel.setBackground(CLEAN_WHITE);
        diasPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        miniCalendarioPanel.add(cabecalhoMini, BorderLayout.NORTH);
        diasPanel.setPreferredSize(new Dimension(210, 150)); // BEM compacto
        diasPanel.setMinimumSize(new Dimension(210, 150));
        diasPanel.setMaximumSize(new Dimension(210, 150));
        JPanel containerCompacto = new JPanel(new BorderLayout());
        containerCompacto.setPreferredSize(new Dimension(210, 150)); // Altura fixa
        containerCompacto.setBackground(CLEAN_WHITE);
        containerCompacto.add(diasPanel, BorderLayout.NORTH); // Norte = não expande
        miniCalendarioPanel.add(containerCompacto, BorderLayout.CENTER);
        lateralPanel.add(miniCalendarioPanel, BorderLayout.CENTER);

        // Painel de Filtros no sul do painel lateral
        filtrosPanel = new JPanel(); 
        filtrosPanel.setLayout(new BoxLayout(filtrosPanel, BoxLayout.Y_AXIS));
        filtrosPanel.setOpaque(false);
        filtrosPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(23, 162, 184), 1),
                "Filtros de Visualizacao",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(23, 162, 184)
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        JLabel lblTipo = new JLabel("Filtrar por:");
        lblTipo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTipo.setForeground(DARK_TEXT);
        cbTipoFiltro = new JComboBox<>(new String[]{"Todos", "Por Medico", "Por Paciente"});
        cbTipoFiltro.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbTipoFiltro.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        cbTipoFiltro.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tipo = (String) cbTipoFiltro.getSelectedItem();
                cbFiltroItem.removeAllItems();
                try {
                    if ("Por Medico".equals(tipo)) {
                        cbFiltroItem.addItem("Selecione o medico...");
                        List<Medico> medicos = medicoDAO.findAll(); 
                        for (Medico medico : medicos) {
                            cbFiltroItem.addItem(medico);
                        }
                    } else if ("Por Paciente".equals(tipo)) {
                        cbFiltroItem.addItem("Selecione o paciente...");
                        List<Paciente> pacientes = pacienteDAO.findAll();
                        for (Paciente paciente : pacientes) {
                            cbFiltroItem.addItem(paciente);
                        }
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(TelaAgendaCalendario.this,
                        "Erro ao carregar filtros: " + ex.getMessage(),
                        "Erro de Sistema",
                        JOptionPane.ERROR_MESSAGE);
                }
                aplicarFiltro();
            }
        });
        JLabel lblItem = new JLabel("Selecionar:");
        lblItem.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblItem.setForeground(DARK_TEXT);
        cbFiltroItem = new JComboBox<>();
        cbFiltroItem.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbFiltroItem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        cbFiltroItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                aplicarFiltro();
            }
        });
        filtrosPanel.add(lblTipo);
        filtrosPanel.add(Box.createVerticalStrut(5));
        filtrosPanel.add(cbTipoFiltro);
        filtrosPanel.add(Box.createVerticalStrut(15));
        filtrosPanel.add(lblItem);
        filtrosPanel.add(Box.createVerticalStrut(5));
        filtrosPanel.add(cbFiltroItem);
        lateralPanel.add(filtrosPanel, BorderLayout.SOUTH);

        // Painel central (Agenda Principal)
        centralPanel = new JPanel(new BorderLayout());
        centralPanel.setBackground(CLEAN_WHITE);
        centralPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // REDUZIDO: 20 -> 15
        JPanel headerCentral = new JPanel(new BorderLayout());
        headerCentral.setOpaque(false);
        headerCentral.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0)); // REDUZIDO: 15 -> 10
        lblSemanaAtual = new JLabel("Agenda da Semana");
        lblSemanaAtual.setFont(new Font("Segoe UI", Font.BOLD, 18)); // REDUZIDO: 20 -> 18
        lblSemanaAtual.setForeground(DARK_TEXT);
        headerCentral.add(lblSemanaAtual, BorderLayout.WEST);

        // Legenda de cores
        JPanel legendaPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); // REDUZIDO: 15 -> 10
        legendaPanel.setOpaque(false);
        legendaPanel.add(criarIndicadorLegenda("Agendada", COR_AGENDADA));
        legendaPanel.add(criarIndicadorLegenda("Realizada", COR_REALIZADA));
        legendaPanel.add(criarIndicadorLegenda("Cancelada", COR_CANCELADA));
        legendaPanel.add(criarIndicadorLegenda("Nao Compareceu", COR_NAO_COMPARECEU));
        headerCentral.add(legendaPanel, BorderLayout.EAST); 

        centralPanel.add(headerCentral, BorderLayout.NORTH);
        
        tabelaAgenda = new JTable(); // Inicializa a tabela antes de passar para o JScrollPane
        scrollPaneTabela = new JScrollPane(tabelaAgenda); 
        scrollPaneTabela.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        scrollPaneTabela.setBackground(CLEAN_WHITE);
        centralPanel.add(scrollPaneTabela, BorderLayout.CENTER);

        // Finaliza a organizacao do layout
        contentPane.add(headerPanel, BorderLayout.NORTH);
        contentPane.add(lateralPanel, BorderLayout.WEST);
        contentPane.add(centralPanel, BorderLayout.CENTER);

        // Funcoes de inicializacao dos dados e interacoes
        criarTabelaAgendamento(); 
        configurarTabelaInteracoes(); 
        aplicarEstilosVisuais(); 
    }

    /**
     * Cria um indicador visual para uso em legendas.
     * @param texto O texto a ser exibido ao lado do indicador.
     * @param cor A cor correspondente do quadrado.
     * @return Um JPanel contendo um quadrado colorido e um JLabel com o texto.
     */
    private JPanel criarIndicadorLegenda(String texto, Color cor) {
        JPanel indicador = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        indicador.setOpaque(false);
        
        JPanel quadrado = new JPanel();
        quadrado.setBackground(cor);
        quadrado.setPreferredSize(new Dimension(12, 12));
        quadrado.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100), 1));
        
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12)); // REDUZIDO: 14 -> 12
        label.setForeground(new Color(108, 117, 125));
        
        indicador.add(quadrado);
        indicador.add(label);
        
        return indicador;
    }
    
    /**
     * Cria um botao padronizado para uso no cabecalho da tela.
     * Inclui estilos de fonte, cor, tamanho e um efeito de hover visual.
     * @param texto O texto a ser exibido no botao.
     * @param corTexto A cor do texto do botao.
     * @param corFundo A cor de fundo padrao do botao.
     * @param acao O ActionListener a ser executado quando o botao e clicado.
     * @return Um JButton configurado com o estilo definido.
     */
    private JButton criarBotaoHeader(String texto, Color corTexto, Color corFundo, ActionListener acao) {
        JButton botao = new JButton(texto);
        botao.setFont(new Font("Segoe UI", Font.BOLD, 12)); // REDUZIDO: 14 -> 12
        botao.setForeground(corTexto);
        botao.setBackground(corFundo);
        botao.setPreferredSize(new Dimension(140, 30)); // REDUZIDO: 160x35 -> 140x30
        botao.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8)); // REDUZIDO: 5,10 -> 4,8
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
     * Este metodo esta vazio porque os botoes de navegacao laterais foram consolidados no cabecalho.
     */
    private void criarBotoesLaterais() {
        // Este metodo esta vazio intencionalmente para nao adicionar componentes.
    }
    
    /**
     * Configura as interacoes do mouse com a tabela da agenda.
     * Permite cliques esquerdo para abrir detalhes da consulta na TelaAgenda
     * e cliques direito para exibir um menu de contexto para mudanca de status.
     *
     * Comunicacao com Outras Classes:
     * - TelaAgenda: Aberta com o ID da consulta.
     * - TelaAgendamento: Aberta para um novo agendamento a partir do horario clicado.
     */
    private void configurarTabelaInteracoes() { 
        tabelaAgenda.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int linha = tabelaAgenda.rowAtPoint(e.getPoint());
                int coluna = tabelaAgenda.columnAtPoint(e.getPoint());
                
                if (linha >= 0 && coluna >= 1) { // Garante que e uma celula de dia e nao a coluna de horario
                    String chave = linha + "," + coluna;
                    Consulta consulta = mapaConsultas.get(chave);
                    
                    if (consulta != null) {
                        if (SwingUtilities.isRightMouseButton(e)) {
                            mostrarMenuStatus(e, consulta);
                        } else if (SwingUtilities.isLeftMouseButton(e)) {
                            abrirTelaAgenda(); 
                        }
                    } else if (SwingUtilities.isLeftMouseButton(e)) {
                        // Se nao houver consulta, abrir tela de agendamento para o horario clicado
                        LocalDateTime dataHoraSelecionada = getDataHoraDaCelula(linha, coluna);
                        if (dataHoraSelecionada != null) {
                            abrirTelaAgendamento();
                        }
                    }
                }
            }
        });
    }
    
    /**
     * Aplica o filtro selecionado (por medico ou paciente) e atualiza a visualizacao da agenda.
     * Busca as consultas no banco de dados com base no filtro e recarrega a tabela.
     *
     * Comunicacao com Outras Classes:
     * - ConsultaDAO: Para buscar consultas por medico, paciente ou todas.
     * - MedicoDAO: Usado para buscar o medico.
     * - PacienteDAO: Usado para buscar o paciente.
     */
    private void aplicarFiltro() {
        String tipoFiltro = (String) cbTipoFiltro.getSelectedItem();
        Object itemSelecionado = cbFiltroItem.getSelectedItem();
        
        try {
            List<Consulta> todasConsultas = consultaDAO.findAll();
            List<Consulta> consultasFiltradas = new ArrayList<>();

            Calendar inicioSemanaCal = (Calendar) calendarioAtual.clone();
            inicioSemanaCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            inicioSemanaCal.set(Calendar.HOUR_OF_DAY, 0);
            inicioSemanaCal.set(Calendar.MINUTE, 0);
            inicioSemanaCal.set(Calendar.SECOND, 0);
            inicioSemanaCal.set(Calendar.MILLISECOND, 0);

            Calendar fimSemanaCal = (Calendar) inicioSemanaCal.clone();
            fimSemanaCal.add(Calendar.DAY_OF_YEAR, 6);
            fimSemanaCal.set(Calendar.HOUR_OF_DAY, 23);
            fimSemanaCal.set(Calendar.MINUTE, 59);
            fimSemanaCal.set(Calendar.SECOND, 59);
            fimSemanaCal.set(Calendar.MILLISECOND, 999);

            LocalDateTime dataInicioPeriodo = LocalDateTime.ofInstant(inicioSemanaCal.toInstant(), ZoneId.systemDefault());
            LocalDateTime dataFimPeriodo = LocalDateTime.ofInstant(fimSemanaCal.toInstant(), ZoneId.systemDefault());

            for (Consulta consulta : todasConsultas) {
                LocalDateTime dataConsulta = consulta.getDataHorario();

                if ((dataConsulta.isAfter(dataInicioPeriodo) || dataConsulta.isEqual(dataInicioPeriodo)) &&
                    (dataConsulta.isBefore(dataFimPeriodo) || dataConsulta.isEqual(dataFimPeriodo))) {
                    
                    boolean adiciona = false;
                    if ("Por Medico".equals(tipoFiltro) && itemSelecionado instanceof Medico) {
                        Medico medicoFiltro = (Medico) itemSelecionado;
                        if (consulta.getMedico() != null && consulta.getMedico().getCrm().equals(medicoFiltro.getCrm())) {
                            adiciona = true;
                        }
                    } else if ("Por Paciente".equals(tipoFiltro) && itemSelecionado instanceof Paciente) {
                        Paciente pacienteFiltro = (Paciente) itemSelecionado;
                        if (consulta.getPaciente() != null && consulta.getPaciente().getCpf().equals(pacienteFiltro.getCpf())) {
                            adiciona = true;
                        }
                    } else {
                        adiciona = true;
                    }

                    if (adiciona) {
                        consultasFiltradas.add(consulta);
                    }
                }
            }
            atualizarTabelaComConsultas(consultasFiltradas);
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(TelaAgendaCalendario.this,
                "Erro ao filtrar: " + ex.getMessage(),
                "Erro de Sistema", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Atualiza os cabecalhos das colunas da tabela da agenda com os dias da semana.
     * Calcula e formata os dias da semana a partir da data atual do calendario.
     * @param colunas Um array de Strings que representa os nomes das colunas da tabela.
     */
    private void atualizarCabecalhosSemana(String[] colunas) {
        Calendar inicioSemana = (Calendar) calendarioAtual.clone();
        inicioSemana.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); 
        
        SimpleDateFormat formatoDia = new SimpleDateFormat("E dd/MM", new Locale("pt", "BR")); // Definido Locale
        for (int i = 1; i <= 7; i++) {
            Calendar dia = (Calendar) inicioSemana.clone();
            dia.add(Calendar.DAY_OF_YEAR, i - 1);
            colunas[i] = formatoDia.format(dia.getTime());
        }
    }
    
    /**
     * Preenche a primeira coluna da tabela com os horarios dos slots de agendamento.
     * Os horarios sao gerados em intervalos de 30 minutos, de 8:00h as 18:30h.
     * @param dados Uma matriz de Strings que representa os dados da tabela.
     */
    private void preencherHorarios(String[][] dados) {
        for (int linha = 0; linha < 22; linha++) { // 8:00 (0) ate 19:00 (22)
            int horas = 8 + (linha / 2);
            int minutos = (linha % 2) * 30;
            dados[linha][0] = String.format("%02d:%02d", horas, minutos);
            
            for (int col = 1; col < 8; col++) {
                dados[linha][col] = "";
            }
        }
    }
    
    /**
     * Recria a estrutura do modelo da tabela de agendamento e atualiza seus cabecalhos.
     * Este metodo e chamado para garantir que a grade esteja pronta para ser preenchida com novas consultas.
     */
    private void criarTabelaAgendamento() {
        String[] colunas = new String[8];
        colunas[0] = "Horario";
        atualizarCabecalhosSemana(colunas);
        
        String[][] dados = new String[22][8]; // 22 linhas agora
        preencherHorarios(dados);
        
        modeloTabela = new DefaultTableModel(dados, colunas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabelaAgenda.setModel(modeloTabela); 
        tabelaAgenda.setFont(new Font("Segoe UI", Font.PLAIN, 13)); // REDUZIDO: 14 -> 13
        tabelaAgenda.setRowHeight(30); // REDUZIDO: 35 -> 30
        tabelaAgenda.setGridColor(new Color(220, 220, 220));
        tabelaAgenda.setSelectionBackground(new Color(52, 144, 220, 30)); 
        tabelaAgenda.setShowVerticalLines(true);
        tabelaAgenda.setShowHorizontalLines(true);
        
        tabelaAgenda.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14)); // REDUZIDO: 16 -> 14
        tabelaAgenda.getTableHeader().setBackground(LIGHT_GRAY);
        tabelaAgenda.getTableHeader().setForeground(DARK_TEXT);
        tabelaAgenda.getTableHeader().setPreferredSize(new Dimension(0, 35)); // REDUZIDO: 40 -> 35
        
        tabelaAgenda.getColumnModel().getColumn(0).setPreferredWidth(70); // REDUZIDO: 80 -> 70
        tabelaAgenda.getColumnModel().getColumn(0).setMaxWidth(70); // REDUZIDO: 80 -> 70
        
        tabelaAgenda.getColumnModel().getColumn(0).setCellRenderer(new HorarioRenderer());
        for (int i = 1; i < 8; i++) {
            tabelaAgenda.getColumnModel().getColumn(i).setCellRenderer(new AgendamentoRenderer());
        }
    }
    
    /**
     * Atualiza o label que exibe o range da semana atual e os cabecalhos de coluna da tabela.
     * Calcula a segunda-feira e o domingo da semana atual para exibir o periodo.
     */
    private void atualizarCabecalhosTabelaAgenda() { 
        Calendar inicioSemana = (Calendar) calendarioAtual.clone();
        inicioSemana.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        Calendar fimSemana = (Calendar) inicioSemana.clone();
        fimSemana.add(Calendar.DAY_OF_YEAR, 6);
        
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        lblSemanaAtual.setText("Semana de " + formato.format(inicioSemana.getTime()) + 
                              " a " + formato.format(fimSemana.getTime()));
        
        SimpleDateFormat formatoDia = new SimpleDateFormat("E dd/MM", new Locale("pt", "BR"));
        for (int i = 1; i <= 7; i++) {
            Calendar dia = (Calendar) inicioSemana.clone();
            dia.add(Calendar.DAY_OF_YEAR, i - 1);
            tabelaAgenda.getColumnModel().getColumn(i).setHeaderValue(formatoDia.format(dia.getTime()));
        }
        
        tabelaAgenda.getTableHeader().repaint();
    }
    
    /**
     * Carrega todas as consultas do banco de dados e as exibe na tabela da agenda.
     * Este metodo agora apenas chama aplicarFiltro(), que fara o trabalho de carregar e filtrar em memoria.
     *
     * Comunicacao com Outras Classes:
     * - ConsultaDAO: Para buscar todas as consultas.
     */
    private void carregarConsultasReais() {
        try {
            aplicarFiltro(); 
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Erro ao carregar consultas: " + e.getMessage(),
                "Erro de Sistema", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Atualiza tabela com lista especifica de consultas.
     * Limpa a tabela e preenche-a novamente com os dados das consultas fornecidas,
     * posicionando-as nas celulas corretas e atualizando o mapa interno de consultas.
     * @param consultas A List de Consulta a ser exibida na tabela.
     */
    private void atualizarTabelaComConsultas(List<Consulta> consultas) {
        for (int linha = 0; linha < modeloTabela.getRowCount(); linha++) {
            for (int col = 1; col < modeloTabela.getColumnCount(); col++) {
                modeloTabela.setValueAt("", linha, col);
            }
        }
        
        mapaConsultas.clear();
        
        for (Consulta consulta : consultas) {
            adicionarConsultaNaTabela(consulta);
        }
        
        tabelaAgenda.repaint();
    }
    
    /**
     * Adiciona uma unica consulta na posicao correta da tabela da agenda.
     * Calcula a linha e coluna com base na data e horario da consulta,
     * formata o texto da celula com o status e armazena a consulta no mapa.
     * Se ja houver texto na celula, adiciona a nova consulta em uma nova linha.
     * @param consulta A Consulta a ser adicionada na tabela.
     */
    private void adicionarConsultaNaTabela(Consulta consulta) {
        LocalDateTime dataHorario = consulta.getDataHorario();
        
        Calendar inicioSemana = (Calendar) calendarioAtual.clone();
        inicioSemana.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); 
        
        if (estaNoRangeSemanal(dataHorario, inicioSemana)) {
            int coluna = calcularColunaDia(dataHorario, inicioSemana);
            int linha = calcularLinhaHorario(dataHorario);
            
            if (linha >= 0 && linha < modeloTabela.getRowCount() && 
               coluna >= 1 && coluna < modeloTabela.getColumnCount()) {
                
                String valorAtual = (String) modeloTabela.getValueAt(linha, coluna);
                String sigla = obterSiglaStatus(consulta.getStatus());
                String novoTexto = String.format("[%s] %s | %s", 
                                             sigla, 
                                             consulta.getPaciente().getNome(), 
                                             consulta.getMedico().getNome());
                
                if (valorAtual != null && !valorAtual.trim().isEmpty()) {
                    modeloTabela.setValueAt(valorAtual + "\n" + novoTexto, linha, coluna);
                } else {
                    modeloTabela.setValueAt(novoTexto, linha, coluna);
                }

                String chave = linha + "," + coluna;
                mapaConsultas.put(chave, consulta);
            }
        }
    }
    
    /**
     * Obtem a sigla de status de uma consulta para exibicao na celula da tabela.
     * @param status O StatusConsulta da consulta.
     * @return Uma string com a sigla correspondente ao status.
     */
    private String obterSiglaStatus(Consulta.StatusConsulta status) {
        switch (status) {
            case AGENDADA:
                return "A";
            case REALIZADA:
                return "R";
            case CANCELADA:
                return "C";
            case NAO_COMPARECEU:
                return "N";
            default:
                return "?";
        }
    }
    
    /**
     * Verifica se a data e hora de uma consulta estao dentro do range da semana exibida na agenda.
     * @param dataHora A LocalDateTime da consulta a ser verificada.
     * @param inicioSemana O Calendar representando o inicio da semana (segunda-feira).
     * @return true se a consulta estiver dentro do range semanal, false caso contrario.
     */
    private boolean estaNoRangeSemanal(LocalDateTime dataHora, Calendar inicioSemana) {
        Calendar fimSemana = (Calendar) inicioSemana.clone();
        fimSemana.add(Calendar.DAY_OF_YEAR, 6); 
        
        Calendar dataConsulta = Calendar.getInstance();
        dataConsulta.set(dataHora.getYear(), dataHora.getMonthValue()-1, dataHora.getDayOfMonth());
        
        dataConsulta.set(Calendar.HOUR_OF_DAY, 0);
        dataConsulta.set(Calendar.MINUTE, 0);
        dataConsulta.set(Calendar.SECOND, 0);
        dataConsulta.set(Calendar.MILLISECOND, 0);

        inicioSemana.set(Calendar.HOUR_OF_DAY, 0);
        inicioSemana.set(Calendar.MINUTE, 0);
        inicioSemana.set(Calendar.SECOND, 0);
        inicioSemana.set(Calendar.MILLISECOND, 0);

        fimSemana.set(Calendar.HOUR_OF_DAY, 23);
        fimSemana.set(Calendar.MINUTE, 59);
        fimSemana.set(Calendar.SECOND, 59);
        fimSemana.set(Calendar.MILLISECOND, 999);
        
        return !dataConsulta.before(inicioSemana) && !dataConsulta.after(fimSemana);
    }
    
    /**
     * Calcula o indice da coluna da tabela (1-7) com base no dia da semana da consulta.
     * @param dataHora A LocalDateTime da consulta.
     * @param inicioSemana O Calendar representando o inicio da semana (segunda-feira).
     * @return O indice da coluna (1 para segunda-feira, 7 para domingo).
     */
    private int calcularColunaDia(LocalDateTime dataHora, Calendar inicioSemana) {
        Calendar dataConsulta = Calendar.getInstance();
        dataConsulta.set(dataHora.getYear(), dataHora.getMonthValue()-1, dataHora.getDayOfMonth());
        dataConsulta.set(Calendar.HOUR_OF_DAY, 0);
        dataConsulta.set(Calendar.MINUTE, 0);
        dataConsulta.set(Calendar.SECOND, 0);
        dataConsulta.set(Calendar.MILLISECOND, 0);

        Calendar inicioSemanaZerado = (Calendar) inicioSemana.clone();
        inicioSemanaZerado.set(Calendar.HOUR_OF_DAY, 0);
        inicioSemanaZerado.set(Calendar.MINUTE, 0);
        inicioSemanaZerado.set(Calendar.SECOND, 0);
        inicioSemanaZerado.set(Calendar.MILLISECOND, 0);

        long diferenca = dataConsulta.getTimeInMillis() - inicioSemanaZerado.getTimeInMillis();
        int dias = (int) (diferenca / (24 * 60 * 60 * 1000));
        
        return dias + 1;
    }
    
    /**
     * Calcula o indice da linha da tabela (0-21) com base no horario da consulta.
     * Os slots sao de 30 minutos, comecando as 8:00h (linha 0) ate as 19:00h (linha 22).
     * @param dataHora A LocalDateTime da consulta.
     * @return O indice da linha na tabela, ou -1 se o horario estiver fora do range.
     */
    private int calcularLinhaHorario(LocalDateTime dataHora) {
        int hora = dataHora.getHour();
        int minuto = dataHora.getMinute();
        
        if (hora < 8 || hora > 19 || (hora == 19 && minuto > 0)) return -1;
        
        int linha = (hora - 8) * 2;
        if (minuto >= 30) linha++;
        
        return linha;
    }

    /**
     * Recupera a data e hora correspondente a celula clicada na tabela.
     * Utilizado para pre-preencher a tela de agendamento ao clicar em um slot vazio.
     * @param linha O indice da linha clicada.
     * @param coluna O indice da coluna clicada.
     * @return Um objeto LocalDateTime com a data e hora da celula, ou null se invalido.
     */
    private LocalDateTime getDataHoraDaCelula(int linha, int coluna) {
        if (coluna < 1 || linha < 0 || linha >= modeloTabela.getRowCount()) {
            return null; 
        }

        String horarioStr = (String) modeloTabela.getValueAt(linha, 0);
        String[] partesHorario = horarioStr.split(":");
        int hora = Integer.parseInt(partesHorario[0]);
        int minuto = Integer.parseInt(partesHorario[1]);

        Calendar diaDaColuna = (Calendar) calendarioAtual.clone();
        diaDaColuna.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        diaDaColuna.add(Calendar.DAY_OF_YEAR, coluna - 1);

        return LocalDateTime.of(
            diaDaColuna.get(Calendar.YEAR),
            diaDaColuna.get(Calendar.MONTH) + 1, 
            diaDaColuna.get(Calendar.DAY_OF_MONTH),
            hora,
            minuto
        );
    }
    
    /**
     * Exibe um menu de contexto (popup) quando o usuario clica com o botao direito
     * em uma celula da tabela que contem uma consulta.
     * Permite ao usuario alterar o status da consulta ou ver seus detalhes.
     *
     * Comunicacao com Outras Classes:
     * - Consulta: Modifica o status e obtem descricoes.
     * - ConsultaDAO: Atualiza a consulta no banco de dados.
     * @param e O MouseEvent que disparou o popup.
     * @param consulta A Consulta selecionada na celula.
     */
    private void mostrarMenuStatus(MouseEvent e, Consulta consulta) {
        JPopupMenu popup = new JPopupMenu();
        
        if (consulta.getStatus() != Consulta.StatusConsulta.REALIZADA) {
            JMenuItem itemRealizada = new JMenuItem("Marcar como Realizada");
            itemRealizada.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ev) {
                    alterarStatusConsulta(consulta, Consulta.StatusConsulta.REALIZADA);
                }
            });
            popup.add(itemRealizada);
        }
        
        if (consulta.getStatus() != Consulta.StatusConsulta.CANCELADA) {
            JMenuItem itemCancelada = new JMenuItem("Cancelar Consulta");
            itemCancelada.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ev) {
                    alterarStatusConsulta(consulta, Consulta.StatusConsulta.CANCELADA);
                }
            });
            popup.add(itemCancelada);
        }
        
        if (consulta.getStatus() != Consulta.StatusConsulta.AGENDADA) {
            JMenuItem itemAgendada = new JMenuItem("Reagendar");
            itemAgendada.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ev) {
                    alterarStatusConsulta(consulta, Consulta.StatusConsulta.AGENDADA);
                }
            });
            popup.add(itemAgendada);
        }

        if (consulta.getStatus() != Consulta.StatusConsulta.NAO_COMPARECEU) {
            JMenuItem itemNaoCompareceu = new JMenuItem("Marcar como Nao Compareceu");
            itemNaoCompareceu.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ev) {
                    alterarStatusConsulta(consulta, Consulta.StatusConsulta.NAO_COMPARECEU);
                }
            });
            popup.add(itemNaoCompareceu);
        }
        
        popup.addSeparator();
        JMenuItem itemInfo = new JMenuItem("Ver Detalhes");
        itemInfo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ev) {
                mostrarDetalhesConsulta(consulta);
            }
        });
        popup.add(itemInfo);
        
        popup.show(tabelaAgenda, e.getX(), e.getY());
    }
    
    /**
     * Altera o status de uma consulta apos confirmacao do usuario.
     * Solicita observacoes adicionais se o novo status for "Realizada" ou "Nao Compareceu".
     * Atualiza a consulta no banco de dados e recarrega a visualizacao da agenda.
     *
     * Comunicacao com Outras Classes:
     * - Consulta: Modifica o status e observacoes.
     * - ConsultaDAO: Para atualizar a consulta no banco de dados.
     * @param consulta A Consulta cujo status sera alterado.
     * @param novoStatus O novo StatusConsulta para a consulta.
     */
    private void alterarStatusConsulta(Consulta consulta, Consulta.StatusConsulta novoStatus) {
        String statusTexto = novoStatus.getDescricao();
        
        int confirmacao = JOptionPane.showConfirmDialog(
            this,
            String.format("Confirma alterar status para '%s'?\n\nPaciente: %s\nMedico: %s\nData: %s", 
                statusTexto,
                consulta.getPaciente().getNome(),
                consulta.getMedico().getNome(),
                consulta.getDataHorarioFormatado()
            ),
            "Confirmar Alteracao",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (confirmacao == JOptionPane.YES_OPTION) {
            try {
                if (novoStatus == Consulta.StatusConsulta.REALIZADA || novoStatus == Consulta.StatusConsulta.NAO_COMPARECEU) {
                    String observacoes = JOptionPane.showInputDialog(
                        this,
                        "Observacoes da consulta:",
                        "Observacoes",
                        JOptionPane.QUESTION_MESSAGE
                    );
                    if (observacoes != null) {
                        consulta.setObservacoes(observacoes.trim());
                    } else {
                        return; 
                    }
                } else {
                    consulta.setObservacoes(null); 
                }
                
                consulta.setStatus(novoStatus);
                consultaDAO.update(consulta);
                
                JOptionPane.showMessageDialog(this, 
                    "Status alterado para: " + statusTexto, 
                    "Sucesso", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                atualizarVisualizacao(); 
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Erro ao alterar status: " + ex.getMessage(), 
                    "Erro de Sistema", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Exibe um dialogo com os detalhes completos de uma consulta selecionada.
     * @param consulta A Consulta cujos detalhes serao exibidos.
     */
    private void mostrarDetalhesConsulta(Consulta consulta) {
        String info = String.format(
            "Paciente: %s\nMedico: %s\nData: %s\nStatus: %s\nObservacoes: %s",
            consulta.getPaciente().getNome(),
            consulta.getMedico().getNome(),
            consulta.getDataHorarioFormatado(),
            consulta.getStatus().getDescricao(),
            consulta.getObservacoes() != null && !consulta.getObservacoes().isEmpty() ? consulta.getObservacoes() : "Nenhuma"
        );
        JOptionPane.showMessageDialog(this, info, "Detalhes da Consulta", JOptionPane.INFORMATION_MESSAGE);
    }
    
    // ========== METODOS DE NAVEGACAO ==========
    
    /**
     * Abre a TelaAgenda (visao de lista de consultas) sem uma consulta especifica selecionada.
     *
     * Comunicacao com Outras Classes:
     * - TelaAgenda: Abre uma nova instancia e a torna visivel.
     */
    private void abrirTelaAgenda() {
        TelaAgenda telaAgenda = new TelaAgenda();
        telaAgenda.setVisible(true);
        telaAgenda.toFront(); 
    }
    
    /**
     * Abre a TelaAgendamento para criar uma nova consulta.
     *
     * Comunicacao com Outras Classes:
     * - TelaAgendamento: Abre uma nova instancia e a torna visivel.
     */
    private void abrirTelaAgendamento() {
        TelaAgendamento telaAgendamento = new TelaAgendamento();
        telaAgendamento.setVisible(true);
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
            new TelaPrincipal().setVisible(true);
        }
    }
    
    // ========== METODOS PUBLICOS PARA COMPATIBILIDADE ==========
    
    /**
     * Torna a janela da TelaAgendaCalendario visivel.
     * E um metodo publico para ser chamado por outras telas ou pelo ponto de entrada.
     */
    public void mostrar() {
        setVisible(true);
    }
    
    /**
     * Traz a janela da TelaAgendaCalendario para a frente da aplicacao
     * e solicita o foco. Util quando a janela ja esta aberta mas precisa ser reativada.
     */
    public void trazerParaFrente() {
        toFront();
        requestFocus();
    }

    // ========== METODOS FALTANTES IMPLEMENTADOS ==========
    
    /**
     * Metodo principal de atualizacao da visualizacao da agenda.
     * Coordena a atualizacao do mini-calendario, cabecalhos da tabela e carregamento das consultas.
     * Este metodo e chamado sempre que ha mudanca de data no calendario ou aplicacao de filtros.
     */
    private void atualizarVisualizacao() {
        atualizarMiniCalendario();
        atualizarCabecalhosTabelaAgenda();
        carregarConsultasReais();
        spinnerData.setValue(calendarioAtual.getTime());
    }
    
    /**
     * Cria o painel de cabecalho da tela com titulo e botoes de navegacao.
     * O header contem o titulo principal e botoes para abrir outras telas do sistema
     * e para fechar a tela atual retornando ao menu principal.
     */
    private void criarHeader() {
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_BLUE);
        headerPanel.setPreferredSize(new Dimension(0, 70)); // REDUZIDO: 80 -> 70
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20)); // REDUZIDO: 15,25 -> 12,20
        
        // Titulo principal
        JPanel tituloPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tituloPanel.setOpaque(false);
        
        JLabel lblTitulo = new JLabel("AGENDA VISUAL DA CLINICA");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20)); // REDUZIDO: 24 -> 20
        lblTitulo.setForeground(CLEAN_WHITE);
        
        JLabel lblSubtitulo = new JLabel("Calendario Semanal de Consultas");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.ITALIC, 14)); // REDUZIDO: 16 -> 14
        lblSubtitulo.setForeground(new Color(220, 230, 240));
        
        tituloPanel.add(lblTitulo);
        tituloPanel.add(Box.createHorizontalStrut(15)); // REDUZIDO: 20 -> 15
        tituloPanel.add(lblSubtitulo);
        
        // Botoes de navegacao
        JPanel botoesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); // REDUZIDO: 15 -> 10
        botoesPanel.setOpaque(false);
        
        btnNovoAgendamento = criarBotaoHeader("Novo Agendamento", CLEAN_WHITE, SUCCESS_GREEN, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirTelaAgendamento();
            }
        });
        
        btnAbrirListaConsultas = criarBotaoHeader("Lista de Consultas", CLEAN_WHITE, MEDICAL_GREEN, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirTelaAgenda();
            }
        });
        
        btnFecharTelaCalendario = criarBotaoHeader("Fechar", CLEAN_WHITE, ACCENT_RED, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int opcao = JOptionPane.showConfirmDialog(
                    TelaAgendaCalendario.this,
                    "Deseja fechar a agenda e retornar ao menu principal?",
                    "Confirmar Fechamento",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );
                
                if (opcao == JOptionPane.YES_OPTION) {
                    dispose();
                    trazerTelaPrincipalParaFrente();
                }
            }
        });
        
        botoesPanel.add(btnNovoAgendamento);
        botoesPanel.add(btnAbrirListaConsultas);
        botoesPanel.add(btnFecharTelaCalendario);
        
        headerPanel.add(tituloPanel, BorderLayout.WEST);
        headerPanel.add(botoesPanel, BorderLayout.EAST);
    }
    
    /**
     * Navega entre meses no mini-calendario.
     * Adiciona ou subtrai meses da data atual do calendario e atualiza toda a visualizacao.
     * @param incremento O numero de meses a navegar (positivo para frente, negativo para tras).
     */
    private void navegarMes(int incremento) {
        calendarioAtual.add(Calendar.MONTH, incremento);
        atualizarVisualizacao();
    }
    
    /**
     * Aplica estilos visuais finais e configuracoes de aparencia na interface.
     * Este metodo e chamado ao final da inicializacao para garantir que todos os componentes
     * tenham a aparencia correta e sejam renderizados adequadamente.
     */
    private void aplicarEstilosVisuais() {
        // Aplicar anti-aliasing nos componentes de texto
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        // Garantir que o spinner tenha o valor inicial correto
        spinnerData.setValue(calendarioAtual.getTime());
        
        // Aplicar foco inicial no spinner para melhor UX
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                spinnerData.requestFocus();
            }
        });
        
        // Forcar repaint de todos os componentes
        repaint();
        revalidate();
    }
    
    /**
     * Atualiza o mini-calendario com os dias do mes atual usando calculo original corrigido.
     * Implementacao baseada no codigo original funcional do GitHub.
     * Gera os botoes para cada dia do mes, aplicando cores sutis e fonte Verdana.
     * Configura listeners para navegacao por clique nos dias.
     */
    private void atualizarMiniCalendario() {
        SimpleDateFormat formatoMesAno = new SimpleDateFormat("MMMM yyyy", new Locale("pt", "BR"));
        lblMesAno.setText(formatoMesAno.format(calendarioAtual.getTime()));
        
        diasPanel.removeAll();
        
        // Cabecalhos dos dias da semana - ESTILO ORIGINAL GITHUB
        String[] diasSemana = {"D", "S", "T", "Q", "Q", "S", "S"};
        for (String dia : diasSemana) {
            JLabel lblDia = new JLabel(dia, SwingConstants.CENTER);
            lblDia.setFont(new Font("Verdana", Font.BOLD, 9));
            lblDia.setForeground(new Color(108, 117, 125));
            lblDia.setOpaque(true);
            lblDia.setBackground(LIGHT_GRAY);
            diasPanel.add(lblDia);
        }
        
        // CALCULOS ORIGINAIS DO GITHUB - FUNCIONAIS
        Calendar temp = (Calendar) calendarioAtual.clone();
        temp.set(Calendar.DAY_OF_MONTH, 1);
        int primeiroDia = temp.get(Calendar.DAY_OF_WEEK) - 1;
        
        // Espacos vazios antes do primeiro dia
        for (int i = 0; i < primeiroDia; i++) {
            JLabel lblVazio = new JLabel();
            lblVazio.setOpaque(true);
            lblVazio.setBackground(CLEAN_WHITE);
            diasPanel.add(lblVazio);
        }
        
        // Dias do mes
        int diasNoMes = temp.getActualMaximum(Calendar.DAY_OF_MONTH);
        Calendar hoje = Calendar.getInstance();
        for (int dia = 1; dia <= diasNoMes; dia++) {
            JButton btnDia = criarBotaoDiaOriginal(dia, hoje);
            diasPanel.add(btnDia);
        }
        
        // CALCULO ORIGINAL GITHUB: 49 componentes total (7x7)
        int componentesAdicionados = 7 + primeiroDia + diasNoMes;
        int posicoesFaltando = 49 - componentesAdicionados;
        
        // Espacos vazios finais para completar 49 total
        for (int i = 0; i < posicoesFaltando; i++) {
            JLabel lblVazioFinal = new JLabel();
            lblVazioFinal.setOpaque(true);
            lblVazioFinal.setBackground(CLEAN_WHITE);
            diasPanel.add(lblVazioFinal);
        }
        
        diasPanel.revalidate();
        diasPanel.repaint();
    }
    
    /**
     * Cria um botao para um dia especifico no mini-calendario no estilo original limpo.
     * Aplica cores sutis para hoje, dia selecionado e dias normais.
     * Remove bordas e usa fonte Verdana para ficar idêntico ao layout original.
     * @param dia O numero do dia do mes.
     * @param hoje O Calendar representando a data atual do sistema.
     * @return Um JButton configurado para o dia especificado no estilo original.
     */
    private JButton criarBotaoDiaOriginal(int dia, Calendar hoje) {
        JButton btnDia = new JButton(String.valueOf(dia));
        btnDia.setFont(new Font("Verdana", Font.PLAIN, 9)); // FONTE ORIGINAL
        btnDia.setBorder(null); // SEM BORDAS - ESTILO CLEAN ORIGINAL
        btnDia.setFocusPainted(false);
        btnDia.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnDia.setPreferredSize(new Dimension(25, 18)); // Altura reduzida: 18px
        btnDia.setMinimumSize(new Dimension(25, 18));
        btnDia.setMaximumSize(new Dimension(25, 18));
        
        // Verificar se e hoje
        boolean eHoje = (dia == hoje.get(Calendar.DAY_OF_MONTH) && 
                        calendarioAtual.get(Calendar.MONTH) == hoje.get(Calendar.MONTH) &&
                        calendarioAtual.get(Calendar.YEAR) == hoje.get(Calendar.YEAR));
        
        // Verificar se e o dia selecionado
        boolean eSelecionado = (dia == calendarioAtual.get(Calendar.DAY_OF_MONTH));
        
        // CORES SUTIS COMO NO ORIGINAL
        if (eHoje) {
            btnDia.setBackground(new Color(76, 175, 80)); // Verde mais suave para hoje
            btnDia.setForeground(CLEAN_WHITE);
        } else if (eSelecionado) {
            btnDia.setBackground(PRIMARY_BLUE);
            btnDia.setForeground(CLEAN_WHITE);
        } else {
            btnDia.setBackground(CLEAN_WHITE);
            btnDia.setForeground(DARK_TEXT);
        }
        
        final int diaFinal = dia;
        btnDia.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calendarioAtual.set(Calendar.DAY_OF_MONTH, diaFinal);
                atualizarVisualizacao();
            }
        });
        
        return btnDia;
    }
    
    // ========== CLASSES INTERNAS PARA RENDERIZACAO DA TABELA ==========
    
    /**
     * DefaultTableCellRenderer personalizado para a coluna de horarios da tabela.
     * Garante um estilo visual consistente para a primeira coluna, com fundo claro,
     * texto escuro e centralizado.
     */
    private class HorarioRenderer extends DefaultTableCellRenderer {
        /**
         * Retorna o componente usado para renderizar a celula da tabela.
         * @param table A tabela na qual a celula esta sendo renderizada.
         * @param value O valor da celula a ser renderizada.
         * @param isSelected true se a celula estiver selecionada.
         * @param hasFocus true se a celula tiver foco.
         * @param row O indice da linha da celula.
         * @param column O indice da coluna da celula.
         * @return O componente renderizador da celula.
         */
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBackground(LIGHT_GRAY);
            setForeground(new Color(108, 117, 125));
            setFont(new Font("Segoe UI", Font.PLAIN, 12)); // REDUZIDO: 14 -> 12
            setHorizontalAlignment(SwingConstants.CENTER);
            setOpaque(true); 
            setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220))); 
            return c;
        }
    }
    
    /**
     * DefaultTableCellRenderer personalizado para as celulas de consulta na grade semanal.
     * Define o estilo visual das celulas, aplicando cores de fundo e texto com base no status da consulta,
     * e uma sobreposicao semitransparente azul para celulas selecionadas.
     */
    private class AgendamentoRenderer extends DefaultTableCellRenderer {
        /**
         * Retorna o componente usado para renderizar a celula da tabela.
         * Aplica estilos visuais diferentes para celulas com e sem consultas,
         * e uma sobreposicao para celulas selecionadas.
         * @param table A tabela na qual a celula esta sendo renderizada.
         * @param value O valor da celula a ser renderizada (o texto da consulta ou vazio).
         * @param isSelected true se a celula estiver selecionada.
         * @param hasFocus true se a celula tiver foco.
         * @param row O indice da linha da celula.
         * @param column O indice da coluna da celula.
         * @return O componente renderizador da celula.
         */
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            setHorizontalAlignment(SwingConstants.CENTER);
            setOpaque(true); 
            setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
            
            if (value != null && !value.toString().trim().isEmpty()) {
                // Buscar consulta para determinar cor por status
                String chave = row + "," + column;
                Consulta consulta = mapaConsultas.get(chave);
                
                if (consulta != null) {
                    // CORES POR STATUS
                    switch (consulta.getStatus()) {
                        case AGENDADA:
                            setBackground(COR_AGENDADA);
                            break;
                        case REALIZADA:
                            setBackground(COR_REALIZADA);
                            break;
                        case CANCELADA:
                            setBackground(COR_CANCELADA);
                            break;
                        case NAO_COMPARECEU:
                            setBackground(COR_NAO_COMPARECEU);
                            break;
                        default:
                            setBackground(CLEAN_WHITE); 
                    }
                    setForeground(CLEAN_WHITE); 
                    setFont(new Font("Segoe UI", Font.BOLD, 11)); // REDUZIDO: 12 -> 11
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    setBackground(CLEAN_WHITE); 
                    setForeground(DARK_TEXT);
                    setFont(new Font("Segoe UI", Font.PLAIN, 11)); // REDUZIDO: 12 -> 11
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            } else {
                setBackground(CLEAN_WHITE); 
                setForeground(DARK_TEXT);
                setFont(new Font("Segoe UI", Font.PLAIN, 12));
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }

            if (isSelected) {
                setBackground(new Color(PRIMARY_BLUE.getRed(), PRIMARY_BLUE.getGreen(), PRIMARY_BLUE.getBlue(), 120));
                setForeground(CLEAN_WHITE); 
            }
            
            return c;
        }
    }
}