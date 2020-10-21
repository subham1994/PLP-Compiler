/**
 * Parser for the class project in COP5556 Programming Language Principles 
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

import java.util.ArrayList;
import java.util.List;

import cop5556fa20.AST.*;
import cop5556fa20.Scanner.Kind;
import cop5556fa20.Scanner.LexicalException;
import cop5556fa20.Scanner.Token;

import javax.swing.plaf.nimbus.State;

import static cop5556fa20.Scanner.Kind.*;

public class Parser {

	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		final Token token;  //the token that caused an error to be discovered.

		public SyntaxException(Token token, String message) {
			super(message);
			this.token = token;
		}

		public Token token() {
			return token;
		}

	}


	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken(); // establish invariant that t is always the next token to be processed
	}

	public Program parse() throws SyntaxException, LexicalException {
		Program p = program();
		matchEOF();
		return p;
	}

	private DecImage imageDeclaration () throws SyntaxException, LexicalException {
		Token first = t;
		Kind op = NOP;
		Expression width = Expression.empty;
		Expression height = Expression.empty;
		Expression source = Expression.empty;

		match(Kind.KW_image);

		if (t.kind() == Kind.LSQUARE) {
			consume();
			width = expression();
			match(Kind.COMMA);
			height = expression();
			match(Kind.RSQUARE);
		}

		Token ident = t;
		match(Kind.IDENT);

		if (t.kind() == Kind.LARROW || t.kind() == Kind.ASSIGN) {
			op = t.kind();
			consume();
			source = expression();
		}

		return new DecImage(first, Type.Image, scanner.getText(ident), width, height, op, source);
	}

	private Type varType () throws SyntaxException {
		Token first = t;
		if (t.kind() == Kind.KW_int || t.kind() == Kind.KW_string) {
			consume();
			return first.kind() == KW_int ? Type.Int : Type.String;
		} else throw new SyntaxException(t, "unexpected var type " + t.kind() + " passed on line: " + t.line() + ", position: " + t.posInLine());
	}

	private DecVar variableDeclaration () throws SyntaxException, LexicalException {
		Token first = t;
		Expression e = Expression.empty;
		Type type = varType();
		Token ident = t;

		match(Kind.IDENT);

		if (t.kind() == Kind.ASSIGN) {
			consume();
			e = expression();
		}

		return new DecVar(first, type, scanner.getText(ident), e);
	}

	private Dec declaration () throws SyntaxException, LexicalException {
		if (t.kind() == Kind.KW_int || t.kind() == Kind.KW_string) return variableDeclaration();
		else if (t.kind() == Kind.KW_image) return imageDeclaration();
		else throw new SyntaxException(t, "Invalid declaration: " + t.kind() + " passed on line: " + t.line() + ", position: " + t.posInLine());
	}

	private Statement imageOutStatement (Token first) throws SyntaxException, LexicalException {
		if (t.kind() == Kind.KW_SCREEN) {
			Expression x = Expression.empty;
			Expression y = Expression.empty;
			consume();

			if (t.kind() == Kind.LSQUARE) {
				consume();
				x = expression();
				match(Kind.COMMA);
				y = expression();
				match(Kind.RSQUARE);
			}

			return new StatementOutScreen(first, scanner.getText(first), x, y);
		} else {
			Expression e = Expression.empty;
			e = expression();
			return new StatementOutFile(first, scanner.getText(first), e);
		}
	}

	private Statement loopStatement (Token first) throws SyntaxException, LexicalException {
		Expression e0 = Expression.empty;
		Expression e1 = Expression.empty;

		match(Kind.STAR);
		constXYSelector();
		match(Kind.COLON);

		if (t.kind() == Kind.PLUS || t.kind() == Kind.MINUS || t.kind() == Kind.EXCL || t.kind() == Kind.INTLIT
				|| t.kind() == Kind.STRINGLIT || t.kind() == Kind.KW_X || t.kind() == Kind.KW_Y || t.kind() == Kind.CONST
				|| t.kind() == Kind.LPIXEL || t.kind() == Kind.AT || t.kind() == Kind.LPAREN || t.kind() == Kind.IDENT) {
			e0 = expression();
		}

		match(Kind.COLON);
		e1 = expression();

		return new StatementLoop(first, scanner.getText(first), e0, e1);
	}

	private Statement statement () throws SyntaxException, LexicalException {
		Token first = t;
		Expression e = Expression.empty;
		match(Kind.IDENT);

		if (t.kind() == Kind.RARROW) {
			consume();
			return imageOutStatement(first);
		} else if (t.kind() == Kind.LARROW) {
			consume();
			e = expression();
			return new StatementImageIn(first, scanner.getText(first), e);
		} else if (t.kind() == Kind.ASSIGN) {
			consume();
			if (t.kind() == Kind.STAR) {
				return loopStatement(first);
			} else {
				e = expression();
				return new StatementAssign(first, scanner.getText(first), e);
			}
		} else throw new SyntaxException(t, "Invalid statement passed to program: " + t.kind() + " passed on line: " + t.line() + ", position: " + t.posInLine());
	}

	private static final Kind[] firstProgram = {KW_int, KW_string, KW_image, IDENT};

	private Program program () throws SyntaxException, LexicalException {
		Token first = t;
		List<ASTNode> decsAndStatements = new ArrayList<ASTNode>();

		while (isKind(firstProgram)) {
			switch (t.kind()) {
				case KW_int, KW_string, KW_image -> {
					Dec dec = declaration();
					decsAndStatements.add(dec);
					match(SEMI);
				}
				case IDENT -> {
					Statement st = statement();
					decsAndStatements.add(st);
					match(SEMI);
				}
				default -> throw new SyntaxException(t, "Invalid statement passed to program: " + t.kind() + " passed on line: " + t.line() + ", position: " + t.posInLine());
			}
		}

		return new Program(first, decsAndStatements);  //return a Program object
	}

	private void constXYSelector () throws SyntaxException {
		match(Kind.LSQUARE);
		match(Kind.KW_X);
		match(Kind.COMMA);
		match(Kind.KW_Y);
		match(Kind.RSQUARE);
	}

	private Expression argExpression () throws SyntaxException, LexicalException {
		Token first = t;
		Expression e = Expression.empty;

		match(Kind.AT);
		e = primary();
		return new ExprArg(first, e);
	}

	private Expression pixelConstructor () throws SyntaxException, LexicalException {
		Token first = t;
		Expression er = Expression.empty;
		Expression eg = Expression.empty;
		Expression eb = Expression.empty;

		match(Kind.LPIXEL);
		er = expression();
		match(Kind.COMMA);
		eg = expression();
		match(Kind.COMMA);
		eb = expression();
		match(Kind.RPIXEL);

		return new ExprPixelConstructor(first, er, eg, eb);
	}

	private String attribute () throws SyntaxException {
		if (t.kind() == Kind.KW_BLUE || t.kind() == Kind.KW_GREEN || t.kind() == Kind.KW_WIDTH || t.kind() == Kind.KW_HEIGHT || t.kind() == Kind.KW_RED) {
			Token token = t;
			consume();
			return scanner.getTokenText(token);
		}
		else throw new SyntaxException(t, "unexpected token " + t.kind() + " passed on line: " + t.line() + ", position: " + t.posInLine());
	}

	private Expression primary () throws SyntaxException, LexicalException {
		Token first = t;
		Expression e0 = Expression.empty;

		if (t.kind() == Kind.INTLIT) {
			consume();
			e0 = new ExprIntLit(first, scanner.intVal(first));
		} else if (t.kind() == Kind.STRINGLIT) {
			consume();
			e0 = new ExprStringLit(first, scanner.getText(first));
		} else if (t.kind() == Kind.IDENT || t.kind() == Kind.KW_X || t.kind() == Kind.KW_Y) {
			consume();
			e0 = new ExprVar(first, scanner.getTokenText(first));
		} else if (t.kind() == Kind.CONST) {
			consume();
			e0 = new ExprConst(first, scanner.getTokenText(first), scanner.intVal(first));
		} else if (t.kind() == Kind.LPAREN) {
			consume();
			e0 = expression();
			match(Kind.RPAREN);
		} else if (t.kind() == Kind.LPIXEL) {
			e0 = pixelConstructor();
		} else if (t.kind() == Kind.AT) {
			e0 = argExpression();
		} else {
			throw new SyntaxException(t, "unexpected token " + t.kind() + " passed on line: " + t.line() + ", position: " + t.posInLine());
		}

		// PixelSelector
		if (t.kind() == Kind.LSQUARE) {
			first = t;
			Expression e1 = Expression.empty;
			Expression e2 = Expression.empty;

			match(Kind.LSQUARE);
			e1 = expression();
			match(Kind.COMMA);
			e2 = expression();
			match(Kind.RSQUARE);

			e0 = new ExprPixelSelector(first, e0, e1, e2);
		}

		return e0;
	}

	private Expression hashExpression () throws SyntaxException, LexicalException {
		Token first = t;
		Expression e0 = Expression.empty;

		e0 = primary();

		while (t.kind() == Kind.HASH) {
			consume();
			String attr = attribute();
			e0 = new ExprHash(first, e0, attr);
		}

		return e0;
	}

	private Expression unaryExpressionNotPlusMinus () throws SyntaxException, LexicalException {
		Token first = t;

		if (t.kind() == Kind.EXCL)  {
			Expression e0 = Expression.empty;

			consume();
			e0 = unaryExpression();
			return new ExprUnary(first, first.kind(), e0);
		} else return hashExpression();
	}

	private Expression unaryExpression () throws SyntaxException, LexicalException {
		Token first = t;

		if (t.kind() == Kind.PLUS || t.kind() == Kind.MINUS)  {
			Expression e0 = Expression.empty;

			consume();
			e0 = unaryExpression();
			return new ExprUnary(first, first.kind(), e0);
		} else return unaryExpressionNotPlusMinus();
	}

	private Expression multExpression () throws SyntaxException, LexicalException {
		Token first = t;
		Expression e0 = Expression.empty;

		e0 = unaryExpression();

		while (t.kind() == Kind.STAR || t.kind() == Kind.DIV || t.kind() == Kind.MOD) {
			Token op = t;
			Expression e1 = Expression.empty;

			consume();
			e1 = unaryExpression();
			e0 = new ExprBinary(first, e0, op.kind(), e1);
		}

		return e0;
	}

	private Expression addExpression () throws SyntaxException, LexicalException {
		Token first = t;
		Expression e0 = Expression.empty;

		e0 = multExpression();

		while (t.kind() == Kind.PLUS || t.kind() == Kind.MINUS)  {
			Token op = t;
			Expression e1 = Expression.empty;

			consume();
			e1 = multExpression();
			e0 = new ExprBinary(first, e0, op.kind(), e1);
		}

		return e0;
	}

	private Expression relExpression () throws SyntaxException, LexicalException {
		Token first = t;
		Expression e0 = Expression.empty;

		e0 = addExpression();

		while (t.kind() == Kind.LT || t.kind() == Kind.GT || t.kind() == Kind.LE || t.kind() == Kind.GE) {
			Token op = t;
			Expression e1 = Expression.empty;

			consume();
			e1 = addExpression();
			e0 = new ExprBinary(first, e0, op.kind(), e1);
		}

		return e0;
	}

	private Expression eqExpression () throws SyntaxException, LexicalException {
		Token first = t;
		Expression e0 = Expression.empty;

		e0 = relExpression();

		while (t.kind() == Kind.EQ || t.kind() == Kind.NEQ) {
			Token op = t;
			Expression e1 = Expression.empty;

			consume();
			e1 = relExpression();
			e0 = new ExprBinary(first, e0, op.kind(), e1);
		}

		return e0;
	}

	private Expression andExpression () throws SyntaxException, LexicalException {
		Token first = t;
		Expression e0 = Expression.empty;

		e0 = eqExpression();

		while (t.kind() == Kind.AND) {
			Token op = t;
			Expression e1 = Expression.empty;

			consume();
			e1 = eqExpression();
			e0 = new ExprBinary(first, e0, op.kind(), e1);
		}

		return e0;
	}

	private Expression orExpression () throws SyntaxException, LexicalException {
		Token first = t;
		Expression e0 = Expression.empty;

		e0 = andExpression();

		while (t.kind() == Kind.OR) {
			Token op = t;
			Expression e1 = Expression.empty;

			consume();
			e1 = andExpression();
			e0 = new ExprBinary(first, e0, op.kind(), e1);
		}

		return e0;
	}

	protected Expression expression() throws SyntaxException, LexicalException {
		Token first = t;
		Expression e0 = Expression.empty;

		e0 = orExpression();

		if (t.kind() == Kind.Q) {
			Expression e1 = Expression.empty;
			Expression e2 = Expression.empty;

			consume();
			e1 = expression();
			match(Kind.COLON);
			e2 = expression();

			e0 = new ExprConditional(first, e0, e1, e2);
		}

		return e0;
	}


	protected boolean isKind (Kind kind) {
		return t.kind() == kind;
	}

	protected boolean isKind (Kind... kinds) {
		for (Kind k : kinds) {
			if (k == t.kind())
				return true;
		}
		return false;
	}


	/**
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match (Kind kind) throws SyntaxException {
		Token tmp = t;
		if (isKind(kind)) {
			consume();
			return tmp;
		}
		error(t, kind.toString());
		return null; // unreachable
	}

	/**
	 * Precondition: kind != EOF
	 * 
	 * @param kinds
	 * @return
	 * @throws SyntaxException
	 */
	private Token match (Kind... kinds) throws SyntaxException {
		Token tmp = t;
		if (isKind(kinds)) {
			consume();
			return tmp;
		}
		error(t, "expected one of " + kinds);
		return null; // unreachable
	}

	private Token consume () throws SyntaxException {
		Token tmp = t;
		if (isKind(EOF)) {
			error(t, "attempting to consume EOF");
		}
		t = scanner.nextToken();
		return tmp;
	}

	private void error (Token t, String m) throws SyntaxException {
		String message = m + " at " + t.line() + ":" + t.posInLine();
		throw new SyntaxException(t, message);
	}
	
	/**
	 * Only for check at end of program. Does not "consume" EOF so there is no
	 * attempt to get the nonexistent next Token.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF () throws SyntaxException {
		if (isKind(EOF)) {
			return t;
		}
		error(t, EOF.toString());
		return null; // unreachable
	}
}
