package br.com.loja_online.service;

import br.com.loja_online.dto.ProdutoDTO;
import br.com.loja_online.model.Produto;
import br.com.loja_online.mapper.ProdutoMapper;
import br.com.loja_online.repository.ProdutoRepository;
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
        Optional<Produto> produto = produtoRepository.findById(id);
        return produto.map(ProdutoMapper::paraDto).orElse(null);
    }

    @Transactional
    public ProdutoDTO insert(ProdutoDTO dto) {
        Produto novoProduto = ProdutoMapper.paraProduto(dto);
        novoProduto = produtoRepository.save(novoProduto);
        return ProdutoMapper.paraDto(novoProduto);
    }

    @Transactional
    public void delete(Integer id) {
        produtoRepository.deleteById(id);
    }
}
