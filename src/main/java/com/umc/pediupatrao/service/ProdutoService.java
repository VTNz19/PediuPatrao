package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.exception.NegocioException;
import com.umc.pediupatrao.repository.ProdutoRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    public Produto novoProduto(Produto produto) {
        return salvar(produto);
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','ATENDENTE')")
    public List<Produto> listarProdutos() {
        return produtoRepository.findAll();
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','ATENDENTE')")
    public Optional<Produto> buscarPorId(String id) {
        return produtoRepository.findById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void excluir(String id) {
        produtoRepository.deleteById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Produto salvar(Produto produto) {
        if (produto.getNome() == null || produto.getNome().isBlank()) {
            throw new NegocioException("Nome do produto é obrigatório.");
        }
        if (produto.getTipo() == null || produto.getTipo().isBlank()) {
            throw new NegocioException("Tipo do produto é obrigatório.");
        }
        if (produto.getPreco() == null || produto.getPreco() <= 0) {
            throw new NegocioException("Preço deve ser maior que zero.");
        }
        if (produto.getAtivo() == null) {
            produto.setAtivo(false);
        }
        if (produto.getId() == null) {
            return produtoRepository.save(produto);
        }
        if (!produtoRepository.existsById(produto.getId())) {
            throw new NegocioException("Produto não encontrado para atualização.");
        }
        return produtoRepository.save(produto);
    }
}
