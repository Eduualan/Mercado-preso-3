@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mercadopreso

import android.widget.Toast
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlin.math.roundToInt

private fun imagemDoProduto(produto: Produto): Int? = when {
    produto.nome.contains("algema", ignoreCase = true) -> R.drawable.product_algemas
    produto.nome.contains("colher", ignoreCase = true) -> R.drawable.product_colher
    produto.nome.contains("smartphone", ignoreCase = true) || produto.nome.contains("celular", ignoreCase = true) -> R.drawable.product_celular
    produto.nome.contains("tênis", ignoreCase = true) || produto.nome.contains("tenis", ignoreCase = true) -> R.drawable.product_tenis
    produto.nome.contains("caneca", ignoreCase = true) -> R.drawable.product_caneca
    produto.nome.contains("caderno", ignoreCase = true) -> R.drawable.product_caderno
    produto.nome.contains("bola", ignoreCase = true) -> R.drawable.product_bola
    produto.nome.contains("fone", ignoreCase = true) || produto.nome.contains("headphone", ignoreCase = true) -> R.drawable.product_fone
    produto.nome.contains("mochila", ignoreCase = true) -> R.drawable.product_mochila
    produto.nome.contains("relógio", ignoreCase = true) || produto.nome.contains("relogio", ignoreCase = true) -> R.drawable.product_relogio
    else -> null
}

private fun salvarFotoProduto(context: Context, uri: Uri): String? {
    val tipo = context.contentResolver.getType(uri)
    val extensao = when (tipo) {
        "image/png" -> "png"
        "image/webp" -> "webp"
        else -> "jpg"
    }
    val pasta = File(context.filesDir, "fotos_produtos").apply { mkdirs() }
    val destino = File(pasta, "produto-${UUID.randomUUID()}.$extensao")
    val entrada = context.contentResolver.openInputStream(uri) ?: return null
    entrada.use { input -> destino.outputStream().use { output -> input.copyTo(output) } }
    return destino.absolutePath
}

private fun lerFotoReduzida(caminho: String): ImageBitmap? = try {
    val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(caminho, limites)
    var amostra = 1
    while (limites.outWidth / amostra > 1200 || limites.outHeight / amostra > 1200) amostra *= 2
    BitmapFactory.decodeFile(caminho, BitmapFactory.Options().apply { inSampleSize = amostra })?.asImageBitmap()
} catch (_: Exception) {
    null
}

@Composable
private fun FotoProduto(produto: Produto, modifier: Modifier = Modifier) {
    val foto = remember(produto.imagemLocal) { produto.imagemLocal?.let(::lerFotoReduzida) }
    val recurso = imagemDoProduto(produto)
    Box(modifier, contentAlignment = Alignment.Center) {
        when {
            foto != null -> Image(bitmap = foto, contentDescription = produto.nome, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
            recurso != null -> Image(painter = painterResource(recurso), contentDescription = produto.nome, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
            else -> Text(produto.nome.take(1).uppercase(), style = MaterialTheme.typography.headlineMedium, color = Azul)
        }
    }
}

@Composable
private fun DestaquesProdutos(produtos: List<Produto>, nav: NavController) {
    val destaques = produtos.filter { it.precoOriginal > it.preco }.ifEmpty { produtos }.take(6)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = destaques.size)
    val destaquesEmLoop = remember(destaques.map { it.id }) { List(3) { destaques }.flatten() }
    LaunchedEffect(destaques.map { it.id }) {
        if (destaques.size > 1) {
            while (true) {
                delay(2800)
                if (listState.firstVisibleItemIndex >= destaques.size * 2) {
                    listState.scrollToItem(destaques.size)
                } else {
                    listState.animateScrollToItem(listState.firstVisibleItemIndex + 1)
                }
            }
        }
    }
    Column(Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 8.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Em destaque", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            Text("Deslize para ver mais", style = MaterialTheme.typography.labelSmall, color = Cinza)
        }
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(destaquesEmLoop.size, key = { index -> "destaque-${destaquesEmLoop[index].id}-$index" }) { index ->
                val produto = destaquesEmLoop[index]
                CartaoPadrao(modifier = Modifier.width(274.dp), onClick = { nav.navigate(Rotas.detalheProduto(produto.id)) }) {
                    Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        FotoProduto(
                            produto,
                            Modifier.fillMaxWidth().height(122.dp).clip(RoundedCornerShape(12.dp)).background(Color.White)
                        )
                        Text(produto.nome, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                        Text(produto.preco.reais(), style = MaterialTheme.typography.titleLarge)
                        Text("Oferta especial", style = MaterialTheme.typography.labelMedium, color = Verde)
                    }
                }
            }
        }
    }
}

private fun descontoPercentual(original: Double, atual: Double): Int =
    if (original > atual) ((original - atual) / original * 100).roundToInt() else 0

// ============ TELA 1: LISTA DE PRODUTOS (aba) ============
@Composable
fun TelaProdutos(nav: NavController, loja: LojaState) {
    LaunchedEffect(Unit) { loja.carregarProdutos(); loja.carregarCategorias() }
    var buscaProdutos by remember { mutableStateOf("") }
    val produtosVisiveis = if (buscaProdutos.isBlank()) loja.produtos else loja.produtos.filter { produto -> produto.nome.contains(buscaProdutos.trim(), ignoreCase = true) || produto.vendedor.contains(buscaProdutos.trim(), ignoreCase = true) }
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            BarraTopo("Mercado Preso")
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "banner") {
                    BannerDestaque(
                        titulo = "Compras sem condicional",
                        descricao = "Achados exclusivos para deixar sua cela completa.",
                        metrica = "${loja.produtos.size} ofertas no catálogo"
                    )
                }
                if (loja.produtos.isNotEmpty()) {
                    item(key = "destaques") { DestaquesProdutos(loja.produtos, nav) }
                }
                item(key = "busca") {
                    OutlinedTextField(
                        value = buscaProdutos,
                        onValueChange = { buscaProdutos = it },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        placeholder = { Text("Buscar produtos") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Azul) },
                        singleLine = true,
                        shape = MaterialTheme.shapes.large
                    )
                }
                if (produtosVisiveis.isEmpty()) {
                    item(key = "estado-vazio") {
                        EstadoVazio(
                            if (buscaProdutos.isBlank()) "Nenhum produto por aqui" else "Nada encontrado",
                            if (buscaProdutos.isBlank()) "Toque em \"Novo produto\" para cadastrar o primeiro." else "Tente buscar por outro nome ou vendedor."
                        )
                    }
                }
                items(produtosVisiveis, key = { it.id }) { produto ->
                    val pct = descontoPercentual(produto.precoOriginal, produto.preco)
                    CartaoPadrao(onClick = { nav.navigate(Rotas.detalheProduto(produto.id)) }) {
                        Row(
                            Modifier.padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FotoProduto(
                                produto,
                                Modifier.size(84.dp).clip(RoundedCornerShape(12.dp)).background(Color.White)
                            )
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(produto.nome, style = MaterialTheme.typography.titleMedium)
                                Text("Vendido por ${produto.vendedor}", style = MaterialTheme.typography.bodySmall, color = Cinza)
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
            contentColor = TextoPrincipal,
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
    var produto by remember(id) { mutableStateOf<Produto?>(null) }
    LaunchedEffect(id) { try { produto = withContext(Dispatchers.IO) { loja.produtoPorId(id) } } catch (e: Exception) { Log.e("MercadoPreso", "Falha ao carregar detalhe", e) } }
    var novoPreco by remember(produto?.preco) { mutableStateOf(produto?.preco?.toString() ?: "") }

    Column(Modifier.fillMaxSize()) {
        BarraTopo("Detalhes do produto", onVoltar = { nav.popBackStack() })

        val produtoAtual = produto
        if (produtoAtual == null) {
            EstadoVazio("Produto não encontrado", "Ele pode ter sido removido da lista.")
            return@Column
        }

        val categoria = loja.categoriaDe(produtoAtual.categoriaId)
        val categoriaAtual = categoria
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
                            loja.atualizarPreco(produtoAtual.id, valor)
                            Toast.makeText(ctx, "Preço atualizado", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) { Text("Atualizar preço") }
            }

            Button(
                onClick = {
                    loja.adicionarAoCarrinho(produtoAtual)
                    nav.irParaAba(Rotas.CARRINHO)
                },
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) { Text("Comprar agora") }

            OutlinedButton(
                onClick = {
                    loja.adicionarAoCarrinho(produtoAtual)
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
    LaunchedEffect(Unit) { loja.carregarCategorias() }
    val ctx = LocalContext.current
    val escopo = rememberCoroutineScope()
    var fotoProduto by remember { mutableStateOf<String?>(null) }
    val seletorDeFoto = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            escopo.launch {
                try {
                    val caminho = withContext(Dispatchers.IO) { salvarFotoProduto(ctx, uri) }
                    fotoProduto = caminho
                    if (caminho == null) Toast.makeText(ctx, "Não foi possível abrir essa imagem", Toast.LENGTH_SHORT).show()
                } catch (_: Exception) {
                    Toast.makeText(ctx, "Não foi possível salvar essa imagem", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
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
            TituloSecao("Foto do produto")
            val fotoEscolhida = remember(fotoProduto) { fotoProduto?.let(::lerFotoReduzida) }
            Box(
                Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(18.dp))
                    .background(Color.White).border(1.dp, CardBorda, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (fotoEscolhida != null) {
                    Image(bitmap = fotoEscolhida, contentDescription = "Foto escolhida para $nome",
                        modifier = Modifier.fillMaxSize().padding(10.dp), contentScale = ContentScale.Fit)
                } else {
                    Text("Adicione uma foto para destacar seu produto", color = Cinza,
                        style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(20.dp))
                }
            }
            OutlinedButton(onClick = { seletorDeFoto.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                Text(if (fotoProduto == null) "Escolher foto da galeria" else "Trocar foto")
            }

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
                            descricao.ifBlank { "Sem descrição. Compre por sua conta e risco." },
                            fotoProduto
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





















