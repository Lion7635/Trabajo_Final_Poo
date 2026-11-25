package gui.mantenimiento;

import clases.catalogo.Paquete;
import clases.catalogo.ServicioAcabado;
import clases.prendas.ModeloPrenda;
import gestion.GestionPaquetes;
import gestion.SistemaTextil;
import gui.FormularioBase;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import utilitario.Validador;

/**
 * RF02 - Paquetes promocionales (agregación de modelos y servicios).
 * Formulario creado con el GUI Builder de NetBeans (pestaña Design).
 */
public class frmPaquetes extends FormularioBase {

    private GestionPaquetes gestion = SistemaTextil.getGestionPaquetes();
    private DefaultTableModel modelo;
    private DefaultTableModel modeloPrendas;
    private DefaultTableModel modeloServicios;

    /**
     * Creates new form frmPaquetes
     */
    public frmPaquetes() {
        initComponents();
        modeloPrendas = crearModelo(new String[]{"Código", "Modelo", "Cantidad"});
        configurarTabla(tblPrendas, modeloPrendas);
        modeloServicios = crearModelo(new String[]{"Código", "Servicio", "Precio/prenda"});
        configurarTabla(tblServicios, modeloServicios);
        modelo = crearModelo(new String[]{"Código", "Nombre", "Contenido", "Prendas", "Precio por separado", "Precio promocional", "Disponible"});
        configurarTabla(tblPaquetes, modelo);
        cargarCombos();
        listar();
        setLocationRelativeTo(null);
    }

    @Override
    protected void refrescar() {
        listar();
    }

    private void cargarCombos() {
        cboModelo.removeAllItems();
        ModeloPrenda[] activos = SistemaTextil.getGestionModelos().listarActivos();
        for (int i = 0; i < activos.length; i++) {
            cboModelo.addItem(activos[i]);
        }
        cboServicio.removeAllItems();
        for (int i = 0; i < SistemaTextil.getGestionServicios().getTotalServicios(); i++) {
            cboServicio.addItem(SistemaTextil.getGestionServicios().getServicios()[i]);
        }
    }

    void listar() {
        modelo.setRowCount(0);
        for (int i = 0; i < gestion.getTotalPaquetes(); i++) {
            Paquete p = gestion.getPaquetes()[i];
            modelo.addRow(new Object[]{p.getCodigo(), p.getNombre(), p.getContenido(), p.getPrendasPorPaquete(),
                Validador.redondear(p.getPrecioRegular()), p.getPrecioPromocional(), p.estaDisponible() ? "Sí" : "No (modelo descontinuado)"});
        }
    }

    private void agregarPrenda() {
        ModeloPrenda m = (ModeloPrenda) cboModelo.getSelectedItem();
        if (m == null || !Validador.esEntero(txtCantidad.getText()) || Validador.leerEntero(txtCantidad.getText()) <= 0) {
            error("Seleccione un modelo e ingrese una cantidad entera mayor a 0.");
            return;
        }
        int cantidad = Validador.leerEntero(txtCantidad.getText());
        for (int i = 0; i < modeloPrendas.getRowCount(); i++) {
            if (modeloPrendas.getValueAt(i, 0).equals(m.getCodigo())) {
                modeloPrendas.setValueAt(cantidad, i, 2);
                actualizarPrecioRegular();
                return;
            }
        }
        modeloPrendas.addRow(new Object[]{m.getCodigo(), m.getNombre(), cantidad});
        txtCantidad.setText("");
        actualizarPrecioRegular();
    }

    private void agregarServicio() {
        ServicioAcabado s = (ServicioAcabado) cboServicio.getSelectedItem();
        if (s == null) {
            return;
        }
        for (int i = 0; i < modeloServicios.getRowCount(); i++) {
            if (modeloServicios.getValueAt(i, 0).equals(s.getCodigo())) {
                return;
            }
        }
        modeloServicios.addRow(new Object[]{s.getCodigo(), s.getNombre(), s.getPrecioPorPrenda()});
        actualizarPrecioRegular();
    }

    private void quitarFila(JTable t, DefaultTableModel m) {
        int fila = t.getSelectedRow();
        if (fila < 0) {
            error("Seleccione la fila a quitar.");
            return;
        }
        m.removeRow(fila);
        actualizarPrecioRegular();
    }

    // Construye un objeto Paquete con lo que hay en pantalla (agregación de objetos existentes)
    private Paquete leerFormulario() {
        if (!Validador.esDecimal(txtPrecio.getText())) {
            error("El precio promocional debe ser un número.");
            return null;
        }
        Paquete p = new Paquete(txtCodigo.getText().trim().toUpperCase(), txtNombre.getText().trim(),
                Validador.leerDecimal(txtPrecio.getText()));
        for (int i = 0; i < modeloPrendas.getRowCount(); i++) {
            ModeloPrenda m = SistemaTextil.getGestionModelos().buscarPorCodigo(modeloPrendas.getValueAt(i, 0).toString());
            p.agregarModelo(m, Integer.parseInt(modeloPrendas.getValueAt(i, 2).toString()));
        }
        for (int i = 0; i < modeloServicios.getRowCount(); i++) {
            p.agregarServicio(SistemaTextil.getGestionServicios().buscarPorCodigo(modeloServicios.getValueAt(i, 0).toString()));
        }
        if (p.getPrecioPromocional() >= p.getPrecioRegular() && p.getTotalModelos() > 0) {
            if (!confirmar("El precio promocional no es menor al precio por separado ("
                    + Validador.dinero(p.getPrecioRegular()) + "). ¿Guardar de todos modos?")) {
                return null;
            }
        }
        return p;
    }

    private void actualizarPrecioRegular() {
        Paquete temp = new Paquete("", "", 0);
        for (int i = 0; i < modeloPrendas.getRowCount(); i++) {
            ModeloPrenda m = SistemaTextil.getGestionModelos().buscarPorCodigo(modeloPrendas.getValueAt(i, 0).toString());
            if (m != null) {
                temp.agregarModelo(m, Integer.parseInt(modeloPrendas.getValueAt(i, 2).toString()));
            }
        }
        for (int i = 0; i < modeloServicios.getRowCount(); i++) {
            ServicioAcabado s = SistemaTextil.getGestionServicios().buscarPorCodigo(modeloServicios.getValueAt(i, 0).toString());
            if (s != null) {
                temp.agregarServicio(s);
            }
        }
        lblRegular.setText(Validador.dinero(temp.getPrecioRegular()) + " (" + temp.getPrendasPorPaquete() + " prendas)");
    }

    private void mostrar(Paquete p) {
        txtCodigo.setText(p.getCodigo());
        txtNombre.setText(p.getNombre());
        txtPrecio.setText(String.valueOf(p.getPrecioPromocional()));
        modeloPrendas.setRowCount(0);
        for (int i = 0; i < p.getTotalModelos(); i++) {
            modeloPrendas.addRow(new Object[]{p.getModelos()[i].getCodigo(), p.getModelos()[i].getNombre(), p.getCantidades()[i]});
        }
        modeloServicios.setRowCount(0);
        for (int i = 0; i < p.getTotalServicios(); i++) {
            ServicioAcabado s = p.getServicios()[i];
            modeloServicios.addRow(new Object[]{s.getCodigo(), s.getNombre(), s.getPrecioPorPrenda()});
        }
        actualizarPrecioRegular();
        txtCodigo.setEditable(false);
    }

    void limpiar() {
        txtCodigo.setText("");
        txtNombre.setText("");
        txtPrecio.setText("");
        txtCantidad.setText("");
        modeloPrendas.setRowCount(0);
        modeloServicios.setRowCount(0);
        lblRegular.setText("-");
        txtCodigo.setEditable(true);
        tblPaquetes.clearSelection();
        cargarCombos();
        txtCodigo.requestFocus();
    }

    void buscar() {
        Paquete p = gestion.buscarPorCodigo(txtCodigo.getText().trim());
        if (p == null) {
            error("No se encontró un paquete con ese código.");
        } else {
            mostrar(p);
        }
    }

    void registrar() {
        Paquete p = leerFormulario();
        if (p == null) {
            return;
        }
        String r = gestion.registrar(p);
        if (r == null) {
            listar();
            limpiar();
            mensaje("Paquete registrado con éxito.");
        } else {
            error(r);
        }
    }

    void actualizar() {
        Paquete p = leerFormulario();
        if (p == null) {
            return;
        }
        String r = gestion.actualizar(p);
        if (r == null) {
            listar();
            mensaje("Paquete actualizado con éxito.");
        } else {
            error(r);
        }
    }

    void eliminar() {
        String codigo = txtCodigo.getText().trim();
        if (!confirmar("¿Eliminar el paquete " + codigo + "?")) {
            return;
        }
        String r = gestion.eliminar(codigo);
        if (r == null) {
            listar();
            limpiar();
            mensaje("Paquete eliminado.");
        } else {
            error(r);
        }
    }

    void navegar() {
        int fila = tblPaquetes.getSelectedRow();
        if (fila >= 0) {
            Paquete p = gestion.buscarPorCodigo(modelo.getValueAt(fila, 0).toString());
            if (p != null) {
                mostrar(p);
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitulo = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        pnlDatos = new javax.swing.JPanel();
        lblCodigo = new javax.swing.JLabel();
        txtCodigo = new javax.swing.JTextField();
        lblNombre = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        lblPrecio = new javax.swing.JLabel();
        txtPrecio = new javax.swing.JTextField();
        lblRegularTxt = new javax.swing.JLabel();
        lblRegular = new javax.swing.JLabel();
        pnlPrendas = new javax.swing.JPanel();
        cboModelo = new javax.swing.JComboBox<>();
        lblCantidad = new javax.swing.JLabel();
        txtCantidad = new javax.swing.JTextField();
        btnAgregarPrenda = new javax.swing.JButton();
        btnQuitarPrenda = new javax.swing.JButton();
        scrPrendas = new javax.swing.JScrollPane();
        tblPrendas = new javax.swing.JTable();
        pnlServicios = new javax.swing.JPanel();
        cboServicio = new javax.swing.JComboBox<>();
        btnAgregarServicio = new javax.swing.JButton();
        btnQuitarServicio = new javax.swing.JButton();
        scrServicios = new javax.swing.JScrollPane();
        tblServicios = new javax.swing.JTable();
        pnlAcciones = new javax.swing.JPanel();
        btnBuscar = new javax.swing.JButton();
        btnRegistrar = new javax.swing.JButton();
        btnActualizar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        btnSalir = new javax.swing.JButton();
        scrPaquetes = new javax.swing.JScrollPane();
        tblPaquetes = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Paquetes Promocionales - Sistema Textil Gamarra");

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(30, 58, 95));
        lblTitulo.setText("Paquetes Promocionales");

        lblSubtitulo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblSubtitulo.setForeground(new java.awt.Color(100, 112, 128));
        lblSubtitulo.setText("Un paquete agrupa modelos de prendas y servicios a un precio menor que comprarlos por separado.");

        lblCodigo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblCodigo.setText("Código:");

        txtCodigo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        lblNombre.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblNombre.setText("Nombre:");

        txtNombre.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        lblPrecio.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblPrecio.setText("Precio promocional (S/):");

        txtPrecio.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        lblRegularTxt.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblRegularTxt.setText("Precio por separado:");

        lblRegular.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblRegular.setForeground(new java.awt.Color(21, 128, 61));
        lblRegular.setText("-");

        pnlDatos.setBackground(new java.awt.Color(236, 242, 249));
        pnlDatos.setBorder(javax.swing.BorderFactory.createTitledBorder("Datos del paquete"));
        javax.swing.GroupLayout pnlDatosLayout = new javax.swing.GroupLayout(pnlDatos);
        pnlDatos.setLayout(pnlDatosLayout);
        pnlDatosLayout.setHorizontalGroup(
            pnlDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDatosLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(pnlDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPrecio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblRegularTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(pnlDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNombre, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                    .addComponent(txtPrecio, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblRegular, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12))
        );
        pnlDatosLayout.setVerticalGroup(
            pnlDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDatosLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(pnlDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(pnlDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(pnlDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPrecio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPrecio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(pnlDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblRegularTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblRegular, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10))
        );

        cboModelo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        lblCantidad.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblCantidad.setText("Cant.:");

        txtCantidad.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        btnAgregarPrenda.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnAgregarPrenda.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/vcsnormal.png"))); // NOI18N
        btnAgregarPrenda.setText("Agregar");
        btnAgregarPrenda.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAgregarPrendaActionPerformed(evt);
            }
        });

        btnQuitarPrenda.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnQuitarPrenda.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/delete_remove.png"))); // NOI18N
        btnQuitarPrenda.setText("Quitar");
        btnQuitarPrenda.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuitarPrendaActionPerformed(evt);
            }
        });

        tblPrendas.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        tblPrendas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));

        scrPrendas.setViewportView(tblPrendas);

        pnlPrendas.setBackground(new java.awt.Color(236, 242, 249));
        pnlPrendas.setBorder(javax.swing.BorderFactory.createTitledBorder("Prendas incluidas"));
        javax.swing.GroupLayout pnlPrendasLayout = new javax.swing.GroupLayout(pnlPrendas);
        pnlPrendas.setLayout(pnlPrendasLayout);
        pnlPrendasLayout.setHorizontalGroup(
            pnlPrendasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPrendasLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(pnlPrendasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlPrendasLayout.createSequentialGroup()
                        .addComponent(cboModelo, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                        .addGap(6, 6, 6)
                        .addComponent(lblCantidad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(txtCantidad, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlPrendasLayout.createSequentialGroup()
                        .addComponent(btnAgregarPrenda, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(btnQuitarPrenda, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(scrPrendas, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE))
                .addGap(10, 10, 10))
        );
        pnlPrendasLayout.setVerticalGroup(
            pnlPrendasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPrendasLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(pnlPrendasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cboModelo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCantidad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCantidad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(pnlPrendasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAgregarPrenda, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnQuitarPrenda, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addComponent(scrPrendas, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                .addGap(8, 8, 8))
        );

        cboServicio.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        btnAgregarServicio.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnAgregarServicio.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/vcsnormal.png"))); // NOI18N
        btnAgregarServicio.setText("Agregar");
        btnAgregarServicio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAgregarServicioActionPerformed(evt);
            }
        });

        btnQuitarServicio.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnQuitarServicio.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/delete_remove.png"))); // NOI18N
        btnQuitarServicio.setText("Quitar");
        btnQuitarServicio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuitarServicioActionPerformed(evt);
            }
        });

        tblServicios.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        tblServicios.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));

        scrServicios.setViewportView(tblServicios);

        pnlServicios.setBackground(new java.awt.Color(236, 242, 249));
        pnlServicios.setBorder(javax.swing.BorderFactory.createTitledBorder("Servicios incluidos (para todas las prendas)"));
        javax.swing.GroupLayout pnlServiciosLayout = new javax.swing.GroupLayout(pnlServicios);
        pnlServicios.setLayout(pnlServiciosLayout);
        pnlServiciosLayout.setHorizontalGroup(
            pnlServiciosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlServiciosLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(pnlServiciosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlServiciosLayout.createSequentialGroup()
                        .addComponent(cboServicio, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE))
                    .addGroup(pnlServiciosLayout.createSequentialGroup()
                        .addComponent(btnAgregarServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(btnQuitarServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(scrServicios, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE))
                .addGap(10, 10, 10))
        );
        pnlServiciosLayout.setVerticalGroup(
            pnlServiciosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlServiciosLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(pnlServiciosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cboServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(pnlServiciosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAgregarServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnQuitarServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addComponent(scrServicios, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                .addGap(8, 8, 8))
        );

        btnBuscar.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnBuscar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/search.png"))); // NOI18N
        btnBuscar.setText("Buscar");
        btnBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarActionPerformed(evt);
            }
        });

        btnRegistrar.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnRegistrar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/vcsnormal.png"))); // NOI18N
        btnRegistrar.setText("Registrar");
        btnRegistrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegistrarActionPerformed(evt);
            }
        });

        btnActualizar.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnActualizar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/update_rotation.png"))); // NOI18N
        btnActualizar.setText("Actualizar");
        btnActualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnActualizarActionPerformed(evt);
            }
        });

        btnEliminar.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnEliminar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/delete_remove.png"))); // NOI18N
        btnEliminar.setText("Eliminar");
        btnEliminar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarActionPerformed(evt);
            }
        });

        btnLimpiar.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnLimpiar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/archivo.png"))); // NOI18N
        btnLimpiar.setText("Limpiar");
        btnLimpiar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimpiarActionPerformed(evt);
            }
        });

        btnSalir.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnSalir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/cerrar.png"))); // NOI18N
        btnSalir.setText("Salir");
        btnSalir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSalirActionPerformed(evt);
            }
        });

        pnlAcciones.setBackground(new java.awt.Color(236, 242, 249));
        pnlAcciones.setBorder(javax.swing.BorderFactory.createTitledBorder("Acciones"));
        javax.swing.GroupLayout pnlAccionesLayout = new javax.swing.GroupLayout(pnlAcciones);
        pnlAcciones.setLayout(pnlAccionesLayout);
        pnlAccionesLayout.setHorizontalGroup(
            pnlAccionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAccionesLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(btnBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnRegistrar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnActualizar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnEliminar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 20, Short.MAX_VALUE)
                .addComponent(btnSalir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10))
        );
        pnlAccionesLayout.setVerticalGroup(
            pnlAccionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAccionesLayout.createSequentialGroup()
                .addGap(3, 3, 3)
                .addGroup(pnlAccionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRegistrar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnActualizar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEliminar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSalir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(7, 7, 7))
        );

        tblPaquetes.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        tblPaquetes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        tblPaquetes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblPaquetesMouseClicked(evt);
            }
        });

        scrPaquetes.setViewportView(tblPaquetes);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTitulo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSubtitulo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pnlDatos, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                        .addGap(8, 8, 8)
                        .addComponent(pnlPrendas, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                        .addGap(8, 8, 8)
                        .addComponent(pnlServicios, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE))
                    .addComponent(pnlAcciones, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(scrPaquetes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(16, 16, 16))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(lblTitulo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(lblSubtitulo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlDatos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlPrendas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlServicios, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(8, 8, 8)
                .addComponent(pnlAcciones, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(scrPaquetes, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                .addGap(12, 12, 12))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
        buscar();
    }//GEN-LAST:event_btnBuscarActionPerformed

    private void btnRegistrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegistrarActionPerformed
        registrar();
    }//GEN-LAST:event_btnRegistrarActionPerformed

    private void btnActualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActualizarActionPerformed
        actualizar();
    }//GEN-LAST:event_btnActualizarActionPerformed

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed
        eliminar();
    }//GEN-LAST:event_btnEliminarActionPerformed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        limpiar();
    }//GEN-LAST:event_btnLimpiarActionPerformed

    private void btnSalirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalirActionPerformed
        dispose();
    }//GEN-LAST:event_btnSalirActionPerformed

    private void tblPaquetesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblPaquetesMouseClicked
        // Al hacer clic en una fila se cargan sus datos arriba
        navegar();
    }//GEN-LAST:event_tblPaquetesMouseClicked

    private void btnAgregarPrendaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarPrendaActionPerformed
        agregarPrenda();
    }//GEN-LAST:event_btnAgregarPrendaActionPerformed

    private void btnQuitarPrendaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitarPrendaActionPerformed
        quitarFila(tblPrendas, modeloPrendas);
    }//GEN-LAST:event_btnQuitarPrendaActionPerformed

    private void btnAgregarServicioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarServicioActionPerformed
        agregarServicio();
    }//GEN-LAST:event_btnAgregarServicioActionPerformed

    private void btnQuitarServicioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitarServicioActionPerformed
        quitarFila(tblServicios, modeloServicios);
    }//GEN-LAST:event_btnQuitarServicioActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActualizar;
    private javax.swing.JButton btnAgregarPrenda;
    private javax.swing.JButton btnAgregarServicio;
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnQuitarPrenda;
    private javax.swing.JButton btnQuitarServicio;
    private javax.swing.JButton btnRegistrar;
    private javax.swing.JButton btnSalir;
    private javax.swing.JComboBox<ModeloPrenda> cboModelo;
    private javax.swing.JComboBox<ServicioAcabado> cboServicio;
    private javax.swing.JLabel lblCantidad;
    private javax.swing.JLabel lblCodigo;
    private javax.swing.JLabel lblNombre;
    private javax.swing.JLabel lblPrecio;
    private javax.swing.JLabel lblRegular;
    private javax.swing.JLabel lblRegularTxt;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel pnlAcciones;
    private javax.swing.JPanel pnlDatos;
    private javax.swing.JPanel pnlPrendas;
    private javax.swing.JPanel pnlServicios;
    private javax.swing.JScrollPane scrPaquetes;
    private javax.swing.JScrollPane scrPrendas;
    private javax.swing.JScrollPane scrServicios;
    private javax.swing.JTable tblPaquetes;
    private javax.swing.JTable tblPrendas;
    private javax.swing.JTable tblServicios;
    private javax.swing.JTextField txtCantidad;
    private javax.swing.JTextField txtCodigo;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtPrecio;
    // End of variables declaration//GEN-END:variables
}
