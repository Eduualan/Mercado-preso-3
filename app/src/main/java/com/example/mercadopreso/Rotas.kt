package com.example.mercadopreso

object Rotas {
    const val PRODUTOS = "produtos"
    const val PERFIL = "perfil"
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
