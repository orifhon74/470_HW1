public class Parser {
    public static final int OP = 10;        // +  -  *  /
    public static final int RELOP = 11;     // <  >  <=  >=  ...
    public static final int LPAREN = 12;    // (
    public static final int RPAREN = 13;    // )
    public static final int SEMI = 14;      // ;
    public static final int COMMA = 15;     // ,
    public static final int INT = 16;       // int
    public static final int NUM = 17;       // number
    public static final int ID = 18;        // identifier
    public static final int PRINT = 19;     // print
    public static final int VAR = 20;       // variable
    public static final int FUNC = 21;      // function
    public static final int IF = 22;        // if
    public static final int THEN = 23;      // then
    public static final int ELSE = 24;      // else
    public static final int WHILE = 25;     // while
    public static final int VOID = 26;      // void function
    public static final int BEGIN = 27;     // {
    public static final int END = 28;       // }
    public static final int ASSIGN = 29;    // :=
    public static final int TYPEOF = 30;      // ::
    public static final int FLOAT = 31;

    Compiler compiler;
    Lexer lexer;     // lexer.yylex() returns token-name
    public ParserVal yylval;    // yylval contains token-attribute

    public Parser(java.io.Reader r, Compiler compiler) throws Exception {
        this.compiler = compiler;
        this.lexer = new Lexer(r, this);
    }

    // 1. parser call lexer.yylex that should return (token-name, token-attribute)
    // 2. lexer
    //    a. assign token-attribute to yyparser.yylval
    //       token attribute can be lexeme, line number, colume, etc.
    //    b. return token-id defined in Parser as a token-name
    // 3. parser print the token on console
    //    if there was an error (-1) in lexer, then print error message
    // 4. repeat until EOF (0) is reached

    public String getTokenName(int tokenId) {
        switch (tokenId) {
            case OP: return "OP";
            case RELOP: return "RELOP";
            case LPAREN: return "LPAREN";
            case RPAREN: return "RPAREN";
            case SEMI: return "SEMI";
            case ASSIGN: return "ASSIGN";
            case TYPEOF: return "TYPEOF";
            case COMMA: return "COMMA";
            case ID: return "ID";
            case NUM: return "NUM";
            case IF: return "IF";
            case INT: return "INT";
            case PRINT: return "PRINT";
            case VAR: return "VAR";
            case FUNC: return "FUNC";
            case THEN: return "THEN";
            case ELSE: return "ELSE";
            case WHILE: return "WHILE";
            case VOID: return "VOID";
            case BEGIN: return "BEGIN";
            case END: return "END";
            default: return "UNKNOWN";
        }
    }

    public int yyparse() throws Exception {
        while (true) {
            int token = lexer.yylex();  // get next token-name
            Object attr = yylval.obj;   // get token-attribute
            String tokenname = getTokenName(token);

            if (token == 0) {
                // EOF is reached
                System.out.println("Success!");
                return 0;
            }
            if (token == -1) {
                // lexical error is found
                System.out.println("Error! There is a lexical error at " + lexer.lineno + ":" + lexer.column + ".");
                return -1;
            }

            System.out.println("<" + tokenname + ", token-attr:\"" + attr + "\", " + lexer.lineno + ":" + lexer.column + ">");
        }
    }
}
