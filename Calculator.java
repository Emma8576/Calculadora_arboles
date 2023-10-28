import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

public class Calculator extends JFrame implements ActionListener {
    private JLabel operation_entryL, title;
    private JButton exit, next, history, help, camera;
    private JTextArea operation_entry_box;
    private static final String OPERADORES = "+-*/%^**";
    private static final String OPERADORES_LOGICOS = "&|~^";
    private static final String PARENTESIS = "()";
    private PrintWriter out;
    private BufferedReader in;
    private Socket clientSocket;

    private ArbolOpBasicas expresionArbol = new ArbolOpBasicas();

   // Método para tokenizar una expresión matemática o lógica en una lista de tokens
private List<String> tokenizarExpresion(String expresion) {
    List<String> tokens = new ArrayList<>();
    StringBuilder token = new StringBuilder();

    for (char c : expresion.toCharArray()) {
        if (Character.isDigit(c) || c == '.') {
            token.append(c);
        } else if (OPERADORES.contains(String.valueOf(c)) || OPERADORES_LOGICOS.contains(String.valueOf(c)) || PARENTESIS.contains(String.valueOf(c))) {
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

    public Calculator() throws Exception{
        //Configuración de sockets y entrada/salida
        //final BufferedReader in;  //Se declara la entrada 
        //final PrintWriter out;    //Se declara la salida
        final Scanner sc = new Scanner(System.in); // Sirve para obtener la informacón que se encuentra en la terminal la cual fue escrita por el teclado
        //Captura del nombre de usuario
        System.out.println("Indique el nombre de usuario con el que se desea ingresar");
        String username = sc.nextLine();//Obtiene el usuario escrito
        clientSocket = new Socket("localhost", 3000);
        out = new PrintWriter(clientSocket.getOutputStream());
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out.println(username);//Envia el username escrito
        out.flush();

        Locale.setDefault(new Locale("es", "ES"));
        setLayout(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("Arial");

        // Cargar la imagen de fondo y redimensionarla según el tamaño de la pantalla
        ImageIcon backgroundImage = new ImageIcon("images/fondo.jpg");
        Image scaledImage = backgroundImage.getImage().getScaledInstance(
            Toolkit.getDefaultToolkit().getScreenSize().width,
            Toolkit.getDefaultToolkit().getScreenSize().height,
            Image.SCALE_SMOOTH
        );

        // Crear un JLabel para el fondo y configurarlo con la imagen redimensionada
        JLabel background = new JLabel(new ImageIcon(scaledImage));

        // Configurar la ventana
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);

        // Agregar los componentes al JLabel de fondo en lugar del JFrame
        background.setLayout(null);

        title = new JLabel("Basic and logic calculator");
        title.setBounds(360, 40, 900, 70);
        title.setForeground(Color.WHITE); 
        title.setFont(new Font("Arial", 4, 60));
        background.add(title);

        operation_entryL = new JLabel("Enter the operation to be solved:");
        operation_entryL.setBounds(450, 150, 800, 70);
        operation_entryL.setForeground(Color.LIGHT_GRAY); 
        operation_entryL.setFont(new Font("Arial", 1, 30));
        background.add(operation_entryL);

        operation_entry_box = new JTextArea();
        operation_entry_box.setBounds(440, 250, 550, 50);
        operation_entry_box.setForeground(Color.WHITE); 
        operation_entry_box.setFont(new Font("Arial", 3, 40));
        operation_entry_box.setForeground(Color.BLACK); 
        background.add(operation_entry_box);

        exit = new JButton("Exit");
        exit.setBounds(750, 480, 220, 70);
        exit.setBackground(new Color(255, 102, 102)); 
        exit.setFont(new Font("Arial", 3, 30));
        exit.setForeground(new Color(244, 236, 247));
        exit.addActionListener(this);
        background.add(exit);        

        help = new JButton("Help");
        help.setBounds(450, 480, 220, 70);
        help.setBackground(new Color(65, 75, 178));
        help.setFont(new Font("Arial", 3, 30));
        help.setForeground(new Color(244, 236, 247));
        help.addActionListener(this);
        background.add(help);

        camera = new JButton("Solve with photo");
        camera.setBounds(550, 600, 300, 70);
        camera.setBackground(new Color(0, 102, 0)); 
        camera.setFont(new Font("Arial", 3, 30));
        camera.setForeground(new Color(244, 236, 247));
        camera.addActionListener(this);
        background.add(camera);
        

        next = new JButton("Resolve");
        next.setBounds(450, 350, 220, 70);
        next.setBackground(new Color(143, 209, 79));
        next.setFont(new Font("Arial", 3, 30));
        next.setForeground(new Color(244, 236, 247));
        next.addActionListener(this);
        background.add(next);

        history = new JButton("History");
        history.setBounds(750, 350, 220, 70);
        history.setBackground(new Color(255, 165, 0)); 
        history.setFont(new Font("Arial", 3, 30));
        history.setForeground(new Color(244, 236, 247));
        history.addActionListener(this);
        background.add(history);

        // Configuracion del JLabel de fondo para que ocupe toda la pantalla
        background.setBounds(0, 0, Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height);

        // Se agrega el JLabel de fondo al JFrame
        add(background);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == exit) {
            dispose();
        } else if (e.getSource() == next) {
            // Obtener la expresión ingresada por el usuario
            String operacion = operation_entry_box.getText();
            // Tokenizar la expresión en una lista de tokens
            out.println(operacion);
            out.flush();
            System.out.println(operacion);
            
            //List<String> tokens = tokenizarExpresion(operacion);
        
            // Construir un árbol de expresión a partir de los tokens
            //Nodo raiz = expresionArbol.construirArbolExpresion(tokens);
            // Evaluar el árbol de expresión y obtener el resultado
            //double resultado = expresionArbol.evaluarArbolExpresion(raiz);
            
            // Muestra el resultado en un cuadro de texto o etiqueta

            try {
                        String resultado = in.readLine();//Se lee el mensaje recibido
                        JOptionPane.showMessageDialog(this, "Resultado: " + resultado, "Resultado", JOptionPane.INFORMATION_MESSAGE);
                        
                    } catch (IOException E) {
                        E.printStackTrace();
                    }
            //String resultado = in.readLine();//Se lee el mensaje recibido
            //JOptionPane.showMessageDialog(this, "Resultado: " + resultado, "Resultado", JOptionPane.INFORMATION_MESSAGE);
            
        }
    }
    

    public static void main(String args[]) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    new Calculator();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }
}
