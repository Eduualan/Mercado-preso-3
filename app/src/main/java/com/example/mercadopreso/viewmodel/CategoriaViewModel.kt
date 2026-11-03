package com.example.mercadopreso.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mercadopreso.data.local.entity.Categoria
import com.example.mercadopreso.data.local.entity.Produto
import com.example.mercadopreso.data.repository.CategoriaRepository
import com.example.mercadopreso.data.repository.ProdutoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoriaUiState(
    val categorias: List<Categoria> = emptyList(),
    val produtos: List<Produto> = emptyList(),
    val categoriaSelecionada: Categoria? = null,
    val produtosDaCategoria: List<Produto> = emptyList(),
    val nome: String = "",
    val descricao: String = "",
    val carregando: Boolean = true,
    val mensagem: String? = null
)

class CategoriaViewModel(
    private val categoriaRepository: CategoriaRepository,
    private val produtoRepository: ProdutoRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CategoriaUiState())
    val uiState = _uiState.asStateFlow()
    private var detalheJob: Job? = null

    init {
        viewModelScope.launch {
            combine(categoriaRepository.buscarTodos(), produtoRepository.buscarTodos()) { categorias, produtos ->
                categorias to produtos
            }.collect { (categorias, produtos) ->
                val selectedId = _uiState.value.categoriaSelecionada?.id
                _uiState.update { state ->
                    state.copy(
                        categorias = categorias,
                        produtos = produtos,
                        produtosDaCategoria = produtos.filter { it.categoriaId == selectedId },
                        carregando = false
                    )
                }
            }
        }
    }

    fun atualizarNome(valor: String) = _uiState.update { it.copy(nome = valor) }
    fun atualizarDescricao(valor: String) = _uiState.update { it.copy(descricao = valor) }

    fun carregarCategoria(id: Int) {
        detalheJob?.cancel()
        detalheJob = viewModelScope.launch {
            categoriaRepository.observarPorId(id).collect { categoria ->
                _uiState.update { state ->
                    state.copy(
                        categoriaSelecionada = categoria,
                        produtosDaCategoria = state.produtos.filter { it.categoriaId == categoria?.id }
                    )
                }
            }
        }
    }

    fun adicionarCategoria() {
        val state = _uiState.value
        if (state.nome.isBlank()) {
            mensagem("Dê um nome para a categoria")
            return
        }
        val nova = Categoria(nome = state.nome.trim(), descricao = state.descricao.ifBlank { "Sem descrição" }.trim())
        viewModelScope.launch {
            runCatching { categoriaRepository.inserir(nova) }
                .onSuccess { _uiState.update { it.copy(nome = "", descricao = "") }; mensagem("Categoria adicionada") }
                .onFailure { Log.e("MercadoPreso", "Falha ao inserir categoria", it); mensagem("Não foi possível adicionar a categoria") }
        }
    }

    fun removerCategoria(categoria: Categoria) {
        if (_uiState.value.produtos.any { it.categoriaId == categoria.id }) {
            mensagem("Remova antes os produtos dessa categoria")
            return
        }
        viewModelScope.launch {
            runCatching { categoriaRepository.deletar(categoria) }
                .onSuccess { mensagem("Categoria removida") }
                .onFailure { Log.e("MercadoPreso", "Falha ao remover categoria", it); mensagem("Não foi possível remover a categoria") }
        }
    }

    fun limparMensagem() = _uiState.update { it.copy(mensagem = null) }

    private fun mensagem(texto: String) = _uiState.update { it.copy(mensagem = texto) }
}
