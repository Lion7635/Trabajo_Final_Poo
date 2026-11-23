package clases;

public class ClienteInstitucional extends Cliente {

    public static final double PORCENTAJE = 0.15;

    public ClienteInstitucional(String documento, String nombres, String telefono, String correo, String direccion) {
        super(documento, nombres, telefono, correo, direccion);
    }

    @Override
    public String getTipo() {
        return "Institucional";
    }

    // Precio especial (15% menos) en uniformes con logo (bordado o estampado de logo)
    @Override
    public double calcularDescuento(double subtotalPrendas, int totalPrendas, boolean incluyeLogo) {
        if (incluyeLogo) {
            return subtotalPrendas * PORCENTAJE;
        }
        return 0;
    }

    @Override
    public String getBeneficio() {
        return "15% en uniformes con logo";
    }
}
