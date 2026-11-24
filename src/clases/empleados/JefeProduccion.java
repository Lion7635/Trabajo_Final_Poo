package clases.empleados;

public class JefeProduccion extends Empleado {

    public JefeProduccion(String dni, String nombres, String apellidos, String usuario, String contrasena) {
        super(dni, nombres, apellidos, usuario, contrasena);
    }

    @Override
    public String getRol() {
        return "Jefe de Producción";
    }

    // Solo registra el avance de producción y el control de calidad
    @Override
    public boolean tienePermiso(String modulo) {
        return modulo.equals(MOD_PRODUCCION) || modulo.equals(MOD_TABLERO)
                || modulo.equals(MOD_STOCK);
    }
}
