package com.example.mercadopreso.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PerfilUiState(
    val nome: String = "Visitante",
    val apelido: String = "Recruta",
    val editando: Boolean = false,
    val avisosAtivos: Boolean = true,
    val quantidadeCarrinho: Int = 0,
    val ofertasDisponiveis: Int = 0,
    val mensagem: String? = null
)

class PerfilViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState = _uiState.asStateFlow()

    fun atualizarResumo(carrinho: Int, ofertas: Int) = _uiState.update {
        it.copy(quantidadeCarrinho = carrinho, ofertasDisponiveis = ofertas)
    }
    fun atualizarNome(valor: String) = _uiState.update { it.copy(nome = valor) }
    fun atualizarApelido(valor: String) = _uiState.update { it.copy(apelido = valor.filter { c -> c.isLetterOrDigit() || c == '_' }) }
    fun iniciarEdicao() = _uiState.update { it.copy(editando = true) }
    fun alterarAvisos(ativo: Boolean) = _uiState.update { it.copy(avisosAtivos = ativo) }

    fun salvarPerfil(): Boolean {
        val state = _uiState.value
        if (state.nome.isBlank() || state.apelido.isBlank()) {
            _uiState.update { it.copy(mensagem = "Preencha nome e apelido") }
            return false
        }
        _uiState.update { it.copy(nome = it.nome.trim(), apelido = it.apelido.trim(), editando = false, mensagem = "Perfil atualizado") }
        return true
    }
    fun limparMensagem() = _uiState.update { it.copy(mensagem = null) }
}
