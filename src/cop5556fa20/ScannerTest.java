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
				!!=>=><=!>>-+<-->-\n\r\r\n\n\r<<
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
				0 1234
				""";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		checkNext(scanner, INTLIT, 0, 1, 1, 1);
		checkNext(scanner, INTLIT, 2, 4, 1, 3);
		checkNextIsEOF(scanner);
		
	}
	
	public void testIntOutOfRange () throws Scanner.LexicalException {
		String input = """
				0 1234 444444444444444444
				""";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Exception exception = assertThrows(LexicalException.class, () -> {new Scanner(input).scan();});
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
		checkNext(scanner, CONST, 8, 4, 1, 9);
		Token t1 = checkNext(scanner, IDENT, 13, 7, 1, 14);
		assertEquals("screenX", scanner.getText(t1));
		checkNext(scanner, KW_SCREEN, 21, 6, 1, 22);
		checkNext(scanner, KW_X, 28, 1, 2, 1);
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
		String input = "ij abc123";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		Token t0 = checkNext(scanner, IDENT, 0, 2, 1, 1);
		assertEquals("ij", scanner.getText(t0));
		
		Token t1 = checkNext(scanner, IDENT, 3, 6, 1, 4);
		assertEquals("abc123", scanner.getText(t1));
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
//	@Test
//	public void failUnclosedStringLiteral() throws LexicalException {
//		String input = """
//				"greetings
//				""";
//		show(input);
//		Exception exception = assertThrows(LexicalException.class, () -> {new Scanner(input).scan();});
//		show(exception);
//	}
	

}
