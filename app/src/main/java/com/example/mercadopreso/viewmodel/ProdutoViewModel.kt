package com.example.mercadopreso.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mercadopreso.data.local.entity.Categoria
import com.example.mercadopreso.data.local.entity.Produto
import com.example.mercadopreso.data.repository.CatalogoInicialRepository
import com.example.mercadopreso.data.repository.CategoriaRepository
import com.example.mercadopreso.data.repository.ProdutoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProdutoUiState(
    val produtos: List<Produto> = emptyList(),
    val categorias: List<Categoria> = emptyList(),
    val produtoSelecionado: Produto? = null,
    val busca: String = "",
    val nome: String = "",
    val vendedor: String = "",
    val descricao: String = "",
    val preco: String = "",
    val precoOriginal: String = "",
    val precoEdicao: String = "",
    val categoriaId: Int? = null,
    val imagemLocal: String? = null,
    val carregando: Boolean = true,
    val salvandoFoto: Boolean = false,
    val mensagem: String? = null
)

class ProdutoViewModel(
    private val produtoRepository: ProdutoRepository,
    private val categoriaRepository: CategoriaRepository,
    private val catalogoInicialRepository: CatalogoInicialRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProdutoUiState())
    val uiState = _uiState.asStateFlow()
    private var detalheJob: Job? = null

    init {
        viewModelScope.launch {
            runCatching { catalogoInicialRepository.inicializarSeNecessario() }
                .onFailure { Log.e("MercadoPreso", "Falha ao preparar catálogo", it) }
            combine(produtoRepository.buscarTodos(), categoriaRepository.buscarTodos()) { produtos, categorias ->
                produtos to categorias
            }.collect { (produtos, categorias) ->
                _uiState.update {
                    it.copy(
                        produtos = produtos,
                        categorias = categorias,
                        categoriaId = it.categoriaId ?: categorias.firstOrNull()?.id,
                        carregando = false
                    )
                }
            }
        }
    }

    fun atualizarBusca(valor: String) = _uiState.update { it.copy(busca = valor) }
    fun atualizarNome(valor: String) = _uiState.update { it.copy(nome = valor) }
    fun atualizarVendedor(valor: String) = _uiState.update { it.copy(vendedor = valor) }
    fun atualizarDescricao(valor: String) = _uiState.update { it.copy(descricao = valor) }
    fun atualizarPreco(valor: String) = _uiState.update { it.copy(preco = valor) }
    fun atualizarPrecoOriginal(valor: String) = _uiState.update { it.copy(precoOriginal = valor) }
    fun selecionarCategoria(id: Int) = _uiState.update { it.copy(categoriaId = id) }

    fun carregarProduto(id: Int) {
        detalheJob?.cancel()
        detalheJob = viewModelScope.launch {
            produtoRepository.observarPorId(id).collect { produto ->
                _uiState.update { state -> state.copy(produtoSelecionado = produto, precoEdicao = produto?.preco?.toString().orEmpty()) }
            }
        }
    }

    fun atualizarPrecoEdicao(valor: String) = _uiState.update { it.copy(precoEdicao = valor) }

    fun selecionarFoto(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(salvandoFoto = true, mensagem = null) }
            runCatching { produtoRepository.salvarFoto(uri) }
                .onSuccess { caminho ->
                    _uiState.update { it.copy(imagemLocal = caminho, salvandoFoto = false) }
                    if (caminho == null) mensagem("Não foi possível abrir essa imagem")
                }
                .onFailure {
                    Log.e("MercadoPreso", "Falha ao salvar foto", it)
                    _uiState.update { state -> state.copy(salvandoFoto = false) }
                    mensagem("Não foi possível salvar essa imagem")
                }
        }
    }

    fun cadastrarProduto() {
        val state = _uiState.value
        val precoAtual = state.preco.trim().replace(',', '.').toDoubleOrNull()
        val categoriaId = state.categoriaId
        if (state.nome.isBlank() || state.vendedor.isBlank() || precoAtual == null || precoAtual <= 0 || categoriaId == null) {
            mensagem("Preencha nome, vendedor, preço e categoria")
            return
        }
        val original = state.precoOriginal.trim().replace(',', '.').toDoubleOrNull() ?: precoAtual
        val produto = Produto(
            nome = state.nome.trim(),
            vendedor = state.vendedor.trim(),
            precoOriginal = maxOf(original, precoAtual),
            preco = precoAtual,
            categoriaId = categoriaId,
            descricao = state.descricao.ifBlank { "Sem descrição. Compre por sua conta e risco." },
            imagemLocal = state.imagemLocal
        )
        viewModelScope.launch {
            runCatching { produtoRepository.inserir(produto) }
                .onSuccess {
                    _uiState.update { it.copy(nome = "", vendedor = "", descricao = "", preco = "", precoOriginal = "", imagemLocal = null, categoriaId = it.categorias.firstOrNull()?.id) }
                    mensagem("Produto cadastrado")
                }
                .onFailure {
                    Log.e("MercadoPreso", "Falha ao inserir produto", it)
                    mensagem("Não foi possível cadastrar o produto")
                }
        }
    }

    fun removerProduto(produto: Produto) {
        viewModelScope.launch {
            runCatching { produtoRepository.deletar(produto) }
                .onSuccess { mensagem("Produto removido") }
                .onFailure { Log.e("MercadoPreso", "Falha ao remover produto", it); mensagem("Não foi possível remover o produto") }
        }
    }

    fun atualizarPreco(id: Int, novoPreco: Double) {
        viewModelScope.launch {
            runCatching { produtoRepository.atualizarPreco(id, novoPreco) }
                .onSuccess { atualizado -> mensagem(if (atualizado) "Preço atualizado" else "Produto não encontrado") }
                .onFailure { Log.e("MercadoPreso", "Falha ao atualizar produto", it); mensagem("Não foi possível atualizar o preço") }
        }
    }

    fun limparMensagem() = _uiState.update { it.copy(mensagem = null) }

    private fun mensagem(texto: String) = _uiState.update { it.copy(mensagem = texto) }
}

