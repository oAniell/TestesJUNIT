# language: pt
@fluxo_completo
Funcionalidade: Fluxo completo de producao de camisetas
  Para atender o pedido do cliente de ponta a ponta
  Como gestor de producao da Camisetas S.A.
  Quero acompanhar o lote da abertura ate a liberacao para despacho

  Regra: Um lote percorre producao, faturamento e expedicao nessa ordem

    @ponta_a_ponta
    Cenario: Produzir, faturar e despachar um lote de 60 camisetas
      Dado que a empresa abriu o lote "LOTE-2024-050" fabricado em "2024-06-01" com capacidade para 60 camisetas
      Quando a producao cadastra os seguintes itens:
        | codigo | tamanho | cor    | quantidade |
        | IT-01  | P       | Branco | 10         |
        | IT-02  | M       | Preto  | 20         |
        | IT-03  | G       | Azul   | 30         |
      E o faturamento emite a nota fiscal "NF-2024-050" em "2024-06-03"
      E a expedicao recebeu o lote faturado
      E a expedicao prepara o lote para envio
      Entao o lote deve estar completo
      E a nota fiscal deve totalizar 60 camisetas
      E a nota fiscal deve registrar 10 camisetas do tamanho "P"
      E a nota fiscal deve registrar 20 camisetas do tamanho "M"
      E a nota fiscal deve registrar 30 camisetas do tamanho "G"
      E a nota fiscal deve registrar 10 camisetas da cor "Branco"
      E a nota fiscal deve registrar 20 camisetas da cor "Preto"
      E a nota fiscal deve registrar 30 camisetas da cor "Azul"
      E um codigo de validacao deve ser gerado para o lote
      E o lote deve estar embalado
      E o lote deve estar marcado como preparado para despacho
      E a etiqueta de envio deve conter o codigo de validacao do lote

    @ponta_a_ponta
    Cenario: Faturar e despachar apenas o que foi efetivamente produzido
      Dado que a empresa abriu o lote "LOTE-2024-051" fabricado em "2024-06-01" com capacidade para 100 camisetas
      Quando a producao cadastra o item "IT-01" de tamanho "M" e cor "Azul" com 40 camisetas
      E o faturamento emite a nota fiscal "NF-2024-051" em "2024-06-02"
      E a expedicao recebeu o lote faturado
      E a expedicao prepara o lote para envio
      Entao o lote nao deve estar completo
      E o lote deve ter capacidade restante de 60 camisetas
      E a nota fiscal deve totalizar 40 camisetas
      E a nota fiscal deve registrar 0 camisetas do tamanho "P"
      E a nota fiscal deve registrar 0 camisetas da cor "Branco"
      E o lote deve estar marcado como preparado para despacho
