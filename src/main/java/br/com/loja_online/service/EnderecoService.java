package br.com.loja_online.service;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.loja_online.model.Endereco;
import br.com.loja_online.model.Usuario;
import br.com.loja_online.repository.EnderecoRepository;
import br.com.loja_online.repository.UsuarioRepository;
import br.com.loja_online.service.exceptions.ForbiddenException;
import br.com.loja_online.service.exceptions.ObjectNotFoundException;

@Service
public class EnderecoService {

    private final EnderecoRepository enderecoRepository;
    private final UsuarioRepository usuarioRepository;

    public EnderecoService(EnderecoRepository enderecoRepository, UsuarioRepository usuarioRepository) {
        this.enderecoRepository = enderecoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Endereco criarEndereco(@NonNull Endereco endereco, @NonNull String emailProprietario) {
        Usuario usuario = usuarioRepository
                .findByEmail(emailProprietario)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não encontrado: " + emailProprietario));
        endereco.setUsuario(usuario);
        return enderecoRepository.save(endereco);
    }

    public Endereco findById(@NonNull Integer id, @NonNull String emailProprietario) {
        Endereco endereco = enderecoRepository
                .findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Endereço não encontrado com o ID: " + id));
        if (endereco.getUsuario() == null || !endereco.getUsuario().getEmail().equals(emailProprietario)) {
            throw new ForbiddenException("Acesso negado: este endereço pertence a outro usuário");
        }
        return endereco;
    }

    @Transactional
    public void deleteById(@NonNull Integer id, @NonNull String emailProprietario) {
        Endereco endereco = enderecoRepository
                .findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Endereço não encontrado com o ID: " + id));
        if (endereco.getUsuario() == null || !endereco.getUsuario().getEmail().equals(emailProprietario)) {
            throw new ForbiddenException("Acesso negado: este endereço pertence a outro usuário");
        }
        enderecoRepository.deleteById(id);
    }
}
