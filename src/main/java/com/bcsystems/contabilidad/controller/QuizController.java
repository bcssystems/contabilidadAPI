package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.dto.QuizAnswerRequest;
import com.bcsystems.contabilidad.dto.QuizSaveResponse;
import com.bcsystems.contabilidad.service.QuizService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/quiz")
public class QuizController {
    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping("/respuestas")
    public ResponseEntity<QuizSaveResponse> save(Authentication authentication,
                                                  @RequestBody QuizAnswerRequest request) {
        return ResponseEntity.ok(quizService.save(authentication.getName(), request));
    }
}
