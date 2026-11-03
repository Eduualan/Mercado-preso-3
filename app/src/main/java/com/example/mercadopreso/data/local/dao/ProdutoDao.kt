package com.example.mercadopreso.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.mercadopreso.data.local.entity.Produto
import kotlinx.coroutines.flow.Flow

@Dao
interface ProdutoDao {
    @Insert
    suspend fun inserir(item: Produto): Long

    @Query("SELECT * FROM produtos ORDER BY nome")
    fun buscarTodos(): Flow<List<Produto>>

    @Query("SELECT * FROM produtos WHERE id = :id LIMIT 1")
    fun observarPorId(id: Int): Flow<Produto?>

    @Query("SELECT * FROM produtos WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): Produto?

    @Update
    suspend fun atualizar(item: Produto)

    @Delete
    suspend fun deletar(item: Produto)
}
