@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mercadopreso.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mercadopreso.Rotas
import com.example.mercadopreso.viewmodel.CategoriaViewModel

@Composable
fun TelaCategorias(nav: NavController, viewModel: CategoriaViewModel) {
    val state by viewModel.uiState.collectAsState()
    val contexto = LocalContext.current
    LaunchedEffect(state.mensagem) {
        state.mensagem?.let { Toast.makeText(contexto, it, Toast.LENGTH_SHORT).show(); viewModel.limparMensagem() }
    }
    Column(Modifier.fillMaxSize()) {
        BarraTopo("Categorias")
        Column(Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TituloSecao("Nova categoria")
            OutlinedTextField(state.nome, viewModel::atualizarNome, label = { Text("Nome da categoria") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(state.descricao, viewModel::atualizarDescricao, label = { Text("Descrição") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Button(onClick = viewModel::adicionarCategoria, modifier = Modifier.fillMaxWidth()) { Text("Adicionar categoria") }
        }
        if (!state.carregando && state.categorias.isEmpty()) {
            EstadoVazio("Nenhuma categoria ainda", "Cadastre a primeira para organizar os produtos.")
        }
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(state.categorias, key = { it.id }) { categoria ->
                CartaoPadrao(onClick = { nav.navigate(Rotas.detalheCategoria(categoria.id)) }) {
                    Row(Modifier.padding(start = 16.dp, top = 14.dp, bottom = 14.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(categoria.nome, style = MaterialTheme.typography.titleMedium)
                            Text(categoria.descricao, style = MaterialTheme.typography.bodySmall, color = Cinza)
                            Selo("${state.produtos.count { it.categoriaId == categoria.id }} produto(s)", Azul)
                        }
                        IconButton(onClick = { viewModel.removerCategoria(categoria) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remover categoria", tint = Cinza)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Estatistica(rotulo: String, valor: String, modifier: Modifier = Modifier) {
    CartaoPadrao(modifier) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(valor, style = MaterialTheme.typography.titleLarge, color = Amarelo)
            Text(rotulo, style = MaterialTheme.typography.bodySmall, color = Cinza)
        }
    }
}

@Composable
fun TelaDetalheCategoria(nav: NavController, viewModel: CategoriaViewModel, id: Int) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(id) { viewModel.carregarCategoria(id) }
    val categoria = state.categoriaSelecionada
    Column(Modifier.fillMaxSize()) {
        BarraTopo("Detalhes da categoria", onVoltar = { nav.popBackStack() })
        if (categoria == null) {
            EstadoVazio("Categoria não encontrada", "Ela pode ter sido removida.")
            return@Column
        }
        val produtos = state.produtosDaCategoria
        val total = produtos.sumOf { it.preco }
        val media = if (produtos.isEmpty()) 0.0 else total / produtos.size
        Column(Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(categoria.nome, style = MaterialTheme.typography.headlineMedium)
                Text(categoria.descricao, style = MaterialTheme.typography.bodyMedium, color = Cinza)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Estatistica("Produtos", "${produtos.size}", Modifier.weight(1f))
                Estatistica("Preço médio", media.reais(), Modifier.weight(1f))
            }
            Estatistica("Valor somado do catálogo", total.reais())
            TituloSecao("Produtos desta categoria", Modifier.padding(top = 4.dp))
        }
        if (produtos.isEmpty()) Text("Nenhum produto aqui ainda.", style = MaterialTheme.typography.bodyMedium, color = Cinza, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(produtos, key = { it.id }) { produto ->
                CartaoPadrao(onClick = { nav.navigate(Rotas.detalheProduto(produto.id)) }) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(produto.nome, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                        Text(produto.preco.reais(), style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
}
