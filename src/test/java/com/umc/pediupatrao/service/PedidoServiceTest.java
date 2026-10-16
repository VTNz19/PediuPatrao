package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.entity.StatusPedido;
import com.umc.pediupatrao.exception.NegocioException;
import com.umc.pediupatrao.repository.ClienteRepository;
import com.umc.pediupatrao.repository.PedidoRepository;
import com.umc.pediupatrao.repository.ProdutoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private ProdutoRepository produtoRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private AuditoriaService auditoriaService;

    private PedidoService pedidoService;

    @BeforeEach
    void setUp() {
        pedidoService = new PedidoService(pedidoRepository, produtoRepository, clienteRepository, auditoriaService);
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("atendente01", null));
        // save() simplesmente devolve o próprio objeto recebido, como o Mongo faria.
        lenient().when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Produto produto(String id, String nome, double preco) {
        Produto produto = new Produto();
        produto.setId(id);
        produto.setNome(nome);
        produto.setPreco(preco);
        return produto;
    }

    @Test
    void criarPedido_calculaValorTotalAPartirDosItens() {
        when(clienteRepository.existsById("cliente1")).thenReturn(true);
        when(produtoRepository.findById("p1")).thenReturn(Optional.of(produto("p1", "Pizza", 50.0)));
        when(produtoRepository.findById("p2")).thenReturn(Optional.of(produto("p2", "Refrigerante", 8.0)));

        Pedido pedido = pedidoService.criarPedido("cliente1", List.of("p1", "p2"), List.of(2, 1));

        assertThat(pedido.getValorBruto()).isEqualTo(108.0);
        assertThat(pedido.getValorTotal()).isEqualTo(108.0);
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.RECEBIDO.name());
        assertThat(pedido.getResponsavelRegistro()).isEqualTo("atendente01");
    }

    @Test
    void criarPedido_ignoraPrecoEnviadoPeloCliente() {
        when(clienteRepository.existsById("cliente1")).thenReturn(true);
        // O catálogo diz R$ 50, não importa o que a requisição "tentasse" mandar.
        when(produtoRepository.findById("p1")).thenReturn(Optional.of(produto("p1", "Pizza", 50.0)));

        Pedido pedido = pedidoService.criarPedido("cliente1", List.of("p1"), List.of(1));

        assertThat(pedido.getItens().get(0).getPrecoUnitario()).isEqualTo(50.0);
    }

    @Test
    void avancarStatus_bloqueiaTransicaoAposFinalizacao() {
        Pedido pedido = new Pedido();
        pedido.setId("ped1");
        pedido.setStatus(StatusPedido.FINALIZADO.name());
        when(pedidoRepository.findById("ped1")).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.avancarStatus("ped1"))
                .isInstanceOf(NegocioException.class);
    }

    @Test
    void cancelarPedido_semJustificativa_lancaExcecao() {
        assertThatThrownBy(() -> pedidoService.cancelarPedido("ped1", "   "))
                .isInstanceOf(NegocioException.class);
    }

    @Test
    void cancelarPedido_bloqueadoAposSaida() {
        Pedido pedido = new Pedido();
        pedido.setId("ped1");
        pedido.setStatus(StatusPedido.SAIU_PARA_ENTREGA_RETIRADO.name());
        when(pedidoRepository.findById("ped1")).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.cancelarPedido("ped1", "cliente desistiu"))
                .isInstanceOf(NegocioException.class);
    }

    @Test
    void aplicarDesconto_acimaDoLimitePermitido_lancaExcecao() {
        assertThatThrownBy(() -> pedidoService.aplicarDesconto("ped1", 25.0))
                .isInstanceOf(NegocioException.class);
    }

    @Test
    void aplicarDesconto_calculaValorComDescontoCorretamente() {
        Pedido pedido = new Pedido();
        pedido.setId("ped1");
        pedido.setStatus(StatusPedido.RECEBIDO.name());
        pedido.setValorBruto(100.0);
        pedido.setValorTotal(100.0);
        when(pedidoRepository.findById("ped1")).thenReturn(Optional.of(pedido));

        Pedido atualizado = pedidoService.aplicarDesconto("ped1", 10.0);

        assertThat(atualizado.getValorDesconto()).isEqualTo(10.0);
        assertThat(atualizado.getValorTotal()).isEqualTo(90.0);
    }
}
