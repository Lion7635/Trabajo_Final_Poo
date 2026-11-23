package clases;

public class EjecutivoVentas extends Empleado {

    public EjecutivoVentas(String dni, String nombres, String apellidos, String usuario, String contrasena) {
        super(dni, nombres, apellidos, usuario, contrasena);
    }

    @Override
    public String getRol() {
        return "Ejecutivo de Ventas";
    }

    // Solo gestiona clientes, pedidos y cobros
    @Override
    public boolean tienePermiso(String modulo) {
        return modulo.equals(MOD_CLIENTES) || modulo.equals(MOD_PEDIDOS)
                || modulo.equals(MOD_TABLERO) || modulo.equals(MOD_STOCK);
    }
}
