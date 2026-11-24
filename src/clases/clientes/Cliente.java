package clases.clientes;

import clases.personas.Persona;

/**
 * Clase abstracta para los clientes (RF03).
 * El descuento se calcula de forma polimórfica según el tipo de cliente.
 */
public abstract class Cliente extends Persona {

    private String telefono;
    private String correo;
    private String direccion;

    public Cliente(String documento, String nombres, String telefono, String correo, String direccion) {
        super(documento, nombres);
        this.telefono = telefono;
        this.correo = correo;
        this.direccion = direccion;
    }

    public abstract String getTipo();

    /**
     * Descuento sobre el subtotal de prendas del pedido.
     * @param subtotalPrendas monto de las prendas (precio x cantidad)
     * @param totalPrendas cantidad total de prendas del pedido
     * @param incluyeLogo true si el pedido lleva un servicio con logo (uniformes)
     */
    public abstract double calcularDescuento(double subtotalPrendas, int totalPrendas, boolean incluyeLogo);

    // Descripción del beneficio para mostrar en pantalla
    public abstract String getBeneficio();

    public boolean tieneRuc() {
        return documento.length() == 11;
    }

    @Override
    public String getNombreCompleto() {
        return nombres;
    }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    @Override
    public String toString() {
        return documento + " - " + nombres;
    }
}
