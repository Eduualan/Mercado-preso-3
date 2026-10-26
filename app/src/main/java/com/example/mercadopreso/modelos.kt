package com.example.mercadopreso

import android.content.Context
import android.util.Log
import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// ---------- Data classes ----------
@Entity(tableName = "categorias")
data class Categoria(@PrimaryKey(autoGenerate = true) val id: Int = 0, val nome: String, val descricao: String)

@Entity(tableName = "produtos")
data class Produto(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nome: String,
    val vendedor: String,
    val precoOriginal: Double,
    val preco: Double,
    val categoriaId: Int,
    val descricao: String,
    @ColumnInfo(defaultValue = "NULL") val imagemLocal: String? = null
)

data class ItemCarrinho(val produto: Produto, val quantidade: Int, val marcado: Boolean = false)

// ---------- Rotas ----------
object Rotas {
    const val PRODUTOS = "produtos"
    const val PERFIL = "perfil"
    const val DETALHE_PRODUTO = "produto/{id}"
    const val NOVO_PRODUTO = "novo_produto"
    const val CATEGORIAS = "categorias"
    const val DETALHE_CATEGORIA = "categoria/{id}"
    const val CARRINHO = "carrinho"
    const val CONFIRMADO = "confirmado"
    const val ACOMPANHAR = "acompanhar"

    fun detalheProduto(id: Int) = "produto/$id"
    fun detalheCategoria(id: Int) = "categoria/$id"
}

class LojaState(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val produtosDao = db.produtoDao()
    private val categoriasDao = db.categoriaDao()
    private val io = CoroutineScope(Dispatchers.IO)
    init {
        io.launch {
            try {
                val categoriasIniciais = listOf(
                    Categoria(nome="Acessórios de Fuga", descricao="Tudo para quem tem planos (e pouca sorte)"),
                    Categoria(nome="Vestuário Penitenciário", descricao="Moda listrada para todas as estações"),
                    Categoria(nome="Documentos e Fiança", descricao="Papelada para sair mais cedo"),
                    Categoria(nome="Tecnologia", descricao="Eletrônicos e acessórios para o dia a dia"),
                    Categoria(nome="Moda", descricao="Roupas, calçados e acessórios"),
                    Categoria(nome="Acessórios", descricao="Mochilas, relógios e complementos para sua rotina"),
                    Categoria(nome="Casa e Cozinha", descricao="Itens úteis para deixar a casa mais prática"),
                    Categoria(nome="Papelaria", descricao="Cadernos e materiais para estudar e criar"),
                    Categoria(nome="Esporte e Lazer", descricao="Produtos para se movimentar e se divertir")
                )
                var categoriasAtuais = categoriasDao.buscarTodos()
                categoriasIniciais.filterNot { nova -> categoriasAtuais.any { it.nome.equals(nova.nome, ignoreCase = true) } }
                    .forEach { categoriasDao.inserir(it) }
                categoriasAtuais = categoriasDao.buscarTodos()
                val idsCategoria = categoriasAtuais.associate { it.nome.lowercase() to it.id }
                val categoriaGeral = categoriasAtuais.first().id
                val produtosIniciais = listOf(
                    Produto(nome="Algemas reforçadas mod. fuga zero", vendedor="Cadeia Pública LTDA", precoOriginal=129.90, preco=89.90, categoriaId=idsCategoria["acessórios de fuga"] ?: categoriaGeral, descricao="Aço inoxidável, ajuste universal. Mais vendido em fugas mal sucedidas."),
                    Produto(nome="Colher de cela multiuso", vendedor="Cadeia Pública LTDA", precoOriginal=24.90, preco=19.90, categoriaId=idsCategoria["acessórios de fuga"] ?: categoriaGeral, descricao="Serve para sopa e para planos de longo prazo."),
                    Produto(nome="Smartphone Android 128 GB", vendedor="Tech do Bairro", precoOriginal=1599.90, preco=1399.90, categoriaId=idsCategoria["tecnologia"] ?: categoriaGeral, descricao="Celular versátil com tela ampla, câmera nítida e espaço para seus aplicativos favoritos."),
                    Produto(nome="Tênis casual urbano", vendedor="Passo Leve", precoOriginal=249.90, preco=189.90, categoriaId=idsCategoria["moda"] ?: categoriaGeral, descricao="Confortável para acompanhar a rotina, com visual casual e sola resistente."),
                    Produto(nome="Fone de ouvido com fio", vendedor="Som & Cia", precoOriginal=129.90, preco=89.90, categoriaId=idsCategoria["tecnologia"] ?: categoriaGeral, descricao="Fone leve com áudio estéreo para ouvir música e acompanhar chamadas."),
                    Produto(nome="Mochila urbana", vendedor="Mundo das Bolsas", precoOriginal=199.90, preco=149.90, categoriaId=idsCategoria["acessórios"] ?: categoriaGeral, descricao="Mochila versátil para levar seus itens essenciais no trabalho, estudo ou passeio."),
                    Produto(nome="Relógio de pulso", vendedor="Tempo Certo", precoOriginal=179.90, preco=129.90, categoriaId=idsCategoria["acessórios"] ?: categoriaGeral, descricao="Relógio de pulso clássico para completar o visual e acompanhar seus horários."),
                    Produto(nome="Caneca de cerâmica", vendedor="Casa Bonita", precoOriginal=49.90, preco=34.90, categoriaId=idsCategoria["casa e cozinha"] ?: categoriaGeral, descricao="Caneca clássica de cerâmica para café, chá ou sua bebida favorita."),
                    Produto(nome="Caderno para anotações", vendedor="Papel & Ideia", precoOriginal=39.90, preco=27.90, categoriaId=idsCategoria["papelaria"] ?: categoriaGeral, descricao="Caderno prático para organizar tarefas, estudos e ideias."),
                    Produto(nome="Bola de futebol", vendedor="Joga Junto", precoOriginal=119.90, preco=89.90, categoriaId=idsCategoria["esporte e lazer"] ?: categoriaGeral, descricao="Bola para partidas e momentos de lazer com os amigos.")
                )
                val nomesAtuais = produtosDao.buscarTodos().map { it.nome.trim().lowercase() }.toSet()
                produtosIniciais.filterNot { it.nome.trim().lowercase() in nomesAtuais }.forEach { produtosDao.inserir(it) }
                carregarProdutos()
                carregarCategorias()
            } catch(e: Exception) {
                Log.e("MercadoPreso", "Falha ao preparar dados iniciais", e)
            }
        }
    }
    val categorias = mutableStateListOf<Categoria>()
    val produtos = mutableStateListOf<Produto>()
    val carrinho = mutableStateListOf<ItemCarrinho>()
    var cupomAplicado by mutableStateOf(true)
    var ultimoPedido by mutableStateOf("")
    var ultimoTotal by mutableStateOf(0.0)

    fun carregarProdutos() { io.launch { try { val itens = produtosDao.buscarTodos(); withContext(Dispatchers.Main) { produtos.clear(); produtos.addAll(itens) } } catch(e: Exception) { Log.e("MercadoPreso", "Falha ao carregar produtos", e) } } }
    fun carregarCategorias() { io.launch { try { val itens = categoriasDao.buscarTodos(); withContext(Dispatchers.Main) { categorias.clear(); categorias.addAll(itens) } } catch(e: Exception) { Log.e("MercadoPreso", "Falha ao carregar categorias", e) } } }
    suspend fun produtoPorId(id: Int) = produtosDao.buscarPorId(id)
    suspend fun categoriaPorId(id: Int) = categoriasDao.buscarPorId(id)
    fun categoriaDe(id: Int): Categoria? = categorias.firstOrNull { it.id == id }
    fun produtosDa(categoriaId: Int): List<Produto> = produtos.filter { it.categoriaId == categoriaId }
    val quantidadeTotal: Int get() = carrinho.sumOf { it.quantidade }
    val subtotal: Double get() = carrinho.sumOf { it.produto.preco * it.quantidade }
    val desconto: Double get() = if (cupomAplicado && carrinho.isNotEmpty()) 10.0 else 0.0
    val total: Double get() = subtotal - desconto

    fun adicionarProduto(nome:String, vendedor:String, original:Double, preco:Double, categoriaId:Int, descricao:String, imagemLocal:String? = null) {
        val item = Produto(nome=nome, vendedor=vendedor, precoOriginal=original, preco=preco, categoriaId=categoriaId, descricao=descricao, imagemLocal=imagemLocal)
        io.launch { try { produtosDao.inserir(item); carregarProdutos() } catch(e:Exception) { Log.e("MercadoPreso", "Falha ao inserir produto", e) } }
    }
    fun removerProduto(produto:Produto) { io.launch { try { produtosDao.deletar(produto); withContext(Dispatchers.Main) { carrinho.removeAll { it.produto.id == produto.id } }; carregarProdutos() } catch(e:Exception) { Log.e("MercadoPreso", "Falha ao remover produto", e) } } }
    fun atualizarPreco(id:Int, novoPreco:Double) { io.launch { try { val atual=produtosDao.buscarPorId(id) ?: return@launch; val atualizado=atual.copy(preco=novoPreco, precoOriginal=maxOf(atual.precoOriginal,novoPreco)); produtosDao.atualizar(atualizado); withContext(Dispatchers.Main) { val i=carrinho.indexOfFirst { it.produto.id==id }; if(i>=0)carrinho[i]=carrinho[i].copy(produto=atualizado) }; carregarProdutos() } catch(e:Exception) { Log.e("MercadoPreso", "Falha ao atualizar produto", e) } } }
    fun adicionarCategoria(nome:String, descricao:String) { io.launch { try { categoriasDao.inserir(Categoria(nome=nome,descricao=descricao)); carregarCategorias() } catch(e:Exception) { Log.e("MercadoPreso", "Falha ao inserir categoria", e) } } }
    fun removerCategoria(categoria:Categoria):Boolean { if(produtosDa(categoria.id).isNotEmpty())return false; io.launch { try { categoriasDao.deletar(categoria); carregarCategorias() } catch(e:Exception) { Log.e("MercadoPreso", "Falha ao remover categoria", e) } }; return true }
    fun adicionarAoCarrinho(produto:Produto) { val i=carrinho.indexOfFirst { it.produto.id==produto.id }; if(i>=0)carrinho[i]=carrinho[i].copy(quantidade=carrinho[i].quantidade+1) else carrinho.add(ItemCarrinho(produto,1)) }
    fun alterarQuantidade(id:Int,delta:Int) { val i=carrinho.indexOfFirst { it.produto.id==id }; if(i<0)return; val n=carrinho[i].quantidade+delta; if(n<=0)carrinho.removeAt(i) else carrinho[i]=carrinho[i].copy(quantidade=n) }
    fun alternarMarcado(id:Int) { val i=carrinho.indexOfFirst { it.produto.id==id }; if(i>=0)carrinho[i]=carrinho[i].copy(marcado=!carrinho[i].marcado) }
    fun removerMarcados():Int { val n=carrinho.count { it.marcado }; carrinho.removeAll { it.marcado }; return n }
    fun finalizarCompra() { ultimoTotal=total; ultimoPedido="#PR-${(10000..99999).random()}"; carrinho.clear() }
}




