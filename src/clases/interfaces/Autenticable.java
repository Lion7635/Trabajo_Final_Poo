package clases.interfaces;

/**
 * Interfaz (contrato) para todo aquel que puede iniciar sesión en el sistema.
 * RF09 - Seguridad y control de acceso.
 */
public interface Autenticable {

    boolean autenticar(String usuario, String contrasena);
}
