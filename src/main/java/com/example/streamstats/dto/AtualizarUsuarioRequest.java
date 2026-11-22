package com.example.streamstats.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// DTO separado da entidade para o cliente só poder alterar o nome nesta operação
public record AtualizarUsuarioRequest(
        @NotBlank(message = "O nome é obrigatório") @Size(max = 100) String nome) {
}
