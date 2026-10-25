package cl.duoc.vidasalud.notifications.messaging;

import java.time.LocalDateTime;

/** Evento que publica ms-vidasalud-appointments cada vez que una atención cambia. */
public record EventoAtencion(
        Long id,
        String pacienteId,
        String paciente,
        String prestacion,
        Long boxId,
        String estado,
        LocalDateTime fecha,
        LocalDateTime ocurridoEn
) {}
