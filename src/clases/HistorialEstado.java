package clases;

import java.time.LocalDateTime;

/**
 * Registro de un cambio de estado del pedido (RF06).
 */
public class HistorialEstado {

    private LocalDateTime fechaHora;
    private String estadoAnterior;
    private String estadoNuevo;
    private String usuario;
    private String observacion;

    public HistorialEstado(LocalDateTime fechaHora, String estadoAnterior, String estadoNuevo,
            String usuario, String observacion) {
        this.fechaHora = fechaHora;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.usuario = usuario;
        this.observacion = observacion;
    }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public String getEstadoAnterior() { return estadoAnterior; }
    public String getEstadoNuevo() { return estadoNuevo; }
    public String getUsuario() { return usuario; }
    public String getObservacion() { return observacion; }
}
