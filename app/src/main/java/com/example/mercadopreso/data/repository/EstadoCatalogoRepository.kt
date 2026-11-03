package com.example.mercadopreso.data.repository

import com.example.mercadopreso.data.local.dao.EstadoCatalogoDao
import com.example.mercadopreso.data.local.entity.EstadoCatalogo

/** Guarda se os itens de demonstração já foram inseridos no banco. */
class EstadoCatalogoRepository(private val estadoCatalogoDao: EstadoCatalogoDao) {
    suspend fun foiInicializado(): Boolean = estadoCatalogoDao.foiInicializado() == true

    suspend fun marcarInicializado() {
        estadoCatalogoDao.salvar(EstadoCatalogo(inicializado = true))
    }
}
