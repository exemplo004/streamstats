package com.example.streamstats.repositories;

import com.example.streamstats.entities.Midia;
import com.example.streamstats.entities.TipoMidia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MidiaRepository extends JpaRepository<Midia, Long> {

    // ContainingIgnoreCase usado para busca parcial sem diferenciar maiúsculas de minúsculas
    List<Midia> findByTituloContainingIgnoreCase(String titulo);

    List<Midia> findByTipo(TipoMidia tipo);

    List<Midia> findByTituloContainingIgnoreCaseAndTipo(String titulo, TipoMidia tipo);
}
