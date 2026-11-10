package com.umc.pediupatrao.service;

import com.umc.pediupatrao.dto.DashboardDtos.*;
import com.umc.pediupatrao.entity.ItemPedido;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.entity.StatusPedido;
import com.umc.pediupatrao.exception.NegocioException;
import com.umc.pediupatrao.repository.ClienteRepository;
import com.umc.pediupatrao.repository.PedidoRepository;
import com.umc.pediupatrao.repository.ProdutoRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Agregações que alimentam o dashboard administrativo.
 * <p>
 * Regras: o período é filtrado pela data de entrada do pedido; pedidos CANCELADO não entram em
 * faturamento, ticket médio, ranking nem categorias (só nas contagens); o faturamento usa o
 * valor total já com desconto.
 */
@Service
@PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
public class DashboardService {

    public static final int PERIODO_PADRAO_DIAS = 30;
    public static final int PERIODO_MAXIMO_DIAS = 366;
    public static final int LIMITE_MAXIMO = 50;
    public static final List<String> AGRUPAMENTOS = List.of("dia", "semana", "mes");

    private static final String SEM_TIPO = "Outros";

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;

    public DashboardService(PedidoRepository pedidoRepository,
                            ClienteRepository clienteRepository,
                            ProdutoRepository produtoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
    }

    // ------------------------------------------------------------------ resumo (RF01)

    public Resumo resumo(LocalDate de, LocalDate ate, String status) {
        LocalDate[] p = periodo(de, ate);
        StatusPedido st = parseStatus(status);
        List<Pedido> pedidos = buscar(p[0], p[1], st);
        List<Pedido> validos = pedidos.stream().filter(DashboardService::valido).toList();

        double faturamento = validos.stream().mapToDouble(Pedido::getValorTotal).sum();
        double desconto = validos.stream().mapToDouble(Pedido::getValorDesconto).sum();
        int cancelados = (int) pedidos.stream().filter(x -> !valido(x)).count();
        int andamento = (int) validos.stream().filter(DashboardService::emAndamento).count();
        long clientes = validos.stream().map(Pedido::getClienteId).filter(Objects::nonNull).distinct().count();

        long dias = ChronoUnit.DAYS.between(p[0], p[1]) + 1;
        List<Pedido> anteriores = buscar(p[0].minusDays(dias), p[0].minusDays(1), st);
        List<Pedido> anterioresValidos = anteriores.stream().filter(DashboardService::valido).toList();
        double faturamentoAnterior = anterioresValidos.stream().mapToDouble(Pedido::getValorTotal).sum();
        Double variacao = faturamentoAnterior > 0
                ? r2((faturamento - faturamentoAnterior) / faturamentoAnterior * 100.0)
                : null;

        return new Resumo(p[0], p[1], pedidos.size(), validos.size(), r2(faturamento),
                validos.isEmpty() ? 0.0 : r2(faturamento / validos.size()),
                r2(desconto), cancelados,
                pedidos.isEmpty() ? 0.0 : r2(cancelados * 100.0 / pedidos.size()),
                andamento, (int) clientes, r2(faturamentoAnterior), anteriores.size(), variacao);
    }

    // ------------------------------------------------------------------ vendas (RF02)

    public List<PontoVenda> vendas(LocalDate de, LocalDate ate, String status, String agrupamento) {
        LocalDate[] p = periodo(de, ate);
        StatusPedido st = parseStatus(status);
        String agrup = agrupamento == null || agrupamento.isBlank() ? "dia" : agrupamento.toLowerCase();
        if (!AGRUPAMENTOS.contains(agrup)) {
            throw new NegocioException("Agrupamento inválido: " + agrupamento + ". Valores aceitos: " + AGRUPAMENTOS + ".");
        }

        Function<LocalDate, String> chave = switch (agrup) {
            case "semana" -> d -> d.with(DayOfWeek.MONDAY).toString();
            case "mes" -> d -> YearMonth.from(d).toString();
            default -> LocalDate::toString;
        };
        Function<String, String> rotulo = switch (agrup) {
            case "semana" -> k -> "Sem. " + LocalDate.parse(k).format(DateTimeFormatter.ofPattern("dd/MM"));
            case "mes" -> k -> YearMonth.parse(k).format(DateTimeFormatter.ofPattern("MM/yyyy"));
            default -> k -> LocalDate.parse(k).format(DateTimeFormatter.ofPattern("dd/MM"));
        };

        // Preenche períodos sem vendas com zero, para o gráfico não "pular" datas.
        Map<String, double[]> serie = new LinkedHashMap<>();
        for (LocalDate d = p[0]; !d.isAfter(p[1]); d = d.plusDays(1)) {
            serie.putIfAbsent(chave.apply(d), new double[2]);
        }
        buscar(p[0], p[1], st).stream().filter(DashboardService::valido).forEach(x -> {
            double[] acc = serie.get(chave.apply(x.getDataHoraEntrada().toLocalDate()));
            acc[0] += x.getValorTotal();
            acc[1] += 1;
        });

        return serie.entrySet().stream()
                .map(e -> new PontoVenda(e.getKey(), rotulo.apply(e.getKey()), r2(e.getValue()[0]), (int) e.getValue()[1]))
                .toList();
    }

    public List<VendaPorHora> vendasPorHora(LocalDate de, LocalDate ate, String status) {
        LocalDate[] p = periodo(de, ate);
        double[][] horas = new double[24][2];
        buscar(p[0], p[1], parseStatus(status)).stream().filter(DashboardService::valido).forEach(x -> {
            int h = x.getDataHoraEntrada().getHour();
            horas[h][0] += x.getValorTotal();
            horas[h][1] += 1;
        });
        List<VendaPorHora> out = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            out.add(new VendaPorHora(h, r2(horas[h][0]), (int) horas[h][1]));
        }
        return out;
    }

    public List<CategoriaVenda> vendasPorCategoria(LocalDate de, LocalDate ate, String status) {
        LocalDate[] p = periodo(de, ate);
        List<Pedido> validos = buscar(p[0], p[1], parseStatus(status)).stream().filter(DashboardService::valido).toList();
        Map<String, String> tipos = tiposPorProduto(validos);
        Map<String, double[]> acc = new HashMap<>();
        for (Pedido pedido : validos) {
            for (ItemPedido it : itens(pedido)) {
                double[] v = acc.computeIfAbsent(tipos.getOrDefault(it.getProdutoId(), SEM_TIPO), k -> new double[2]);
                v[0] += it.getQuantidade();
                v[1] += it.getSubtotal();
            }
        }
        return acc.entrySet().stream()
                .map(e -> new CategoriaVenda(e.getKey(), (int) e.getValue()[0], r2(e.getValue()[1])))
                .sorted(Comparator.comparingDouble(CategoriaVenda::receita).reversed())
                .toList();
    }

    // ------------------------------------------------------------------ situação dos pedidos (RF03)

    public List<StatusQuantidade> pedidosPorStatus(LocalDate de, LocalDate ate, String status) {
        LocalDate[] p = periodo(de, ate);
        List<Pedido> pedidos = buscar(p[0], p[1], parseStatus(status));
        Map<String, Integer> cont = new LinkedHashMap<>();
        for (StatusPedido s : StatusPedido.values()) {
            cont.put(s.name(), 0);
        }
        pedidos.forEach(x -> cont.computeIfPresent(String.valueOf(x.getStatus()), (k, v) -> v + 1));
        int total = pedidos.size();
        return cont.entrySet().stream()
                .map(e -> new StatusQuantidade(e.getKey(), e.getValue(),
                        total == 0 ? 0.0 : r2(e.getValue() * 100.0 / total)))
                .toList();
    }

    public List<PedidoRecente> pedidosRecentes(LocalDate de, LocalDate ate, String status, Integer limite) {
        LocalDate[] p = periodo(de, ate);
        int lim = limite(limite, 10);
        List<Pedido> recentes = buscar(p[0], p[1], parseStatus(status)).stream()
                .sorted(Comparator.comparing(Pedido::getDataHoraEntrada).reversed())
                .limit(lim)
                .toList();
        Map<String, String> nomes = new HashMap<>();
        clienteRepository.findAllById(recentes.stream().map(Pedido::getClienteId).filter(Objects::nonNull).distinct().toList())
                .forEach(c -> nomes.put(c.getId(), c.getNome()));
        return recentes.stream()
                .map(x -> new PedidoRecente(x.getId(), nomes.getOrDefault(x.getClienteId(), "—"),
                        x.getStatus(), r2(x.getValorTotal()), x.getDataHoraEntrada()))
                .toList();
    }

    // ------------------------------------------------------------------ produtos mais vendidos (RF04)

    public List<ProdutoRanking> produtosMaisVendidos(LocalDate de, LocalDate ate, String status, String tipo, Integer limite) {
        LocalDate[] p = periodo(de, ate);
        int lim = limite(limite, 5);
        List<Pedido> validos = buscar(p[0], p[1], parseStatus(status)).stream().filter(DashboardService::valido).toList();
        Map<String, String> tipos = tiposPorProduto(validos);

        Map<String, Object[]> acc = new HashMap<>(); // produtoId -> [nome, tipo, qtd, receita]
        for (Pedido pedido : validos) {
            for (ItemPedido it : itens(pedido)) {
                String t = tipos.getOrDefault(it.getProdutoId(), SEM_TIPO);
                if (tipo != null && !tipo.isBlank() && !t.equalsIgnoreCase(tipo.trim())) {
                    continue;
                }
                Object[] v = acc.computeIfAbsent(it.getProdutoId(),
                        k -> new Object[]{it.getNomeProduto(), t, 0, 0.0});
                v[2] = (int) v[2] + it.getQuantidade();
                v[3] = (double) v[3] + it.getSubtotal();
            }
        }
        return acc.entrySet().stream()
                .map(e -> new ProdutoRanking(e.getKey(), (String) e.getValue()[0], (String) e.getValue()[1],
                        (int) e.getValue()[2], r2((double) e.getValue()[3])))
                .sorted(Comparator.comparingInt(ProdutoRanking::quantidade).reversed()
                        .thenComparing(Comparator.comparingDouble(ProdutoRanking::receita).reversed()))
                .limit(lim)
                .toList();
    }

    // ------------------------------------------------------------------ filtros (RF05)

    public Filtros filtros() {
        List<String> tipos = produtoRepository.findAll().stream()
                .map(Produto::getTipo).filter(t -> t != null && !t.isBlank())
                .distinct().sorted().toList();
        return new Filtros(Arrays.stream(StatusPedido.values()).map(Enum::name).toList(), tipos, AGRUPAMENTOS);
    }

    // ------------------------------------------------------------------ utilitários

    /** Resolve e valida o período: padrão = últimos 30 dias até hoje. Retorna {de, ate}. */
    static LocalDate[] periodo(LocalDate de, LocalDate ate) {
        LocalDate fim = ate != null ? ate : LocalDate.now();
        LocalDate ini = de != null ? de : fim.minusDays(PERIODO_PADRAO_DIAS - 1);
        if (ini.isAfter(fim)) {
            throw new NegocioException("A data inicial (de) não pode ser posterior à data final (ate).");
        }
        if (ChronoUnit.DAYS.between(ini, fim) + 1 > PERIODO_MAXIMO_DIAS) {
            throw new NegocioException("O período máximo de consulta é de " + PERIODO_MAXIMO_DIAS + " dias.");
        }
        return new LocalDate[]{ini, fim};
    }

    static StatusPedido parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        StatusPedido s = StatusPedido.fromString(status.trim().toUpperCase());
        if (s == null) {
            throw new NegocioException("Status inválido: " + status + ". Valores aceitos: "
                    + Arrays.stream(StatusPedido.values()).map(Enum::name).collect(Collectors.joining(", ")) + ".");
        }
        return s;
    }

    private static int limite(Integer limite, int padrao) {
        if (limite == null) {
            return padrao;
        }
        if (limite < 1 || limite > LIMITE_MAXIMO) {
            throw new NegocioException("O limite deve estar entre 1 e " + LIMITE_MAXIMO + ".");
        }
        return limite;
    }

    private List<Pedido> buscar(LocalDate de, LocalDate ate, StatusPedido status) {
        List<Pedido> lista = pedidoRepository
                .findByDataHoraEntradaGreaterThanEqualAndDataHoraEntradaLessThan(de.atStartOfDay(), ate.plusDays(1).atStartOfDay());
        return lista.stream()
                .filter(x -> x.getDataHoraEntrada() != null)
                .filter(x -> status == null || status.name().equals(x.getStatus()))
                .toList();
    }

    private Map<String, String> tiposPorProduto(List<Pedido> pedidos) {
        Set<String> ids = pedidos.stream().flatMap(x -> itens(x).stream())
                .map(ItemPedido::getProdutoId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<String, String> tipos = new HashMap<>();
        if (!ids.isEmpty()) {
            produtoRepository.findAllById(ids).forEach(pr ->
                    tipos.put(pr.getId(), pr.getTipo() == null || pr.getTipo().isBlank() ? SEM_TIPO : pr.getTipo()));
        }
        return tipos;
    }

    private static List<ItemPedido> itens(Pedido p) {
        return p.getItens() == null ? List.of() : p.getItens();
    }

    private static boolean valido(Pedido p) {
        return !StatusPedido.CANCELADO.name().equals(p.getStatus());
    }

    private static boolean emAndamento(Pedido p) {
        StatusPedido s = StatusPedido.fromString(p.getStatus());
        return s != null && !s.isTerminal();
    }

    private static double r2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
