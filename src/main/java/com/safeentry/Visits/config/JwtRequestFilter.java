package com.safeentry.Visits.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.safeentry.Visits.util.JwtUtil; // Certifique-se de que este caminho está correto

import java.io.IOException;
import java.util.UUID; // Importar UUID

// Filtro JWT para interceptar requisições e validar o token no serviço Visits
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService; // Será usado para carregar UserDetails

    public JwtRequestFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null; // O email do usuário
        String jwt = null;
        UUID userId = null; // Para armazenar o UUID do usuário

        // Verifica se o cabeçalho Authorization existe e começa com "Bearer "
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7); // Extrai o token JWT
            try {
                username = jwtUtil.extractUsername(jwt); // Extrai o email do usuário do token
                userId = jwtUtil.extractUserId(jwt); // Extrai o userId do token
            } catch (Exception e) {
                // Logar o erro (e.g., token inválido, expirado, etc.)
                System.err.println("Erro ao extrair informações do JWT: " + e.getMessage());
                // Não lançar exceção aqui, deixe o fluxo continuar para o Spring Security lidar com 401/403
            }
        }

        // Se o email e o userId foram extraídos e não há autenticação no contexto de segurança atual
        if (username != null && userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Carrega os detalhes do usuário usando o UserDetailsService
            // Este UserDetailsService no Visits service será simplificado,
            // apenas criando um UserDetails com base no username e autoridades do token.
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // Valida o token
            if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {
                // Cria um token de autenticação e o define no contexto de segurança
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                
                // Anexe o JWT completo nos detalhes da autenticação.
                // Isso permite que o controlador o recupere, como você tentou fazer.
                usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // Podemos adicionar o userId diretamente nos detalhes se quiser, mas a extração do cabeçalho já funciona.
                // ((WebAuthenticationDetails)usernamePasswordAuthenticationToken.getDetails()).setCustomAttribute("userId", userId); 
                
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        chain.doFilter(request, response); // Continua a cadeia de filtros
    }
}