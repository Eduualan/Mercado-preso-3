package com.example.mercadopreso.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.mercadopreso.data.local.entity.Categoria
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriaDao {
    @Insert
    suspend fun inserir(item: Categoria): Long

    @Query("SELECT * FROM categorias ORDER BY nome")
    fun buscarTodos(): Flow<List<Categoria>>

    @Query("SELECT * FROM categorias WHERE id = :id LIMIT 1")
    fun observarPorId(id: Int): Flow<Categoria?>

    @Query("SELECT * FROM categorias WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): Categoria?

    @Update
    suspend fun atualizar(item: Categoria)

    @Delete
    suspend fun deletar(item: Categoria)
}
