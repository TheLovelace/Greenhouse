package org.greenhouse.integration.fixture

import org.greenhouse.entity.Usuario
import org.greenhouse.usecase.AuthUseCase

class AutenticacaoFixture {
    // Injeção do caso de uso e repositório dependerá de como o seu projeto de testes está configurado
    lateinit var useCase: AuthUseCase

    fun novoUsuarioAtivo(): Usuario {
        // TODO: Inserir um funcionário e um usuário ativo no banco de testes
        throw NotImplementedError("Implementar inserção de usuário no banco")
    }

    fun novoUsuarioInativo(): Usuario {
        // TODO: Inserir um usuário com ativo = false no banco de testes
        throw NotImplementedError("Implementar inserção de usuário inativo no banco")
    }

    fun limpar(cpf: String? = null, email: String? = null, login: String? = null) {
        // TODO: Deletar o usuário e funcionário gerados no banco após o teste rodar
    }
}