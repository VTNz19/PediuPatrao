package com.umc.pediupatrao.util;

import com.umc.pediupatrao.entity.CampoAlterado;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DiffUtilsTest {

    @Test
    void diff_retornaApenasCamposRealmenteAlterados() {
        List<CampoAlterado> resultado = DiffUtils.diff(
                new String[]{"logradouro", "numero", "cidade"},
                new String[]{"Rua das Flores", "10", "São Paulo"},
                new String[]{"Av. Brasil", "10", "São Paulo"});

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getCampo()).isEqualTo("logradouro");
        assertThat(resultado.get(0).getValorAnterior()).isEqualTo("Rua das Flores");
        assertThat(resultado.get(0).getValorNovo()).isEqualTo("Av. Brasil");
    }

    @Test
    void diff_semAlteracoes_retornaListaVazia() {
        List<CampoAlterado> resultado = DiffUtils.diff(
                new String[]{"nome"},
                new String[]{"João"},
                new String[]{"João"});

        assertThat(resultado).isEmpty();
    }
}
