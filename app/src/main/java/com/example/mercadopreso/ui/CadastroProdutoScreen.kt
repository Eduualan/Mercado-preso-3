package com.example.mercadopreso.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mercadopreso.viewmodel.ProdutoViewModel

@Composable
// Formulário de cadastro e seleção de imagem do produto.
fun TelaNovoProduto(nav: NavController, viewModel: ProdutoViewModel) {
    val ctx = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val seletorDeFoto = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let(viewModel::selecionarFoto)
    }
    LaunchedEffect(uiState.mensagem) {
        uiState.mensagem?.let { mensagem ->
            Toast.makeText(ctx, mensagem, Toast.LENGTH_SHORT).show()
            viewModel.limparMensagem()
            if (mensagem == "Produto cadastrado") nav.popBackStack()
        }
    }

    Column(Modifier.fillMaxSize()) {
        BarraTopo("Novo produto", onVoltar = { nav.popBackStack() })
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TituloSecao("Foto do produto")
            val fotoEscolhida = remember(uiState.imagemLocal) { uiState.imagemLocal?.let(::lerFotoReduzida) }
            Box(
                Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(18.dp))
                    .background(Color.White).border(1.dp, CardBorda, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (fotoEscolhida != null) {
                    Image(bitmap = fotoEscolhida, contentDescription = "Foto escolhida para ${uiState.nome}",
                        modifier = Modifier.fillMaxSize().padding(10.dp), contentScale = ContentScale.Fit)
                } else {
                    Text("Adicione uma foto para destacar seu produto", color = Cinza,
                        style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(20.dp))
                }
            }
            OutlinedButton(onClick = { seletorDeFoto.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                Text(if (uiState.imagemLocal == null) "Escolher foto da galeria" else "Trocar foto")
            }

            TituloSecao("Sobre o produto")
            OutlinedTextField(uiState.nome, viewModel::atualizarNome, label = { Text("Nome do produto") },
                singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(uiState.vendedor, viewModel::atualizarVendedor, label = { Text("Vendedor") },
                singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(uiState.descricao, viewModel::atualizarDescricao, label = { Text("Descrição") },
                minLines = 3, modifier = Modifier.fillMaxWidth())

            TituloSecao("Preço", Modifier.padding(top = 8.dp))
            OutlinedTextField(uiState.preco, viewModel::atualizarPreco, label = { Text("Preço (R$)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(uiState.precoOriginal, viewModel::atualizarPrecoOriginal, label = { Text("Preço original (opcional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true, modifier = Modifier.fillMaxWidth())

            TituloSecao("Categoria", Modifier.padding(top = 8.dp))
            if (uiState.categorias.isEmpty()) {
                Text(
                    "Cadastre uma categoria na aba Categorias primeiro.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Cinza
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.categorias, key = { it.id }) { c ->
                        FilterChip(
                            selected = uiState.categoriaId == c.id,
                            onClick = { viewModel.selecionarCategoria(c.id) },
                            label = { Text(c.nome) }
                        )
                    }
                }
            }

            Button(
                onClick = { viewModel.cadastrarProduto() },
                modifier = Modifier.fillMaxWidth().height(54.dp).padding(top = 0.dp)
            ) { Text("Cadastrar produto") }
        }
    }
}