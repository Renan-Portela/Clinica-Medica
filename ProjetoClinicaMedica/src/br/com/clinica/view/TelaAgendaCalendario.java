package br.com.clinica.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;

import java.awt.Font;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import java.awt.GridLayout;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

import java.awt.Component;
import javax.swing.table.DefaultTableCellRenderer;
import br.com.clinica.dao.*;
import br.com.clinica.model.*;
import java.sql.SQLException;

public class TelaAgendaCalendario {

    private JFrame frmAgendamento;
    private static final Color COR_PRINCIPAL = new Color(52, 144, 220);
    private static final Color COR_FUNDO = new Color(248, 249, 250);
    private static final Color COR_BRANCO = Color.WHITE;
    private static final Color COR_TEXTO = new Color(52, 58, 64);
    
    // NOVO: Cores por status
    private static final Color COR_AGENDADA = new Color(52, 144, 220);  // Azul
    private static final Color COR_REALIZADA = new Color(76, 175, 80);  // Verde
    private static final Color COR_CANCELADA = new Color(244, 67, 54);  // Vermelho
    
    private ConsultaDAO consultaDAO;
    private MedicoDAO medicoDAO;
    private PacienteDAO pacienteDAO;
    private JTable table;
    private Calendar calendarioAtual;
    private JLabel lblMesAno;
    private JButton btnMesAnterior;
    private JButton btnProximoMes;
    private JPanel panelDias;
    private JSpinner spinner;
    private DefaultTableModel modeloTabela;
    private JLabel lblSemanaAtual;
    private JComboBox<String> cbTipoFiltro;
    private JComboBox<Object> cbFiltroItem;
    private Map<String, Consulta> mapaConsultas;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    TelaAgendaCalendario window = new TelaAgendaCalendario();
                    window.frmAgendamento.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public TelaAgendaCalendario() {
        this.consultaDAO = new ConsultaDAO();
        this.medicoDAO = new MedicoDAO();  
        this.pacienteDAO = new PacienteDAO();
        this.mapaConsultas = new HashMap<>();
        calendarioAtual = Calendar.getInstance();
        initialize();
        criarTabelaAgendamento();
        atualizarMiniCalendario();
        carregarConsultasReais();
    }
    
    public void mostrar() {
        frmAgendamento.setVisible(true);
    }

    public void trazerParaFrente() {
        frmAgendamento.toFront();
        frmAgendamento.requestFocus();
    }
    
    public void setVisible(boolean visible) {
        frmAgendamento.setVisible(visible);
    }

    private void initialize() {
        frmAgendamento = new JFrame();
        frmAgendamento.setTitle("Agenda da Clínica");
        frmAgendamento.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frmAgendamento.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frmAgendamento.getContentPane().setLayout(null);
        
        JPanel panelBotoes = new JPanel();
        panelBotoes.setBackground(COR_PRINCIPAL);
        panelBotoes.setBounds(1153, 33, 227, 37);
        frmAgendamento.getContentPane().add(panelBotoes);
        FlowLayout fl_panelBotoes = (FlowLayout) panelBotoes.getLayout();
        fl_panelBotoes.setAlignment(FlowLayout.RIGHT);
        
        JButton btnNovaConsulta = new JButton("Agendamentos");
        btnNovaConsulta.addActionListener(e -> {
            new TelaAgenda().setVisible(true);
        });
        panelBotoes.add(btnNovaConsulta);

        JButton btnMedicos = new JButton("Médicos");
        btnMedicos.addActionListener(e -> new TelaMedicos().setVisible(true));
        panelBotoes.add(btnMedicos);

        JButton btnPacientes = new JButton("Pacientes");
        btnPacientes.addActionListener(e -> new TelaPacientes().setVisible(true));
        panelBotoes.add(btnPacientes);
        
        JButton btnAgenda = new JButton("Agenda");
        btnAgenda.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Já está na agenda
            }
        });
        panelBotoes.add(btnAgenda);

        JButton btnNovoAgendamentoCabecalho = new JButton("+ Novo");
        btnNovoAgendamentoCabecalho.setBackground(Color.WHITE);
        btnNovoAgendamentoCabecalho.setForeground(COR_PRINCIPAL);
        btnNovoAgendamentoCabecalho.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TelaAgendamento telaAgendar = new TelaAgendamento();
                telaAgendar.setVisible(true);
            }
        });
        panelBotoes.add(btnNovoAgendamentoCabecalho);
        
        JPanel panelCabecalho = new JPanel();
        FlowLayout fl_panelCabecalho = (FlowLayout) panelCabecalho.getLayout();
        fl_panelCabecalho.setHgap(25);
        fl_panelCabecalho.setVgap(25);
        fl_panelCabecalho.setAlignment(FlowLayout.LEFT);
        panelCabecalho.setBackground(COR_PRINCIPAL);
        panelCabecalho.setBounds(0, 0, 1400, 70);
        frmAgendamento.getContentPane().add(panelCabecalho);
        
        JLabel lblTitulo = new JLabel("AGENDA DA CLÍNICA");
        lblTitulo.setFont(new Font("Verdana", Font.BOLD, 24));
        lblTitulo.setHorizontalAlignment(SwingConstants.LEFT);
        lblTitulo.setForeground(Color.WHITE);
        panelCabecalho.add(lblTitulo);
        
        JPanel panelCalendario = new JPanel();
        panelCalendario.setLayout(null);
        panelCalendario.setBounds(0, 70, 250, 750);
        frmAgendamento.getContentPane().add(panelCalendario);
        
        JLabel lblSelecaoDeData = new JLabel("Seleção de Data");
        lblSelecaoDeData.setBounds(10, 10, 200, 25);
        panelCalendario.add(lblSelecaoDeData);
        
        spinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "dd/MM/yyyy");
        spinner.setEditor(editor);
        spinner.setBounds(10, 40, 200, 30);
        panelCalendario.add(spinner);
        
        JButton btnNovoAgendamento = new JButton("+ Novo Agendamento");
        btnNovoAgendamento.setBounds(10, 350, 210, 35);
        btnNovoAgendamento.setBackground(COR_PRINCIPAL);
        btnNovoAgendamento.setForeground(Color.WHITE);
        btnNovoAgendamento.setFont(new Font("Verdana", Font.BOLD, 12));
        btnNovoAgendamento.setFocusPainted(false);
        btnNovoAgendamento.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnNovoAgendamento.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TelaAgendamento telaAgendar = new TelaAgendamento();
                telaAgendar.setVisible(true);
            }
        });
        panelCalendario.add(btnNovoAgendamento);
        
        // Filtros
        JLabel lblFiltro = new JLabel("Filtrar por:");
        lblFiltro.setBounds(10, 480, 100, 25);
        lblFiltro.setFont(new Font("Verdana", Font.BOLD, 12));
        panelCalendario.add(lblFiltro);

        cbTipoFiltro = new JComboBox<>(new String[]{"Todos", "Por Médico", "Por Paciente"});
        cbTipoFiltro.setBounds(10, 505, 100, 25);
        panelCalendario.add(cbTipoFiltro);

        cbFiltroItem = new JComboBox<>();
        cbFiltroItem.setBounds(115, 505, 105, 25);
        panelCalendario.add(cbFiltroItem);
        
        configurarFiltros();
        
        JPanel panelMiniCalendario = new JPanel();
        panelMiniCalendario.setBounds(10, 80, 210, 260);
        panelMiniCalendario.setLayout(new BorderLayout());
        panelMiniCalendario.setBackground(Color.WHITE);
        panelMiniCalendario.setBorder(BorderFactory.createLineBorder(new Color(222, 226, 230)));
        panelCalendario.add(panelMiniCalendario);
        
        JPanel cabecalhoMini = new JPanel(new BorderLayout());
        cabecalhoMini.setBackground(COR_FUNDO);
        cabecalhoMini.setPreferredSize(new Dimension(0, 30));

        btnMesAnterior = new JButton("<-");
        btnMesAnterior.setPreferredSize(new Dimension(25, 25));
        btnMesAnterior.setBackground(COR_FUNDO);
        btnMesAnterior.setBorder(null);
        btnMesAnterior.setFocusPainted(false);
        btnMesAnterior.setFont(new Font("Verdana", Font.PLAIN, 10));
        btnMesAnterior.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                calendarioAtual.add(Calendar.MONTH, -1);
                spinner.setValue(calendarioAtual.getTime());
                atualizarMiniCalendario();
                criarTabelaAgendamento();
                carregarConsultasReais();
            }
        });

        btnProximoMes = new JButton("->");
        btnProximoMes.setPreferredSize(new Dimension(25, 25));
        btnProximoMes.setBackground(COR_FUNDO);
        btnProximoMes.setBorder(null);
        btnProximoMes.setFocusPainted(false);
        btnProximoMes.setFont(new Font("Verdana", Font.PLAIN, 10));
        btnProximoMes.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                calendarioAtual.add(Calendar.MONTH, 1);
                spinner.setValue(calendarioAtual.getTime());
                atualizarMiniCalendario();
                criarTabelaAgendamento();
                carregarConsultasReais();
            }
        });

        lblMesAno = new JLabel("", SwingConstants.CENTER);
        lblMesAno.setFont(new Font("Verdana", Font.BOLD, 11));
        lblMesAno.setForeground(COR_TEXTO);

        cabecalhoMini.add(btnMesAnterior, BorderLayout.WEST);
        cabecalhoMini.add(lblMesAno, BorderLayout.CENTER);
        cabecalhoMini.add(btnProximoMes, BorderLayout.EAST);

        panelMiniCalendario.add(cabecalhoMini, BorderLayout.NORTH);
        
        panelDias = new JPanel(new GridLayout(7, 7, 1, 1));
        panelDias.setBackground(Color.WHITE);
        panelMiniCalendario.add(panelDias, BorderLayout.CENTER);
        
        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(null);
        panelPrincipal.setBackground(COR_FUNDO);
        panelPrincipal.setBounds(250, 70, 1150, 730);
        frmAgendamento.getContentPane().add(panelPrincipal);
        
        lblSemanaAtual = new JLabel("Agenda da Semana");
        lblSemanaAtual.setBounds(20, 10, 500, 30);
        lblSemanaAtual.setFont(new Font("Verdana", Font.BOLD, 18));
        lblSemanaAtual.setForeground(COR_TEXTO);
        panelPrincipal.add(lblSemanaAtual);
        
        // NOVO: Legenda de cores
        JPanel legendaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        legendaPanel.setBounds(600, 10, 500, 30);
        legendaPanel.setBackground(COR_FUNDO);
        
        JLabel lblLegenda = new JLabel("🔵 Agendada  🟢 Realizada  🔴 Cancelada");
        lblLegenda.setFont(new Font("Verdana", Font.PLAIN, 12));
        legendaPanel.add(lblLegenda);
        panelPrincipal.add(legendaPanel);
        
        table = new JTable();
        table.setEnabled(true);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(20, 50, 1100, 650);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(222, 226, 230)));
        panelPrincipal.add(scrollPane);
        
        frmAgendamento.setLocationRelativeTo(null);
        
        spinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                Date dataSelecionada = (Date) spinner.getValue();
                calendarioAtual.setTime(dataSelecionada);
                atualizarMiniCalendario();
                criarTabelaAgendamento();
                carregarConsultasReais();
            }
        });
    }
    
    private void configurarFiltros() {
        cbTipoFiltro.addActionListener(e -> {
            String tipo = (String) cbTipoFiltro.getSelectedItem();
            cbFiltroItem.removeAllItems();
            
            try {
                if("Por Médico".equals(tipo)) {
                    cbFiltroItem.addItem("Selecione o médico...");
                    List<Medico> medicos = medicoDAO.findAll();
                    for(Medico medico : medicos) {
                        cbFiltroItem.addItem(medico);
                    }
                } else if("Por Paciente".equals(tipo)) {
                    cbFiltroItem.addItem("Selecione o paciente...");
                    List<Paciente> pacientes = pacienteDAO.findAll();
                    for(Paciente paciente : pacientes) {
                        cbFiltroItem.addItem(paciente);
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frmAgendamento, "Erro ao carregar filtros: " + ex.getMessage());
            }
        });

        cbFiltroItem.addActionListener(e -> aplicarFiltro());
    }
    
    private void aplicarFiltro() {
        String tipoFiltro = (String) cbTipoFiltro.getSelectedItem();
        Object itemSelecionado = cbFiltroItem.getSelectedItem();
        
        try {
            List<Consulta> consultas;
            
            if("Por Médico".equals(tipoFiltro) && itemSelecionado instanceof Medico) {
                Medico medico = (Medico) itemSelecionado;
                consultas = consultaDAO.findByMedico(medico.getCrm());
            } 
            else if("Por Paciente".equals(tipoFiltro) && itemSelecionado instanceof Paciente) {
                Paciente paciente = (Paciente) itemSelecionado;
                consultas = consultaDAO.findByPaciente(paciente.getCpf());
            } 
            else {
                consultas = consultaDAO.findAll();
            }
            
            atualizarTabelaComConsultas(consultas);
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frmAgendamento, "Erro ao filtrar: " + ex.getMessage());
        }
    }
    
    private void atualizarMiniCalendario() {
        SimpleDateFormat formatoMesAno = new SimpleDateFormat("MMMM yyyy");
        lblMesAno.setText(formatoMesAno.format(calendarioAtual.getTime()));
        
        panelDias.removeAll();
        
        String[] diasSemana = {"D", "S", "T", "Q", "Q", "S", "S"};
        for (String dia : diasSemana) {
            JLabel lblDia = new JLabel(dia, SwingConstants.CENTER);
            lblDia.setFont(new Font("Verdana", Font.BOLD, 9));
            lblDia.setForeground(new Color(108, 117, 125));
            lblDia.setOpaque(true);
            lblDia.setBackground(COR_FUNDO);
            panelDias.add(lblDia);
        }
        
        Calendar temp = (Calendar) calendarioAtual.clone();
        temp.set(Calendar.DAY_OF_MONTH, 1);
        int primeiroDia = temp.get(Calendar.DAY_OF_WEEK) - 1;
        
        for (int i = 0; i < primeiroDia; i++) {
            JLabel lblVazio = new JLabel();
            lblVazio.setOpaque(true);
            lblVazio.setBackground(Color.WHITE);
            panelDias.add(lblVazio);
        }
        
        int diasNoMes = temp.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int dia = 1; dia <= diasNoMes; dia++) {
            JButton btnDia = criarBotaoDia(dia);
            panelDias.add(btnDia);
        }
        
        int componentesAdicionados = 7 + primeiroDia + diasNoMes;
        int posicoesFaltando = 49 - componentesAdicionados;
        
        for (int i = 0; i < posicoesFaltando; i++) {
            JLabel lblVazioFinal = new JLabel();
            lblVazioFinal.setOpaque(true);
            lblVazioFinal.setBackground(Color.WHITE);
            panelDias.add(lblVazioFinal);
        }
        
        panelDias.revalidate();
        panelDias.repaint();
        carregarConsultasReais();
    }
    
    private JButton criarBotaoDia(int dia) {
        JButton btnDia = new JButton(String.valueOf(dia));
        btnDia.setFont(new Font("Verdana", Font.PLAIN, 9));
        btnDia.setBorder(null);
        btnDia.setFocusPainted(false);
        btnDia.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        aplicarCoresDia(btnDia, dia);
        
        btnDia.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                calendarioAtual.set(Calendar.DAY_OF_MONTH, dia);
                spinner.setValue(calendarioAtual.getTime());
                atualizarMiniCalendario();
                criarTabelaAgendamento();
                carregarConsultasReais();
            }
        });
        
        return btnDia;
    }
    
    private void aplicarCoresDia(JButton btnDia, int dia) {
        Calendar hoje = Calendar.getInstance();
        int diaHoje = hoje.get(Calendar.DAY_OF_MONTH);
        int mesHoje = hoje.get(Calendar.MONTH);
        int anoHoje = hoje.get(Calendar.YEAR);
        
        int diaSelecionado = calendarioAtual.get(Calendar.DAY_OF_MONTH);
        int mesSelecionado = calendarioAtual.get(Calendar.MONTH);
        int anoSelecionado = calendarioAtual.get(Calendar.YEAR);
        
        if (dia == diaHoje && mesSelecionado == mesHoje && anoSelecionado == anoHoje) {
            btnDia.setBackground(new Color(76, 175, 80));
            btnDia.setForeground(Color.WHITE);
        } else if (dia == diaSelecionado) {
            btnDia.setBackground(COR_PRINCIPAL);
            btnDia.setForeground(Color.WHITE);
        } else {
            btnDia.setBackground(Color.WHITE);
            btnDia.setForeground(COR_TEXTO);
        }
    }
    
    public void addWindowListener(WindowAdapter windowAdapter) {
        // Método para compatibilidade
    }
    
    private void criarTabelaAgendamento() {
        Calendar inicioSemana = (Calendar) calendarioAtual.clone();
        inicioSemana.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        
        String[] colunas = new String[8];
        colunas[0] = "Horário";
        
        SimpleDateFormat formatoDia = new SimpleDateFormat("E dd/MM");
        for (int i = 1; i <= 7; i++) {
            Calendar dia = (Calendar) inicioSemana.clone();
            dia.add(Calendar.DAY_OF_WEEK, i - 1);
            colunas[i] = formatoDia.format(dia.getTime());
        }
        
        String[][] dados = new String[21][8];
        
        for (int linha = 0; linha < 21; linha++) {
            int horas = 8 + (linha / 2);
            int minutos = (linha % 2) * 30;
            dados[linha][0] = String.format("%02d:%02d", horas, minutos);
            
            for (int col = 1; col < 8; col++) {
                dados[linha][col] = "";
            }
        }
        
        modeloTabela = new DefaultTableModel(dados, colunas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        table.setModel(modeloTabela);
        configurarAparenciaTabela();
        atualizarTabelaAgendamento();
        carregarConsultasReais();
    }
    
    // MODIFICADO: Agora com clique direito e clique esquerdo
    private void configurarAparenciaTabela() {
        table.setRowHeight(35);
        table.setGridColor(new Color(222, 226, 230));
        table.setSelectionBackground(new Color(232, 244, 253));
        table.setFont(new Font("Verdana", Font.PLAIN, 12));
        
        // NOVO: MouseListener com clique direito E esquerdo
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int linha = table.rowAtPoint(e.getPoint());
                int coluna = table.columnAtPoint(e.getPoint());
                
                if (linha >= 0 && coluna >= 1) {
                    String chave = linha + "," + coluna;
                    Consulta consulta = mapaConsultas.get(chave);
                    
                    if (consulta != null) {
                        if (SwingUtilities.isRightMouseButton(e)) {
                            // CLIQUE DIREITO: Menu popup para mudar status
                            mostrarMenuStatus(e, consulta);
                        } else if (SwingUtilities.isLeftMouseButton(e)) {
                            // CLIQUE ESQUERDO: Abre TelaAgenda
                            TelaAgenda telaAgenda = new TelaAgenda();
                            telaAgenda.setVisible(true);
                            telaAgenda.toFront();
                        }
                    }
                }
            }
        });
        
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(0).setMaxWidth(80);
        table.getColumnModel().getColumn(0).setCellRenderer(new HorarioRenderer());
        
        for (int i = 1; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(new AgendamentoRenderer());
        }
        
        table.getTableHeader().setBackground(COR_FUNDO);
        table.getTableHeader().setForeground(COR_TEXTO);
        table.getTableHeader().setFont(new Font("Verdana", Font.BOLD, 12));
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
    }
    
    // NOVO: Menu popup para mudar status da consulta
    private void mostrarMenuStatus(MouseEvent e, Consulta consulta) {
        JPopupMenu popup = new JPopupMenu();
        
        // Menu items baseados no status atual
        if (consulta.getStatus() != Consulta.StatusConsulta.REALIZADA) {
            JMenuItem itemRealizada = new JMenuItem("✅ Marcar como Realizada");
            itemRealizada.addActionListener(ev -> alterarStatusConsulta(consulta, Consulta.StatusConsulta.REALIZADA));
            popup.add(itemRealizada);
        }
        
        if (consulta.getStatus() != Consulta.StatusConsulta.CANCELADA) {
            JMenuItem itemCancelada = new JMenuItem("❌ Cancelar Consulta");
            itemCancelada.addActionListener(ev -> alterarStatusConsulta(consulta, Consulta.StatusConsulta.CANCELADA));
            popup.add(itemCancelada);
        }
        
        if (consulta.getStatus() != Consulta.StatusConsulta.AGENDADA) {
            JMenuItem itemAgendada = new JMenuItem("🔄 Reagendar");
            itemAgendada.addActionListener(ev -> alterarStatusConsulta(consulta, Consulta.StatusConsulta.AGENDADA));
            popup.add(itemAgendada);
        }
        
        popup.addSeparator();
        JMenuItem itemInfo = new JMenuItem("ℹ️ Ver Detalhes");
        itemInfo.addActionListener(ev -> {
            String info = String.format(
                "Paciente: %s\nMédico: %s\nData: %s\nStatus: %s\nObservações: %s",
                consulta.getPaciente().getNome(),
                consulta.getMedico().getNome(),
                consulta.getDataHorarioFormatado(),
                consulta.getStatus().getDescricao(),
                consulta.getObservacoes() != null ? consulta.getObservacoes() : "Nenhuma"
            );
            JOptionPane.showMessageDialog(frmAgendamento, info, "Detalhes da Consulta", JOptionPane.INFORMATION_MESSAGE);
        });
        popup.add(itemInfo);
        
        popup.show(table, e.getX(), e.getY());
    }
    
    // NOVO: Método para alterar status da consulta
    private void alterarStatusConsulta(Consulta consulta, Consulta.StatusConsulta novoStatus) {
        String statusTexto = novoStatus.getDescricao();
        
        int confirmacao = JOptionPane.showConfirmDialog(
            frmAgendamento,
            String.format("Confirma alterar status para '%s'?\n\nPaciente: %s\nMédico: %s\nData: %s", 
                statusTexto,
                consulta.getPaciente().getNome(),
                consulta.getMedico().getNome(),
                consulta.getDataHorarioFormatado()
            ),
            "Confirmar Alteração",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (confirmacao == JOptionPane.YES_OPTION) {
            try {
                // Se for marcar como realizada, perguntar por observações
                if (novoStatus == Consulta.StatusConsulta.REALIZADA) {
                    String observacoes = JOptionPane.showInputDialog(
                        frmAgendamento,
                        "Observações da consulta realizada:",
                        "Observações",
                        JOptionPane.QUESTION_MESSAGE
                    );
                    if (observacoes != null) {
                        consulta.setObservacoes(observacoes.trim());
                    }
                }
                
                consulta.setStatus(novoStatus);
                consultaDAO.update(consulta);
                
                JOptionPane.showMessageDialog(frmAgendamento, 
                    "Status alterado para: " + statusTexto, 
                    "Sucesso", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Recarregar a agenda para mostrar nova cor
                carregarConsultasReais();
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frmAgendamento, 
                    "Erro ao alterar status: " + ex.getMessage(), 
                    "Erro", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void atualizarTabelaAgendamento() {
        Calendar inicioSemana = (Calendar) calendarioAtual.clone();
        inicioSemana.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        Calendar fimSemana = (Calendar) inicioSemana.clone();
        fimSemana.add(Calendar.DAY_OF_WEEK, 6);
        
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        lblSemanaAtual.setText("Semana de " + formato.format(inicioSemana.getTime()) + 
                              " a " + formato.format(fimSemana.getTime()));
        
        SimpleDateFormat formatoDia = new SimpleDateFormat("E dd/MM");
        for (int i = 1; i <= 7; i++) {
            Calendar dia = (Calendar) inicioSemana.clone();
            dia.add(Calendar.DAY_OF_WEEK, i - 1);
            table.getColumnModel().getColumn(i).setHeaderValue(formatoDia.format(dia.getTime()));
        }
        
        table.getTableHeader().repaint();
    }
    
    private class HorarioRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBackground(COR_FUNDO);
            setForeground(new Color(108, 117, 125));
            setFont(new Font("Verdana", Font.PLAIN, 11));
            setHorizontalAlignment(SwingConstants.CENTER);
            return c;
        }
    }

    // MODIFICADO: Renderer com cores por status
    private class AgendamentoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value != null && !value.toString().trim().isEmpty()) {
                // Buscar consulta para determinar cor por status
                String chave = row + "," + column;
                Consulta consulta = mapaConsultas.get(chave);
                
                if (consulta != null) {
                    // CORES POR STATUS
                    switch (consulta.getStatus()) {
                        case AGENDADA:
                            setBackground(COR_AGENDADA);  // Azul
                            break;
                        case REALIZADA:
                            setBackground(COR_REALIZADA); // Verde
                            break;
                        case CANCELADA:
                            setBackground(COR_CANCELADA); // Vermelho
                            break;
                        default:
                            setBackground(COR_AGENDADA);
                    }
                } else {
                    setBackground(COR_AGENDADA); // Padrão azul
                }
                
                setForeground(Color.WHITE);
                setFont(new Font("Verdana", Font.BOLD, 11));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else {
                setBackground(Color.WHITE);
                setForeground(COR_TEXTO);
                setFont(new Font("Verdana", Font.PLAIN, 11));
            }
            
            setHorizontalAlignment(SwingConstants.CENTER);
            return c;
        }
    }
    
    private void carregarConsultasReais() {
        try {
            List<Consulta> consultas = consultaDAO.findAll();
            atualizarTabelaComConsultas(consultas);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frmAgendamento, "Erro ao carregar consultas: " + e.getMessage());
        }
    }

    private void atualizarTabelaComConsultas(List<Consulta> consultas) {
        // Limpar tabela
        for(int linha = 0; linha < modeloTabela.getRowCount(); linha++) {
            for(int col = 1; col < modeloTabela.getColumnCount(); col++) {
                modeloTabela.setValueAt("", linha, col);
            }
        }
        
        // Limpar mapa de consultas
        mapaConsultas.clear();
        
        for(Consulta consulta : consultas) {
            adicionarConsultaNaTabela(consulta);
        }
    }

    private void adicionarConsultaNaTabela(Consulta consulta) {
        LocalDateTime dataHora = consulta.getDataHorario();
        
        Calendar inicioSemana = (Calendar) calendarioAtual.clone();
        inicioSemana.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        
        if(estaNoRangeSemanal(dataHora, inicioSemana)) {
            int coluna = calcularColunaDia(dataHora, inicioSemana);
            int linha = calcularLinhaHorario(dataHora);
            
            if(linha >= 0 && linha < modeloTabela.getRowCount() && 
               coluna >= 1 && coluna < modeloTabela.getColumnCount()) {
                
                String texto = consulta.getPaciente().getNome() + " | " + 
                              consulta.getMedico().getNome();
                modeloTabela.setValueAt(texto, linha, coluna);
                
                // Armazenar consulta no mapa para clique
                String chave = linha + "," + coluna;
                mapaConsultas.put(chave, consulta);
            }
        }
    }

    private boolean estaNoRangeSemanal(LocalDateTime dataHora, Calendar inicioSemana) {
        Calendar fimSemana = (Calendar) inicioSemana.clone();
        fimSemana.add(Calendar.DAY_OF_WEEK, 6);
        
        Calendar dataConsulta = Calendar.getInstance();
        dataConsulta.set(dataHora.getYear(), dataHora.getMonthValue()-1, dataHora.getDayOfMonth());
        
        return !dataConsulta.before(inicioSemana) && !dataConsulta.after(fimSemana);
    }

    private int calcularColunaDia(LocalDateTime dataHora, Calendar inicioSemana) {
        Calendar dataConsulta = Calendar.getInstance();
        dataConsulta.set(dataHora.getYear(), dataHora.getMonthValue()-1, dataHora.getDayOfMonth());
        
        long diferenca = dataConsulta.getTimeInMillis() - inicioSemana.getTimeInMillis();
        int dias = (int) (diferenca / (24 * 60 * 60 * 1000));
        
        return dias + 1;
    }

    private int calcularLinhaHorario(LocalDateTime dataHora) {
        int hora = dataHora.getHour();
        int minuto = dataHora.getMinute();
        
        if(hora < 8 || hora > 18) return -1;
        
        int linha = (hora - 8) * 2;
        if(minuto >= 30) linha++;
        
        return linha;
    }
}