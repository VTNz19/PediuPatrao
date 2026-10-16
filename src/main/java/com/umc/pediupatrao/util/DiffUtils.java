package com.umc.pediupatrao.util;

import com.umc.pediupatrao.entity.CampoAlterado;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class DiffUtils {

    private DiffUtils() {
    }

    public static List<CampoAlterado> diff(String[] campos, String[] valoresAnteriores, String[] valoresNovos) {
        if (campos.length != valoresAnteriores.length || campos.length != valoresNovos.length) {
            throw new IllegalArgumentException("Arrays de diff precisam ter o mesmo tamanho.");
        }
        List<CampoAlterado> alterados = new ArrayList<>();
        for (int i = 0; i < campos.length; i++) {
            String anterior = valoresAnteriores[i];
            String novo = valoresNovos[i];
            if (!Objects.equals(anterior, novo)) {
                alterados.add(new CampoAlterado(campos[i], anterior, novo));
            }
        }
        return alterados;
    }
}
