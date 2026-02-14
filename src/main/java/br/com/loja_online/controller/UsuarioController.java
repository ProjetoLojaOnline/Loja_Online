package br.com.loja_online.controller;

import br.com.loja_online.dto.AutenticacaoDTO;
import br.com.loja_online.dto.UsuarioDTO;
import br.com.loja_online.model.Usuario;
import br.com.loja_online.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;


    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> buscarPorId(@PathVariable Integer id) {
        UsuarioDTO usuario = usuarioService.findById(id);
        return ResponseEntity.ok(usuario);

    }

    @GetMapping("/login/{email}")
    public ResponseEntity<UsuarioDTO> buscarPorLogin(@PathVariable String email) {
        UsuarioDTO usuario = usuarioService.findByLogin(email);
        return ResponseEntity.ok(usuario);
    }

    @PostMapping
    public ResponseEntity<Void> criar(@Valid @RequestBody AutenticacaoDTO autenticacaoDTO) {
        var usuarioSalvo = usuarioService.criar(autenticacaoDTO);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(autenticacaoDTO.id())
                .toUri();
        return ResponseEntity.status(HttpStatus.CREATED).build();
        
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> atualizar(@PathVariable Integer id, @RequestBody Usuario usuario) {
        usuario.setId(id);
        usuarioService.atualizaUsuario(usuario);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        usuarioService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}