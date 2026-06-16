package br.com.loja_online.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.loja_online.dto.CartaoDTO;
import br.com.loja_online.mapper.CartaoMapper;
import br.com.loja_online.model.Cartao;
import br.com.loja_online.service.CartaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/cartao")
@RequiredArgsConstructor
public class CartaoController {
    private final CartaoService cartaoService;

    @PostMapping("/create")
    public ResponseEntity<CartaoDTO> insert(@Valid @NonNull @RequestBody CartaoDTO dto){
        Cartao cartao = cartaoService.criarCartao(CartaoMapper.paraCartao(dto));
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(cartao.getId()).toUri();
        return ResponseEntity.created(uri).body(CartaoMapper.paraDto(cartao));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CartaoDTO> getCartaoPorId(@NonNull @PathVariable Integer id) {
        Cartao cartao = cartaoService.getCartaoPorId(id);
        return ResponseEntity.status(200).body(CartaoMapper.paraDto(cartao));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCartao(@NonNull @PathVariable Integer id) {
        cartaoService.deletarCartao(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CartaoDTO> updateCartao(@NonNull @PathVariable Integer id, @Valid @NonNull @RequestBody CartaoDTO dto) {
        Cartao cartao = cartaoService.atualizarCartao(id, CartaoMapper.paraCartao(dto));
        return ResponseEntity.status(200).body(CartaoMapper.paraDto(cartao));
    }   
}
