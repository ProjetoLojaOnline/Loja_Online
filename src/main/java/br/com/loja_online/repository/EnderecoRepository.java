package br.com.loja_online.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.loja_online.model.Endereco;

public interface EnderecoRepository extends JpaRepository<Endereco, Integer> {
}
