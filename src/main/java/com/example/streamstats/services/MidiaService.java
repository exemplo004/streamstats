package com.example.streamstats.services;

import com.example.streamstats.entities.Midia;
import com.example.streamstats.entities.TipoMidia;
import com.example.streamstats.exceptions.RecursoNaoEncontradoException;
import com.example.streamstats.repositories.ItemListaRepository;
import com.example.streamstats.repositories.MidiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MidiaService {

    @Autowired
    private MidiaRepository midiaRepository;

    @Autowired
    private ItemListaRepository itemListaRepository;

    public List<Midia> buscar(String titulo, TipoMidia tipo) {
        
        // Escolhe a consulta adequada conforme os filtros realmente informados
        boolean temTitulo = titulo != null && !titulo.isBlank();

        if (temTitulo && tipo != null) {
            return midiaRepository.findByTituloContainingIgnoreCaseAndTipo(titulo.trim(), tipo);
        }
        if (temTitulo) {
            return midiaRepository.findByTituloContainingIgnoreCase(titulo.trim());
        }
        if (tipo != null) {
            return midiaRepository.findByTipo(tipo);
        }
        return midiaRepository.findAll();
    }

    public Midia findById(Long id) {
        return midiaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Mídia não encontrada: " + id));
    }

    public Midia criar(Midia midia) {
        midia.setId(null);
        aplicarRegras(midia);
        return midiaRepository.save(midia);
    }

    public Midia atualizar(Long id, Midia dados) {
        Midia midia = findById(id);
        midia.setTitulo(dados.getTitulo());
        midia.setTipo(dados.getTipo());
        midia.setGenero(dados.getGenero());
        midia.setAno(dados.getAno());
        midia.setTotalEpisodios(dados.getTotalEpisodios());
        midia.setDuracaoMediaEpisodioMin(dados.getDuracaoMediaEpisodioMin());
        midia.setSinopse(dados.getSinopse());
        midia.setCapaUrl(dados.getCapaUrl());
        aplicarRegras(midia);
        return midiaRepository.save(midia);
    }

    // @Transactional faz com que as duas exclusões funcionem como uma única operação
    @Transactional
    public void deletar(Long id) {
        // Então usando o Transactional garantindo que os itens dependentes e a mídia sejam removidos juntos
        findById(id);
        itemListaRepository.deleteByMidiaId(id);
        midiaRepository.deleteById(id);
    }

    // Regra só para que o Filme possuir sempre 1 episódio
    private void aplicarRegras(Midia midia) {
        if (midia.getTipo() == TipoMidia.FILME) {
            midia.setTotalEpisodios(1);
        }
    }
}
