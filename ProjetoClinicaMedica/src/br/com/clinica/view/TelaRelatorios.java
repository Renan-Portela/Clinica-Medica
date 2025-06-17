package br.com.clinica.view;

import br.com.clinica.dao.*;
import br.com.clinica.model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

public class TelaRelatorios extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private JComboBox<String> cbTipoRelatorio;
    private JComboBox<Medico> cbMedico;
    private JComboBox<String> cbMes;
    private JComboBox<Integer> cbAno;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextArea txtResumo;
    
    private ConsultaDAO consultaDAO;
    private MedicoDAO medicoDAO;
    private PacienteDAO pacienteDAO;
    
    private final String[] TIPOS_RELATORIO = {
        "Consultas por Medico",
        "Pacientes por Especialidade",
        "Consultas Canceladas",
        "Historico do Paciente",
        "Distribuicao de Consultas",
        "Pacientes sem Consulta (1 ano)"
    };
    
    private final String[] MESES = {
        "Todos", "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
        "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    };
    
    public TelaRelatorios() {
        this.consultaDAO = new ConsultaDAO();
        this.medicoDAO = new MedicoDAO();
        this.pacienteDAO = new PacienteDAO();
        
        initComponents();
        carregarFiltros();
    }
    
    private void initComponents() {
        setTitle("Relatorios Gerenciais");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        getContentPane().add(mainPanel);
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new FlowLayout());
        headerPanel.setBackground(new Color(240, 248, 255));
        
        // Tipo de Relatório
        JLabel lblTipo = new JLabel("Tipo de Relatorio:");
        lblTipo.setFont(new Font("Arial", Font.BOLD, 12));
        headerPanel.add(lblTipo);
        
        cbTipoRelatorio = new JComboBox<>(TIPOS_RELATORIO);
        headerPanel.add(cbTipoRelatorio);
        
        headerPanel.add(new JLabel(" | "));
        
        // Filtro Médico
        JLabel lblMedico = new JLabel("Médico:");
        lblMedico.setFont(new Font("Arial", Font.BOLD, 12));
        headerPanel.add(lblMedico);
        
        cbMedico = new JComboBox<>();
        headerPanel.add(cbMedico);
        
        // Filtro Mês
        JLabel lblMes = new JLabel("Mês:");
        lblMes.setFont(new Font("Arial", Font.BOLD, 12));
        headerPanel.add(lblMes);
        
        cbMes = new JComboBox<>(MESES);
        headerPanel.add(cbMes);
        
        // Filtro Ano
        JLabel lblAno = new JLabel("Ano:");
        lblAno.setFont(new Font("Arial", Font.BOLD, 12));
        headerPanel.add(lblAno);
        
        cbAno = new JComboBox<>();
        headerPanel.add(cbAno);
        
        headerPanel.add(new JLabel(" | "));
        
        // Botão Gerar
        JButton btnGerar = new JButton("Gerar Relatorio");
        btnGerar.setBackground(new Color(76, 175, 80));
        btnGerar.setForeground(Color.WHITE);
        btnGerar.addActionListener(e -> gerarRelatorio());
        headerPanel.add(btnGerar);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Tabela
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        table = new JTable(tableModel);
        table.setFont(new Font("Dialog", Font.PLAIN, 20));
        table.setRowHeight(25);
        JScrollPane scrollTable = new JScrollPane(table);
        scrollTable.setPreferredSize(new Dimension(0, 400));
        
        mainPanel.add(scrollTable, BorderLayout.CENTER);
        
        // Resumo
        JPanel resumoPanel = new JPanel();
        resumoPanel.setLayout(new BorderLayout());
        resumoPanel.setBorder(BorderFactory.createTitledBorder("Resumo"));
        resumoPanel.setPreferredSize(new Dimension(0, 150));
        
        txtResumo = new JTextArea();
        txtResumo.setEditable(false);
        txtResumo.setBackground(new Color(248, 248, 248));
        txtResumo.setFont(new Font("Arial", Font.PLAIN, 30));
        
        JScrollPane scrollResumo = new JScrollPane(txtResumo);
        resumoPanel.add(scrollResumo, BorderLayout.CENTER);
        
        mainPanel.add(resumoPanel, BorderLayout.SOUTH);
    }
    
    private void carregarFiltros() {
        try {
            // Carregar médicos
            cbMedico.removeAllItems();
            cbMedico.addItem(null);
            
            List<Medico> medicos = medicoDAO.findAll();
            for (Medico medico : medicos) {
                cbMedico.addItem(medico);
            }
            
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
            
            // Carregar anos
            cbAno.removeAllItems();
            cbAno.addItem(null);
            
            int anoAtual = LocalDateTime.now().getYear();
            for (int ano = anoAtual - 5; ano <= anoAtual + 2; ano++) {
                cbAno.addItem(ano);
            }
            
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
            JOptionPane.showMessageDialog(this, "Erro ao carregar filtros: " + e.getMessage());
        }
    }
    
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
    
    private void gerarRelatorio() {
        String tipo = (String) cbTipoRelatorio.getSelectedItem();
        
        try {
            switch (tipo) {
                case "Consultas por Medico":
                    gerarConsultasPorMedico();
                    break;
                case "Pacientes por Especialidade":
                    gerarPacientesPorEspecialidade();
                    break;
                case "Consultas Canceladas":
                    gerarConsultasCanceladas();
                    break;
                case "Historico do Paciente":
                    gerarHistoricoPaciente();
                    break;
                case "Distribuicao de Consultas":
                    gerarDistribuicaoConsultas();
                    break;
                case "Pacientes sem Consulta (1 ano)":
                    gerarPacientesSemConsulta();
                    break;
                default:
                    JOptionPane.showMessageDialog(this, "Relatorio nao implementado: " + tipo);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao gerar relatorio: " + e.getMessage());
        }
    }
    
    // MODIFICADO: Agora com coluna Data
    private void gerarConsultasPorMedico() throws Exception {
        List<Consulta> todasConsultas = consultaDAO.findAll();
        List<Consulta> consultas = filtrarConsultas(todasConsultas);
        
        // Configurar tabela COM DATA
        tableModel.setColumnIdentifiers(new String[]{"Data", "Medico", "Paciente", "Status"});
        tableModel.setRowCount(0);
        
        for (Consulta consulta : consultas) {
            tableModel.addRow(new Object[]{
                consulta.getDataHorarioFormatado(),
                consulta.getMedico().getNome(),
                consulta.getPaciente().getNome(),
                consulta.getStatus().getDescricao()
            });
        }
        
        // Resumo
        Map<String, Integer> contadores = new HashMap<>();
        for (Consulta consulta : consultas) {
            String medico = consulta.getMedico().getNome();
            contadores.put(medico, contadores.getOrDefault(medico, 0) + 1);
        }
        
        String resumo = String.format(
            "RELATORIO DE CONSULTAS POR MEDICO\n" +
            "Total de consultas: %d\n" +
            "Numero de medicos: %d\n" +
            "Media por medico: %.1f",
            consultas.size(), contadores.size(),
            consultas.size() > 0 ? (double) consultas.size() / contadores.size() : 0.0
        );
        
        txtResumo.setText(resumo);
    }
    
    // MODIFICADO: Agora com coluna Data
    private void gerarPacientesPorEspecialidade() throws Exception {
        List<Consulta> todasConsultas = consultaDAO.findAll();
        List<Consulta> consultas = filtrarConsultas(todasConsultas);
        
        // Configurar tabela COM DATA
        tableModel.setColumnIdentifiers(new String[]{"Data", "Especialidade", "Paciente", "Medico"});
        tableModel.setRowCount(0);
        
        for (Consulta consulta : consultas) {
            tableModel.addRow(new Object[]{
                consulta.getDataHorarioFormatado(),
                consulta.getMedico().getEspecialidade(),
                consulta.getPaciente().getNome(),
                consulta.getMedico().getNome()
            });
        }
        
        // Resumo
        Map<String, Set<String>> pacientesPorEsp = new HashMap<>();
        for (Consulta consulta : consultas) {
            String especialidade = consulta.getMedico().getEspecialidade();
            String paciente = consulta.getPaciente().getNome();
            pacientesPorEsp.computeIfAbsent(especialidade, k -> new HashSet<>()).add(paciente);
        }
        
        int totalPacientes = pacientesPorEsp.values().stream().mapToInt(Set::size).sum();
        
        String resumo = String.format(
            "PACIENTES POR ESPECIALIDADE\n" +
            "Total de consultas: %d\n" +
            "Pacientes unicos: %d\n" +
            "Especialidades: %d",
            consultas.size(), totalPacientes, pacientesPorEsp.size()
        );
        
        txtResumo.setText(resumo);
    }
    
    private void gerarConsultasCanceladas() throws Exception {
        List<Consulta> todasConsultas = consultaDAO.findAll();
        List<Consulta> consultas = filtrarConsultas(todasConsultas);
        
        List<Consulta> canceladas = new ArrayList<>();
        for (Consulta consulta : consultas) {
            if (consulta.getStatus() == Consulta.StatusConsulta.CANCELADA) {
                canceladas.add(consulta);
            }
        }
        
        tableModel.setColumnIdentifiers(new String[]{"Data", "Medico", "Paciente", "Observacoes"});
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
            "CONSULTAS CANCELADAS\n" +
            "Total canceladas: %d\n" +
            "Taxa de cancelamento: %.1f%%",
            canceladas.size(), taxaCancelamento
        );
        
        txtResumo.setText(resumo);
    }
    
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
        
        tableModel.setColumnIdentifiers(new String[]{"Data", "Medico", "Status", "Observacoes"});
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
            "HISTORICO DO PACIENTE: %s\n" +
            "Total de consultas: %d",
            nomePaciente, historico.size()
        );
        
        txtResumo.setText(resumo);
    }
    
    // MODIFICADO: Agora com coluna Data
    private void gerarDistribuicaoConsultas() throws Exception {
        List<Consulta> todasConsultas = consultaDAO.findAll();
        List<Consulta> consultas = filtrarConsultas(todasConsultas);
        
        // Configurar tabela COM DATA
        tableModel.setColumnIdentifiers(new String[]{"Data", "Dia da Semana", "Medico", "Paciente"});
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
        distribuicao.put("Terca", 0);
        distribuicao.put("Quarta", 0);
        distribuicao.put("Quinta", 0);
        distribuicao.put("Sexta", 0);
        distribuicao.put("Sabado", 0);
        distribuicao.put("Domingo", 0);
        
        for (Consulta consulta : consultas) {
            String dia = consulta.getDataHorario().getDayOfWeek().toString();
            String diaPortugues = converterDiaSemana(dia);
            distribuicao.put(diaPortugues, distribuicao.get(diaPortugues) + 1);
        }
        
        String resumo = String.format(
            "DISTRIBUICAO POR DIA DA SEMANA\n" +
            "Total de consultas: %d\n" +
            "Media por dia: %.1f",
            consultas.size(), consultas.size() / 7.0
        );
        
        txtResumo.setText(resumo);
    }
    
    private void gerarPacientesSemConsulta() throws Exception {
        List<Paciente> todosPacientes = pacienteDAO.findAll();
        List<Consulta> todasConsultas = consultaDAO.findAll();
        List<Consulta> consultas = filtrarConsultas(todasConsultas);
        
        Set<String> pacientesComConsulta = new HashSet<>();
        for (Consulta consulta : consultas) {
            pacientesComConsulta.add(consulta.getPaciente().getCpf());
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
            "PACIENTES SEM CONSULTA\n" +
            "Total sem consulta: %d\n" +
            "Total de pacientes: %d\n" +
            "Percentual inativo: %.1f%%",
            pacientesSemConsulta.size(), todosPacientes.size(),
            todosPacientes.size() > 0 ? (pacientesSemConsulta.size() * 100.0 / todosPacientes.size()) : 0.0
        );
        
        txtResumo.setText(resumo);
    }
    
    private String converterDiaSemana(String diaIngles) {
        return switch (diaIngles) {
            case "MONDAY" -> "Segunda";
            case "TUESDAY" -> "Terca";
            case "WEDNESDAY" -> "Quarta";
            case "THURSDAY" -> "Quinta";
            case "FRIDAY" -> "Sexta";
            case "SATURDAY" -> "Sabado";
            case "SUNDAY" -> "Domingo";
            default -> "Desconhecido";
        };
    }
}