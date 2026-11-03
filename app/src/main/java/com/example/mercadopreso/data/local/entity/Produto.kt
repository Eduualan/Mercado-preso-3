package com.example.mercadopreso.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "produtos")
data class Produto(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nome: String,
    val vendedor: String,
    val precoOriginal: Double,
    val preco: Double,
    val categoriaId: Int,
    val descricao: String,
    @ColumnInfo(defaultValue = "NULL") val imagemLocal: String? = null
)
