# Sistema Textil Gamarra · Proyecto Final POO 2026-2

Sistema de gestión de prendas, materia prima y pedidos de producción para una empresa de confecciones de Gamarra.
Java 21 + Swing, proyecto **NetBeans (Ant)**. Los datos viven en **arreglos de objetos en memoria** (sin base de datos ni librerías externas).

## Cómo abrirlo y ejecutarlo

1. NetBeans → *File → Open Project* → seleccionar esta carpeta (`Trabajo_Final_Poo`).
2. *Run Project* (F6). La clase principal es `gui.frmLogin`.
3. Usuarios de prueba:

| Rol | Usuario | Contraseña |
|---|---|---|
| Administrador | `admin` | `admin123` |
| Ejecutivo de Ventas | `cmendoza` / `dsalas` | `ventas123` |
| Jefe de Producción | `rhuaman` / `lchavez` | `prod123` |

Al iniciar se cargan datos de ejemplo (`gestion/DatosIniciales.java`): 5 empleados, 10 telas, 7 insumos, 6 modelos (1 descontinuado), 4 servicios, 2 paquetes, 5 clientes y 9 pedidos en distintas etapas (entregados, en producción, cancelado), para que el tablero y los reportes tengan información.

## Estructura (mismo esquema que el Lab de la Semana 13)

| Paquete | Contenido |
|---|---|
| `clases` | Modelo: entidades, jerarquías, interfaces |
| `gestion` | Clases `Gestion*` con los arreglos de objetos, validaciones, flujo de pedidos y reportes |
| `gui` | Ventanas `frm*` (JFrame). Todas heredan de `FormularioBase` |
| `utilitario` | `Validador` (validaciones y formatos) y `EscritorioPersonalizado` (fondo) |
| `imagenes` | Íconos de botones y menús |

## Dónde se aplica cada concepto de POO

| Concepto | Dónde |
|---|---|
| **Clases abstractas** | `Persona`, `Empleado`, `Cliente`, `ModeloPrenda`, `MateriaPrima`, `ItemPedido`, `Comprobante` |
| **Herencia** | `Empleado → Administrador / EjecutivoVentas / JefeProduccion`; `Cliente → Minorista / Mayorista / Institucional`; `ModeloPrenda → Polo / Jean / Casaca`; `MateriaPrima → Tela / Insumo`; `ItemPedido → DetallePrenda / DetalleServicio / DetallePaquete`; `Comprobante → Boleta / Factura`; `FormularioBase → frm*` |
| **Polimorfismo** | `calcularCostoProduccion()` (cada prenda), `calcularDescuento()` (cada cliente), `tienePermiso()` (cada rol → menús), `calcularSubtotal()/calcularCosto()/getPrendas()` (cada ítem del pedido), `getDatosCliente()` (boleta vs factura), `stockBajoMinimo()` recorre un `MateriaPrima[]` con telas e insumos |
| **Interfaces** | `Autenticable` (login), `Inventariable` (control de stock), `Calculable` (precio y costo de los ítems) |
| **Arreglos de objetos** | Todas las clases `Gestion*` (`Empleado[]`, `Cliente[]`, `Pedido[]`…) con contador, búsqueda, registro, actualización y eliminación con corrimiento, igual que `GestionLibro` |
| **Composición / agregación** | `Pedido` tiene `ItemPedido[]` y `HistorialEstado[]`; `Paquete` agrupa `ModeloPrenda[]` y `ServicioAcabado[]`; `ModeloPrenda` tiene `InsumoRequerido[]` |

## Requerimientos → código

| RF | Clases principales | Ventana |
|---|---|---|
| RF01 Personal | `Empleado` y subclases, `GestionEmpleados` | `frmEmpleados` |
| RF02 Modelos | `ModeloPrenda`, `Polo`, `Jean`, `Casaca`, `GestionModelos` | `frmModelos` |
| RF03 Clientes | `Cliente` y subclases, `GestionClientes` | `frmClientes` |
| RF04 Materia prima, servicios, paquetes | `Tela`, `Insumo`, `ServicioAcabado`, `Paquete`, `Gestion*` | `frmTelas`, `frmInsumos`, `frmServicios`, `frmPaquetes` |
| RF05 Registro de pedidos | `Pedido`, `ItemPedido`…, `GestionPedidos.validar()` | `frmNuevoPedido` |
| RF06 Estados del pedido | `GestionPedidos` (confirmar, iniciarCorte, registrarAvance, registrarControlCalidad, despachar, cancelar), `HistorialEstado` | `frmGestionPedidos`, `frmProduccion` |
| RF07 Facturación | `Comprobante`, `Boleta`, `Factura`, `GestionComprobantes` | `frmGestionPedidos` (Despachar) |
| RF08 Reportes | `GestionReportes` | `frmReportes` |
| RF09 Seguridad | `Autenticable`, `tienePermiso()` | `frmLogin`, `frmPrincipal` |
| RF10 Usabilidad | `FormularioBase` | `frmTablero`, `frmStockTelas` |

## Reglas de negocio y supuestos (revisarlos en grupo)

Los valores están en constantes para cambiarlos en un solo lugar.

- **Merma de tela:** 5 % (`Pedido.MERMA`). Tela necesaria = consumo por talla × cantidad × 1.05.
- **Tela e insumos disponibles** = stock − lo reservado por pedidos *Registrados* o *Confirmados* que aún no se cortan. El stock se descuenta en el **corte**.
- **Capacidad de planta:** 300 prendas/día (`GestionPedidos.CAPACIDAD_PRENDAS_POR_DIA`), lunes a sábado. Se restan las prendas de pedidos en curso con entrega hasta la misma fecha.
- **Mínimos:** 50 prendas y entrega con 7 días de anticipación.
- **Descuentos:** Mayorista 10 % desde 200 prendas; Institucional 15 % si el pedido lleva un servicio con logo. Solo aplican a prendas sueltas (los paquetes ya tienen precio cerrado).
- **Precios sin IGV;** el comprobante suma IGV 18 %. Adelanto mínimo: 50 % del total.
- **Costo de producción:** Polo = tela + costura (+ insumos); Jean = + lavado; Casaca = + forro + avíos.
- **Control de calidad:** si hay defectuosas, el pedido vuelve a *En Confección*; si no, pasa a *Listo para Despacho*.
- **Cancelación:** antes del corte se devuelve el adelanto; después queda retenido. *Entregado* y *Cancelado* no se modifican.
- **Factura:** exige RUC válido (11 dígitos, empieza en 10 o 20) y razón social.

## Reparto sugerido para 3 integrantes

| Integrante | Requerimientos | Archivos |
|---|---|---|
| 1 | RF01, RF03, RF09, RF10 | Persona/Empleado/Cliente y subclases, `GestionEmpleados`, `GestionClientes`, `frmLogin`, `frmPrincipal`, `frmEmpleados`, `frmClientes`, `frmTablero`, `frmStockTelas` |
| 2 | RF02, RF04, RF07 | ModeloPrenda y subclases, MateriaPrima/Tela/Insumo, ServicioAcabado, Paquete, Comprobante/Boleta/Factura, sus `Gestion*` y ventanas |
| 3 | RF05, RF06, RF08 | Pedido, ItemPedido y subclases, HistorialEstado, `GestionPedidos`, `GestionReportes`, `frmNuevoPedido`, `frmGestionPedidos`, `frmProduccion`, `frmReportes` |

El diagrama de clases está en [`docs/diagrama-clases.md`](docs/diagrama-clases.md).
