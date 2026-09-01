import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
        if (args.length > 0) {
            calcular(String.join(" ", args));
            return;
        }

        System.out.println("=== Conversor de expressao infixa para RPN ===");
        System.out.println("\nExemplos de teste:");
        for (String exemplo : EXEMPLOS) {
            calcular(exemplo);
        }

        System.out.println("Digite uma expressao ou 'sair' para encerrar.");
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                if (!scanner.hasNextLine()) {
                    break;
                }

                String expressao = scanner.nextLine().trim();
                if (expressao.equalsIgnoreCase("sair")) {
                    break;
                }
                if (expressao.isEmpty()) {
                    System.out.println("Digite uma expressao valida.");
                    continue;
                }
                calcular(expressao);
            }
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
        Pilha<String> operadores = new Pilha<>();
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
                while (!operadores.estaVazia()
                        && ehOperador(operadores.topo())
                        && precedencia(operadores.topo()) >= precedencia(token)) {
                    saida.add(operadores.desempilhar());
                }
                operadores.empilhar(token);
                esperaOperando = true;
            } else if (token.equals("(")) {
                if (!esperaOperando) {
                    throw new IllegalArgumentException("Falta um operador antes de '('.");
                }
                operadores.empilhar(token);
            } else if (token.equals(")")) {
                if (esperaOperando) {
                    throw new IllegalArgumentException("Parenteses sem expressao valida.");
                }
                boolean encontrouAbertura = false;
                while (!operadores.estaVazia()) {
                    String operador = operadores.desempilhar();
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

        while (!operadores.estaVazia()) {
            String operador = operadores.desempilhar();
            if (operador.equals("(")) {
                throw new IllegalArgumentException("Parenteses desbalanceados.");
            }
            saida.add(operador);
        }
        return saida;
    }

    /** Avalia uma expressao em RPN usando uma pilha de valores double. */
    public static double avaliarRpn(List<String> rpn) {
        Pilha<Double> valores = new Pilha<>();

        for (String token : rpn) {
            if (ehNumero(token)) {
                valores.empilhar(Double.parseDouble(token));
                continue;
            }

            if (valores.tamanho() < 2) {
                throw new IllegalArgumentException("Expressao RPN invalida.");
            }
            double segundoOperando = valores.desempilhar();
            double primeiroOperando = valores.desempilhar();
            valores.empilhar(aplicarOperador(token, primeiroOperando, segundoOperando));
        }

        if (valores.tamanho() != 1) {
            throw new IllegalArgumentException("Expressao RPN invalida.");
        }
        return valores.desempilhar();
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

    /** Implementacao simples de pilha, usada tanto na conversao quanto no calculo. */
    private static class Pilha<T> {
        private final List<T> elementos = new ArrayList<>();

        void empilhar(T elemento) {
            elementos.add(elemento);
        }

        T desempilhar() {
            if (estaVazia()) {
                throw new IllegalArgumentException("Tentativa de remover de uma pilha vazia.");
            }
            return elementos.remove(elementos.size() - 1);
        }

        T topo() {
            if (estaVazia()) {
                throw new IllegalArgumentException("Tentativa de consultar uma pilha vazia.");
            }
            return elementos.get(elementos.size() - 1);
        }

        boolean estaVazia() {
            return elementos.isEmpty();
        }

        int tamanho() {
            return elementos.size();
        }
    }
}
