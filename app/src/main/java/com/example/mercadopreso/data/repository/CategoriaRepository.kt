package com.example.mercadopreso.data.repository

import com.example.mercadopreso.data.local.dao.CategoriaDao
import com.example.mercadopreso.data.local.entity.Categoria
import kotlinx.coroutines.flow.Flow

class CategoriaRepository(private val categoriaDao: CategoriaDao) {
    fun buscarTodos(): Flow<List<Categoria>> = categoriaDao.buscarTodos()

    fun observarPorId(id: Int): Flow<Categoria?> = categoriaDao.observarPorId(id)

    suspend fun buscarPorId(id: Int): Categoria? = categoriaDao.buscarPorId(id)

    suspend fun inserir(categoria: Categoria): Long = categoriaDao.inserir(categoria)

    suspend fun atualizar(categoria: Categoria) = categoriaDao.atualizar(categoria)

    suspend fun deletar(categoria: Categoria) = categoriaDao.deletar(categoria)
}
