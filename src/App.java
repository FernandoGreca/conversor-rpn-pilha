import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

/** Converte expressoes aritmeticas da notacao infixa para RPN e as avalia. */
public class App {
    private static final Pattern TOKEN = Pattern.compile("(?:\\d+(?:\\.\\d+)?|\\.\\d+)|[()+\\-*/]");

    private static final String[] EXEMPLOS = {
        "3 + 4 * 2 / (1 - 5)",
        "(12.5 + 7.5) * 2",
        "10 / 2 + 3 * 4",
        "(8 + 2.5) / 5",
        "7.2 * (3 - 1.5) + 4"
    };

    private static final String[] EXPERIMENTOS_PONTO_FLUTUANTE = {
    "0.1 + 0.2",
    "0.1 + 0.1 + 0.1",
    "(0.1 + 0.2) - 0.3",
    "1.0 / 3.0",
    "10000000000000000.0 + 1.0"
    };

    public static void main(String[] args) {
        System.out.println("=== Conversor de expressao infixa para RPN ===");
        System.out.println("\nCinco casos de teste:");
        for (String exemplo : EXEMPLOS) calcular(exemplo);

        System.out.println("\n=== Investigacao sobre ponto flutuante ===");

        for (int i = 0; i < EXPERIMENTOS_PONTO_FLUTUANTE.length; i++) {
        System.out.println("\n--- Experimento " + (i + 1) + " ---");
        calcular(EXPERIMENTOS_PONTO_FLUTUANTE[i]);
        }
    }

    private static void calcular(String expressao) {
        List<String> rpn = converterParaRpn(expressao);
        double resultado = avaliarRpn(rpn);

        System.out.println("\nExpressao original: " + expressao);
        System.out.println("Expressao em RPN:    " + String.join(" ", rpn));
        System.out.println("Resultado:           " + resultado);
    }

    /** Aplica o algoritmo Shunting-yard para produzir a Notacao Polonesa Reversa. */
    public static List<String> converterParaRpn(String expressao) {
        List<String> saida = new ArrayList<>();
        Stack<String> operadores = new Stack<>();
        List<String> tokens = tokenizar(expressao);

        for (String token : tokens) {
            if (ehNumero(token)) {
                saida.add(token);
            } else if (ehOperador(token)) {
                while (!operadores.empty()
                        && ehOperador(operadores.peek())
                        && precedencia(operadores.peek()) >= precedencia(token)) {
                    saida.add(operadores.pop());
                }
                operadores.push(token);
            } else if (token.equals("(")) {
                operadores.push(token);
            } else if (token.equals(")")) {
                while (!operadores.peek().equals("(")) {
                    saida.add(operadores.pop());
                }
                operadores.pop();
            }
        }

        while (!operadores.empty()) {
            saida.add(operadores.pop());
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

            double segundoOperando = valores.pop();
            double primeiroOperando = valores.pop();
            valores.push(aplicarOperador(token, primeiroOperando, segundoOperando));
        }

        return valores.pop();
    }

    private static double aplicarOperador(String operador, double a, double b) {
        return switch (operador) {
            case "+" -> a + b;
            case "-" -> a - b;
            case "*" -> a * b;
            default -> a / b;
        };
    }

    private static List<String> tokenizar(String expressao) {
        return TOKEN.matcher(expressao).results().map(resultado -> resultado.group()).toList();
    }

    private static boolean ehNumero(String token) {
        return !token.isEmpty() && (Character.isDigit(token.charAt(0)) || token.charAt(0) == '.');
    }

    private static boolean ehOperador(String token) {
        return "+-*/".contains(token);
    }

    private static int precedencia(String operador) {
        return (operador.equals("*") || operador.equals("/")) ? 2 : 1;
    }

}
