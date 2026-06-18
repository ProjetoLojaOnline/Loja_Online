package br.com.loja_online.controller;

import java.net.URI;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.loja_online.dto.CartaoDTO;
import br.com.loja_online.mapper.CartaoMapper;
import br.com.loja_online.model.Cartao;
import br.com.loja_online.service.CartaoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/cartao")
@RequiredArgsConstructor
@Tag(name = "Cartões", description = "Gerenciamento de cartões de crédito e débito")
@SecurityRequirement(name = "bearer-jwt")
public class CartaoController {

    private final CartaoService cartaoService;

    @PostMapping("/create")
    @Operation(summary = "Criar cartão", description = "Cria um novo cartão para o usuário autenticado")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Cartão criado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    public ResponseEntity<CartaoDTO> insert(@Valid @NonNull @RequestBody CartaoDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Cartao cartao = cartaoService.criarCartao(CartaoMapper.paraCartao(dto), email);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(cartao.getId()).toUri();
        return ResponseEntity.created(uri).body(CartaoMapper.paraDto(cartao));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cartão por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cartão encontrado"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "404", description = "Cartão não encontrado")
    })
    public ResponseEntity<CartaoDTO> getCartaoPorId(@NonNull @PathVariable Long id) {
        return ResponseEntity.ok(CartaoMapper.paraDto(cartaoService.getCartaoPorId(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar cartão")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Cartão deletado"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "403", description = "Proibido — cartão pertence a outro usuário"),
        @ApiResponse(responseCode = "404", description = "Cartão não encontrado")
    })
    public ResponseEntity<Void> deleteCartao(@NonNull @PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        cartaoService.deletarCartao(id, email);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar cartão")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cartão atualizado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "403", description = "Proibido — cartão pertence a outro usuário"),
        @ApiResponse(responseCode = "404", description = "Cartão não encontrado")
    })
    public ResponseEntity<CartaoDTO> updateCartao(
            @NonNull @PathVariable Long id, @Valid @NonNull @RequestBody CartaoDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Cartao cartao = cartaoService.atualizarCartao(id, CartaoMapper.paraCartao(dto), email);
        return ResponseEntity.ok(CartaoMapper.paraDto(cartao));
    }
}
