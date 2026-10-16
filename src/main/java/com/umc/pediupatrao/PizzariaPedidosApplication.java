package com.umc.pediupatrao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PizzariaPedidosApplication {

    public static void main(String[] args) {
        // Alguns JDKs negociam TLS 1.3 por padrão e isso tem dado "fatal alert: internal_error"
        // ao conectar no MongoDB Atlas nesta máquina. Forçar TLS 1.2 evita o problema.
        System.setProperty("jdk.tls.client.protocols", "TLSv1.2");
        SpringApplication.run(PizzariaPedidosApplication.class, args);
    }
}
