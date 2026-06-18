package br.com.loja_online.service;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.loja_online.model.Cartao;
import br.com.loja_online.model.Usuario;
import br.com.loja_online.repository.CartaoRepository;
import br.com.loja_online.repository.UsuarioRepository;
import br.com.loja_online.service.exceptions.ForbiddenException;
import br.com.loja_online.service.exceptions.ObjectNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartaoService {

    private final CartaoRepository cartaoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public Cartao criarCartao(@NonNull Cartao cartao, @NonNull String emailProprietario) {
        Usuario usuario = usuarioRepository
                .findByEmail(emailProprietario)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não encontrado: " + emailProprietario));
        cartao.setUsuario(usuario);
        return cartaoRepository.save(cartao);
    }

    public Cartao getCartaoPorId(@NonNull Long id) {
        return cartaoRepository
                .findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Cartão não encontrado com o ID: " + id));
    }

    @Transactional
    public void deletarCartao(@NonNull Long id, @NonNull String emailProprietario) {
        Cartao cartao = cartaoRepository
                .findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Cartão não encontrado com o ID: " + id));
        if (cartao.getUsuario() == null || !cartao.getUsuario().getEmail().equals(emailProprietario)) {
            throw new ForbiddenException("Acesso negado: este cartão pertence a outro usuário");
        }
        cartaoRepository.deleteById(id);
    }

    @Transactional
    public Cartao atualizarCartao(@NonNull Long id, @NonNull Cartao dadosAtualizados, @NonNull String emailProprietario) {
        Cartao cartaoExistente = cartaoRepository
                .findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Cartão não encontrado com o ID: " + id));
        if (cartaoExistente.getUsuario() == null
                || !cartaoExistente.getUsuario().getEmail().equals(emailProprietario)) {
            throw new ForbiddenException("Acesso negado: este cartão pertence a outro usuário");
        }
        cartaoExistente.setNumeroCartao(dadosAtualizados.getNumeroCartao());
        cartaoExistente.setNomeCartao(dadosAtualizados.getNomeCartao());
        cartaoExistente.setDataValidade(dadosAtualizados.getDataValidade());
        cartaoExistente.setCvv(dadosAtualizados.getCvv());
        cartaoExistente.setDefaultCard(dadosAtualizados.getDefaultCard());
        return cartaoRepository.save(cartaoExistente);
    }
}
