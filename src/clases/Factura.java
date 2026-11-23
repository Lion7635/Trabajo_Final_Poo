package clases;

import java.time.LocalDate;

/**
 * La factura exige RUC (11 dígitos) y razón social.
 */
public class Factura extends Comprobante {

    private String ruc;
    private String razonSocial;

    public Factura(String numero, LocalDate fecha, Pedido pedido, String ruc, String razonSocial) {
        super(numero, fecha, pedido);
        this.ruc = ruc;
        this.razonSocial = razonSocial;
    }

    // RUC válido: 11 dígitos que empiezan en 10 o 20
    public static boolean esRucValido(String ruc) {
        if (ruc == null || ruc.length() != 11) {
            return false;
        }
        for (int i = 0; i < ruc.length(); i++) {
            if (!Character.isDigit(ruc.charAt(i))) {
                return false;
            }
        }
        return ruc.startsWith("10") || ruc.startsWith("20");
    }

    @Override
    public String getTipo() {
        return "Factura";
    }

    @Override
    protected String getDatosCliente() {
        return "Razón social: " + razonSocial + "\nRUC: " + ruc + "\n";
    }

    public String getRuc() { return ruc; }
    public String getRazonSocial() { return razonSocial; }
}
