@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mercadopreso

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import java.util.Locale

val Fundo = Color(0xFF1B1A12)
val CardCor = Color(0xFF2A2920)
val Azul = Color(0xFF1E7BFF)
val Verde = Color(0xFF3DDC84)
val Cinza = Color(0xFF9A9A8E)

@Composable
fun MercadoPresoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Azul,
            background = Fundo,
            surface = Fundo,
            surfaceVariant = CardCor,
            onBackground = Color.White,
            onSurface = Color.White
        ),
        content = content
    )
}

fun Double.reais(): String =
    "R$ " + String.format(Locale.forLanguageTag("pt-BR"), "%,.2f", this)

fun String.paraDouble(): Double? = trim().replace(",", ".").toDoubleOrNull()

/** Navegação das abas do BottomNavigation (não empilha telas repetidas). */
fun NavController.irParaAba(rota: String) {
    navigate(rota) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun BarraTopo(
    titulo: String,
    onVoltar: (() -> Unit)? = null,
    acoes: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = { Text(titulo, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            if (onVoltar != null) {
                IconButton(onClick = onVoltar) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                }
            }
        },
        actions = acoes,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Fundo)
    )
}

@Composable
fun LinhaValor(rotulo: String, valor: String, corValor: Color = Color.White, negrito: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(rotulo, color = if (negrito) Color.White else Cinza, fontWeight = if (negrito) FontWeight.Bold else null)
        Text(valor, color = corValor, fontWeight = if (negrito) FontWeight.Bold else null)
    }
}
