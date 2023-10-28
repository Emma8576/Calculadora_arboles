import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Date;
import javax.swing.JOptionPane;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;

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

/** 
 * Esta clase es la encargada de manejar la comunicación con un cliente en un chat.
*/
public class ClientHandler implements Runnable {

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;
    private static final String OPERADORES = "+-*/%^**&|~^";
    private static final String PARENTESIS = "()";
    private PrintWriter out;
    private String[] datos = {};
    FileWriter writer;
    private boolean logica = false;
    
    


    

    /**
     * Este constructor es el encargado de que se inicialicen los flujos de entrada/salida, así como obtener el nombre de usuario del cliente que se conecto al servidor.
     *
     * @param socket El socket de la conexión con el cliente.
     */
    public ClientHandler(Socket socket){
        try{
            this.socket = socket;
            this.bufferedWriter =new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())); 
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream());
            this.clientUsername = bufferedReader.readLine(); 
            clientHandlers.add(this); 
            //broadcastMessage("SERVER: "+clientUsername+" ha entrado al chat!");
        } catch (IOException e){
            closeEverything(socket,bufferedReader,bufferedWriter);
        }
        try {
        writer = new FileWriter("miarchivo.csv", true); // El segundo parámetro indica modo "append"  
                    } catch (IOException E) {
                        E.printStackTrace();
                    }
    }
    

    @Override
    public void run(){
        String messageFromClient;

        while (socket.isConnected()){
            try{
                
                messageFromClient = bufferedReader.readLine();
                //broadcastMessage(messageFromClient);
                System.out.println(messageFromClient);
                Date date = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String timestamp = dateFormat.format(date);


                List<String> tokens = tokenizarExpresion(messageFromClient);
                System.out.println("Ayuda!!!!!1");
                System.out.print(messageFromClient);
                System.out.println(tokens);

                for (String token : tokens) {
                    if (token.contains("&") || token.contains("|") || token.contains("^") || token.contains("~")) {
                        logica = true;
                        break; // Salir del bucle si se encuentra el símbolo &
                    }
                    else{
                        logica = false;
                    }
                }
                
                
                Nodo raiz = construirArbolExpresion(tokens);

                System.out.println("Ayuda!!!!!2");
                System.out.println(raiz);

                if(logica){
                    System.out.println("logica");
                    System.out.println(raiz);
                    boolean resultadologica = evaluarExpresionLogica(raiz);
                    String resultadol1logica = String.valueOf(resultadologica);
                    out.println(resultadol1logica);
                    out.flush();
                    guardarRegistro(messageFromClient, timestamp, resultadol1logica);

                }else{
                    System.out.println("Aritmetica");
                    double resultado = evaluarArbolExpresion(raiz);
                    String resultado1 = String.valueOf(resultado);
                    out.println(resultado1);
                    out.flush();
                    guardarRegistro(messageFromClient, timestamp, resultado1);
                }
                //double resultado = evaluarArbolExpresion(raiz);
                //System.out.println(resultado);
                //System.out.println("Ayuda!!!!!3");
                //String resultado1 = String.valueOf(resultado);
                //System.out.println(resultado1);
                //out.println(resultado1);
                //out.flush();
                //guardarRegistro(messageFromClient, timestamp, resultado1);
                


            }catch(IOException e){
                closeEverything(socket,bufferedReader,bufferedWriter);
                break;
            }
        }

    }
    /**
     * Este método es utilizado para transmitir un mensaje a todos los clientes conectados por ende este mensaje sería uno de broadcast.
     *
     * @param messageToSend Es el mensaje que se va a enviar a todos los clientes.
     */
    public void broadcastMessage(String messageToSend){
        for(ClientHandler clientHandler: clientHandlers){
            try{
                if (!clientHandler.clientUsername.equals(clientUsername)){
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            }catch(IOException e) {
                closeEverything(socket,bufferedReader,bufferedWriter);
            }
        }
    }
    /**
     * Método para el manejador de cliente, al igual que el avisar a los demás clientes que un usuario se desconecto del chat.
     */
    public void removeClientHandler(){
        clientHandlers.remove(this);
        broadcastMessage("SERVER"+clientUsername+" ha abandonado el chat");
    }
    
    /**
     * Este método es utilizado para cerrar los flujos de entrada/salida y el socket.
     *
     * @param socket          El socket a cerrar.
     * @param bufferedReader  El lector de flujo de entrada a cerrar.
     * @param bufferedWriter  El escritor de flujo de salida a cerrar.
     */
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        removeClientHandler();
        try{
            if (bufferedReader != null){
                bufferedReader.close();
            }
            if (bufferedWriter != null){
                bufferedWriter.close();
            }
            if (socket != null){
                socket.close();
            }
        } catch (IOException e){
            e.printStackTrace();
        }

    }

    

    // Método para construir un árbol de expresión a partir de una lista de tokens
    public Nodo construirArbolExpresion(List<String> tokens) {
        Stack<Nodo> pila = new Stack<>();
        Stack<String> operadores = new Stack<>();
        
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if (esOperando(token)) {
                pila.push(new Nodo(token));
            } else if (token.equals("-") && (i == 0 || esOperador(tokens.get(i - 1)) || tokens.get(i - 1).equals("(")) || token.equals("~")) {
                // Manejo de operadores unarios "-" y "~"
                if (i < tokens.size() - 1) {
                    String operando = tokens.get(i + 1);
                    pila.push(new Nodo("~")); // Crear nodo "~"
                    pila.push(new Nodo(operando)); // Crear nodo para el operando
                    i++; // Saltar el operando
                } else {
                    throw new IllegalArgumentException("Operando faltante para el operador ~.");
                }
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
            case "**":
                return 3;
            case "&":
            case "|":
                return 4;
            case "~":
                return 5;
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
            case "**":
                return Math.pow(valorIzquierdo, valorDerecho); // Calcula la potencia
            default:
                throw new IllegalArgumentException("Operador no válido: " + raiz.valor);
        }
    }

    public boolean evaluarExpresionLogica(Nodo raiz) {
        if (raiz == null) {
            throw new IllegalArgumentException("Expresión vacía.");
        }
    
        if (esOperando(raiz.valor)) {
            // Si el nodo raíz es un operando, devolvemos el valor booleano correspondiente
            return Boolean.parseBoolean(raiz.valor);
        }
    
        if (raiz.valor.equals("(")) {
            // Tratar el grupo de paréntesis
            if (raiz.derecho != null) {
                return evaluarExpresionLogica(raiz.derecho);
            } else {
                throw new IllegalArgumentException("Paréntesis sin expresión válida.");
            }
        }
        
    
        if (raiz.valor.equals("~")) {
            if (raiz.derecho != null) {
                boolean valorDerecho = evaluarExpresionLogica(raiz.derecho);
                return !valorDerecho; // Aplicar el operador NOT lógico
            } else {
                throw new IllegalArgumentException("Operador NOT sin expresión válida.");
            }
        }
    
        boolean valorIzquierdo = evaluarExpresionLogica(raiz.izquierdo);
        boolean valorDerecho = evaluarExpresionLogica(raiz.derecho);
    
        switch (raiz.valor) {
            case "&":
                return valorIzquierdo && valorDerecho; // AND lógico
            case "|":
                return valorIzquierdo || valorDerecho; // OR lógico
            case "^":
                return valorIzquierdo ^ valorDerecho; // XOR lógico
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
        return OPERADORES.contains(token) && (token.length() == 1 || token.equals("**"));
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
    
        for (int i = 0; i < expresion.length(); i++) {
            char c = expresion.charAt(i);
    
            if (Character.isDigit(c) || c == '.') {
                token.append(c);
            } else if (OPERADORES.contains(String.valueOf(c)) || PARENTESIS.contains(String.valueOf(c))) {
                if (token.length() > 0) {
                    tokens.add(token.toString());
                    token.setLength(0);
                }
    
                if (c == '*' && i < expresion.length() - 1 && expresion.charAt(i + 1) == '*') {
                    tokens.add("**");
                    i++; // Saltar el segundo asterisco
                } else if (c == '&' || c == '|' || c == '^' || c == '~') {
                    tokens.add(String.valueOf(c));
                } else {
                    tokens.add(String.valueOf(c));
                }
            }
        }
    
        if (token.length() > 0) {
            tokens.add(token.toString());
        }
    
        return tokens;
    }
    

    public void guardarRegistro(String message, String timestamp, String resultado) {
        try {
            writer.append(message);
            writer.append(",");
            writer.append(timestamp);
            writer.append(",");
            writer.append(resultado);
            writer.append("\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
