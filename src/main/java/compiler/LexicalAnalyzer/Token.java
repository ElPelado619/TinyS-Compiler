package compiler.LexicalAnalyzer;

public class Token {

    private String token;
    private String lexeme;
    private int row;
    private int column;


    public Token(String s, String s1, int initialRow, int initialColumn) {
        this.token = s;
        this.lexeme = s1;
        this.row = initialRow;
        this.column = initialColumn;
    }

    public Object getToken() {
        return token;
    }

    public Object getLexeme() {
        return lexeme;
    }

    public Object getRow() {
        return row;
    }

    public Object getColumn() {
        return column;
    }
}
