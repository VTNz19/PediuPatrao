package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.service.PedidoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    public record NovoPedidoRequest(String clienteId, List<String> produtoIds, List<Integer> quantidades) {
    }

    @PostMapping
    public Pedido criarPedido(@RequestBody NovoPedidoRequest request) {
        return pedidoService.criarPedido(request.clienteId(), request.produtoIds(), request.quantidades());
    }

    @GetMapping
    public List<Pedido> listarPedidos() {
        return pedidoService.listarPedidos();
    }

    @PutMapping("/{id}/avancar")
    public Pedido avancarStatus(@PathVariable String id) {
        return pedidoService.avancarStatus(id);
    }

    @PutMapping("/{id}/cancelar")
    public Pedido cancelarPedido(@PathVariable String id, @RequestBody Map<String, String> body) {
        return pedidoService.cancelarPedido(id, body.get("justificativa"));
    }

    @PutMapping("/{id}/desconto")
    public Pedido aplicarDesconto(@PathVariable String id, @RequestBody Map<String, Double> body) {
        return pedidoService.aplicarDesconto(id, body.get("percentual"));
    }
}
