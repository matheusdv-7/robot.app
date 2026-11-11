package tech.pd7.wellbot.auth

data class AuthSession(
    val operatorName: String,
    val accessToken: String? = null,
)

interface AuthGateway {
    suspend fun login(username: String, password: String): AuthSession
}

/**
 * Login temporário usado enquanto a autenticação da nuvem não está disponível.
 * Qualquer usuário e senha são aceitos e nenhum dado é enviado ou armazenado.
 */
class DemoAuthGateway : AuthGateway {
    override suspend fun login(username: String, password: String): AuthSession =
        AuthSession(operatorName = username.ifBlank { "Operador" })
}

/**
 * Ponto de ativação da autenticação real.
 * Quando a API da nuvem estiver definida, esta implementação deverá trocar as
 * credenciais por um token temporário. Senhas nunca devem ser salvas no app.
 */
class CloudAuthGateway : AuthGateway {
    override suspend fun login(username: String, password: String): AuthSession =
        error("Autenticação da nuvem ainda não configurada.")
}
