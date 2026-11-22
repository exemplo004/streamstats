package com.example.streamstats.dto;

// DTO de saída para mostrar os indicadores prontos para a tela inicial
public record EstatisticasResponse(
        long queroAssistir,
        long assistindo,
        long assistidos,
        Double notaMedia,
        TempoTotalResponse tempoTotal) {
}
