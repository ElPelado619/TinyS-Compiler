package compiler.LexicalAnalyzer;

import compiler.FileScanner;

import java.io.FileNotFoundException;
import java.io.IOException;

public class LexicalAnalyzer {

    private int initialRow;
    private int initialColumn;
    private int currentRow;
    private int currentColumn;
    private int currentCharacter;

    private String currentLexeme;
    private FileScanner file;

    public LexicalAnalyzer(String filePath) throws FileNotFoundException {
        this.initialRow = 1;
        this.initialColumn = 1;
        this.currentRow = 1;
        this.currentColumn = 1;
        this.currentCharacter = 0;
        this.currentLexeme = "";

        this.file = new FileScanner(filePath);
    }

    /**
     * Función que devuelve el siguiente token.
     * @return Token
     * @throws IOException
     */
    public Token nextToken() throws IOException {
        updateInitialValues();

        currentCharacter = file.readCharacter();
        // concat current character to lexeme
        currentLexeme = String.valueOf((char) currentCharacter);

        if (currentCharacter == -1) {
            return new Token("EOF", "", initialRow, initialColumn);
        }
        // If current character starts with a lowercase letter
        else if (Character.isLowerCase(currentCharacter)) {
            // Increase column number
            currentColumn++;
            return s3();
        // If current character starts with a uppercase letter
        } else if (Character.isUpperCase(currentCharacter)) {
            // Increase column number
            currentColumn++;
            return s1();
        } else {
            // Increase column number
            currentColumn++;
            return new Token("UNKNOWN", currentLexeme, initialRow, initialColumn);
        }

    }

    /**
     * Estado que puede reconocer un identificador de Clase.
     * @return Token
     * @throws IOException
     */
    public Token s1() throws IOException {
        currentCharacter = file.readCharacter();
        currentLexeme += (char) currentCharacter;

        if (currentCharacter == -1) {
            return new Token("EOF", "", initialRow, initialColumn);
        } else if (Character.isLowerCase(currentCharacter) || Character.isUpperCase(currentCharacter)) {
            currentColumn++;
            return s1();
        // If current character is a space, a brace, bracket or parenthesis
        } else if (Character.isWhitespace(currentCharacter) || currentCharacter == 40 || currentCharacter == 41 || currentCharacter == 123 || currentCharacter == 125 || currentCharacter == 91 || currentCharacter == 93) {
            // Increase column number
            currentColumn++;
            resizeLexeme();
            return new Token("classID", currentLexeme, initialRow, initialColumn);
        } else {
            // Increase column number
            currentColumn++;
            return new Token("UNKNOWN", String.valueOf((char) currentCharacter), initialRow, initialColumn);
        }
    }

    /**
     * Estado que puede reconocer un identificador de Objeto.
     * @return Token
     * @throws IOException
     */
    public Token s3() throws IOException {
        currentCharacter = file.readCharacter();
        currentLexeme += (char) currentCharacter;

        if (currentCharacter == -1) {
            return new Token("EOF", "", initialRow, initialColumn);
        } else if (Character.isLowerCase(currentCharacter)) {
            currentColumn++;
            return s3();
        // If current character is a space
        } else if (Character.isWhitespace(currentCharacter)) {
            // Increase column number
            currentColumn++;
            resizeLexeme();
            return new Token("objectID", currentLexeme, initialRow, initialColumn);
        } else {
            // Increase column number
            currentColumn++;
            return new Token("UNKNOWN", String.valueOf((char) currentCharacter), initialRow, initialColumn);
        }
    }

    public void resizeLexeme() {
        currentLexeme = currentLexeme.substring(0, currentLexeme.length() - 1);
    }

    public void updateInitialValues() {
        initialRow = currentRow;
        initialColumn = currentColumn;
    }

}
