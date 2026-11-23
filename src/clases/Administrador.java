package clases;

public class Administrador extends Empleado {

    public Administrador(String dni, String nombres, String apellidos, String usuario, String contrasena) {
        super(dni, nombres, apellidos, usuario, contrasena);
    }

    @Override
    public String getRol() {
        return "Administrador";
    }

    // Solo el Administrador gestiona personal, modelos, precios, telas, insumos y ve reportes
    @Override
    public boolean tienePermiso(String modulo) {
        return modulo.equals(MOD_EMPLEADOS) || modulo.equals(MOD_MODELOS)
                || modulo.equals(MOD_MATERIA_PRIMA) || modulo.equals(MOD_REPORTES)
                || modulo.equals(MOD_TABLERO) || modulo.equals(MOD_STOCK);
    }
}
