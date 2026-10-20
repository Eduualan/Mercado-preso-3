@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mercadopreso

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlin.math.roundToInt

private fun descontoPercentual(original: Double, atual: Double): Int =
    if (original > atual) ((original - atual) / original * 100).roundToInt() else 0

// ============ TELA 1: LISTA DE PRODUTOS (aba) ============
@Composable
fun TelaProdutos(nav: NavController, loja: LojaState) {
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            BarraTopo("Mercado Preso")
            Text(
                "sua compra não sai em condicional",
                style = MaterialTheme.typography.bodySmall,
                color = Cinza,
                modifier = Modifier.padding(start = 16.dp, top = 10.dp, end = 16.dp)
            )
            if (loja.produtos.isEmpty()) {
                EstadoVazio(
                    "Nenhum produto por aqui",
                    "Toque em \"Novo produto\" para cadastrar o primeiro."
                )
            }
            LazyColumn(
                contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(loja.produtos, key = { it.id }) { produto ->
                    val pct = descontoPercentual(produto.precoOriginal, produto.preco)
                    CartaoPadrao(onClick = { nav.navigate(Rotas.detalheProduto(produto.id)) }) {
                        Row(
                            Modifier.padding(start = 16.dp, top = 14.dp, bottom = 14.dp, end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(produto.nome, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "Vendido por ${produto.vendedor}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Cinza
                                )
                                Text(
                                    loja.categoriaDe(produto.categoriaId)?.nome ?: "Sem categoria",
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
                            IconButton(onClick = { loja.removerProduto(produto) }) {
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
            contentColor = Fundo,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        )
    }
}

// ============ TELA 2: DETALHE DO PRODUTO ============
// Complexidade extra: valores calculados (desconto %, parcela, economia),
// categoria do produto com navegação secundária e edição de preço na própria tela.
@Composable
fun TelaDetalheProduto(nav: NavController, loja: LojaState, id: Int) {
    val ctx = LocalContext.current
    val produto = loja.produtos.firstOrNull { it.id == id }
    var novoPreco by remember(produto?.preco) { mutableStateOf(produto?.preco?.toString() ?: "") }

    Column(Modifier.fillMaxSize()) {
        BarraTopo("Detalhes do produto", onVoltar = { nav.popBackStack() })

        if (produto == null) {
            EstadoVazio("Produto não encontrado", "Ele pode ter sido removido da lista.")
            return@Column
        }

        val categoria = loja.categoriaDe(produto.categoriaId)
        val descontoPct = descontoPercentual(produto.precoOriginal, produto.preco)
        val economia = produto.precoOriginal - produto.preco

        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Sem imagem real: a inicial do produto faz o papel de foto
            Box(
                Modifier.fillMaxWidth().height(170.dp)
                    .background(CardCor, MaterialTheme.shapes.large)
                    .border(1.dp, CardBorda, MaterialTheme.shapes.large),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    produto.nome.take(1).uppercase(),
                    style = MaterialTheme.typography.displaySmall,
                    fontSize = 80.sp,
                    lineHeight = 84.sp,
                    color = Amarelo
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Mais vendido em fugas mal sucedidas",
                    style = MaterialTheme.typography.labelMedium,
                    color = Verde
                )
                Text(produto.nome, style = MaterialTheme.typography.headlineMedium)
            }

            CartaoPadrao {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (descontoPct > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                produto.precoOriginal.reais(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Cinza,
                                textDecoration = TextDecoration.LineThrough
                            )
                            Selo("-$descontoPct% OFF", Verde)
                        }
                    }
                    Text(produto.preco.reais(), style = MaterialTheme.typography.displaySmall)
                    Text(
                        "em até 6x de ${(produto.preco / 6).reais()} sem juros",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Verde
                    )
                    if (economia > 0) {
                        Text(
                            "Você economiza ${economia.reais()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Verde
                        )
                    }
                }
            }

            CartaoPadrao {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Chegando amanhã na cela", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Frete grátis por seu preço plus",
                        style = MaterialTheme.typography.bodySmall,
                        color = Cinza
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                TituloSecao("Descrição")
                Text(produto.descricao, style = MaterialTheme.typography.bodyLarge)
            }

            // Navegação secundária: do produto para a categoria dele
            CartaoPadrao(
                onClick = if (categoria != null) {
                    { nav.navigate(Rotas.detalheCategoria(categoria.id)) }
                } else null
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Categoria", style = MaterialTheme.typography.bodySmall, color = Cinza)
                        Text(
                            categoria?.nome ?: "Sem categoria",
                            style = MaterialTheme.typography.titleMedium,
                            color = Azul
                        )
                    }
                    Text("›", style = MaterialTheme.typography.titleLarge, color = Cinza)
                }
            }

            CartaoPadrao {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Vendedor", style = MaterialTheme.typography.bodySmall, color = Cinza)
                    Text(produto.vendedor, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "98% de reincidência positiva",
                        style = MaterialTheme.typography.bodySmall,
                        color = Cinza
                    )
                }
            }

            // Edição de preço ali mesmo
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TituloSecao("Editar preço")
                OutlinedTextField(
                    value = novoPreco,
                    onValueChange = { novoPreco = it },
                    label = { Text("Novo preço (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick = {
                        val valor = novoPreco.paraDouble()
                        if (valor == null || valor <= 0) {
                            Toast.makeText(ctx, "Preço inválido", Toast.LENGTH_SHORT).show()
                        } else {
                            loja.atualizarPreco(produto.id, valor)
                            Toast.makeText(ctx, "Preço atualizado", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) { Text("Atualizar preço") }
            }

            Button(
                onClick = {
                    loja.adicionarAoCarrinho(produto)
                    nav.irParaAba(Rotas.CARRINHO)
                },
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) { Text("Comprar agora") }

            OutlinedButton(
                onClick = {
                    loja.adicionarAoCarrinho(produto)
                    Toast.makeText(ctx, "Adicionado ao carrinho", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) { Text("Adicionar ao carrinho") }
        }
    }
}

// ============ TELA 3: NOVO PRODUTO (formulário) ============
@Composable
fun TelaNovoProduto(nav: NavController, loja: LojaState) {
    val ctx = LocalContext.current
    var nome by remember { mutableStateOf("") }
    var vendedor by remember { mutableStateOf("") }
    var preco by remember { mutableStateOf("") }
    var precoOriginal by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var categoriaId by remember { mutableStateOf(loja.categorias.firstOrNull()?.id) }

    Column(Modifier.fillMaxSize()) {
        BarraTopo("Novo produto", onVoltar = { nav.popBackStack() })
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TituloSecao("Sobre o produto")
            OutlinedTextField(nome, { nome = it }, label = { Text("Nome do produto") },
                singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(vendedor, { vendedor = it }, label = { Text("Vendedor") },
                singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(descricao, { descricao = it }, label = { Text("Descrição") },
                minLines = 3, modifier = Modifier.fillMaxWidth())

            TituloSecao("Preço", Modifier.padding(top = 8.dp))
            OutlinedTextField(preco, { preco = it }, label = { Text("Preço (R$)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(precoOriginal, { precoOriginal = it }, label = { Text("Preço original (opcional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true, modifier = Modifier.fillMaxWidth())

            TituloSecao("Categoria", Modifier.padding(top = 8.dp))
            if (loja.categorias.isEmpty()) {
                Text(
                    "Cadastre uma categoria na aba Categorias primeiro.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Cinza
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(loja.categorias, key = { it.id }) { c ->
                        FilterChip(
                            selected = categoriaId == c.id,
                            onClick = { categoriaId = c.id },
                            label = { Text(c.nome) }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    val p = preco.paraDouble()
                    val cat = categoriaId
                    if (nome.isBlank() || vendedor.isBlank() || p == null || p <= 0 || cat == null) {
                        Toast.makeText(ctx, "Preencha nome, vendedor, preço e categoria", Toast.LENGTH_SHORT).show()
                    } else {
                        val original = precoOriginal.paraDouble() ?: p
                        loja.adicionarProduto(
                            nome.trim(), vendedor.trim(), maxOf(original, p), p, cat,
                            descricao.ifBlank { "Sem descrição. Compre por sua conta e risco." }
                        )
                        Toast.makeText(ctx, "Produto cadastrado", Toast.LENGTH_SHORT).show()
                        nav.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp).padding(top = 0.dp)
            ) { Text("Cadastrar produto") }
        }
    }
}
