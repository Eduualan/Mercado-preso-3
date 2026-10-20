@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mercadopreso

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import java.util.Locale

// ---------- Paleta ----------
// Fundo e CardCor continuam os mesmos de antes; o amarelo é a cor da marca
// (a paródia do marketplace) e também a cor da fita de interdição.
val Fundo = Color(0xFF1B1A12)
val CardCor = Color(0xFF2A2920)
val CardBorda = Color(0xFF3F3D2D)
val Amarelo = Color(0xFFFFD60A)
val Azul = Color(0xFF5AA2FF)
val Verde = Color(0xFF3DDC84)
val Cinza = Color(0xFFA3A396)

// ---------- Tipografia ----------
// Barlow Condensed lembra placa de sinalização (títulos e preços);
// Barlow cuida do texto corrido. Os .ttf ficam em app/src/main/res/font/.
val FonteTitulo = FontFamily.Default

val FonteCorpo = FontFamily.Default


val TipografiaPreso = Typography(
    // preço grande
    displaySmall = TextStyle(
        fontFamily = FonteTitulo, fontWeight = FontWeight.Bold,
        fontSize = 44.sp, lineHeight = 48.sp
    ),
    // título de tela e de seção principal
    headlineMedium = TextStyle(
        fontFamily = FonteTitulo, fontWeight = FontWeight.Bold,
        fontSize = 32.sp, lineHeight = 36.sp
    ),
    // barra do topo, preços de lista
    titleLarge = TextStyle(
        fontFamily = FonteTitulo, fontWeight = FontWeight.Bold,
        fontSize = 26.sp, lineHeight = 30.sp
    ),
    // nome do produto / categoria nos cards
    titleMedium = TextStyle(
        fontFamily = FonteTitulo, fontWeight = FontWeight.Bold,
        fontSize = 21.sp, lineHeight = 25.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FonteCorpo, fontWeight = FontWeight.Normal,
        fontSize = 17.sp, lineHeight = 25.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FonteCorpo, fontWeight = FontWeight.Normal,
        fontSize = 15.sp, lineHeight = 21.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FonteCorpo, fontWeight = FontWeight.Normal,
        fontSize = 13.sp, lineHeight = 18.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FonteCorpo, fontWeight = FontWeight.Bold,
        fontSize = 16.sp, lineHeight = 20.sp, letterSpacing = 0.3.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FonteCorpo, fontWeight = FontWeight.Medium,
        fontSize = 13.sp, lineHeight = 16.sp, letterSpacing = 0.2.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FonteCorpo, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.2.sp
    )
)

@Composable
fun MercadoPresoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Amarelo,
            onPrimary = Fundo,
            secondary = Azul,
            onSecondary = Fundo,
            tertiary = Verde,
            onTertiary = Fundo,
            background = Fundo,
            onBackground = Color.White,
            surface = Fundo,
            onSurface = Color.White,
            surfaceVariant = CardCor,
            onSurfaceVariant = Cinza,
            surfaceContainer = CardCor,
            secondaryContainer = Color(0xFF4A4217),
            onSecondaryContainer = Amarelo,
            outline = CardBorda,
            outlineVariant = CardBorda
        ),
        typography = TipografiaPreso,
        shapes = Shapes(
            small = RoundedCornerShape(10.dp),
            medium = RoundedCornerShape(14.dp),
            large = RoundedCornerShape(22.dp)
        ),
        content = content
    )
}

// ---------- Utilitários ----------
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

// ---------- Componentes ----------

/** Fita de interdição: o detalhe visual que aparece embaixo da barra do topo em todas as telas. */
@Composable
fun FaixaListrada(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(8.dp)
            .background(Amarelo)
            .clipToBounds()
            .drawBehind {
                val largura = 16.dp.toPx()
                val altura = size.height
                var x = -altura
                while (x < size.width) {
                    val faixa = Path().apply {
                        moveTo(x, altura)
                        lineTo(x + largura / 2, altura)
                        lineTo(x + largura / 2 + altura, 0f)
                        lineTo(x + altura, 0f)
                        close()
                    }
                    drawPath(faixa, Fundo)
                    x += largura
                }
            }
    )
}

@Composable
fun BarraTopo(
    titulo: String,
    onVoltar: (() -> Unit)? = null,
    acoes: @Composable RowScope.() -> Unit = {}
) {
    Column {
        TopAppBar(
            title = { Text(titulo, style = MaterialTheme.typography.titleLarge) },
            navigationIcon = {
                if (onVoltar != null) {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            },
            actions = acoes,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Fundo,
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White,
                actionIconContentColor = Color.White
            )
        )
        FaixaListrada()
    }
}

/** Card padrão do app: fundo escuro com borda fina, clicável quando recebe onClick. */
@Composable
fun CartaoPadrao(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cores = CardDefaults.cardColors(containerColor = CardCor)
    val borda = BorderStroke(1.dp, CardBorda)
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            colors = cores,
            border = borda,
            content = content
        )
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = cores,
            border = borda,
            content = content
        )
    }
}

/** Etiqueta pequena (desconto, quantidade de produtos). */
@Composable
fun Selo(texto: String, cor: Color, modifier: Modifier = Modifier) {
    Text(
        texto,
        color = cor,
        style = MaterialTheme.typography.labelMedium,
        modifier = modifier
            .background(cor.copy(alpha = 0.15f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Composable
fun TituloSecao(texto: String, modifier: Modifier = Modifier) {
    Text(
        texto,
        style = MaterialTheme.typography.titleMedium,
        color = Amarelo,
        modifier = modifier.padding(top = 4.dp)
    )
}

/** Tela/lista sem conteúdo: explica o que fazer em seguida. */
@Composable
fun EstadoVazio(titulo: String, subtitulo: String, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(titulo, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(
            subtitulo,
            style = MaterialTheme.typography.bodyMedium,
            color = Cinza,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LinhaValor(rotulo: String, valor: String, corValor: Color = Color.White, negrito: Boolean = false) {
    val estilo = if (negrito) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyMedium
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(rotulo, style = estilo, color = if (negrito) Color.White else Cinza)
        Text(valor, style = estilo, color = corValor)
    }
}
