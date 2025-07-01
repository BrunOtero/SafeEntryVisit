package com.safeentry.Visits.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.safeentry.Visits.util.JwtUtil;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    // Removemos a injeção direta de UserDetailsService no construtor de SecurityConfig
    // O UserDetailsService será um bean independente, injetado onde for necessário.
    public SecurityConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    // Este bean define o UserDetailsService. Ele não depende do SecurityConfig para ser criado.
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            // Este UserDetailsService é simplificado para o serviço Visits,
            // pois a autenticação principal (validade do token) é feita pelo JwtRequestFilter.
            // As autoridades (roles) virão das claims do JWT, conforme configurado no serviço Auth.
            // Por enquanto, "ROLE_USER" é um placeholder, mas o ideal é extrair do JWT.
            return User.withUsername(username)
                    .password("") // Senha não é usada para autenticação JWT neste ponto
                    .authorities("ROLE_USER") // Exemplo de autoridade. O JwtRequestFilter pode popular com as do token.
                    .accountExpired(false)
                    .accountLocked(false)
                    .credentialsExpired(false)
                    .disabled(false)
                    .build();
        };
    }

    // Definimos o JwtRequestFilter como um bean. O Spring irá injetar suas dependências (JwtUtil e UserDetailsService).
    @Bean
    public JwtRequestFilter jwtRequestFilter(UserDetailsService userDetailsService) {
        return new JwtRequestFilter(jwtUtil, userDetailsService);
    }

    // O SecurityFilterChain agora injeta o JwtRequestFilter como um parâmetro.
    // Isso garante que ambos os beans sejam criados independentemente e injetados corretamente.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtRequestFilter jwtRequestFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Desabilita CSRF para APIs REST sem sessão
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/agendamentos/**").authenticated() // Endpoints de agendamento exigem autenticação
                .anyRequest().permitAll() // Todas as outras requisições (se houver) são permitidas sem autenticação
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Sem estado de sessão (ideal para JWT)
            )
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class); // Adiciona o filtro JWT customizado antes do filtro de autenticação padrão

        return http.build();
    }

    // Este bean é necessário para o Spring Security, e ele resolve suas próprias dependências.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}