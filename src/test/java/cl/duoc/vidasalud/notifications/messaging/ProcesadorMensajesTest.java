package cl.duoc.vidasalud.notifications.messaging;

import cl.duoc.vidasalud.notifications.exception.MensajeInvalidoException;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ProcesadorMensajesTest {

    private final ProcesadorMensajes procesador = new ProcesadorMensajes();
    private final Channel channel = mock(Channel.class);
    private final EventoAtencion evento = new EventoAtencion(
            1L, "pac-1", "Juan Pérez", "Consulta", 1L, "CONFIRMADA", LocalDateTime.now(), LocalDateTime.now());

    @Test
    void exitoHaceAck() throws Exception {
        procesador.procesar("q.test", evento, channel, 7L, false, e -> { });

        verify(channel).basicAck(7L, false);
    }

    @Test
    void errorNoRecuperableVaDirectoADlq() throws Exception {
        procesador.procesar("q.test", evento, channel, 7L, false, e -> {
            throw new MensajeInvalidoException("dato inválido");
        });

        verify(channel).basicNack(7L, false, false);
        verify(channel, never()).basicAck(7L, false);
    }

    @Test
    void errorRecuperableSeReintentaUnaVez() throws Exception {
        procesador.procesar("q.test", evento, channel, 7L, false, e -> {
            throw new IllegalStateException("caída temporal");
        });

        verify(channel).basicNack(7L, false, true);
    }

    @Test
    void errorEnElReintentoVaADlq() throws Exception {
        procesador.procesar("q.test", evento, channel, 7L, true, e -> {
            throw new IllegalStateException("caída temporal");
        });

        verify(channel).basicNack(7L, false, false);
    }

    @Test
    void eventoSinIdVaADlq() throws Exception {
        EventoAtencion sinId = new EventoAtencion(null, null, null, null, null, null, null, null);

        procesador.procesar("q.test", sinId, channel, 7L, false, e -> { });

        verify(channel).basicNack(7L, false, false);
    }
}
