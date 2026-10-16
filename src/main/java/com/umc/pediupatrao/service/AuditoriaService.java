package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.CampoAlterado;
import com.umc.pediupatrao.entity.RegistroAuditoria;
import com.umc.pediupatrao.repository.AuditoriaRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaService(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    /**
     * Único ponto de escrita na auditoria: não existe update/delete expostos,
     * então os registros são imutáveis por construção.
     */
    public void registrar(String tipoOperacao, String entidade, String entidadeId,
                           List<CampoAlterado> camposAlterados, String justificativa) {
        if (camposAlterados == null || camposAlterados.isEmpty()) {
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        RegistroAuditoria registro = new RegistroAuditoria();
        registro.setUsuario(auth != null ? auth.getName() : "desconhecido");
        registro.setPerfil(extrairPerfil(auth));
        registro.setDataHora(LocalDateTime.now());
        registro.setTipoOperacao(tipoOperacao);
        registro.setEntidade(entidade);
        registro.setEntidadeId(entidadeId);
        registro.setCamposAlterados(camposAlterados);
        registro.setJustificativa(justificativa);

        auditoriaRepository.save(registro);
    }

    private String extrairPerfil(Authentication auth) {
        if (auth == null) {
            return "desconhecido";
        }
        for (GrantedAuthority authority : auth.getAuthorities()) {
            String nome = authority.getAuthority();
            if (nome.startsWith("ROLE_")) {
                return nome.substring("ROLE_".length());
            }
        }
        return "SEM_PERFIL";
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public List<RegistroAuditoria> listarTodos() {
        return auditoriaRepository.findAllByOrderByDataHoraDesc();
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public List<RegistroAuditoria> listarPorEntidade(String entidade, String entidadeId) {
        return auditoriaRepository.findByEntidadeAndEntidadeIdOrderByDataHoraDesc(entidade, entidadeId);
    }
}
