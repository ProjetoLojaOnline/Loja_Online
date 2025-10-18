package br.com.loja_online.service;

import br.com.loja_online.model.Endereco;
import br.com.loja_online.repository.EnderecoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EnderecoService {

  private final EnderecoRepository enderecoRepository;

  @Transactional
  public Endereco criarEndereco(Endereco endereco){
    return enderecoRepository.save(endereco);
  }
}
