Feature: Busca de artigos no AgiBlog

  Background:
    Given que o usuário acessa a página inicial do blog

  @E2E @positive
  Scenario Outline: Busca <Nome do Caso de Teste>
    When o usuário digita <termo> no campo de busca
    And pressiona Enter
    Then deve visualizar uma lista de artigos
    And os resultados devem conter a palavra "<resultado>" no título ou conteúdo

    Examples:
      | Nome do Caso de Teste                  | termo | resultado |
      | por palavra-chave válida usando enter  | "pix"     | pix       |
      | é case insensitive                     | "PIX"     | PIX       |
      | com espaços extras                     | " pix "   | pix       |

  @E2E @negative @regression
  Scenario Outline: Tentativa de Busca - <Nome do Caso de Teste>
    When o usuário digita "<termo>" no campo de busca
    And pressiona Enter
    Then deve visualizar mensagem de "<resultado>"

    Examples:
      | Nome do Caso de Teste          | termo       | resultado                                                                                  |
      | Busca sem resultados           | xyzabc123 | Lamentamos, mas nada foi encontrado para sua pesquisa, tente novamente com outras palavras.  |
      | Busca com caracteres especiais | %$#@!     | Lamentamos, mas nada foi encontrado para sua pesquisa, tente novamente com outras palavras.  |
      | Busca com termo parcial        | emprés    | Lamentamos, mas nada foi encontrado para sua pesquisa, tente novamente com outras palavras.  |

@AGI
  Scenario: Busca usando clique na lupa
    When o usuário digita "empréstimo" no campo de busca
    And clica no ícone de busca
    Then deve visualizar uma lista de artigos

 @E2E
  Scenario: Busca ignora acentuação
    When o usuário digita "emprestimo" no campo de busca
    And pressiona Enter
    Then os resultados devem incluir conteúdos de "empréstimo"



