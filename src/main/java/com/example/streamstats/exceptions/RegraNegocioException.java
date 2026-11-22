package com.example.streamstats.exceptions;

// Exceção para uma operação que violar uma regra válida do domínio
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
