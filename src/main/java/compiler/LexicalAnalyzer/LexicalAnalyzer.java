package compiler.LexicalAnalyzer;

import compiler.FileScanner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
     * Conjunto de palabras clave del lenguaje.
     * Estas palabras no pueden ser utilizadas como identificadores.
     */
    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
            "class", "impl", "else", "false", "if", "ret", "while", "true", "nil", "new",
            "fn", "st", "pub", "self", "div", "void", "start"
    ));

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
            return processIdMethodAttributeOrKeyword();
        // caracter -> Comilla (string literal)
        } else if (currentCharacter == '"') {
            return processStringLiteral();
        // caracter -> / (comentario o división)
        } else if (currentCharacter == '/') {
            return processCommentOrDivision();
        // caracter -> Signo igual (asignación o comparación)
        } else if (currentCharacter == '=') {
            return processEqualsOrAssign();
        // caracter -> Digito (literal entero o doble)
        } else if (Character.isDigit(currentCharacter)) {
            return processIntOrDoubleLiteral();
        // caracter -> Menor que (<) o menor o igual que (<=)
        } else if (currentCharacter == '<') {
            return processLessOrLessEqual();
        // caracter -> Mayor que (>) o mayor o igual que (>=)
        } else if (currentCharacter == '>') {
            return processGreaterOrGreaterEqual();
        // caracter -> Suma (+) o incremento (++)
        } else if (currentCharacter == '+') {
            return processAddOrIncrement();
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
    private Token processIdMethodAttributeOrKeyword() {
        int initialRow = row;
        int initialColumn = column - 1;

        currentCharacter = readCharacter();
        while (Character.isLetterOrDigit(currentCharacter) || currentCharacter == '_') {
            currentLexeme.append((char) currentCharacter);
            currentCharacter = readCharacter();
        }

        String lexema = currentLexeme.toString();
        String nombreToken;

        if (KEYWORDS.contains(lexema)) {
            nombreToken = lexema;
        } else {
            nombreToken = "idMetAt";
        }

        return new Token(nombreToken, lexema, initialRow, initialColumn);
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
     * Procesa un operador de igualdad o asignación.
     * Si encuentra un '=', verifica si es el inicio de una comparación de igualdad.
     * @return Un token representando el operador de igualdad o asignación.
     */
    private Token processEqualsOrAssign() {
        int initialRow = row;
        int initialColumn = column - 1;
        currentCharacter = readCharacter();
        if (currentCharacter == '=') {
            currentLexeme.append((char) currentCharacter);
            currentCharacter = readCharacter();
            return new Token("equalsOp", currentLexeme.toString(), initialRow, initialColumn);
        } else {
            return new Token("assignOp", "=", initialRow, initialColumn);
        }
    }

    /**
     * Procesa un literal entero o de punto flotante.
     * Un literal entero consiste en dígitos, mientras que un literal de punto flotante
     * contiene un punto decimal seguido de más dígitos.
     * @return Un token representando el literal entero o de punto flotante.
     */
    private Token processIntOrDoubleLiteral() {
        int initialRow = row;
        int initialColumn = column - 1;
        boolean isDouble = false;

        currentCharacter = readCharacter();
        // Read digits
        while (Character.isDigit(currentCharacter)) {
            currentLexeme.append((char) currentCharacter);
            currentCharacter = readCharacter();
        }

        // Check for decimal point
        if (currentCharacter == '.') {
            isDouble = true;
            currentLexeme.append((char) currentCharacter);
            currentCharacter = readCharacter();
            // Read digits after decimal point
            while (Character.isDigit(currentCharacter)) {
                currentLexeme.append((char) currentCharacter);
                currentCharacter = readCharacter();
            }
        }

        String tokenType = isDouble ? "doubleLiteral" : "intLiteral";
        return new Token(tokenType, currentLexeme.toString(), initialRow, initialColumn);
    }

    /**
     * Procesa un operador de menor o menor que.
     * Si encuentra un '<', verifica si es el inicio de una comparación de menor o menor o igual.
     * @return Un token representando el operador de menor o menor que.
     */
    private Token processLessOrLessEqual() {
        int initialRow = row;
        int initialColumn = column - 1;
        currentCharacter = readCharacter();
        if (currentCharacter == '=') {
            currentLexeme.append((char) currentCharacter);
            currentCharacter = readCharacter();
            return new Token("lessEqOp", currentLexeme.toString(), initialRow, initialColumn);
        } else {
            return new Token("lessOp", "<", initialRow, initialColumn);
        }
    }

    /**
     * Procesa un operador de mayor o mayor que.
     * Si encuentra un '>', verifica si es el inicio de una comparación de mayor o mayor o igual.
     * @return Un token representando el operador de mayor o mayor que.
     */
    private Token processGreaterOrGreaterEqual() {
        int initialRow = row;
        int initialColumn = column - 1;
        currentCharacter = readCharacter();
        if (currentCharacter == '=') {
            currentLexeme.append((char) currentCharacter);
            currentCharacter = readCharacter();
            return new Token("greaterEqOp", currentLexeme.toString(), initialRow, initialColumn);
        } else {
            return new Token("greaterOp", ">", initialRow, initialColumn);
        }
    }

    /**
     * Procesa un operador de suma o incremento.
     * Si encuentra un '+', verifica si es el inicio de un operador de incremento.
     * @return Un token representando el operador de suma o incremento.
     */
    private Token processAddOrIncrement() {
        int initialRow = row;
        int initialColumn = column - 1;
        currentCharacter = readCharacter();
        if (currentCharacter == '+') {
            currentLexeme.append((char) currentCharacter);
            currentCharacter = readCharacter();
            return new Token("incrementOp", currentLexeme.toString(), initialRow, initialColumn);
        } else {
            return new Token("addOp", "+", initialRow, initialColumn);
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
        return switch (character) {
            case '(' -> new Token("lParen", "(", initialRow, initialColumn);
            case ')' -> new Token("rParen", ")", initialRow, initialColumn);
            case '{' -> new Token("lBrace", "{", initialRow, initialColumn);
            case '}' -> new Token("rBrace", "}", initialRow, initialColumn);
            case '[' -> new Token("lBracket", "[", initialRow, initialColumn);
            case ']' -> new Token("rBracket", "]", initialRow, initialColumn);
            case ',' -> new Token("comma", ",", initialRow, initialColumn);
            case ';' -> new Token("semicolon", ";", initialRow, initialColumn);
            case '.' -> new Token("dot", ".", initialRow, initialColumn);
            default -> new Token("UNKNOWN", String.valueOf(character), initialRow, initialColumn);
        };
    }
}