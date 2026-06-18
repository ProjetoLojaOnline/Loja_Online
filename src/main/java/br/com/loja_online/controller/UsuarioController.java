package br.com.loja_online.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import br.com.loja_online.dto.UsuarioCadastroWrapper;
import br.com.loja_online.dto.UsuarioResponseDTO;
import br.com.loja_online.dto.UsuarioUpdateDTO;
import br.com.loja_online.service.UsuarioService;

import lombok.RequiredArgsConstructor;

@SuppressWarnings("null")
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodos() {
        return ResponseEntity.ok(usuarioService.findAll());
    }

    @GetMapping("/login/{login}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorLogin(@NonNull @PathVariable String login) {
        return ResponseEntity.ok(usuarioService.findByLogin(login));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@NonNull @PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.findById(id));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> criar(@Valid @RequestBody UsuarioCadastroWrapper request) {
        UsuarioResponseDTO resultado = usuarioService.insert(request.usuario(), request.login());
        return ResponseEntity.status(201).body(resultado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> atualizar(
            @NonNull @PathVariable Long id, @Valid @RequestBody UsuarioUpdateDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(usuarioService.atualizaUsuario(id, dto, email));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@NonNull @PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        usuarioService.deleteById(id, email);
        return ResponseEntity.noContent().build();
    }
}
