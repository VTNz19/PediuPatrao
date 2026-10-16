package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Perfil;
import com.umc.pediupatrao.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepository.findByUsername(username)
                .map(usuario -> {
                    User.UserBuilder builder = User.builder()
                            .username(usuario.getUsername())
                            .password(usuario.getPassword());

                    // Role nula/desconhecida (ex.: dado legado de outro ambiente) não derruba
                    // o login — o usuário só fica sem nenhuma authority, ou seja, sem acesso
                    // a nada protegido por hasRole/hasAnyRole.
                    if (Perfil.isValido(usuario.getRole())) {
                        builder.roles(usuario.getRole());
                    } else {
                        builder.authorities(java.util.List.of());
                    }

                    return builder.build();
                })
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }
}
