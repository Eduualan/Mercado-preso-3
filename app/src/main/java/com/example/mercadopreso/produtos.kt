
@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mercadopreso

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlin.math.roundToInt

// ============ TELA 1: LISTA DE PRODUTOS (aba) ============
@Composable
fun TelaProdutos(nav: NavController, loja: LojaState) {
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            BarraTopo("Mercado Preso")
            Text(
                "sua compra não sai em condicional",
                color = Cinza, fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            if (loja.produtos.isEmpty()) {
                Text("Nenhum produto. Toque em \"Novo produto\".", Modifier.padding(16.dp), color = Cinza)
            }
            LazyColumn(
                contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(loja.produtos, key = { it.id }) { produto ->
                    Card(
                        onClick = { nav.navigate(Rotas.detalheProduto(produto.id)) },
                        colors = CardDefaults.cardColors(containerColor = CardCor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(produto.nome, fontWeight = FontWeight.Bold)
                                Text("Vendido por ${produto.vendedor}", color = Cinza, fontSize = 12.sp)
                                Text(loja.categoriaDe(produto.categoriaId)?.nome ?: "Sem categoria", color = Azul, fontSize = 12.sp)
                                Text(produto.preco.reais(), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                            IconButton(onClick = { loja.removerProduto(produto) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remover produto")
                            }
                        }
                    }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = { nav.navigate(Rotas.NOVO_PRODUTO) },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Novo produto") },
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
            Text("Produto não encontrado.", Modifier.padding(16.dp), color = Cinza)
            return@Column
        }

        val categoria = loja.categoriaDe(produto.categoriaId)
        val descontoPct = if (produto.precoOriginal > produto.preco)
            ((produto.precoOriginal - produto.preco) / produto.precoOriginal * 100).roundToInt() else 0
        val economia = produto.precoOriginal - produto.preco

        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                Modifier.fillMaxWidth().height(160.dp).background(CardCor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) { Text("[imagem do produto]", color = Cinza) }

            Text("Mais vendido em fugas mal sucedidas", color = Verde, fontSize = 12.sp)
            Text(produto.nome, fontSize = 20.sp, fontWeight = FontWeight.Medium)

            if (descontoPct > 0) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(produto.precoOriginal.reais(), color = Cinza, textDecoration = TextDecoration.LineThrough)
                    Text("-$descontoPct% OFF", color = Verde, fontSize = 12.sp)
                }
            }
            Text(produto.preco.reais(), fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text("em até 6x de ${(produto.preco / 6).reais()} sem juros", color = Verde, fontSize = 13.sp)
            if (economia > 0) Text("Você economiza ${economia.reais()}", color = Verde, fontSize = 13.sp)

            Text("Chegando amanhã na cela", Modifier.padding(top = 8.dp))
            Text("Frete grátis por seu preço plus", color = Cinza, fontSize = 12.sp)
            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            Text(produto.descricao)

            // Navegação secundária: do produto para a categoria dele
            Text(
                "Categoria: ${categoria?.nome ?: "Sem categoria"}  ›",
                color = Azul,
                modifier = Modifier.clickable(enabled = categoria != null) {
                    nav.navigate(Rotas.detalheCategoria(produto.categoriaId))
                }.padding(vertical = 8.dp)
            )
            Text(produto.vendedor, fontWeight = FontWeight.Medium)
            Text("98% de reincidência positiva", color = Cinza, fontSize = 12.sp)

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            // Edição de preço ali mesmo
            OutlinedTextField(
                value = novoPreco,
                onValueChange = { novoPreco = it },
                label = { Text("Editar preço (R$)") },
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
                modifier = Modifier.fillMaxWidth()
            ) { Text("Atualizar preço") }

            Button(
                onClick = {
                    loja.adicionarAoCarrinho(produto)
                    nav.irParaAba(Rotas.CARRINHO)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text("Comprar agora") }

            OutlinedButton(
                onClick = {
                    loja.adicionarAoCarrinho(produto)
                    Toast.makeText(ctx, "Adicionado ao carrinho", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp)
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
            OutlinedTextField(nome, { nome = it }, label = { Text("Nome do produto") },
                singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(vendedor, { vendedor = it }, label = { Text("Vendedor") },
                singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(preco, { preco = it }, label = { Text("Preço (R$)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(precoOriginal, { precoOriginal = it }, label = { Text("Preço original (opcional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(descricao, { descricao = it }, label = { Text("Descrição") },
                minLines = 3, modifier = Modifier.fillMaxWidth())

            Text("Categoria", fontWeight = FontWeight.Medium)
            if (loja.categorias.isEmpty()) {
                Text("Cadastre uma categoria na aba Categorias primeiro.", color = Cinza)
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
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text("Cadastrar produto") }
        }
    }
}
