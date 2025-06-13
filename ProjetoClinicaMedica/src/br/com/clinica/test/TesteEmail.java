package br.com.clinica.test;

import br.com.clinica.service.EmailService;
import javax.swing.*;

public class TesteEmail {
    
    public static void main(String[] args) {
        System.out.println("=== TESTE DO SISTEMA DE EMAIL ===");
        
        // Teste simples
        boolean resultado = EmailService.testarEmail();
        
        if (resultado) {
            System.out.println("✓ Email enviado com sucesso!");
            JOptionPane.showMessageDialog(null, "Email de teste enviado com sucesso!\nVerifique sua caixa de entrada.");
        } else {
            System.out.println("✗ Falha ao enviar email!");
            JOptionPane.showMessageDialog(null, "Erro ao enviar email.\nVerifique as configuracoes.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Método para testar envio de confirmação de consulta
    public static void testarConfirmacaoConsulta() {
        String emailPaciente = "projeto.clinicamedica.2025@gmail.com"; // ALTERE AQUI
        String nomePaciente = "José Vitor";
        String nomeMedico = "Dr. Carlos Santos";
        String dataHora = "15/06/2025 14:30";
        
        EmailService.enviarConfirmacaoConsulta(emailPaciente, nomePaciente, nomeMedico, dataHora);
    }
}
