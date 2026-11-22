package com.example.streamstats.repositories;

import com.example.streamstats.entities.ItemLista;
import com.example.streamstats.entities.StatusLista;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// JpaRepository como passado na ultima aula, fornecendo o CRUD e o  ItemLista é a entidade
public interface ItemListaRepository extends JpaRepository<ItemLista, Long> {

    // O Spring Data cria a implementação das consultas a partir do nome dos métodos
    List<ItemLista> findByUsuarioId(Long usuarioId);

    List<ItemLista> findByUsuarioIdAndStatus(Long usuarioId, StatusLista status);

    boolean existsByUsuarioIdAndMidiaId(Long usuarioId, Long midiaId);

    void deleteByUsuarioId(Long usuarioId);

    void deleteByMidiaId(Long midiaId);
}
