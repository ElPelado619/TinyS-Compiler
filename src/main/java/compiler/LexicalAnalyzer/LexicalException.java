package compiler.LexicalAnalyzer;

/**
 * Excepción personalizada para errores léxicos.
 * MODIFICADO: Almacena el lexema inválido y formatea el mensaje de error de salida.
 */
public class LexicalException extends RuntimeException {

    private final int row;
    private final int column;
    private final String errorMessage;
    private final String invalidLexeme;

    public LexicalException(String message, String invalidLexeme, int row, int column) {
        // Pasamos un mensaje simple al constructor de la superclase.
        // El mensaje formateado se generará en el método getMessage().
        super(message);
        this.errorMessage = message;
        this.invalidLexeme = invalidLexeme;
        this.row = row;
        this.column = column;
    }

    /**
     * Sobreescribe el método getMessage para devolver el error con el formato especificado.
     * @return El mensaje de error formateado.
     */
    @Override
    public String getMessage() {
        return String.format(
                "ERROR: LEXICO\n" +
                "| NUMERO DE LINEA (NUMERO DE COLUMNA) | DESCRIPCION: |\n" +
                "| LINEA %d (COLUMNA %d) | %s: %s",
                this.row, this.column, this.errorMessage, this.invalidLexeme
        );
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public String getInvalidLexeme() {
        return invalidLexeme;
    }
}