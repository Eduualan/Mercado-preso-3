package com.example.mercadopreso

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun TelaPerfil(nav: NavController, loja: LojaState) {
    val contexto = LocalContext.current
    var nome by rememberSaveable { mutableStateOf("Visitante") }
    var apelido by rememberSaveable { mutableStateOf("Recruta") }
    var editando by rememberSaveable { mutableStateOf(false) }
    var avisosAtivos by rememberSaveable { mutableStateOf(true) }

    Column(Modifier.fillMaxSize()) {
        BarraTopo("Meu perfil")
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CartaoPadrao {
                Row(
                    Modifier.fillMaxWidth().background(Amarelo).padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        Modifier.size(72.dp).background(Azul, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            nome.trim().firstOrNull()?.uppercase() ?: "V",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("CREDENCIAL DO MERCADO", style = MaterialTheme.typography.labelSmall, color = TextoPrincipal)
                        Text(nome.ifBlank { "Visitante" }, style = MaterialTheme.typography.headlineMedium, color = TextoPrincipal)
                        Text("@$apelido • ala de ofertas", style = MaterialTheme.typography.bodyMedium, color = TextoPrincipal)
                    }
                }
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Status da conta", style = MaterialTheme.typography.bodySmall, color = Cinza)
                        Text("Confinamento Premium", style = MaterialTheme.typography.titleMedium, color = Azul)
                    }
                    Selo("ATIVO", Verde)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CartaoPadrao(Modifier.weight(1f)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("${loja.carrinho.size}", style = MaterialTheme.typography.headlineMedium, color = Azul)
                        Text("itens no carrinho", style = MaterialTheme.typography.bodySmall, color = Cinza)
                    }
                }
                CartaoPadrao(Modifier.weight(1f)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("${loja.produtos.size}", style = MaterialTheme.typography.headlineMedium, color = Azul)
                        Text("ofertas disponíveis", style = MaterialTheme.typography.bodySmall, color = Cinza)
                    }
                }
            }

            CartaoPadrao {
                Column(
                    Modifier.fillMaxWidth().padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Dados do perfil", style = MaterialTheme.typography.titleLarge)
                        if (!editando) {
                            OutlinedButton(onClick = { editando = true }) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Editar", modifier = Modifier.padding(start = 6.dp))
                            }
                        }
                    }
                    HorizontalDivider(color = CardBorda)
                    if (editando) {
                        OutlinedTextField(
                            value = nome,
                            onValueChange = { nome = it },
                            label = { Text("Nome") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = apelido,
                            onValueChange = { apelido = it.filter { char -> char.isLetterOrDigit() || char == '_' } },
                            label = { Text("Apelido") },
                            prefix = { Text("@") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                if (nome.isBlank() || apelido.isBlank()) {
                                    Toast.makeText(contexto, "Preencha nome e apelido", Toast.LENGTH_SHORT).show()
                                } else {
                                    nome = nome.trim()
                                    apelido = apelido.trim()
                                    editando = false
                                    Toast.makeText(contexto, "Perfil atualizado", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Salvar perfil") }
                    } else {
                        LinhaPerfil("Nome", nome)
                        LinhaPerfil("Apelido", "@$apelido")
                        LinhaPerfil("Endereço de entrega", "Bloco B • Ala de encomendas")
                        LinhaPerfil("Identificação", "MP-2026-042")
                    }
                }
            }

            CartaoPadrao {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("Avisos de ofertas", style = MaterialTheme.typography.titleMedium)
                        Text("Novidades e entregas sob escolta", style = MaterialTheme.typography.bodySmall, color = Cinza)
                    }
                    Switch(checked = avisosAtivos, onCheckedChange = { avisosAtivos = it })
                }
            }

            CartaoPadrao {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Central da portaria", style = MaterialTheme.typography.titleMedium)
                    Text("Precisa de ajuda? Nossa equipe está de plantão — sem direito a fuga do atendimento.", style = MaterialTheme.typography.bodyMedium, color = Cinza)
                }
            }
            OutlinedButton(
                onClick = { nav.irParaAba(Rotas.CARRINHO) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Abrir meu carrinho") }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun LinhaPerfil(rotulo: String, valor: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(rotulo, style = MaterialTheme.typography.bodySmall, color = Cinza)
        Text(valor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}


