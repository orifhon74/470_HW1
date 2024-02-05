import java.io.IOException;
import java.util.ArrayList;

public class Lexer
{
    private static final char EOF        =  0;
    private static final int BUFFER_SIZE = 10;

    private Parser         yyparser; // parent parser object
    private java.io.Reader reader;   // input stream
    public int             lineno;   // line number
    public int             column;   // column
    public int columnNumber;
    private char lastChar = EOF;

    private final char[] buffer1 = new char[10];
    private final char[] buffer2 = new char[10];
    private boolean activeBuffer = true; // true for buffer1, false for buffer2
    private int bufferIndex = 0; // Index of the current character in the active buffer
    private int bufferLimit1 = 0; // How much buffer1 is filled
    private int bufferLimit2 = 0; // How much buffer2 is filled


    public Lexer(java.io.Reader reader, Parser yyparser) throws Exception
    {
        this.reader   = reader;
        this.yyparser = yyparser;
        lineno = 1;
        column = 0;
        columnNumber = 0;
        fillBuffer(true);
    }

    private void fillBuffer(boolean useBuffer1) throws IOException {
        char[] targetBuffer = useBuffer1 ? buffer1 : buffer2;
        int bytesRead = reader.read(targetBuffer, 0, BUFFER_SIZE);
        if (useBuffer1) {
            bufferLimit1 = bytesRead;
        } else {
            bufferLimit2 = bytesRead;
        }
    }

    private char getNextCharFromBuffer() throws Exception {
        if (activeBuffer) { // If we are reading from buffer1
            if (bufferIndex < bufferLimit1) {
                return buffer1[bufferIndex++];
            } else {
                fillBuffer(false); // Fill buffer2 for the next read
                activeBuffer = false; // Switch to buffer2
                bufferIndex = 0; // Reset index for the new buffer
                if (bufferLimit2 == -1) return EOF; // Check for EOF
                return getNextCharFromBuffer(); // Recursive call to get the next char from the new buffer
            }
        } else { // Reading from buffer2
            if (bufferIndex < bufferLimit2) {
                return buffer2[bufferIndex++];
            } else {
                fillBuffer(true); // Fill buffer1 for the next read
                activeBuffer = true; // Switch back to buffer1
                bufferIndex = 0; // Reset index for the new buffer
                if (bufferLimit1 == -1) return EOF; // Check for EOF
                return getNextCharFromBuffer(); // Recursive call to get the next char from the new buffer
            }
        }
    }

    public char NextChar() throws Exception {
        if (lastChar != EOF) {
            char c = lastChar;
            lastChar = EOF;
            return c;
        }

        char c = getNextCharFromBuffer();
        if (c == EOF) {
            return EOF;
        }

        if (c == '\n') {
            columnNumber = 0; // Reset column number at the start of a new line
        } else {
            columnNumber++; // Increment column number
        }

        return c;
    }

    public void UnreadChar(char c) {
        lastChar = c;
    }

    private Integer checkForKeyword(StringBuilder buffer) {
        String identifier = buffer.toString();
        switch (identifier) {
            case "print": return Parser.PRINT;
            case "var": return Parser.VAR;
            case "func": return Parser.FUNC;
            case "if": return Parser.IF;
            case "then": return Parser.THEN;
            case "else": return Parser.ELSE;
            case "while": return Parser.WHILE;
            case "void": return Parser.VOID;
            case "begin": return Parser.BEGIN;
            case "end": return Parser.END;
            case "int": return Parser.INT;
            default: return null; // Not a keyword
        }
    }

    public int Fail()
    {
        return -1;
    }

    // * If yylex reach to the end of file, return  0
    // * If there is an lexical error found, return -1
    // * If a proper lexeme is determined, return token <token-id, token-attribute> as follows:
    //   1. set token-attribute into yyparser.yylval
    //   2. return token-id defined in Parser
    //   token attribute can be lexeme, line number, colume, etc.
    public int yylex() throws Exception
    {
        StringBuilder buffer = new StringBuilder();
        int state = 0;

        while(true)
        {
            char c = EOF;

            switch(state)
            {
                case 0:
                    c = NextChar();

                    if (Character.isWhitespace(c)) {
                        if (c == '\n') {
                            lineno++;
                            columnNumber = 0;
                        }
                        continue;
                    }

                    column = columnNumber;

                    if(c == ';') { state = 1; continue; }
                    if(c == '+') { state = 2; continue; }
                    if(c == '-') { state = 3; continue; }
                    if(c == '*') { state = 4; continue; }
                    if(c == '/') { state = 5; continue; }
                    if(c == '(') { state = 6; continue; }
                    if(c == ')') { state = 7; continue; }
                    if(c == ':') { state = 8; continue; }
                    if(c == '<') { state = 9; continue; }
                    if(c == '>') { state = 10; continue; }
                    if(c == ',') { state = 11; continue; }
                    if(c == '=') { state = 12; continue; }

                    if (Character.isDigit(c)) {
                        buffer.append(c);
                        state = 13;
                        continue;
                    }
                    // check for identifier
                    if (Character.isLetter(c) || c == '_') {
                        buffer.append(c);
                        state = 14;
                        continue;
                    }
                    if(c == EOF) { state = 9999; continue; }
                    return Fail();
                case 1:
                    yyparser.yylval = new ParserVal((Object)";");   // set token-attribute to yyparser.yylval
                    return Parser.SEMI;                             // return token-name
                case 2:
                    yyparser.yylval = new ParserVal((Object)"+");
                    return Parser.OP;
                case 3:
                    yyparser.yylval = new ParserVal((Object)"-");
                    return Parser.OP;
                case 4:
                    yyparser.yylval = new ParserVal((Object)"*");
                    return Parser.OP;
                case 5:
                    yyparser.yylval = new ParserVal((Object)"/");
                    return Parser.OP;
                case 6:
                    yyparser.yylval = new ParserVal((Object)"(");
                    return Parser.LPAREN;
                case 7:
                    yyparser.yylval = new ParserVal((Object)")");
                    return Parser.RPAREN;
                case 8:
                    c = NextChar();
                    if(c == '=') {
                        yyparser.yylval = new ParserVal((Object)":=");
                        return Parser.ASSIGN;
                    }
                    if(c == ':') {
                        yyparser.yylval = new ParserVal((Object)"::");
                        return Parser.TYPEOF;
                    }
                    return Fail();
                case 9:
                    c = NextChar();
                    if(c == '=') {
                        yyparser.yylval = new ParserVal((Object)"<=");
                        return Parser.RELOP;
                    }
                    else if(c == '>') {
                        yyparser.yylval = new ParserVal((Object)"<>");
                        return Parser.RELOP;
                    }
                    UnreadChar(c);
                    yyparser.yylval = new ParserVal((Object) "<");
                    return Parser.RELOP;
                case 10:
                    c = NextChar();
                    if(c == '=') {
                        yyparser.yylval = new ParserVal((Object)">=");
                        return Parser.RELOP;
                    }
                    UnreadChar(c);
                    yyparser.yylval = new ParserVal((Object) ">");
                    return Parser.RELOP;
                case 11:
                    yyparser.yylval = new ParserVal((Object)",");
                    return Parser.COMMA;
                case 12:
                    yyparser.yylval = new ParserVal((Object)"=");
                    return Parser.RELOP;
                case 13:
                    c = NextChar();
                    // If still a digit, keep building the number
                    if (Character.isDigit(c)) {
                        buffer.append(c);
                        continue;
                    } else if (c == '.') {
                        buffer.append(c);
                        state = 15;
                        continue;
                    } else {
                        // When no more digits, return the integer number token
                        UnreadChar(c);
                        yyparser.yylval = new ParserVal((Object)Integer.parseInt(buffer.toString()));
                        buffer.setLength(0);
                        return Parser.NUM;
                    }
                case 14:
                    c = NextChar();
                    // If a letter, digit, or underscore, keep building the identifier
                    if (Character.isLetterOrDigit(c) || c == '_') {
                        buffer.append(c);
                        continue;
                    } else {
                        if (buffer.charAt(0) == '_') {
                            return Fail();
                        }
                        Integer keywordToken = checkForKeyword(buffer);
                        UnreadChar(c);
                        yyparser.yylval = new ParserVal((Object) buffer.toString());
                        buffer.setLength(0);
                        if (keywordToken != null) {
                            return keywordToken;
                        } else {
                            return Parser.ID;
                        }
                    }
                case 15: // State for handling the decimal part of floating point numbers
                    c = NextChar();
                    if (Character.isDigit(c)) {
                        buffer.append(c);
                        continue;
                    } else {
                        // Error if the buffer ends with a period and no digits follow
                        if (buffer.charAt(buffer.length() - 1) == '.') {
                            return Fail();
                        }
                        UnreadChar(c);
                        yyparser.yylval = new ParserVal((Object)Float.parseFloat(buffer.toString()));
                        buffer.setLength(0);
                        return Parser.NUM;
                    }

                case 9999:
                    return EOF;                                     // return end-of-file symbol
            }

        }
    }

}
