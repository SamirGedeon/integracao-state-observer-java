import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Integra State (ciclo do pedido) e Observer (notificação de mudanças). */
public class Main {
    interface ObservadorPedido {
        void aoMudarStatus(String statusAnterior, String statusAtual);
    }

    interface EstadoPedido {
        String nome();
        default void pagar(Pedido pedido) { throw new IllegalStateException("Pagamento não permitido: " + nome()); }
        default void enviar(Pedido pedido) { throw new IllegalStateException("Envio não permitido: " + nome()); }
        default void entregar(Pedido pedido) { throw new IllegalStateException("Entrega não permitida: " + nome()); }
        default void cancelar(Pedido pedido) { throw new IllegalStateException("Cancelamento não permitido: " + nome()); }
    }

    static final class Pedido {
        private EstadoPedido estado = new AguardandoPagamento();
        private final List<ObservadorPedido> observadores = new ArrayList<>();

        public String getStatus() { return estado.nome(); }
        public void adicionarObservador(ObservadorPedido observador) {
            observadores.add(Objects.requireNonNull(observador, "observador"));
        }
        public void removerObservador(ObservadorPedido observador) { observadores.remove(observador); }
        private void mudarEstado(EstadoPedido novoEstado) {
            String anterior = estado.nome();
            estado = Objects.requireNonNull(novoEstado, "novoEstado");
            String atual = estado.nome();
            for (ObservadorPedido observador : new ArrayList<>(observadores)) {
                observador.aoMudarStatus(anterior, atual);
            }
        }
        public void pagar() { estado.pagar(this); }
        public void enviar() { estado.enviar(this); }
        public void entregar() { estado.entregar(this); }
        public void cancelar() { estado.cancelar(this); }
    }

    static final class AguardandoPagamento implements EstadoPedido {
        public String nome() { return "Aguardando pagamento"; }
        public void pagar(Pedido pedido) { pedido.mudarEstado(new Pago()); }
        public void cancelar(Pedido pedido) { pedido.mudarEstado(new Cancelado()); }
    }
    static final class Pago implements EstadoPedido {
        public String nome() { return "Pago"; }
        public void enviar(Pedido pedido) { pedido.mudarEstado(new Enviado()); }
        public void cancelar(Pedido pedido) { pedido.mudarEstado(new Cancelado()); }
    }
    static final class Enviado implements EstadoPedido {
        public String nome() { return "Enviado"; }
        public void entregar(Pedido pedido) { pedido.mudarEstado(new Entregue()); }
    }
    static final class Entregue implements EstadoPedido {
        public String nome() { return "Entregue"; }
    }
    static final class Cancelado implements EstadoPedido {
        public String nome() { return "Cancelado"; }
    }

    /** Observer de exemplo que registra cada transição para acompanhamento. */
    static final class NotificadorConsole implements ObservadorPedido {
        private final String nome;
        NotificadorConsole(String nome) { this.nome = nome; }
        public void aoMudarStatus(String anterior, String atual) {
            System.out.println(nome + ": pedido mudou de '" + anterior + "' para '" + atual + "'.");
        }
    }

    public static void main(String[] args) {
        Pedido pedido = new Pedido();
        pedido.adicionarObservador(new NotificadorConsole("Cliente"));
        pedido.adicionarObservador(new NotificadorConsole("Painel da loja"));
        pedido.pagar();
        pedido.enviar();
        pedido.entregar();
    }
}
