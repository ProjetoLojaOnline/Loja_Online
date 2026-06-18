package br.com.loja_online.service;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.loja_online.model.Endereco;
import br.com.loja_online.repository.EnderecoRepository;
import br.com.loja_online.service.exceptions.ObjectNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EnderecoService {

  private final EnderecoRepository enderecoRepository;

  @Transactional
  public Endereco criarEndereco(@NonNull Endereco endereco){
    return enderecoRepository.save(endereco);
  }

  public Endereco getEnderecoPorId(@NonNull Integer id) {
    return enderecoRepository.findById(id).orElseThrow(() ->
            new ObjectNotFoundException("Endereço não encontrado com o ID: " + id));
  }
}
