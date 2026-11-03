package com.example.mercadopreso.data.repository

import android.content.Context
import android.net.Uri
import com.example.mercadopreso.data.local.dao.ProdutoDao
import com.example.mercadopreso.data.local.entity.Produto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class ProdutoRepository(
    private val produtoDao: ProdutoDao,
    context: Context
) {
    private val appContext = context.applicationContext

    fun buscarTodos(): Flow<List<Produto>> = produtoDao.buscarTodos()

    fun observarPorId(id: Int): Flow<Produto?> = produtoDao.observarPorId(id)

    suspend fun buscarPorId(id: Int): Produto? = produtoDao.buscarPorId(id)

    suspend fun inserir(produto: Produto): Long = produtoDao.inserir(produto)

    suspend fun atualizar(produto: Produto) = produtoDao.atualizar(produto)

    suspend fun deletar(produto: Produto) = produtoDao.deletar(produto)

    suspend fun atualizarPreco(id: Int, novoPreco: Double): Boolean {
        val atual = produtoDao.buscarPorId(id) ?: return false
        produtoDao.atualizar(
            atual.copy(
                preco = novoPreco,
                precoOriginal = maxOf(atual.precoOriginal, novoPreco)
            )
        )
        return true
    }

    suspend fun salvarFoto(uri: Uri): String? = withContext(Dispatchers.IO) {
        val tipo = appContext.contentResolver.getType(uri)
        val extensao = when (tipo) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }
        val pasta = File(appContext.filesDir, "fotos_produtos").apply { mkdirs() }
        val destino = File(pasta, "produto-${UUID.randomUUID()}.$extensao")
        val entrada = appContext.contentResolver.openInputStream(uri) ?: return@withContext null
        entrada.use { input -> destino.outputStream().use(input::copyTo) }
        destino.absolutePath
    }
}
