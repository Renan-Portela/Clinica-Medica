package br.com.clinica.service;

import br.com.clinica.dao.ConsultaDAO;
import br.com.clinica.dao.MedicoDAO;
import br.com.clinica.dao.PacienteDAO;
import br.com.clinica.model.Consulta;
import br.com.clinica.model.Medico;
import br.com.clinica.model.Paciente;

import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RelatorioService {

    private final ConsultaDAO consultaDAO;
    private final MedicoDAO medicoDAO;
    private final PacienteDAO pacienteDAO;

    public RelatorioService() {
        this.consultaDAO = new ConsultaDAO();
        this.medicoDAO = new MedicoDAO();
        this.pacienteDAO = new PacienteDAO();
    }

    private Map<String, Object> criarResultado(DefaultTableModel tableModel, String summaryText) {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("tableModel", tableModel);
        resultado.put("summaryText", summaryText);
        return resultado;
    }

    private List<Consulta> filtrarConsultasPorPeriodo(List<Consulta> consultas, int mes, int ano) {
        return consultas.stream().filter(c -> {
            boolean match = true;
            if (ano > 0) match = c.getDataHorario().getYear() == ano;
            if (mes > 0) match = match && c.getDataHorario().getMonthValue() == mes;
            return match;
        }).collect(Collectors.toList());
    }

    public Map<String, Object> gerarRelatorioConsultasPorMedico(Medico medicoFiltro, int mes, int ano) throws SQLException {
        List<Consulta> consultas = filtrarConsultasPorPeriodo(consultaDAO.findAll(), mes, ano).stream()
            .filter(c -> medicoFiltro == null || c.getMedico().getCrm().equals(medicoFiltro.getCrm()))
            .collect(Collectors.toList());

        String[] colunas = {"ID Consulta", "Data", "Médico", "Paciente", "Status", "Obs.", "CRM Médico", "CPF Paciente"};
        DefaultTableModel model = new DefaultTableModel(colunas, 0);
        consultas.forEach(c -> model.addRow(new Object[]{
            c.getId(), c.getDataHorarioFormatado(), c.getMedico().getNome(), c.getPaciente().getNome(), 
            c.getStatus().getDescricao(), c.getObservacoes(), c.getMedico().getCrm(), c.getPaciente().getCpf()
        }));

        String resumo = String.format("Total de consultas encontradas no filtro: %d", consultas.size());
        return criarResultado(model, resumo);
    }
    
    public Map<String, Object> gerarRelatorioConsultasCanceladas(int mes, int ano) throws SQLException {
        List<Consulta> canceladas = filtrarConsultasPorPeriodo(consultaDAO.findAll(), mes, ano).stream()
            .filter(c -> c.getStatus() == Consulta.StatusConsulta.CANCELADA)
            .collect(Collectors.toList());

        String[] colunas = {"ID Consulta", "Data", "Médico", "Paciente", "Observações", "CRM Médico", "CPF Paciente"};
        DefaultTableModel model = new DefaultTableModel(colunas, 0);
        canceladas.forEach(c -> model.addRow(new Object[]{
            c.getId(), c.getDataHorarioFormatado(), c.getMedico().getNome(), c.getPaciente().getNome(), c.getObservacoes(),
            c.getMedico().getCrm(), c.getPaciente().getCpf()
        }));
        
        String resumo = String.format("Total de consultas canceladas no período: %d", canceladas.size());
        return criarResultado(model, resumo);
    }
    
    public Map<String, Object> gerarRelatorioHistoricoPaciente(String pacienteInput) throws SQLException {
        if (pacienteInput == null || pacienteInput.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome ou CPF do paciente é obrigatório.");
        }
        
        Paciente pacienteEncontrado = pacienteDAO.findAll().stream()
            .filter(p -> p.getNome().equalsIgnoreCase(pacienteInput.trim()) || p.getCpf().equals(pacienteInput.replaceAll("[^0-9]", "")))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado."));

        List<Consulta> historico = consultaDAO.findByPaciente(pacienteEncontrado.getCpf());
        
        String[] colunas = {"ID Consulta", "Data", "Médico", "Status", "Observações", "CRM Médico"};
        DefaultTableModel model = new DefaultTableModel(colunas, 0);
        historico.forEach(c -> model.addRow(new Object[]{
            c.getId(), c.getDataHorarioFormatado(), c.getMedico().getNome(), c.getStatus().getDescricao(), c.getObservacoes(),
            c.getMedico().getCrm()
        }));

        String resumo = String.format("Histórico do Paciente: %s\nTotal de consultas: %d", pacienteEncontrado.getNome(), historico.size());
        return criarResultado(model, resumo);
    }
    
    public Map<String, Object> gerarRelatorioPacientesInativos() throws SQLException {
        LocalDateTime umAnoAtras = LocalDateTime.now().minusYears(1);
        Set<String> cpfsAtivos = consultaDAO.findAll().stream()
            .filter(c -> c.getDataHorario().isAfter(umAnoAtras))
            .map(c -> c.getPaciente().getCpf())
            .collect(Collectors.toSet());
            
        List<Paciente> pacientesInativos = pacienteDAO.findAll().stream()
            .filter(p -> !cpfsAtivos.contains(p.getCpf()))
            .collect(Collectors.toList());

        String[] colunas = {"Nome", "CPF", "Telefone"};
        DefaultTableModel model = new DefaultTableModel(colunas, 0);
        pacientesInativos.forEach(p -> model.addRow(new Object[]{ p.getNome(), p.getCpf(), p.getTelefone() }));

        String resumo = String.format("Total de pacientes sem consulta no último ano: %d", pacientesInativos.size());
        return criarResultado(model, resumo);
    }

    public Map<String, Object> gerarRelatorioDistribuicaoConsultas(int mes, int ano) throws SQLException {
        List<Consulta> consultas = filtrarConsultasPorPeriodo(consultaDAO.findAll(), mes, ano);

        Map<String, Long> contagemPorDia = consultas.stream()
            .map(c -> c.getDataHorario().getDayOfWeek())
            .collect(Collectors.groupingBy(
                dayOfWeek -> dayOfWeek.getDisplayName(TextStyle.FULL, new Locale("pt", "BR")),
                TreeMap::new, // Garante a ordem
                Collectors.counting()
            ));

        String[] colunas = {"Dia da Semana", "Nº de Consultas"};
        DefaultTableModel model = new DefaultTableModel(colunas, 0);
        contagemPorDia.forEach((dia, total) -> model.addRow(new Object[]{dia, total}));

        String resumo = "Distribuição de consultas por dia da semana no período selecionado.";
        return criarResultado(model, resumo);
    }
}