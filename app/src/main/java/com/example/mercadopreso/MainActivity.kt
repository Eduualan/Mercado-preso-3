package com.example.mercadopreso

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mercadopreso.data.MercadoPresoContainer
import com.example.mercadopreso.ui.*
import com.example.mercadopreso.viewmodel.CarrinhoViewModel
import com.example.mercadopreso.viewmodel.CarrinhoViewModelFactory
import com.example.mercadopreso.viewmodel.CategoriaViewModel
import com.example.mercadopreso.viewmodel.CategoriaViewModelFactory
import com.example.mercadopreso.viewmodel.PerfilViewModel
import com.example.mercadopreso.viewmodel.PerfilViewModelFactory
import com.example.mercadopreso.viewmodel.ProdutoViewModel
import com.example.mercadopreso.viewmodel.ProdutoViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MercadoPresoTheme { AppMercadoPreso() }
        }
    }
}

data class Aba(val rota: String, val titulo: String, val icone: ImageVector)

@Composable
fun AppMercadoPreso() {
    val navController = rememberNavController()
    val context = LocalContext.current.applicationContext
    val container = remember { MercadoPresoContainer(context) }
    val produtoRepository = container.produtoRepository
    val categoriaRepository = container.categoriaRepository
    val catalogoInicialRepository = container.catalogoInicialRepository

    val produtoFactory = remember { ProdutoViewModelFactory(produtoRepository, categoriaRepository, catalogoInicialRepository) }
    val categoriaFactory = remember { CategoriaViewModelFactory(categoriaRepository, produtoRepository) }
    val produtoViewModel: ProdutoViewModel = viewModel(factory = produtoFactory)
    val categoriaViewModel: CategoriaViewModel = viewModel(factory = categoriaFactory)
    val carrinhoViewModel: CarrinhoViewModel = viewModel(factory = remember { CarrinhoViewModelFactory() })
    val perfilViewModel: PerfilViewModel = viewModel(factory = remember { PerfilViewModelFactory() })
    val produtoState by produtoViewModel.uiState.collectAsState()
    val carrinhoState by carrinhoViewModel.uiState.collectAsState()

    LaunchedEffect(carrinhoState.itens.size, produtoState.produtos.size) {
        perfilViewModel.atualizarResumo(carrinhoState.itens.size, produtoState.produtos.size)
    }
    val abas = listOf(
        Aba(Rotas.PRODUTOS, "Ofertas", Icons.Default.Home),
        Aba(Rotas.CATEGORIAS, "Setores", Icons.AutoMirrored.Filled.List),
        Aba(Rotas.CARRINHO, "Carrinho", Icons.Default.ShoppingCart),
        Aba(Rotas.PERFIL, "Perfil", Icons.Default.Person)
    )
    val entradaAtual by navController.currentBackStackEntryAsState()
    val rotaAtual = entradaAtual?.destination?.route

    Scaffold(
        containerColor = Fundo,
        bottomBar = {
            if (rotaAtual in abas.map { it.rota }) {
                NavigationBar(containerColor = Color.White, contentColor = Cinza, tonalElevation = 8.dp) {
                    abas.forEach { aba ->
                        NavigationBarItem(
                            selected = rotaAtual == aba.rota,
                            onClick = { navController.irParaAba(aba.rota) },
                            icon = {
                                if (aba.rota == Rotas.CARRINHO && carrinhoState.itens.isNotEmpty()) {
                                    BadgedBox(badge = { Badge { Text("${carrinhoState.quantidadeTotal}") } }) {
                                        Icon(aba.icone, contentDescription = aba.titulo)
                                    }
                                } else Icon(aba.icone, contentDescription = aba.titulo)
                            },
                            label = { Text(aba.titulo) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TextoPrincipal,
                                selectedTextColor = Amarelo,
                                indicatorColor = Amarelo,
                                unselectedIconColor = Cinza,
                                unselectedTextColor = Cinza
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(navController, startDestination = Rotas.PRODUTOS, modifier = Modifier.padding(padding)) {
            composable(Rotas.PRODUTOS) { TelaProdutos(navController, produtoViewModel, carrinhoViewModel) }
            composable(Rotas.DETALHE_PRODUTO, arguments = listOf(navArgument("id") { type = NavType.IntType })) { entrada ->
                TelaDetalheProduto(navController, produtoViewModel, carrinhoViewModel, entrada.arguments?.getInt("id") ?: -1)
            }
            composable(Rotas.NOVO_PRODUTO) { TelaNovoProduto(navController, produtoViewModel) }
            composable(Rotas.CATEGORIAS) { TelaCategorias(navController, categoriaViewModel) }
            composable(Rotas.DETALHE_CATEGORIA, arguments = listOf(navArgument("id") { type = NavType.IntType })) { entrada ->
                TelaDetalheCategoria(navController, categoriaViewModel, entrada.arguments?.getInt("id") ?: -1)
            }
            composable(Rotas.CARRINHO) { TelaCarrinho(navController, carrinhoViewModel) }
            composable(Rotas.PERFIL) { TelaPerfil(navController, perfilViewModel) }
            composable(Rotas.CONFIRMADO) { TelaPedidoConfirmado(navController, carrinhoViewModel) }
            composable(Rotas.ACOMPANHAR) { TelaAcompanharPedido(navController, carrinhoViewModel) }
        }
    }
}
