package br.com.loja_online.controller;

import java.net.URI;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.loja_online.dto.EnderecoDTO;
import br.com.loja_online.mapper.EnderecoMapper;
import br.com.loja_online.model.Endereco;
import br.com.loja_online.service.EnderecoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/endereco")
@RequiredArgsConstructor
public class EnderecoController {

    private final EnderecoService enderecoService;

    @GetMapping("/{id}")
    public ResponseEntity<EnderecoDTO> buscarPorId(@NonNull @PathVariable Integer id) {
        return ResponseEntity.ok(EnderecoMapper.paraDto(enderecoService.findById(id)));
    }

    @PostMapping("/create")
    public ResponseEntity<EnderecoDTO> insert(@Valid @NonNull @RequestBody EnderecoDTO dto) {
        Endereco endereco = enderecoService.criarEndereco(EnderecoMapper.paraEndereco(dto));
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .replacePath("/endereco/{id}")
                .buildAndExpand(endereco.getId())
                .toUri();
        return ResponseEntity.created(uri).body(EnderecoMapper.paraDto(endereco));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@NonNull @PathVariable Integer id) {
        enderecoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
