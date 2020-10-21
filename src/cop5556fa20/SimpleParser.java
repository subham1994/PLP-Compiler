/**
 * Class for  for the class project in COP5556 Programming Language Principles 
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

import cop5556fa20.Scanner.LexicalException;
import cop5556fa20.Scanner.Token;
import cop5556fa20.Scanner.Kind;

public class SimpleParser {

	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		final Token token;

		public SyntaxException(Token token, String message) {
			super(message);
			this.token = token;
		}

		public Token token() {
			return token;
		}

	}


	final Scanner scanner;
	Token t;


	SimpleParser(Scanner scanner) {
		this.scanner = scanner;
		this.t = scanner.nextToken();
	}
	
	Token consume () {
		Token token = t;
		t = scanner.nextToken();
		return token;
	}
	
	Token match (Kind kind) throws SyntaxException {
		if (kind == t.kind()) return consume();
		else throw new SyntaxException(t, "token mismatch on line : " + t.line() + ", position: " + t.posInLine() + "; Expected " + kind + ", got " + t.kind());
	}

	public void parse() throws SyntaxException, LexicalException {
		program();

		// If consumedAll returns false, then there is at least one
		// token left (the EOF token) so the call to nextToken is safe.
		if (!consumedAll()) throw new SyntaxException(scanner.nextToken(), "tokens remain after parsing");
	}
	

	public boolean consumedAll() {
		if (scanner.hasTokens()) { 
			Token t = scanner.nextToken();
			return t.kind() == Kind.EOF;
		}
		return true;
	}

	private void declaration () throws SyntaxException, LexicalException {
		if (t.kind() == Kind.KW_int || t.kind() == Kind.KW_string) variableDeclaration();
		else if (t.kind() == Kind.KW_image) imageDeclaration();
		else throw new SyntaxException(t, "Invalid declaration: " + t.kind() + " passed on line: " + t.line() + ", position: " + t.posInLine());
	}
	
	private void variableDeclaration () throws SyntaxException, LexicalException {
		varType();
		match(Kind.IDENT);
		
		if (t.kind() == Kind.ASSIGN) {
			consume();
			expression();
		}
	}
	
	private void varType () throws SyntaxException, LexicalException {
		if (t.kind() == Kind.KW_int || t.kind() == Kind.KW_string) {
			consume();
		} else throw new SyntaxException(t, "unexpected var type " + t.kind() + " passed on line: " + t.line() + ", position: " + t.posInLine());
	}
	
	private void imageDeclaration () throws SyntaxException, LexicalException {
		match(Kind.KW_image);
		
		if (t.kind() == Kind.LSQUARE) {
			consume();
			expression();
			match(Kind.COMMA);
			expression();
			match(Kind.RSQUARE);
		}
		
		match(Kind.IDENT);
		
		if (t.kind() == Kind.LARROW || t.kind() == Kind.ASSIGN) {
			consume();
			expression();
		}
	}
	
	private void imageOutStatement () throws SyntaxException, LexicalException {
		if (t.kind() == Kind.KW_SCREEN) {
			consume();
			
			if (t.kind() == Kind.LSQUARE) {
				consume();
				expression();
				match(Kind.COMMA);
				expression();
				match(Kind.RSQUARE);
			}
		} else expression();
	}
	
	private void loopStatement () throws SyntaxException, LexicalException {
		match(Kind.STAR);
		constXYSelector();
		match(Kind.COLON);
		
		if (t.kind() == Kind.PLUS || t.kind() == Kind.MINUS || t.kind() == Kind.EXCL || t.kind() == Kind.INTLIT
				|| t.kind() == Kind.STRINGLIT || t.kind() == Kind.KW_X || t.kind() == Kind.KW_Y || t.kind() == Kind.CONST
				|| t.kind() == Kind.LPIXEL || t.kind() == Kind.AT || t.kind() == Kind.LPAREN || t.kind() == Kind.IDENT) {
			expression();
		}
		
		match(Kind.COLON);
		expression();
	}
	
	private void statement () throws SyntaxException, LexicalException {
		match(Kind.IDENT);
		
		if (t.kind() == Kind.RARROW) {
			consume();
			imageOutStatement();
		} else if (t.kind() == Kind.LARROW) {
			consume();
			expression();
		} else if (t.kind() == Kind.ASSIGN) {
			consume();
			if (t.kind() == Kind.STAR) {
				loopStatement();
			} else expression();
		} else throw new SyntaxException(t, "Invalid statement passed to program: " + t.kind() + " passed on line: " + t.line() + ", position: " + t.posInLine());
	}
	
	private void program () throws SyntaxException, LexicalException {
		while (scanner.hasTokens()) {
			if (t.kind() == Kind.KW_int || t.kind() == Kind.KW_string || t.kind() == Kind.KW_image) {
				declaration();
				match(Kind.SEMI);
			} else if (t.kind() == Kind.IDENT) {
				statement();
				match(Kind.SEMI);
			} else throw new SyntaxException(t, "Invalid input passed to program: " + t.kind() + " passed on line: " + t.line() + ", position: " + t.posInLine());
		}
	}
	
	public void constXYSelector () throws SyntaxException, LexicalException {
		match(Kind.LSQUARE);
		match(Kind.KW_X);
		match(Kind.COMMA);
		match(Kind.KW_Y);
		match(Kind.RSQUARE);
	}
	
	public void argExpression () throws SyntaxException, LexicalException {
		match(Kind.AT);
		primary();
	}

	public void pixelSelector () throws SyntaxException, LexicalException {
		match(Kind.LSQUARE);
		expression();
		match(Kind.COMMA);
		expression();
		match(Kind.RSQUARE);
	}
	
	public void pixelConstructor () throws SyntaxException, LexicalException {
		match(Kind.LPIXEL);
		expression();
		match(Kind.COMMA);
		expression();
		match(Kind.COMMA);
		expression();
		match(Kind.RPIXEL);
	}
	
	public void attribute () throws SyntaxException, LexicalException {
		if (t.kind() == Kind.KW_BLUE || t.kind() == Kind.KW_GREEN || t.kind() == Kind.KW_WIDTH || t.kind() == Kind.KW_HEIGHT || t.kind() == Kind.KW_RED) consume();
		else throw new SyntaxException(t, "unexpected token " + t.kind() + " passed on line: " + t.line() + ", position: " + t.posInLine());
	}
	
	public void primary () throws SyntaxException, LexicalException {
		if (t.kind() == Kind.INTLIT || t.kind() == Kind.IDENT || t.kind() == Kind.STRINGLIT
				|| t.kind() == Kind.KW_X || t.kind() == Kind.KW_Y || t.kind() == Kind.CONST) {
			consume();
		} else if (t.kind() == Kind.LPAREN) {
			consume();
			expression();
			match(Kind.RPAREN);
		} else if (t.kind() == Kind.LPIXEL) {
			pixelConstructor();
		} else if (t.kind() == Kind.AT) {
			argExpression();
		} else {
			throw new SyntaxException(t, "unexpected token " + t.kind() + " passed on line: " + t.line() + ", position: " + t.posInLine());
		}
		
		if (t.kind() == Kind.LSQUARE) {
			pixelSelector();
		}	
	}
	
	public void hashExpression () throws SyntaxException, LexicalException {
		primary();

		while (t.kind() == Kind.HASH) {
			consume();
			attribute();
		}
	}
	
	public void unaryExpressionNotPlusMinus () throws SyntaxException, LexicalException {
		if (t.kind() == Kind.EXCL)  {
			consume();
			unaryExpression();
		} else hashExpression();
	}
	
	public void unaryExpression () throws SyntaxException, LexicalException {
		if (t.kind() == Kind.PLUS || t.kind() == Kind.MINUS)  {
			consume();
			unaryExpression();
		} else unaryExpressionNotPlusMinus();
	}
	
	public void multExpression () throws SyntaxException, LexicalException {
		unaryExpression();
		
		while (t.kind() == Kind.STAR || t.kind() == Kind.DIV || t.kind() == Kind.MOD) {
			consume();
			unaryExpression();
		}
	}
	
	public void addExpression () throws SyntaxException, LexicalException {
		multExpression();
		
		while (t.kind() == Kind.PLUS || t.kind() == Kind.MINUS)  {
			consume();
			multExpression();
		}
	}
	
	public void relExpression () throws SyntaxException, LexicalException {
		addExpression();
		
		while (t.kind() == Kind.LT || t.kind() == Kind.GT || t.kind() == Kind.LE || t.kind() == Kind.GE) {
			consume();
			addExpression();
		}
	}
	
	public void eqExpression () throws SyntaxException, LexicalException {
		relExpression();
		
		while (t.kind() == Kind.EQ || t.kind() == Kind.NEQ) {
			consume();
			relExpression();
		}
	}
	
	public void andExpression () throws SyntaxException, LexicalException {
		eqExpression();
		
		while (t.kind() == Kind.AND) {
			consume();
			eqExpression();
		}
	}
	
	public void orExpression () throws SyntaxException, LexicalException {
		andExpression();
		
		while (t.kind() == Kind.OR) {
			consume();
			andExpression();
		}
	}

	public void expression() throws SyntaxException, LexicalException {
		orExpression();
		
		if (t.kind() == Kind.Q) {
			consume();
			expression();
			match(Kind.COLON);
			expression();
		}
	}
}
