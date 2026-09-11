# Ecoa

Assistente financeiro por voz feito com Spring Boot, Spring AI, Gemini e `espeak-ng`.

Envie um áudio com um gasto. Ecoa entende a fala, extrai informações da transação, pede confirmação e responde por áudio em português brasileiro.

## Recursos

- Upload de áudio em MP3, WAV, OGG, M4A, AAC ou FLAC.
- Interpretação de voz pelo Gemini via Spring AI.
- Resposta em WAV gerada localmente pelo `espeak-ng`.
- Confirmação antes de salvar uma transação.
- Consulta de gastos por categoria.
- Arquivos de entrada e saída organizados em `midia/`.

## Arquitetura

```text
Cliente
  -> POST /api/audio
  -> Gemini entende áudio e decide ação
  -> transações são consultadas ou persistidas
  -> espeak-ng gera resposta WAV
  -> cliente recebe áudio
```

Gemini cuida de compreensão e conversa. Spring Boot cuida de HTTP, regras de negócio e persistência. `espeak-ng` cuida somente da voz.

## Tecnologias

- Java 25
- Spring Boot
- Spring AI `google-genai`
- Gemini Flash, na cota gratuita
- JPA e H2 local para transações
- `espeak-ng` para texto em voz local

## Configuração

Defina chave Gemini:

```bash
export GEMINI_KEY="sua-chave"
```

Instale `espeak-ng` no ambiente que executa aplicação:

```bash
sudo apt install espeak-ng
```

O banco H2 é criado localmente como `ecoa.mv.db`. Execute aplicação:

```bash
./mvnw spring-boot:run
```

## Uso

Envie áudio:

```bash
curl -F "file=@gasto.ogg" http://localhost:8080/api/audio --output resposta.wav
```

API responde `audio/wav`. Os arquivos processados ficam em `midia/entrada` e `midia/saida`.

Para confirmar um gasto, envie outro áudio ao mesmo endpoint, por exemplo: “sim, confirmo”.

Consulte gastos confirmados:

```bash
curl http://localhost:8080/api/expenses
curl "http://localhost:8080/api/expenses?category=ALIMENTACAO"
```

## Estrutura

```text
src/main/java/com/marcelodev/ecoa/
├── media/
│   ├── AudioController.java
│   ├── AudioConversationService.java
│   └── MediaProperties.java
├── expense/
│   ├── ExpenseController.java
│   ├── ExpenseTools.java
│   ├── ExpenseRepository.java
│   └── ExpenseDraftRepository.java
└── EcoaApplication.java
```

## Regra principal

Ecoa nunca grava gasto apenas porque ouviu áudio. Primeiro informa valor, descrição e categoria; depois registra somente quando usuário confirmar.
