package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.dto.QuizAnswerRequest;
import com.bcsystems.contabilidad.dto.QuizSaveResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Service
public class QuizService {
    private static final Set<String> ALLOWED_USERS = Set.of("bucordero", "ngfonseca");
    private static final DateTimeFormatter FILE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private final Path quizDir;

    public QuizService(@Value("${app.quiz.upload.dir:./uploads/quiz}") String quizDir) {
        this.quizDir = Paths.get(quizDir).toAbsolutePath().normalize();
    }

    public QuizSaveResponse save(String username, QuizAnswerRequest request) {
        String normalizedUsername = username == null ? "" : username.trim().toLowerCase();
        if (!ALLOWED_USERS.contains(normalizedUsername)) {
            throw new AccessDeniedException("Usuario no autorizado para guardar el quiz");
        }
        if (request == null || request.respuestas() == null || request.respuestas().size() != 2
                || request.respuestas().stream().anyMatch(answer -> answer == null
                || answer.pregunta() == null || answer.pregunta().isBlank()
                || answer.respuesta() == null || answer.respuesta().isBlank())) {
            throw new IllegalArgumentException("El quiz debe contener exactamente dos respuestas");
        }

        StringBuilder content = new StringBuilder()
                .append("Respuestas del quiz especial\n")
                .append("Usuario: ").append(normalizedUsername).append('\n')
                .append("Fecha: ").append(LocalDateTime.now()).append("\n\n");
        List<QuizAnswerRequest.Answer> answers = request.respuestas();
        for (int index = 0; index < answers.size(); index++) {
            QuizAnswerRequest.Answer answer = answers.get(index);
            content.append("Pregunta ").append(index + 1).append(": ").append(answer.pregunta()).append('\n')
                    .append("Respuesta ").append(index + 1).append(": ").append(answer.respuesta()).append("\n\n");
        }

        try {
            Files.createDirectories(quizDir);
            String filename = "respuestas-quiz-" + normalizedUsername + "-"
                    + FILE_DATE.format(LocalDateTime.now()) + ".txt";
            Path target = quizDir.resolve(filename).normalize();
            if (!target.getParent().equals(quizDir)) {
                throw new IOException("Ruta de quiz invalida");
            }
            Files.writeString(target, content.toString(), StandardCharsets.UTF_8);
            return new QuizSaveResponse("Respuestas guardadas en el servidor");
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudieron guardar las respuestas del quiz", exception);
        }
    }
}
