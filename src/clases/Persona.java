package clases;

/**
 * Clase abstracta base para Empleado y Cliente.
 */
public abstract class Persona {

    protected String documento;  // DNI (8 dígitos) o RUC (11 dígitos)
    protected String nombres;

    public Persona(String documento, String nombres) {
        this.documento = documento;
        this.nombres = nombres;
    }

    // Cada hija decide cómo mostrar su nombre completo
    public abstract String getNombreCompleto();

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
}
