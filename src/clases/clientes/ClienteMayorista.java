package clases.clientes;

public class ClienteMayorista extends Cliente {

    public static final int PRENDAS_MINIMAS = 200;
    public static final double PORCENTAJE = 0.10;

    public ClienteMayorista(String documento, String nombres, String telefono, String correo, String direccion) {
        super(documento, nombres, telefono, correo, direccion);
    }

    @Override
    public String getTipo() {
        return "Mayorista";
    }

    // Descuento por volumen: 10% a partir de 200 prendas por pedido
    @Override
    public double calcularDescuento(double subtotalPrendas, int totalPrendas, boolean incluyeLogo) {
        if (totalPrendas >= PRENDAS_MINIMAS) {
            return subtotalPrendas * PORCENTAJE;
        }
        return 0;
    }

    @Override
    public String getBeneficio() {
        return "10% desde 200 prendas";
    }
}
