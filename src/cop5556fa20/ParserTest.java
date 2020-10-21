/**
 * Test class for  for the class project in COP5556 Programming Language Principles 
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

import static cop5556fa20.AST.ASTTestLambdas.*;
import static cop5556fa20.AST.ASTTestLambdas.checkStatementLoop;
import static cop5556fa20.Scanner.Kind.*;
import static org.junit.Assert.assertEquals;
import java.util.List;

import cop5556fa20.AST.*;
import org.junit.jupiter.api.Test;

import cop5556fa20.Parser.SyntaxException;
import cop5556fa20.Scanner.LexicalException;

/**
 * @author Beverly Sanders
 *
 */
@SuppressWarnings("preview") // text blocks are preview features in Java 14

class ParserTest {

	// To make it easy to print objects and turn this output on and off.
	static final boolean doPrint = true;

	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	// creates and returns a parser for the given input.
	private Parser makeParser(String input) throws LexicalException {
		show(input); // Display the input
		Scanner scanner = new Scanner(input).scan(); // Create a Scanner and initialize it
		show(scanner); // Display the Scanner
		Parser parser = new Parser(scanner);
		return parser;
	}

	@Test
	public void testEmpty() throws Scanner.LexicalException, SyntaxException {
		String input = ""; // The input is the empty string. This is legal
		Parser parser = makeParser(input);
		ASTNode node = parser.parse();
		assertEquals(Program.class, node.getClass()); // checks that the Parser returns a Program object
		assertEquals(0, ((Program) node).decOrStatement().size()); // checks that the decOrStatement list is empty
	}

	@Test
	public void testDec0() throws Scanner.LexicalException, SyntaxException {
		String input = """
				int abc;
				string bcd = "a";
				""";
		Parser parser = makeParser(input);
		ASTNode node = parser.parse();
		assertEquals(Program.class, node.getClass()); // checks that parser returns a Program object
		List<ASTNode> decOrStatement = ((Program) node).decOrStatement(); // get the decOrStatements list for
																			// convenience
		assertEquals(2, decOrStatement.size()); // check size of decOrStatements
		DecVar dec0 = (DecVar) decOrStatement.get(0); // get element from decOrStatements. If not a DecVar, the cast
														// will fail
		assertEquals(Type.Int, dec0.type()); // check the type of the variable whose declaration is represented by the
												// dec0 node
		assertEquals("abc", dec0.name()); // check the name of the variable whose declaration is represented by the dec0
											// node
		DecVar dec1 = (DecVar) decOrStatement.get(1); // get element from decOrStatements. If not a DecVar, the cast
														// will fail
		assertEquals(Type.String, dec1.type()); // check the type of the variable whose declaration is represented by
												// the dec1 node
		assertEquals("bcd", dec1.name()); // check the name of the variable whose declaration is represented by the dec1
											// node
		assertEquals("a", (((ExprStringLit) dec1.expression()).text())); //check that the expression is an ExprStringLit with value "a")
	}

	
	/**
	 * This example uses a lambda provided in ASTTestLambdas for testing
	 * convenience.
	 * 
	 */
	@Test
	public void testImageIn0() throws Scanner.LexicalException, SyntaxException {
		String input = """
				abc <- "https://this.is.a.url";
				""";
		Parser parser = makeParser(input);
		ASTNode node = parser.parse();
		List<ASTNode> decOrStatement = ((Program) node).decOrStatement(); // gets the decOrStatement list. The cast will
																			// fail if the node returned from parse is
																			// not a Program.
		ASTNode s0 = decOrStatement.get(0); // get the statement from decOrStatement.
		/**
		 * build up a test using the lambdas, and execute it by calling test. for this
		 * case, we expect s0 to be a StatementImageIn object, so use
		 * checkStatementImageIn. A StatementImageIn has a name, and an Expression. The
		 * first parameter is the name of the variable on the left hand side. The second
		 * is a Predicate<ASTNode> to check that the Expression is a ExprStringLit with
		 * the given value. The constructed Predicate is then executed by calling its
		 * test method and passing in the ASTNode under test.
		 */
		checkStatementImageIn("abc", checkExprStringLit("https://this.is.a.url")).test(s0);
	}

	/**
	 * Another example that uses lambdas for checking.  This one has a binary expression on the right side of a declaration.
	 * Here, lambdas are used to check each of the subexpressions.  
	 * 
	 * @throws Scanner.LexicalException
	 * @throws SyntaxException
	 */
	@Test
	public void testBinary0() throws Scanner.LexicalException, SyntaxException {
		String input = """
				int abc = 4 + RED;
				""";
		Parser parser = makeParser(input);
		ASTNode node = parser.parse();
		List<ASTNode> decOrStatement = ((Program) node).decOrStatement(); // gets the decOrStatement list. The cast will
																			// fail if the node returned from parse is
																			// not a Program.
		DecVar d0 = (DecVar) decOrStatement.get(0); // gets the declaration. The cast will fail if not a DecVar
		// use checkDecVar lambda, which takes type, name, and a lambda to check the
		// expression.
		checkDecVar(Type.Int, // the type should be int
				"abc", // the variable name should be "abc"
				checkExprBinary( // the expression is a binary expression with operator PLUS
						checkExprIntLit(4), // the left expression is an ExprIntLit with value 4, use checkExprIntLit to
											// check
						checkExprConst("RED", Scanner.constants.get("RED")), // the right expression is an ExprConst.
																				// Use checkExprConst to check name
																				// "RED", and value from table in
																				// Scanner.
						PLUS)).test(d0); // invoke the test method with the decVar to evaluate.
	}

	/**
	 * This example uses a lambda provided in ASTTestLambdas for testing
	 * convenience.
	 * 
	 */
	@Test
	public void testVarDec1() throws Scanner.LexicalException, SyntaxException {
		String input = """
				int abc = 4;
				""";
		Parser parser = makeParser(input);
		ASTNode node = parser.parse();
		List<ASTNode> decOrStatement = ((Program) node).decOrStatement(); 
		DecVar d0 = (DecVar) decOrStatement.get(0);																	
		checkDecVar(Type.Int, "abc", checkExprIntLit(4)).test(d0);
	}

	@Test
	public void testLoopStatementWithCond() throws Scanner.LexicalException, SyntaxException {
		String input = """
				abc =* [X,Y] : ( a - b ) : a * + b;
				""";
		Parser parser = makeParser(input);
		ASTNode node = parser.parse();
		List<ASTNode> decOrStatement = ((Program) node).decOrStatement();
		StatementLoop s = (StatementLoop) decOrStatement.get(0);
		checkStatementLoopWithCond("abc",
				checkExprBinary(checkExprVar("a"), checkExprVar("b"), MINUS),
				checkExprBinary(checkExprVar("a"), checkExprUnary(PLUS, checkExprVar("b")), STAR)
		).test(s);
	}

	@Test
	public void testLoopStatementWithoutCOnd() throws Scanner.LexicalException, SyntaxException {
		String input = """
				im = *[X,Y]:: <<r,g,b>>;\n
				""";
		Parser parser = makeParser(input);
		ASTNode node = parser.parse();
		List<ASTNode> decOrStatement = ((Program) node).decOrStatement();
		StatementLoop s = (StatementLoop) decOrStatement.get(0);
		checkStatementLoop("im",
				checkExprPixelConstructor(checkExprVar("r"), checkExprVar("g"), checkExprVar("b"))
		).test(s);
	}

	@Test
	public void testImageDec() throws Scanner.LexicalException, SyntaxException {
		String input = """
				image[1000,2000]  im= im2;
				""";
		Parser parser = makeParser(input);
		ASTNode node = parser.parse();
		List<ASTNode> decOrStatement = ((Program) node).decOrStatement();
		DecImage d = (DecImage) decOrStatement.get(0);
		checkDecImage(
				"im",
				checkExprIntLit(1000),
				checkExprIntLit(2000),
				ASSIGN,
				checkExprVar("im2")
				).test(d);
	}

	@Test
	public void testStatementOutScreen() throws Scanner.LexicalException, SyntaxException {
		String input = """
				abc -> screen[10, 40];
				""";
		Parser parser = makeParser(input);
		ASTNode node = parser.parse();
		List<ASTNode> decOrStatement = ((Program) node).decOrStatement();
		StatementOutScreen s = (StatementOutScreen) decOrStatement.get(0);
		checkStatementOutScreen("abc", checkExprIntLit(10), checkExprIntLit(40)).test(s);
	}

	@Test
	public void testStatementOutFile() throws Scanner.LexicalException, SyntaxException {
		String input = """
				input -> "Why am I doing this?";
				""";
		Parser parser = makeParser(input);
		ASTNode node = parser.parse();
		List<ASTNode> decOrStatement = ((Program) node).decOrStatement();
		StatementOutFile s = (StatementOutFile) decOrStatement.get(0);
		checkStatementOutFile(
				"input",
				checkExprStringLit("Why am I doing this?")
		).test(s);
	}

	@Test
	public void testStatementAssign() throws Scanner.LexicalException, SyntaxException {
		String input = """
				input = a * + b;
				input = "value";
				""";
		Parser parser = makeParser(input);
		ASTNode node = parser.parse();
		List<ASTNode> decOrStatement = ((Program) node).decOrStatement();
		StatementAssign s = (StatementAssign) decOrStatement.get(0);
		checkStatementAssignment(
				"input",
				checkExprBinary(checkExprVar("a"), checkExprUnary(PLUS, checkExprVar("b")), STAR)
		).test(s);

		s = (StatementAssign) decOrStatement.get(1);
		checkStatementAssignment(
				"input",
				checkExprStringLit("value")
		).test(s);
	}

	@Test
	public void testMixedDec() throws Scanner.LexicalException, SyntaxException {
		String input = """
				int x = @ abc [1000, 2000];
				""";
		Parser parser = makeParser(input);
		ASTNode node = parser.parse();
		List<ASTNode> decOrStatement = ((Program) node).decOrStatement();
		DecVar d = (DecVar) decOrStatement.get(0);
		checkDecVar(
				Type.Int,
				"x",
				checkExprArg(
						checkExprPixelSelector(checkExprVar("abc"),
						checkExprIntLit(1000),
						checkExprIntLit(2000))
		)).test(d);
	}

	@Test
	public void testHashExpression() throws Scanner.LexicalException, SyntaxException {
		String input = """
				int x = @ abc [1000, 2000] # green;
				""";
		Parser parser = makeParser(input);
		ASTNode node = parser.parse();
		List<ASTNode> decOrStatement = ((Program) node).decOrStatement();
		DecVar d = (DecVar) decOrStatement.get(0);
		checkDecVar(
				Type.Int,
				"x",
				checkExprHash(checkExprArg(
						checkExprPixelSelector(checkExprVar("abc"),
								checkExprIntLit(1000),
								checkExprIntLit(2000))
				), "green")
		).test(d);
	}
}