package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

class Nodo {
    String valor;
    Nodo izquierdo;
    Nodo derecho;

    public Nodo(String valor) {
        this.valor = valor;
        this.izquierdo = null;
        this.derecho = null;
    }
}

public class ArbolOpBasicas {
    private static final String OPERADORES = "+-*/%^**";
    private static final String PARENTESIS = "()";

    // Método para construir un árbol de expresión a partir de una lista de tokens
    public Nodo construirArbolExpresion(List<String> tokens) {
        Stack<Nodo> pila = new Stack<>();
        Stack<String> operadores = new Stack<>();

        for (String token : tokens) {
            if (esOperando(token)) {
                pila.push(new Nodo(token));
            } else if (esOperador(token)) {
                while (!operadores.isEmpty() && tienePrecedenciaMayor(operadores.peek(), token)) {
                    aplicarOperador(pila, operadores);
                }
                operadores.push(token);
            } else if (token.equals("(")) {
                operadores.push(token);
            } else if (token.equals(")")) {
                while (!operadores.isEmpty() && !operadores.peek().equals("(")) {
                    aplicarOperador(pila, operadores);
                }
                operadores.pop();
            }
        }

        while (!operadores.isEmpty()) {
            aplicarOperador(pila, operadores);
        }

        return pila.pop(); // La raíz del árbol de expresión
    }

    // Método para aplicar un operador en la pila de operadores y la pila de nodos
    private void aplicarOperador(Stack<Nodo> pila, Stack<String> operadores) {
        String operador = operadores.pop();
        Nodo derecho = pila.pop();
        Nodo izquierdo = pila.pop();
        Nodo nodoOperador = new Nodo(operador);
        nodoOperador.izquierdo = izquierdo;
        nodoOperador.derecho = derecho;
        pila.push(nodoOperador);
    }

    // Método para determinar si un operador tiene mayor precedencia que otro
    private boolean tienePrecedenciaMayor(String operador1, String operador2) {
        return (prioridadOperador(operador1) >= prioridadOperador(operador2));
    }

    // Método para asignar prioridades a los operadores
    private int prioridadOperador(String operador) {
        switch (operador) {
            case "+":
            case "-":
                return 1;
            case "*":
            case "/":
            case "%":
                return 2;
            case "^":
            case "**":
                return 3;
            default:
                return 0;
        }
    }

    // Método para evaluar el árbol de expresión y calcular el resultado
    public double evaluarArbolExpresion(Nodo raiz) {
        if (raiz == null) {
            return 0.0;
        }
        if (esOperando(raiz.valor)) {
            return Double.parseDouble(raiz.valor);
        }
        double valorIzquierdo = evaluarArbolExpresion(raiz.izquierdo);
        double valorDerecho = evaluarArbolExpresion(raiz.derecho);
        switch (raiz.valor) {
            case "+":
                return valorIzquierdo + valorDerecho;
            case "-":
                return valorIzquierdo - valorDerecho;
            case "*":
                return valorIzquierdo * valorDerecho;
            case "/":
                if (valorDerecho == 0) {
                    throw new ArithmeticException("División por cero no permitida.");
                }
                return valorIzquierdo / valorDerecho;
            case "%":
                return valorIzquierdo % valorDerecho;
            case "^":
            case "**":
                return Math.pow(valorIzquierdo, valorDerecho); // Calcula la potencia
            default:
                throw new IllegalArgumentException("Operador no válido: " + raiz.valor);
        }
    }

    // Método para verificar si un token es un operando (número)
    public boolean esOperando(String token) {
        try {
            Double.parseDouble(token);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Método para verificar si un token es un operador válido
    public boolean esOperador(String token) {
        return OPERADORES.contains(token) && token.length() == 1;
    }

    public static void main(String[] args) {
        ArbolOpBasicas expresionArbol = new ArbolOpBasicas();
        String expresion = "3 + 4 * (5*5) + (5 - 3) % 4 ** 2"; // Ejemplo con potencia
        List<String> tokens = expresionArbol.tokenizarExpresion(expresion);
        Nodo raiz = expresionArbol.construirArbolExpresion(tokens);
        double resultado = expresionArbol.evaluarArbolExpresion(raiz);
        System.out.println("Resultado: " + resultado);
    }

    // Método para tokenizar una expresión matemática en una lista de tokens
    public List<String> tokenizarExpresion(String expresion) {
        List<String> tokens = new ArrayList<>();
        StringBuilder token = new StringBuilder();

        for (char c : expresion.toCharArray()) {
            if (Character.isDigit(c) || c == '.') {
                token.append(c);
            } else if (OPERADORES.contains(String.valueOf(c)) || PARENTESIS.contains(String.valueOf(c))) {
                if (token.length() > 0) {
                    tokens.add(token.toString());
                    token.setLength(0);
                }
                tokens.add(String.valueOf(c));
            }
        }

        if (token.length() > 0) {
            tokens.add(token.toString());
        }

        return tokens;
    }
}

