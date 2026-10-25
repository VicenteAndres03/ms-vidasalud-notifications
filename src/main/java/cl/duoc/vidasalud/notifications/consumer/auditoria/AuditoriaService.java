package cl.duoc.vidasalud.notifications.consumer.auditoria;

import cl.duoc.vidasalud.notifications.messaging.EventoAtencion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Deja registro de cada cambio de estado de una atención. */
@Service
public class AuditoriaService {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaService.class);

    public void registrar(EventoAtencion evento) {
        log.info("AUDITORÍA -> atención {} de {} pasó a {} ({})",
                evento.id(), evento.paciente(), evento.estado(), evento.ocurridoEn());
    }
}
