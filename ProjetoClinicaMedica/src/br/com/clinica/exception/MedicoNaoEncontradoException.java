package br.com.clinica.exception;

/**
 * Exceção lançada quando uma busca por um médico específico (geralmente pelo CRM)
 * não retorna resultados, indicando que o médico não está cadastrado no sistema.
 */
public class MedicoNaoEncontradoException extends Exception {

    /**
     * Construtor padrão com uma mensagem detalhada.
     */
    public MedicoNaoEncontradoException() {
        super("O médico procurado não foi encontrado no sistema.");
    }

    /**
     * Construtor que aceita uma mensagem personalizada sobre o erro.
     * @param message A mensagem explicando a causa da exceção.
     */
    public MedicoNaoEncontradoException(String message) {
        super(message);
    }
}