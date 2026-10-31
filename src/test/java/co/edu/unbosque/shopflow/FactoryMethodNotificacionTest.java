package co.edu.unbosque.shopflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.shopflow.notificaciones.BandejaNotificaciones;
import co.edu.unbosque.shopflow.notificaciones.NotificadorEmail;
import co.edu.unbosque.shopflow.notificaciones.NotificadorSms;
import co.edu.unbosque.shopflow.notificaciones.NotificadorWhatsApp;

class FactoryMethodNotificacionTest {

    @Test
    void whatsappCreaUnaNotificacionConIndicativoYPrefijo() {
        BandejaNotificaciones bandeja = new BandejaNotificaciones();
        NotificadorWhatsApp notificador = new NotificadorWhatsApp(bandeja);

        notificador.notificarPagoAprobado("+573001234567", "PED-1", new BigDecimal("85.00"));

        assertEquals("WHATSAPP", bandeja.todas().get(0).canal());
        assertTrue(bandeja.todas().get(0).mensaje().startsWith("✅ "));
        assertTrue(bandeja.todas().get(0).mensaje().contains("85.00 USD"));
        assertThrows(IllegalArgumentException.class,
                () -> notificador.notificarPagoAprobado("3001234567", "PED-2", BigDecimal.TEN));
    }

    @Test
    void emailYsmsConservanSusReglasDeCanal() {
        BandejaNotificaciones bandeja = new BandejaNotificaciones();
        NotificadorEmail email = new NotificadorEmail(bandeja);
        NotificadorSms sms = new NotificadorSms(bandeja);

        email.notificarPagoAprobado("cliente@correo.com", "PED-2", new BigDecimal("20.00"));
        sms.notificarPagoAprobado("3001234567", "PED-3", new BigDecimal("20.00"));

        assertTrue(bandeja.todas().get(0).mensaje().startsWith("Asunto: Pago aprobado"));
        assertEquals("SMS", bandeja.todas().get(1).canal());
        assertTrue(bandeja.todas().get(1).mensaje().length() <= 160);
        assertThrows(IllegalArgumentException.class,
                () -> email.notificarPagoAprobado("sin-correo", "PED-4", BigDecimal.TEN));
    }
}
