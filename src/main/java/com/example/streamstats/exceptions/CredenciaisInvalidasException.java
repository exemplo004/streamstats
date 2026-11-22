package com.example.streamstats.exceptions;

// Exceção para evitar expor detalhes sobre qual credencial falhou
public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException() {
        super("E-mail ou senha inválidos");
    }
}
