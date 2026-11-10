package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.exception.ApiExceptionHandler;
import com.umc.pediupatrao.repository.ClienteRepository;
import com.umc.pediupatrao.repository.PedidoRepository;
import com.umc.pediupatrao.repository.ProdutoRepository;
import com.umc.pediupatrao.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Contrato HTTP da API: códigos de status, formato de erro e validação de parâmetros. */
class DashboardControllerTest {

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        DashboardService service = new DashboardService(
                Mockito.mock(PedidoRepository.class), Mockito.mock(ClienteRepository.class), Mockito.mock(ProdutoRepository.class));
        mvc = MockMvcBuilders.standaloneSetup(new DashboardController(service))
                .setControllerAdvice(new ApiExceptionHandler())
                // Mesmo comportamento do Spring Boot: datas em ISO-8601 (não em array de números).
                .setMessageConverters(new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(
                        org.springframework.http.converter.json.Jackson2ObjectMapperBuilder.json()
                                .featuresToDisable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                                .build()))
                .build();
    }

    @Test
    void resumoRetorna200ComIndicadores() throws Exception {
        mvc.perform(get("/api/dashboard/resumo").param("de", "2025-01-01").param("ate", "2025-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPedidos").value(0))
                .andExpect(jsonPath("$.faturamento").value(0.0))
                .andExpect(jsonPath("$.de").value("2025-01-01"));
    }

    @Test
    void vendasPorHoraRetorna24Posicoes() throws Exception {
        mvc.perform(get("/api/dashboard/vendas/por-hora"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(24));
    }

    @Test
    void dataMalFormatadaRetorna400() throws Exception {
        mvc.perform(get("/api/dashboard/resumo").param("de", "31/01/2025"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value(org.hamcrest.Matchers.containsString("yyyy-MM-dd")));
    }

    @Test
    void periodoInvertidoRetorna400() throws Exception {
        mvc.perform(get("/api/dashboard/vendas").param("de", "2025-02-01").param("ate", "2025-01-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").exists());
    }

    @Test
    void statusInvalidoRetorna400() throws Exception {
        mvc.perform(get("/api/dashboard/pedidos/por-status").param("status", "XPTO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value(org.hamcrest.Matchers.containsString("Status inválido")));
    }

    @Test
    void limiteNaoNumericoRetorna400() throws Exception {
        mvc.perform(get("/api/dashboard/produtos/mais-vendidos").param("limite", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void limiteForaDaFaixaRetorna400() throws Exception {
        mvc.perform(get("/api/dashboard/produtos/mais-vendidos").param("limite", "0"))
                .andExpect(status().isBadRequest());
    }
}
