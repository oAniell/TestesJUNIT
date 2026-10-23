# language: pt
@despacho
Funcionalidade: Preparacao do lote para despacho
  Para liberar o lote ao transportador
  Como analista de expedicao da Camisetas S.A.
  Quero gerar o codigo de validacao, embalar o lote e marca-lo como preparado para despacho

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
    E que o faturamento emitiu a nota fiscal "NF-0001" em "2024-05-11"
    E que a expedicao recebeu o lote faturado

  @codigo_validacao
  Cenario: Gerar o codigo de validacao do lote
    Quando a expedicao gera o codigo de validacao do lote
    Entao um codigo de validacao deve ser gerado para o lote
    E o codigo de validacao deve seguir o padrao "VAL-LOTE-2024-001-NF-0001-[0-9]{4}"
    E o lote nao deve estar embalado
    E o lote nao deve estar marcado como preparado para despacho

  @embalagem
  Cenario: Embalar o lote depois de gerar o codigo de validacao
    Quando a expedicao gera o codigo de validacao do lote
    E a expedicao embala o lote
    Entao o lote deve estar embalado
    E o lote nao deve estar marcado como preparado para despacho

  @liberacao
  Cenario: Marcar o lote como preparado para despacho
    Quando a expedicao gera o codigo de validacao do lote
    E a expedicao embala o lote
    E a expedicao marca o lote como preparado para despacho
    Entao o lote deve estar marcado como preparado para despacho
    E a data de preparacao do despacho deve ser a data de hoje
    E a etiqueta de envio deve conter "ENVIO | Lote LOTE-2024-001 | NF NF-0001 | 100 camisetas"
    E a etiqueta de envio deve conter o codigo de validacao do lote

  @liberacao
  Cenario: Preparar o lote para envio em uma unica operacao
    Quando a expedicao prepara o lote para envio
    Entao um codigo de validacao deve ser gerado para o lote
    E o lote deve estar embalado
    E o lote deve estar marcado como preparado para despacho
    E a operacao deve ser concluida sem erros

  @regra_negocio
  Cenario: Recusar embalagem antes da geracao do codigo de validacao
    Quando a expedicao tenta embalar o lote
    Entao a recusa deve ser do tipo "DespachoInvalidoException"
    E a operacao deve ser recusada com a mensagem "Gere o codigo de validacao antes de embalar o lote."
    E o lote nao deve estar embalado
    E nenhum codigo de validacao deve ter sido gerado

  @regra_negocio
  Cenario: Recusar liberacao de lote que ainda nao foi embalado
    Quando a expedicao gera o codigo de validacao do lote
    E a expedicao tenta marcar o lote como preparado para despacho
    Entao a operacao deve ser recusada com a mensagem "Embale o lote antes de marca-lo como preparado para despacho."
    E o lote nao deve estar marcado como preparado para despacho

  @regra_negocio
  Cenario: Recusar segunda geracao do codigo de validacao
    Quando a expedicao gera o codigo de validacao do lote
    E a expedicao tenta gerar novamente o codigo de validacao do lote
    Entao a operacao deve ser recusada com a mensagem "Codigo de validacao ja foi gerado para o lote LOTE-2024-001."
    E um codigo de validacao deve ser gerado para o lote

  @regra_negocio
  Cenario: Recusar emissao da etiqueta antes da liberacao do lote
    Quando a expedicao gera o codigo de validacao do lote
    E a expedicao embala o lote
    E a expedicao tenta emitir a etiqueta de envio
    Entao a operacao deve ser recusada com a mensagem "Lote ainda nao esta preparado para despacho."
    E o lote nao deve estar marcado como preparado para despacho
