# language: pt
@lote
Funcionalidade: Abertura do lote e cadastro dos itens de producao
  Para controlar a producao de camisetas
  Como analista de producao da Camisetas S.A.
  Quero abrir lotes identificados e cadastrar os itens produzidos com tamanho e cor

  Contexto:
    Dado que a empresa abriu o lote "LOTE-2024-001" fabricado em "2024-05-10" com capacidade para 100 camisetas

  @abertura
  Cenario: Abrir um lote de producao com numero, data de fabricacao e capacidade
    Entao o numero do lote deve ser "LOTE-2024-001"
    E a data de fabricacao do lote deve ser "2024-05-10"
    E a capacidade total do lote deve ser de 100 camisetas
    E o lote deve estar vazio

  @cadastro
  Cenario: Cadastrar um item com tamanho e cor no lote
    Quando a producao cadastra o item "IT-01" de tamanho "M" e cor "Azul" com 25 camisetas
    Entao o lote deve conter 1 registros de item
    E o lote deve ter 25 camisetas cadastradas
    E o lote deve ter capacidade restante de 75 camisetas
    E o item "IT-01" deve constar no lote com tamanho "M" e cor "Azul"
    E o lote nao deve estar completo

  @cadastro
  Cenario: Cadastrar varios itens de tamanhos e cores diferentes
    Quando a producao cadastra os seguintes itens:
      | codigo | tamanho | cor    | quantidade |
      | IT-01  | P       | Branco | 20         |
      | IT-02  | P       | Preto  | 10         |
      | IT-03  | M       | Branco | 30         |
      | IT-04  | M       | Azul   | 15         |
      | IT-05  | G       | Preto  | 20         |
      | IT-06  | G       | Azul   | 5          |
    Entao o lote deve conter 6 registros de item
    E o lote deve ter 100 camisetas cadastradas
    E o lote deve estar completo
    E o lote deve ter capacidade restante de 0 camisetas

  @cadastro
  Cenario: Remover um item cadastrado por engano
    Quando a producao cadastra o item "IT-01" de tamanho "G" e cor "Preto" com 40 camisetas
    E a producao remove o item "IT-01" do lote
    Entao o lote deve estar vazio
    E o lote deve ter 0 camisetas cadastradas

  @regra_negocio
  Cenario: Recusar cadastro acima da capacidade do lote
    Quando a producao cadastra o item "IT-01" de tamanho "P" e cor "Branco" com 90 camisetas
    E a producao tenta cadastrar o item "IT-02" de tamanho "G" e cor "Azul" com 20 camisetas
    Entao a recusa deve ser do tipo "CapacidadeLoteExcedidaException"
    E a operacao deve ser recusada com a mensagem "Capacidade do lote LOTE-2024-001 excedida. Total permitido: 100, solicitado: 110"
    E o lote deve ter 90 camisetas cadastradas

  @regra_negocio
  Cenario: Recusar dois itens com o mesmo codigo no lote
    Quando a producao cadastra o item "IT-01" de tamanho "P" e cor "Branco" com 10 camisetas
    E a producao tenta cadastrar o item "IT-01" de tamanho "G" e cor "Azul" com 5 camisetas
    Entao a recusa deve ser do tipo "ItemDuplicadoException"
    E a operacao deve ser recusada com a mensagem "Item ja cadastrado no lote LOTE-2024-001: IT-01"
    E o lote deve conter 1 registros de item

  @regra_negocio
  Esquema do Cenario: Recusar item com tamanho ou cor fora do catalogo
    Quando a producao tenta cadastrar o item "IT-99" de tamanho "<tamanho>" e cor "<cor>" com 1 camisetas
    Entao a operacao deve ser recusada com a mensagem "<mensagem>"
    E o lote deve estar vazio

    Exemplos:
      | tamanho | cor      | mensagem                                      |
      | XG      | Branco   | Tamanho invalido: XG. Use P, M ou G.          |
      | PP      | Azul     | Tamanho invalido: PP. Use P, M ou G.          |
      | M       | Verde    | Cor invalida: Verde. Use Branco, Preto ou Azul. |
      | G       | Vermelho | Cor invalida: Vermelho. Use Branco, Preto ou Azul. |

  @regra_negocio
  Esquema do Cenario: Recusar abertura de lote com dados invalidos
    Quando a empresa tenta abrir o lote "<numero>" fabricado em "2024-05-10" com capacidade para <capacidade> camisetas
    Entao a operacao deve ser recusada com a mensagem "<mensagem>"

    Exemplos:
      | numero        | capacidade | mensagem                                                       |
      |               | 10         | Numero do lote e obrigatorio.                                  |
      | LOTE-2024-002 | 0          | Total de itens do lote deve ser maior que zero. Informado: 0   |
      | LOTE-2024-003 | -5         | Total de itens do lote deve ser maior que zero. Informado: -5  |
