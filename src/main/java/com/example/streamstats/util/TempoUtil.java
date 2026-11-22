package com.example.streamstats.util;

import com.example.streamstats.dto.TempoTotalResponse;

// Apenas uma conversão dos minutos totais em dias, horas e minutos sendo 1 dia = 1440 min

public final class TempoUtil {
    private TempoUtil() {
    }
    public static TempoTotalResponse converter(long totalMinutos) {
        long dias = totalMinutos / 1440;
        long horas = (totalMinutos % 1440) / 60;
        long minutos = totalMinutos % 60;
        String texto = dias + " dias, " + horas + " h e " + minutos + " min";
        return new TempoTotalResponse(totalMinutos, dias, horas, minutos, texto);
    }
}
