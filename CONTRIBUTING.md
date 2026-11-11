# Como contribuir

## Fluxo de trabalho

1. Atualize sua branch principal local.
2. Crie uma branch curta para a tarefa.
3. Faça alterações pequenas e relacionadas ao mesmo objetivo.
4. Execute as verificações disponíveis no projeto.
5. Abra um pull request e solicite revisão.

Sugestões de nomes de branch:

- `feature/selecao-de-robo`
- `fix/reconexao-mqtt`
- `docs/contrato-de-comandos`
- `chore/configuracao-gradle`

## Commits

Use mensagens objetivas, preferencialmente no padrão Conventional Commits:

```text
feat: adiciona seleção de robô por SN
fix: evita publicação sem conexão MQTT
docs: registra tópicos do broker de teste
test: cobre serialização de comandos
chore: atualiza configuração do Gradle
```

## Pull requests

- Explique o problema e a solução proposta.
- Relacione a issue ou demanda correspondente.
- Inclua imagens ou vídeo quando houver mudança visual.
- Informe como a alteração foi testada.
- Não misture refatorações grandes com uma funcionalidade não relacionada.
- Não inclua segredos, dados de clientes ou identificadores reais de produção.

## Revisão mínima

Antes do merge, confirme:

- compilação e testes aprovados;
- estados de carregamento, erro e desconexão tratados;
- recursos e textos Android organizados;
- acessibilidade básica dos controles;
- logs sem credenciais ou dados sensíveis;
- alterações no protocolo documentadas;
- aprovação de pelo menos outro integrante, quando a política do repositório permitir.

## Proteção recomendada no GitHub

Para a branch principal, recomenda-se exigir pull request, revisão e verificações automatizadas antes do merge. A configuração deve ser feita pelos responsáveis do repositório após a publicação no GitHub.
