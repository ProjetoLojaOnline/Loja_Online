package br.com.loja_online.service;

import br.com.loja_online.dto.ProdutoDTO;
import br.com.loja_online.model.Produto;
import br.com.loja_online.mapper.ProdutoMapper;
import br.com.loja_online.repository.ProdutoRepository;
import br.com.loja_online.service.exceptions.ObjectNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @Transactional(readOnly = true)
    public Page<ProdutoDTO> findAll(Pageable pageable) {
        Page<Produto> produtos = produtoRepository.findAll(pageable);
        return produtos.map(ProdutoMapper::paraDto);
    }

    @Transactional(readOnly = true)
    public ProdutoDTO findById(Integer id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Produto não encontrado com o ID: " + id));
        return ProdutoMapper.paraDto(produto);
    }

    @Transactional
    public ProdutoDTO insert(ProdutoDTO dto) {
        Produto novoProduto = ProdutoMapper.paraProduto(dto);
        novoProduto.setId(null); // o Jpa exige receber um id nulo para criação com auto-generate de ids. Seria melhor usar um DTO específico para criação, sem id
        novoProduto = produtoRepository.save(novoProduto);
        return ProdutoMapper.paraDto(novoProduto);
    }



    @Transactional
    public void delete(Integer id) {
        produtoRepository.findById(id).orElseThrow(() -> new ObjectNotFoundException("Produto não encontrado com o ID: " + id));
        produtoRepository.deleteById(id);
    }
}
