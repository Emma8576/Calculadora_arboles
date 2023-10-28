package org.example;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;




/**
 * Esta clase representa un servidor sencillo el cual es capaz de aceptar conexiones de clientes atravez del uso de sockets.
 */
public class Server{

    private ServerSocket serverSocket;



    /**
     * Constructor que se encarga de inicializar el servidor con un ServerSocket.
     *
     * @param serverSocket Este parametros que recibe es el ServerSocket que va a utilizar este servidor.
     */

    public Server(ServerSocket serverSocket){
        this.serverSocket = serverSocket;


    }

    /**
     * Este método es el encargado aceptar conexiones que envían los clientes por medio del Socket.
     */

    public void startServer(){
        try{
            while (!serverSocket.isClosed()){
                Socket socket = serverSocket.accept();
                System.out.println("Un nuevo cliente se ha conectado");
                ClientHandler clientHandler = new ClientHandler(socket);
                Thread thread = new Thread(clientHandler);
                thread.start();

            }

        }catch(IOException e){

        }
    }

    /**
     * Este método es utilizado para cerrar el ServerSocket del servidor.
     */

    public void closeServerSocket(){
        try{
            if (serverSocket != null){
                serverSocket.close();
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }
    /**
     * Este método es el main(principal) el cual crea un servidor y lo inicia en el puerto 1234.
     *
     * @param args Los argumentos de la línea de comandos (no se utilizan).
     * @throws IOException Si ocurre un error de entrada/salida al crear el ServerSocket.
     */
    public static void main(String[]args) throws IOException{
        ServerSocket serverSocket = new ServerSocket(3000);
        Server server = new Server(serverSocket);
        server.startServer();
    }
}