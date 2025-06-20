package br.com.clinica.exception;

/**
 * Exceção lançada quando uma busca por um paciente específico (geralmente pelo CPF)
 * não retorna resultados, indicando que o paciente não está cadastrado no sistema.
 */
public class PacienteNaoEncontradoException extends Exception {

    /**
     * Construtor padrão com uma mensagem detalhada.
     */
    public PacienteNaoEncontradoException() {
        super("O paciente procurado não foi encontrado no sistema.");
    }

    /**
     * Construtor que aceita uma mensagem personalizada sobre o erro.
     * @param message A mensagem explicando a causa da exceção.
     */
    public PacienteNaoEncontradoException(String message) {
        super(message);
    }
}