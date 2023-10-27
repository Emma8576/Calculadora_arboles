package org.example;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.ValidationResult;
import net.objecthunter.exp4j.tokenizer.UnknownFunctionOrVariableException;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgproc.Imgproc;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.highgui.HighGui;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Stack;

public class Main {
    private static boolean captureRequested = false;

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        VideoCapture camera = new VideoCapture(0);

        if (!camera.isOpened()) {
            System.out.println("Error al abrir la cámara.");
            return;
        }

        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath("C:\\Users\\User\\Documents\\Descargas\\Tesseract\\tessdata");

        tesseract.setTessVariable("tessedit_char_whitelist", "0123456789+-*/%()^");
        tesseract.setPageSegMode(7);

        Mat destination = new Mat();
        Mat source = new Mat();

        JFrame frame = new JFrame("Calculator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1280, 1024);
        frame.setLocationRelativeTo(null);

        VideoPanel videoPanel = new VideoPanel();
        frame.add(videoPanel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        frame.add(buttonPanel, BorderLayout.SOUTH);

        JButton captureButton = new JButton("Capturar Expresión");
        buttonPanel.add(captureButton);

        captureButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                captureRequested = true;
            }
        });

        frame.setFocusable(true);
        frame.requestFocus();
        frame.setVisible(true);

        while (true) {
            if (camera.read(source)) {
                if (captureRequested) {
                    destination = new Mat(source.rows(), source.cols(), source.type());
                    Imgproc.GaussianBlur(source, destination, new Size(101, 101), 10);
                    Core.addWeighted(source, 1.5, destination, -0.5, 0, destination);

                    source = destination;

                    Imgproc.cvtColor(destination, destination, Imgproc.COLOR_BGR2GRAY);

                    Mat resultMat = new Mat();
                    Imgproc.GaussianBlur(destination, resultMat, new Size(5, 5), 0);
                    Imgproc.threshold(resultMat, resultMat, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);

                    Imgcodecs.imwrite("C:\\Users\\User\\Documents\\math\\imagen_preprocesada.jpg", resultMat);

                    try {
                        String texto = tesseract.doOCR(new File("C:\\Users\\User\\Documents\\math\\imagen_preprocesada.jpg"));
                        System.out.println("Texto extraído: " + texto);

                        String resultado = evaluateExpression(texto);
                        System.out.println("Resultado de la expresión: " + resultado);

                        showResultPopup(resultado);

                    } catch (TesseractException ex) {
                        ex.printStackTrace();
                    }

                    captureRequested = false;
                }

                videoPanel.updateImage((BufferedImage) HighGui.toBufferedImage(source));

                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    static class VideoPanel extends JPanel {
        private Image image;

        public void updateImage(BufferedImage newImage) {
            image = newImage;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null) {
                g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
            }
        }
    }

    private static String evaluateExpression(String expression) {
        expression = expression.replaceAll("\\s+", "");
        if (!expression.matches("[0-9+\\-*/()%^]+")) {
            return "Expresión inválida";
        }
        String postfix = infixToPostfix(expression);



        try {
            Expression e = new ExpressionBuilder(expression)
                    .build();

            ValidationResult validationResult = e.validate();
            if (!validationResult.isValid()) {
                return "Expresión inválida";
            }

            double result = e.evaluate();
            return String.valueOf(result);

        } catch (UnknownFunctionOrVariableException ex) {
            return "Expresión inválida";
        } catch (ArithmeticException ex) {
            return "Error aritmético";
        }
    }

    private static void showResultPopup(String resultado) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(null, "Resultado: " + resultado, "Resultado", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    private static String infixToPostfix(String infix) {
        StringBuilder postfix = new StringBuilder();
        Stack<Character> stack = new Stack<>();

        for (char c : infix.toCharArray()) {
            if (Character.isDigit(c)) {
                postfix.append(c);
            } else if (c == '(') {
                stack.push(c);
            } else if (c == ')') {
                while (!stack.isEmpty() && stack.peek() != '(') {
                    postfix.append(stack.pop());
                }
                stack.pop();
            } else {
                while (!stack.isEmpty() && precedence(c) <= precedence(stack.peek())) {
                    postfix.append(stack.pop());
                }
                stack.push(c);
            }
        }

        while (!stack.isEmpty()) {
            postfix.append(stack.pop());
        }

        return postfix.toString();
    }


    private static int precedence(char operator) {
        if (operator == '+' || operator == '-') {
            return 1;
        } else if (operator == '*' || operator == '/') {
            return 2;
        } else if (operator == '%') {
            return 3;
        } else if (operator == '^'){
            return 4;
        } else if (operator == '(' || operator == ')') {
            return 0; 
        }
        return 0;
    }

    private static double evaluatePostfix(String postfix) {
        Stack<Double> stack = new Stack<>();

        for (char c : postfix.toCharArray()) {
            if (Character.isDigit(c)) {
                stack.push(Double.parseDouble(String.valueOf(c)));
            } else if (isOperator(c)) {
                double operand2 = stack.pop();
                double operand1 = stack.pop();
                double result = applyOperator(c, operand1, operand2);
                stack.push(result);
            }
        }

        return stack.pop();
    }

    private static boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '%' || c =='^';
    }

    private static double applyOperator(char operator, double operand1, double operand2) {
        switch (operator) {
            case '+':
                return operand1 + operand2;
            case '-':
                return operand1 - operand2;
            case '*':
                return operand1 * operand2;
            case '/':
                if (operand2 != 0) {
                    return operand1 / operand2;
                } else {
                    throw new ArithmeticException("División por cero");
                }
            case '%':
                return operand1 % operand2;
            case '^':
                return Math.pow(operand1, operand2);
            default:
                throw new IllegalArgumentException("Operador no válido: " + operator);
        }
    }
}
