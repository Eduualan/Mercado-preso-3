package com.example.mercadopreso.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mercadopreso.data.local.entity.EstadoCatalogo

@Dao
interface EstadoCatalogoDao {
    @Query("SELECT inicializado FROM estado_catalogo WHERE id = 1 LIMIT 1")
    suspend fun foiInicializado(): Boolean?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvar(estado: EstadoCatalogo)
}
