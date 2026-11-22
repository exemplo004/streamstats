package com.example.streamstats.controllers;

import com.example.streamstats.entities.Midia;
import com.example.streamstats.entities.TipoMidia;
import com.example.streamstats.services.MidiaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

//Rest e Request como já usados...

@RestController
@RequestMapping("/midias")
public class MidiaController {
    // A mídia representan o catálogo compartilhado por todos os usuários

    @Autowired
    private MidiaService midiaService;
    // Parâmetros opcionais permitem pesquisar por título, tipo ou pelos dois.

    // @GetMapping para atender o GET das /midias; os @RequestParam para ler os filtros opcionais da URL
    @GetMapping
    public List<Midia> listar(@RequestParam(required = false) String titulo,@RequestParam(required = false) TipoMidia tipo) {
        return midiaService.buscar(titulo, tipo);
    }

    @GetMapping("/{id}")
    public Midia buscar(@PathVariable Long id) {
        return midiaService.findById(id);
    }

    // @Valid para executar as regras de validação declaradas na entidade Midia
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Midia criar(@Valid @RequestBody Midia midia) {
        return midiaService.criar(midia);
    }

    // PUT para uma atualização completa dos dados da mídia
    // @PutMapping para a atualização e @PathVariable para capturar o id da URL
    @PutMapping("/{id}")
    public Midia atualizar(@PathVariable Long id, @Valid @RequestBody Midia midia) {
        return midiaService.atualizar(id, midia);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        midiaService.deletar(id);
    }
}
