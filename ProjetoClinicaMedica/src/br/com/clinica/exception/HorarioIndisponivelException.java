package br.com.clinica.exception;

/**
 * Exceção lançada quando uma tentativa de agendamento ocorre em um horário
 * que já está ocupado ou indisponível para um determinado médico.
 */
public class HorarioIndisponivelException extends Exception {

    /**
     * Construtor padrão sem mensagem detalhada.
     */
    public HorarioIndisponivelException() {
        super("O horário selecionado não está disponível.");
    }

    /**
     * Construtor que aceita uma mensagem detalhada sobre o erro.
     * @param message A mensagem explicando a causa da exceção.
     */
    public HorarioIndisponivelException(String message) {
        super(message);
    }
}