package com.example.mercadopreso.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ItemCarrinhoUi(val produto: com.example.mercadopreso.data.local.entity.Produto, val quantidade: Int, val marcado: Boolean = false)

data class CarrinhoUiState(
    val itens: List<ItemCarrinhoUi> = emptyList(),
    val cupomAplicado: Boolean = true,
    val ultimoPedido: String = "",
    val ultimoTotal: Double = 0.0,
    val etapaAtual: Int = 1
) {
    val quantidadeTotal: Int get() = itens.sumOf { it.quantidade }
    val subtotal: Double get() = itens.sumOf { it.produto.preco * it.quantidade }
    val desconto: Double get() = if (cupomAplicado && itens.isNotEmpty()) 10.0 else 0.0
    val total: Double get() = subtotal - desconto
}

class CarrinhoViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CarrinhoUiState())
    val uiState = _uiState.asStateFlow()

    fun adicionarAoCarrinho(produto: com.example.mercadopreso.data.local.entity.Produto) = _uiState.update { state ->
        val index = state.itens.indexOfFirst { it.produto.id == produto.id }
        val itens = if (index >= 0) state.itens.toMutableList().also { it[index] = it[index].copy(quantidade = it[index].quantidade + 1) }
        else state.itens + ItemCarrinhoUi(produto, 1)
        state.copy(itens = itens)
    }

    fun removerProduto(id: Int) = _uiState.update { it.copy(itens = it.itens.filterNot { item -> item.produto.id == id }) }

    fun alterarQuantidade(id: Int, delta: Int) = _uiState.update { state ->
        val item = state.itens.firstOrNull { it.produto.id == id } ?: return@update state
        val novaQuantidade = item.quantidade + delta
        val itens = if (novaQuantidade <= 0) state.itens.filterNot { it.produto.id == id }
        else state.itens.map { if (it.produto.id == id) it.copy(quantidade = novaQuantidade) else it }
        state.copy(itens = itens)
    }

    fun alternarMarcado(id: Int) = _uiState.update { state ->
        state.copy(itens = state.itens.map { if (it.produto.id == id) it.copy(marcado = !it.marcado) else it })
    }

    fun removerMarcados(): Int {
        val removidos = _uiState.value.itens.count { it.marcado }
        _uiState.update { it.copy(itens = it.itens.filterNot { it.marcado }) }
        return removidos
    }

    fun alternarCupom() = _uiState.update { it.copy(cupomAplicado = !it.cupomAplicado) }

    fun finalizarCompra(): Boolean {
        val state = _uiState.value
        if (state.itens.isEmpty()) return false
        _uiState.update {
            it.copy(
                ultimoTotal = state.total,
                ultimoPedido = "#PR-${(10000..99999).random()}",
                itens = emptyList(),
                etapaAtual = 1
            )
        }
        return true
    }

    fun proximaEtapa() = _uiState.update { it.copy(etapaAtual = (it.etapaAtual + 1).coerceAtMost(3)) }
}

