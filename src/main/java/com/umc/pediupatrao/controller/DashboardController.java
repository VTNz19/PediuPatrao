package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.dto.DashboardDtos.*;
import com.umc.pediupatrao.service.DashboardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/** API de leitura que alimenta o dashboard. Acesso: ADMIN e GERENTE (ver SecurityConfig). */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping("/filtros")
    public Filtros filtros() {
        return service.filtros();
    }

    @GetMapping("/resumo")
    public Resumo resumo(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
                         @RequestParam(required = false) String status) {
        return service.resumo(de, ate, status);
    }

    @GetMapping("/vendas")
    public List<PontoVenda> vendas(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
                                   @RequestParam(required = false) String status,
                                   @RequestParam(required = false) String agrupamento) {
        return service.vendas(de, ate, status, agrupamento);
    }

    @GetMapping("/vendas/por-hora")
    public List<VendaPorHora> vendasPorHora(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
                                            @RequestParam(required = false) String status) {
        return service.vendasPorHora(de, ate, status);
    }

    @GetMapping("/vendas/por-categoria")
    public List<CategoriaVenda> vendasPorCategoria(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
                                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
                                                   @RequestParam(required = false) String status) {
        return service.vendasPorCategoria(de, ate, status);
    }

    @GetMapping("/pedidos/por-status")
    public List<StatusQuantidade> pedidosPorStatus(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
                                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
                                                   @RequestParam(required = false) String status) {
        return service.pedidosPorStatus(de, ate, status);
    }

    @GetMapping("/pedidos/recentes")
    public List<PedidoRecente> pedidosRecentes(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
                                               @RequestParam(required = false) String status,
                                               @RequestParam(required = false) Integer limite) {
        return service.pedidosRecentes(de, ate, status, limite);
    }

    @GetMapping("/produtos/mais-vendidos")
    public List<ProdutoRanking> produtosMaisVendidos(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
                                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
                                                     @RequestParam(required = false) String status,
                                                     @RequestParam(required = false) String tipo,
                                                     @RequestParam(required = false) Integer limite) {
        return service.produtosMaisVendidos(de, ate, status, tipo, limite);
    }
}
