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
    private boolean canRead = true;

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

        if (canRead) {
            updateInitialValues();
            currentCharacter = file.readCharacter();
            currentColumn++;
        } else if (currentCharacter != 32) {
            currentColumn--;
        }

        currentLexeme = String.valueOf((char) currentCharacter);

        if (currentCharacter == -1) {
            return new Token("EOF", "", initialRow, initialColumn);
        } else if (Character.isUpperCase(currentCharacter)) {
            return s1();
        } else if (Character.isLowerCase(currentCharacter)) {
            return idMetAt();
        } else {
            canRead = true;
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
        currentColumn++;
        currentLexeme += (char) currentCharacter;

        if (currentCharacter == -1) {
            return new Token("EOF", "", initialRow, initialColumn);
        } else if (Character.isLowerCase(currentCharacter) || Character.isUpperCase(currentCharacter)) {
            return idClass();
        } else if (isKnownSymbol()) {
            resizeLexeme();
            return new Token("idClass", currentLexeme, initialRow, initialColumn);
        } else if (isLineBreak()) {
            currentRow++;
            currentColumn = 1;
            return new Token("idClass", currentLexeme, initialRow, initialColumn);
        } else {
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
        currentColumn++;
        currentLexeme += (char) currentCharacter;

        if (currentCharacter == -1) {
            return new Token("EOF", "", initialRow, initialColumn);
        } else if (Character.isLowerCase(currentCharacter) || Character.isUpperCase(currentCharacter)) {
            return idClass();
        // If current character is a number or an underscore
        } else if (Character.isDigit(currentCharacter) || currentCharacter == 95) {
            return s1();
        // If current character is a space, a brace, bracket or parenthesis
        } else if (isKnownSymbol()) {
            resizeLexeme();
            canRead = false;
            return new Token("idClass", currentLexeme, initialRow, initialColumn);
        } else if (isLineBreak()) {
            currentRow++;
            currentColumn = 1;
            return new Token("idClass", currentLexeme, initialRow, initialColumn);
        } else {
            
            
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
        currentColumn++;
        currentLexeme += (char) currentCharacter;

        if (currentCharacter == -1) {
            return new Token("EOF", "", initialRow, initialColumn);
        } else if (Character.isLowerCase(currentCharacter) || Character.isUpperCase(currentCharacter)
                || Character.isDigit(currentCharacter) || currentCharacter == 95) {
            return idMetAt();
        } else if (isKnownSymbol()) {
            resizeLexeme();
            canRead = false;
            return new Token("idMetAt", currentLexeme, initialRow, initialColumn);
        } else if (isLineBreak()) {
            currentRow++;
            currentColumn = 1;
            return new Token("idMetAt", currentLexeme, initialRow, initialColumn);
        } else {
            return new Token("UNKNOWN", String.valueOf((char) currentCharacter), initialRow, initialColumn);
        }
    }

    public Token singleCharToken() throws IOException {
        // switch to check for single character tokens
        switch (currentCharacter) {
            // enter or return
            case 10, 13 -> {
                currentRow++;
                currentColumn = 1;
                return nextToken();
            }
            // space
            case 32 -> {
                
                return nextToken();
            }
            // left parenthesis
            case 40 -> {
                
                return new Token("lParen", currentLexeme, initialRow, initialColumn);
            }
            // right parenthesis
            case 41 -> {
                
                return new Token("rParen", currentLexeme, initialRow, initialColumn);
            }
            // comma
            case 44 -> {
                
                return new Token("comma", currentLexeme, initialRow, initialColumn);
            }
            // semicolon
            case 59 -> {
                
                return new Token("semicolon", currentLexeme, initialRow, initialColumn);
            }
            case 123 -> {
                
                return new Token("lBrace", currentLexeme, initialRow, initialColumn);
            }
            case 125 -> {
                
                return new Token("rBrace", currentLexeme, initialRow, initialColumn);
            }
            case 91 -> {
                
                return new Token("lBracket", currentLexeme, initialRow, initialColumn);
            }
            case 93 -> {
                
                return new Token("rBracket", currentLexeme, initialRow, initialColumn);
            }
            default -> {

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

    public boolean isKnownSymbol() {
        return currentCharacter == 40
                || currentCharacter == 41
                || currentCharacter == 123
                || currentCharacter == 125
                || currentCharacter == 91
                || currentCharacter == 93
                || currentCharacter == 59
                || currentCharacter == 44
                || currentCharacter == 32;
    }

    public boolean isLineBreak() {
        return currentCharacter == 10 || currentCharacter == 13;
    }

}
