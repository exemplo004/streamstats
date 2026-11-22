package com.example.streamstats.repositories;

import com.example.streamstats.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Optional para poder saber se existe ou não um usuário com aquele e-mail
    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);
}
