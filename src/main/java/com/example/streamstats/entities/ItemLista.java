package com.example.streamstats.entities;

import com.example.streamstats.exceptions.RegraNegocioException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;

// @Entity diz pro JPA/Hibernate que esta classe deve ser persistida como uma tabela
@Entity

// @Table vai personalizar o nome da tabela e criar a regra de unicidade usuário + mídia
@Table(name = "itens_lista",
        uniqueConstraints = @UniqueConstraint(name = "uk_usuario_midia", columnNames = {"usuario_id", "midia_id"}))
public class ItemLista {

    // @Id para identificar a chave primária da tabela
    @Id

    // @GeneratedValue deixa o banco criar o próximo id automaticamente
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @JsonIgnore para impedir que o usuário completo seja repetido dentro do JSON do item
    @JsonIgnore

    // @ManyToOne vai fazer com que vários itens podem pertencer ao mesmo usuário (o 1 pra n)
    // e o optional=false torna a relação obrigatória no banco
    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    // Muitos itens podem apontar para uma mídia do catálogo
    @ManyToOne(optional = false)
    @JoinColumn(name = "midia_id")
    private Midia midia;

    // @Enumerated para definir um enum que será salvo, sendo o texto mais seguro que a posição numérica
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusLista status;

    @Column(name = "episodios_assistidos", nullable = false)
    private Integer episodiosAssistidos = 0;

    private Integer nota;

    @Column(name = "data_atualizacao", nullable = false)
    private LocalDate dataAtualizacao = LocalDate.now();

    public ItemLista() {
    }

    public ItemLista(Usuario usuario, Midia midia, StatusLista status) {
        this.usuario = usuario;
        this.midia = midia;
        alterarStatus(status);
    }

    // Regras de domínio que vai trocar o status, pode também mudar progresso e nota


    // Troca o status e ajusta o progresso para ficar correto com o que tiver
    public void alterarStatus(StatusLista novoStatus) {
        int total = midia.getTotalEpisodios();
        switch (novoStatus) {
            case ASSISTIDO -> episodiosAssistidos = total;
            case QUERO_ASSISTIR -> {
                episodiosAssistidos = 0;
                nota = null;
            }
            case ASSISTINDO -> {
                // voltando de "assistido" para "assistindo" que vai deixar o último episódio pendente
                if (episodiosAssistidos >= total) {
                    episodiosAssistidos = Math.max(total - 1, 0);
                }
            }
        }
        this.status = novoStatus;
        atualizarData();
    }

    // Atualiza quantos episódios foram vistos e o status vai acompanhar o progresso sozinho
    public void atualizarProgresso(int episodios) {
        int total = midia.getTotalEpisodios();
        if (episodios < 0 || episodios > total) {
            throw new RegraNegocioException("Os episódios assistidos devem estar entre 0 e " + total);
        }
        this.episodiosAssistidos = episodios;

        if (episodios == total) {
            this.status = StatusLista.ASSISTIDO;
        } else if (episodios > 0) {
            this.status = StatusLista.ASSISTINDO;
        } else if (this.status == StatusLista.ASSISTIDO) {
            this.status = StatusLista.QUERO_ASSISTIR;
            this.nota = null;
        }
        atualizarData();
    }

    // A Avalição de cada midia dando uma nota de 1 a 10 e só é permitido depois de ter começado a assistir
    public void avaliar(Integer nota) {
        if (status == StatusLista.QUERO_ASSISTIR) {
            throw new RegraNegocioException("Só é possível dar nota a algo que você já começou a assistir");
        }
        if (nota != null && (nota < 1 || nota > 10)) {
            throw new RegraNegocioException("A nota deve estar entre 1 e 10");
        }
        this.nota = nota;
        atualizarData();
    }

    // Minutos de tela gastos na mídia (episódios assistidos x duração média)
    public long getMinutosAssistidos() {
        return midia.calcularMinutos(episodiosAssistidos);
    }

    // Usado para a atualização evitar esquecer a data em alguma operação futura
    private void atualizarData() {
        this.dataAtualizacao = LocalDate.now();
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Midia getMidia() {
        return midia;
    }

    public StatusLista getStatus() {
        return status;
    }

    public Integer getEpisodiosAssistidos() {
        return episodiosAssistidos;
    }

    public Integer getNota() {
        return nota;
    }

    public LocalDate getDataAtualizacao() {
        return dataAtualizacao;
    }
}
