package clases.clientes;

public class ClienteMinorista extends Cliente {

    public ClienteMinorista(String documento, String nombres, String telefono, String correo, String direccion) {
        super(documento, nombres, telefono, correo, direccion);
    }

    @Override
    public String getTipo() {
        return "Minorista";
    }

    // El minorista no tiene descuento
    @Override
    public double calcularDescuento(double subtotalPrendas, int totalPrendas, boolean incluyeLogo) {
        return 0;
    }

    @Override
    public String getBeneficio() {
        return "Sin descuento";
    }
}
