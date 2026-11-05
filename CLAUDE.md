# CLAUDE.md — Rekly Mobile

Contexto e regras para qualquer agente de IA (ou pessoa) que trabalhe neste repositório. Leia antes de alterar código.

## O projeto

Rekly Mobile é o Projeto Final da disciplina **Desenvolvimento Mobile II**, feito em dupla por Guilherme S. Machado e Murilo K. Klein. É um app Android para gerenciar gastos recorrentes (assinaturas e contas como água, luz, internet), versão mobile do Rekly web (`github.com/muriloklein/rekly`).

O app é avaliado por **boas práticas, organização e documentação**, não só por funcionar. Prefira sempre a solução simples, legível e fácil de explicar numa apresentação.

### Requisitos obrigatórios da disciplina

- Kotlin + Android Studio, layouts em **XML** (não usar Jetpack Compose)
- Mínimo de 4 Activities (o plano tem 7) e uso de Fragments
- Tela de login funcional (local, sem backend)
- Navegação e layout consistentes entre telas
- Formulários com validações e regras de negócio
- Objetos de domínio (data classes, enums)
- Persistência com **Room** (SQLite)
- CRUD completo (assinaturas e categorias)
- GitHub com branches `main` e `develop` e **Conventional Commits**
- README com nome, integrantes, descrição, tecnologias, instruções de execução e prints

## Stack

- Kotlin, AGP 9 (Kotlin embutido no plugin Android), minSdk 24, targetSdk 36
- ViewBinding (nunca `findViewById` em código novo)
- Material Components 3, ConstraintLayout, RecyclerView
- Room (runtime + ktx + compiler via **KSP**, nunca kapt)
- Lifecycle: ViewModel + LiveData, Coroutines
- WorkManager (apenas para o lembrete de vencimento, se implementado)
- Versões de dependências **somente** em `gradle/libs.versions.toml`

**Fora do escopo — não adicionar:** Retrofit, Firebase, Hilt/Dagger, Navigation Component, Compose, import/export de CSV, Open Finance, recuperação de senha por e-mail. Não adicione nenhuma dependência nova sem a dupla pedir.

## Arquitetura (MVVM)

Fluxo único: **Activity/Fragment → ViewModel → Repository → DAO (Room)**.

- Telas não acessam DAO nem banco diretamente.
- ViewModel não conhece `Context`, `View` nem classes do Android UI.
- Regras de negócio ficam em `domain/rules` como **funções puras** (sem Android), testadas em `src/test`.
- Operações de banco em coroutines (`suspend` no DAO, `viewModelScope` no ViewModel). Nada de `allowMainThreadQueries`.

```
com.example.rekly
├── data
│   ├── local        → AppDatabase, DAOs, Converters
│   ├── entity       → User, Category, Subscription, Payment
│   └── repository   → um Repository por agregado
├── domain
│   ├── model        → enums BillingPeriod, SubscriptionStatus, PaymentStatus; OperationResult, RuleError
│   ├── catalog      → ServiceCatalog (lista fixa de serviços e contas)
│   └── rules        → ExpenseCalculator, Validators, PaymentGenerator, PasswordHasher, SavingsSuggestions
├── ui
│   ├── auth         → LoginActivity, SignUpActivity
│   ├── onboarding   → OnboardingActivity
│   ├── main         → MainActivity + SummaryFragment, SubscriptionsFragment, ProfileFragment
│   ├── subscription → SubscriptionFormActivity, SubscriptionDetailsActivity, CatalogBottomSheet
│   └── category     → CategoriesActivity
└── util             → SessionManager, formatação de moeda e data
```

Arquivo novo vai para o pacote da sua camada. Se não couber em nenhum, pergunte antes de criar pacote novo.

## Modelo de dados

| Entidade | Campos principais |
|---|---|
| `User` (`users`) | id, name, email (único), passwordHash, salt, onboardingCompleted, createdAt |
| `Category` (`categories`) | id, name, isDefault, userId (nulo nas padrão) |
| `Subscription` (`subscriptions`) | id, userId, categoryId, serviceName, amountCents, variableAmount, period, startDate, billingDay, status, participants, activeSince, createdAt |
| `Payment` (`payments`) | id, subscriptionId, referenceMonth (`AAAA-MM`), amountCents, dueDate, paidDate, status — índice único em (subscriptionId, referenceMonth) |

- Valores monetários: `Long` em **centavos** no banco; formatação em BRL só na UI (`util`).
- Datas: `LocalDate` com `TypeConverter` (epochDay). Use `java.time` (core library desugaring se necessário para minSdk 24).
- Enums persistidos pelo `name`, via `TypeConverter`.
- Mudou uma entidade? Suba a `version` do `AppDatabase` e acrescente uma `AutoMigration(from, to)`. Renomear ou apagar coluna/tabela exige uma `AutoMigrationSpec`. Nunca usar `fallbackToDestructiveMigration`: os dados de teste devem ser preservados.
- Commite a pasta `app/schemas`: é dela que o Room gera as migrations.

## Regras de negócio

- **Mensalidade** = valor ÷ meses do período (mensal 1, trimestral 3, semestral 6, anual 12).
- **Custo real** = mensalidade ÷ participantes (mínimo 1).
- **Próximo vencimento**: próxima data com o dia de cobrança; dia maior que o último do mês cai no último dia.
- **Pagamento presumido**: ao abrir a `MainActivity`, para cada assinatura ativa e cada competência cujo dia de cobrança já passou sem pagamento, gera um. Valor fixo → `PAID`; valor variável → `PENDING` com o último valor pago como estimativa. Deve ser **idempotente** (abrir o app duas vezes não duplica).
- Pendente vira pago com confirmação do usuário, podendo ajustar o valor.
- **Exclusão** de assinatura bloqueada se houver pagamentos; a alternativa é cancelar. Cancelada não gera novos pagamentos.
- **Categoria**: nome único por usuário; padrão não editável nem excluível; exclusão bloqueada se tiver assinaturas.
- **Sugestões** (calculadas na hora, sem tabela): 2+ ativas na mesma categoria; em teste há mais de 30 dias; pagamento pendente há mais de 7 dias.
- **Catálogo**: preenche nome, categoria, período e `variableAmount`. Nunca preço (muda com o tempo).
- **Senha**: hash SHA-256 com salt aleatório por usuário. Nunca salvar ou logar senha em texto puro.
- Todo dado é filtrado pelo usuário logado (`userId` da sessão).

## Validações de formulário

- Erros aparecem **no campo**, via `TextInputLayout.error`, não só em Toast.
- Validação mora em `domain/rules/Validators` (testável); a Activity só exibe o resultado.
- Assinatura: nome obrigatório; valor > 0; dia de cobrança 1–31; participantes ≥ 1; data de início não futura; período e status válidos.
- Cadastro: nome ≥ 3 caracteres; e-mail válido e não cadastrado; senha ≥ 6; confirmação igual.

## UI e ciclo de vida

- Um layout XML por tela; itens de lista em `item_*.xml`.
- Textos em `strings.xml`, cores em `colors.xml`, dimensões em `dimens.xml`. Nada de string ou cor fixa no layout ou no Kotlin.
- Tema único `Theme.Rekly` (Material 3) e mesma toolbar em todas as telas.
- Estado de formulário no ViewModel para sobreviver à rotação.
- Listas recarregam observando LiveData do Room, não com chamadas manuais em `onResume`.
- Sessão checada em `onStart` das telas autenticadas; sem sessão, volta ao Login limpando a pilha.
- Passagem de dados entre Activities por `Intent` extras com chaves em `companion object`, só com ids (nunca objetos inteiros).
- Exclusões e cancelamentos sempre com diálogo de confirmação.

## Convenções de código

- Estilo oficial Kotlin (`kotlin.code.style=official`), indentação de 4 espaços.
- **Código em inglês**: classes, funções, variáveis, ids de layout, chaves do `strings.xml`, tabelas e colunas do banco, nomes de testes e comentários (`Subscription`, `monthlyCents`, `fabNewSubscription`).
- **Textos da interface em português**, sempre no `strings.xml`. Dados exibidos ao usuário, como os nomes das categorias padrão, também ficam em português.
- **Mensagens de commit em português** (ver Git).
- Nomes: `XxxActivity`, `XxxFragment`, `XxxViewModel`, `XxxRepository`, `XxxDao`, `activity_xxx.xml`, `fragment_xxx.xml`, `item_xxx.xml`.
- `val` por padrão; evite `!!`.
- Funções curtas, um propósito cada. Comentários explicam o *porquê*, não o *o quê*.
- Sem código morto, `println` ou `Log.d` esquecidos no merge.

## Testes

- Testes unitários (JUnit) para tudo em `domain/rules`: cálculos, validações, geração de pagamentos e sugestões.
- Nome do teste descreve o comportamento: `annual_generates_one_payment_per_year`.
- Rodar `./gradlew test` antes de fazer push na `develop`.

## Git

- `main`: só versões estáveis, recebe merge da `develop` (ex.: ao fim de cada etapa).
- `develop`: onde o trabalho acontece. Os dois integrantes fazem commits diários nela.
- Antes de começar, `git pull` na `develop` para evitar conflitos.
- Cada integrante commita da própria conta do GitHub.
- **Conventional Commits**, em português, minúsculas, imperativo, um assunto por commit:

```
feat(assinaturas): adiciona formulário de criação e edição
fix(pagamentos): evita pagamento duplicado na mesma competência
test(rules): cobre cálculo de custo real com rateio
refactor(ui): extrai adapter da lista de assinaturas
docs(readme): adiciona prints das telas
chore(gradle): adiciona dependências do room
```

- Tipos: `feat`, `fix`, `refactor`, `style`, `test`, `docs`, `chore`.
- Commits pequenos e frequentes; nada de um commit gigante com a feature inteira.
- Não commitar `build/`, `.idea/workspace.xml`, `local.properties` nem arquivos `.apk`.

## Definição de pronto

Uma feature só está pronta quando:

1. Compila e roda no emulador sem crash, inclusive girando a tela.
2. Validações mostram erro no campo.
3. Regras de negócio têm teste unitário passando.
4. Strings e cores estão nos recursos.
5. Commits seguem Conventional Commits.
6. Commitado na `develop`; vai para a `main` quando a versão estiver estável.

## Para agentes de IA

- Siga este arquivo acima de qualquer padrão genérico.
- Antes de mudanças grandes, proponha o plano e espere confirmação.
- Mantenha o escopo: nada de bibliotecas ou features fora da lista acima.
- Gere código que a dupla consiga explicar linha a linha na apresentação.
- Ao sugerir commits, use o padrão da seção Git.
