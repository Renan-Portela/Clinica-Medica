package br.com.clinica;

import br.com.clinica.view.TelaPrincipal;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            // Look and Feel
            UIManager.setLookAndFeel(UIManager.getLookAndFeel());
        } catch (Exception e) {
            // Usar padrao se der erro
        }
        
        // Iniciar aplicacao
        SwingUtilities.invokeLater(() -> {
            new TelaPrincipal().setVisible(true);
        });
    }
}