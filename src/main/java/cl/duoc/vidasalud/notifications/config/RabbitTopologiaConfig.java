package cl.duoc.vidasalud.notifications.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topología RabbitMQ de los consumidores.
 *
 * Exchange topic "vidasalud.atenciones": eventos del ciclo de vida de una atención.
 *   q.notificaciones  <- atencion.confirmada, atencion.cancelada  (avisar al paciente)
 *   q.tickets         <- atencion.en_espera                       (ticket de llamado)
 *   q.auditoria       <- atencion.#                               (registro de todo)
 *
 * Exchange direct "vidasalud.dlx": recibe los mensajes rechazados de cada cola
 * y los deja en su DLQ (q.xxx.dlq) para revisarlos.
 */
@Configuration
@EnableConfigurationProperties(RabbitPropiedades.class)
public class RabbitTopologiaConfig {

    private final RabbitPropiedades props;

    public RabbitTopologiaConfig(RabbitPropiedades props) {
        this.props = props;
    }

    // ---------- exchanges ----------

    @Bean
    public TopicExchange exchangeAtenciones() {
        return ExchangeBuilder.topicExchange(props.exchangeAtenciones()).durable(true).build();
    }

    @Bean
    public DirectExchange exchangeDlx() {
        return ExchangeBuilder.directExchange(props.exchangeDlx()).durable(true).build();
    }

    // ---------- caso de uso: notificaciones ----------

    @Bean
    public Queue colaNotificaciones() {
        return colaConDlq(props.colas().notificaciones());
    }

    @Bean
    public Queue dlqNotificaciones() {
        return QueueBuilder.durable(props.dlq(props.colas().notificaciones())).build();
    }

    @Bean
    public Binding notificacionesConfirmada() {
        return BindingBuilder.bind(colaNotificaciones()).to(exchangeAtenciones()).with(props.routingKeys().confirmada());
    }

    @Bean
    public Binding notificacionesCancelada() {
        return BindingBuilder.bind(colaNotificaciones()).to(exchangeAtenciones()).with(props.routingKeys().cancelada());
    }

    @Bean
    public Binding dlqNotificacionesBinding() {
        return BindingBuilder.bind(dlqNotificaciones()).to(exchangeDlx()).with(props.dlq(props.colas().notificaciones()));
    }

    // ---------- caso de uso: tickets ----------

    @Bean
    public Queue colaTickets() {
        return colaConDlq(props.colas().tickets());
    }

    @Bean
    public Queue dlqTickets() {
        return QueueBuilder.durable(props.dlq(props.colas().tickets())).build();
    }

    @Bean
    public Binding ticketsEnEspera() {
        return BindingBuilder.bind(colaTickets()).to(exchangeAtenciones()).with(props.routingKeys().enEspera());
    }

    @Bean
    public Binding dlqTicketsBinding() {
        return BindingBuilder.bind(dlqTickets()).to(exchangeDlx()).with(props.dlq(props.colas().tickets()));
    }

    // ---------- caso de uso: auditoría ----------

    @Bean
    public Queue colaAuditoria() {
        return colaConDlq(props.colas().auditoria());
    }

    @Bean
    public Queue dlqAuditoria() {
        return QueueBuilder.durable(props.dlq(props.colas().auditoria())).build();
    }

    @Bean
    public Binding auditoriaTodas() {
        return BindingBuilder.bind(colaAuditoria()).to(exchangeAtenciones()).with(props.routingKeys().todas());
    }

    @Bean
    public Binding dlqAuditoriaBinding() {
        return BindingBuilder.bind(dlqAuditoria()).to(exchangeDlx()).with(props.dlq(props.colas().auditoria()));
    }

    // ---------- conversión JSON ----------

    @Bean
    public MessageConverter rabbitMessageConverter() {
        ObjectMapper mapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(mapper);
        // El productor manda su propia clase; aquí se usa el tipo del parámetro del listener
        converter.setAlwaysConvertToInferredType(true);
        return converter;
    }

    /** Cola durable cuyos mensajes rechazados van al DLX con routing key "<cola>.dlq". */
    private Queue colaConDlq(String nombre) {
        return QueueBuilder.durable(nombre)
                .deadLetterExchange(props.exchangeDlx())
                .deadLetterRoutingKey(props.dlq(nombre))
                .build();
    }
}
