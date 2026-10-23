# language: pt
@nota_fiscal @regra_negocio
Funcionalidade: Restricao fiscal para lote sem producao
  Para evitar faturamento indevido
  Como analista fiscal da Camisetas S.A.
  Quero que a emissao da nota fiscal seja bloqueada quando o lote nao tiver itens

  Cenario: Recusar emissao de nota fiscal para lote sem itens
    Dado que a empresa abriu o lote "LOTE-2024-009" fabricado em "2024-05-10" com capacidade para 50 camisetas
    Quando o faturamento tenta emitir a nota fiscal "NF-0009" em "2024-05-11"
    Entao a recusa deve ser do tipo "NotaFiscalInvalidaException"
    E a operacao deve ser recusada com a mensagem "Nao e possivel emitir nota fiscal para o lote LOTE-2024-009 sem itens."
    E nenhuma nota fiscal deve ter sido emitida
    E o lote deve estar vazio
