package com.example.streamstats.controllers;

import com.example.streamstats.dto.LoginRequest;
import com.example.streamstats.dto.RegistroRequest;
import com.example.streamstats.dto.UsuarioResponse;
import com.example.streamstats.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// @RestController como dito na aula que transforma esta classe em um controller que responde normalmente em JSON
@RestController

// @RequestMapping para definir o prefixo comum dos endpoints desta classe
@RequestMapping("/auth")
public class AuthController {
    // O controller vai receber o HTTP, depois as regras ficam no service, para não se misturarem

    // @Autowired como o professor tbm comentou faz o Spring fornecer uma instância pronta para UsuarioService
    @Autowired
    private UsuarioService usuarioService;

    // @PostMapping que liga o método ao POST e ao caminho /auth/register.
    @PostMapping("/register")

    // @ResponseStatus devolve os códigos como comentado tbm na ultima aula sendo o "201 Created" quando o cadastro é realizado
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registrar(@Valid @RequestBody RegistroRequest dto) {
        return UsuarioResponse.from(usuarioService.cadastrar(dto));
    }

    // @Valid executa as validações do DTO antes do método continuar
    // @RequestBody converte o JSON do body da requisição para LoginRequest
    @PostMapping("/login")
    public UsuarioResponse login(@Valid @RequestBody LoginRequest dto) {
        return UsuarioResponse.from(usuarioService.autenticar(dto));
    }
}
