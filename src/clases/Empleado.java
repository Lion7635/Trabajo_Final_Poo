package clases;

/**
 * Clase abstracta para los empleados que usan el sistema (RF01, RF09).
 * Cada rol es una subclase que decide polimórficamente a qué módulos accede.
 */
public abstract class Empleado extends Persona implements Autenticable {

    // Módulos del sistema (se usan para habilitar los menús según el rol)
    public static final String MOD_EMPLEADOS = "EMPLEADOS";
    public static final String MOD_MODELOS = "MODELOS";
    public static final String MOD_MATERIA_PRIMA = "MATERIA_PRIMA";   // telas, insumos, servicios, paquetes
    public static final String MOD_CLIENTES = "CLIENTES";
    public static final String MOD_PEDIDOS = "PEDIDOS";               // registrar, confirmar, cancelar, despachar y cobrar
    public static final String MOD_PRODUCCION = "PRODUCCION";         // corte, avance y control de calidad
    public static final String MOD_REPORTES = "REPORTES";
    public static final String MOD_TABLERO = "TABLERO";
    public static final String MOD_STOCK = "STOCK";                   // consulta de stock de telas

    private String apellidos;
    private String usuario;
    private String contrasena;

    public Empleado(String dni, String nombres, String apellidos, String usuario, String contrasena) {
        super(dni, nombres);
        this.apellidos = apellidos;
        this.usuario = usuario;
        this.contrasena = contrasena;
    }

    // Métodos abstractos: los implementa cada rol
    public abstract String getRol();

    public abstract boolean tienePermiso(String modulo);

    @Override
    public boolean autenticar(String usuario, String contrasena) {
        return this.usuario.equals(usuario) && this.contrasena.equals(contrasena);
    }

    @Override
    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    public String getDni() { return documento; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
}
