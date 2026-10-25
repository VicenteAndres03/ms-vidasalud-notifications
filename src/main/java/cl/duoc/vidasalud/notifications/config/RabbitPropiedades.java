package cl.duoc.vidasalud.notifications.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Nombres de RabbitMQ definidos en application.yml (app.rabbit.*). */
@ConfigurationProperties(prefix = "app.rabbit")
public record RabbitPropiedades(
        String exchangeAtenciones,
        String exchangeDlx,
        String sufijoDlq,
        Colas colas,
        RoutingKeys routingKeys
) {
    public record Colas(String notificaciones, String tickets, String auditoria) {}

    public record RoutingKeys(String confirmada, String cancelada, String enEspera, String todas) {}

    public String dlq(String cola) {
        return cola + sufijoDlq;
    }
}
