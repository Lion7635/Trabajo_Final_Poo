package gestion;

import clases.inventario.Insumo;
import clases.prendas.Casaca;
import clases.prendas.Jean;
import clases.prendas.ModeloPrenda;
import clases.prendas.Polo;
import utilitario.Validador;

/**
 * RF02 - Gestión de modelos de prendas.
 * El arreglo es de tipo ModeloPrenda (clase padre) y guarda Polos, Jeans y Casacas.
 */
public class GestionModelos {

    private ModeloPrenda[] modelos;
    private int totalModelos;

    public GestionModelos(GestionInsumos gestionInsumos) {
        modelos = new ModeloPrenda[100];
        totalModelos = 0;

        // Datos iniciales: se registran al crear la gestión (igual que en los laboratorios)
        // Los insumos ya están registrados en GestionInsumos: se buscan por su código
        Insumo hilo = gestionInsumos.buscarPorCodigo("I01");
        Insumo etiqueta = gestionInsumos.buscarPorCodigo("I02");
        Insumo boton = gestionInsumos.buscarPorCodigo("I03");
        Insumo cierre = gestionInsumos.buscarPorCodigo("I04");
        Insumo broche = gestionInsumos.buscarPorCodigo("I05");
        Insumo remache = gestionInsumos.buscarPorCodigo("I06");

        // consumo de tela por talla S, M, L, XL; tiempo (min); precio; costo de costura; activo
        Polo poloPima = new Polo("M01", "Polo Pima Clásico", "Jersey Algodón Pima",
                new double[]{1.10, 1.20, 1.30, 1.45}, 25, 35.00, 4.50, true);
        poloPima.agregarInsumo(hilo, 0.04);
        poloPima.agregarInsumo(etiqueta, 1);
        registrar(poloPima);

        Polo poloPique = new Polo("M02", "Polo Piqué Cuello Camisero", "Piqué Algodón",
                new double[]{1.20, 1.30, 1.40, 1.55}, 35, 42.00, 6.00, true);
        poloPique.agregarInsumo(hilo, 0.05);
        poloPique.agregarInsumo(etiqueta, 1);
        poloPique.agregarInsumo(boton, 3);
        registrar(poloPique);

        Jean jean = new Jean("M03", "Jean Clásico 5 Bolsillos", "Denim 12 oz",
                new double[]{1.30, 1.40, 1.50, 1.65}, 45, 89.00, 9.00, true, 6.50);
        jean.agregarInsumo(hilo, 0.08);
        jean.agregarInsumo(etiqueta, 1);
        jean.agregarInsumo(boton, 1);
        jean.agregarInsumo(remache, 6);
        registrar(jean);

        Casaca casacaDrill = new Casaca("M04", "Casaca Drill Corporativa", "Drill Poliéster",
                new double[]{1.80, 1.95, 2.10, 2.30}, 70, 129.00, 14.00, true, 9.50, 4.00);
        casacaDrill.agregarInsumo(hilo, 0.10);
        casacaDrill.agregarInsumo(etiqueta, 1);
        casacaDrill.agregarInsumo(cierre, 1);
        casacaDrill.agregarInsumo(broche, 4);
        registrar(casacaDrill);

        Casaca casacaPolar = new Casaca("M05", "Casaca Polar Escolar", "Polar",
                new double[]{1.60, 1.70, 1.85, 2.00}, 55, 95.00, 10.00, true, 0.00, 3.00);
        casacaPolar.agregarInsumo(hilo, 0.08);
        casacaPolar.agregarInsumo(etiqueta, 1);
        casacaPolar.agregarInsumo(cierre, 1);
        registrar(casacaPolar);

        Polo poloBasico = new Polo("M06", "Polo Básico Cuello Redondo", "Jersey Algodón Pima",
                new double[]{1.00, 1.10, 1.20, 1.30}, 20, 25.00, 3.50, false);   // Descontinuado
        poloBasico.agregarInsumo(etiqueta, 1);
        registrar(poloBasico);
    }

    public ModeloPrenda buscarPorCodigo(String codigo) {
        for (int i = 0; i < totalModelos; i++) {
            if (modelos[i].getCodigo().equalsIgnoreCase(codigo)) {
                return modelos[i];
            }
        }
        return null;
    }

    private String validarDatos(ModeloPrenda m) {
        if (Validador.esVacio(m.getCodigo()) || Validador.esVacio(m.getNombre())) {
            return "Código y nombre son obligatorios.";
        }
        if (Validador.esVacio(m.getTipoTela())) {
            return "Seleccione la tela principal.";
        }
        for (int i = 0; i < m.getConsumoPorTalla().length; i++) {
            if (m.getConsumoPorTalla()[i] <= 0) {
                return "El consumo de tela de la talla " + ModeloPrenda.TALLAS[i] + " debe ser mayor a 0.";
            }
        }
        if (m.getTiempoConfeccion() <= 0) {
            return "El tiempo de confección debe ser mayor a 0.";
        }
        if (m.getPrecioVenta() <= 0 || m.getCostoCostura() < 0) {
            return "El precio de venta debe ser mayor a 0 y el costo de costura no negativo.";
        }
        return null;
    }

    public String registrar(ModeloPrenda m) {
        String error = validarDatos(m);
        if (error != null) {
            return error;
        }
        if (totalModelos == modelos.length) {
            return "No hay espacio para más modelos.";
        }
        if (buscarPorCodigo(m.getCodigo()) != null) {
            return "Ya existe un modelo con el código " + m.getCodigo() + ".";
        }
        modelos[totalModelos] = m;
        totalModelos++;
        return null;
    }

    public String actualizar(ModeloPrenda datos, GestionPedidos gestionPedidos, GestionPaquetes gestionPaquetes) {
        String error = validarDatos(datos);
        if (error != null) {
            return error;
        }
        for (int i = 0; i < totalModelos; i++) {
            ModeloPrenda m = modelos[i];
            if (m.getCodigo().equalsIgnoreCase(datos.getCodigo())) {
                if (m.getClass() != datos.getClass()) {
                    if (gestionPedidos.usaModelo(m) || gestionPaquetes.usaModelo(m)) {
                        return "No se puede cambiar el tipo: el modelo está en pedidos o paquetes.";
                    }
                    modelos[i] = datos;
                    return null;
                }
                m.setNombre(datos.getNombre());
                m.setTipoTela(datos.getTipoTela());
                m.setConsumoPorTalla(datos.getConsumoPorTalla());
                m.setTiempoConfeccion(datos.getTiempoConfeccion());
                m.setPrecioVenta(datos.getPrecioVenta());
                m.setCostoCostura(datos.getCostoCostura());
                m.setActivo(datos.isActivo());
                m.limpiarInsumos();
                m.copiarInsumosDe(datos);
                // Atributos propios de cada subclase (casting)
                if (m instanceof Jean) {
                    ((Jean) m).setCostoLavado(((Jean) datos).getCostoLavado());
                }
                if (m instanceof Casaca) {
                    ((Casaca) m).setCostoForro(((Casaca) datos).getCostoForro());
                    ((Casaca) m).setCostoAvios(((Casaca) datos).getCostoAvios());
                }
                return null;
            }
        }
        return "No existe un modelo con el código " + datos.getCodigo() + ".";
    }

    public String eliminar(String codigo, GestionPedidos gestionPedidos, GestionPaquetes gestionPaquetes) {
        for (int i = 0; i < totalModelos; i++) {
            if (modelos[i].getCodigo().equalsIgnoreCase(codigo)) {
                if (gestionPedidos.usaModelo(modelos[i])) {
                    return "No se puede eliminar: el modelo está en pedidos. Márquelo como Descontinuado.";
                }
                if (gestionPaquetes.usaModelo(modelos[i])) {
                    return "No se puede eliminar: el modelo forma parte de un paquete.";
                }
                for (int j = i; j < totalModelos - 1; j++) {
                    modelos[j] = modelos[j + 1];
                }
                modelos[totalModelos - 1] = null;
                totalModelos--;
                return null;
            }
        }
        return "No existe un modelo con el código " + codigo + ".";
    }

    public ModeloPrenda[] listarActivos() {
        int c = 0;
        for (int i = 0; i < totalModelos; i++) {
            if (modelos[i].isActivo()) {
                c++;
            }
        }
        ModeloPrenda[] activos = new ModeloPrenda[c];
        int k = 0;
        for (int i = 0; i < totalModelos; i++) {
            if (modelos[i].isActivo()) {
                activos[k] = modelos[i];
                k++;
            }
        }
        return activos;
    }

    public boolean usaTipoTela(String tipoTela) {
        for (int i = 0; i < totalModelos; i++) {
            if (modelos[i].getTipoTela().equalsIgnoreCase(tipoTela)) {
                return true;
            }
        }
        return false;
    }

    public ModeloPrenda[] getModelos() { return modelos; }
    public int getTotalModelos() { return totalModelos; }
}
