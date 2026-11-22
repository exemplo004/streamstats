package com.example.streamstats.controllers;

import com.example.streamstats.dto.AdicionarItemRequest;
import com.example.streamstats.dto.EstatisticasResponse;
import com.example.streamstats.dto.NotaRequest;
import com.example.streamstats.dto.ProgressoRequest;
import com.example.streamstats.dto.StatusRequest;
import com.example.streamstats.dto.TempoTotalResponse;
import com.example.streamstats.entities.ItemLista;
import com.example.streamstats.entities.StatusLista;
import com.example.streamstats.services.ItemListaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController

// @RequestMapping novamente para definir o prefixo comum dos endpoints desta classe sendo /lista.
@RequestMapping("/lista")
public class ItemListaController {
    // Dependência responsável pelas regras da lista pessoal do usuário.

    // @Autowired como sempre para poder fazer as alterações
    @Autowired
    private ItemListaService itemListaService;

    // @GetMapping para atender consultas GET.
    // @PathVariable para ler o valor que aparece quando tiver {usuarioId} da URL
    // @RequestParam para ler ?status=...; e o required=false permite não enviar o filtro junto
    // retorna a Lista do usuário, que filtrei opcionalmente pelo /lista/usuario/1?status=ASSISTINDO
    @GetMapping("/usuario/{usuarioId}")
    public List<ItemLista> listar(@PathVariable Long usuarioId,@RequestParam(required = false) StatusLista status) {
        return itemListaService.listar(usuarioId, status);
    }

    // Consultas para gerar o tempo total
    @GetMapping("/usuario/{usuarioId}/tempo-total")
    public TempoTotalResponse tempoTotal(@PathVariable Long usuarioId) {
        return itemListaService.calcularTempoTotal(usuarioId);
    }

    @GetMapping("/usuario/{usuarioId}/estatisticas")
    public EstatisticasResponse estatisticas(@PathVariable Long usuarioId) {
        return itemListaService.estatisticas(usuarioId);
    }

    // Cada endpoint traduzindo uma ação da interface para sua chamada de negócio
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemLista adicionar(@Valid @RequestBody AdicionarItemRequest dto) {
        return itemListaService.adicionar(dto);
    }

    @PatchMapping("/{id}/status")
    public ItemLista alterarStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest dto) {
        return itemListaService.alterarStatus(id, dto.status());
    }

    @PatchMapping("/{id}/progresso")
    public ItemLista atualizarProgresso(@PathVariable Long id, @Valid @RequestBody ProgressoRequest dto) {
        return itemListaService.atualizarProgresso(id, dto.episodios());
    }

    @PatchMapping("/{id}/nota")
    public ItemLista avaliar(@PathVariable Long id, @Valid @RequestBody NotaRequest dto) {
        return itemListaService.avaliar(id, dto.nota());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long id) {
        itemListaService.remover(id);
    }
}
