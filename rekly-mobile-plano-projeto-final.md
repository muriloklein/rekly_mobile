# Rekly Mobile — Plano do Projeto Final

## Visão geral

O Rekly Mobile é um app Android nativo (Kotlin + XML) que traz para o celular o núcleo da versão web: cadastrar gastos recorrentes, ver quanto eles custam por mês e não perder vencimentos. Os dados ficam no próprio aparelho com Room (SQLite), sem depender de servidor.

O repositório `rekly_mobile` hoje tem só o projeto vazio do Android Studio (AGP 9.2, minSdk 24, ViewBinding ligado) e 2 commits na `main`. Tudo abaixo parte desse ponto.

**O que vem da versão web:**

- Usuário com login e cadastro (local)
- Categorias (padrão do app + criadas pelo usuário)
- Assinaturas e contas: nome, valor, período (mensal, trimestral, semestral, anual), dia de cobrança, status (ativo, teste, cancelado) e participantes para rateio
- Pagamentos por assinatura, agora gerados automaticamente
- Dashboard do mês atual: total mensal, custo real após rateio, próximos vencimentos
- Sugestões de economia

**O que o mobile faz melhor que a web (menos trabalho manual):**

- **Catálogo de serviços e contas:** streaming, música, internet, celular, água, luz, gás, aluguel, condomínio, academia e outros. Tocar no item abre o formulário já com nome, categoria e período preenchidos.
- **Onboarding em checklist:** no primeiro acesso o usuário marca o que paga e as assinaturas são criadas em lote; só falta informar valor e dia.
- **Pagamento presumido:** quando passa o dia de cobrança, o app gera o pagamento do mês sozinho. Valor fixo (Netflix) entra como pago; valor variável (água, luz) entra como pendente para confirmar o valor real com um toque.

**O que fica de fora:** recuperação de senha por e-mail, logs e triggers de auditoria, preferências detalhadas de notificação, múltiplas moedas (fica só BRL), filtro histórico por mês no dashboard, logout por inatividade e importação/exportação de CSV. Open Finance fica citado como evolução futura: exige instituição autorizada ou agregador pago com backend próprio.

**Por que Room:** funciona offline, não depende do deploy da versão web estar no ar no dia da apresentação, e é o conteúdo mais cobrado na disciplina (entidades, DAOs, relacionamentos). Retrofit ou Firebase podem entrar como extra no fim, se sobrar tempo.

## Requisitos da disciplina

Cada requisito do enunciado tem um lugar definido no app; a coluna da direita é o que mostrar na apresentação.

| Requisito | Onde é atendido | O que mostrar |
| --- | --- | --- |
| Kotlin + Android Studio | Projeto inteiro | Estrutura de pacotes |
| Estrutura de projeto Android | Pacotes `data`, `domain`, `ui`, `util`; Manifest; Gradle com version catalog | Árvore do projeto |
| Activities (mín. 4) | 7 Activities: Login, Cadastro, Onboarding, Main, AssinaturaForm, AssinaturaDetalhe, Categorias | Manifest |
| Fragments | 3 Fragments na `MainActivity`: Resumo, Assinaturas, Perfil | Troca pela barra inferior |
| Layouts XML | Um layout por tela, `item_assinatura.xml` para lista, tema Material 3 | Layouts no editor |
| Ciclo de vida | ViewModel sobrevive à rotação; listas observam LiveData do Room e se atualizam sozinhas; sessão checada em `onStart` | Girar a tela sem perder o formulário |
| Login funcional | `LoginActivity` valida contra o Room, sessão em SharedPreferences | Login errado e certo |
| Navegação e layout consistentes | Mesmo tema, mesma toolbar, mesmas cores da web | Percorrer o app |
| Formulários e validações | Cadastro, login, assinatura, categoria | Erros nos campos |
| Regras de negócio | Mensalidade, custo real, bloqueio de exclusão, sugestões | Dashboard e detalhe |
| Objetos de domínio | `data class` para entidades + enums `Periodo` e `Status` | Código |
| Persistência | Room com 4 tabelas e relacionamentos | App Inspection > Database Inspector |
| CRUD | Assinaturas (completo) e Categorias (completo) | Criar, listar, editar, excluir |
| GitHub + branches | Branches `main` e `develop`; commits diários dos dois na `develop`; merge na `main` só de versões estáveis | Gráfico de branches e autores dos commits |
| Conventional Commits | Todos os commits | `git log --oneline` |
| README + prints | `README.md` e pasta `prints/` | Repositório |

## Arquitetura

O app segue MVVM em três camadas: a tela (Activity/Fragment) observa um ViewModel, que chama um Repository, que fala com os DAOs do Room. Nenhuma tela acessa o banco direto.

```
com.example.rekly
├── data
│   ├── local        → AppDatabase, DAOs, Converters
│   ├── entity       → Usuario, Categoria, Assinatura, Pagamento
│   └── repository   → UsuarioRepository, CategoriaRepository, AssinaturaRepository, PagamentoRepository
├── domain
│   ├── model        → enums Periodo, StatusAssinatura, StatusPagamento; Resultado e ErroRegra
│   ├── catalogo     → CatalogoServicos (lista fixa de serviços e contas)
│   └── rules        → CalculoGastos, Validadores, GeradorPagamentos, SugestaoEconomia
├── ui
│   ├── auth         → LoginActivity, CadastroActivity
│   ├── onboarding   → OnboardingActivity
│   ├── main         → MainActivity + fragments resumo, assinaturas, perfil
│   ├── assinatura   → AssinaturaFormActivity, AssinaturaDetalheActivity, CatalogoBottomSheet
│   └── categoria    → CategoriasActivity
└── util             → SessionManager, formatação de moeda e data
```

**Dependências a adicionar:** Room (runtime, ktx, compiler via KSP), Lifecycle (viewmodel-ktx, livedata-ktx), Coroutines e RecyclerView. Atenção: o projeto usa AGP 9 com Kotlin embutido, então a versão do plugin KSP precisa ser compatível com ele. Vale testar isso no primeiro dia.

### Tabelas do Room

| Entidade | Campos principais | Relação |
| --- | --- | --- |
| `Usuario` | id, nome, email (único), senhaHash, salt, onboardingConcluido, criadoEm | — |
| `Categoria` | id, nome (sem diferenciar maiúsculas), isPadrao, usuarioId (nulo nas padrão) | N:1 Usuario; único por usuário + nome |
| `Assinatura` | id, usuarioId, categoriaId, nomeServico, valorCentavos, valorVariavel, periodo, dataInicio, diaCobranca, status, participantes, ativaDesde, criadoEm | N:1 Usuario e Categoria (categoria com RESTRICT) |
| `Pagamento` | id, assinaturaId, competencia (AAAA-MM), valorCentavos, dataVencimento, dataPagamento, status (PAGO, PENDENTE) | N:1 Assinatura (RESTRICT); único por assinatura + competência |

Categorias padrão (Streaming, Música, Moradia, Telecom, Saúde e bem-estar, Seguros, Outros) têm ids fixos de 1 a 7 (`CategoriasPadrao`) e entram pelo callback `onOpen` do banco com `INSERT OR IGNORE`. O `onCreate` não serviria: ele não roda em bancos que chegam por AutoMigration, então quem já tinha o app instalado ficaria sem as categorias.

**Valores em dinheiro** são guardados em centavos (`Long`), nunca em `Double`, para o rateio e a mensalidade não acumularem erro de arredondamento. A formatação em reais fica só na tela.

**Assinaturas incompletas:** as criadas pelo onboarding não têm `valorCentavos`, `dataInicio` nem `diaCobranca` (campos nulos). `Assinatura.isCompleta()` indica se já dá para usá-las; incompletas ficam fora dos totais e da geração de pagamentos e mostram o selo "falta valor".

**Erros de regra:** os repositórios devolvem `Resultado.Sucesso` ou `Resultado.Falha(ErroRegra)`, e a tela transforma o `ErroRegra` em mensagem do `strings.xml`. As chaves estrangeiras com `RESTRICT` reforçam no próprio banco os bloqueios de exclusão.

Mudanças nas tabelas usam **AutoMigrations** do Room para manter os dados de teste: a cada mudança, a `version` do banco sobe e entra uma `AutoMigration(from, to)`. O Room exporta a estrutura de cada versão para `app/schemas`, que vai no commit.

### Regras de negócio portadas da web

- **Mensalidade** = valor ÷ meses do período (1, 3, 6 ou 12), em centavos, arredondando meio centavo para cima.
- **Custo real** = mensalidade ÷ participantes (mínimo 1).
- **Vencimentos** seguem o ciclo do período a partir do mês da data de início (anual vence uma vez por ano, trimestral a cada 3 meses); um vencimento anterior à data de início é ignorado. Dia 31 em mês curto cai no último dia do mês.
- **Próximo vencimento** = primeiro vencimento do ciclo a partir de hoje, inclusive. Cancelada não tem próximo vencimento.
- **Pagamento presumido:** ao abrir o app, para cada assinatura ativa e completa e cada competência do ciclo cujo vencimento já chegou (inclusive hoje) e ainda não tem pagamento, cria um. Valor fixo → `PAGO` com o valor da assinatura. Valor variável → `PENDENTE` com o último valor pago como estimativa (ou o valor da assinatura, se não houver histórico). O índice único por assinatura + competência impede duplicar.
- **Sem pagamentos retroativos:** `ativaDesde` guarda quando a assinatura passou a ser ativa (ao criar, ao converter um teste ou ao reativar uma cancelada), e só são gerados pagamentos a partir desse mês. Cadastrar uma Netflix assinada em 2022 não cria dezenas de pagamentos.
- **Assinatura em teste** não gera pagamento e não entra no total mensal; o resumo mostra as assinaturas em teste separadas.
- **Confirmar pagamento:** um pendente vira pago com um toque, permitindo ajustar o valor.
- **Exclusão bloqueada** se a assinatura tiver pagamentos; a alternativa é cancelar, que interrompe a geração de novos pagamentos.
- **Categoria** não pode ser excluída se tiver assinaturas, nem repetir o nome de uma categoria padrão ou do próprio usuário (sem diferenciar maiúsculas). Categorias padrão não são editáveis nem excluíveis.
- **Sugestões**, calculadas na hora: duas ou mais assinaturas ativas na mesma categoria (sobreposição); assinatura em teste há mais de 30 dias ("vai começar a cobrar?"); pagamento pendente há mais de 7 dias.
- **Catálogo:** só preenche nome, categoria, período e se o valor é variável. Não traz preço, porque preços mudam e variam por plano.
- **Senha** guardada como hash SHA-256 com salt, nunca em texto puro.

Essas funções ficam em `domain/rules` como funções puras, o que permite testes unitários simples em `src/test`.

## Telas e navegação

São 7 Activities e 3 Fragments. A `MainActivity` é o centro: tudo parte dela e volta para ela.

&#91;embedded content: navegação · 7 Activities, 3 Fragments\]

Com sessão ativa o app abre direto na MainActivity; o botão Sair, no Perfil, limpa a sessão e volta ao Login.

| Tela | Tipo | Conteúdo | Validações e regras |
| --- | --- | --- | --- |
| Login | Activity (launcher) | E-mail, senha, botão Entrar, link para cadastro | E-mail em formato válido; senha não vazia; credenciais conferidas no Room; se já há sessão, pula direto para a Main |
| Cadastro | Activity | Nome, e-mail, senha, confirmar senha | Nome ≥ 3 letras; e-mail válido e não cadastrado; senha ≥ 6 caracteres; confirmação igual |
| Onboarding | Activity | Chips do catálogo agrupados por categoria ("O que você paga todo mês?") e botão Concluir | Aparece só no primeiro acesso; cria as assinaturas marcadas sem valor, para completar depois; pode ser pulado |
| Main | Activity + BottomNavigation | Hospeda os 3 Fragments e o botão + flutuante | Sem sessão, volta para o Login; ao abrir, roda a geração de pagamentos presumidos |
| Resumo | Fragment | Total mensal, custo real, gasto por categoria, próximos 7 dias, pendentes a confirmar, sugestões | Totais consideram só assinaturas ativas e completas; as em teste aparecem separadas |
| Assinaturas | Fragment | RecyclerView com busca por nome e filtro por status; selo "falta valor" nas criadas pelo onboarding | Toque abre o detalhe |
| Perfil | Fragment | Nome e e-mail, Categorias, Sair | Sair limpa a sessão e fecha a pilha |
| Form de assinatura | Activity + BottomSheet do catálogo | Ao criar, abre primeiro o catálogo (ou "Outro"); depois nome, categoria, valor, valor variável, período, data de início, dia de cobrança, status, participantes | Campos obrigatórios; valor > 0; dia entre 1 e 31; participantes ≥ 1; data não futura |
| Detalhe da assinatura | Activity | Dados, mensalidade, custo real, próximo vencimento, histórico de pagamentos | Confirmar pendente ajustando valor; editar; cancelar; excluir com diálogo de confirmação e bloqueio se houver pagamentos |
| Categorias | Activity | Lista com padrão e pessoais, diálogo para criar ou renomear | Nome obrigatório e único; padrão não editável; exclusão bloqueada se houver assinaturas |

O estado do formulário fica no ViewModel, então girar a tela não apaga o que foi digitado. Esse é o exemplo de ciclo de vida para a apresentação.

## Git, branches e divisão da dupla

O repositório usa só duas branches:

- **`develop`**: onde o trabalho acontece. Os dois fazem commits diários nela.
- **`main`**: só versões estáveis. Recebe merge da `develop` quando o app compila, roda sem crash e as features do momento estão completas.

Antes de começar o dia, cada um faz `git pull` na `develop` para pegar o que o outro enviou, evitando conflitos. Cada um commita da própria conta do GitHub, para o histórico mostrar os dois autores.

| # | Tarefa | Responsável | Depende de | Entrega |
| --- | --- | --- | --- | --- |
| 1 | Setup do projeto | Guilherme | — | Dependências, KSP, pacotes, tema e cores, `CLAUDE.md`, branch `develop`, `AppDatabase` com a entidade `Usuario` |
| 2 | Banco de dados (Room) | Murilo | 1 | Entidades `Categoria`, `Assinatura` e `Pagamento` com DAOs, repositórios, regras de cálculo e testes |
| 3 | Autenticação | Guilherme | 1 | Login, Cadastro, SessionManager |
| 4 | Navegação principal | Murilo | 1 | MainActivity, BottomNavigation, 3 Fragments vazios |
| 5 | Catálogo de serviços | Murilo | 2 | Lista de serviços e contas + BottomSheet de escolha |
| 6 | CRUD de assinaturas | Guilherme | 4, 5 | Lista, formulário com catálogo, detalhe, editar, cancelar, excluir |
| 7 | CRUD de categorias | Murilo | 2 | Tela de categorias com CRUD completo |
| 8 | Onboarding | Guilherme | 3, 5 | Checklist do primeiro acesso, criação em lote |
| 9 | Pagamentos | Guilherme | 6 | Geração presumida, confirmação de pendentes, histórico no detalhe |
| 10 | Dashboard | Murilo | 9 | Fragment Resumo com totais, vencimentos, pendentes e sugestões |
| 11 | Notificações | Murilo | 9 | Lembrete diário com WorkManager (extra) |
| 12 | README e prints | Os dois | todas | README completo e pasta `prints/` |

Depois do setup, as tarefas 2, 3 e 4 andam em paralelo; depois 5 e 7; depois 6 e 8. O `Usuario` fica no setup para a autenticação não esperar pelo banco das outras entidades; cada nova entidade só é acrescentada na lista do `AppDatabase`.

### Conventional Commits

Formato: `tipo(escopo): descrição no imperativo`, em português e minúsculas. Commits pequenos, um assunto por commit.

```
chore(gradle): adiciona dependências do room e lifecycle
feat(db): cria entidades usuario, categoria e assinatura
feat(db): adiciona dao de assinaturas com consultas por usuário
test(rules): cobre cálculo de mensalidade e custo real
feat(auth): implementa tela de login com validação de campos
feat(assinaturas): adiciona formulário de criação e edição
fix(assinaturas): impede dia de cobrança fora de 1 a 31
refactor(ui): extrai adapter da lista de assinaturas
style(layout): ajusta espaçamentos do card de resumo
docs(readme): adiciona instruções de execução e prints
```

Tipos usados: `feat`, `fix`, `refactor`, `style`, `test`, `docs`, `chore`.

## Etapas e checklist de entrega

O trabalho cabe em 4 etapas; o app precisa estar completo no fim da etapa 3, deixando a 4 só para polimento e apresentação. Falta definir a data de entrega para transformar etapas em semanas.

1. **Base** — tarefas 1, 2 e 4. Ao fim: app abre, banco criado, navegação vazia funcionando.
2. **Núcleo** — tarefas 3, 5, 6 e 7. Ao fim: login real, catálogo e os dois CRUDs completos.
3. **Menos trabalho manual** — tarefas 8, 9 e 10. Ao fim: onboarding, pagamentos automáticos e dashboard.
4. **Entrega** — tarefas 11 (se der tempo) e 12, testes no aparelho, ensaio da apresentação.

O fim de cada etapa é um bom momento para fazer o merge da `develop` na `main`.

### Checklist

- [ ] Professor adicionado como colaborador do repositório
- [ ] Branch `develop` criada; `main` recebe só merges da `develop`
- [ ] Mínimo de 4 Activities declaradas no Manifest (plano tem 7)
- [ ] CRUD de assinaturas funcionando de ponta a ponta
- [ ] Criar assinatura pelo catálogo em menos de 30 segundos
- [ ] Pagamentos gerados ao abrir o app, sem duplicar ao abrir de novo
- [ ] Validações mostrando erro no próprio campo (`TextInputLayout.error`)
- [ ] Dados persistem após fechar e reabrir o app
- [ ] Rotação de tela não perde dados do formulário
- [ ] Todos os commits em Conventional Commits, dos dois autores
- [ ] Versão final da `develop` mesclada na `main`
- [ ] README com nome, integrantes, descrição, tecnologias e instruções de execução
- [ ] Pasta `prints/` com uma imagem por tela, referenciada no README
- [ ] Roteiro da apresentação: cadastro → onboarding → completar valores → dashboard → confirmar conta de luz → editar → excluir bloqueado → rotação → Database Inspector → histórico do Git
