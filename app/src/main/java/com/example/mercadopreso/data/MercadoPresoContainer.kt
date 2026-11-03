package com.example.mercadopreso.data

import android.content.Context
import com.example.mercadopreso.data.local.AppDatabase
import com.example.mercadopreso.data.repository.CatalogoInicialRepository
import com.example.mercadopreso.data.repository.CategoriaRepository
import com.example.mercadopreso.data.repository.EstadoCatalogoRepository
import com.example.mercadopreso.data.repository.ProdutoRepository

/** Monta as dependências da camada de dados em um único ponto da aplicação. */
class MercadoPresoContainer(context: Context) {
    private val database = AppDatabase.getDatabase(context.applicationContext)

    val produtoRepository = ProdutoRepository(database.produtoDao(), context.applicationContext)
    val categoriaRepository = CategoriaRepository(database.categoriaDao())
    val estadoCatalogoRepository = EstadoCatalogoRepository(database.estadoCatalogoDao())
    val catalogoInicialRepository = CatalogoInicialRepository(database, estadoCatalogoRepository, categoriaRepository, produtoRepository)
}
