package com.example.streamstats.services;

import com.example.streamstats.dto.AdicionarItemRequest;
import com.example.streamstats.dto.EstatisticasResponse;
import com.example.streamstats.dto.TempoTotalResponse;
import com.example.streamstats.entities.ItemLista;
import com.example.streamstats.entities.Midia;
import com.example.streamstats.entities.StatusLista;
import com.example.streamstats.entities.Usuario;
import com.example.streamstats.exceptions.RecursoNaoEncontradoException;
import com.example.streamstats.exceptions.RegraNegocioException;
import com.example.streamstats.repositories.ItemListaRepository;
import com.example.streamstats.util.TempoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;

// @Service para registrar esta classe como componente da camada de negócio e coordenar as entidades, as validações e as persistências
@Service
public class ItemListaService {

    @Autowired
    private ItemListaRepository itemListaRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private MidiaService midiaService;

    public List<ItemLista> listar(Long usuarioId, StatusLista status) {
        usuarioService.findById(usuarioId); 
        // Garantir que caia o 404 se o usuário não existir
        if (status == null) {
            return itemListaRepository.findByUsuarioId(usuarioId);
        }
        return itemListaRepository.findByUsuarioIdAndStatus(usuarioId, status);
    }

    public ItemLista findById(Long id) {
        return itemListaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item da lista não encontrado: " + id));
    }

    public ItemLista adicionar(AdicionarItemRequest dto) {
        // O service vai buscar as entidades necessárias antes de criar qualquer relacionamento
        Usuario usuario = usuarioService.findById(dto.usuarioId());
        Midia midia = midiaService.findById(dto.midiaId());

        if (itemListaRepository.existsByUsuarioIdAndMidiaId(usuario.getId(), midia.getId())) {
            throw new RegraNegocioException("Essa mídia já está na sua lista");
        }
        return itemListaRepository.save(new ItemLista(usuario, midia, dto.status()));
    }

    public ItemLista alterarStatus(Long itemId, StatusLista status) {
        ItemLista item = findById(itemId);
        item.alterarStatus(status);
        return itemListaRepository.save(item);
    }

    public ItemLista atualizarProgresso(Long itemId, int episodios) {
        ItemLista item = findById(itemId);
        item.atualizarProgresso(episodios);
        return itemListaRepository.save(item);
    }

    public ItemLista avaliar(Long itemId, Integer nota) {
        ItemLista item = findById(itemId);
        item.avaliar(nota);
        return itemListaRepository.save(item);
    }

    public void remover(Long itemId) {
        findById(itemId);
        itemListaRepository.deleteById(itemId);
    }

    // Soma o tempo de tela de tudo que o usuário já assistiu e converte para dias/horas/minutos
    public TempoTotalResponse calcularTempoTotal(Long usuarioId) {
        usuarioService.findById(usuarioId);
        long totalMinutos = itemListaRepository.findByUsuarioId(usuarioId).stream()
                .mapToLong(ItemLista::getMinutosAssistidos)
                .sum();
        return TempoUtil.converter(totalMinutos);
    }

    public EstatisticasResponse estatisticas(Long usuarioId) {
        usuarioService.findById(usuarioId);
        List<ItemLista> itens = itemListaRepository.findByUsuarioId(usuarioId);

        long queroAssistir = contar(itens, StatusLista.QUERO_ASSISTIR);
        long assistindo = contar(itens, StatusLista.ASSISTINDO);
        long assistidos = contar(itens, StatusLista.ASSISTIDO);

        // Stream percorre a coleção declarando o que calcular
        OptionalDouble media = itens.stream()
                .map(ItemLista::getNota)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average();
        Double notaMedia = media.isPresent() ? media.getAsDouble() : null;

        return new EstatisticasResponse(queroAssistir, assistindo, assistidos, notaMedia,
                calcularTempoTotal(usuarioId));
    }

    private long contar(List<ItemLista> itens, StatusLista status) {
        return itens.stream().filter(i -> i.getStatus() == status).count();
    }
}
