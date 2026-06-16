package br.com.loja_online.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.loja_online.model.Cartao;

public interface CartaoRepository extends JpaRepository<Cartao, Integer> {
}
