# ms-vidasalud-notifications

Consumidores RabbitMQ de VidaSalud. Escucha los eventos de atenciones que publica `ms-vidasalud-appointments` en el exchange topic `vidasalud.atenciones`.

| Cola | Routing keys | Qué hace | DLQ |
|---|---|---|---|
| `q.notificaciones` | `atencion.confirmada`, `atencion.cancelada` | Aviso al paciente | `q.notificaciones.dlq` |
| `q.tickets` | `atencion.en_espera` | Número de llamado | `q.tickets.dlq` |
| `q.auditoria` | `atencion.#` | Registro de cada cambio | `q.auditoria.dlq` |

Los mensajes rechazados van al exchange direct `vidasalud.dlx`, que los deja en la DLQ de su cola.

ACK manual (`ProcesadorMensajes`): éxito → ACK; error no recuperable → NACK a DLQ; error recuperable → un reintento y luego DLQ. Cada envío a DLQ queda en el log.

Variables: `RABBIT_ADDRESSES`, `RABBIT_USER`, `RABBIT_PASS`.
