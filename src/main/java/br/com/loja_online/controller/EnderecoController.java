package br.com.loja_online.controller;

import java.net.URI;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.loja_online.dto.EnderecoDTO;
import br.com.loja_online.mapper.EnderecoMapper;
import br.com.loja_online.model.Endereco;
import br.com.loja_online.service.EnderecoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@SuppressWarnings("null")
@RestController
@RequestMapping("/endereco")
@RequiredArgsConstructor
@Tag(name = "Endereços", description = "Gerenciamento de endereços")
@SecurityRequirement(name = "bearer-jwt")
public class EnderecoController {

    private final EnderecoService enderecoService;

    @GetMapping("/{id}")
    @Operation(summary = "Buscar endereço por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Endereço encontrado"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "404", description = "Endereço não encontrado")
    })
    public ResponseEntity<EnderecoDTO> buscarPorId(@NonNull @PathVariable Integer id) {
        return ResponseEntity.ok(EnderecoMapper.paraDto(enderecoService.findById(id)));
    }

    @PostMapping("/create")
    @Operation(summary = "Criar endereço", description = "Cria um novo endereço para o usuário autenticado")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Endereço criado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    public ResponseEntity<EnderecoDTO> insert(@Valid @NonNull @RequestBody EnderecoDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Endereco endereco = enderecoService.criarEndereco(EnderecoMapper.paraEndereco(dto), email);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .replacePath("/endereco/{id}")
                .buildAndExpand(endereco.getId())
                .toUri();
        return ResponseEntity.created(uri).body(EnderecoMapper.paraDto(endereco));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar endereço")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Endereço deletado"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "403", description = "Proibido — endereço pertence a outro usuário"),
        @ApiResponse(responseCode = "404", description = "Endereço não encontrado")
    })
    public ResponseEntity<Void> deletar(@NonNull @PathVariable Integer id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        enderecoService.deleteById(id, email);
        return ResponseEntity.noContent().build();
    }
}
