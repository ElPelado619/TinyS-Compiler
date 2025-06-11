package compiler.LexicalAnalyzer;

import compiler.FileScanner;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Clase LexicalAnalyzer que se encarga de analizar léxicamente un archivo fuente.
 * Esta clase lee el archivo carácter por carácter y genera tokens basados en las reglas del lenguaje.
 */
public class LexicalAnalyzer {

    private int row;
    private int column;
    private int currentCharacter;
    private final StringBuilder currentLexeme = new StringBuilder();
    private final FileScanner fileScanner;

    /**
     * Constructor que inicializa el analizador léxico.
     * @param filePath Ruta del archivo a analizar.
     * @throws FileNotFoundException Si el archivo no se encuentra.
     */
    public LexicalAnalyzer(String filePath) throws FileNotFoundException {
        this.row = 1;
        this.column = 1;
        this.fileScanner = new FileScanner(filePath);
        // Empieza leyendo el primer carácter del archivo
        this.currentCharacter = readCharacter();
    }

    /**
     * Metodo que lee el siguiente carácter del archivo y actualiza la posición de fila y columna.
     * @return El carácter leído -1 si se ha llegado al final del archivo.
     */
    private int readCharacter() {
        try {
            // Lee el siguiente carácter del archivo
            int charRead = fileScanner.readCharacter();
            // Si es un salto de línea, actualiza la fila y resetea la columna
            if (charRead == '\n') {
                row++;
                column = 1;
            } else {
                column++;
            }
            return charRead;
        } catch (IOException e) {
            // Final del archivo o error de lectura
            return -1;
        }
    }

    /**
     * Metodo que obtiene el siguiente token del archivo.
     * @return El siguiente token encontrado.
     */
    public Token nextToken() {
        currentLexeme.setLength(0);
        int initialRow = row;
        int initialColumn = column - 1;

        // Salta espacios en blanco y actualiza la posición
        while (Character.isWhitespace(currentCharacter)) {
            initialRow = row;
            initialColumn = column;
            currentCharacter = readCharacter();
        }

        // Si el carácter actual es -1, significa que hemos llegado al final del archivo
        if (currentCharacter == -1) {
            return new Token("EOF", "", row, column);
        }

        // Añade el carácter actual al lexema
        currentLexeme.append((char) currentCharacter);

        // caracter -> Mayúscula (idClass)
        if (Character.isUpperCase(currentCharacter)) {
            return processIdClass();
        // caracter -> Minúscula (idMetAt)
        } else if (Character.isLowerCase(currentCharacter)) {
            return processIdMethodAttribute();
        // caracter -> Comilla (string literal)
        } else if (currentCharacter == '"') {
            return processStringLiteral();
        // caracter -> / (comentario o división)
        } else if (currentCharacter == '/') {
            return processCommentOrDivision();
        // de lo contrario, procesar como un token de un solo carácter
        } else {
            return processSingleCharacterToken(initialRow, initialColumn);
        }
    }

    /**
     * Procesa un identificador de clase (idClass).
     * Un idClass comienza con una letra mayúscula y puede contener letras, dígitos o guiones bajos.
     * @return Un token representando el idClass.
     */
    private Token processIdClass() {
        int initialRow = row;
        int initialColumn = column - 1;
        currentCharacter = readCharacter();
        // Continúa leyendo caracteres mientras sean letras, dígitos o guiones bajos
        while (Character.isLetterOrDigit(currentCharacter) || currentCharacter == '_') {
            currentLexeme.append((char) currentCharacter);
            currentCharacter = readCharacter();
        }
        return new Token("idClass", currentLexeme.toString(), initialRow, initialColumn);
    }

    /**
     * Procesa un identificador de metodo o atributo (idMetAt).
     * Un idMetAt comienza con una letra minúscula y puede contener letras, dígitos o guiones bajos.
     * @return Un token representando el idMetAt.
     */
    private Token processIdMethodAttribute() {
        int initialRow = row;
        int initialColumn = column - 1;
        currentCharacter = readCharacter();
        // Continúa leyendo caracteres mientras sean letras, dígitos o guiones bajos
        while (Character.isLetterOrDigit(currentCharacter) || currentCharacter == '_') {
            currentLexeme.append((char) currentCharacter);
            currentCharacter = readCharacter();
        }
        return new Token("idMetAt", currentLexeme.toString(), initialRow, initialColumn);
    }

    /**
     * Procesa un literal de cadena (string literal).
     * Un string literal comienza y termina con comillas dobles.
     * @return Un token representando el string literal.
     */
    private Token processStringLiteral() {
        int initialRow = row;
        int initialColumn = column - 1;
        currentLexeme.setLength(0); // Clear the initial quote
        currentCharacter = readCharacter();
        while (currentCharacter != '"' && currentCharacter != -1) {
            currentLexeme.append((char) currentCharacter);
            currentCharacter = readCharacter();
        }
        if (currentCharacter == '"') {
            currentCharacter = readCharacter(); // Consume the closing quote
        }
        return new Token("StrLiteral", currentLexeme.toString(), initialRow, initialColumn);
    }

    /**
     * Procesa un comentario o un operador de división.
     * Si encuentra un '/', verifica si es el inicio de un comentario de una línea o de varias líneas.
     * @return Un token representando el comentario o el operador de división.
     */
    private Token processCommentOrDivision() {
        int initialRow = row;
        int initialColumn = column - 1;
        currentCharacter = readCharacter();
        if (currentCharacter == '/') {
            return processSingleLineComment(initialRow, initialColumn);
        } else if (currentCharacter == '*') {
            return processMultiLineComment(initialRow, initialColumn);
        } else {
            // If it's just a '/', it could be a division operator or an unknown token
            return new Token("op_div", "/", initialRow, initialColumn);
        }
    }

    /**
     * Procesa un comentario de una sola línea.
     * Un comentario de una sola línea comienza con "//" y termina con un salto de línea.
     * @return Un token representando el comentario de una sola línea.
     */
    private Token processSingleLineComment(int initialRow, int initialColumn) {
        currentLexeme.append('/');
        currentCharacter = readCharacter();
        while (currentCharacter != '\n' && currentCharacter != -1) {
            currentLexeme.append((char) currentCharacter);
            currentCharacter = readCharacter();
        }
        return new Token("SingleLineComment", currentLexeme.toString(), initialRow, initialColumn);
    }

    /**
     * Procesa un comentario de varias líneas.
     * Un comentario de varias líneas comienza con / * y termina con * /
     * @return Un token representando el comentario de varias líneas.
     */
    private Token processMultiLineComment(int initialRow, int initialColumn) {
        currentLexeme.append('*');
        currentCharacter = readCharacter();
        while (true) {
            if (currentCharacter == -1) {
                // Unterminated comment
                return new Token("UNKNOWN", currentLexeme.toString(), initialRow, initialColumn);
            }
            if (currentCharacter == '*') {
                currentLexeme.append((char) currentCharacter);
                currentCharacter = readCharacter();
                if (currentCharacter == '/') {
                    currentLexeme.append((char) currentCharacter);
                    currentCharacter = readCharacter();
                    return new Token("MultiLineComment", currentLexeme.toString(), initialRow, initialColumn);
                }
            } else {
                currentLexeme.append((char) currentCharacter);
                currentCharacter = readCharacter();
            }
        }
    }

    /**
     * Procesa un token de un solo carácter.
     * Dependiendo del carácter, devuelve el token correspondiente.
     * @param initialRow Fila inicial del token.
     * @param initialColumn Columna inicial del token.
     * @return Un token representando el carácter leído.
     */
    private Token processSingleCharacterToken(int initialRow, int initialColumn) {
        char character = (char) currentCharacter;
        currentCharacter = readCharacter();
        switch (character) {
            case '(':
                return new Token("lParen", "(", initialRow, initialColumn);
            case ')':
                return new Token("rParen", ")", initialRow, initialColumn);
            case '{':
                return new Token("lBrace", "{", initialRow, initialColumn);
            case '}':
                return new Token("rBrace", "}", initialRow, initialColumn);
            case '[':
                return new Token("lBracket", "[", initialRow, initialColumn);
            case ']':
                return new Token("rBracket", "]", initialRow, initialColumn);
            case ',':
                return new Token("comma", ",", initialRow, initialColumn);
            case ';':
                return new Token("semicolon", ";", initialRow, initialColumn);
            default:
                return new Token("UNKNOWN", String.valueOf(character), initialRow, initialColumn);
        }
    }
}