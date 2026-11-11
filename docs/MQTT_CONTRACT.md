# Contrato MQTT provisório

Este documento registra o contrato implementado para permitir o desenvolvimento independente da interface. Nada aqui deve ser considerado definitivo até a validação conjunta com a nuvem e o sistema embarcado.

## Segurança e sessão

- Transporte obrigatório por TLS: `ssl://` ou `wss://`.
- MQTT 3.1.1.
- QoS 1 para assinatura e publicação.
- Keep-alive de 20 segundos.
- Timeout de conexão de 10 segundos.
- Reconexão automática.
- Sessão limpa nesta fase inicial.
- Sem mensagens de comando retidas.

Credenciais não estão implementadas na interface do usuário nem armazenadas no repositório. A produção deve preferir token temporário por usuário/dispositivo e autorização de tópicos no broker.

## Tópicos

Raiz provisória: `wellbot/v1`.

| Finalidade | Tópico |
| --- | --- |
| Lista de robôs | `wellbot/v1/robots/availability` |
| Points de navegação | `wellbot/v1/robots/{SN}/navigation/points` |
| Solicitação de navegação | `wellbot/v1/robots/{SN}/navigation/requests` |
| Mapas do robô | `wellbot/v1/robots/{SN}/maps` |
| Transferência de mapa | `wellbot/v1/maps/transfers` |
| Estado | `wellbot/v1/robots/{SN}/state` |
| Confirmações | `wellbot/v1/robots/{SN}/acks` |

O SN aceita somente letras, números, ponto, sublinhado e hífen, com até 64 caracteres. Essa validação impede a injeção de curingas MQTT no tópico.

## Descoberta

Payload esperado no tópico de disponibilidade:

```json
{
  "robots": [
    {
      "sn": "WB-001",
      "displayName": "WELLBOT Arena",
      "online": true
    }
  ]
}
```

## Points de navegação

Payload esperado por robô:

```json
{
  "points": [
    { "id": "entrada", "label": "Entrada principal" },
    { "id": "palco", "label": "Palco" }
  ]
}
```

A interface cria um botão verde para cada point presente nessa mensagem.

## Comando

Exemplo publicado:

```json
{
  "version": 1,
  "requestId": "9f56e92a-1cf7-40b2-a787-4fe610d02a36",
  "sn": "WB-001",
  "pointId": "palco",
  "timestamp": "2026-11-05T12:00:00Z"
}
```

`requestId` permite correlacionar uma futura confirmação e tratar idempotência. O horário utiliza UTC no formato ISO 8601.

## Mapas e transferência

O robô selecionado publica sua lista de mapas:

```json
{
  "maps": [
    { "id": "map-evento-a", "name": "Mapa Evento A", "version": "3" }
  ]
}
```

O aplicativo não transfere o arquivo binário. Ele solicita à nuvem que copie e instale o mapa no robô de destino:

```json
{
  "version": 1,
  "requestId": "9f56e92a-1cf7-40b2-a787-4fe610d02a36",
  "sourceSn": "WB-001",
  "targetSn": "WB-002",
  "mapId": "map-evento-a",
  "timestamp": "2026-11-05T12:00:00Z"
}
```

Somente robôs online podem ser escolhidos como destino. A nuvem deverá responder com progresso, sucesso ou falha e garantir a instalação atômica no armazenamento do robô.

## Itens obrigatórios antes do robô real

- Confirmar a raiz e todos os nomes de tópicos.
- Definir se descoberta e controle usam o mesmo usuário ou credenciais diferentes.
- Definir a origem e a expiração das credenciais.
- Definir retained messages e Last Will and Testament.
- Definir esquema de confirmação, rejeição e estado.
- Definir timeout de comando e comportamento de parada segura.
- Confirmar comandos contínuos versus comandos discretos.
- Validar se a sessão deve ser persistente.
- Validar certificados e rotação de chaves.
- Testar perda de rede, reconexão e disputa entre operadores.
