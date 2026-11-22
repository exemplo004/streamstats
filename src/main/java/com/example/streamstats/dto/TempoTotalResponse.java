package com.example.streamstats.dto;

// DTO da resposta já convertida para facilitar a apresentação de tempo na interface
public record TempoTotalResponse(long totalMinutos, long dias, long horas, long minutos, String texto) {
}
