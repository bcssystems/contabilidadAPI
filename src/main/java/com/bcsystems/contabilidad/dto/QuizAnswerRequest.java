package com.bcsystems.contabilidad.dto;

import java.util.List;

public record QuizAnswerRequest(List<Answer> respuestas) {
    public record Answer(String pregunta, String respuesta) {
    }
}
