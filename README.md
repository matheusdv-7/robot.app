# WELLBOT Mobile

Base do aplicativo Android de navegação e gerenciamento dos robôs WELLBOT em eventos.

O aplicativo permite localizar robôs disponíveis na nuvem, selecionar um equipamento pelo número de série (SN), solicitar navegação para points publicados pelo próprio robô e solicitar transferências de mapas entre robôs online.

## Estado atual

A base Android já compila e possui uma primeira navegação funcional:

- descoberta e seleção de robôs por SN;
- tela de login demonstrativo preparada para autenticação futura;
- robôs online e offline, com os online apresentados primeiro;
- área de navegação com botões gerados pelos points recebidos da nuvem;
- fluxo demonstrativo de cópia e instalação de mapas entre robôs online;
- modo demonstração automático enquanto não houver broker configurado;
- duas conexões MQTT isoladas: descoberta e sessão do robô;
- comandos JSON versionados com identificador e timestamp;
- reconexão automática, QoS 1 e exigência de transporte TLS;
- tema inicial pixelado em verde com identificação PD7tech.

O contrato MQTT presente no código é provisório. Ele deve ser confrontado com o broker e o sistema de movimentação antes de controlar um equipamento real.

## Escopo inicial

- Exibir os números de série disponíveis no broker em nuvem.
- Permitir que o usuário selecione um robô.
- Abrir uma sessão de comunicação para o SN selecionado.
- Consultar ou receber da nuvem as navegações disponíveis.
- Apresentar os points de navegação como botões verdes recebidos da nuvem.
- Publicar cada solicitação de navegação em formato JSON usando MQTT `pub/sub`.
- Solicitar à nuvem a cópia e instalação de mapas entre robôs online.
- Preparar uma camada separada para eventual comunicação TCP.
- Aplicar a identidade visual PD7tech, com estética pixelada e predominância de verde.

O sistema de movimentação embarcado do robô já existe e não faz parte desta primeira entrega.

## Direção técnica inicial

- Android Studio
- Kotlin
- Jetpack Compose
- Arquitetura MVVM
- MQTT para publicação e assinatura
- JSON para mensagens
- TCP isolado em uma camada de comunicação própria, caso seja necessário

Essas escolhas são a direção inicial e devem ser validadas pela equipe antes da implementação.

## Documentação

- [Visão e requisitos iniciais](docs/PROJECT_BRIEF.md)
- [Preparação do ambiente Android](docs/SETUP.md)
- [Contrato MQTT provisório](docs/MQTT_CONTRACT.md)
- [Como contribuir](CONTRIBUTING.md)

## Executar

1. Abra a raiz do repositório no Android Studio.
2. Aguarde a sincronização do Gradle.
3. Selecione a configuração `app` e execute em um emulador ou dispositivo.

Sem configuração adicional, a variante `debug` abre em modo demonstração. Para usar um broker de desenvolvimento com TLS:

```powershell
.\gradlew.bat assembleDebug -PWELLBOT_MQTT_BROKER_URI=ssl://host-do-broker:8883
```

Não use usuário ou senha em parâmetros, arquivos versionados ou `BuildConfig`. A autenticação definitiva precisa utilizar credenciais temporárias ou armazenamento seguro e ainda será definida com a equipe de nuvem.

## Próximas definições

Antes de implementar a integração, a equipe precisa confirmar:

- endereço, porta e protocolo seguro do broker;
- método de autenticação;
- tópicos MQTT e regras por SN;
- formatos dos JSONs de entrada e saída;
- QoS, mensagens retidas, keep-alive e reconexão;
- origem da lista de SN e das navegações disponíveis;
- necessidade e finalidade exata do TCP;
- comportamento de segurança quando a conexão for perdida.

Não publique credenciais, certificados privados, endereços internos ou arquivos locais de configuração no GitHub.
