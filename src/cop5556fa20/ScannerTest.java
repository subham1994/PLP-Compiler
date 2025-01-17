/**
 * Example JUnit tests for the Scanner in the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Fall 2020.  
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Fall 2020 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2020
 *
 */
package cop5556fa20;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cop5556fa20.Scanner.LexicalException;
import cop5556fa20.Scanner.Token;

import static cop5556fa20.Scanner.Kind.*;

@SuppressWarnings("preview") //text blocks are preview features in Java 14

class ScannerTest {
	
	//To make it easy to print objects and turn this output on and off.
	static final boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}
	
	char[] stringChars(String s){
		 return Arrays.copyOf(s.toCharArray(), s.length()); // input string terminated with null char
	}
	
	void showChars(char[] chars){
		System.out.println("index\tascii\tcharacter");
		for(int i = 0; i < chars.length; ++i) {
			char ch = chars[i];
			System.out.println(i + "\t" + (int)ch + "\t" + ch);
		}
	}


	/**
	 * Retrieves the next token and checks that its kind, position, length, line, and position in line
	 * match the given parameters.
	 * 
	 * @param scanner
	 * @param kind
	 * @param pos
	 * @param length
	 * @param line
	 * @param pos_in_line
	 * @return  the Token that was retrieved
	 */
	Token checkNext(Scanner scanner, Scanner.Kind kind, int pos, int length, int line, int pos_in_line) {
		Token t = scanner.nextToken();
		Token expected = new Token(kind,pos,length,line,pos_in_line);
		assertEquals(expected, t);
		return t;
	}
	
	
	/**
	 *Retrieves the next token and checks that it is an EOF token. 
	 *Also checks that this was the last token.
	 *
	 * @param scanner
	 * @return the Token that was retrieved
	 */
	
	Token checkNextIsEOF(Scanner scanner) {
		Token token = scanner.nextToken();
		assertEquals(Scanner.Kind.EOF, token.kind());
		assertFalse(scanner.hasTokens());
		return token;
	}
	
	/**
	 * Simple test case with a (legal) empty program
	 *   
	 * @throws LexicalException
	 */
	@Test
	public void testEmpty() throws Scanner.LexicalException {
		String input = "";  //The input is the empty string.  This is legal
		show(input);        //Display the input
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		checkNextIsEOF(scanner);  //Check that the only token is the EOF token.
	}	
	
	
	/**
	 * Test illustrating how to check content of tokens.
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void testSemi() throws Scanner.LexicalException {		
		
		String input = """
				;;
				;;
				""";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		checkNext(scanner, SEMI, 0, 1, 1, 1);
		checkNext(scanner, SEMI, 1, 1, 1, 2);
		checkNext(scanner, SEMI, 3, 1, 2, 1);
		checkNext(scanner, SEMI, 4, 1, 2, 2);
		checkNextIsEOF(scanner);
	}
	
	/**
	 * Test verifying symbols and escape characters.
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void testSymbols() throws Scanner.LexicalException {		
		
		String input = """
				!!=>=><=!>>
				<<>=
				""";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		checkNext(scanner, EXCL, 0, 1, 1, 1);
		checkNext(scanner, NEQ, 1, 2, 1, 2);
		checkNext(scanner, GE, 3, 2, 1, 4);
		checkNext(scanner, GT, 5, 1, 1, 6);
		checkNext(scanner, LE, 6, 2, 1, 7);
		checkNext(scanner, EXCL, 8, 1, 1, 9);
		checkNext(scanner, RPIXEL, 9, 2, 1, 10);
		checkNext(scanner, LPIXEL, 12, 2, 2, 1);
		checkNext(scanner, GE, 14, 2, 2, 3);
		checkNextIsEOF(scanner);
	}
	
	/**
	 * Test more symbols and Line terminators.
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void testSymbolsAndLineTerms () throws Scanner.LexicalException {		
		
		String input = """
				!!=>=><=!>>-+<-->-\n\r\r\n\n\r<<<
				""";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		checkNext(scanner, EXCL, 0, 1, 1, 1);
		checkNext(scanner, NEQ, 1, 2, 1, 2);
		checkNext(scanner, GE, 3, 2, 1, 4);
		checkNext(scanner, GT, 5, 1, 1, 6);
		checkNext(scanner, LE, 6, 2, 1, 7);
		checkNext(scanner, EXCL, 8, 1, 1, 9);
		checkNext(scanner, RPIXEL, 9, 2, 1, 10);
		checkNext(scanner, MINUS, 11, 1, 1, 12);
		checkNext(scanner, PLUS, 12, 1, 1, 13);
		checkNext(scanner, LARROW, 13, 2, 1, 14);
		checkNext(scanner, RARROW, 15, 2, 1, 16);
		checkNext(scanner, MINUS, 17, 1, 1, 18);
		checkNext(scanner, LPIXEL, 24, 2, 6, 1);
		checkNext(scanner, LT, 26, 1, 6, 3);
		checkNextIsEOF(scanner);
	}
	
	/**
	 * Test Integer literals.
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void testIntLits () throws Scanner.LexicalException {
		String input = """
				0 1234 9 -3456
				""";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Token t0 = checkNext(scanner, INTLIT, 0, 1, 1, 1);
		assertEquals(0, scanner.intVal(t0));
		Token t1 = checkNext(scanner, INTLIT, 2, 4, 1, 3);
		assertEquals(1234, scanner.intVal(t1));
		Token t2 = checkNext(scanner, INTLIT, 7, 1, 1, 8);
		assertEquals(9, scanner.intVal(t2));
		checkNext(scanner, MINUS, 9, 1, 1, 10);
		Token t3 = checkNext(scanner, INTLIT, 10, 4, 1, 11);
		assertEquals(3456, scanner.intVal(t3));
		checkNextIsEOF(scanner);
	}
	
	
	/**
	 * Test throws excption if integer value is out of bound
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void testIntOutOfRange () throws Scanner.LexicalException {
		String input = """
				0 1234 444444444444444444
				""";
		show(input);
		Exception exception = assertThrows(LexicalException.class, () -> {new Scanner(input).scan();});
		show(exception);
	}
	
	
	/**
	 * Test throws excption if input has invalid characters
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void testInvalidInput () throws Scanner.LexicalException {
		String input = """
				"\\"
				""";
		show(input);
		Exception exception = assertThrows(Scanner.LexicalException.class, () -> {new Scanner(input).scan();});
		show(exception);
	}
	
	
	/**
	 * Test keywords and constants.
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void testKeywordsAndConst () throws Scanner.LexicalException {		
		
		String input = """
				ijBLUEc NAVY screenX screen\nX
				""";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Token t0 = checkNext(scanner, IDENT, 0, 7, 1, 1);
		assertEquals("ijBLUEc", scanner.getText(t0));
		Token t = checkNext(scanner, CONST, 8, 4, 1, 9);
		assertEquals(0xff000080, scanner.intVal(t));
		Token t1 = checkNext(scanner, IDENT, 13, 7, 1, 14);
		assertEquals("screenX", scanner.getText(t1));
		checkNext(scanner, KW_SCREEN, 21, 6, 1, 22);
		checkNext(scanner, KW_X, 28, 1, 2, 1);
		checkNextIsEOF(scanner);
	}
	
	/**
	 * Test comments
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void testComments () throws Scanner.LexicalException {		
		
		String input = """
				ijBLUEc //NAVY screenX screen\nX
				""";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Token t0 = checkNext(scanner, IDENT, 0, 7, 1, 1);
		assertEquals("ijBLUEc", scanner.getText(t0));
		checkNext(scanner, KW_X, 30, 1, 2, 1);
		checkNextIsEOF(scanner);
	}
	
	/**
	 * Another example test, this time with an ident.  While simple tests like this are useful,
	 * many errors occur with sequences of tokens, so make sure that you have more complex test cases
	 * with multiple tokens and test the edge cases. 
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void testIdent() throws LexicalException {
		String input = "ij abc123 123abc c";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		Token t0 = checkNext(scanner, IDENT, 0, 2, 1, 1);
		assertEquals("ij", scanner.getText(t0));
		
		Token t1 = checkNext(scanner, IDENT, 3, 6, 1, 4);
		assertEquals("abc123", scanner.getText(t1));
		
		Token t = checkNext(scanner, INTLIT, 10, 3, 1, 11);
		assertEquals(123, scanner.intVal(t));
		
		Token t2 = checkNext(scanner, IDENT, 13, 3, 1, 14);
		assertEquals("abc", scanner.getText(t2));
		
		Token t3 = checkNext(scanner, IDENT, 17, 1, 1, 18);
		assertEquals("c", scanner.getText(t3));
		checkNextIsEOF(scanner);
	}
	
	
	/**
	 * This example shows how to test that your scanner is behaving when the
	 * input is illegal.  In this case, a String literal
	 * that is missing the closing ".  
	 * 
	 * In contrast to Java String literals, the text block feature simply passes the characters
	 * to the scanner as given, using a LF (\n) as newline character.  If we had instead used a 
	 * Java String literal, we would have had to escape the double quote and explicitly insert
	 * the LF at the end:  String input = "\"greetings\n";
	 * 
	 * assertThrows takes the class of the expected exception and a lambda with the test code in the body.
	 * The test passes if the expected exception is thrown.  The Exception object is returned and
	 * an be printed.  It should contain an appropriate error message. 
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void failUnclosedStringLiteral() throws LexicalException {
		String input = """
				"greetings
				""";
		show(input);
		Exception exception = assertThrows(LexicalException.class, () -> {new Scanner(input).scan();});
		show(exception);
	}
	
	@Test
	public void testStringBasic() throws LexicalException {
		String input =  """
              \n "Example\\nString" 
              """;
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		checkNext(scanner, STRINGLIT, 2, 17, 2, 2);
		checkNextIsEOF(scanner);
	}
	
	
	@Test
	public void testMixed() throws LexicalException {
		String input =  """
              ijBLUEc //NAVY screenX screen\nX\n "Example\\'Strin\\ng\\r\\n" 123+
              """;
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Token t0 = checkNext(scanner, IDENT, 0, 7, 1, 1);
		assertEquals("ijBLUEc", scanner.getText(t0));
		checkNext(scanner, KW_X, 30, 1, 2, 1);
		Token t1 = checkNext(scanner, STRINGLIT, 33, 23, 3, 2);
		String text = scanner.getText(t1);
		System.out.println("Token text");
		
		showChars(stringChars(text));
		assertEquals("Example\'Strin\ng\r\n", scanner.getText(t1));
		checkNext(scanner, INTLIT, 57, 3, 3, 26);
		checkNext(scanner, PLUS, 60, 1, 3, 29);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testFailedA () throws LexicalException {
		String input = "= == === ==== =; =\n =\n=\n";
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		checkNext(scanner, ASSIGN, 0, 1, 1, 1);
		checkNext(scanner, EQ, 2, 2, 1, 3);
		checkNext(scanner, EQ, 5, 2, 1, 6);
		checkNext(scanner, ASSIGN, 7, 1, 1, 8);
		checkNext(scanner, EQ, 9, 2, 1, 10);
		checkNext(scanner, EQ, 11, 2, 1, 12);
		checkNext(scanner, ASSIGN, 14, 1, 1, 15);
		checkNext(scanner, SEMI, 15, 1, 1, 16);
		checkNext(scanner, ASSIGN, 17, 1, 1, 18);
		checkNext(scanner, ASSIGN, 20, 1, 2, 2);
		checkNext(scanner, ASSIGN, 22, 1, 3, 1);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testFailedB () throws LexicalException {
		String input = """
				<<- ->>\n<<== <= <<=\n
				""";
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		checkNext(scanner, LPIXEL, 0, 2, 1, 1);
		checkNext(scanner, MINUS, 2, 1, 1, 3);
		checkNext(scanner, RARROW, 4, 2, 1, 5);
		checkNext(scanner, GT, 6, 1, 1, 7);
		checkNext(scanner, LPIXEL, 8, 2, 2, 1);
		checkNext(scanner, EQ, 10, 2, 2, 3);
		checkNext(scanner, LE, 13, 2, 2, 6);
		checkNext(scanner, LPIXEL, 16, 2, 2, 9);
		checkNext(scanner, ASSIGN, 18, 1, 2, 11);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testFailedC () throws LexicalException {
		String input = """
				//---\nabc def // ...\n33 jkl\n//xx\n
				""";
		
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		checkNext(scanner, IDENT, 6, 3, 2, 1);
		checkNext(scanner, IDENT, 10, 3, 2, 5);
		checkNext(scanner, INTLIT, 21, 2, 3, 1);
		checkNext(scanner, IDENT, 24, 3, 3, 4);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testFailedD () throws LexicalException {
		String input = """
				x=\"\b\";
				""";
		
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		checkNext(scanner, IDENT, 0, 1, 1, 1);
		checkNext(scanner, ASSIGN, 1, 1, 1, 2);
		Token t1 = checkNext(scanner, STRINGLIT, 2, 3, 1, 3);
		checkNext(scanner, SEMI, 5, 1, 1, 6);
		checkNextIsEOF(scanner);
		
		String text = scanner.getText(t1);
		System.out.println("Token text");
		
		showChars(stringChars(text));
		assertEquals("\b", scanner.getText(t1));
		
	}
	
	@Test
	public void testStringEscapeA () throws LexicalException {
		String input = """
				x="abc\\\nef";
				""";
		
		show(input);
		Exception exception = assertThrows(LexicalException.class, () -> {new Scanner(input).scan();});
		show(exception);
		
	}
	
	@Test
	public void testStringEscapeB () throws LexicalException {
		String input = """
				x="\\";
				""";
		
		show(input);
		Exception exception = assertThrows(LexicalException.class, () -> {new Scanner(input).scan();});
		show(exception);
		
	}
	
	@Test
	public void testStringEscapeC () throws LexicalException {
		String input = """
				x="abc\\\\nef";
				""";
		
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		scanner.nextToken();
		scanner.nextToken();
		String stringLit = scanner.getText(scanner.nextToken());
		assertEquals("abc\\nef", stringLit);
	}
}
