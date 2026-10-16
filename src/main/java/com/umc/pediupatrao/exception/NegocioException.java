package com.umc.pediupatrao.exception;

/** Violação de uma regra de negócio (transição inválida, desconto fora do limite, etc.). */
public class NegocioException extends RuntimeException {
    public NegocioException(String message) {
        super(message);
    }
}
