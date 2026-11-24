package clases.comprobantes;

import clases.pedidos.ItemPedido;
import clases.pedidos.Pedido;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Clase abstracta para los comprobantes de pago (RF07).
 * Guarda los montos calculados al momento del despacho.
 */
public abstract class Comprobante {

    private String numero;       // serie + correlativo, ej. B001-000001
    private LocalDate fecha;
    private Pedido pedido;
    private double subtotal;     // base imponible
    private double igv;
    private double total;
    private double adelanto;
    private double saldo;

    public Comprobante(String numero, LocalDate fecha, Pedido pedido) {
        this.numero = numero;
        this.fecha = fecha;
        this.pedido = pedido;
        this.subtotal = pedido.calcularSubtotal();
        this.igv = pedido.calcularIgv();
        this.total = pedido.calcularTotal();
        this.adelanto = pedido.getAdelanto();
        this.saldo = total - adelanto;
    }

    public abstract String getTipo();               // Boleta o Factura

    protected abstract String getDatosCliente();    // cada comprobante muestra al cliente distinto

    // Arma el texto completo del comprobante con el detalle de cada concepto
    public String generarTexto() {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String linea = "--------------------------------------------------------------------------\n";
        String t = "";
        t += "            CONFECCIONES GAMARRA - " + getTipo().toUpperCase() + " ELECTRÓNICA\n";
        t += "            N° " + numero + "        Fecha: " + fecha.format(f) + "\n";
        t += linea;
        t += getDatosCliente();
        t += "Pedido: " + pedido.getNumeroTexto() + "   Tipo de cliente: " + pedido.getCliente().getTipo() + "\n";
        t += linea;
        t += String.format("%-44s %6s %10s %11s%n", "Concepto", "Cant.", "P. Unit.", "Importe");
        t += linea;
        for (int i = 0; i < pedido.getTotalItems(); i++) {
            ItemPedido item = pedido.getItems()[i];
            String desc = "[" + item.getTipoItem() + "] " + item.getDescripcion();
            if (desc.length() > 44) {
                desc = desc.substring(0, 41) + "...";
            }
            t += String.format("%-44s %6d %10.2f %11.2f%n", desc, item.getCantidad(),
                    item.getPrecioUnitario(), item.calcularSubtotal());
        }
        t += linea;
        t += String.format("%-62s %11.2f%n", "Prendas", pedido.getSubtotalPorTipo("Prenda"));
        t += String.format("%-62s %11.2f%n", "(-) Descuento " + pedido.getCliente().getTipo()
                + " (" + pedido.getCliente().getBeneficio() + ")", -pedido.calcularDescuento());
        t += String.format("%-62s %11.2f%n", "Servicios de acabado", pedido.getSubtotalPorTipo("Servicio"));
        t += String.format("%-62s %11.2f%n", "Paquetes promocionales", pedido.getSubtotalPorTipo("Paquete"));
        t += linea;
        t += String.format("%-62s %11.2f%n", "SUBTOTAL", subtotal);
        t += String.format("%-62s %11.2f%n", "IGV (18%)", igv);
        t += String.format("%-62s %11.2f%n", "TOTAL", total);
        t += String.format("%-62s %11.2f%n", "(-) Adelanto pagado", -adelanto);
        t += String.format("%-62s %11.2f%n", "SALDO A PAGAR", saldo);
        return t;
    }

    public String getNumero() { return numero; }
    public LocalDate getFecha() { return fecha; }
    public Pedido getPedido() { return pedido; }
    public double getSubtotal() { return subtotal; }
    public double getIgv() { return igv; }
    public double getTotal() { return total; }
    public double getAdelanto() { return adelanto; }
    public double getSaldo() { return saldo; }
}
