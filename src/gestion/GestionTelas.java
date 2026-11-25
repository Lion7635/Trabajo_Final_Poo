package gestion;

import clases.inventario.Tela;
import utilitario.Validador;

/**
 * RF04 - Gestión de telas.
 */
public class GestionTelas {

    private Tela[] telas;
    private int totalTelas;

    public GestionTelas() {
        telas = new Tela[100];
        totalTelas = 0;

        // Datos iniciales: se registran al crear la gestión (igual que en los laboratorios)
        // código, tipo, color, metros en stock, stock mínimo, costo por metro
        registrar(new Tela("T01", "Jersey Algodón Pima", "Negro", 1400, 250, 14.50));
        registrar(new Tela("T02", "Jersey Algodón Pima", "Blanco", 1200, 250, 14.50));
        registrar(new Tela("T03", "Jersey Algodón Pima", "Azul marino", 300, 150, 14.50));
        registrar(new Tela("T04", "Piqué Algodón", "Blanco", 900, 200, 12.00));
        registrar(new Tela("T05", "Piqué Algodón", "Azul marino", 1000, 200, 12.00));
        registrar(new Tela("T06", "Denim 12 oz", "Azul índigo", 1100, 250, 18.00));
        registrar(new Tela("T07", "Denim 12 oz", "Negro", 300, 200, 18.00));
        registrar(new Tela("T08", "Drill Poliéster", "Negro", 800, 150, 11.00));
        registrar(new Tela("T09", "Drill Poliéster", "Azul marino", 400, 150, 11.00));
        registrar(new Tela("T10", "Polar", "Azul marino", 250, 120, 15.00));
    }

    public Tela buscarPorCodigo(String codigo) {
        for (int i = 0; i < totalTelas; i++) {
            if (telas[i].getCodigo().equalsIgnoreCase(codigo)) {
                return telas[i];
            }
        }
        return null;
    }

    public Tela buscarPorTipoYColor(String tipo, String color) {
        for (int i = 0; i < totalTelas; i++) {
            if (telas[i].getTipo().equalsIgnoreCase(tipo) && telas[i].getColor().equalsIgnoreCase(color)) {
                return telas[i];
            }
        }
        return null;
    }

    private String validarDatos(Tela t) {
        if (Validador.esVacio(t.getCodigo()) || Validador.esVacio(t.getTipo()) || Validador.esVacio(t.getColor())) {
            return "Código, tipo y color son obligatorios.";
        }
        if (t.getStock() < 0 || t.getStockMinimo() < 0) {
            return "El stock y el stock mínimo no pueden ser negativos.";
        }
        if (t.getCostoMetro() <= 0) {
            return "El costo por metro debe ser mayor a 0.";
        }
        return null;
    }

    public String registrar(Tela t) {
        String error = validarDatos(t);
        if (error != null) {
            return error;
        }
        if (totalTelas == telas.length) {
            return "No hay espacio para más telas.";
        }
        if (buscarPorCodigo(t.getCodigo()) != null) {
            return "Ya existe una tela con el código " + t.getCodigo() + ".";
        }
        if (buscarPorTipoYColor(t.getTipo(), t.getColor()) != null) {
            return "Ya existe la tela " + t.getDescripcion() + ".";
        }
        telas[totalTelas] = t;
        totalTelas++;
        return null;
    }

    public String actualizar(Tela datos) {
        String error = validarDatos(datos);
        if (error != null) {
            return error;
        }
        Tela igual = buscarPorTipoYColor(datos.getTipo(), datos.getColor());
        if (igual != null && !igual.getCodigo().equalsIgnoreCase(datos.getCodigo())) {
            return "Ya existe la tela " + datos.getDescripcion() + " con otro código.";
        }
        Tela t = buscarPorCodigo(datos.getCodigo());
        if (t == null) {
            return "No existe una tela con el código " + datos.getCodigo() + ".";
        }
        t.setTipo(datos.getTipo());
        t.setColor(datos.getColor());
        t.setStock(datos.getStock());
        t.setStockMinimo(datos.getStockMinimo());
        t.setCostoUnitario(datos.getCostoMetro());
        return null;
    }

    public String eliminar(String codigo, GestionPedidos gestionPedidos) {
        for (int i = 0; i < totalTelas; i++) {
            if (telas[i].getCodigo().equalsIgnoreCase(codigo)) {
                if (gestionPedidos.usaTela(telas[i])) {
                    return "No se puede eliminar: la tela está en pedidos en curso.";
                }
                for (int j = i; j < totalTelas - 1; j++) {
                    telas[j] = telas[j + 1];
                }
                telas[totalTelas - 1] = null;
                totalTelas--;
                return null;
            }
        }
        return "No existe una tela con el código " + codigo + ".";
    }

    // Tipos de tela sin repetir (para los combos)
    public String[] getTiposDistintos() {
        String[] temp = new String[totalTelas];
        int c = 0;
        for (int i = 0; i < totalTelas; i++) {
            boolean repetido = false;
            for (int j = 0; j < c; j++) {
                if (temp[j].equalsIgnoreCase(telas[i].getTipo())) {
                    repetido = true;
                }
            }
            if (!repetido) {
                temp[c] = telas[i].getTipo();
                c++;
            }
        }
        String[] tipos = new String[c];
        for (int i = 0; i < c; i++) {
            tipos[i] = temp[i];
        }
        return tipos;
    }

    // Colores disponibles de un tipo de tela (tipo = null para todos)
    public String[] getColores(String tipo) {
        String[] temp = new String[totalTelas];
        int c = 0;
        for (int i = 0; i < totalTelas; i++) {
            if (tipo == null || telas[i].getTipo().equalsIgnoreCase(tipo)) {
                boolean repetido = false;
                for (int j = 0; j < c; j++) {
                    if (temp[j].equalsIgnoreCase(telas[i].getColor())) {
                        repetido = true;
                    }
                }
                if (!repetido) {
                    temp[c] = telas[i].getColor();
                    c++;
                }
            }
        }
        String[] colores = new String[c];
        for (int i = 0; i < c; i++) {
            colores[i] = temp[i];
        }
        return colores;
    }

    public Tela[] getTelas() { return telas; }
    public int getTotalTelas() { return totalTelas; }
}
