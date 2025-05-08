package compiler;

import compiler.LexicalAnalyzer.LexicalAnalyzer;
import compiler.LexicalAnalyzer.Token;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        execute();
    }

    // Create execute method
    public static void execute() throws FileNotFoundException {
        LexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzer("src/test/java/fibonacci.s");

        // Create a linked list to store tokens
        LinkedList<Token> tokens = new LinkedList<>();

        // Read tokens until EOF
        try {
            Token token;
            for (int i = 0; i < 16; i++) {
                token = lexicalAnalyzer.nextToken();
                tokens.add(token);
            }

            /*
            do {
                token = lexicalAnalyzer.nextToken();
                tokens.add(token);
            } while (!token.getToken().equals("EOF"));
             */
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }

        // Print tokens
        System.out.println("CORRECTO: ANALISIS LEXICO");
        for (Token token : tokens) {
            System.out.println("| " + token.getToken() + " | " + token.getLexeme() + " | LINEA "
                    + token.getRow() + " (COLUMNA " + token.getColumn() + ") |");
        }
    }
}