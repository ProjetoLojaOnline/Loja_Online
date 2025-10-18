package br.com.loja_online.controller;

import br.com.loja_online.dto.ProdutoDTO;
import br.com.loja_online.services.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping(value = "/produto")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping
    public ResponseEntity<Page<ProdutoDTO>> findAll(Pageable pageable) {
        Page<ProdutoDTO> produtos = produtoService.findAll(pageable);
        return ResponseEntity.ok(produtos);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<ProdutoDTO> findById(@PathVariable Integer id) {
        ProdutoDTO produto = produtoService.findById(id);
        return ResponseEntity.ok(produto);
    }

    @PostMapping
    public ResponseEntity<ProdutoDTO> save(@Valid @RequestBody ProdutoDTO produtoDTO) {
        produtoDTO = produtoService.insert(produtoDTO);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(produtoDTO.id())
                .toUri();
        return ResponseEntity.created(uri).body(produtoDTO);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<ProdutoDTO> delete(@PathVariable Integer id) {
        produtoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
