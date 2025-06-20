package br.com.clinica.util;

import java.awt.Color;

/**
 * Interface para centralizar as constantes de cores da aplicação,
 * garantindo um tema visual consistente em todas as telas.
 */
public interface UITheme {
    Color PRIMARY_BLUE = new Color(52, 144, 220);
    Color SUCCESS_GREEN = new Color(40, 167, 69);
    Color ACCENT_RED = new Color(220, 53, 69);
    Color MEDICAL_GREEN = new Color(76, 175, 80);
    Color CLEAN_WHITE = new Color(255, 255, 255);
    Color LIGHT_GRAY = new Color(248, 249, 250);
    Color DARK_TEXT = new Color(52, 58, 64);
    
    // Cores para status de consulta
    Color COR_AGENDADA = new Color(52, 144, 220, 200);
    Color COR_REALIZADA = new Color(76, 175, 80, 200);
    Color COR_CANCELADA = new Color(244, 67, 54, 200);
    Color COR_NAO_COMPARECEU = new Color(108, 117, 125, 200);
}