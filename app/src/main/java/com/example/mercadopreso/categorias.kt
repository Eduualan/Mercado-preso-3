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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(loja.categorias, key = { it.id }) { categoria ->
                Card(
                    onClick = { nav.navigate(Rotas.detalheCategoria(categoria.id)) },
                    colors = CardDefaults.cardColors(containerColor = CardCor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(categoria.nome, fontWeight = FontWeight.Bold)
                            Text(categoria.descricao, color = Cinza, fontSize = 12.sp)
                            Text("${loja.produtosDa(categoria.id).size} produto(s)", color = Azul, fontSize = 12.sp)
                        }
                        IconButton(onClick = {
                            if (!loja.removerCategoria(categoria)) {
                                Toast.makeText(ctx, "Remova antes os produtos dessa categoria", Toast.LENGTH_SHORT).show()
                            }
                        }) { Icon(Icons.Default.Delete, contentDescription = "Remover categoria") }
                    }
                }
            }
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
            Text("Categoria não encontrada.", Modifier.padding(16.dp), color = Cinza)
            return@Column
        }

        val produtos = loja.produtosDa(categoria.id)
        val valorTotal = produtos.sumOf { it.preco }
        val media = if (produtos.isEmpty()) 0.0 else valorTotal / produtos.size

        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(categoria.nome, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(categoria.descricao, color = Cinza)
            Text("Produtos: ${produtos.size}", Modifier.padding(top = 8.dp))
            Text("Valor somado do catálogo: ${valorTotal.reais()}")
            Text("Preço médio: ${media.reais()}")
            Text("Produtos desta categoria", fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 12.dp))
        }

        if (produtos.isEmpty()) {
            Text("Nenhum produto aqui ainda.", Modifier.padding(horizontal = 16.dp), color = Cinza)
        }
        LazyColumn(
            contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(produtos, key = { it.id }) { produto ->
                Card(
                    onClick = { nav.navigate(Rotas.detalheProduto(produto.id)) },
                    colors = CardDefaults.cardColors(containerColor = CardCor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(produto.nome, Modifier.weight(1f))
                        Text(produto.preco.reais(), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
