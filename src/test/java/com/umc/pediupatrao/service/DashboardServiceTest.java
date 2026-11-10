package com.umc.pediupatrao.service;

import com.umc.pediupatrao.dto.DashboardDtos.*;
import com.umc.pediupatrao.entity.ItemPedido;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.exception.NegocioException;
import com.umc.pediupatrao.repository.ClienteRepository;
import com.umc.pediupatrao.repository.PedidoRepository;
import com.umc.pediupatrao.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private ProdutoRepository produtoRepository;

    private DashboardService service;

    private final LocalDate hoje = LocalDate.now();
    private final LocalDate ontem = hoje.minusDays(1);

    @BeforeEach
    void setUp() {
        service = new DashboardService(pedidoRepository, clienteRepository, produtoRepository);

        // p1: hoje 10h, finalizado, 2x Pizza (50) = 100
        Pedido p1 = pedido("p1", "c1", "FINALIZADO", 100, 0, hoje.atTime(10, 0),
                new ItemPedido("pizza", "Pizza Mussarela", 2, 50));
        // p2: hoje 12h, recebido, 1x Pizza + 2x Refri (20) = 90, 20% off => 72
        Pedido p2 = pedido("p2", "c2", "RECEBIDO", 72, 18, hoje.atTime(12, 30),
                new ItemPedido("pizza", "Pizza Mussarela", 1, 50), new ItemPedido("refri", "Refrigerante", 2, 20));
        // p3: ontem 20h, cancelado — só conta como cancelado
        Pedido p3 = pedido("p3", "c1", "CANCELADO", 50, 0, ontem.atTime(20, 0),
                new ItemPedido("esfiha", "Esfiha", 1, 50));

        // Só o período [ontem, hoje] retorna dados; o período anterior cai no default (lista vazia).
        lenient().when(pedidoRepository.findByDataHoraEntradaGreaterThanEqualAndDataHoraEntradaLessThan(
                ontem.atStartOfDay(), hoje.plusDays(1).atStartOfDay())).thenReturn(List.of(p1, p2, p3));

        lenient().when(produtoRepository.findAllById(any())).thenReturn(List.of(
                produto("pizza", "Pizza"), produto("refri", "Bebida"), produto("esfiha", "Esfiha")));
    }

    @Test
    void resumoCalculaIndicadoresIgnorandoCancelados() {
        Resumo r = service.resumo(ontem, hoje, null);

        assertThat(r.totalPedidos()).isEqualTo(3);
        assertThat(r.pedidosValidos()).isEqualTo(2);
        assertThat(r.faturamento()).isEqualTo(172.0);
        assertThat(r.ticketMedio()).isEqualTo(86.0);
        assertThat(r.descontoTotal()).isEqualTo(18.0);
        assertThat(r.pedidosCancelados()).isEqualTo(1);
        assertThat(r.taxaCancelamento()).isEqualTo(33.33);
        assertThat(r.pedidosEmAndamento()).isEqualTo(1);
        assertThat(r.clientesAtendidos()).isEqualTo(2);
        assertThat(r.variacaoFaturamento()).isNull(); // período anterior sem vendas
    }

    @Test
    void resumoComFiltroDeStatus() {
        Resumo r = service.resumo(ontem, hoje, "finalizado");

        assertThat(r.totalPedidos()).isEqualTo(1);
        assertThat(r.faturamento()).isEqualTo(100.0);
    }

    @Test
    void resumoVazioNaoDividePorZero() {
        Resumo r = service.resumo(hoje.minusDays(100), hoje.minusDays(50), null);

        assertThat(r.totalPedidos()).isZero();
        assertThat(r.ticketMedio()).isZero();
        assertThat(r.taxaCancelamento()).isZero();
    }

    @Test
    void vendasPorDiaPreencheDiasSemVendas() {
        List<PontoVenda> v = service.vendas(hoje.minusDays(2), hoje, null, "dia");

        assertThat(v).hasSize(3);
        assertThat(v.get(2).faturamento()).isZero(); // sem stub para esse intervalo
    }

    @Test
    void vendasPorDiaSomaFaturamentoDoDia() {
        List<PontoVenda> v = service.vendas(ontem, hoje, null, "dia");

        assertThat(v).hasSize(2);
        assertThat(v.get(0).faturamento()).isZero();       // ontem: só cancelado
        assertThat(v.get(1).faturamento()).isEqualTo(172.0);
        assertThat(v.get(1).pedidos()).isEqualTo(2);
    }

    @Test
    void vendasAgrupadasPorMesConsolidaPeriodo() {
        List<PontoVenda> v = service.vendas(ontem, hoje, null, "mes");

        assertThat(v.stream().mapToDouble(PontoVenda::faturamento).sum()).isEqualTo(172.0);
        assertThat(v.size()).isBetween(1, 2); // 1 mês, ou 2 se ontem for outro mês
    }

    @Test
    void vendasPorHoraRetorna24Faixas() {
        List<VendaPorHora> h = service.vendasPorHora(ontem, hoje, null);

        assertThat(h).hasSize(24);
        assertThat(h.get(10).pedidos()).isEqualTo(1);
        assertThat(h.get(12).faturamento()).isEqualTo(72.0);
        assertThat(h.get(20).pedidos()).isZero(); // cancelado não conta
    }

    @Test
    void pedidosPorStatusTrazTodosOsStatusComPercentual() {
        List<StatusQuantidade> s = service.pedidosPorStatus(ontem, hoje, null);

        assertThat(s).hasSize(6);
        assertThat(s).filteredOn(x -> x.status().equals("CANCELADO")).first()
                .satisfies(x -> {
                    assertThat(x.quantidade()).isEqualTo(1);
                    assertThat(x.percentual()).isEqualTo(33.33);
                });
        assertThat(s).filteredOn(x -> x.status().equals("PRONTO")).first()
                .satisfies(x -> assertThat(x.quantidade()).isZero());
    }

    @Test
    void rankingOrdenaPorQuantidadeEExcluiCancelados() {
        List<ProdutoRanking> r = service.produtosMaisVendidos(ontem, hoje, null, null, null);

        assertThat(r).extracting(ProdutoRanking::nome).containsExactly("Pizza Mussarela", "Refrigerante");
        assertThat(r.get(0).quantidade()).isEqualTo(3);
        assertThat(r.get(0).receita()).isEqualTo(150.0);
        assertThat(r.get(0).tipo()).isEqualTo("Pizza");
    }

    @Test
    void rankingFiltraPorTipoELimite() {
        assertThat(service.produtosMaisVendidos(ontem, hoje, null, "bebida", null))
                .extracting(ProdutoRanking::nome).containsExactly("Refrigerante");
        assertThat(service.produtosMaisVendidos(ontem, hoje, null, null, 1)).hasSize(1);
    }

    @Test
    void categoriasSomamReceitaPorTipo() {
        List<CategoriaVenda> c = service.vendasPorCategoria(ontem, hoje, null);

        assertThat(c).extracting(CategoriaVenda::tipo).containsExactly("Pizza", "Bebida");
        assertThat(c.get(0).receita()).isEqualTo(150.0);
    }

    @Test
    void pedidosRecentesMaisNovoPrimeiroELimite() {
        List<PedidoRecente> r = service.pedidosRecentes(ontem, hoje, null, 2);

        assertThat(r).extracting(PedidoRecente::id).containsExactly("p2", "p1");
        assertThat(r.get(0).cliente()).isEqualTo("—"); // cliente não encontrado no mock
    }

    // ---- validações / erros ----

    @Test
    void rejeitaDataInicialPosteriorAFinal() {
        assertThatThrownBy(() -> service.resumo(hoje, ontem, null))
                .isInstanceOf(NegocioException.class).hasMessageContaining("posterior");
    }

    @Test
    void rejeitaPeriodoMaiorQueUmAno() {
        assertThatThrownBy(() -> service.resumo(hoje.minusDays(400), hoje, null))
                .isInstanceOf(NegocioException.class).hasMessageContaining("366");
    }

    @Test
    void rejeitaStatusInvalido() {
        assertThatThrownBy(() -> service.resumo(ontem, hoje, "XPTO"))
                .isInstanceOf(NegocioException.class).hasMessageContaining("Status inválido");
    }

    @Test
    void rejeitaAgrupamentoInvalido() {
        assertThatThrownBy(() -> service.vendas(ontem, hoje, null, "ano"))
                .isInstanceOf(NegocioException.class).hasMessageContaining("Agrupamento inválido");
    }

    @Test
    void rejeitaLimiteForaDaFaixa() {
        assertThatThrownBy(() -> service.produtosMaisVendidos(ontem, hoje, null, null, 0))
                .isInstanceOf(NegocioException.class);
        assertThatThrownBy(() -> service.pedidosRecentes(ontem, hoje, null, 51))
                .isInstanceOf(NegocioException.class);
    }

    @Test
    void semParametrosUsaUltimos30Dias() {
        Resumo r = service.resumo(null, null, null);

        assertThat(r.ate()).isEqualTo(hoje);
        assertThat(r.de()).isEqualTo(hoje.minusDays(29));
    }

    // ---- helpers ----

    private static Pedido pedido(String id, String clienteId, String status, double total, double desconto,
                                 LocalDateTime entrada, ItemPedido... itens) {
        Pedido p = new Pedido();
        p.setId(id);
        p.setClienteId(clienteId);
        p.setStatus(status);
        p.setValorTotal(total);
        p.setValorDesconto(desconto);
        p.setDataHoraEntrada(entrada);
        p.setItens(List.of(itens));
        return p;
    }

    private static Produto produto(String id, String tipo) {
        Produto p = new Produto();
        p.setId(id);
        p.setTipo(tipo);
        return p;
    }
}
