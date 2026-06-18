package br.com.loja_online.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import br.com.loja_online.builder.ProdutoBuilder;
import br.com.loja_online.dto.ProdutoDTO;
import br.com.loja_online.model.Produto;
import br.com.loja_online.repository.ProdutoRepository;
import br.com.loja_online.service.ProdutoService;
import br.com.loja_online.service.exceptions.ObjectNotFoundException;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private ProdutoService produtoService;

    private Produto produto;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        produto = ProdutoBuilder.padrao().comId(1).buildModel();
        pageable = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("findAllDeveRetornarPaginaComProdutos")
    void findAllDeveRetornarPaginaComProdutos() {
        when(produtoRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(produto)));

        Page<ProdutoDTO> resultado = produtoService.findAll(pageable);

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).nome()).isEqualTo(produto.getNome());
    }

    @Test
    @DisplayName("findAllDeveRetornarPaginaVazia")
    void findAllDeveRetornarPaginaVazia() {
        when(produtoRepository.findAll(pageable)).thenReturn(Page.empty());

        Page<ProdutoDTO> resultado = produtoService.findAll(pageable);

        assertThat(resultado.getContent()).isEmpty();
    }

    @Test
    @DisplayName("findByIdDeveRetornarProdutoQuandoExiste")
    void findByIdDeveRetornarProdutoQuandoExiste() {
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));

        ProdutoDTO resultado = produtoService.findById(1);

        assertThat(resultado.nome()).isEqualTo(produto.getNome());
    }

    @Test
    @DisplayName("findByIdDeveLancarExceptionQuandoNaoExiste")
    void findByIdDeveLancarExceptionQuandoNaoExiste() {
        when(produtoRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoService.findById(99))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("insertDeveRetornarProdutoSalvoComIdGerado")
    void insertDeveRetornarProdutoSalvoComIdGerado() {
        ProdutoDTO dto = ProdutoBuilder.padrao().buildDto();
        Produto salvo = ProdutoBuilder.padrao().comId(5).buildModel();
        when(produtoRepository.save(any(Produto.class))).thenReturn(salvo);

        ProdutoDTO resultado = produtoService.insert(dto);

        assertThat(resultado.id()).isEqualTo(5);
    }

    @Test
    @DisplayName("deleteDeveRemoverQuandoExiste")
    void deleteDeveRemoverQuandoExiste() {
        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));

        produtoService.delete(1);

        verify(produtoRepository).deleteById(1);
    }

    @Test
    @DisplayName("deleteDeveLancarExceptionQuandoNaoExiste")
    void deleteDeveLancarExceptionQuandoNaoExiste() {
        when(produtoRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoService.delete(99))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessageContaining("99");
    }
}
