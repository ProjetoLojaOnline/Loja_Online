package br.com.loja_online.service;

import br.com.loja_online.model.Cartao;
import br.com.loja_online.repository.CartaoRepository;
import br.com.loja_online.service.exceptions.ObjectNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartaoService {

    private final CartaoRepository cartaoRepository;

    @Transactional
    public Cartao criarCartao(@NonNull Cartao cartao) {
        return cartaoRepository.save(cartao);
    }

    public Cartao getCartaoPorId(@NonNull Integer id) {
        return cartaoRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Cartão não encontrado com o ID: " + id));
    }

    @Transactional
    public void deletarCartao(@NonNull Integer id) {
        if (!cartaoRepository.existsById(id)) {
            throw new ObjectNotFoundException("Cartão não encontrado com o ID: " + id);
        }
        cartaoRepository.deleteById(id);
    }

    @Transactional
    public Cartao atualizarCartao(@NonNull Integer id, @NonNull Cartao cartao) {
        if (!cartaoRepository.existsById(id)) {
            throw new ObjectNotFoundException("Cartão não encontrado com o ID: " + id);
        }
        cartao.setId(id.longValue());
        return cartaoRepository.save(cartao);
    }
}
