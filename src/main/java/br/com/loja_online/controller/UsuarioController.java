package br.com.loja_online.controller;

import br.com.loja_online.dto.*;
import br.com.loja_online.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodos() {
        return ResponseEntity.ok(usuarioService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@NonNull @PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.findById(id));
    }

    @GetMapping("/login/{login}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorLogin(@PathVariable String login) {
        return ResponseEntity.ok(usuarioService.findByLogin(login));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> criar(@Valid @RequestBody UsuarioCadastroWrapper request) {
        UsuarioResponseDTO resultado = usuarioService.insert(request.usuario(), request.login());
        return ResponseEntity.status(201).body(resultado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> atualizar(
            @NonNull @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateDTO dto) {
            UsuarioResponseDTO responseDTO = usuarioService.atualizaUsuario(id, dto);
            return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@NonNull @PathVariable Long id) {
        usuarioService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}