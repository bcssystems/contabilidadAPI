package com.bcsystems.contabilidad.domain.auth;

import com.bcsystems.contabilidad.domain.Usuario;
import com.bcsystems.contabilidad.domain.en.TokenType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "token")
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idToken;
    @Column(unique = true)
    private String token;
    @Column(nullable = false)
    private Boolean isRevoked;
    @Column(nullable = false)
    private Boolean isExpired;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TokenType type = TokenType.BEARER;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user")
    private Usuario usuario;
}
