package com.example.streamstats.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

//Catálogo 


@Entity
@Table(name = "midias")
public class Midia {

    // @Id e @GeneratedValue novamente
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @NotBlank, @NotNull, @Min e @Size usadas novamente como as regras de validação executadas por @Valid
    @NotBlank(message = "O título é obrigatório")
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String titulo;

    // @NotNull exigindo um tipo e @Enumerated salva a SERIE/FILME/ANIME como texto
    @NotNull(message = "O tipo é obrigatório (SERIE, FILME ou ANIME)")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMidia tipo;

    @Size(max = 80)
    @Column(length = 80)
    private String genero;

    private Integer ano;

    // Filme sempre tendo 1 "episódio" colocando para o service forçar isso
    @NotNull
    @Min(value = 1, message = "Deve ter pelo menos 1 episódio")
    @Column(name = "total_episodios", nullable = false)
    private Integer totalEpisodios = 1;

    @NotNull(message = "A duração média do episódio (min) é obrigatória")
    @Min(value = 1, message = "A duração deve ser de pelo menos 1 minuto")
    @Column(name = "duracao_media_episodio_min", nullable = false)
    private Integer duracaoMediaEpisodioMin;

    @Size(max = 1000)
    @Column(length = 1000)
    private String sinopse;

    // Link da imagem de capa para futuramente melhorar o projeto. Sem capa a interface vai gerar um pôster com o título
    @Size(max = 500)
    @Pattern(regexp = "^(https?://.*)?$", message = "A capa deve ser um link que comece com http:// ou https://")
    @Column(name = "capa_url", length = 500)
    private String capaUrl;

    public Midia() {
    }

    public Midia(String titulo, TipoMidia tipo, int totalEpisodios, int duracaoMediaEpisodioMin) {
        this.titulo = titulo;
        this.tipo = tipo;
        this.totalEpisodios = totalEpisodios;
        this.duracaoMediaEpisodioMin = duracaoMediaEpisodioMin;
    }

    // A mídia vai pegar sua própria duração e vai calcular quantos minutos correspondem a N episódios da midia
    public long calcularMinutos(int episodios) {
        return (long) episodios * duracaoMediaEpisodioMin;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public TipoMidia getTipo() {
        return tipo;
    }

    public void setTipo(TipoMidia tipo) {
        this.tipo = tipo;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public Integer getTotalEpisodios() {
        return totalEpisodios;
    }

    public void setTotalEpisodios(Integer totalEpisodios) {
        this.totalEpisodios = totalEpisodios;
    }

    public Integer getDuracaoMediaEpisodioMin() {
        return duracaoMediaEpisodioMin;
    }

    public void setDuracaoMediaEpisodioMin(Integer duracaoMediaEpisodioMin) {
        this.duracaoMediaEpisodioMin = duracaoMediaEpisodioMin;
    }

    public String getSinopse() {
        return sinopse;
    }

    public void setSinopse(String sinopse) {
        this.sinopse = sinopse;
    }

    public String getCapaUrl() {
        return capaUrl;
    }

    public void setCapaUrl(String capaUrl) {
        this.capaUrl = capaUrl;
    }
}
