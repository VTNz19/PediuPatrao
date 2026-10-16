package com.umc.pediupatrao.entity;

import java.util.EnumMap;
import java.util.Map;

public enum StatusPedido {
    RECEBIDO,
    EM_PREPARACAO,
    PRONTO,
    SAIU_PARA_ENTREGA_RETIRADO,
    FINALIZADO,
    CANCELADO;

    private static final Map<StatusPedido, StatusPedido> PROXIMA_TRANSICAO = new EnumMap<>(StatusPedido.class);
    static {
        PROXIMA_TRANSICAO.put(RECEBIDO, EM_PREPARACAO);
        PROXIMA_TRANSICAO.put(EM_PREPARACAO, PRONTO);
        PROXIMA_TRANSICAO.put(PRONTO, SAIU_PARA_ENTREGA_RETIRADO);
        PROXIMA_TRANSICAO.put(SAIU_PARA_ENTREGA_RETIRADO, FINALIZADO);
    }

    public StatusPedido proximo() {
        return PROXIMA_TRANSICAO.get(this);
    }

    public boolean isTerminal() {
        return this == FINALIZADO || this == CANCELADO;
    }

    public boolean podeSerCanceladoOuTerDesconto() {
        return this != SAIU_PARA_ENTREGA_RETIRADO && this != FINALIZADO && this != CANCELADO;
    }

    public static StatusPedido fromString(String valor) {
        if (valor == null) {
            return null;
        }
        try {
            return StatusPedido.valueOf(valor);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
