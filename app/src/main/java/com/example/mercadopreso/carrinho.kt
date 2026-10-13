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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()
                .background(CardCor, RoundedCornerShape(12.dp)).padding(12.dp)
        )

        if (loja.carrinho.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Seu carrinho está vazio... como a sua cela.", color = Cinza)
                    TextButton(onClick = { nav.irParaAba(Rotas.PRODUTOS) }) { Text("Ver produtos") }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(loja.carrinho, key = { it.produto.id }) { item ->
                    Row(verticalAlignment = Alignment.Top) {
                        Checkbox(
                            checked = item.marcado,
                            onCheckedChange = { loja.alternarMarcado(item.produto.id) }
                        )
                        Column(
                            Modifier.weight(1f).clickable { nav.navigate(Rotas.detalheProduto(item.produto.id)) }
                        ) {
                            Text(item.produto.nome)
                            Text("Vendido por ${item.produto.vendedor}", color = Cinza, fontSize = 12.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { loja.alterarQuantidade(item.produto.id, -1) }) {
                                    Text("−", fontSize = 20.sp)
                                }
                                Text("Qtd: ${item.quantidade}", fontSize = 12.sp)
                                IconButton(onClick = { loja.alterarQuantidade(item.produto.id, +1) }) {
                                    Text("+", fontSize = 20.sp)
                                }
                            }
                        }
                        Text((item.produto.preco * item.quantidade).reais(), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Resumo da compra
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                Modifier.fillMaxWidth().clickable { loja.cupomAplicado = !loja.cupomAplicado },
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    if (loja.cupomAplicado) "Cupom PRIMEIRAFUGA aplicado (toque para remover)"
                    else "Aplicar cupom PRIMEIRAFUGA",
                    fontSize = 13.sp
                )
                Text(if (loja.desconto > 0) "-${loja.desconto.reais()}" else "", color = Verde, fontSize = 13.sp)
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
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text("Finalizar prisão da compra") }
            Text("Compra 100% segura. Sem fiança de reembolso.", color = Cinza, fontSize = 11.sp)
        }
    }
}

@Composable
private fun EtapaPedido(cor: Color, titulo: String, detalhe: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).background(cor, CircleShape))
        Column(Modifier.padding(start = 12.dp)) {
            Text(titulo, fontSize = 14.sp)
            Text(detalhe, color = Cinza, fontSize = 11.sp)
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(Modifier.size(64.dp).background(Verde, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Text("Prisão da compra concluída", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(
                "Pedido ${loja.ultimoPedido} · Sem direito a fiança de arrependimento",
                color = Cinza, fontSize = 12.sp, textAlign = TextAlign.Center
            )

            Column(
                Modifier.fillMaxWidth().background(CardCor, RoundedCornerShape(12.dp)).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Previsão de chegada: amanhã, até 18h", fontWeight = FontWeight.Bold)
                EtapaPedido(Verde, "Pedido algemado no depósito", "Hoje, 9:12")
                EtapaPedido(Azul, "Em transferência sob escolta", "Hoje, 14:47")
                EtapaPedido(Cinza, "Entregue na sua cela", "Previsto para amanhã")
            }

            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Total pago", color = Cinza, fontSize = 12.sp)
                    Text(loja.ultimoTotal.reais(), fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Forma de pagamento", color = Cinza, fontSize = 12.sp)
                    Text("Pix da cadeia", fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = { nav.navigate(Rotas.ACOMPANHAR) },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text("Acompanhar prisão do pedido") }

            OutlinedButton(
                onClick = { nav.voltarAoInicio() },
                modifier = Modifier.fillMaxWidth().height(52.dp)
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
            Spacer(Modifier.height(8.dp))
            if (etapaAtual >= etapas.lastIndex) {
                Text("Entregue! Lembrando: sem direito a devolução.", color = Verde)
            }
            Button(
                onClick = { if (etapaAtual < etapas.lastIndex) etapaAtual++ },
                enabled = etapaAtual < etapas.lastIndex,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text("Simular próxima etapa") }
            OutlinedButton(
                onClick = { nav.voltarAoInicio() },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text("Voltar ao início") }
        }
    }
}
