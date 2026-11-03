package com.example.mercadopreso.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "estado_catalogo")
data class EstadoCatalogo(
    @PrimaryKey val id: Int = 1,
    val inicializado: Boolean = false
)
