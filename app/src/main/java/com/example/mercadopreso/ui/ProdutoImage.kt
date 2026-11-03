package com.example.mercadopreso.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.mercadopreso.R
import com.example.mercadopreso.data.local.entity.Produto

private fun imagemDoProduto(produto: Produto): Int? = when {
    produto.nome.contains("algema", ignoreCase = true) -> R.drawable.product_algemas
    produto.nome.contains("colher", ignoreCase = true) -> R.drawable.product_colher
    produto.nome.contains("smartphone", ignoreCase = true) || produto.nome.contains("celular", ignoreCase = true) -> R.drawable.product_celular
    produto.nome.contains("tênis", ignoreCase = true) || produto.nome.contains("tenis", ignoreCase = true) -> R.drawable.product_tenis
    produto.nome.contains("caneca", ignoreCase = true) -> R.drawable.product_caneca
    produto.nome.contains("caderno", ignoreCase = true) -> R.drawable.product_caderno
    produto.nome.contains("bola", ignoreCase = true) -> R.drawable.product_bola
    produto.nome.contains("fone", ignoreCase = true) || produto.nome.contains("headphone", ignoreCase = true) -> R.drawable.product_fone
    produto.nome.contains("mochila", ignoreCase = true) -> R.drawable.product_mochila
    produto.nome.contains("relógio", ignoreCase = true) || produto.nome.contains("relogio", ignoreCase = true) -> R.drawable.product_relogio
    else -> null
}

internal fun lerFotoReduzida(caminho: String): ImageBitmap? = try {
    val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(caminho, limites)
    var amostra = 1
    while (limites.outWidth / amostra > 1200 || limites.outHeight / amostra > 1200) amostra *= 2
    BitmapFactory.decodeFile(caminho, BitmapFactory.Options().apply { inSampleSize = amostra })?.asImageBitmap()
} catch (_: Exception) {
    null
}

@Composable
fun FotoProduto(produto: Produto, modifier: Modifier = Modifier) {
    val foto = remember(produto.imagemLocal) { produto.imagemLocal?.let(::lerFotoReduzida) }
    val recurso = imagemDoProduto(produto)
    Box(modifier, contentAlignment = Alignment.Center) {
        when {
            foto != null -> Image(bitmap = foto, contentDescription = produto.nome, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
            recurso != null -> Image(painter = painterResource(recurso), contentDescription = produto.nome, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
            else -> Text(produto.nome.take(1).uppercase(), style = MaterialTheme.typography.headlineMedium, color = Azul)
        }
    }
}