package clases;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Pedido de producción (RF05, RF06, RF07).
 * Se compone de un arreglo de ítems (composición) y de un arreglo con el historial de estados.
 */
public class Pedido {

    // Estados del flujo operativo
    public static final String REGISTRADO = "Registrado";
    public static final String CONFIRMADO = "Confirmado";
    public static final String EN_CORTE = "En Corte";
    public static final String EN_CONFECCION = "En Confección";
    public static final String EN_ACABADO = "En Acabado";
    public static final String LISTO_DESPACHO = "Listo para Despacho";
    public static final String ENTREGADO = "Entregado";
    public static final String CANCELADO = "Cancelado";

    // Etapas activas en el orden del tablero
    public static final String[] ETAPAS_ACTIVAS = {REGISTRADO, CONFIRMADO, EN_CORTE, EN_CONFECCION, EN_ACABADO, LISTO_DESPACHO};

    public static final double IGV = 0.18;
    public static final double MERMA = 0.05;
    public static final double PORCENTAJE_ADELANTO = 0.50;

    private int numero;
    private LocalDate fechaRegistro;
    private LocalDate fechaEntrega;      // solicitada por el cliente
    private Cliente cliente;
    private EjecutivoVentas ejecutivo;   // quien vendió el pedido
    private JefeProduccion jefeProduccion; // quien registró el corte

    private ItemPedido[] items;
    private int totalItems;

    private String estado;
    private HistorialEstado[] historial;
    private int totalHistorial;

    private double adelanto;
    private String situacionAdelanto;     // Pagado, Devuelto o Retenido
    private int[] defectuososPorTalla;    // acumulado del control de calidad (S, M, L, XL)
    private double metrosConsumidos;      // se registra al cortar (incluye merma)
    private double metrosMerma;
    private LocalDate fechaDespacho;
    private Comprobante comprobante;

    public Pedido(Cliente cliente, EjecutivoVentas ejecutivo, LocalDate fechaRegistro, LocalDate fechaEntrega) {
        this.cliente = cliente;
        this.ejecutivo = ejecutivo;
        this.fechaRegistro = fechaRegistro;
        this.fechaEntrega = fechaEntrega;
        this.items = new ItemPedido[30];
        this.totalItems = 0;
        this.historial = new HistorialEstado[60];
        this.totalHistorial = 0;
        this.estado = "Nuevo";
        this.situacionAdelanto = "Sin adelanto";
        this.defectuososPorTalla = new int[4];
    }

    // ---------- Ítems (arreglo de objetos) ----------
    public boolean agregarItem(ItemPedido item) {
        if (totalItems == items.length) {
            return false;
        }
        items[totalItems] = item;
        totalItems++;
        return true;
    }

    public boolean quitarItem(int posicion) {
        if (posicion < 0 || posicion >= totalItems) {
            return false;
        }
        for (int j = posicion; j < totalItems - 1; j++) {
            items[j] = items[j + 1];
        }
        items[totalItems - 1] = null;
        totalItems--;
        return true;
    }

    // ---------- Cantidades ----------
    public int getTotalPrendas() {
        int total = 0;
        for (int i = 0; i < totalItems; i++) {
            total += items[i].getTotalPrendas();
        }
        return total;
    }

    public double getMetrosNetos(Tela tela) {
        double metros = 0;
        for (int i = 0; i < totalItems; i++) {
            metros += items[i].getMetrosTela(tela);
        }
        return metros;
    }

    // Consumo por talla x cantidad, más 5% de merma
    public double getMetrosConMerma(Tela tela) {
        return getMetrosNetos(tela) * (1 + MERMA);
    }

    public double getCantidadInsumo(Insumo insumo) {
        double total = 0;
        for (int i = 0; i < totalItems; i++) {
            total += items[i].getCantidadInsumo(insumo);
        }
        return total;
    }

    public boolean requiereAcabado() {
        for (int i = 0; i < totalItems; i++) {
            if (items[i].requiereAcabado()) {
                return true;
            }
        }
        return false;
    }

    public boolean incluyeLogo() {
        for (int i = 0; i < totalItems; i++) {
            if (items[i].incluyeLogo()) {
                return true;
            }
        }
        return false;
    }

    // ---------- Montos (RF07) ----------
    // Suma los subtotales de los ítems de un tipo: "Prenda", "Servicio" o "Paquete"
    public double getSubtotalPorTipo(String tipoItem) {
        double total = 0;
        for (int i = 0; i < totalItems; i++) {
            if (items[i].getTipoItem().equals(tipoItem)) {
                total += items[i].calcularSubtotal();
            }
        }
        return total;
    }

    // Descuento polimórfico según el tipo de cliente (solo sobre prendas sueltas)
    public double calcularDescuento() {
        return cliente.calcularDescuento(getSubtotalPorTipo("Prenda"), getTotalPrendas(), incluyeLogo());
    }

    public double calcularSubtotal() {
        return getSubtotalPorTipo("Prenda") - calcularDescuento()
                + getSubtotalPorTipo("Servicio") + getSubtotalPorTipo("Paquete");
    }

    public double calcularIgv() {
        return calcularSubtotal() * IGV;
    }

    public double calcularTotal() {
        return calcularSubtotal() + calcularIgv();
    }

    public double calcularAdelantoRequerido() {
        return calcularTotal() * PORCENTAJE_ADELANTO;
    }

    public double calcularSaldo() {
        return calcularTotal() - adelanto;
    }

    public double calcularCostoProduccion() {
        double costo = 0;
        for (int i = 0; i < totalItems; i++) {
            costo += items[i].calcularCosto();
        }
        return costo;
    }

    // ---------- Estados (RF06) ----------
    public void cambiarEstado(String nuevoEstado, String usuario, String observacion, LocalDate fecha) {
        if (totalHistorial < historial.length) {
            LocalDateTime fechaHora = fecha.atTime(LocalTime.now().withNano(0));
            historial[totalHistorial] = new HistorialEstado(fechaHora, estado, nuevoEstado, usuario, observacion);
            totalHistorial++;
        }
        estado = nuevoEstado;
    }

    // Prendas a fabricar de una talla (0=S, 1=M, 2=L, 3=XL)
    public int getPrendasPorTalla(int indiceTalla) {
        int total = 0;
        for (int i = 0; i < totalItems; i++) {
            DetallePrenda[] prendas = items[i].getPrendas();
            for (int j = 0; j < prendas.length; j++) {
                if (ModeloPrenda.indiceTalla(prendas[j].getTalla()) == indiceTalla) {
                    total += prendas[j].getCantidad();
                }
            }
        }
        return total;
    }

    public boolean estaFinalizado() {
        return estado.equals(ENTREGADO) || estado.equals(CANCELADO);
    }

    // Ya se descontó la tela del inventario
    public boolean yaFueCortado() {
        return estado.equals(EN_CORTE) || estado.equals(EN_CONFECCION) || estado.equals(EN_ACABADO)
                || estado.equals(LISTO_DESPACHO) || estado.equals(ENTREGADO) || metrosConsumidos > 0;
    }

    public void registrarDefectuosos(int[] defectuosos) {
        for (int i = 0; i < defectuososPorTalla.length; i++) {
            defectuososPorTalla[i] += defectuosos[i];
        }
    }

    public int getTotalDefectuosos() {
        int total = 0;
        for (int i = 0; i < defectuososPorTalla.length; i++) {
            total += defectuososPorTalla[i];
        }
        return total;
    }

    public boolean fueEntregadoATiempo() {
        return estado.equals(ENTREGADO) && fechaDespacho != null && !fechaDespacho.isAfter(fechaEntrega);
    }

    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }
    public String getNumeroTexto() { return String.format("PED-%04d", numero); }
    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public LocalDate getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(LocalDate fechaEntrega) { this.fechaEntrega = fechaEntrega; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public EjecutivoVentas getEjecutivo() { return ejecutivo; }
    public JefeProduccion getJefeProduccion() { return jefeProduccion; }
    public void setJefeProduccion(JefeProduccion jefeProduccion) { this.jefeProduccion = jefeProduccion; }
    public ItemPedido[] getItems() { return items; }
    public int getTotalItems() { return totalItems; }
    public String getEstado() { return estado; }
    public HistorialEstado[] getHistorial() { return historial; }
    public int getTotalHistorial() { return totalHistorial; }
    public double getAdelanto() { return adelanto; }
    public void setAdelanto(double adelanto) { this.adelanto = adelanto; }
    public String getSituacionAdelanto() { return situacionAdelanto; }
    public void setSituacionAdelanto(String situacionAdelanto) { this.situacionAdelanto = situacionAdelanto; }
    public int[] getDefectuososPorTalla() { return defectuososPorTalla; }
    public double getMetrosConsumidos() { return metrosConsumidos; }
    public void setMetrosConsumidos(double metrosConsumidos) { this.metrosConsumidos = metrosConsumidos; }
    public double getMetrosMerma() { return metrosMerma; }
    public void setMetrosMerma(double metrosMerma) { this.metrosMerma = metrosMerma; }
    public LocalDate getFechaDespacho() { return fechaDespacho; }
    public void setFechaDespacho(LocalDate fechaDespacho) { this.fechaDespacho = fechaDespacho; }
    public Comprobante getComprobante() { return comprobante; }
    public void setComprobante(Comprobante comprobante) { this.comprobante = comprobante; }

    @Override
    public String toString() {
        return getNumeroTexto() + " - " + cliente.getNombreCompleto();
    }
}
