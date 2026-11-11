# Visão e requisitos iniciais

## Objetivo

Criar um aplicativo Android para navegação e gerenciamento dos robôs WELLBOT durante eventos. A primeira versão será responsável por login demonstrativo, seleção do robô, points de navegação e transferência de mapas. A movimentação física já é executada por um sistema existente.

## Fluxo esperado

1. O aplicativo conecta-se ao serviço em nuvem.
2. Recebe o conjunto de números de série disponíveis no broker.
3. O usuário seleciona um SN.
4. O aplicativo estabelece ou configura uma sessão MQTT para o robô selecionado.
5. Uma nova tela apresenta os points de navegação disponibilizados pela nuvem.
6. Ao tocar em um point, o aplicativo publica uma solicitação JSON.
7. O app recebe estados, confirmações ou erros pelos tópicos de assinatura definidos no contrato.

## Requisitos funcionais iniciais

### Descoberta e seleção

- Receber da nuvem a lista de SN disponíveis.
- Exibir estados de carregamento, lista vazia, falha e desconexão.
- Permitir a escolha de somente um robô por sessão de controle.

### Sessão do robô

- Associar a sessão ao SN selecionado.
- Receber as ações de navegação permitidas para o equipamento.
- Impedir o envio de comandos antes de a sessão estar pronta.
- Informar visualmente o estado da conexão.

### Navegação

- Representar os points disponíveis por botões verdes claros e acessíveis.
- Publicar uma mensagem JSON para cada point selecionado pelo usuário.
- Evitar disparos duplicados acidentais.

### Switch de mapa

- Listar os mapas publicados pelo robô selecionado.
- Listar como destino somente outros robôs online.
- Solicitar à nuvem a cópia e instalação do mapa escolhido.
- Exibir futuramente progresso, sucesso ou falha retornados pela nuvem.

### Identidade visual

- Estética inspirada em pixels.
- Verde como cor predominante.
- Identificação visual PD7tech.
- Boa legibilidade em ambientes de eventos e telas de tamanhos diferentes.

## Arquitetura preliminar

Separação sugerida para o futuro projeto Android:

- `ui`: telas, componentes e tema visual;
- `presentation`: estados de tela e ViewModels;
- `domain`: modelos e casos de uso;
- `data`: repositórios e conversão de mensagens;
- `communication/mqtt`: conexão, assinatura e publicação MQTT;
- `communication/tcp`: comunicação TCP, se confirmada;
- `core`: configuração, tratamento de erros e utilitários compartilhados.

A estrutura final será criada junto com o projeto Android, depois da definição do nome do pacote, SDK mínimo e módulos necessários.

## Contratos ainda não definidos

Os exemplos abaixo são apenas campos a confirmar; não representam um protocolo implementado:

```json
{
  "sn": "SN_A_CONFIRMAR",
  "command": "MOVE_A_CONFIRMAR",
  "requestId": "ID_A_CONFIRMAR",
  "timestamp": "FORMATO_A_CONFIRMAR"
}
```

Precisam ser definidos:

- tópicos de descoberta, comando, estado e confirmação;
- esquema e versionamento das mensagens;
- comandos suportados e seus parâmetros;
- resposta esperada para cada comando;
- timeout, repetição e idempotência;
- autorização de cada usuário e robô;
- parada segura e comportamento offline.

## Fora do escopo inicial

- Implementar o controle de motores no robô.
- Alterar o firmware ou o sistema de movimentação existente.
- Definir por suposição o protocolo final da nuvem.
- Armazenar segredos diretamente no código ou no repositório.

## Critério inicial de sucesso

Uma primeira entrega poderá ser considerada válida quando o usuário conseguir visualizar SNs disponibilizados por um ambiente de teste, escolher um robô, visualizar os controles permitidos e publicar comandos JSON rastreáveis com estados de conexão e erro apresentados na interface.
