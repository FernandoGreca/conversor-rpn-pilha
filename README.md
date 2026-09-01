# Conversor de Expressoes Infixas para RPN

Aplicacao Java de console que recebe expressoes matematicas em notacao infixa, as converte para Notacao Polonesa Reversa (RPN) e calcula o resultado com `double`.

## Recursos

- Numeros inteiros e decimais com ponto (`12.5`);
- Operadores `+`, `-`, `*` e `/`;
- Parenteses e precedencia de operadores;
- Conversao pelo algoritmo *Shunting-yard*;
- Avaliacao da RPN com uma pilha generica implementada no proprio projeto;
- Mensagens para expressoes invalidas, parenteses desbalanceados e divisao por zero.

## Como executar

Com o JDK instalado, na raiz do projeto:

```bash
javac -d bin src/App.java
java -cp bin App
```

Tambem e possivel passar uma expressao diretamente pela linha de comando:

```bash
java -cp bin App "(12.5 + 7.5) * 2"
```

## Cinco expressoes usadas nos testes

| Expressao infixa | RPN | Resultado |
| --- | --- | ---: |
| `3 + 4 * 2 / (1 - 5)` | `3 4 2 * 1 5 - / +` | `1.0` |
| `(12.5 + 7.5) * 2` | `12.5 7.5 + 2 *` | `40.0` |
| `10 / 2 + 3 * 4` | `10 2 / 3 4 * +` | `17.0` |
| `(8 + 2.5) / 5` | `8 2.5 + 5 /` | `2.1` |
| `7.2 * (3 - 1.5) + 4` | `7.2 3 1.5 - * 4 +` | `14.8` |

## Etapas da solucao

1. A expressao e separada em *tokens* (numeros, operadores e parenteses).
2. Na conversao, os numeros vao para a saida; operadores ficam em uma pilha ate que sua precedencia permita envia-los para a saida.
3. Ao encontrar `)`, os operadores sao retirados da pilha ate o `(` correspondente.
4. Na avaliacao RPN, cada numero e empilhado. Ao encontrar um operador, os dois valores do topo sao retirados, a operacao e calculada e o resultado volta para a pilha.
