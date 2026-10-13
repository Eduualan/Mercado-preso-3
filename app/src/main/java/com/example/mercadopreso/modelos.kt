package com.example.mercadopreso

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// ---------- Data classes ----------
data class Categoria(val id: Int, val nome: String, val descricao: String)

data class Produto(
    val id: Int,
    val nome: String,
    val vendedor: String,
    val precoOriginal: Double,
    val preco: Double,
    val categoriaId: Int,
    val descricao: String
)

data class ItemCarrinho(val produto: Produto, val quantidade: Int, val marcado: Boolean = false)

// ---------- Rotas ----------
object Rotas {
    const val PRODUTOS = "produtos"
    const val DETALHE_PRODUTO = "produto/{id}"
    const val NOVO_PRODUTO = "novo_produto"
    const val CATEGORIAS = "categorias"
    const val DETALHE_CATEGORIA = "categoria/{id}"
    const val CARRINHO = "carrinho"
    const val CONFIRMADO = "confirmado"
    const val ACOMPANHAR = "acompanhar"

    fun detalheProduto(id: Int) = "produto/$id"
    fun detalheCategoria(id: Int) = "categoria/$id"
}

// ---------- Estado (tudo em memória, como pede o trabalho) ----------
class LojaState {
    private var proximoId = 100

    val categorias = mutableStateListOf(
        Categoria(1, "Acessórios de Fuga", "Tudo para quem tem planos (e pouca sorte)"),
        Categoria(2, "Vestuário Penitenciário", "Moda listrada para todas as estações"),
        Categoria(3, "Documentos e Fiança", "Papelada para sair mais cedo")
    )

    val produtos = mutableStateListOf(
        Produto(1, "Algemas reforçadas mod. fuga zero", "Cadeia Pública LTDA", 129.90, 89.90, 1,
            "Aço inoxidável, ajuste universal. Mais vendido em fugas mal sucedidas."),
        Produto(2, "Uniforme listrado tamanho G", "Confecções da Cela", 79.90, 64.90, 2,
            "Algodão listrado, caimento clássico. Não amassa na solitária."),
        Produto(3, "Carta de fiança (modelo genérico)", "Cartório Digital", 60.00, 45.00, 3,
            "Modelo genérico, preencha e torça para dar certo."),
        Produto(4, "Colher de cela multiuso", "Cadeia Pública LTDA", 24.90, 19.90, 1,
            "Serve para sopa e para planos de longo prazo."),
        Produto(5, "Chinelo de banho coletivo", "Confecções da Cela", 29.90, 22.50, 2,
            "Antiderrapante, ideal para o pátio.")
    )

    val carrinho = mutableStateListOf(
        ItemCarrinho(produtos[0], 1),
        ItemCarrinho(produtos[1], 2),
        ItemCarrinho(produtos[2], 1)
    )

    var cupomAplicado by mutableStateOf(true)
    var ultimoPedido by mutableStateOf("")
    var ultimoTotal by mutableStateOf(0.0)

    // ----- consultas -----
    fun categoriaDe(id: Int): Categoria? = categorias.firstOrNull { it.id == id }
    fun produtosDa(categoriaId: Int): List<Produto> = produtos.filter { it.categoriaId == categoriaId }

    val quantidadeTotal: Int get() = carrinho.sumOf { it.quantidade }
    val subtotal: Double get() = carrinho.sumOf { it.produto.preco * it.quantidade }
    val desconto: Double get() = if (cupomAplicado && carrinho.isNotEmpty()) 10.0 else 0.0
    val total: Double get() = subtotal - desconto

    // ----- produtos -----
    fun adicionarProduto(nome: String, vendedor: String, original: Double, preco: Double, categoriaId: Int, descricao: String) {
        produtos.add(Produto(proximoId++, nome, vendedor, original, preco, categoriaId, descricao))
    }

    fun removerProduto(produto: Produto) {
        produtos.remove(produto)
        carrinho.removeAll { it.produto.id == produto.id }
    }

    fun atualizarPreco(id: Int, novoPreco: Double) {
        val i = produtos.indexOfFirst { it.id == id }
        if (i < 0) return
        val atualizado = produtos[i].copy(preco = novoPreco, precoOriginal = maxOf(produtos[i].precoOriginal, novoPreco))
        produtos[i] = atualizado
        val j = carrinho.indexOfFirst { it.produto.id == id }
        if (j >= 0) carrinho[j] = carrinho[j].copy(produto = atualizado)
    }

    // ----- categorias -----
    fun adicionarCategoria(nome: String, descricao: String) {
        categorias.add(Categoria(proximoId++, nome, descricao))
    }

    /** Só remove se a categoria estiver vazia. Retorna false se não foi possível. */
    fun removerCategoria(categoria: Categoria): Boolean {
        if (produtosDa(categoria.id).isNotEmpty()) return false
        categorias.remove(categoria)
        return true
    }

    // ----- carrinho -----
    fun adicionarAoCarrinho(produto: Produto) {
        val i = carrinho.indexOfFirst { it.produto.id == produto.id }
        if (i >= 0) carrinho[i] = carrinho[i].copy(quantidade = carrinho[i].quantidade + 1)
        else carrinho.add(ItemCarrinho(produto, 1))
    }

    fun alterarQuantidade(id: Int, delta: Int) {
        val i = carrinho.indexOfFirst { it.produto.id == id }
        if (i < 0) return
        val nova = carrinho[i].quantidade + delta
        if (nova <= 0) carrinho.removeAt(i) else carrinho[i] = carrinho[i].copy(quantidade = nova)
    }

    fun alternarMarcado(id: Int) {
        val i = carrinho.indexOfFirst { it.produto.id == id }
        if (i >= 0) carrinho[i] = carrinho[i].copy(marcado = !carrinho[i].marcado)
    }

    fun removerMarcados(): Int {
        val n = carrinho.count { it.marcado }
        carrinho.removeAll { it.marcado }
        return n
    }

    fun finalizarCompra() {
        ultimoTotal = total
        ultimoPedido = "#PR-${(10000..99999).random()}"
        carrinho.clear()
    }
}
