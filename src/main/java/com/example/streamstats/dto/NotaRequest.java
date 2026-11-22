package com.example.streamstats.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

// nota null vai remover a avaliação
// @Min e @Max usadas para validação; meio intuitivas mas enfim, elas só vão ser usadas quando o parâmetro receber @Valid.
public record NotaRequest(
        @Min(value = 1, message = "A nota mínima é 1") @Max(value = 10, message = "A nota máxima é 10") Integer nota) {
}
