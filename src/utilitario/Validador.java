package utilitario;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Métodos de apoyo para validar datos de los formularios.
 */
public class Validador {

    public static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static boolean esVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    public static boolean soloDigitos(String texto, int longitud) {
        if (texto == null || texto.length() != longitud) {
            return false;
        }
        for (int i = 0; i < texto.length(); i++) {
            if (!Character.isDigit(texto.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean esDni(String texto) {
        return soloDigitos(texto, 8);
    }

    public static boolean esRuc(String texto) {
        return soloDigitos(texto, 11) && (texto.startsWith("10") || texto.startsWith("20"));
    }

    public static boolean esTelefono(String texto) {
        return soloDigitos(texto, 9) || soloDigitos(texto, 7);
    }

    public static boolean esCorreo(String texto) {
        if (texto == null) {
            return false;
        }
        int arroba = texto.indexOf('@');
        return arroba > 0 && texto.indexOf('.', arroba) > arroba + 1 && !texto.endsWith(".");
    }

    public static boolean esEntero(String texto) {
        try {
            Integer.parseInt(texto.trim());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean esDecimal(String texto) {
        try {
            Double.parseDouble(texto.trim().replace(',', '.'));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static double leerDecimal(String texto) {
        return Double.parseDouble(texto.trim().replace(',', '.'));
    }

    public static int leerEntero(String texto) {
        return Integer.parseInt(texto.trim());
    }

    // Convierte "dd/MM/yyyy" en fecha; devuelve null si el formato es incorrecto
    public static LocalDate leerFecha(String texto) {
        try {
            return LocalDate.parse(texto.trim(), FORMATO_FECHA);
        } catch (Exception e) {
            return null;
        }
    }

    public static String fecha(LocalDate fecha) {
        if (fecha == null) {
            return "-";
        }
        return fecha.format(FORMATO_FECHA);
    }

    public static String dinero(double monto) {
        return String.format("S/ %,.2f", monto);
    }

    public static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
