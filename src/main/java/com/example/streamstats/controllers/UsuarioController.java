package com.example.streamstats.controllers;

import com.example.streamstats.dto.AtualizarUsuarioRequest;
import com.example.streamstats.dto.UsuarioResponse;
import com.example.streamstats.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {
    // O controller do Usuario...

    // @Autowired novamente
    @Autowired
    private UsuarioService usuarioService;

    // O método from para evitar expor os campos internos, para o hash da senha
    @GetMapping
    public List<UsuarioResponse> listar() {
        return usuarioService.findAll().stream().map(UsuarioResponse::from).toList();
    }

    // @GetMapping novamente e @PathVariable para extrair o id de /usuarios/{id}
    @GetMapping("/{id}")
    public UsuarioResponse buscar(@PathVariable Long id) {
        return UsuarioResponse.from(usuarioService.findById(id));
    }

    @PutMapping("/{id}")
    public UsuarioResponse atualizar(@PathVariable Long id, @Valid @RequestBody AtualizarUsuarioRequest dto) {
        return UsuarioResponse.from(usuarioService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        usuarioService.deletar(id);
    }
}
