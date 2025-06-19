package compiler;

import compiler.LexicalAnalyzer.LexicalAnalyzer;
import compiler.LexicalAnalyzer.LexicalException;
import compiler.LexicalAnalyzer.Token;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;

public class Main {
    public static void main(String[] args) throws IOException {

        // execute("src/test/java/fibonacci.s");

        File folder = new File("src/test/lexical/fail");
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                try {
                    System.out.println(Color.ORANGE_BOLD + "\nAnalizando archivo: " + file.getName() + Color.RESET);
                    execute(file.getAbsolutePath());
                } catch (LexicalException e) {
                    // Print the error message from the LexicalException
                    System.out.println(Color.RED_BOLD + e.getMessage() + Color.RESET);
                } catch (FileNotFoundException e) {
                    // Handle file not found exception
                    System.out.println(Color.RED_BOLD + "Archivo no encontrado: " + file.getName() + Color.RESET);
                } catch (IOException e) {
                    // Handle other IO exceptions
                    System.out.println(Color.RED_BOLD + "Error de entrada/salida al procesar el archivo: " + file.getName() + Color.RESET);
                }
            }
        } else {
            System.out.println("No se encontraron archivos en la carpeta especificada.");
        }
    }

    // Create execute method
    public static void execute(String path) throws FileNotFoundException, IOException {
        LexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzer(path);

        // Create a linked list to store tokens
        LinkedList<Token> tokens = new LinkedList<>();

        // Read tokens until EOF
        Token token;
        do  {
            token = lexicalAnalyzer.nextToken();
            tokens.add(token);
        } while (!token.getToken().equals("EOF"));

        System.out.print(Color.GREEN_BOLD);
        System.out.println("CORRECTO: ANALISIS LEXICO");
        System.out.print(Color.RESET);
        for (Token t : tokens) {
            printStick(); System.out.print(t.getToken());
            printStick(); System.out.print(t.getLexeme());
            printStick(); System.out.print("LINEA " + t.getRow());
            System.out.print(" (COLUMNA " + t.getColumn() + ")");
            printStick(); System.out.println();
        }
    }

    public static void printStick() {
        System.out.print(Color.GREEN_BOLD);
        System.out.print(" | ");
        System.out.print(Color.RESET);
    }

    enum Color {
        //Color end string, color reset
        RESET("\033[0m"),

        // Regular Colors. Normal color, no bold, background color etc.
        BLACK("\033[0;30m"),    // BLACK
        RED("\033[0;31m"),      // RED
        GREEN("\033[0;32m"),    // GREEN
        YELLOW("\033[0;33m"),   // YELLOW
        BLUE("\033[0;34m"),     // BLUE
        MAGENTA("\033[0;35m"),  // MAGENTA
        CYAN("\033[0;36m"),     // CYAN
        WHITE("\033[0;37m"),    // WHITE

        // Bold
        BLACK_BOLD("\033[1;30m"),   // BLACK
        RED_BOLD("\033[1;31m"),     // RED
        GREEN_BOLD("\033[1;38;5;40m"),   // GREEN
        YELLOW_BOLD("\033[1;33m"),  // YELLOW
        BLUE_BOLD("\033[1;34m"),    // BLUE
        MAGENTA_BOLD("\033[1;35m"), // MAGENTA
        CYAN_BOLD("\033[1;36m"),    // CYAN
        WHITE_BOLD("\033[1;37m"),   // WHITE
        ORANGE_BOLD("\033[1;38;5;202m"), // ORANGE

        // Underline
        BLACK_UNDERLINED("\033[4;30m"),     // BLACK
        RED_UNDERLINED("\033[4;31m"),       // RED
        GREEN_UNDERLINED("\033[4;32m"),     // GREEN
        YELLOW_UNDERLINED("\033[4;33m"),    // YELLOW
        BLUE_UNDERLINED("\033[4;34m"),      // BLUE
        MAGENTA_UNDERLINED("\033[4;35m"),   // MAGENTA
        CYAN_UNDERLINED("\033[4;36m"),      // CYAN
        WHITE_UNDERLINED("\033[4;37m"),     // WHITE

        // Background
        BLACK_BACKGROUND("\033[40m"),   // BLACK
        RED_BACKGROUND("\033[41m"),     // RED
        GREEN_BACKGROUND("\033[42m"),   // GREEN
        YELLOW_BACKGROUND("\033[43m"),  // YELLOW
        BLUE_BACKGROUND("\033[44m"),    // BLUE
        MAGENTA_BACKGROUND("\033[45m"), // MAGENTA
        CYAN_BACKGROUND("\033[46m"),    // CYAN
        WHITE_BACKGROUND("\033[47m"),   // WHITE

        // High Intensity
        BLACK_BRIGHT("\033[0;90m"),     // BLACK
        RED_BRIGHT("\033[0;91m"),       // RED
        GREEN_BRIGHT("\033[0;92m"),     // GREEN
        YELLOW_BRIGHT("\033[0;93m"),    // YELLOW
        BLUE_BRIGHT("\033[0;94m"),      // BLUE
        MAGENTA_BRIGHT("\033[0;95m"),   // MAGENTA
        CYAN_BRIGHT("\033[0;96m"),      // CYAN
        WHITE_BRIGHT("\033[0;97m"),     // WHITE

        // Bold High Intensity
        BLACK_BOLD_BRIGHT("\033[1;90m"),    // BLACK
        RED_BOLD_BRIGHT("\033[1;91m"),      // RED
        GREEN_BOLD_BRIGHT("\033[1;92m"),    // GREEN
        YELLOW_BOLD_BRIGHT("\033[1;93m"),   // YELLOW
        BLUE_BOLD_BRIGHT("\033[1;94m"),     // BLUE
        MAGENTA_BOLD_BRIGHT("\033[1;95m"),  // MAGENTA
        CYAN_BOLD_BRIGHT("\033[1;96m"),     // CYAN
        WHITE_BOLD_BRIGHT("\033[1;97m"),    // WHITE

        // High Intensity backgrounds
        BLACK_BACKGROUND_BRIGHT("\033[0;100m"),     // BLACK
        RED_BACKGROUND_BRIGHT("\033[0;101m"),       // RED
        GREEN_BACKGROUND_BRIGHT("\033[0;102m"),     // GREEN
        YELLOW_BACKGROUND_BRIGHT("\033[0;103m"),    // YELLOW
        BLUE_BACKGROUND_BRIGHT("\033[0;104m"),      // BLUE
        MAGENTA_BACKGROUND_BRIGHT("\033[0;105m"),   // MAGENTA
        CYAN_BACKGROUND_BRIGHT("\033[0;106m"),      // CYAN
        WHITE_BACKGROUND_BRIGHT("\033[0;107m");     // WHITE

        private final String code;

        Color(String code) {
            this.code = code;
        }

        @Override
        public String toString() {
            return code;
        }
    }
}