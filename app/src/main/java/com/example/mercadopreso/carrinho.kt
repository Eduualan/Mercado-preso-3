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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

private fun NavController.voltarAoInicio() {
    navigate(Rotas.PRODUTOS) {
        popUpTo(Rotas.PRODUTOS) { inclusive = false }
        launchSingleTop = true
    }
}

// ============ TELA 6: CARRINHO (aba) ============
@Composable
fun TelaCarrinho(nav: NavController, loja: LojaState) {
    val ctx = LocalContext.current

    Column(Modifier.fillMaxSize()) {
        BarraTopo("Meu carrinho", acoes = {
            IconButton(onClick = {
                val n = loja.removerMarcados()
                val msg = if (n == 0) "Marque itens para remover" else "$n item(ns) removido(s)"
                Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
            }) { Icon(Icons.Default.Delete, contentDescription = "Remover marcados") }
        })

        Text(
            "mercado preso — sua compra não sai em condicional",
            style = MaterialTheme.typography.bodySmall,
            color = Amarelo,
            modifier = Modifier
                .padding(start = 16.dp, top = 12.dp, end = 16.dp)
                .fillMaxWidth()
                .background(Amarelo.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                .padding(12.dp)
        )

        if (loja.carrinho.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    EstadoVazio("Seu carrinho está vazio", "Como a sua cela. Que tal ver os produtos?")
                    TextButton(onClick = { nav.irParaAba(Rotas.PRODUTOS) }) { Text("Ver produtos") }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(loja.carrinho, key = { it.produto.id }) { item ->
                    CartaoPadrao {
                        Row(
                            Modifier.padding(start = 4.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Checkbox(
                                checked = item.marcado,
                                onCheckedChange = { loja.alternarMarcado(item.produto.id) }
                            )
                            Column(
                                Modifier.weight(1f).padding(top = 12.dp)
                                    .clickable { nav.navigate(Rotas.detalheProduto(item.produto.id)) }
                            ) {
                                Text(item.produto.nome, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "Vendido por ${item.produto.vendedor}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Cinza
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { loja.alterarQuantidade(item.produto.id, -1) }) {
                                        Text("−", style = MaterialTheme.typography.titleLarge, color = Amarelo)
                                    }
                                    Text("${item.quantidade}", style = MaterialTheme.typography.titleMedium)
                                    IconButton(onClick = { loja.alterarQuantidade(item.produto.id, +1) }) {
                                        Text("+", style = MaterialTheme.typography.titleLarge, color = Amarelo)
                                    }
                                }
                            }
                            Text(
                                (item.produto.preco * item.quantidade).reais(),
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }
                    }
                }
            }
        }

        // Resumo da compra
        CartaoPadrao(Modifier.padding(16.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    Modifier.fillMaxWidth().clickable { loja.cupomAplicado = !loja.cupomAplicado },
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        if (loja.cupomAplicado) "Cupom PRIMEIRAFUGA aplicado (toque para remover)"
                        else "Aplicar cupom PRIMEIRAFUGA",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (loja.cupomAplicado) Verde else Amarelo,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        if (loja.desconto > 0) "-${loja.desconto.reais()}" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = Verde
                    )
                }
                HorizontalDivider()
                LinhaValor("Subtotal (${loja.quantidadeTotal} itens)", loja.subtotal.reais())
                LinhaValor("Frete até a cela", "Grátis", Verde)
                LinhaValor("Total", loja.total.reais(), negrito = true)
                Button(
                    onClick = {
                        if (loja.carrinho.isEmpty()) {
                            Toast.makeText(ctx, "Carrinho vazio", Toast.LENGTH_SHORT).show()
                        } else {
                            loja.finalizarCompra()
                            nav.navigate(Rotas.CONFIRMADO)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp)
                ) { Text("Finalizar prisão da compra") }
                Text(
                    "Compra 100% segura. Sem fiança de reembolso.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Cinza,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun EtapaPedido(cor: Color, titulo: String, detalhe: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(12.dp).background(cor, CircleShape))
        Column(Modifier.padding(start = 14.dp)) {
            Text(titulo, style = MaterialTheme.typography.bodyLarge)
            Text(detalhe, style = MaterialTheme.typography.bodySmall, color = Cinza)
        }
    }
}

// ============ TELA 7: PEDIDO CONFIRMADO ============
@Composable
fun TelaPedidoConfirmado(nav: NavController, loja: LojaState) {
    Column(Modifier.fillMaxSize()) {
        BarraTopo("Pedido confirmado")
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(Modifier.size(72.dp).background(Verde, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Fundo, modifier = Modifier.size(38.dp))
            }
            Text(
                "Prisão da compra concluída",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Text(
                "Pedido ${loja.ultimoPedido}. Sem direito a fiança de arrependimento.",
                style = MaterialTheme.typography.bodySmall,
                color = Cinza,
                textAlign = TextAlign.Center
            )

            CartaoPadrao {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Previsão de chegada: amanhã, até 18h", style = MaterialTheme.typography.titleMedium)
                    EtapaPedido(Verde, "Pedido algemado no depósito", "Hoje, 9:12")
                    EtapaPedido(Azul, "Em transferência sob escolta", "Hoje, 14:47")
                    EtapaPedido(Cinza, "Entregue na sua cela", "Previsto para amanhã")
                }
            }

            CartaoPadrao {
                Row(
                    Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Total pago", style = MaterialTheme.typography.bodySmall, color = Cinza)
                        Text(loja.ultimoTotal.reais(), style = MaterialTheme.typography.titleLarge)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Forma de pagamento", style = MaterialTheme.typography.bodySmall, color = Cinza)
                        Text("Pix da cadeia", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }

            Button(
                onClick = { nav.navigate(Rotas.ACOMPANHAR) },
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) { Text("Acompanhar prisão do pedido") }

            OutlinedButton(
                onClick = { nav.voltarAoInicio() },
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) { Text("Voltar ao início") }
        }
    }
}

// ============ TELA 8: ACOMPANHAR PEDIDO ============
@Composable
fun TelaAcompanharPedido(nav: NavController) {
    val etapas = listOf(
        "Pedido algemado no depósito",
        "Em transferência sob escolta",
        "Saiu para entrega na ala B",
        "Entregue na sua cela"
    )
    var etapaAtual by remember { mutableIntStateOf(1) }

    Column(Modifier.fillMaxSize()) {
        BarraTopo("Acompanhar pedido", onVoltar = { nav.popBackStack() })
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            CartaoPadrao {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    etapas.forEachIndexed { i, titulo ->
                        val cor = when {
                            i < etapaAtual -> Verde
                            i == etapaAtual -> Azul
                            else -> Cinza
                        }
                        EtapaPedido(cor, titulo, when {
                            i < etapaAtual -> "Concluída"
                            i == etapaAtual -> "Em andamento"
                            else -> "Aguardando"
                        })
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            if (etapaAtual >= etapas.lastIndex) {
                Text(
                    "Entregue! Lembrando: sem direito a devolução.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Verde
                )
            }
            Button(
                onClick = { if (etapaAtual < etapas.lastIndex) etapaAtual++ },
                enabled = etapaAtual < etapas.lastIndex,
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) { Text("Simular próxima etapa") }
            OutlinedButton(
                onClick = { nav.voltarAoInicio() },
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) { Text("Voltar ao início") }
        }
    }
}
