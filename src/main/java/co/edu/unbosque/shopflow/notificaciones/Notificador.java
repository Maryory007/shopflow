package co.edu.unbosque.shopflow.notificaciones;

import java.math.BigDecimal;

public abstract class Notificador {

    public final void notificarPagoAprobado(String destino, String pedidoId, BigDecimal total) {
        if (destino == null || destino.isBlank()) {
            throw new IllegalArgumentException("El contacto del cliente es obligatorio para notificar");
        }
        String mensaje = "Pago aprobado para el pedido " + pedidoId + ". Total pagado: " + total + " USD.";
        crearNotificacion().enviar(destino, mensaje);
    }

    public abstract String canal();

    protected abstract Notificacion crearNotificacion();
}
