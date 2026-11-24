package gestion;

import clases.catalogo.Paquete;
import clases.catalogo.ServicioAcabado;
import clases.clientes.Cliente;
import clases.comprobantes.Comprobante;
import clases.comprobantes.Factura;
import clases.empleados.Empleado;
import clases.empleados.JefeProduccion;
import clases.inventario.Insumo;
import clases.inventario.Tela;
import clases.pedidos.DetallePaquete;
import clases.pedidos.DetallePrenda;
import clases.pedidos.DetalleServicio;
import clases.pedidos.ItemPedido;
import clases.pedidos.Pedido;
import clases.prendas.ModeloPrenda;
import java.time.DayOfWeek;
import java.time.LocalDate;
import utilitario.Validador;

/**
 * RF05 y RF06 - Registro de pedidos y control de su flujo operativo.
 * Convención: los métodos devuelven null si todo salió bien o el texto con la causa del rechazo.
 */
public class GestionPedidos {

    public static final int PRENDAS_MINIMAS = 50;
    public static final int DIAS_MINIMOS_ENTREGA = 7;
    public static final int CAPACIDAD_PRENDAS_POR_DIA = 300;   // capacidad de la planta

    private Pedido[] pedidos;
    private int totalPedidos;
    private int correlativo;          // número automático del siguiente pedido

    private GestionTelas gestionTelas;
    private GestionInsumos gestionInsumos;
    private GestionComprobantes gestionComprobantes;

    public GestionPedidos(GestionTelas gestionTelas, GestionInsumos gestionInsumos, GestionComprobantes gestionComprobantes) {
        this.pedidos = new Pedido[300];
        this.totalPedidos = 0;
        this.correlativo = 0;
        this.gestionTelas = gestionTelas;
        this.gestionInsumos = gestionInsumos;
        this.gestionComprobantes = gestionComprobantes;
    }

    public Pedido buscarPorNumero(int numero) {
        for (int i = 0; i < totalPedidos; i++) {
            if (pedidos[i].getNumero() == numero) {
                return pedidos[i];
            }
        }
        return null;
    }

    // =====================================================================
    //  RF05 - REGISTRO Y VALIDACIONES
    // =====================================================================

    public String registrar(Pedido p, LocalDate hoy) {
        String error = validar(p, hoy);
        if (error != null) {
            return error;
        }
        if (totalPedidos == pedidos.length) {
            return "No hay espacio para más pedidos.";
        }
        correlativo++;
        p.setNumero(correlativo);
        p.setFechaRegistro(hoy);
        p.cambiarEstado(Pedido.REGISTRADO, p.getEjecutivo().getUsuario(), "Pedido registrado", hoy);
        pedidos[totalPedidos] = p;
        totalPedidos++;
        return null;
    }

    // Revisa todas las reglas y junta todas las causas de rechazo
    public String validar(Pedido p, LocalDate hoy) {
        String errores = "";
        if (p.getCliente() == null) {
            errores += "- Seleccione un cliente.\n";
        }
        if (p.getTotalItems() == 0) {
            return "- El pedido no tiene prendas.";
        }

        // Modelos descontinuados
        for (int i = 0; i < p.getTotalItems(); i++) {
            DetallePrenda[] prendas = p.getItems()[i].getPrendas();
            for (int j = 0; j < prendas.length; j++) {
                if (!prendas[j].getModelo().isActivo()) {
                    errores += "- El modelo " + prendas[j].getModelo().getNombre() + " está descontinuado.\n";
                }
            }
        }

        // (d) mínimo de prendas y anticipación de la entrega
        int totalPrendas = p.getTotalPrendas();
        if (totalPrendas < PRENDAS_MINIMAS) {
            errores += "- El pedido debe tener al menos " + PRENDAS_MINIMAS + " prendas (tiene " + totalPrendas + ").\n";
        }
        LocalDate minima = hoy.plusDays(DIAS_MINIMOS_ENTREGA);
        if (p.getFechaEntrega() == null || p.getFechaEntrega().isBefore(minima)) {
            errores += "- La fecha de entrega debe ser desde el " + Validador.fecha(minima)
                    + " (mínimo " + DIAS_MINIMOS_ENTREGA + " días).\n";
        }

        // (a) tela suficiente: consumo por talla x cantidad + 5% de merma
        for (int i = 0; i < gestionTelas.getTotalTelas(); i++) {
            Tela t = gestionTelas.getTelas()[i];
            double necesario = p.getMetrosConMerma(t);
            if (necesario > 0) {
                double disponible = t.getStock() - metrosComprometidos(t, p);
                if (necesario > disponible + 0.0001) {
                    errores += String.format("- Faltan %.1f m de %s (necesita %.1f m, disponible %.1f m).%n",
                            necesario - disponible, t.getDescripcion(), necesario, Math.max(disponible, 0));
                }
            }
        }

        // (b) insumos suficientes
        for (int i = 0; i < gestionInsumos.getTotalInsumos(); i++) {
            Insumo in = gestionInsumos.getInsumos()[i];
            double necesario = p.getCantidadInsumo(in);
            if (necesario > 0) {
                double disponible = in.getStock() - insumoComprometido(in, p);
                if (necesario > disponible + 0.0001) {
                    errores += String.format("- Faltan %.0f %s de %s (necesita %.0f, disponible %.0f).%n",
                            necesario - disponible, in.getUnidad(), in.getNombre(), necesario, Math.max(disponible, 0));
                }
            }
        }

        // (c) capacidad de la planta
        if (p.getFechaEntrega() != null && !p.getFechaEntrega().isBefore(minima)) {
            int dias = contarDiasHabiles(hoy, p.getFechaEntrega());
            int comprometidas = prendasComprometidas(p.getFechaEntrega(), p);
            int capacidad = dias * CAPACIDAD_PRENDAS_POR_DIA - comprometidas;
            if (totalPrendas > capacidad) {
                errores += "- Capacidad de planta insuficiente: hasta el " + Validador.fecha(p.getFechaEntrega())
                        + " se pueden producir " + Math.max(capacidad, 0) + " prendas (" + dias + " días hábiles x "
                        + CAPACIDAD_PRENDAS_POR_DIA + " - " + comprometidas + " ya comprometidas) y el pedido tiene "
                        + totalPrendas + ".\n";
            }
        }

        if (errores.isEmpty()) {
            return null;
        }
        return errores;
    }

    // Tela reservada por pedidos registrados o confirmados que todavía no se cortan
    public double metrosComprometidos(Tela t, Pedido excluir) {
        double total = 0;
        for (int i = 0; i < totalPedidos; i++) {
            Pedido p = pedidos[i];
            if (p != excluir && (p.getEstado().equals(Pedido.REGISTRADO) || p.getEstado().equals(Pedido.CONFIRMADO))) {
                total += p.getMetrosConMerma(t);
            }
        }
        return total;
    }

    public double insumoComprometido(Insumo in, Pedido excluir) {
        double total = 0;
        for (int i = 0; i < totalPedidos; i++) {
            Pedido p = pedidos[i];
            if (p != excluir && (p.getEstado().equals(Pedido.REGISTRADO) || p.getEstado().equals(Pedido.CONFIRMADO))) {
                total += p.getCantidadInsumo(in);
            }
        }
        return total;
    }

    // Prendas de pedidos en curso que se entregan hasta esa fecha
    public int prendasComprometidas(LocalDate hasta, Pedido excluir) {
        int total = 0;
        for (int i = 0; i < totalPedidos; i++) {
            Pedido p = pedidos[i];
            if (p != excluir && !p.estaFinalizado() && !p.getEstado().equals(Pedido.LISTO_DESPACHO)
                    && !p.getFechaEntrega().isAfter(hasta)) {
                total += p.getTotalPrendas();
            }
        }
        return total;
    }

    // Días de lunes a sábado entre mañana y la fecha de entrega (Gamarra trabaja los sábados)
    public static int contarDiasHabiles(LocalDate desde, LocalDate hasta) {
        int dias = 0;
        LocalDate d = desde.plusDays(1);
        while (!d.isAfter(hasta)) {
            if (d.getDayOfWeek() != DayOfWeek.SUNDAY) {
                dias++;
            }
            d = d.plusDays(1);
        }
        return dias;
    }

    // Arma la línea de prenda buscando la tela del modelo en el color pedido
    public String crearDetallePrenda(Pedido p, ModeloPrenda modelo, String color, String talla, int cantidad) {
        if (cantidad <= 0) {
            return "La cantidad debe ser mayor a 0.";
        }
        if (!modelo.isActivo()) {
            return "El modelo " + modelo.getNombre() + " está descontinuado y no puede incluirse en pedidos.";
        }
        Tela tela = gestionTelas.buscarPorTipoYColor(modelo.getTipoTela(), color);
        if (tela == null) {
            return "No existe la tela " + modelo.getTipoTela() + " color " + color + ".";
        }
        if (!p.agregarItem(new DetallePrenda(modelo, tela, talla, cantidad))) {
            return "El pedido llegó al máximo de líneas.";
        }
        return null;
    }

    public String crearDetalleServicio(Pedido p, ServicioAcabado servicio, int cantidadPrendas) {
        if (cantidadPrendas <= 0) {
            return "La cantidad de prendas debe ser mayor a 0.";
        }
        if (!p.agregarItem(new DetalleServicio(servicio, cantidadPrendas))) {
            return "El pedido llegó al máximo de líneas.";
        }
        return null;
    }

    public String crearDetallePaquete(Pedido p, Paquete paquete, String color, String talla, int cantidad) {
        if (cantidad <= 0) {
            return "La cantidad debe ser mayor a 0.";
        }
        if (!paquete.estaDisponible()) {
            return "El paquete " + paquete.getNombre() + " tiene modelos descontinuados.";
        }
        Tela[] telas = new Tela[paquete.getTotalModelos()];
        for (int i = 0; i < paquete.getTotalModelos(); i++) {
            ModeloPrenda m = paquete.getModelos()[i];
            telas[i] = gestionTelas.buscarPorTipoYColor(m.getTipoTela(), color);
            if (telas[i] == null) {
                return "No existe la tela " + m.getTipoTela() + " color " + color + " para " + m.getNombre() + ".";
            }
        }
        if (!p.agregarItem(new DetallePaquete(paquete, talla, color, telas, cantidad))) {
            return "El pedido llegó al máximo de líneas.";
        }
        return null;
    }

    // =====================================================================
    //  RF06 - FLUJO DEL PEDIDO
    // =====================================================================

    private String validarModificable(Pedido p) {
        if (p == null) {
            return "Seleccione un pedido.";
        }
        if (p.estaFinalizado()) {
            return "El pedido " + p.getNumeroTexto() + " está " + p.getEstado() + " y ya no puede modificarse.";
        }
        return null;
    }

    // Confirmación: el ejecutivo registra el adelanto del 50%
    public String confirmar(Pedido p, double monto, Empleado usuario, LocalDate fecha) {
        String error = validarModificable(p);
        if (error != null) {
            return error;
        }
        if (!p.getEstado().equals(Pedido.REGISTRADO)) {
            return "Solo se confirman pedidos en estado Registrado.";
        }
        double requerido = Validador.redondear(p.calcularAdelantoRequerido());
        if (monto < requerido - 0.01) {
            return "El adelanto mínimo es " + Validador.dinero(requerido) + " (50% del total).";
        }
        if (monto > p.calcularTotal() + 0.01) {
            return "El adelanto no puede superar el total del pedido.";
        }
        p.setAdelanto(monto);
        p.setSituacionAdelanto("Pagado");
        p.cambiarEstado(Pedido.CONFIRMADO, usuario.getUsuario(), "Adelanto de " + Validador.dinero(monto), fecha);
        return null;
    }

    // Corte: se descuentan la tela (con merma) y los insumos del inventario
    public String iniciarCorte(Pedido p, JefeProduccion jefe, LocalDate fecha) {
        String error = validarModificable(p);
        if (error != null) {
            return error;
        }
        if (!p.getEstado().equals(Pedido.CONFIRMADO)) {
            return "Solo se cortan pedidos Confirmados (con adelanto pagado).";
        }
        // Se revisa el stock real antes de descontar
        String faltantes = "";
        for (int i = 0; i < gestionTelas.getTotalTelas(); i++) {
            Tela t = gestionTelas.getTelas()[i];
            double necesario = p.getMetrosConMerma(t);
            if (necesario > 0 && !t.haySuficiente(necesario)) {
                faltantes += String.format("- Faltan %.1f m de %s.%n", necesario - t.getStock(), t.getDescripcion());
            }
        }
        for (int i = 0; i < gestionInsumos.getTotalInsumos(); i++) {
            Insumo in = gestionInsumos.getInsumos()[i];
            double necesario = p.getCantidadInsumo(in);
            if (necesario > 0 && !in.haySuficiente(necesario)) {
                faltantes += String.format("- Faltan %.0f %s de %s.%n", necesario - in.getStock(), in.getUnidad(), in.getNombre());
            }
        }
        if (!faltantes.isEmpty()) {
            return "No hay stock para cortar:\n" + faltantes;
        }
        double metrosTotales = 0;
        double metrosNetos = 0;
        for (int i = 0; i < gestionTelas.getTotalTelas(); i++) {
            Tela t = gestionTelas.getTelas()[i];
            double necesario = p.getMetrosConMerma(t);
            if (necesario > 0) {
                t.descontar(necesario);
                metrosTotales += necesario;
                metrosNetos += p.getMetrosNetos(t);
            }
        }
        for (int i = 0; i < gestionInsumos.getTotalInsumos(); i++) {
            Insumo in = gestionInsumos.getInsumos()[i];
            double necesario = p.getCantidadInsumo(in);
            if (necesario > 0) {
                in.descontar(necesario);
            }
        }
        p.setMetrosConsumidos(metrosTotales);
        p.setMetrosMerma(metrosTotales - metrosNetos);
        p.setJefeProduccion(jefe);
        p.cambiarEstado(Pedido.EN_CORTE, jefe.getUsuario(),
                String.format("Corte iniciado: %.1f m de tela (merma %.1f m)", metrosTotales, metrosTotales - metrosNetos), fecha);
        return null;
    }

    // Avance: En Corte -> En Confección -> En Acabado (solo si lleva estampado o bordado)
    public String registrarAvance(Pedido p, Empleado usuario, LocalDate fecha) {
        String error = validarModificable(p);
        if (error != null) {
            return error;
        }
        if (p.getEstado().equals(Pedido.EN_CORTE)) {
            p.cambiarEstado(Pedido.EN_CONFECCION, usuario.getUsuario(), "Corte terminado, inicia confección", fecha);
            return null;
        }
        if (p.getEstado().equals(Pedido.EN_CONFECCION)) {
            if (p.requiereAcabado()) {
                p.cambiarEstado(Pedido.EN_ACABADO, usuario.getUsuario(), "Confección terminada, pasa a estampado/bordado", fecha);
                return null;
            }
            return "Este pedido no lleva estampado ni bordado: registre el control de calidad.";
        }
        if (p.getEstado().equals(Pedido.EN_ACABADO)) {
            return "El acabado está en curso: registre el control de calidad.";
        }
        return "El pedido está " + p.getEstado() + ": no tiene avance de producción por registrar.";
    }

    public boolean puedeControlCalidad(Pedido p) {
        return p.getEstado().equals(Pedido.EN_ACABADO)
                || (p.getEstado().equals(Pedido.EN_CONFECCION) && !p.requiereAcabado());
    }

    // Control de calidad: con defectos vuelve a confección; sin defectos pasa a Listo para Despacho
    public String registrarControlCalidad(Pedido p, int[] defectuosos, Empleado usuario, LocalDate fecha) {
        String error = validarModificable(p);
        if (error != null) {
            return error;
        }
        if (!puedeControlCalidad(p)) {
            if (p.getEstado().equals(Pedido.EN_CONFECCION)) {
                return "Primero registre el avance a En Acabado (el pedido lleva estampado o bordado).";
            }
            return "El control de calidad se registra al terminar la confección o el acabado.";
        }
        int total = 0;
        String detalle = "";
        for (int i = 0; i < defectuosos.length; i++) {
            if (defectuosos[i] < 0) {
                return "Las cantidades defectuosas no pueden ser negativas.";
            }
            if (defectuosos[i] > p.getPrendasPorTalla(i)) {
                return "La talla " + ModeloPrenda.TALLAS[i] + " solo tiene " + p.getPrendasPorTalla(i) + " prendas.";
            }
            total += defectuosos[i];
            if (defectuosos[i] > 0) {
                detalle += ModeloPrenda.TALLAS[i] + ":" + defectuosos[i] + " ";
            }
        }
        if (total > 0) {
            p.registrarDefectuosos(defectuosos);
            p.cambiarEstado(Pedido.EN_CONFECCION, usuario.getUsuario(),
                    total + " prendas defectuosas (" + detalle.trim() + ") vuelven a confección", fecha);
        } else {
            p.cambiarEstado(Pedido.LISTO_DESPACHO, usuario.getUsuario(), "Control de calidad aprobado", fecha);
        }
        return null;
    }

    // Despacho: se emite el comprobante y el pedido pasa a Entregado
    public String despachar(Pedido p, String tipoComprobante, String ruc, String razonSocial, Empleado usuario, LocalDate fecha) {
        String error = validarModificable(p);
        if (error != null) {
            return error;
        }
        if (!p.getEstado().equals(Pedido.LISTO_DESPACHO)) {
            return "Solo se despachan pedidos Listos para Despacho.";
        }
        Comprobante c;
        if (tipoComprobante.equals("Factura")) {
            if (!Factura.esRucValido(ruc)) {
                return "Para emitir Factura ingrese un RUC válido (11 dígitos, empieza en 10 o 20).";
            }
            if (Validador.esVacio(razonSocial)) {
                return "Para emitir Factura ingrese la razón social.";
            }
            c = gestionComprobantes.emitirFactura(p, fecha, ruc, razonSocial.trim());
        } else {
            c = gestionComprobantes.emitirBoleta(p, fecha);
        }
        p.setComprobante(c);
        p.setFechaDespacho(fecha);
        p.cambiarEstado(Pedido.ENTREGADO, usuario.getUsuario(), c.getTipo() + " " + c.getNumero()
                + " - saldo cobrado " + Validador.dinero(c.getSaldo()), fecha);
        return null;
    }

    // Cancelación: antes del corte se devuelve el adelanto; después queda retenido
    public String cancelar(Pedido p, String motivo, Empleado usuario, LocalDate fecha) {
        String error = validarModificable(p);
        if (error != null) {
            return error;
        }
        String obs;
        if (p.getAdelanto() == 0) {
            obs = "Cancelado sin adelanto";
        } else if (!p.yaFueCortado()) {
            p.setSituacionAdelanto("Devuelto");
            obs = "Se devuelve el adelanto de " + Validador.dinero(p.getAdelanto());
        } else {
            p.setSituacionAdelanto("Retenido");
            obs = "Adelanto de " + Validador.dinero(p.getAdelanto()) + " retenido (ya se cortó la tela)";
        }
        if (!Validador.esVacio(motivo)) {
            obs += ". Motivo: " + motivo.trim();
        }
        p.cambiarEstado(Pedido.CANCELADO, usuario.getUsuario(), obs, fecha);
        return null;
    }

    // =====================================================================
    //  CONSULTAS
    // =====================================================================

    public Pedido[] listarPorEstado(String estado) {
        int c = 0;
        for (int i = 0; i < totalPedidos; i++) {
            if (pedidos[i].getEstado().equals(estado)) {
                c++;
            }
        }
        Pedido[] lista = new Pedido[c];
        int k = 0;
        for (int i = 0; i < totalPedidos; i++) {
            if (pedidos[i].getEstado().equals(estado)) {
                lista[k] = pedidos[i];
                k++;
            }
        }
        return lista;
    }

    public boolean tienePedidosActivos(Cliente cliente) {
        for (int i = 0; i < totalPedidos; i++) {
            if (pedidos[i].getCliente() == cliente && !pedidos[i].estaFinalizado()) {
                return true;
            }
        }
        return false;
    }

    public boolean usaModelo(ModeloPrenda modelo) {
        for (int i = 0; i < totalPedidos; i++) {
            for (int j = 0; j < pedidos[i].getTotalItems(); j++) {
                DetallePrenda[] prendas = pedidos[i].getItems()[j].getPrendas();
                for (int k = 0; k < prendas.length; k++) {
                    if (prendas[k].getModelo() == modelo) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean usaTela(Tela tela) {
        for (int i = 0; i < totalPedidos; i++) {
            if (!pedidos[i].estaFinalizado() && pedidos[i].getMetrosNetos(tela) > 0) {
                return true;
            }
        }
        return false;
    }

    public boolean usaServicio(ServicioAcabado servicio) {
        for (int i = 0; i < totalPedidos; i++) {
            for (int j = 0; j < pedidos[i].getTotalItems(); j++) {
                ItemPedido item = pedidos[i].getItems()[j];
                if (item instanceof DetalleServicio && ((DetalleServicio) item).getServicio() == servicio) {
                    return true;
                }
            }
        }
        return false;
    }

    public Pedido[] getPedidos() { return pedidos; }
    public int getTotalPedidos() { return totalPedidos; }
}
