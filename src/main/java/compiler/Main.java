package compiler;

import compiler.LexicalAnalyzer.LexicalAnalyzer;
import compiler.LexicalAnalyzer.Token;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws IOException {
        execute();
    }

    // Create execute method
    public static void execute() throws FileNotFoundException, IOException {
        LexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzer("src/test/java/fibonacci.s");

        // Create a linked list to store tokens
        LinkedList<Token> tokens = new LinkedList<>();

        // Read tokens until EOF
        Token token;
        for (int i = 0; i < 26; i++) {
            token = lexicalAnalyzer.nextToken();
            tokens.add(token);
        }


        System.out.println("CORRECTO: ANALISIS LEXICO");
        for (Token t : tokens) {
            System.out.println("| " + t.getToken() + " | " + t.getLexeme() + " | LINEA "
                    + t.getRow() + " (COLUMNA " + t.getColumn() + ") |");
        }
    }
}