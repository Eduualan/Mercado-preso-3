package com.example.mercadopreso

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MercadoPresoTheme {
                AppMercadoPreso()
            }
        }
    }
}

data class Aba(val rota: String, val titulo: String, val icone: ImageVector)

@Composable
fun AppMercadoPreso() {
    val navController = rememberNavController()
    val loja = remember { LojaState() }

    val abas = listOf(
        Aba(Rotas.PRODUTOS, "Produtos", Icons.Default.Home),
        Aba(Rotas.CATEGORIAS, "Categorias", Icons.AutoMirrored.Filled.List),
        Aba(Rotas.CARRINHO, "Carrinho", Icons.Default.ShoppingCart)
    )

    val entradaAtual by navController.currentBackStackEntryAsState()
    val rotaAtual = entradaAtual?.destination?.route

    Scaffold(
        containerColor = Fundo,
        bottomBar = {
            // A barra de baixo só aparece nas 3 telas principais
            if (rotaAtual in abas.map { it.rota }) {
                NavigationBar {
                    abas.forEach { aba ->
                        NavigationBarItem(
                            selected = rotaAtual == aba.rota,
                            onClick = { navController.irParaAba(aba.rota) },
                            icon = {
                                if (aba.rota == Rotas.CARRINHO && loja.carrinho.isNotEmpty()) {
                                    BadgedBox(badge = { Badge { Text("${loja.quantidadeTotal}") } }) {
                                        Icon(aba.icone, contentDescription = aba.titulo)
                                    }
                                } else {
                                    Icon(aba.icone, contentDescription = aba.titulo)
                                }
                            },
                            label = { Text(aba.titulo) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Rotas.PRODUTOS,
            modifier = Modifier.padding(padding)
        ) {
            composable(Rotas.PRODUTOS) { TelaProdutos(navController, loja) }

            composable(
                Rotas.DETALHE_PRODUTO,
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { entrada ->
                TelaDetalheProduto(navController, loja, entrada.arguments?.getInt("id") ?: -1)
            }

            composable(Rotas.NOVO_PRODUTO) { TelaNovoProduto(navController, loja) }

            composable(Rotas.CATEGORIAS) { TelaCategorias(navController, loja) }

            composable(
                Rotas.DETALHE_CATEGORIA,
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { entrada ->
                TelaDetalheCategoria(navController, loja, entrada.arguments?.getInt("id") ?: -1)
            }

            composable(Rotas.CARRINHO) { TelaCarrinho(navController, loja) }

            composable(Rotas.CONFIRMADO) { TelaPedidoConfirmado(navController, loja) }

            composable(Rotas.ACOMPANHAR) { TelaAcompanharPedido(navController) }
        }
    }
}
