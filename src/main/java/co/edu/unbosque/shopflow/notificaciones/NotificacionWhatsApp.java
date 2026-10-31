package co.edu.unbosque.shopflow.notificaciones;

public class NotificacionWhatsApp implements Notificacion {

    private final BandejaNotificaciones bandeja;

    public NotificacionWhatsApp(BandejaNotificaciones bandeja) {
        this.bandeja = bandeja;
    }

    @Override
    public void enviar(String destino, String mensaje) {
        if (destino == null || !destino.matches("\\+[1-9][0-9]{7,14}")) {
            throw new IllegalArgumentException("WhatsApp requiere un número internacional, por ejemplo +573001234567");
        }
        bandeja.registrar("WHATSAPP", destino, "✅ " + mensaje);
    }
}
