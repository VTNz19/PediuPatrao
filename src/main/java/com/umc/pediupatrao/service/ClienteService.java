package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.CampoAlterado;
import com.umc.pediupatrao.entity.Cliente;
import com.umc.pediupatrao.exception.NegocioException;
import com.umc.pediupatrao.repository.ClienteRepository;
import com.umc.pediupatrao.util.DiffUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final AuditoriaService auditoriaService;

    public ClienteService(ClienteRepository clienteRepository, AuditoriaService auditoriaService) {
        this.clienteRepository = clienteRepository;
        this.auditoriaService = auditoriaService;
    }

    public Cliente novoCliente(Cliente cliente) {
        return salvar(cliente);
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','ATENDENTE')")
    public List<Cliente> listarClientes() {
        return clienteRepository.findAll();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void excluir(String id) {
        clienteRepository.deleteById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','ATENDENTE')")
    public Optional<Cliente> buscarPorId(String id) {
        return clienteRepository.findById(id);
    }

    @PreAuthorize("hasRole('ATENDENTE')")
    public Cliente salvar(Cliente cliente) {
        if (cliente.getId() == null) {
            Cliente salvo = clienteRepository.save(cliente);
            auditoriaService.registrar("CRIACAO_CLIENTE", "Cliente", salvo.getId(),
                    List.of(new CampoAlterado("nome", null, salvo.getNome())),
                    null);
            return salvo;
        }

        Cliente existente = clienteRepository.findById(cliente.getId())
                .orElseThrow(() -> new NegocioException("Cliente não encontrado para atualização."));

        List<CampoAlterado> alteracoes = DiffUtils.diff(
                new String[]{"nome", "telefone", "cep", "logradouro", "numero", "complemento", "bairro", "cidade", "estado"},
                new String[]{existente.getNome(), existente.getTelefone(), existente.getCep(), existente.getLogradouro(),
                        existente.getNumero(), existente.getComplemento(), existente.getBairro(), existente.getCidade(), existente.getEstado()},
                new String[]{cliente.getNome(), cliente.getTelefone(), cliente.getCep(), cliente.getLogradouro(),
                        cliente.getNumero(), cliente.getComplemento(), cliente.getBairro(), cliente.getCidade(), cliente.getEstado()});

        Cliente salvo = clienteRepository.save(cliente);

        if (!alteracoes.isEmpty()) {
            auditoriaService.registrar("ALTERACAO_CLIENTE", "Cliente", salvo.getId(), alteracoes, null);
        }

        return salvo;
    }
}
