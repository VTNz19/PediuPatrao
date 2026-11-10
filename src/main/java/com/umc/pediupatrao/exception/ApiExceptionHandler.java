package com.umc.pediupatrao.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/** Só se aplica a @RestController (ver atributo annotations) — rotas MVC do HomeController tratam seus próprios erros. */
@RestControllerAdvice(annotations = RestController.class)
public class ApiExceptionHandler {

    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<Map<String, String>> handleNegocioException(NegocioException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("erro", ex.getMessage()));
    }

    /** Parâmetro com formato inválido (ex.: data fora de yyyy-MM-dd, limite não numérico). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTipoInvalido(MethodArgumentTypeMismatchException ex) {
        String esperado = ex.getRequiredType() != null && ex.getRequiredType().getSimpleName().equals("LocalDate")
                ? "yyyy-MM-dd" : "valor válido";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("erro", "Parâmetro '" + ex.getName() + "' inválido: '" + ex.getValue() + "'. Esperado: " + esperado + "."));
    }
}
