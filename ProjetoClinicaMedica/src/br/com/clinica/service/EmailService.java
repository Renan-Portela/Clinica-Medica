package br.com.clinica.service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Serviço responsável pelo envio de e-mails utilizando o SMTP do Gmail.
 *
 * ATENÇÃO: Este código contém credenciais hardcoded, que é como fazer deploy 
 * direto em produção numa sexta-feira às 18h. Tecnicamente funciona, mas você vai acordar 
 * de madrugada com o celular tocando.
 * 
 * TODO: Migrar para variáveis de ambiente antes que o chefe descubra.
 */
public class EmailService {

    private static final String HOST = "smtp.gmail.com";
    private static final String PORT = "587";
    private static final String USERNAME = "projeto.clinicamedica.2025@gmail.com";
    private static final String PASSWORD = "yrdj eecd hpds mzgq ";

    /**
     * Envia um e-mail para um destinatário com um assunto e mensagem específicos.
     * Interage com as classes: Properties, Session, Message da API JavaMail.
     * @param destinatario O e-mail do destinatário.
     * @param assunto O assunto do e-mail.
     * @param mensagem O corpo da mensagem do e-mail.
     * @return true se o e-mail foi enviado com sucesso, false caso contrário.
     */
    public static boolean enviarEmail(String destinatario, String assunto, String mensagem) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", HOST);
            props.put("mail.smtp.port", PORT);
            props.put("mail.smtp.ssl.trust", HOST);

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(USERNAME, PASSWORD);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            message.setSubject(assunto);
            message.setText(mensagem);

            Transport.send(message);

            System.out.println("Email enviado com sucesso para: " + destinatario);
            return true;

        } catch (MessagingException e) {
            System.err.println("Erro ao enviar email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Método de teste para verificar se a configuração de envio de e-mail está funcionando.
     * Envia um e-mail de teste para o endereço configurado na classe.
     * @return true se o e-mail foi enviado com sucesso, false caso contrário.
     */
    public static boolean testarEmail() {
        String emailTeste = "projeto.clinicamedica.2025@gmail.com";
        String assunto = "Teste - Sistema Clinica";
        String mensagem = "Este e um teste do sistema de email da clinica medica.\n\nSe voce recebeu este email, o sistema esta funcionando!";

        return enviarEmail(emailTeste, assunto, mensagem);
    }

    /**
     * Monta e envia um e-mail padronizado de confirmação de agendamento para um paciente.
     * Interage com as classes: Internamente chama o método enviarEmail.
     * @param emailPaciente O e-mail do paciente.
     * @param nomePaciente O nome do paciente.
     * @param nomeMedico O nome do médico.
     * @param dataHora A data e hora formatada da consulta.
     * @return true se o e-mail de confirmação foi enviado com sucesso, false caso contrário.
     */
    public static boolean enviarConfirmacaoConsulta(String emailPaciente, String nomePaciente,
                                                   String nomeMedico, String dataHora) {
        String assunto = "Confirmacao de Consulta - Clinica Medica";
        String mensagem = String.format(
            "Prezado(a) %s,\n\n" +
            "Sua consulta foi agendada com sucesso!\n\n" +
            "Detalhes:\n" +
            "Medico: %s\n" +
            "Data/Hora: %s\n\n" +
            "Por favor, chegue 15 minutos antes do horario.\n\n" +
            "Atenciosamente,\n" +
            "Clinica Medica",
            nomePaciente, nomeMedico, dataHora
        );

        boolean enviado = enviarEmail(emailPaciente, assunto, mensagem);
        if (enviado) {
            System.out.println("Email de confirmacao enviado para: " + nomePaciente);
        } else {
            System.err.println("Falha ao enviar email para: " + nomePaciente);
        }
        
        return enviado;
    }
}