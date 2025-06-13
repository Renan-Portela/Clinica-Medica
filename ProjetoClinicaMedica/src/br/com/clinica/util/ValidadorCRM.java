package br.com.clinica.util;

public class ValidadorCRM {
	
	public static boolean validar(String crm) {
        if (crm == null || crm.trim().isEmpty()) return false;
        return crm.matches("CRM\\d{4,6}");
    }
}
