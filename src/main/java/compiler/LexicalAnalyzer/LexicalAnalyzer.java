package compiler.LexicalAnalyzer;

import compiler.FileScanner;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Clase LexicalAnalyzer que se encarga de analizar léxicamente un archivo fuente.
 * MODIFICADO: Se ajusta el lanzamiento de excepciones para incluir el lexema inválido
 * y así soportar el nuevo formato de error.
 */
public class LexicalAnalyzer {

    private int row;
    private int column;
    private int currentCharacter;
    private final StringBuilder currentLexeme = new StringBuilder();
    private final FileScanner fileScanner;
    private static final int MAX_ID_LENGTH = 1024;
    private static final int MAX_STRING_LENGTH = 1024;

    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
            "class", "impl", "else", "false", "if", "ret", "while", "true", "nil", "new",
            "fn", "st", "pub", "self", "div", "void", "start"
    ));

    public LexicalAnalyzer(String filePath) throws IOException {
        if (!filePath.endsWith(".s")) {
            // Error de extensión de archivo, el lexema es la ruta del archivo.
            throw new LexicalException("La extensión del archivo debe ser '.s'", filePath, 0, 0);
        }
        this.fileScanner = new FileScanner(filePath);
        if (fileScanner.isEmpty()) {
            // Error de archivo vacío, el lexema es la ruta del archivo.
            throw new LexicalException("El archivo 'tinyS' está vacío", filePath, 0, 0);
        }
        this.row = 1;
        this.column = 1;
        this.currentCharacter = readCharacter();
    }

    private int readCharacter() {
        try {
            int charRead = fileScanner.readCharacter();
            if (charRead > 255) {
                // Error de símbolo, el lexema es el carácter inválido.
                throw new LexicalException("Símbolo inválido detectado (fuera de ASCII)", String.valueOf((char) charRead), row, column);
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

    public Token nextToken() {
        currentLexeme.setLength(0);
        int initialRow = row;
        int initialColumn = column > 1 ? column - 1 : 1;

        while (Character.isWhitespace(currentCharacter)) {
            initialRow = row;
            initialColumn = column;
            currentCharacter = readCharacter();
        }

        if (currentCharacter == -1) {
            return new Token("EOF", "", row, column);
        }

        currentLexeme.append((char) currentCharacter);
        initialColumn = column -1; // Ajustar la columna inicial al caracter actual

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

    private Token processIdClass(int initialRow, int initialColumn) {
        currentCharacter = readCharacter();
        while (Character.isLetterOrDigit(currentCharacter) || currentCharacter == '_') {
            if (currentLexeme.length() >= MAX_ID_LENGTH) {
                String lexeme = currentLexeme.append((char) currentCharacter).toString();
                throw new LexicalException("La longitud del 'idClass' excede los " + MAX_ID_LENGTH + " caracteres", lexeme, initialRow, initialColumn);
            }
            if (!Character.isLetter(currentCharacter)) {
                String lexeme = currentLexeme.append((char) currentCharacter).toString();
                throw new LexicalException("'idClass' inválido, solo puede contener letras", lexeme, initialRow, initialColumn);
            }
            currentLexeme.append((char) currentCharacter);
            currentCharacter = readCharacter();
        }
        String lexeme = currentLexeme.toString();
        if (!Character.isLetter(lexeme.charAt(lexeme.length() - 1))) {
            throw new LexicalException("'idClass' inválido, debe terminar con una letra", lexeme, initialRow, initialColumn);
        }
        return new Token("idClass", lexeme, initialRow, initialColumn);
    }

    private Token processIdMethodAttributeOrKeyword(int initialRow, int initialColumn) {
        currentCharacter = readCharacter();
        while (Character.isLetterOrDigit(currentCharacter) || currentCharacter == '_') {
            if (currentLexeme.length() >= MAX_ID_LENGTH) {
                String lexeme = currentLexeme.append((char) currentCharacter).toString();
                throw new LexicalException("La longitud del 'idMetAt' excede los " + MAX_ID_LENGTH + " caracteres", lexeme, initialRow, initialColumn);
            }
            currentLexeme.append((char) currentCharacter);
            currentCharacter = readCharacter();
        }
        String lexeme = currentLexeme.toString();
        if (Character.isLetterOrDigit(currentCharacter) || currentCharacter == '_') {
            String invalidLexeme = lexeme + (char) currentCharacter;
            throw new LexicalException("'idMetAt' inválido, contiene un símbolo no permitido", invalidLexeme, initialRow, initialColumn);
        }
        String tokenType = KEYWORDS.contains(lexeme) ? lexeme : "idMetAt";
        return new Token(tokenType, lexeme, initialRow, initialColumn);
    }

    private Token processStringLiteral(int initialRow, int initialColumn) {
        currentLexeme.setLength(0);
        currentCharacter = readCharacter();
        while (currentCharacter != '"') {
            if (currentCharacter == -1 || currentCharacter == '\0') {
                throw new LexicalException("String inválido, se encontró EOF o NUL", "\"" + currentLexeme.toString(), initialRow, initialColumn);
            }
            if (currentCharacter == '\n') {
                throw new LexicalException("String sin cerrar, salto de línea encontrado", "\"" + currentLexeme.toString(), initialRow, initialColumn);
            }
            if (currentLexeme.length() >= MAX_STRING_LENGTH) {
                String lexeme = currentLexeme.append((char) currentCharacter).toString();
                throw new LexicalException("La longitud del string excede los " + MAX_STRING_LENGTH + " caracteres", "\"" + lexeme + "...", initialRow, initialColumn);
            }
            currentLexeme.append((char) currentCharacter);
            currentCharacter = readCharacter();
        }
        currentCharacter = readCharacter();
        if (currentLexeme.isEmpty()) {
            throw new LexicalException("El string está vacío", "\"\"", initialRow, initialColumn);
        }
        return new Token("StrLiteral", currentLexeme.toString(), initialRow, initialColumn);
    }

    private Token processCommentOrDivision(int initialRow, int initialColumn) {
        currentCharacter = readCharacter();
        if (currentCharacter == '/') {
            processSingleLineComment(initialRow, initialColumn);
            return nextToken();
        } else if (currentCharacter == '*') {
            processMultiLineComment(initialRow, initialColumn);
            return nextToken();
        } else {
            return new Token("op_div", "/", initialRow, initialColumn);
        }
    }

    private Token processEqualsOrAssign(int initialRow, int initialColumn) {
        currentCharacter = readCharacter();
        if (currentCharacter == '=') {
            currentLexeme.append((char) currentCharacter);
            currentCharacter = readCharacter();
            return new Token("equalsOp", currentLexeme.toString(), initialRow, initialColumn);
        } else {
            return new Token("assignOp", "=", initialRow, initialColumn);
        }
    }

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
                throw new LexicalException("Literal doble inválido, falta la parte fraccionaria", currentLexeme.toString(), initialRow, initialColumn);
            }
            while (Character.isDigit(currentCharacter)) {
                currentLexeme.append((char) currentCharacter);
                currentCharacter = readCharacter();
            }
            if (currentCharacter == '.') {
                throw new LexicalException("Literal doble inválido, múltiples puntos decimales", currentLexeme.toString() + ".", initialRow, initialColumn);
            }
        }
        if (Character.isLetter(currentCharacter)) {
            String lexeme = currentLexeme.append((char) currentCharacter).toString();
            throw new LexicalException("Literal numérico inválido", lexeme, initialRow, initialColumn);
        }
        String tokenType = isDouble ? "doubleLiteral" : "intLiteral";
        return new Token(tokenType, currentLexeme.toString(), initialRow, initialColumn);
    }

    private Token processLessOrLessEqual(int initialRow, int initialColumn) {
        currentCharacter = readCharacter();
        if (currentCharacter == '=') {
            currentLexeme.append((char) currentCharacter);
            currentCharacter = readCharacter();
            return new Token("lessEqOp", currentLexeme.toString(), initialRow, initialColumn);
        } else {
            return new Token("lessOp", "<", initialRow, initialColumn);
        }
    }

    private Token processGreaterOrGreaterEqual(int initialRow, int initialColumn) {
        currentCharacter = readCharacter();
        if (currentCharacter == '=') {
            currentLexeme.append((char) currentCharacter);
            currentCharacter = readCharacter();
            return new Token("greaterEqOp", currentLexeme.toString(), initialRow, initialColumn);
        } else {
            return new Token("greaterOp", ">", initialRow, initialColumn);
        }
    }

    private Token processAddOrIncrement(int initialRow, int initialColumn) {
        currentCharacter = readCharacter();
        if (currentCharacter == '+') {
            currentLexeme.append((char) currentCharacter);
            currentCharacter = readCharacter();
            return new Token("incrementOp", currentLexeme.toString(), initialRow, initialColumn);
        } else {
            return new Token("addOp", "+", initialRow, initialColumn);
        }
    }

    private Token processAndOperator(int initialRow, int initialColumn) {
        currentCharacter = readCharacter();
        if (currentCharacter == '&') {
            currentLexeme.append((char) currentCharacter);
            currentCharacter = readCharacter();
            return new Token("andOp", "&&", initialRow, initialColumn);
        } else {
            throw new LexicalException("Operación 'and' inválida, se esperaba '&&'", "&", initialRow, initialColumn);
        }
    }

    private Token processOrOperator(int initialRow, int initialColumn) {
        currentCharacter = readCharacter();
        if (currentCharacter == '|') {
            currentLexeme.append((char) currentCharacter);
            currentCharacter = readCharacter();
            return new Token("orOp", "||", initialRow, initialColumn);
        } else {
            throw new LexicalException("Operación 'or' inválida, se esperaba '||'", "|", initialRow, initialColumn);
        }
    }

    private void processSingleLineComment(int initialRow, int initialColumn) {
        while (currentCharacter != '\n' && currentCharacter != -1) {
            if (currentCharacter > 255) {
                throw new LexicalException("Comentario inválido, contiene caracteres no ASCII", String.valueOf((char) currentCharacter), row, column);
            }
            currentCharacter = readCharacter();
        }
    }

    private void processMultiLineComment(int initialRow, int initialColumn) {
        currentCharacter = readCharacter();
        while (true) {
            if (currentCharacter > 255) {
                throw new LexicalException("Comentario inválido, contiene caracteres no ASCII", String.valueOf((char) currentCharacter), row, column);
            }
            if (currentCharacter == -1) {
                throw new LexicalException("Comentario de varias líneas sin cerrar", "/*", initialRow, initialColumn);
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

    private Token processSingleCharacterToken(int initialRow, int initialColumn) {
        char character = (char) currentCharacter;
        currentCharacter = readCharacter();
        return switch (character) {
            case '(', ')', '{', '}', '[', ']', ',', ';', '.', '-', '*', '!' ->
                // Token de un solo carácter válido, no necesita una lógica compleja aquí
                    new Token(getTokenTypeForChar(character), String.valueOf(character), initialRow, initialColumn);
            default ->
                // Error de símbolo, el lexema es el propio carácter no reconocido.
                    throw new LexicalException("Símbolo no pertenece al lenguaje", String.valueOf(character), initialRow, initialColumn);
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