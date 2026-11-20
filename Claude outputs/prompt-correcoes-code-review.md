# Correções da revisão de código

Contexto: repositório rekly_mobile (`C:\dev\rekly_mobile`), branch `develop`. Leia o `CLAUDE.md` antes de mexer. Uma revisão de código achou dois problemas pequenos. Corrija só estes dois, sem mexer em mais nada.

**IMPORTANTE:** há 5 arquivos de layout com alterações não commitadas que NÃO são seus (`activity_onboarding.xml`, `fragment_subscriptions.xml`, `item_payment.xml`, `item_subscription.xml`, `layout_detail_row.xml`) e a pasta `Claude outputs/`. Não altere, não faça stage e não commite nada disso. Use `git add` apenas com os caminhos exatos dos arquivos abaixo.

## 1) Desligar o backup automático do Android

**Problema:** `AndroidManifest.xml` tem `android:allowBackup="true"` e aponta para `res/xml/backup_rules.xml` e `res/xml/data_extraction_rules.xml`, que são só o exemplo do Android Studio (tudo comentado). Sem regras reais, o backup na nuvem leva o banco `rekly.db` (e-mails, hash e salt das senhas, todos os gastos) e a sessão salva no SharedPreferences.

**Faça:**
- No `<application>` de `app/src/main/AndroidManifest.xml`: troque para `android:allowBackup="false"` e remova os atributos `android:dataExtractionRules` e `android:fullBackupContent`.
- Apague `app/src/main/res/xml/backup_rules.xml` e `app/src/main/res/xml/data_extraction_rules.xml`.
- Antes de apagar, rode grep por `backup_rules` e `data_extraction_rules` em todo o projeto para garantir que mais nada os referencia.
- Não adicione dependências.

**Commit sugerido:** `chore(manifest): desativa o backup automático do app`

## 2) Remover os testes de exemplo do template

**Problema:** `app/src/test/java/com/example/rekly/ExampleUnitTest.kt` (assert `2 + 2`) e `app/src/androidTest/java/com/example/rekly/ExampleInstrumentedTest.kt` (confere o nome do pacote) não testam nada do app. O CLAUDE.md pede "sem código morto".

**Faça:**
- Apague os dois arquivos.
- Não remova as dependências de teste do `build.gradle.kts` nem do `libs.versions.toml`.

**Commit sugerido:** `chore(test): remove testes de exemplo do template`

## Verificação (obrigatória antes de dizer que terminou)

- `./gradlew test` deve passar (os testes de `domain/rules` continuam rodando).
- `./gradlew assembleDebug` deve compilar sem erro nem warning novo sobre recursos faltando.
- Mostre a saída resumida dos dois comandos.

## Regras de commit (CLAUDE.md)

- Conventional Commits em português, minúsculas, imperativo, um assunto por commit: 2 commits separados.
- Termine cada mensagem com a linha de coautoria que o seu ambiente exigir.
- Commite na `develop`. Não faça push, não faça merge na `main`.

## Ao final, informe

- Quais arquivos mudaram em cada commit.
- Confirmação de que `git status` ainda mostra os 5 layouts como modificados e não commitados.
