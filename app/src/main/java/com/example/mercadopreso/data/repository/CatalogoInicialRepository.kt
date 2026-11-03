package com.example.mercadopreso.data.repository

import androidx.room.withTransaction
import com.example.mercadopreso.data.local.AppDatabase
import com.example.mercadopreso.data.local.entity.Categoria
import com.example.mercadopreso.data.local.entity.Produto
import java.util.Locale

/** Carrega o catálogo demonstrativo somente na criação de um banco novo. */
class CatalogoInicialRepository(
    private val database: AppDatabase,
    private val estadoCatalogoRepository: EstadoCatalogoRepository,
    private val categoriaRepository: CategoriaRepository,
    private val produtoRepository: ProdutoRepository
) {
    suspend fun inicializarSeNecessario() {
        database.withTransaction {
            if (estadoCatalogoRepository.foiInicializado()) return@withTransaction

            val categoriasIniciais = listOf(
                Categoria(nome = "Acessórios de Fuga", descricao = "Tudo para quem tem planos (e pouca sorte)"),
                Categoria(nome = "Vestuário Penitenciário", descricao = "Moda listrada para todas as estações"),
                Categoria(nome = "Documentos e Fiança", descricao = "Papelada para sair mais cedo"),
                Categoria(nome = "Tecnologia", descricao = "Eletrônicos e acessórios para o dia a dia"),
                Categoria(nome = "Moda", descricao = "Roupas, calçados e acessórios"),
                Categoria(nome = "Acessórios", descricao = "Mochilas, relógios e complementos para sua rotina"),
                Categoria(nome = "Casa e Cozinha", descricao = "Itens úteis para deixar a casa mais prática"),
                Categoria(nome = "Papelaria", descricao = "Cadernos e materiais para estudar e criar"),
                Categoria(nome = "Esporte e Lazer", descricao = "Produtos para se movimentar e se divertir")
            )
            val categoriaIds = categoriasIniciais.associate { categoria ->
                categoria.nome.lowercase(Locale.ROOT) to categoriaRepository.inserir(categoria).toInt()
            }
            val geral = categoriaIds.values.first()

            val produtosIniciais = listOf(
                Produto(nome = "Algemas reforçadas mod. fuga zero", vendedor = "Cadeia Pública LTDA", precoOriginal = 129.90, preco = 89.90, categoriaId = categoriaIds["acessórios de fuga"] ?: geral, descricao = "Aço inoxidável, ajuste universal. Mais vendido em fugas mal sucedidas."),
                Produto(nome = "Colher de cela multiuso", vendedor = "Cadeia Pública LTDA", precoOriginal = 24.90, preco = 19.90, categoriaId = categoriaIds["acessórios de fuga"] ?: geral, descricao = "Serve para sopa e para planos de longo prazo."),
                Produto(nome = "Smartphone Android 128 GB", vendedor = "Tech do Bairro", precoOriginal = 1599.90, preco = 1399.90, categoriaId = categoriaIds["tecnologia"] ?: geral, descricao = "Celular versátil com tela ampla, câmera nítida e espaço para seus aplicativos favoritos."),
                Produto(nome = "Tênis casual urbano", vendedor = "Passo Leve", precoOriginal = 249.90, preco = 189.90, categoriaId = categoriaIds["moda"] ?: geral, descricao = "Confortável para acompanhar a rotina, com visual casual e sola resistente."),
                Produto(nome = "Fone de ouvido com fio", vendedor = "Som & Cia", precoOriginal = 129.90, preco = 89.90, categoriaId = categoriaIds["tecnologia"] ?: geral, descricao = "Fone leve com áudio estéreo para ouvir música e acompanhar chamadas."),
                Produto(nome = "Mochila urbana", vendedor = "Mundo das Bolsas", precoOriginal = 199.90, preco = 149.90, categoriaId = categoriaIds["acessórios"] ?: geral, descricao = "Mochila versátil para levar seus itens essenciais no trabalho, estudo ou passeio."),
                Produto(nome = "Relógio de pulso", vendedor = "Tempo Certo", precoOriginal = 179.90, preco = 129.90, categoriaId = categoriaIds["acessórios"] ?: geral, descricao = "Relógio de pulso clássico para completar o visual e acompanhar seus horários."),
                Produto(nome = "Caneca de cerâmica", vendedor = "Casa Bonita", precoOriginal = 49.90, preco = 34.90, categoriaId = categoriaIds["casa e cozinha"] ?: geral, descricao = "Caneca clássica de cerâmica para café, chá ou sua bebida favorita."),
                Produto(nome = "Caderno para anotações", vendedor = "Papel & Ideia", precoOriginal = 39.90, preco = 27.90, categoriaId = categoriaIds["papelaria"] ?: geral, descricao = "Caderno prático para organizar tarefas, estudos e ideias."),
                Produto(nome = "Bola de futebol", vendedor = "Joga Junto", precoOriginal = 119.90, preco = 89.90, categoriaId = categoriaIds["esporte e lazer"] ?: geral, descricao = "Bola para partidas e momentos de lazer com os amigos.")
            )
            produtosIniciais.forEach { produtoRepository.inserir(it) }
            estadoCatalogoRepository.marcarInicializado()
        }
    }
}
