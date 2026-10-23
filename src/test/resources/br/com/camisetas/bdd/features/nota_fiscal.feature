# language: pt
@nota_fiscal
Funcionalidade: Emissao da nota fiscal do lote
  Para faturar a producao
  Como analista fiscal da Camisetas S.A.
  Quero emitir a nota fiscal do lote com o total de camisetas por tamanho e por cor

  Contexto:
    Dado que a empresa abriu o lote "LOTE-2024-001" fabricado em "2024-05-10" com capacidade para 100 camisetas
    E que o lote possui os seguintes itens cadastrados:
      | codigo | tamanho | cor    | quantidade |
      | IT-01  | P       | Branco | 20         |
      | IT-02  | P       | Preto  | 10         |
      | IT-03  | M       | Branco | 30         |
      | IT-04  | M       | Azul   | 15         |
      | IT-05  | G       | Preto  | 20         |
      | IT-06  | G       | Azul   | 5          |

  @emissao
  Cenario: Emitir a nota fiscal com numero, data e vinculo ao lote
    Quando o faturamento emite a nota fiscal "NF-0001" em "2024-05-11"
    Entao o numero da nota fiscal deve ser "NF-0001"
    E a data de emissao da nota fiscal deve ser "2024-05-11"
    E a nota fiscal deve estar vinculada ao lote "LOTE-2024-001"
    E a nota fiscal deve totalizar 100 camisetas

  @totalizacao
  Cenario: Contabilizar a quantidade de camisetas por tamanho
    Quando o faturamento emite a nota fiscal "NF-0001" em "2024-05-11"
    Entao a nota fiscal deve detalhar as quantidades por tamanho:
      | tamanho | quantidade |
      | P       | 30         |
      | M       | 45         |
      | G       | 25         |

  @totalizacao
  Cenario: Contabilizar a quantidade de camisetas por cor
    Quando o faturamento emite a nota fiscal "NF-0001" em "2024-05-11"
    Entao a nota fiscal deve detalhar as quantidades por cor:
      | cor    | quantidade |
      | Branco | 50         |
      | Preto  | 30         |
      | Azul   | 20         |

  @totalizacao
  Esquema do Cenario: Consultar o total faturado de cada tamanho
    Quando o faturamento emite a nota fiscal "NF-0001" em "2024-05-11"
    Entao a nota fiscal deve registrar <quantidade> camisetas do tamanho "<tamanho>"

    Exemplos:
      | tamanho | quantidade |
      | P       | 30         |
      | M       | 45         |
      | G       | 25         |

  @totalizacao
  Esquema do Cenario: Consultar o total faturado de cada cor
    Quando o faturamento emite a nota fiscal "NF-0001" em "2024-05-11"
    Entao a nota fiscal deve registrar <quantidade> camisetas da cor "<cor>"

    Exemplos:
      | cor    | quantidade |
      | Branco | 50         |
      | Preto  | 30         |
      | Azul   | 20         |

  @impressao
  Cenario: Imprimir o detalhamento da nota fiscal para conferencia
    Quando o faturamento emite a nota fiscal "NF-0001" em "2024-05-11"
    Entao o detalhamento impresso da nota fiscal deve conter "NOTA FISCAL NF-0001"
    E o detalhamento impresso da nota fiscal deve conter "Total 100 camisetas"
    E o detalhamento impresso da nota fiscal deve conter "Tamanho M: 45"
    E o detalhamento impresso da nota fiscal deve conter "Cor Branco: 50"

  @emissao
  Cenario: Emitir a nota fiscal do lote na data de hoje
    Quando o faturamento emite a nota fiscal do lote na data de hoje
    Entao o numero da nota fiscal deve ser "NF-LOTE-2024-001"
    E a nota fiscal deve totalizar 100 camisetas

  @regra_negocio
  Cenario: Recusar emissao com data anterior a fabricacao do lote
    Quando o faturamento tenta emitir a nota fiscal "NF-0002" em "2024-05-09"
    Entao a recusa deve ser do tipo "NotaFiscalInvalidaException"
    E a operacao deve ser recusada com a mensagem "Data de emissao (2024-05-09) nao pode ser anterior a fabricacao (2024-05-10)."
    E nenhuma nota fiscal deve ter sido emitida

  @regra_negocio
  Cenario: Recusar emissao sem numero de nota fiscal
    Quando o faturamento tenta emitir a nota fiscal "" em "2024-05-11"
    Entao a operacao deve ser recusada com a mensagem "Numero da nota fiscal e obrigatorio."
    E nenhuma nota fiscal deve ter sido emitida
