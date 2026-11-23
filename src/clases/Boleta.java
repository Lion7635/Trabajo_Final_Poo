package clases;

import java.time.LocalDate;

public class Boleta extends Comprobante {

    public Boleta(String numero, LocalDate fecha, Pedido pedido) {
        super(numero, fecha, pedido);
    }

    @Override
    public String getTipo() {
        return "Boleta";
    }

    @Override
    protected String getDatosCliente() {
        Cliente c = getPedido().getCliente();
        return "Cliente: " + c.getNombreCompleto() + "\nDocumento: " + c.getDocumento() + "\n";
    }
}
