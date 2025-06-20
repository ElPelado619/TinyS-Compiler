package compiler.LexicalAnalyzer;

import compiler.FileScanner;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Clase LexicalAnalyzer que se encarga de analizar léxicamente un archivo
 * fuente.
 */
public class LexicalAnalyzer {

    private int row;
    private int column;
    private int currentCharacter;
    private final StringBuilder currentLexeme = new StringBuilder();
    private final FileScanner fileScanner;
    private static final int MAX_ID_LENGTH = 1024;
    private static final int MAX_STRING_LENGTH = 1024;

    /**
     * Conjunto de palabras clave del lenguaje.
     */
    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
            "class", "impl", "else", "false", "if", "ret", "while", "true",
            "nil", "new", "fn", "st", "pub", "self", "div", "void", "start"
    ));

    /**
     * Constructor de LexicalAnalyzer que inicializa el analizador léxico
     * con la ruta del archivo fuente.
     *
     * @param filePath Ruta del archivo fuente a analizar.
     * @throws IOException Si ocurre un error al leer el archivo.
     * @throws LexicalException Si el archivo no tiene la extensión correcta
     * o está vacío.
     */
    public LexicalAnalyzer(String filePath) throws IOException {
        if (!filePath.endsWith(".s")) {
            // Error de extensión de archivo, el lexema es la ruta del archivo.
            throw new LexicalException(
                    "La extensión del archivo debe ser '.s'",
                    filePath, 0, 0);
        }
        this.fileScanner = new FileScanner(filePath);
        if (fileScanner.isEmpty()) {
            // Error de archivo vacío, el lexema es la ruta del archivo.
            throw new LexicalException("El archivo 'tinyS' está vacío",
                    filePath, 0, 0);
        }
        this.row = 1;
        this.column = 1;
        this.currentCharacter = readCharacter();
    }

    /**
     * Metodo que lee un carácter del archivo y actualiza la posición
     * de fila y columna.
     *
     * @return El carácter leído, o -1 si se alcanza el final del archivo.
     * @throws LexicalException Si se encuentra un símbolo inválido.
     */
    private int readCharacter() {
        try {
            int charRead = fileScanner.readCharacter();
            if (charRead > 255) {
                // Error de símbolo, el lexema es el carácter inválido.
                throw new LexicalException(
                        "Símbolo inválido detectado (fuera de ASCII)",
                        String.valueOf((char) charRead), row, column);
            }
            if (charRead == '\n') {
                row++;
                column = 1;
            } else {
                column++;
            }
            return charRead;
        } catch (IOException e) {
            return -1;
        }
    }

    /**
     * Metodo que analiza el siguiente token del archivo fuente.
     *
     * @return El siguiente token encontrado.
     * @throws LexicalException Si se encuentra un error léxico.
     */
    public Token nextToken() {
        currentLexeme.setLength(0);
        int initialRow = row;
        int initialColumn;

        while (Character.isWhitespace(currentCharacter)) {
            initialRow = row;
            currentCharacter = readCharacter();
        }

        if (currentCharacter == -1) {
            return new Token("EOF", "", row, column);
        }

        currentLexeme.append((char) currentCharacter);
        initialColumn = column -1; // Ajusta la columna inicial al char actual

        if (Character.isUpperCase(currentCharacter)) {
            return processIdClass(initialRow, initialColumn);
        } else if (Character.isLowerCase(currentCharacter)) {
            return processIdMethodAttributeOrKeyword(initialRow, initialColumn);
        } else if (currentCharacter == '"') {
            return processStringLiteral(initialRow, initialColumn);
        } else if (currentCharacter == '/') {
            return processCommentOrDivision(initialRow, initialColumn);
        } else if (currentCharacter == '=') {
            return processEqualsOrAssign(initialRow, initialColumn);
        } else if (Character.isDigit(currentCharacter)) {
            return processIntOrDoubleLiteral(initialRow, initialColumn);
        } else if (currentCharacter == '<') {
            return processLessOrLessEqual(initialRow, initialColumn);
        } else if (currentCharacter == '>') {
            return processGreaterOrGreaterEqual(initialRow, initialColumn);
        } else if (currentCharacter == '+') {
            return processAddOrIncrement(initialRow, initialColumn);
        } else if (currentCharacter == '&') {
            return processAndOperator(initialRow, initialColumn);
        } else if (currentCharacter == '|') {
            return processOrOperator(initialRow, initialColumn);
        } else {
            return processSingleCharacterToken(initialRow, initialColumn);
        }
    }

    /**
     * Procesa un identificador de clase (idClass) y verifica su validez.
     *
     * @param initialRow Fila inicial del lexema.
     * @param initialColumn Columna inicial del lexema.
     * @return Un token representando el idClass.
     * @throws LexicalException Si el idClass es inválido o excede la longitud máxima.
     */
    private Token processIdClass(int initialRow, int initialColumn) {
        currentCharacter = readCharacter();
        while (Character.isLetterOrDigit(currentCharacter) ||
                currentCharacter == '_') {
            if (currentLexeme.length() >= MAX_ID_LENGTH) {
                String lexeme = currentLexeme.append(
                        (char) currentCharacter).toString();
                throw new LexicalException(
                        "La longitud del 'idClass' excede los "
                                + MAX_ID_LENGTH
                                + " caracteres",
                        lexeme, initialRow, initialColumn);
            }
            if (!Character.isLetter(currentCharacter)) {
                String lexeme = currentLexeme.append(
                        (char) currentCharacter).toString();
                throw new LexicalException(
                        "'idClass' inválido, solo puede contener letras",
                        lexeme, initialRow, initialColumn);
            }
            currentLexeme.append((char) currentCharacter);
            currentCharacter = readCharacter();
        }
        String lexeme = currentLexeme.toString();
        if (!Character.isLetter(lexeme.charAt(lexeme.length() - 1))) {
            throw new LexicalException(
                    "'idClass' inválido, debe terminar con una letra",
                    lexeme, initialRow, initialColumn);
        }
        return new Token("idClass", lexeme, initialRow, initialColumn);
    }

    /**
     * Procesa un identificador de método, atributo o palabra clave
     * (idMetAt o keyword) y verifica su validez.
     *
     * @param initialRow Fila inicial del lexema.
     * @param initialColumn Columna inicial del lexema.
     * @return Un token representando el idMetAt o la palabra clave.
     * @throws LexicalException Si el idMetAt es inválido o excede la longitud máxima.
     */
    private Token processIdMethodAttributeOrKeyword(
            int initialRow, int initialColumn) {
        currentCharacter = readCharacter();
        while (Character.isLetterOrDigit(currentCharacter)
                || currentCharacter == '_') {
            if (currentLexeme.length() >= MAX_ID_LENGTH) {
                String lexeme = currentLexeme.append(
                        (char) currentCharacter).toString();
                throw new LexicalException(
                        "La longitud del 'idMetAt' excede los "
                                + MAX_ID_LENGTH + " caracteres",
                        lexeme, initialRow, initialColumn);
            }
            currentLexeme.append((char) currentCharacter);
            currentCharacter = readCharacter();
        }
        String lexeme = currentLexeme.toString();
        if (Character.isLetterOrDigit(currentCharacter)
                || currentCharacter == '_') {
            String invalidLexeme = lexeme + (char) currentCharacter;
            throw new LexicalException(
                    "'idMetAt' inválido, contiene un símbolo no permitido",
                    invalidLexeme, initialRow, initialColumn);
        }
        String tokenType = KEYWORDS.contains(lexeme) ? lexeme : "idMetAt";
        return new Token(tokenType, lexeme, initialRow, initialColumn);
    }

    /**
     * Procesa un literal de cadena (string literal)
     * y verifica su validez.
     *
     * @param initialRow Fila inicial del lexema.
     * @param initialColumn Columna inicial del lexema.
     * @return Un token representando el string literal.
     * @throws LexicalException Si el string literal es inválido o excede la longitud máxima.
     */
    private Token processStringLiteral(int initialRow, int initialColumn) {
        currentLexeme.setLength(0);
        currentCharacter = readCharacter();
        while (currentCharacter != '"') {
            if (currentCharacter == -1 || currentCharacter == '\0') {
                throw new LexicalException(
                        "String inválido, se encontró EOF o NUL",
                        "\"" + currentLexeme,
                        initialRow, initialColumn);
            }
            if (currentCharacter == '\n') {
                throw new LexicalException(
                        "String sin cerrar, salto de línea encontrado",
                        "\"" + currentLexeme,
                        initialRow, initialColumn);
            }
            if (currentLexeme.length() >= MAX_STRING_LENGTH) {
                String lexeme = currentLexeme.append(
                        (char) currentCharacter).toString();
                throw new LexicalException(
                        "La longitud del string excede los "
                                + MAX_STRING_LENGTH
                                + " caracteres", "\"" + lexeme + "...",
                        initialRow, initialColumn);
            }
            currentLexeme.append((char) currentCharacter);
            currentCharacter = readCharacter();
        }
        currentCharacter = readCharacter();
        if (currentLexeme.isEmpty()) {
            throw new LexicalException("El string está vacío", "\"\"",
                    initialRow, initialColumn);
        }
        return new Token("StrLiteral", currentLexeme.toString(),
                initialRow, initialColumn);
    }

    /**
     * Procesa un comentario o una división (op_div)
     * y devuelve el token correspondiente.
     *
     * @param initialRow Fila inicial del lexema.
     * @param initialColumn Columna inicial del lexema.
     * @return Un token representando el comentario o la división.
     */
    private Token processCommentOrDivision(int initialRow, int initialColumn) {
        currentCharacter = readCharacter();
        if (currentCharacter == '/') {
            processSingleLineComment();
            return nextToken();
        } else if (currentCharacter == '*') {
            processMultiLineComment(initialRow, initialColumn);
            return nextToken();
        } else {
            return new Token("op_div", "/", initialRow, initialColumn);
        }
    }

    /**
     * Procesa el operador de igualdad o asignación (op_equals o op_assign)
     * y devuelve el token correspondiente.
     *
     * @param initialRow Fila inicial del lexema.
     * @param initialColumn Columna inicial del lexema.
     * @return Un token representando el operador de igualdad o asignación.
     */
    private Token processEqualsOrAssign(int initialRow, int initialColumn) {
        currentCharacter = readCharacter();
        if (currentCharacter == '=') {
            currentLexeme.append((char) currentCharacter);
            currentCharacter = readCharacter();
            return new Token("equalsOp", currentLexeme.toString(),
                    initialRow, initialColumn);
        } else {
            return new Token("assignOp", "=", initialRow, initialColumn);
        }
    }

    /**
     * Procesa un literal numérico (entero o doble) y
     * devuelve el token correspondiente.
     *
     * @param initialRow Fila inicial del lexema.
     * @param initialColumn Columna inicial del lexema.
     * @return Un token representando el literal numérico.
     * @throws LexicalException Si el literal es inválido o excede la longitud máxima.
     */
    private Token processIntOrDoubleLiteral(int initialRow, int initialColumn) {
        boolean isDouble = false;
        currentCharacter = readCharacter();
        while (Character.isDigit(currentCharacter)) {
            currentLexeme.append((char) currentCharacter);
            currentCharacter = readCharacter();
        }
        if (currentCharacter == '.') {
            isDouble = true;
            currentLexeme.append((char) currentCharacter);
            currentCharacter = readCharacter();
            if (!Character.isDigit(currentCharacter)) {
                throw new LexicalException(
                        "Literal doble inválido, falta la parte fraccionaria",
                        currentLexeme.toString(), initialRow, initialColumn);
            }
            while (Character.isDigit(currentCharacter)) {
                currentLexeme.append((char) currentCharacter);
                currentCharacter = readCharacter();
            }
            if (currentCharacter == '.') {
                throw new LexicalException(
                        "Literal doble inválido, múltiples puntos decimales",
                        currentLexeme + ".",
                        initialRow, initialColumn);
            }
        }
        if (Character.isLetter(currentCharacter)) {
            String lexeme = currentLexeme.append(
                    (char) currentCharacter).toString();
            throw new LexicalException("Literal numérico inválido",
                    lexeme, initialRow, initialColumn);
        }
        String tokenType = isDouble ? "doubleLiteral" : "intLiteral";
        return new Token(tokenType, currentLexeme.toString(),
                initialRow, initialColumn);
    }

    /**
     * Procesa el operador menor o menor o igual (op_less o op_lessEq)
     * y devuelve el token correspondiente.
     *
     * @param initialRow Fila inicial del lexema.
     * @param initialColumn Columna inicial del lexema.
     * @return Un token representando el operador menor o menor o igual.
     */
    private Token processLessOrLessEqual(int initialRow, int initialColumn) {
        currentCharacter = readCharacter();
        if (currentCharacter == '=') {
            currentLexeme.append((char) currentCharacter);
            currentCharacter = readCharacter();
            return new Token("lessEqOp", currentLexeme.toString(),
                    initialRow, initialColumn);
        } else {
            return new Token("lessOp", "<", initialRow, initialColumn);
        }
    }

    /**
     * Procesa el operador mayor o mayor o igual (op_greater o op_greaterEq)
     * y devuelve el token correspondiente.
     *
     * @param initialRow Fila inicial del lexema.
     * @param initialColumn Columna inicial del lexema.
     * @return Un token representando el operador mayor o mayor o igual.
     */
    private Token processGreaterOrGreaterEqual(int initialRow,
                                               int initialColumn) {
        currentCharacter = readCharacter();
        if (currentCharacter == '=') {
            currentLexeme.append((char) currentCharacter);
            currentCharacter = readCharacter();
            return new Token("greaterEqOp", currentLexeme.toString(),
                    initialRow, initialColumn);
        } else {
            return new Token("greaterOp", ">",
                    initialRow, initialColumn);
        }
    }

    /**
     * Procesa el operador suma o incremento (incrementOp o addOp)
     * y devuelve el token correspondiente.
     *
     * @param initialRow Fila inicial del lexema.
     * @param initialColumn Columna inicial del lexema.
     * @return Un token representando el operador suma o incremento.
     */
    private Token processAddOrIncrement(int initialRow, int initialColumn) {
        currentCharacter = readCharacter();
        if (currentCharacter == '+') {
            currentLexeme.append((char) currentCharacter);
            currentCharacter = readCharacter();
            return new Token("incrementOp", currentLexeme.toString(),
                    initialRow, initialColumn);
        } else {
            return new Token("addOp", "+", initialRow, initialColumn);
        }
    }

    /**
     * Procesa el operador and
     * y devuelve el token correspondiente.
     *
     * @param initialRow Fila inicial del lexema.
     * @param initialColumn Columna inicial del lexema.
     * @return Un token representando el operador and.
     */
    private Token processAndOperator(int initialRow, int initialColumn) {
        currentCharacter = readCharacter();
        if (currentCharacter == '&') {
            currentLexeme.append((char) currentCharacter);
            currentCharacter = readCharacter();
            return new Token("andOp", "&&", initialRow, initialColumn);
        } else {
            throw new LexicalException(
                    "Operación 'and' inválida, se esperaba '&&'", "&",
                    initialRow, initialColumn);
        }
    }

    /**
     * Procesa el operador or
     * y devuelve el token correspondiente.
     *
     * @param initialRow Fila inicial del lexema.
     * @param initialColumn Columna inicial del lexema.
     * @return Un token representando el operador or.
     */
    private Token processOrOperator(int initialRow, int initialColumn) {
        currentCharacter = readCharacter();
        if (currentCharacter == '|') {
            currentLexeme.append((char) currentCharacter);
            currentCharacter = readCharacter();
            return new Token("orOp", "||", initialRow, initialColumn);
        } else {
            throw new LexicalException(
                    "Operación 'or' inválida, se esperaba '||'", "|",
                    initialRow, initialColumn);
        }
    }

    /**
     * Procesa comentarios de una sola linea
     * y verifica simbolos no permitidos.
     */
    private void processSingleLineComment() {
        while (currentCharacter != '\n' && currentCharacter != -1) {
            if (currentCharacter > 255) {
                throw new LexicalException(
                        "Comentario inválido, contiene caracteres no ASCII",
                        String.valueOf((char) currentCharacter), row, column);
            }
            currentCharacter = readCharacter();
        }
    }

    /**
     * Procesa comentarios multi-linea
     * y verifica simbolos no permitidos.
     */
    private void processMultiLineComment(int initialRow, int initialColumn) {
        currentCharacter = readCharacter();
        while (true) {
            if (currentCharacter > 255) {
                throw new LexicalException(
                        "Comentario inválido, contiene caracteres no ASCII",
                        String.valueOf((char) currentCharacter), row, column);
            }
            if (currentCharacter == -1) {
                throw new LexicalException(
                        "Comentario de varias líneas sin cerrar", "/*",
                        initialRow, initialColumn);
            }
            if (currentCharacter == '*') {
                currentCharacter = readCharacter();
                if (currentCharacter == '/') {
                    currentCharacter = readCharacter();
                    return;
                }
            } else {
                currentCharacter = readCharacter();
            }
        }
    }

    /**
     * Procesa el token con lexemna de un solo caracter.
     *
     * @param initialRow Fila inicial del lexema.
     * @param initialColumn Columna inicial del lexema.
     * @return Token correspondiente.
     */
    private Token processSingleCharacterToken(int initialRow,
                                              int initialColumn) {
        char character = (char) currentCharacter;
        currentCharacter = readCharacter();
        return switch (character) {
            case '(', ')', '{', '}', '[', ']', ',', ';', '.', '-', '*', '!' ->
                    new Token(getTokenTypeForChar(character),
                            String.valueOf(character),
                            initialRow, initialColumn);
            default ->
                    throw new LexicalException(
                            "Símbolo no pertenece al lenguaje",
                            String.valueOf(character),
                            initialRow, initialColumn);
        };
    }

    private String getTokenTypeForChar(char c) {
        return switch (c) {
            case '(' -> "lParen";
            case ')' -> "rParen";
            case '{' -> "lBrace";
            case '}' -> "rBrace";
            case '[' -> "lBracket";
            case ']' -> "rBracket";
            case ',' -> "comma";
            case ';' -> "semicolon";
            case '.' -> "dot";
            case '-' -> "subOp";
            case '*' -> "mulOp";
            case '!' -> "notOp";
            default -> "UNKNOWN";
        };
    }
}