package br.com.clinica.service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {

    // CONFIGURAÇÕES GMAIL
    private static final String HOST = "smtp.gmail.com";
    private static final String PORT = "587";

    // ALTERE AQUI COM SEUS DADOS
    private static final String USERNAME = "projeto.clinicamedica.2025@gmail.com"; // SEU EMAIL
    private static final String PASSWORD = "yrdj eecd hpds mzgq "; // SUA SENHA DE APP

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

    // MÉTODO PARA TESTE SIMPLES
    public static boolean testarEmail() {
        String emailTeste = "projeto.clinicamedica.2025@gmail.com"; // COLOQUE SEU EMAIL AQUI
        String assunto = "Teste - Sistema Clinica";
        String mensagem = "Este e um teste do sistema de email da clinica medica.\n\nSe voce recebeu este email, o sistema esta funcionando!";

        return enviarEmail(emailTeste, assunto, mensagem);
    }

    // MÉTODO CORRIGIDO - AGORA RETORNA BOOLEAN
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
        
        return enviado; // RETORNA O RESULTADO
    }
}