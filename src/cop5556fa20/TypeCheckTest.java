package cop5556fa20;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

import cop5556fa20.AST.ASTNode;
import cop5556fa20.AST.Program;
import cop5556fa20.Parser.SyntaxException;
import cop5556fa20.Scanner.LexicalException;
import cop5556fa20.TypeCheckVisitor.TypeException;

@SuppressWarnings("preview")
class TypeCheckTest {

	/*
	 * To make it easy to print objects and turn this output on and off.
	 */
	static final boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}
	
	/**
	 * Test input program that is expected to be syntactically correct and pass type checking.
	 * @param input   
	 * @throws Exception
	 */
	void pass(String input) throws Exception {
		Program prog = parseAndGetProgram(input);		
		TypeCheckVisitor v = new TypeCheckVisitor();
		prog.visit(v, null);
		show(prog);
	}
	
	
	/**
	 * Test input program that is expected to be syntactically correct, but fails type checking.
	 * @param input
	 * @throws LexicalException
	 * @throws SyntaxException
	 */
	void fail(String input) throws LexicalException, SyntaxException {
		Program prog = parseAndGetProgram(input);
		//show(prog);  //Display the AST
		TypeCheckVisitor v = new TypeCheckVisitor();
		Exception exception = assertThrows(TypeException.class, () -> {
			prog.visit(v, null);
		});
		show(exception);
	}
	
	//creates and returns a parser for the given input.
	private Parser makeParser(String input) throws LexicalException {
		show(input);        //Display the input 
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		//show(scanner);   //Display the Scanner
		Parser parser = new Parser(scanner);
		return parser;
	}
	
	Program parseAndGetProgram(String input) throws LexicalException, SyntaxException{
		Parser parser = makeParser(input);
		ASTNode node = parser.parse();
		assertFalse(parser.scanner.hasTokens());
		return (Program) node;
	}
	
	@Test
	public void testEmpty() throws Exception {
		String input = "";  //The input is the empty string.  This is legal
		Program prog = parseAndGetProgram(input);
		TypeCheckVisitor v = new TypeCheckVisitor();
		prog.visit(v, null);
	}	
	
	
	/**
	 * This is one of the simplest nonempty correct programs.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testdec0() throws Exception {
		String input = """
				int x;
				""";
		pass(input);
	}

	@Test
	public void testdec1() throws Exception {
		String input = """
    			int a = 2;
				int x = a + @1;
				""";
		pass(input);
	}

	@Test
	public void testdec2() throws Exception {
		String input = """
    			string s = "Hello, ";
				string x = s + @1;
				""";
		pass(input);
	}

	@Test
	public void exprArgFail() throws Exception {
		String input = """
    			string s = "Hello, ";
				int x = s + @1;
				""";
		fail(input);
	}

	/**
	 * This program fails type checking due to the attempt to redefine x
	 * @throws Exception
	 */
	@Test
	public void testdec1_fail() throws Exception {
		String input = """
				int x;
				string x;
				""";
		fail(input);
	}

	/**
	 * This program fails type checking due to undefined variable
	 * @throws Exception
	 */
	@Test
	public void testdec2_fail() throws Exception {
		String input = """
				int x = +a + b;
				""";
		fail(input);
	}

	/**
	 * This program fails type checking due to type mismatch
	 * @throws Exception
	 */
	@Test
	public void testdec3_fail() throws Exception {
		String input = """
				int x = "fail";
				""";
		fail(input);
	}

	@Test
	public void testImagePass () throws Exception {
		String input = """
    			string url = "source to image";
				image [1000, Z] im_a <- url;
				image [X, Y] im_e <- url;
				image im_b = im_a;
				image im_c <- @1;
				image im_d <- @1 + url;
				image im;
				""";
		pass(input);
	}

	@Test
	public void testImagefailA () throws Exception {
		String input = """
				image im = @1;
				""";
		fail(input);
	}

	@Test
	public void testExprPass () throws Exception {
		String input = """
				int a = @1 + @2 - @3 / @4 * MAROON;
				int b = @1 - 5;
				b = 2 + (3 - 4) * 6 * 8 / (9 + a) + b;
				int d = !(3 < 4) ? 0 : 1;
				string s = @a + @b;
				""";
		pass(input);
	}

	@Test
	public void testExprStringLitfail () throws Exception {
		String input = """
				string a = @1 - @2;
				""";
		fail(input);
	}

	@Test
	public void testExprCondExprFailA () throws Exception {
		String input = """
				int d = (3 + 4) ? 0 : 1;
				""";
		fail(input);
	}

	@Test
	public void testCondExprFailB () throws Exception {
		String input = """
       			int x = @0 == @1 ? 0 : 1;
       			x -> screen;
				""";
		fail(input);
	}

	@Test
	public void testCondExprFailC () throws Exception {
		String input = """
       			int x = 1 == (@0 + @1) ? 0 : 1;
       			x -> screen;
				""";
		fail(input);
	}

	@Test
	public void testStatementOutScreenPass () throws Exception {
		String input = """
				string url = "source to image";
				image [1000, Z] im_a <- url;
				url -> screen;
				""";
		pass(input);
	}

	@Test
	public void testStatementOutFilePass () throws Exception {
		String input = """
				string url = "source to image";
				image [1000, Z] im_a <- url;
				im_a -> url;
				""";
		pass(input);
	}

	@Test
	public void testStatementImageInPass () throws Exception {
		String input = """
				string url = "source to image";
				image [1000, Z] im_a <- url;
				im_a <- url;
				im_a <- im_a;
				""";
		pass(input);
	}

	@Test
	public void testStatementImageInFail () throws Exception {
		String input = """
				string url = "source to image";
				image [1000, Z] im_a <- url;
				im_a <- 3;
				""";
		fail(input);
	}

	@Test
	public void testStatementAssignFail () throws Exception {
		String input = """
				int a;
				a = "hello";
				""";
		fail(input);
	}

	@Test
	public void testStatementLoopPass () throws Exception {
		String input = """
				int a;
				image [1000, Z] im;
				im =* [X, Y] : (3 > 4) : a;
				""";
		pass(input);
	}

	@Test
	public void testExprHashPass () throws Exception {
		String input = """
				int h = Z # red;
				h = 1000 # green;
				image im <- @1;
				h = im # width;
				int i = im # width;
				""";
		pass(input);
	}

	@Test
	public void testExprHashIntFail () throws Exception {
		String input = """
				int h = Z # height;
				""";
		fail(input);
	}

	@Test
	public void testExprHashImageFail () throws Exception {
		String input = """
    			image im <- @1;
				int i = im # red;
				""";
		fail(input);
	}

	@Test
	public void testPixelPass () throws Exception {
		String input = """
       			image im <- @1;
    			int a = im [4, @1];
    			int b = << 3, @0, 5>>;
				""";
		pass(input);
	}
}