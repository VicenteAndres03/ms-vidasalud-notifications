package cl.duoc.vidasalud.notifications.consumer.tickets;

import cl.duoc.vidasalud.notifications.messaging.EventoAtencion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

/** Genera el número de llamado cuando el paciente pasa a sala de espera. */
@Service
public class TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);

    private final AtomicInteger correlativo = new AtomicInteger();

    public void generar(EventoAtencion evento) {
        // Falla simulada para la demo: prestación con "FALLA" -> error recuperable (se reintenta)
        if (evento.prestacion() != null && evento.prestacion().toUpperCase().contains("FALLA")) {
            throw new IllegalStateException("impresora de tickets no disponible");
        }

        String ticket = "A-%03d".formatted(correlativo.incrementAndGet());
        log.info("TICKET {} -> {} | {} | box {}", ticket, evento.paciente(), evento.prestacion(), evento.boxId());
    }
}
