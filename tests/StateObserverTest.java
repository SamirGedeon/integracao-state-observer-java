import java.util.ArrayList;
import java.util.List;

/** Testes de integração State + Observer sem bibliotecas externas. */
public final class StateObserverTest {
    private static int verificacoes;
    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) throw new AssertionError(mensagem);
        verificacoes++;
    }
    private static void status(Main.Pedido pedido, String esperado) {
        verificar(esperado.equals(pedido.getStatus()), "Esperado " + esperado + ", recebido " + pedido.getStatus());
    }
    private static void proibida(Main.Pedido pedido, Runnable acao) {
        String antes = pedido.getStatus();
        try { acao.run(); }
        catch (IllegalStateException esperada) { status(pedido, antes); return; }
        throw new AssertionError("A operação deveria ser proibida");
    }
    private static final class Coletor implements Main.ObservadorPedido {
        final List<String> eventos = new ArrayList<>();
        public void aoMudarStatus(String anterior, String atual) { eventos.add(anterior + " -> " + atual); }
    }
    public static void main(String[] args) {
        Main.Pedido pedido = new Main.Pedido();
        Coletor cliente = new Coletor();
        Coletor loja = new Coletor();
        pedido.adicionarObservador(cliente);
        pedido.adicionarObservador(loja);
        status(pedido, "Aguardando pagamento");
        proibida(pedido, pedido::enviar);
        verificar(cliente.eventos.isEmpty(), "Ação inválida não deve notificar");
        pedido.pagar();
        pedido.enviar();
        pedido.entregar();
        status(pedido, "Entregue");
        verificar(cliente.eventos.size() == 3, "Cliente recebe uma notificação por transição");
        verificar(loja.eventos.equals(cliente.eventos), "Todos os observadores recebem os mesmos eventos");
        verificar(cliente.eventos.get(0).equals("Aguardando pagamento -> Pago"), "Primeira transição correta");
        verificar(cliente.eventos.get(2).equals("Enviado -> Entregue"), "Última transição correta");
        proibida(pedido, pedido::cancelar);
        verificar(cliente.eventos.size() == 3, "Estado final não gera evento em operação inválida");
        pedido.removerObservador(loja);
        Main.Pedido cancelamento = new Main.Pedido();
        Coletor removido = new Coletor();
        cancelamento.adicionarObservador(removido);
        cancelamento.removerObservador(removido);
        cancelamento.cancelar();
        status(cancelamento, "Cancelado");
        verificar(removido.eventos.isEmpty(), "Observador removido não recebe notificações");
        verificar(cliente.eventos.size() == 3, "Pedidos mantêm listas independentes de observadores");
        System.out.println(verificacoes + " verificações aprovadas.");
    }
}
