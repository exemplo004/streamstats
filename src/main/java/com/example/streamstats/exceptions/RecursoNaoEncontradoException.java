package com.example.streamstats.exceptions;

// Exceção para um erro de busca, o HTTP 404
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
