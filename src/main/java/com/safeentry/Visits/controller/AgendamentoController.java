package com.safeentry.Visits.controller;

import com.safeentry.Visits.dto.AgendamentoRequest;
import com.safeentry.Visits.dto.AgendamentoResponse;
import com.safeentry.Visits.model.Agendamento;
import com.safeentry.Visits.service.AgendamentoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // Para obter o usuário autenticado
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/agendamentos")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    public AgendamentoController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    // Endpoint para criar um novo agendamento
    // Apenas moradores podem criar agendamentos
    @PostMapping
    // @PreAuthorize("hasAuthority('MORADOR')") // Use Spring Security se integrar com Auth Service
    public ResponseEntity<AgendamentoResponse> createAgendamento(@Valid @RequestBody AgendamentoRequest request,
                                                                 Authentication authentication) {
        try {
            // Extrair o ID do morador do token JWT (assumindo que o ID do usuário é o "name" ou um claim)
            // No serviço de Auth, o email é o username. Aqui, precisaremos do ID do morador.
            // Para isso, você precisaria estender o UserDetails ou adicionar um claim com o ID no JWT do AuthService
            // Por simplicidade para este exemplo, vamos pegar o email e "simular" um ID ou esperar que o Auth Service
            // forneça o ID do morador no token para validações mais robustas.
            // Para um sistema real, o JWT do Auth Service DEVE incluir o UUID do usuário.
            // Exemplo: String userIdString = jwtUtil.extractClaim(jwt, claims -> claims.get("userId", String.class));
            // UUID moradorId = UUID.fromString(userIdString);

            // Para este microsserviço de Agendamento, vamos simular o moradorId.
            // EM PRODUÇÃO: O moradorId deve vir de um cabeçalho customizado ou de um claim JWT válido e seguro
            // transmitido pelo gateway ou extraído de um token JWT compartilhado/validado.
            // Para testar localmente, você pode passar o moradorId no corpo ou em um cabeçalho (apenas para dev!).
            // Ou, melhor ainda, se você tiver o microsserviço de Auth rodando, você pode fazer uma chamada
            // interna para ele para obter o ID do usuário autenticado.

            // Por enquanto, vamos usar um UUID hardcoded para teste ou injetar um
            // String principalEmail = authentication.getName(); // Email do usuário autenticado
            // UUID moradorId = UUID.fromString("00000000-0000-0000-0000-000000000001"); // Exemplo de UUID de morador para teste
            
            // Para um cenário mais realista onde o ID do usuário está no token:
            // Assumimos que o JWT do Auth Service injeta o UUID do morador no SecurityContext
            // Ou que você fez uma chamada para o Auth Service para obter o ID
            // Se o AuthService retorna o UUID no JWT como um claim "userId", você pode obtê-lo assim:
            UUID moradorId = null;
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                 // Supondo que você adicionou o ID do usuário como um claim no JWT e o UserDetailsService no Auth Service
                 // o inclua no UserDetails personalizado ou você o busque por email
                 // Para este exemplo, vamos usar o email para simplificar e buscar o ID no Auth Service (idealmente)
                 // Ou se você tiver um Gateway que passa o ID do usuário para este serviço
                 // Por enquanto, vou usar um valor mock para que o código compile e seja testável.
                 // Realistically, the ID should be passed in a specific way by the gateway or another service.
                moradorId = UUID.randomUUID(); // Mock UUID for demonstration. REPLACE THIS IN REAL APP.
                // OU, se você tiver um serviço de autorização interno que já validou e forneceu o ID:
                // moradorId = (UUID) authentication.getDetails(); // Se você configurar o Authentication object para ter o ID
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

    // Endpoint para listar agendamentos de um morador
    @GetMapping("/morador/{moradorId}")
    // @PreAuthorize("hasAuthority('MORADOR') or hasAuthority('ADMIN')") // Acesso restrito
    public ResponseEntity<List<AgendamentoResponse>> getAgendamentosByMorador(@PathVariable UUID moradorId) {
        List<Agendamento> agendamentos = agendamentoService.getAgendamentosByMorador(moradorId);
        List<AgendamentoResponse> responses = agendamentos.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    // Endpoint para obter detalhes de um agendamento específico
    @GetMapping("/{id}")
    // @PreAuthorize("hasAuthority('MORADOR') or hasAuthority('ADMIN') or hasAuthority('PORTEIRO')")
    public ResponseEntity<AgendamentoResponse> getAgendamentoById(@PathVariable UUID id) {
        return agendamentoService.getAgendamentoById(id)
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agendamento não encontrado."));
    }

    // Endpoint para obter um agendamento pelo QR Token (para o serviço de portaria)
    @GetMapping("/qr/{qrToken}")
    // @PreAuthorize("hasAuthority('PORTEIRO')") // Apenas porteiros podem usar este endpoint
    public ResponseEntity<AgendamentoResponse> getAgendamentoByQrToken(@PathVariable String qrToken) {
        return agendamentoService.getAgendamentoByQrToken(qrToken)
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agendamento não encontrado para o QR Token fornecido."));
    }

    // Endpoint para marcar um agendamento como usado (para o serviço de portaria)
    @PatchMapping("/qr/{qrToken}/use")
    // @PreAuthorize("hasAuthority('PORTEIRO')")
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


    // Método auxiliar para converter Agendamento para AgendamentoResponse DTO
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