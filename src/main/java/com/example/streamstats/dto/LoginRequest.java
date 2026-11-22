package com.example.streamstats.dto;

import jakarta.validation.constraints.NotBlank;

// DTO para transportar as credenciais do login.
// @NotBlank que impede que e-mail ou senha sejam enviados vazios quando o controller usa @Valid
public record LoginRequest(
        @NotBlank(message = "O e-mail é obrigatório") String email,
        @NotBlank(message = "A senha é obrigatória") String senha) {
}
