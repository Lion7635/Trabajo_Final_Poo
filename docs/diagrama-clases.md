# Diagrama de clases (UML)

GitHub dibuja este diagrama automáticamente. Para el informe se puede exportar desde https://mermaid.live (pegar el bloque).
Se muestran los atributos y métodos más relevantes; getters y setters se omiten.

```mermaid
classDiagram
direction TB

class Autenticable {
  <<interface>>
  +autenticar(usuario, contrasena) boolean
}
class Inventariable {
  <<interface>>
  +haySuficiente(cantidad) boolean
  +descontar(cantidad)
  +estaBajoMinimo() boolean
}
class Calculable {
  <<interface>>
  +calcularSubtotal() double
  +calcularCosto() double
}

class Persona {
  <<abstract>>
  #documento: String
  #nombres: String
  +getNombreCompleto() String*
}
class Empleado {
  <<abstract>>
  -apellidos: String
  -usuario: String
  -contrasena: String
  +getRol() String*
  +tienePermiso(modulo) boolean*
}
class Administrador
class EjecutivoVentas
class JefeProduccion
Persona <|-- Empleado
Empleado ..|> Autenticable
Empleado <|-- Administrador
Empleado <|-- EjecutivoVentas
Empleado <|-- JefeProduccion

class Cliente {
  <<abstract>>
  -telefono: String
  -correo: String
  -direccion: String
  +getTipo() String*
  +calcularDescuento(subtotal, prendas, logo) double*
}
class ClienteMinorista
class ClienteMayorista
class ClienteInstitucional
Persona <|-- Cliente
Cliente <|-- ClienteMinorista
Cliente <|-- ClienteMayorista
Cliente <|-- ClienteInstitucional

class MateriaPrima {
  <<abstract>>
  #codigo: String
  #stock: double
  #stockMinimo: double
  #costoUnitario: double
  +getDescripcion() String*
  +getUnidad() String*
}
class Tela {
  -tipo: String
  -color: String
}
class Insumo {
  -nombre: String
  -unidad: String
}
MateriaPrima ..|> Inventariable
MateriaPrima <|-- Tela
MateriaPrima <|-- Insumo

class ModeloPrenda {
  <<abstract>>
  -codigo: String
  -nombre: String
  -tipoTela: String
  -consumoPorTalla: double[]
  -tiempoConfeccion: int
  -precioVenta: double
  -costoCostura: double
  -activo: boolean
  -insumos: InsumoRequerido[]
  +calcularCostoProduccion(talla, costoMetro) double*
}
class Polo
class Jean {
  -costoLavado: double
}
class Casaca {
  -costoForro: double
  -costoAvios: double
}
class InsumoRequerido {
  -cantidadPorPrenda: double
}
ModeloPrenda <|-- Polo
ModeloPrenda <|-- Jean
ModeloPrenda <|-- Casaca
ModeloPrenda *-- "0..10" InsumoRequerido
InsumoRequerido --> "1" Insumo

class ServicioAcabado {
  -codigo: String
  -precioPorPrenda: double
  -costoPorPrenda: double
  -requiereEtapaAcabado: boolean
  -conLogo: boolean
}
class Paquete {
  -codigo: String
  -precioPromocional: double
  -modelos: ModeloPrenda[]
  -cantidades: int[]
  -servicios: ServicioAcabado[]
  +estaDisponible() boolean
}
Paquete o-- "1..10" ModeloPrenda
Paquete o-- "0..10" ServicioAcabado

class ItemPedido {
  <<abstract>>
  #cantidad: int
  +getPrendas() DetallePrenda[]*
  +requiereAcabado() boolean*
  +incluyeLogo() boolean*
}
class DetallePrenda {
  -talla: String
}
class DetalleServicio
class DetallePaquete {
  -talla: String
  -color: String
}
ItemPedido ..|> Calculable
ItemPedido <|-- DetallePrenda
ItemPedido <|-- DetalleServicio
ItemPedido <|-- DetallePaquete
DetallePrenda --> ModeloPrenda
DetallePrenda --> Tela
DetalleServicio --> ServicioAcabado
DetallePaquete --> Paquete

class Pedido {
  -numero: int
  -fechaRegistro: LocalDate
  -fechaEntrega: LocalDate
  -estado: String
  -adelanto: double
  -defectuososPorTalla: int[]
  +calcularDescuento() double
  +calcularTotal() double
  +getMetrosConMerma(tela) double
  +cambiarEstado(estado, usuario, obs, fecha)
}
class HistorialEstado {
  -fechaHora: LocalDateTime
  -estadoAnterior: String
  -estadoNuevo: String
}
Pedido *-- "1..30" ItemPedido
Pedido *-- "0..60" HistorialEstado
Pedido --> "1" Cliente
Pedido --> "1" EjecutivoVentas
Pedido --> "0..1" JefeProduccion
Pedido --> "0..1" Comprobante

class Comprobante {
  <<abstract>>
  -numero: String
  -subtotal: double
  -igv: double
  -total: double
  -saldo: double
  +getTipo() String*
  +generarTexto() String
}
class Boleta
class Factura {
  -ruc: String
  -razonSocial: String
}
Comprobante <|-- Boleta
Comprobante <|-- Factura

class GestionPedidos {
  -pedidos: Pedido[]
  -totalPedidos: int
  +registrar(pedido, hoy) String
  +validar(pedido, hoy) String
  +confirmar(...) String
  +iniciarCorte(...) String
  +registrarAvance(...) String
  +registrarControlCalidad(...) String
  +despachar(...) String
  +cancelar(...) String
}
GestionPedidos o-- "0..300" Pedido
```

Las demás clases `Gestion*` (`GestionEmpleados`, `GestionClientes`, `GestionModelos`, `GestionTelas`, `GestionInsumos`, `GestionServicios`, `GestionPaquetes`, `GestionComprobantes`) siguen el mismo patrón: un arreglo de su entidad, un contador y los métodos `buscar`, `registrar`, `actualizar` y `eliminar`. `SistemaTextil` guarda una instancia de cada gestor para que todas las ventanas compartan los mismos datos.
