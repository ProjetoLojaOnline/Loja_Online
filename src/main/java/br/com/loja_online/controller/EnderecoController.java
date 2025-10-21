package br.com.loja_online.controller;

import br.com.loja_online.dto.EnderecoDTO;
import br.com.loja_online.mapper.EnderecoMapper;
import br.com.loja_online.model.Endereco;
import br.com.loja_online.service.EnderecoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/endereco")
@RequiredArgsConstructor
public class EnderecoController {

  private final EnderecoService enderecoService;

  @PostMapping("/create")
  public ResponseEntity<Endereco> insert(@Valid @RequestBody EnderecoDTO dto){
    Endereco endereco = enderecoService.criarEndereco(EnderecoMapper.paraEndereco(dto));
    URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
            .buildAndExpand(endereco.getId()).toUri();
    return ResponseEntity.created(uri).body(endereco);
  }

  @GetMapping("/{id}")
  public ResponseEntity<EnderecoDTO> getEnderecoPorId(@PathVariable Integer id) {
    Endereco endereco = enderecoService.getEnderecoPorId(id);
    return ResponseEntity.status(200).body(EnderecoMapper.paraDto(endereco));
  }
}
