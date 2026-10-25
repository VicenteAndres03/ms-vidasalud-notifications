package cl.duoc.vidasalud.notifications.messaging;

import cl.duoc.vidasalud.notifications.exception.MensajeInvalidoException;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Manejo común de confirmaciones (ACK manual) para todos los consumidores.
 *
 *  - Éxito                         -> basicAck.
 *  - Error no recuperable          -> basicNack sin reencolar -> DLQ.
 *  - Error recuperable, 1er intento -> basicNack reencolando (un reintento).
 *  - Error recuperable, ya reintentado -> basicNack sin reencolar -> DLQ.
 *
 * Todo mensaje que termina en la DLQ queda registrado en el log.
 */
@Component
public class ProcesadorMensajes {

    private static final Logger log = LoggerFactory.getLogger(ProcesadorMensajes.class);

    public void procesar(String cola, EventoAtencion evento, Channel channel, long tag,
                         boolean reentregado, Consumer<EventoAtencion> accion) {
        try {
            validar(evento);
            accion.accept(evento);
            channel.basicAck(tag, false);
            log.info("[{}] ACK atención {} ({})", cola, evento.id(), evento.estado());
        } catch (MensajeInvalidoException e) {
            enviarADlq(cola, evento, channel, tag, e.getMessage());
        } catch (Exception e) {
            if (!reentregado) {
                log.warn("[{}] Error procesando atención {}: {}. Se reintenta una vez.",
                        cola, idDe(evento), e.getMessage());
                nack(channel, tag, true);
            } else {
                enviarADlq(cola, evento, channel, tag, "falló también el reintento: " + e.getMessage());
            }
        }
    }

    private void validar(EventoAtencion evento) {
        if (evento == null || evento.id() == null || evento.estado() == null) {
            throw new MensajeInvalidoException("evento sin id o sin estado");
        }
    }

    private void enviarADlq(String cola, EventoAtencion evento, Channel channel, long tag, String motivo) {
        log.error("[{}] Mensaje enviado a DLQ. Atención {}: {} | evento={}", cola, idDe(evento), motivo, evento);
        nack(channel, tag, false);
    }

    private void nack(Channel channel, long tag, boolean reencolar) {
        try {
            channel.basicNack(tag, false, reencolar);
        } catch (IOException e) {
            log.error("No se pudo enviar NACK (tag {}): {}", tag, e.getMessage());
        }
    }

    private Object idDe(EventoAtencion evento) {
        return evento == null ? "?" : evento.id();
    }
}
