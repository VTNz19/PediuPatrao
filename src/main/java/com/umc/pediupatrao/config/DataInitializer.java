package com.umc.pediupatrao.config;

import com.umc.pediupatrao.entity.Cliente;
import com.umc.pediupatrao.entity.Perfil;
import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.repository.ClienteRepository;
import com.umc.pediupatrao.repository.ProdutoRepository;
import com.umc.pediupatrao.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    private record SeedUser(String username, String senha, Perfil perfil) {
    }

    @Bean
    CommandLineRunner initDatabase(UsuarioRepository repo, PasswordEncoder passwordEncoder) {
        return args -> {
            SeedUser[] seeds = {
                    new SeedUser("admin", "teste123", Perfil.ADMIN),
                    new SeedUser("gerente01", "teste123", Perfil.GERENTE),
                    new SeedUser("atendente01", "teste123", Perfil.ATENDENTE)
            };

            for (SeedUser seed : seeds) {
                Usuario existente = repo.findByUsername(seed.username()).orElse(null);

                if (existente == null) {
                    Usuario usuario = new Usuario();
                    usuario.setUsername(seed.username());
                    usuario.setPassword(passwordEncoder.encode(seed.senha()));
                    usuario.setRole(seed.perfil().name());
                    repo.save(usuario);
                    System.out.println("Usuário seed criado: " + seed.username() + " / senha: " + seed.senha()
                            + " / perfil: " + seed.perfil());
                } else if (!Perfil.isValido(existente.getRole())) {
                    // Backfill: conta antiga (do fluxo de bootstrap anterior) sem perfil válido.
                    existente.setRole(seed.perfil().name());
                    repo.save(existente);
                    System.out.println("Usuário '" + seed.username() + "' recebeu perfil " + seed.perfil() + " (backfill).");
                }
            }
        };
    }

    @Bean
    CommandLineRunner initCatalogoEClientes(ProdutoRepository produtoRepository, ClienteRepository clienteRepository) {
        return args -> {
            if (produtoRepository.count() == 0) {
                produtoRepository.save(produto("Pizza Mussarela", "Pizza", 45.0));
                produtoRepository.save(produto("Pizza Calabresa", "Pizza", 48.0));
                produtoRepository.save(produto("Refrigerante Lata", "Bebida", 7.0));
                produtoRepository.save(produto("Esfiha de Carne", "Esfiha", 8.5));
                System.out.println("Catálogo de produtos seed criado (4 itens).");
            }

            if (clienteRepository.count() == 0) {
                Cliente cliente = new Cliente();
                cliente.setNome("Cliente Teste");
                cliente.setTelefone("(11) 99999-0000");
                cliente.setCep("01310-100");
                cliente.setLogradouro("Av. Paulista");
                cliente.setNumero("1000");
                cliente.setBairro("Bela Vista");
                cliente.setCidade("São Paulo");
                cliente.setEstado("SP");
                clienteRepository.save(cliente);
                System.out.println("Cliente seed criado: Cliente Teste.");
            }
        };
    }

    private Produto produto(String nome, String tipo, double preco) {
        Produto produto = new Produto();
        produto.setNome(nome);
        produto.setTipo(tipo);
        produto.setPreco(preco);
        produto.setAtivo(true);
        return produto;
    }
}
