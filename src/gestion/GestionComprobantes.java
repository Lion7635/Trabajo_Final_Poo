package gestion;

import clases.comprobantes.Boleta;
import clases.comprobantes.Comprobante;
import clases.comprobantes.Factura;
import clases.pedidos.Pedido;
import java.time.LocalDate;

/**
 * RF07 - Emisión de comprobantes con numeración correlativa por serie.
 */
public class GestionComprobantes {

    private Comprobante[] comprobantes;
    private int totalComprobantes;
    private int correlativoBoletas;
    private int correlativoFacturas;

    public GestionComprobantes() {
        comprobantes = new Comprobante[300];
        totalComprobantes = 0;
        correlativoBoletas = 0;
        correlativoFacturas = 0;
    }

    public Comprobante emitirBoleta(Pedido pedido, LocalDate fecha) {
        correlativoBoletas++;
        Comprobante c = new Boleta(String.format("B001-%06d", correlativoBoletas), fecha, pedido);
        guardar(c);
        return c;
    }

    public Comprobante emitirFactura(Pedido pedido, LocalDate fecha, String ruc, String razonSocial) {
        correlativoFacturas++;
        Comprobante c = new Factura(String.format("F001-%06d", correlativoFacturas), fecha, pedido, ruc, razonSocial);
        guardar(c);
        return c;
    }

    private void guardar(Comprobante c) {
        if (totalComprobantes < comprobantes.length) {
            comprobantes[totalComprobantes] = c;
            totalComprobantes++;
        }
    }

    public Comprobante[] getComprobantes() { return comprobantes; }
    public int getTotalComprobantes() { return totalComprobantes; }
}
