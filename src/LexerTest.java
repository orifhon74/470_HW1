//import java.io.FileReader;
//
//public class LexerTest {
//    public static void main(String[] args) {
//
//        try {
//            FileReader fileReader = new FileReader("src/test0.minc");
//            Lexer lexer = new Lexer(fileReader, null); // Assuming the Parser is not needed for the test
//
//            int tokenId;
//            while ((tokenId = lexer.yylex()) != Lexer.EOF) {
//                System.out.println("Token ID: " + tokenId + ", Value: " + lexer.yyparser.yylval.obj);
//            }
//
//            fileReader.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}

