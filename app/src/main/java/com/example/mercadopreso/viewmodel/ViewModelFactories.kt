package com.example.mercadopreso.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mercadopreso.data.repository.CatalogoInicialRepository
import com.example.mercadopreso.data.repository.CategoriaRepository
import com.example.mercadopreso.data.repository.ProdutoRepository

class ProdutoViewModelFactory(
    private val produtoRepository: ProdutoRepository,
    private val categoriaRepository: CategoriaRepository,
    private val catalogoInicialRepository: CatalogoInicialRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProdutoViewModel::class.java)) {
            return ProdutoViewModel(produtoRepository, categoriaRepository, catalogoInicialRepository) as T
        }
        throw IllegalArgumentException("ViewModel desconhecido: ${modelClass.name}")
    }
}

class CategoriaViewModelFactory(
    private val categoriaRepository: CategoriaRepository,
    private val produtoRepository: ProdutoRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoriaViewModel::class.java)) {
            return CategoriaViewModel(categoriaRepository, produtoRepository) as T
        }
        throw IllegalArgumentException("ViewModel desconhecido: ${modelClass.name}")
    }
}

class CarrinhoViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CarrinhoViewModel::class.java)) return CarrinhoViewModel() as T
        throw IllegalArgumentException("ViewModel desconhecido: ${modelClass.name}")
    }
}

class PerfilViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PerfilViewModel::class.java)) return PerfilViewModel() as T
        throw IllegalArgumentException("ViewModel desconhecido: ${modelClass.name}")
    }
}
