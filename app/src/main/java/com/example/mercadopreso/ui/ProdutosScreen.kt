package com.example.mercadopreso.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mercadopreso.Rotas
import com.example.mercadopreso.viewmodel.CarrinhoViewModel
import com.example.mercadopreso.viewmodel.ProdutoViewModel

@Composable
fun TelaProdutos(nav: NavController, viewModel: ProdutoViewModel, carrinhoViewModel: CarrinhoViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val produtosVisiveis = if (uiState.busca.isBlank()) uiState.produtos else uiState.produtos.filter { produto -> produto.nome.contains(uiState.busca.trim(), ignoreCase = true) || produto.vendedor.contains(uiState.busca.trim(), ignoreCase = true) }
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            BarraTopo("Mercado Preso")
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "banner") {
                    BannerDestaque(
                        titulo = "Compras sem condicional",
                        descricao = "Achados exclusivos para deixar sua cela completa.",
                        metrica = "${uiState.produtos.size} ofertas no catálogo"
                    )
                }
                if (uiState.produtos.isNotEmpty()) {
                    item(key = "destaques") { DestaquesProdutos(uiState.produtos, nav) }
                }
                item(key = "busca") {
                    OutlinedTextField(
                        value = uiState.busca,
                        onValueChange = viewModel::atualizarBusca,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        placeholder = { Text("Buscar produtos") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Azul) },
                        singleLine = true,
                        shape = MaterialTheme.shapes.large
                    )
                }
                if (produtosVisiveis.isEmpty()) {
                    item(key = "estado-vazio") {
                        EstadoVazio(
                            if (uiState.busca.isBlank()) "Nenhum produto por aqui" else "Nada encontrado",
                            if (uiState.busca.isBlank()) "Toque em \"Novo produto\" para cadastrar o primeiro." else "Tente buscar por outro nome ou vendedor."
                        )
                    }
                }
                items(produtosVisiveis, key = { it.id }) { produto ->
                    val pct = descontoPercentual(produto.precoOriginal, produto.preco)
                    CartaoPadrao(onClick = { nav.navigate(Rotas.detalheProduto(produto.id)) }) {
                        Row(
                            Modifier.padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FotoProduto(
                                produto,
                                Modifier.size(84.dp).clip(RoundedCornerShape(12.dp)).background(Color.White)
                            )
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(produto.nome, style = MaterialTheme.typography.titleMedium)
                                Text("Vendido por ${produto.vendedor}", style = MaterialTheme.typography.bodySmall, color = Cinza)
                                Text(
                                    uiState.categorias.firstOrNull { it.id == produto.categoriaId }?.nome ?: "Sem categoria",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Azul
                                )
                                Row(
                                    Modifier.padding(top = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(produto.preco.reais(), style = MaterialTheme.typography.titleLarge)
                                    if (pct > 0) Selo("-$pct%", Verde)
                                }
                            }
                            IconButton(onClick = { viewModel.removerProduto(produto); carrinhoViewModel.removerProduto(produto.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remover produto", tint = Cinza)
                            }
                        }
                    }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = { nav.navigate(Rotas.NOVO_PRODUTO) },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Novo produto", style = MaterialTheme.typography.labelLarge) },
            containerColor = Amarelo,
            contentColor = TextoPrincipal,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        )
    }
}
