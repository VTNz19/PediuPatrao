package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.CampoAlterado;
import com.umc.pediupatrao.entity.ItemPedido;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.entity.StatusPedido;
import com.umc.pediupatrao.exception.NegocioException;
import com.umc.pediupatrao.repository.ClienteRepository;
import com.umc.pediupatrao.repository.PedidoRepository;
import com.umc.pediupatrao.repository.ProdutoRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PedidoService {

    private static final double DESCONTO_MAXIMO_PERCENTUAL = 20.0;

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final ClienteRepository clienteRepository;
    private final AuditoriaService auditoriaService;

    public PedidoService(PedidoRepository pedidoRepository,
                          ProdutoRepository produtoRepository,
                          ClienteRepository clienteRepository,
                          AuditoriaService auditoriaService) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
        this.clienteRepository = clienteRepository;
        this.auditoriaService = auditoriaService;
    }

    private String usuarioAtual() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @PreAuthorize("hasAnyRole('ATENDENTE','GERENTE')")
    public Pedido criarPedido(String clienteId, List<String> produtoIds, List<Integer> quantidades) {
        if (clienteId == null || !clienteRepository.existsById(clienteId)) {
            throw new NegocioException("Cliente não encontrado.");
        }
        if (produtoIds == null || produtoIds.isEmpty() || quantidades == null
                || produtoIds.size() != quantidades.size()) {
            throw new NegocioException("O pedido precisa de ao menos um item válido.");
        }

        List<ItemPedido> itens = new ArrayList<>();
        double valorBruto = 0.0;
        for (int i = 0; i < produtoIds.size(); i++) {
            int quantidade = quantidades.get(i);
            if (quantidade <= 0) {
                throw new NegocioException("Quantidade deve ser maior que zero.");
            }
            String produtoId = produtoIds.get(i);
            Produto produto = produtoRepository.findById(produtoId)
                    .orElseThrow(() -> new NegocioException("Produto não encontrado: " + produtoId));

            // Preço sempre lido do catálogo no servidor — nunca aceito do cliente.
            ItemPedido item = new ItemPedido(produto.getId(), produto.getNome(), quantidade, produto.getPreco());
            itens.add(item);
            valorBruto += item.getSubtotal();
        }

        Pedido pedido = new Pedido();
        pedido.setClienteId(clienteId);
        pedido.setItens(itens);
        pedido.setStatus(StatusPedido.RECEBIDO.name());
        pedido.setValorBruto(valorBruto);
        pedido.setPercentualDesconto(0.0);
        pedido.setValorDesconto(0.0);
        pedido.setValorTotal(valorBruto);
        pedido.setResponsavelRegistro(usuarioAtual());
        pedido.setDataHoraEntrada(LocalDateTime.now());

        Pedido salvo = pedidoRepository.save(pedido);

        auditoriaService.registrar("CRIACAO_PEDIDO", "Pedido", salvo.getId(),
                List.of(new CampoAlterado("status", null, salvo.getStatus()),
                        new CampoAlterado("valorTotal", null, String.valueOf(salvo.getValorTotal()))),
                null);

        return salvo;
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','ATENDENTE')")
    public List<Pedido> listarPedidos() {
        return pedidoRepository.findAll();
    }

    private Pedido buscarOuFalhar(String id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new NegocioException("Pedido não encontrado."));
    }

    @PreAuthorize("hasAnyRole('ATENDENTE','GERENTE')")
    public Pedido avancarStatus(String id) {
        Pedido pedido = buscarOuFalhar(id);
        StatusPedido atual = StatusPedido.fromString(pedido.getStatus());
        if (atual == null || atual.isTerminal()) {
            throw new NegocioException("Este pedido não pode mais mudar de status.");
        }
        StatusPedido proximo = atual.proximo();
        if (proximo == null) {
            throw new NegocioException("Transição de status inválida a partir de " + atual + ".");
        }

        String statusAnterior = pedido.getStatus();
        pedido.setStatus(proximo.name());
        if (proximo == StatusPedido.SAIU_PARA_ENTREGA_RETIRADO) {
            pedido.setResponsavelSaida(usuarioAtual());
            pedido.setDataHoraSaida(LocalDateTime.now());
        }

        Pedido salvo = pedidoRepository.save(pedido);

        auditoriaService.registrar("ALTERACAO_STATUS", "Pedido", salvo.getId(),
                List.of(new CampoAlterado("status", statusAnterior, salvo.getStatus())),
                null);

        return salvo;
    }

    @PreAuthorize("hasRole('GERENTE')")
    public Pedido cancelarPedido(String id, String justificativa) {
        if (justificativa == null || justificativa.isBlank()) {
            throw new NegocioException("Cancelamento exige justificativa.");
        }
        Pedido pedido = buscarOuFalhar(id);
        StatusPedido atual = StatusPedido.fromString(pedido.getStatus());
        if (atual == null || !atual.podeSerCanceladoOuTerDesconto()) {
            throw new NegocioException("Pedido não pode mais ser cancelado (já saiu para entrega/retirada, já foi finalizado ou já está cancelado).");
        }

        String statusAnterior = pedido.getStatus();
        pedido.setStatus(StatusPedido.CANCELADO.name());
        pedido.setMotivoCancelamento(justificativa);
        pedido.setResponsavelCancelamento(usuarioAtual());
        pedido.setDataHoraCancelamento(LocalDateTime.now());

        Pedido salvo = pedidoRepository.save(pedido);

        auditoriaService.registrar("CANCELAMENTO_PEDIDO", "Pedido", salvo.getId(),
                List.of(new CampoAlterado("status", statusAnterior, salvo.getStatus())),
                justificativa);

        return salvo;
    }

    @PreAuthorize("hasRole('GERENTE')")
    public Pedido aplicarDesconto(String id, double percentual) {
        if (percentual <= 0 || percentual > DESCONTO_MAXIMO_PERCENTUAL) {
            throw new NegocioException("Desconto deve ser maior que 0% e no máximo " + DESCONTO_MAXIMO_PERCENTUAL + "%.");
        }
        Pedido pedido = buscarOuFalhar(id);
        StatusPedido atual = StatusPedido.fromString(pedido.getStatus());
        if (atual == null || !atual.podeSerCanceladoOuTerDesconto()) {
            throw new NegocioException("Desconto só pode ser aplicado antes da saída para entrega/retirada.");
        }

        double percentualAnterior = pedido.getPercentualDesconto();
        double valorTotalAnterior = pedido.getValorTotal();

        double valorDesconto = pedido.getValorBruto() * percentual / 100.0;
        pedido.setPercentualDesconto(percentual);
        pedido.setValorDesconto(valorDesconto);
        pedido.setValorTotal(pedido.getValorBruto() - valorDesconto);

        Pedido salvo = pedidoRepository.save(pedido);

        auditoriaService.registrar("APLICACAO_DESCONTO", "Pedido", salvo.getId(),
                List.of(new CampoAlterado("percentualDesconto", String.valueOf(percentualAnterior), String.valueOf(salvo.getPercentualDesconto())),
                        new CampoAlterado("valorTotal", String.valueOf(valorTotalAnterior), String.valueOf(salvo.getValorTotal()))),
                null);

        return salvo;
    }
}
