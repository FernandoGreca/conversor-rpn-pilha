import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/** Converte expressoes aritmeticas da notacao infixa para RPN e as avalia. */
public class App {
    private static final String[] EXEMPLOS = {
        "3 + 4 * 2 / (1 - 5)",
        "(12.5 + 7.5) * 2",
        "10 / 2 + 3 * 4",
        "(8 + 2.5) / 5",
        "7.2 * (3 - 1.5) + 4"
    };

    public static void main(String[] args) {
        System.out.println("=== Conversor de expressao infixa para RPN ===");
        System.out.println("\nCinco casos de teste:");
        for (String exemplo : EXEMPLOS) {
            calcular(exemplo);
        }
    }

    private static void calcular(String expressao) {
        try {
            List<String> rpn = converterParaRpn(expressao);
            double resultado = avaliarRpn(rpn);

            System.out.println("\nExpressao original: " + expressao);
            System.out.println("Expressao em RPN:    " + String.join(" ", rpn));
            System.out.println("Resultado:           " + resultado);
        } catch (IllegalArgumentException | ArithmeticException e) {
            System.out.println("\nExpressao original: " + expressao);
            System.out.println("Erro: " + e.getMessage());
        }
    }

    /** Aplica o algoritmo Shunting-yard para produzir a Notacao Polonesa Reversa. */
    public static List<String> converterParaRpn(String expressao) {
        List<String> saida = new ArrayList<>();
        Stack<String> operadores = new Stack<>();
        List<String> tokens = tokenizar(expressao);
        boolean esperaOperando = true;

        for (String token : tokens) {
            if (ehNumero(token)) {
                if (!esperaOperando) {
                    throw new IllegalArgumentException("Falta um operador entre os numeros.");
                }
                saida.add(token);
                esperaOperando = false;
            } else if (ehOperador(token)) {
                if (esperaOperando) {
                    throw new IllegalArgumentException("Operador sem operando: " + token);
                }
                while (!operadores.empty()
                        && ehOperador(operadores.peek())
                        && precedencia(operadores.peek()) >= precedencia(token)) {
                    saida.add(operadores.pop());
                }
                operadores.push(token);
                esperaOperando = true;
            } else if (token.equals("(")) {
                if (!esperaOperando) {
                    throw new IllegalArgumentException("Falta um operador antes de '('.");
                }
                operadores.push(token);
            } else if (token.equals(")")) {
                if (esperaOperando) {
                    throw new IllegalArgumentException("Parenteses sem expressao valida.");
                }
                boolean encontrouAbertura = false;
                while (!operadores.empty()) {
                    String operador = operadores.pop();
                    if (operador.equals("(")) {
                        encontrouAbertura = true;
                        break;
                    }
                    saida.add(operador);
                }
                if (!encontrouAbertura) {
                    throw new IllegalArgumentException("Parenteses desbalanceados.");
                }
            }
        }

        if (tokens.isEmpty() || esperaOperando) {
            throw new IllegalArgumentException("A expressao termina com um operador ou esta vazia.");
        }

        while (!operadores.empty()) {
            String operador = operadores.pop();
            if (operador.equals("(")) {
                throw new IllegalArgumentException("Parenteses desbalanceados.");
            }
            saida.add(operador);
        }
        return saida;
    }

    /** Avalia uma expressao em RPN usando uma pilha de valores double. */
    public static double avaliarRpn(List<String> rpn) {
        Stack<Double> valores = new Stack<>();

        for (String token : rpn) {
            if (ehNumero(token)) {
                valores.push(Double.parseDouble(token));
                continue;
            }

            if (valores.size() < 2) {
                throw new IllegalArgumentException("Expressao RPN invalida.");
            }
            double segundoOperando = valores.pop();
            double primeiroOperando = valores.pop();
            valores.push(aplicarOperador(token, primeiroOperando, segundoOperando));
        }

        if (valores.size() != 1) {
            throw new IllegalArgumentException("Expressao RPN invalida.");
        }
        return valores.pop();
    }

    private static double aplicarOperador(String operador, double a, double b) {
        return switch (operador) {
            case "+" -> a + b;
            case "-" -> a - b;
            case "*" -> a * b;
            case "/" -> {
                if (b == 0.0) {
                    throw new ArithmeticException("Divisao por zero nao permitida.");
                }
                yield a / b;
            }
            default -> throw new IllegalArgumentException("Operador invalido: " + operador);
        };
    }

    private static List<String> tokenizar(String expressao) {
        List<String> tokens = new ArrayList<>();
        int indice = 0;

        while (indice < expressao.length()) {
            char caractere = expressao.charAt(indice);
            if (Character.isWhitespace(caractere)) {
                indice++;
            } else if (Character.isDigit(caractere) || caractere == '.') {
                int inicio = indice;
                boolean encontrouPonto = false;
                while (indice < expressao.length()) {
                    char atual = expressao.charAt(indice);
                    if (Character.isDigit(atual)) {
                        indice++;
                    } else if (atual == '.' && !encontrouPonto) {
                        encontrouPonto = true;
                        indice++;
                    } else {
                        break;
                    }
                }
                String numero = expressao.substring(inicio, indice);
                if (numero.equals(".")) {
                    throw new IllegalArgumentException("Numero decimal invalido.");
                }
                tokens.add(numero);
            } else if (ehOperador(String.valueOf(caractere)) || caractere == '(' || caractere == ')') {
                tokens.add(String.valueOf(caractere));
                indice++;
            } else {
                throw new IllegalArgumentException("Caractere invalido: " + caractere);
            }
        }
        return tokens;
    }

    private static boolean ehNumero(String token) {
        return !token.isEmpty() && (Character.isDigit(token.charAt(0)) || token.charAt(0) == '.');
    }

    private static boolean ehOperador(String token) {
        return token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/");
    }

    private static int precedencia(String operador) {
        return (operador.equals("*") || operador.equals("/")) ? 2 : 1;
    }

}
