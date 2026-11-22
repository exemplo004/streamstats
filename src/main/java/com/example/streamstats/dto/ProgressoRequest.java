package com.example.streamstats.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// DTO para representar o número de episódios assistidos enviado pelo cliente
// @Min impede que o progresso seja negativo
public record ProgressoRequest(
        @NotNull(message = "episodios é obrigatório") @Min(value = 0, message = "episodios não pode ser negativo") Integer episodios) {
}
