@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mercadopreso

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

// ============ TELA 4: LISTA DE CATEGORIAS (aba) ============
@Composable
fun TelaCategorias(nav: NavController, loja: LojaState) {
    val ctx = LocalContext.current
    var nome by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize()) {
        BarraTopo("Categorias")

        // Formulário para ADICIONAR
        Column(
            Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TituloSecao("Nova categoria")
            OutlinedTextField(nome, { nome = it }, label = { Text("Nome da categoria") },
                singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(descricao, { descricao = it }, label = { Text("Descrição") },
                singleLine = true, modifier = Modifier.fillMaxWidth())
            Button(
                onClick = {
                    if (nome.isBlank()) {
                        Toast.makeText(ctx, "Dê um nome para a categoria", Toast.LENGTH_SHORT).show()
                    } else {
                        loja.adicionarCategoria(nome.trim(), descricao.ifBlank { "Sem descrição" }.trim())
                        nome = ""
                        descricao = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Adicionar categoria") }
        }

        if (loja.categorias.isEmpty()) {
            EstadoVazio("Nenhuma categoria ainda", "Cadastre a primeira para organizar os produtos.")
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(loja.categorias, key = { it.id }) { categoria ->
                CartaoPadrao(onClick = { nav.navigate(Rotas.detalheCategoria(categoria.id)) }) {
                    Row(
                        Modifier.padding(start = 16.dp, top = 14.dp, bottom = 14.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(categoria.nome, style = MaterialTheme.typography.titleMedium)
                            Text(
                                categoria.descricao,
                                style = MaterialTheme.typography.bodySmall,
                                color = Cinza
                            )
                            Selo("${loja.produtosDa(categoria.id).size} produto(s)", Azul)
                        }
                        IconButton(onClick = {
                            if (!loja.removerCategoria(categoria)) {
                                Toast.makeText(ctx, "Remova antes os produtos dessa categoria", Toast.LENGTH_SHORT).show()
                            }
                        }) { Icon(Icons.Default.Delete, contentDescription = "Remover categoria", tint = Cinza) }
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

// ============ TELA 5: DETALHE DA CATEGORIA ============
// Combina dados das duas listas: mostra os produtos da categoria,
// quantidade, valor total e preço médio (calculados).
@Composable
fun TelaDetalheCategoria(nav: NavController, loja: LojaState, id: Int) {
    val categoria = loja.categoriaDe(id)

    Column(Modifier.fillMaxSize()) {
        BarraTopo("Detalhes da categoria", onVoltar = { nav.popBackStack() })

        if (categoria == null) {
            EstadoVazio("Categoria não encontrada", "Ela pode ter sido removida.")
            return@Column
        }

        val produtos = loja.produtosDa(categoria.id)
        val valorTotal = produtos.sumOf { it.preco }
        val media = if (produtos.isEmpty()) 0.0 else valorTotal / produtos.size

        Column(
            Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(categoria.nome, style = MaterialTheme.typography.headlineMedium)
                Text(categoria.descricao, style = MaterialTheme.typography.bodyMedium, color = Cinza)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Estatistica("Produtos", "${produtos.size}", Modifier.weight(1f))
                Estatistica("Preço médio", media.reais(), Modifier.weight(1f))
            }
            Estatistica("Valor somado do catálogo", valorTotal.reais())
            TituloSecao("Produtos desta categoria", Modifier.padding(top = 4.dp))
        }

        if (produtos.isEmpty()) {
            Text(
                "Nenhum produto aqui ainda.",
                style = MaterialTheme.typography.bodyMedium,
                color = Cinza,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
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
