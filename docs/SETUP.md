# Preparação do ambiente Android

## Ferramentas

- Git
- Android Studio compatível com Android Gradle Plugin 9.4
- Android SDK configurado pelo Android Studio
- JDK compatível com a versão do Android Gradle Plugin escolhida
- Emulador Android ou dispositivo físico autorizado para testes

O projeto usa Gradle Wrapper 9.6, Android Gradle Plugin 9.4, Kotlin 2.4.10, Java 17 como alvo de bytecode e `compileSdk`/`targetSdk` 37.

## Primeiro acesso ao repositório

```bash
git clone <URL_DO_REPOSITORIO>
cd <PASTA_DO_REPOSITORIO>
```

1. Abra a raiz no Android Studio.
2. Aguarde a sincronização do Gradle.
3. Instale pelo SDK Manager a plataforma Android 17/API 37 e os componentes solicitados.
4. Se necessário, copie `local.properties.example` para `local.properties` e ajuste o caminho local do SDK.
5. Execute os testes e a variante de desenvolvimento antes de abrir um pull request.

## Configuração e segredos

- `local.properties` permanece apenas na máquina do desenvolvedor.
- Credenciais MQTT, tokens e certificados privados nunca devem entrar no Git.
- Valores compartilháveis devem ter um arquivo de exemplo sem dados reais.
- Ambientes de desenvolvimento, homologação e produção devem permanecer separados.
- O acesso ao broker de produção deve seguir as permissões definidas pela PD7tech.
- A URI MQTT de desenvolvimento pode ser fornecida por `-PWELLBOT_MQTT_BROKER_URI=ssl://host:porta`.
- A aplicação rejeita URIs MQTT sem TLS. Use `ssl://` ou `wss://`.

## Ambientes planejados

- `dev`: desenvolvimento local e broker de teste;
- `staging`: validação integrada antes de uma entrega;
- `production`: ambiente de eventos e equipamentos reais.

Os nomes, endpoints e mecanismos de injeção de configuração ainda precisam ser aprovados.

## Comandos úteis

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
```

O APK de desenvolvimento é criado em `app/build/outputs/apk/debug/app-debug.apk`.
