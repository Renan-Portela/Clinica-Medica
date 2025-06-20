package br.com.clinica.service;

import br.com.clinica.dao.ConsultaDAO;
import br.com.clinica.dao.MedicoDAO;
import br.com.clinica.model.Consulta;
import br.com.clinica.model.Consulta.StatusConsulta;
import br.com.clinica.model.Medico;
import br.com.clinica.model.Paciente;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Camada de serviço para gerenciar as regras de negócio relacionadas a consultas.
 * Esta classe centraliza a lógica de manipulação de consultas, separando-a
 * da camada de visualização (telas).
 * Interage com as classes: ConsultaDAO, MedicoDAO, Consulta, Medico, Paciente.
 */
public class ConsultaService {

    private final ConsultaDAO consultaDAO;
    private final MedicoDAO medicoDAO;

    /**
     * Construtor do serviço. Inicializa as dependências dos DAOs.
     */
    public ConsultaService() {
        this.consultaDAO = new ConsultaDAO();
        this.medicoDAO = new MedicoDAO();
    }
    
    /**
     * Agenda uma nova consulta, aplicando as regras de negócio.
     * @param medico O médico para a consulta.
     * @param paciente O paciente da consulta.
     * @param dataHorario A data e hora da consulta.
     * @param observacoes Observações iniciais.
     * @return O objeto Consulta criado e salvo no banco.
     * @throws SQLException Se ocorrer um erro de banco de dados.
     * @throws IllegalStateException Se a data do agendamento for no passado.
     */
    public Consulta agendarNovaConsulta(Medico medico, Paciente paciente, LocalDateTime dataHorario, String observacoes) throws SQLException, IllegalStateException {
        if (dataHorario.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Não é possível agendar consultas em datas ou horários passados.");
        }

        Consulta consulta = new Consulta(medico, paciente, dataHorario);
        consulta.setObservacoes(observacoes);

        consultaDAO.save(consulta);
        return consulta;
    }

    /**
     * Retorna uma lista de horários disponíveis para um médico em uma data específica.
     * @param medico O médico selecionado.
     * @param data A data selecionada.
     * @return Uma lista de Strings com os horários disponíveis (formato HH:mm).
     * @throws SQLException Se ocorrer um erro de banco de dados.
     */
    public List<String> getHorariosDisponiveis(Medico medico, LocalDate data) throws SQLException {
        // Horários padrão de funcionamento da clínica
        String[] todosHorarios = {
                "08:00", "08:30", "09:00", "09:30", "10:00", "10:30",
                "11:00", "11:30", "14:00", "14:30", "15:00", "15:30",
                "16:00", "16:30", "17:00"
        };
        
        List<String> horariosDisponiveis = new ArrayList<>(List.of(todosHorarios));

        List<Consulta> consultasDoDia = consultaDAO.findAll().stream()
            .filter(c -> c.getMedico().getCrm().equals(medico.getCrm()) &&
                         c.getDataHorario().toLocalDate().equals(data) &&
                         (c.getStatus() == StatusConsulta.AGENDADA || c.getStatus() == StatusConsulta.REALIZADA))
            .collect(Collectors.toList());

        for (Consulta consulta : consultasDoDia) {
            String horarioOcupado = consulta.getDataHorario().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
            horariosDisponiveis.remove(horarioOcupado);
        }

        return horariosDisponiveis;
    }

    /**
     * Altera o status de uma consulta para CANCELADA.
     * Aplica a regra de negócio que só permite o cancelamento de consultas
     * que estão com o status AGENDADA.
     *
     * @param consulta A consulta a ser cancelada.
     * @throws SQLException Se ocorrer um erro de banco de dados.
     * @throws IllegalStateException Se a consulta não estiver com o status AGENDADA.
     */
    public void cancelarConsulta(Consulta consulta) throws SQLException, IllegalStateException {
        if (consulta.getStatus() != StatusConsulta.AGENDADA) {
            throw new IllegalStateException("Apenas consultas com status 'Agendada' podem ser canceladas.");
        }
        
        consulta.setStatus(StatusConsulta.CANCELADA);
        consultaDAO.update(consulta);
    }

    /**
     * Altera o status de uma consulta para REALIZADA.
     * Aplica a regra de negócio que só permite a realização de consultas
     * que estão com o status AGENDADA.
     *
     * @param consulta A consulta a ser marcada como realizada.
     * @param observacoes O texto com as observações da consulta realizada.
     * @throws SQLException Se ocorrer um erro de banco de dados.
     * @throws IllegalStateException Se a consulta não estiver com o status AGENDADA.
     */
    public void marcarComoRealizada(Consulta consulta, String observacoes) throws SQLException, IllegalStateException {
        if (consulta.getStatus() != StatusConsulta.AGENDADA) {
            throw new IllegalStateException("Apenas consultas com status 'Agendada' podem ser marcadas como realizadas.");
        }

        consulta.setStatus(StatusConsulta.REALIZADA);
        consulta.setObservacoes(observacoes);
        consultaDAO.update(consulta);
    }
}