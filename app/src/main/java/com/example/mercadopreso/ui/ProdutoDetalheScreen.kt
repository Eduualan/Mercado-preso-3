package com.example.mercadopreso.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mercadopreso.Rotas
import com.example.mercadopreso.viewmodel.CarrinhoViewModel
import com.example.mercadopreso.viewmodel.ProdutoViewModel

@Composable
// Tela de informações, preço, navegação para categoria e ações de compra.
fun TelaDetalheProduto(nav: NavController, viewModel: ProdutoViewModel, carrinhoViewModel: CarrinhoViewModel, id: Int) {
    val ctx = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(id) { viewModel.carregarProduto(id) }
    LaunchedEffect(uiState.mensagem) { uiState.mensagem?.let { Toast.makeText(ctx, it, Toast.LENGTH_SHORT).show(); viewModel.limparMensagem() } }

    Column(Modifier.fillMaxSize()) {
        BarraTopo("Detalhes do produto", onVoltar = { nav.popBackStack() })

        val produtoAtual = uiState.produtoSelecionado
        if (produtoAtual == null) {
            EstadoVazio("Produto não encontrado", "Ele pode ter sido removido da lista.")
            return@Column
        }

        val categoriaAtual = uiState.categorias.firstOrNull { it.id == produtoAtual.categoriaId }
        val descontoPct = descontoPercentual(produtoAtual.precoOriginal, produtoAtual.preco)
        val economia = produtoAtual.precoOriginal - produtoAtual.preco
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                Modifier.fillMaxWidth().height(190.dp)
                    .background(Color.White, MaterialTheme.shapes.large)
                    .border(1.dp, CardBorda, MaterialTheme.shapes.large),
                contentAlignment = Alignment.Center
            ) {
                FotoProduto(produtoAtual, Modifier.fillMaxSize().padding(8.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Uma boa escolha para sua rotina",
                    style = MaterialTheme.typography.labelMedium,
                    color = Verde
                )
                Text(produtoAtual.nome, style = MaterialTheme.typography.headlineMedium)
            }

            CartaoPadrao {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (descontoPct > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                produtoAtual.precoOriginal.reais(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Cinza,
                                textDecoration = TextDecoration.LineThrough
                            )
                            Selo("-$descontoPct% OFF", Verde)
                        }
                    }
                    Text(produtoAtual.preco.reais(), style = MaterialTheme.typography.displaySmall)
                    Text(
                        "em até 6x de ${(produtoAtual.preco / 6).reais()} sem juros",
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
                    Text("Entrega rápida para você", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Frete grátis em ofertas selecionadas",
                        style = MaterialTheme.typography.bodySmall,
                        color = Cinza
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                TituloSecao("Descrição")
                Text(produtoAtual.descricao, style = MaterialTheme.typography.bodyLarge)
            }

            // Navegação secundária: do produto para a categoria dele
            CartaoPadrao(
                onClick = if (categoriaAtual != null) {
                    { nav.navigate(Rotas.detalheCategoria(categoriaAtual.id)) }
                } else null
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Categoria", style = MaterialTheme.typography.bodySmall, color = Cinza)
                        Text(
                            categoriaAtual?.nome ?: "Sem categoria",
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
                    Text(produtoAtual.vendedor, style = MaterialTheme.typography.titleMedium)
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
                    value = uiState.precoEdicao,
                    onValueChange = viewModel::atualizarPrecoEdicao,
                    label = { Text("Novo preço (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick = {
                        val valor = uiState.precoEdicao.paraDouble()
                        if (valor == null || valor <= 0) {
                            Toast.makeText(ctx, "Preço inválido", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.atualizarPreco(produtoAtual.id, valor)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) { Text("Atualizar preço") }
            }

            Button(
                onClick = {
                    carrinhoViewModel.adicionarAoCarrinho(produtoAtual)
                    nav.irParaAba(Rotas.CARRINHO)
                },
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) { Text("Comprar agora") }

            OutlinedButton(
                onClick = {
                    carrinhoViewModel.adicionarAoCarrinho(produtoAtual)
                    Toast.makeText(ctx, "Adicionado ao carrinho", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) { Text("Adicionar ao carrinho") }
        }
    }
}
