package com.example.streamstats.dto;

import com.example.streamstats.entities.StatusLista;
import jakarta.validation.constraints.NotNull;

// DTO para caso tenha a alteração isolada do status de um item
public record StatusRequest(
        @NotNull(message = "status é obrigatório (QUERO_ASSISTIR, ASSISTINDO ou ASSISTIDO)") StatusLista status) {
}
