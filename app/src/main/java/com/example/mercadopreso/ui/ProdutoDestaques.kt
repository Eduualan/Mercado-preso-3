package com.example.mercadopreso.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mercadopreso.Rotas
import com.example.mercadopreso.data.local.entity.Produto
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun DestaquesProdutos(produtos: List<Produto>, nav: NavController) {
    val destaques = produtos.filter { it.precoOriginal > it.preco }.ifEmpty { produtos }.take(6)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = destaques.size)
    val destaquesEmLoop = remember(destaques.map { it.id }) { List(3) { destaques }.flatten() }
    LaunchedEffect(destaques.map { it.id }) {
        if (destaques.size > 1) {
            while (true) {
                delay(2800)
                if (listState.firstVisibleItemIndex >= destaques.size * 2) {
                    listState.scrollToItem(destaques.size)
                } else {
                    listState.animateScrollToItem(listState.firstVisibleItemIndex + 1)
                }
            }
        }
    }
    Column(Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 8.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Em destaque", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            Text("Deslize para ver mais", style = MaterialTheme.typography.labelSmall, color = Cinza)
        }
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(destaquesEmLoop.size, key = { index -> "destaque-${destaquesEmLoop[index].id}-$index" }) { index ->
                val produto = destaquesEmLoop[index]
                CartaoPadrao(modifier = Modifier.width(274.dp), onClick = { nav.navigate(Rotas.detalheProduto(produto.id)) }) {
                    Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        FotoProduto(
                            produto,
                            Modifier.fillMaxWidth().height(122.dp).clip(RoundedCornerShape(12.dp)).background(Color.White)
                        )
                        Text(produto.nome, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                        Text(produto.preco.reais(), style = MaterialTheme.typography.titleLarge)
                        Text("Oferta especial", style = MaterialTheme.typography.labelMedium, color = Verde)
                    }
                }
            }
        }
    }
}

fun descontoPercentual(original: Double, atual: Double): Int =
    if (original > atual) ((original - atual) / original * 100).roundToInt() else 0

// ============ TELA 1: LISTA DE PRODUTOS (aba) ============