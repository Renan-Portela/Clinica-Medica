package br.com.clinica.view;

import br.com.clinica.dao.*;
import br.com.clinica.model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class TelaRelatorios extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private JComboBox<String> cbTipoRelatorio;
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
    
    public TelaRelatorios() {
        this.consultaDAO = new ConsultaDAO();
        this.medicoDAO = new MedicoDAO();
        this.pacienteDAO = new PacienteDAO();
        
        initComponents();
    }
    
    private void initComponents() {
        setTitle("Relatorios Gerenciais");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Tela cheia
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        getContentPane().add(mainPanel);
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new FlowLayout());
        headerPanel.setBackground(new Color(240, 248, 255));
        
        JLabel lblTipo = new JLabel("Tipo de Relatorio:");
        lblTipo.setFont(new Font("Arial", Font.BOLD, 12));
        headerPanel.add(lblTipo);
        
        cbTipoRelatorio = new JComboBox<>(TIPOS_RELATORIO);
        headerPanel.add(cbTipoRelatorio);
        
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
                return false; // Nao editavel
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
    
    private void gerarConsultasPorMedico() throws Exception {
        List<Consulta> consultas = consultaDAO.findAll();
        Map<String, Integer> contadores = new HashMap<>();
        
        for (Consulta consulta : consultas) {
            String medico = consulta.getMedico().getNome();
            contadores.put(medico, contadores.getOrDefault(medico, 0) + 1);
        }
        
        // Configurar tabela
        tableModel.setColumnIdentifiers(new String[]{"Medico", "Total Consultas", "Percentual"});
        tableModel.setRowCount(0);
        
        int totalGeral = consultas.size();
        
        for (Map.Entry<String, Integer> entry : contadores.entrySet()) {
            String medico = entry.getKey();
            Integer total = entry.getValue();
            Double percentual = totalGeral > 0 ? (total * 100.0 / totalGeral) : 0.0;
            
            tableModel.addRow(new Object[]{
                medico,
                total,
                String.format("%.1f%%", percentual)
            });
        }
        
        // Resumo
        String resumo = String.format(
            "RELATORIO DE CONSULTAS POR MEDICO\n" +
            "Total de consultas: %d\n" +
            "Numero de medicos: %d\n" +
            "Media por medico: %.1f",
            totalGeral, contadores.size(),
            totalGeral > 0 ? (double) totalGeral / contadores.size() : 0.0
        );
        
        txtResumo.setText(resumo);
    }
    
    private void gerarPacientesPorEspecialidade() throws Exception {
        List<Consulta> consultas = consultaDAO.findAll();
        Map<String, Set<String>> pacientesPorEsp = new HashMap<>();
        
        for (Consulta consulta : consultas) {
            String especialidade = consulta.getMedico().getEspecialidade();
            String paciente = consulta.getPaciente().getNome();
            
            pacientesPorEsp.computeIfAbsent(especialidade, k -> new HashSet<>()).add(paciente);
        }
        
        // Configurar tabela
        tableModel.setColumnIdentifiers(new String[]{"Especialidade", "Pacientes Unicos"});
        tableModel.setRowCount(0);
        
        int totalPacientes = 0;
        for (Map.Entry<String, Set<String>> entry : pacientesPorEsp.entrySet()) {
            totalPacientes += entry.getValue().size();
            tableModel.addRow(new Object[]{
                entry.getKey(),
                entry.getValue().size()
            });
        }
        
        String resumo = String.format(
            "PACIENTES POR ESPECIALIDADE\n" +
            "Total de pacientes unicos: %d\n" +
            "Numero de especialidades: %d",
            totalPacientes, pacientesPorEsp.size()
        );
        
        txtResumo.setText(resumo);
    }
    
    private void gerarConsultasCanceladas() throws Exception {
        List<Consulta> consultas = consultaDAO.findAll();
        List<Consulta> canceladas = new ArrayList<>();
        
        for (Consulta consulta : consultas) {
            if (consulta.getStatus() == Consulta.StatusConsulta.CANCELADA) {
                canceladas.add(consulta);
            }
        }
        
        // Configurar tabela
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
        
        List<Consulta> consultas = consultaDAO.findAll();
        List<Consulta> historico = new ArrayList<>();
        
        for (Consulta consulta : consultas) {
            if (consulta.getPaciente().getNome().toLowerCase().contains(nomePaciente.toLowerCase())) {
                historico.add(consulta);
            }
        }
        
        // Configurar tabela
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
    
    private void gerarDistribuicaoConsultas() throws Exception {
        List<Consulta> consultas = consultaDAO.findAll();
        Map<String, Integer> distribuicao = new LinkedHashMap<>();
        
        // Inicializar dias
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
        
        // Configurar tabela
        tableModel.setColumnIdentifiers(new String[]{"Dia da Semana", "Quantidade", "Percentual"});
        tableModel.setRowCount(0);
        
        int total = consultas.size();
        
        for (Map.Entry<String, Integer> entry : distribuicao.entrySet()) {
            Double percentual = total > 0 ? (entry.getValue() * 100.0 / total) : 0.0;
            tableModel.addRow(new Object[]{
                entry.getKey(),
                entry.getValue(),
                String.format("%.1f%%", percentual)
            });
        }
        
        String resumo = String.format(
            "DISTRIBUICAO POR DIA DA SEMANA\n" +
            "Total de consultas: %d\n" +
            "Media por dia: %.1f",
            total, total / 7.0
        );
        
        txtResumo.setText(resumo);
    }
    
    private void gerarPacientesSemConsulta() throws Exception {
        List<Paciente> todosPacientes = pacienteDAO.findAll();
        List<Consulta> consultas = consultaDAO.findAll();
        
        // Pacientes com consulta
        Set<String> pacientesComConsulta = new HashSet<>();
        for (Consulta consulta : consultas) {
            pacientesComConsulta.add(consulta.getPaciente().getCpf());
        }
        
        // Pacientes sem consulta
        List<Paciente> pacientesSemConsulta = new ArrayList<>();
        for (Paciente paciente : todosPacientes) {
            if (!pacientesComConsulta.contains(paciente.getCpf())) {
                pacientesSemConsulta.add(paciente);
            }
        }
        
        // Configurar tabela
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