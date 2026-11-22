package com.example.streamstats.dto;

import com.example.streamstats.entities.Usuario;

import java.time.LocalDateTime;

// DTO que define quais dados do usuário podem sair da API
public record UsuarioResponse(Long id, String nome, String email, LocalDateTime dataCriacao) {

    // Método usado para transformar uma entidade de banco em uma resposta segura
    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getDataCriacao());
    }
}
