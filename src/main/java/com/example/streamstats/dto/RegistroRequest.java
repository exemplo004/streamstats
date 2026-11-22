package com.example.streamstats.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// DTO de dados públicos necessários para poder criar um usuário
// @NotBlank para rejeitar o vazio e @Email verifica formato, o @Size limita o tamanho
public record RegistroRequest(
        @NotBlank(message = "O nome é obrigatório") @Size(max = 100) String nome,
        @NotBlank(message = "O e-mail é obrigatório") @Email(message = "E-mail inválido") String email,
        @NotBlank(message = "A senha é obrigatória") @Size(min = 6, max = 72, message = "A senha deve ter de 6 a 72 caracteres") String senha) {
}
