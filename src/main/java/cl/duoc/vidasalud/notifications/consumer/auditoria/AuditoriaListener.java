package cl.duoc.vidasalud.notifications.consumer.auditoria;

import cl.duoc.vidasalud.notifications.messaging.EventoAtencion;
import cl.duoc.vidasalud.notifications.messaging.ProcesadorMensajes;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class AuditoriaListener {

    private final ProcesadorMensajes procesador;
    private final AuditoriaService service;
    private final String cola;

    public AuditoriaListener(ProcesadorMensajes procesador, AuditoriaService service,
                             @Value("${app.rabbit.colas.auditoria}") String cola) {
        this.procesador = procesador;
        this.service = service;
        this.cola = cola;
    }

    @RabbitListener(queues = "${app.rabbit.colas.auditoria}")
    public void recibir(EventoAtencion evento, Channel channel,
                        @Header(AmqpHeaders.DELIVERY_TAG) long tag,
                        @Header(AmqpHeaders.REDELIVERED) boolean reentregado) {
        procesador.procesar(cola, evento, channel, tag, reentregado, service::registrar);
    }
}
