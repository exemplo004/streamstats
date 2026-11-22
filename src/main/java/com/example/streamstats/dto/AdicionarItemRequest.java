package com.example.streamstats.dto;

import com.example.streamstats.entities.StatusLista;
import jakarta.validation.constraints.NotNull;

// Record que vai ser uma classe curta para dados que gera construtor, acesso e igualdade automaticamente
// DTO de entrada para carregar somente os dados necessários para adicionar uma mídia na lista
// @NotNull para impedir que seja criado sem um usuário, uma mídia ou um status
public record AdicionarItemRequest(
        @NotNull(message = "usuarioId é obrigatório") Long usuarioId,
        @NotNull(message = "midiaId é obrigatório") Long midiaId,
        @NotNull(message = "status é obrigatório (QUERO_ASSISTIR, ASSISTINDO ou ASSISTIDO)") StatusLista status) {
}
