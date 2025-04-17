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

        // If current character is -1 (EOF)
        if (currentCharacter == -1) {
            return new Token("EOF", "", initialRow, initialColumn);

        // If current character starts with an uppercase letter
        } else if (Character.isUpperCase(currentCharacter)) {
            // Increase column number
            currentColumn++;
            return s1();

        // If current character starts with a lowercase letter
        } else if (Character.isLowerCase(currentCharacter)) {
            // Increase column number
            currentColumn++;
            return idMetAt();

        } else {
            // Increase column number
            currentColumn++;
            return singleCharToken();
        }

    }

    /**
     * Estado que reconoce un posible identificador de Clase.
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
            return idClass();
        // If current character is a space, a brace, bracket or parenthesis
        } else if (Character.isWhitespace(currentCharacter) || currentCharacter == 40 || currentCharacter == 41 || currentCharacter == 123 || currentCharacter == 125 || currentCharacter == 91 || currentCharacter == 93) {
            // Increase column number
            currentColumn++;
            resizeLexeme();
            return new Token("idClass", currentLexeme, initialRow, initialColumn);
        } else {
            // Increase column number
            currentColumn++;
            return new Token("UNKNOWN", String.valueOf((char) currentCharacter), initialRow, initialColumn);
        }
    }

    /**
     * Estado que reconoce un identificador de Clase.
     * @return Token
     * @throws IOException
     */
    public Token idClass() throws IOException {
        currentCharacter = file.readCharacter();
        currentLexeme += (char) currentCharacter;

        if (currentCharacter == -1) {
            return new Token("EOF", "", initialRow, initialColumn);
        // If char is lowercase or uppercase return idClass
        } else if (Character.isLowerCase(currentCharacter) || Character.isUpperCase(currentCharacter)) {
            currentColumn++;
            return idClass();
        // If current character is a number or an underscore
        } else if (Character.isDigit(currentCharacter) || currentCharacter == 95) {
            currentColumn++;
            return s1();
        // If current character is a space, a brace, bracket or parenthesis
        } else if (Character.isWhitespace(currentCharacter) || currentCharacter == 40 || currentCharacter == 41 || currentCharacter == 123 || currentCharacter == 125 || currentCharacter == 91 || currentCharacter == 93) {
            // Increase column number
            currentColumn++;
            resizeLexeme();
            return new Token("idClass", currentLexeme, initialRow, initialColumn);
        } else {
            // Increase column number
            currentColumn++;
            return new Token("UNKNOWN", String.valueOf((char) currentCharacter), initialRow, initialColumn);
        }
    }

    /**
     * Estado que puede reconocer un identificador de Objeto/Metodo/Atributo.
     * @return Token
     * @throws IOException
     */
    public Token idMetAt() throws IOException {
        currentCharacter = file.readCharacter();
        currentLexeme += (char) currentCharacter;

        if (currentCharacter == -1) {
            return new Token("EOF", "", initialRow, initialColumn);
        // If char is lowercase, uppercase, digit or underscore, return idMetAt
        } else if (Character.isLowerCase(currentCharacter) || Character.isUpperCase(currentCharacter)
                || Character.isDigit(currentCharacter) || currentCharacter == 95) {
            currentColumn++;
            return idMetAt();

        // If current character is a space
        } else if (Character.isWhitespace(currentCharacter)) {
            // Increase column number
            currentColumn++;
            resizeLexeme();
            return new Token("idMetAt", currentLexeme, initialRow, initialColumn);
        } else {
            // Increase column number
            currentColumn++;
            return new Token("UNKNOWN", String.valueOf((char) currentCharacter), initialRow, initialColumn);
        }
    }

    public Token singleCharToken() throws IOException {

        // switch to check for single character tokens
        switch (currentCharacter) {
            // space
            case 32 -> {
                currentColumn++;
                return nextToken();
            }
            // left parenthesis
            case 40 -> {
                currentColumn++;
                return new Token("LPAREN", currentLexeme, initialRow, initialColumn);
            }
            case 41 -> {
                currentColumn++;
                return new Token("RPAREN", currentLexeme, initialRow, initialColumn);
            }
            case 123 -> {
                currentColumn++;
                return new Token("lBrace", currentLexeme, initialRow, initialColumn);
            }
            case 125 -> {
                currentColumn++;
                return new Token("rBrace", currentLexeme, initialRow, initialColumn);
            }
            case 91 -> {
                currentColumn++;
                return new Token("LBRACKET", currentLexeme, initialRow, initialColumn);
            }
            case 93 -> {
                currentColumn++;
                return new Token("RBRACKET", currentLexeme, initialRow, initialColumn);
            }
            default -> {
                // Increase column number
                currentColumn++;
                return new Token("UNKNOWN", String.valueOf((char) currentCharacter), initialRow, initialColumn);
            }
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
