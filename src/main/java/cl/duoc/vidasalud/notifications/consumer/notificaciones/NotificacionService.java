package cl.duoc.vidasalud.notifications.consumer.notificaciones;

import cl.duoc.vidasalud.notifications.exception.MensajeInvalidoException;
import cl.duoc.vidasalud.notifications.messaging.EventoAtencion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Simula el envío de un aviso al paciente (en un sistema real: correo o SMS). */
@Service
public class NotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);

    public void enviar(EventoAtencion evento) {
        // Falla simulada para la demo: prestación con "INVALIDO" -> error no recuperable
        if (evento.prestacion() != null && evento.prestacion().toUpperCase().contains("INVALIDO")) {
            throw new MensajeInvalidoException("datos de contacto inválidos para " + evento.paciente());
        }

        String accion = "CANCELADA".equals(evento.estado()) ? "fue cancelada" : "fue confirmada";
        log.info("NOTIFICACIÓN -> {}: tu atención de {} {} ({}).",
                evento.paciente(), evento.prestacion(), accion, evento.fecha());
    }
}
