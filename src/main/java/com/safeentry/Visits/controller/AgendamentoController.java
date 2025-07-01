package com.safeentry.Visits.controller;

import com.safeentry.Visits.dto.AgendamentoRequest;
import com.safeentry.Visits.dto.AgendamentoResponse;
import com.safeentry.Visits.model.Agendamento;
import com.safeentry.Visits.service.AgendamentoService;
import com.safeentry.Visits.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest; // Importar HttpServletRequest

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/agendamentos")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;
    private final JwtUtil jwtUtil;

    public AgendamentoController(AgendamentoService agendamentoService, JwtUtil jwtUtil) {
        this.agendamentoService = agendamentoService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public ResponseEntity<AgendamentoResponse> createAgendamento(@Valid @RequestBody AgendamentoRequest request,
                                                                 Authentication authentication,
                                                                 HttpServletRequest httpRequest) { // Injetar HttpServletRequest
        try {
            System.out.println("\n\nAQUIIIIIIII\n\n");
            UUID moradorId = null;
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                String authorizationHeader = httpRequest.getHeader("Authorization"); // Obter o cabeçalho Authorization
                if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                    String jwt = authorizationHeader.substring(7); // Extrair o token JWT (remover "Bearer ")
                    moradorId = jwtUtil.extractUserId(jwt); // Extrair o userId do token
                } else {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token JWT não fornecido ou formato inválido.");
                }
            } else {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado ou ID do morador não disponível.");
            }

            Agendamento newAgendamento = agendamentoService.createAgendamento(request, moradorId);
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(newAgendamento));
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao criar agendamento: " + e.getMessage());
        }
    }

    @GetMapping("/morador/{moradorId}")
    public ResponseEntity<List<AgendamentoResponse>> getAgendamentosByMorador(@PathVariable UUID moradorId) {
        List<Agendamento> agendamentos = agendamentoService.getAgendamentosByMorador(moradorId);
        List<AgendamentoResponse> responses = agendamentos.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/me")
    public ResponseEntity<List<AgendamentoResponse>> getMyAgendamentos(Authentication authentication,
                                                                       HttpServletRequest httpRequest) { // Injetar HttpServletRequest
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado.");
        }

        String authorizationHeader = httpRequest.getHeader("Authorization"); // Obter o cabeçalho Authorization
        UUID moradorId = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwt = authorizationHeader.substring(7); // Extrair o token JWT
            moradorId = jwtUtil.extractUserId(jwt); // Extrair o userId do token
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token JWT não fornecido ou formato inválido.");
        }

        List<Agendamento> agendamentos = agendamentoService.getAgendamentosByMorador(moradorId);
        List<AgendamentoResponse> responses = agendamentos.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    // Endpoint para obter detalhes de um agendamento específico
    @GetMapping("/{id}")
    public ResponseEntity<AgendamentoResponse> getAgendamentoById(@PathVariable UUID id) {
        return agendamentoService.getAgendamentoById(id)
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agendamento não encontrado."));
    }

    // Endpoint para obter um agendamento pelo QR Token (para o serviço de portaria)
    @GetMapping("/qr/{qrToken}")
    public ResponseEntity<AgendamentoResponse> getAgendamentoByQrToken(@PathVariable String qrToken) {
        return agendamentoService.getAgendamentoByQrToken(qrToken)
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agendamento não encontrado para o QR Token fornecido."));
    }

    // Endpoint para marcar um agendamento como usado (para o serviço de portaria)
    @PatchMapping("/qr/{qrToken}/use")
    public ResponseEntity<AgendamentoResponse> markAgendamentoAsUsed(@PathVariable String qrToken) {
        try {
            Agendamento updatedAgendamento = agendamentoService.markAgendamentoAsUsed(qrToken);
            return ResponseEntity.ok(convertToDto(updatedAgendamento));
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao marcar agendamento como usado: " + e.getMessage());
        }
    }

    @PatchMapping("/me/{id}/cancel")
    public ResponseEntity<AgendamentoResponse> cancelMyAgendamento(@PathVariable UUID id,
                                                                  Authentication authentication,
                                                                  HttpServletRequest httpRequest) {
        try {
            UUID moradorId = null;
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                String authorizationHeader = httpRequest.getHeader("Authorization");
                if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                    String jwt = authorizationHeader.substring(7);
                    moradorId = jwtUtil.extractUserId(jwt);
                } else {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token JWT não fornecido ou formato inválido.");
                }
            } else {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado ou ID do morador não disponível.");
            }

            Agendamento cancelledAgendamento = agendamentoService.cancelAgendamento(id, moradorId);
            return ResponseEntity.ok(convertToDto(cancelledAgendamento));
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao cancelar agendamento: " + e.getMessage());
        }
    }

    private AgendamentoResponse convertToDto(Agendamento agendamento) {
        return new AgendamentoResponse(
                agendamento.getId(),
                agendamento.getMoradorId(),
                agendamento.getDataHoraVisita(),
                agendamento.getVisitanteJson(),
                agendamento.getQrToken(),
                agendamento.getUsado(),
                agendamento.getStatus(),
                agendamento.getCriadoEm()
        );
    }
}