package com.umc.pediupatrao.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Contratos de resposta da API do dashboard (/api/dashboard/**). */
public final class DashboardDtos {

    private DashboardDtos() {
    }

    /** Indicadores gerais do período filtrado, com comparativo ao período anterior de mesma duração. */
    public record Resumo(
            LocalDate de,
            LocalDate ate,
            int totalPedidos,
            int pedidosValidos,
            double faturamento,
            double ticketMedio,
            double descontoTotal,
            int pedidosCancelados,
            double taxaCancelamento,
            int pedidosEmAndamento,
            int clientesAtendidos,
            double faturamentoPeriodoAnterior,
            int pedidosPeriodoAnterior,
            Double variacaoFaturamento) {
    }

    public record PontoVenda(String periodo, String rotulo, double faturamento, int pedidos) {
    }

    public record VendaPorHora(int hora, double faturamento, int pedidos) {
    }

    public record StatusQuantidade(String status, int quantidade, double percentual) {
    }

    public record ProdutoRanking(String produtoId, String nome, String tipo, int quantidade, double receita) {
    }

    public record CategoriaVenda(String tipo, int quantidade, double receita) {
    }

    public record PedidoRecente(String id, String cliente, String status, double valorTotal,
                                LocalDateTime dataHoraEntrada) {
    }

    public record Filtros(List<String> status, List<String> tipos, List<String> agrupamentos) {
    }
}
