/**
 * Scanner for the class project in COP5556 Programming Language Principles 
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class Scanner {
	
	@SuppressWarnings("preview")
	public record Token(
		Kind kind,
		int pos, //position in char array.  Starts at zero
		int length, //number of chars in token
		int line, //line number of token in source.  Starts at 1
		int posInLine //position in line of source.  Starts at 1
		) {
	}
	
	@SuppressWarnings("serial")
	public static class LexicalException extends Exception {
		int pos;
		public LexicalException(String message, int pos) {
			super(message);
			this.pos = pos;
		}
		public int pos() { return pos; }
	}
	
	private static enum states {
		START, EQUALS, IDENT, DIGITS, LINE_TERM, GREATER, LESS, NOT, DASH, STRING_LIT, R_SLASH, COMMENT
	}
	
	public static enum Kind {
		IDENT, INTLIT, STRINGLIT, CONST, COMMENT,
		KW_X/* X */,  KW_Y/* Y */, KW_WIDTH/* width */,KW_HEIGHT/* height */, 
		KW_SCREEN/* screen */, KW_SCREEN_WIDTH /* screen_width */, KW_SCREEN_HEIGHT /*screen_height */,
		KW_image/* image */, KW_int/* int */, KW_string /* string */,
		KW_RED /* red */,  KW_GREEN /* green */, KW_BLUE /* blue */,
		ASSIGN/* = */, GT/* > */, LT/* < */, 
		EXCL/* ! */, Q/* ? */, COLON/* : */, EQ/* == */, NEQ/* != */, GE/* >= */, LE/* <= */, 
		AND/* & */, OR/* | */, PLUS/* + */, MINUS/* - */, STAR/* * */, DIV/* / */, MOD/* % */, 
	    AT/* @ */, HASH /* # */, RARROW/* -> */, LARROW/* <- */, LPAREN/* ( */, RPAREN/* ) */, 
		LSQUARE/* [ */, RSQUARE/* ] */, LPIXEL /* << */, RPIXEL /* >> */,  SEMI/* ; */, COMMA/* , */,  EOF
	}
	

	/**
	 * Returns the text of the token.  If the token represents a String literal, then
	 * the returned text omits the delimiting double quotes and replaces escape sequences with
	 * the represented character.
	 * 
	 * @param token
	 * @return
	 */
	public String getText(Token token) {
		if (token.kind == Kind.IDENT) {
			return String.valueOf(Arrays.copyOfRange(chars, token.pos, token.pos + token.length));
		}
		
		return "";
	}
	
	
	/**
	 * Returns true if the internal interator has more Tokens
	 * 
	 * @return
	 */
	public boolean hasTokens() {
		return nextTokenPos < tokens.size();
	}
	
	/**
	 * Returns the next Token and updates the internal iterator so that
	 * the next call to nextToken will return the next token in the list.
	 * 
	 * Precondition:  hasTokens()
	 * @return
	 */
	public Token nextToken() {
		return tokens.get(nextTokenPos++);
	}
	

	/**
	 * The list of tokens created by the scan method.
	 */
	private final ArrayList<Token> tokens = new ArrayList<Token>();
	

	/**
	 * position of the next token to be returned by a call to nextToken
	 */
	private int nextTokenPos = 0;
//	static final char EOFChar = 0;
	private final char chars[];

	Scanner(String inputString) {
		int len = inputString.length();
		chars = Arrays.copyOf(inputString.toCharArray(), len);
	}
	
	private void addIntLitToken (String s, int pos, int line, int posInLine) throws LexicalException {
		try {
			Integer.parseInt(s);
		} catch (NumberFormatException e) {
			throw new LexicalException("Integer literal value out of range; line " + line + " pos " + (posInLine - s.length() + 1), pos - s.length() + 1);
		}
		tokens.add(new Token(Kind.INTLIT, pos - s.length() + 1, s.length(), line, posInLine - s.length() + 1));
	}
	
	private void addIdentConstKWTokenConditionally (String s, int pos, int line, int posInLine) {
		if (constants.containsKey(s)) tokens.add(new Token(Kind.CONST, pos - s.length() + 1, s.length(), line, posInLine - s.length() + 1));
		else if (keywords.containsKey(s)) tokens.add(new Token(keywords.get(s), pos - s.length() + 1, s.length(), line, posInLine - s.length() + 1));
		else tokens.add(new Token(Kind.IDENT, pos - s.length() + 1, s.length(), line, posInLine - s.length() + 1));
	}

	
	public Scanner scan() throws LexicalException {
		int pos = 0;
		int line = 1;
		int posInLine = 1;
		String temp = "";
		states state = states.START;
		
		while (pos < chars.length) {
			char ch = chars[pos];
			
			switch (state) {
				case START -> {
					switch (ch) {
						case '+' -> {
							tokens.add(new Token(Kind.PLUS, pos, 1, line, posInLine));
							pos++;
							posInLine++;
							break;
						}
						case '(' -> {
							tokens.add(new Token(Kind.LPAREN, pos, 1, line, posInLine));
							pos++;
							posInLine++;
							break;
						}
						case ')' -> {
							tokens.add(new Token(Kind.RPAREN, pos, 1, line, posInLine));
							pos++;
							posInLine++;
							break;
						}
						case '[' -> {
							tokens.add(new Token(Kind.LSQUARE, pos, 1, line, posInLine));
							pos++;
							posInLine++;
							break;
						}
						case ']' -> {
							tokens.add(new Token(Kind.RSQUARE, pos, 1, line, posInLine));
							pos++;
							posInLine++;
							break;
						}
						case ';' -> {
							tokens.add(new Token(Kind.SEMI, pos, 1, line, posInLine));
							pos++;
							posInLine++;
							break;
						}
						case ':' -> {
							tokens.add(new Token(Kind.COLON, pos, 1, line, posInLine));
							pos++;
							posInLine++;
							break;
						}
						case ',' -> {
							tokens.add(new Token(Kind.COMMA, pos, 1, line, posInLine));
							pos++;
							posInLine++;
							break;
						}
						case '?' -> {
							tokens.add(new Token(Kind.Q, pos, 1, line, posInLine));
							pos++;
							posInLine++;
							break;
						}
						case '*' -> {
							tokens.add(new Token(Kind.STAR, pos, 1, line, posInLine));
							pos++;
							posInLine++;
							break;
						}
						case '%' -> {
							tokens.add(new Token(Kind.MOD, pos, 1, line, posInLine));
							pos++;
							posInLine++;
							break;
						}
						case '@' -> {
							tokens.add(new Token(Kind.AT, pos, 1, line, posInLine));
							pos++;
							posInLine++;
							break;
						}
						case '#' -> {
							tokens.add(new Token(Kind.HASH, pos, 1, line, posInLine));
							pos++;
							posInLine++;
							break;
						}
						case '&' -> {
							tokens.add(new Token(Kind.AND, pos, 1, line, posInLine));
							pos++;
							posInLine++;
							break;
						}
						case '|' -> {
							tokens.add(new Token(Kind.OR, pos, 1, line, posInLine));
							pos++;
							posInLine++;
							break;
						}
						case '0' -> {
							tokens.add(new Token(Kind.INTLIT, pos, 1, line, posInLine));
							pos++;
							posInLine++;
							break;
						}
						case '-' -> {
							if (pos + 1 == chars.length) tokens.add(new Token(Kind.MINUS, pos, 1, line, posInLine));
							pos++;
							posInLine++;
							state = states.DASH;
							break;
						}
						case '>' -> {
							if (pos + 1 == chars.length) tokens.add(new Token(Kind.GT, pos, 1, line, posInLine));
							pos++;
							posInLine++;
							state = states.GREATER;
							break;
						}
						case '<' -> {
							if (pos + 1 == chars.length) tokens.add(new Token(Kind.LT, pos, 1, line, posInLine));
							pos++;
							posInLine++;
							state = states.LESS;
							break;
						}
						case '!' -> {
							if (pos + 1 == chars.length) tokens.add(new Token(Kind.EXCL, pos, 1, line, posInLine));
							pos++;
							posInLine++;
							state = states.NOT;
							break;
						}
						case '=' -> {
							if (pos + 1 == chars.length) tokens.add(new Token(Kind.ASSIGN, pos, 1, line, posInLine));
							pos++;
							posInLine++;
							state = states.EQUALS;
							break;
						}
						case '\t', ' ', '\f' -> {
							pos++;
							posInLine++;
							break;
						}
						case '\n' -> {
							pos++;
							posInLine = 1;
							line++;
							break;
						}
						case '\r' -> {
							pos++;
							posInLine = 1;
							line++;
							state = states.LINE_TERM;
							break;
						}
						case '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
							temp += ch;
							if (pos + 1 == chars.length) addIntLitToken(temp, pos, line, posInLine);
							pos++;
							posInLine++;
							state = states.DIGITS;
							break;
						}
						case '/' -> {
							temp += ch;
							if (pos + 1 == chars.length) tokens.add(new Token(Kind.DIV, pos, 1, line, posInLine));
							pos++;
							posInLine++;
							state = states.R_SLASH;
							break;
							
						}
						case '\"' -> {
							break;
						}
						default -> {
							if (Character.isJavaIdentifierStart(ch)) {
								temp += ch;
								if (pos + 1 == chars.length) addIdentConstKWTokenConditionally(temp, pos, line, posInLine);
								pos++;
								posInLine++;
								state = states.IDENT;
								break;
							} else {
								throw new LexicalException("Unknown character '" + ch + "' found at " + line + " pos " + posInLine, pos);
							}
						}
					}
				}
				case DASH -> {
					switch (ch) {
						case '>' -> {
							tokens.add(new Token(Kind.RARROW, pos - 1, 2, line, posInLine - 1));
							pos++;
							posInLine++;
							state = states.START;
							break;
						}
						default -> {
							tokens.add(new Token(Kind.MINUS, pos - 1, 1, line, posInLine - 1));
							state = states.START;
							break;
						}
					}
				}
				case EQUALS -> {
					switch (ch) {
						case '=' -> {
							tokens.add(new Token(Kind.EQ, pos - 1, 2, line, posInLine - 1));
							pos++;
							posInLine++;
							break;
						}
						default -> {
							tokens.add(new Token(Kind.ASSIGN, pos - 1, 1, line, posInLine - 1));
							state = states.START;
							break;
						}
					}
				}
				case GREATER -> {
					switch (ch) {
						case '=' -> {
							tokens.add(new Token(Kind.GE, pos - 1, 2, line, posInLine - 1));
							pos++;
							posInLine++;
							state = states.START;
							break;
						}
						case '>' -> {
							tokens.add(new Token(Kind.RPIXEL, pos - 1, 2, line, posInLine - 1));
							pos++;
							posInLine++;
							state = states.START;
							break;
						}
						default -> {
							tokens.add(new Token(Kind.GT, pos - 1, 1, line, posInLine - 1));
							state = states.START;
							break;
						}
					}
				}
				case LESS -> {
					switch (ch) {
						case '=' -> {
							tokens.add(new Token(Kind.LE, pos - 1, 2, line, posInLine - 1));
							pos++;
							posInLine++;
							state = states.START;
							break;
						}
						case '<' -> {
							tokens.add(new Token(Kind.LPIXEL, pos - 1, 2, line, posInLine - 1));
							pos++;
							posInLine++;
							state = states.START;
							break;
						}
						case '-' -> {
							tokens.add(new Token(Kind.LARROW, pos - 1, 2, line, posInLine - 1));
							pos++;
							posInLine++;
							state = states.START;
							break;
						}
						default -> {
							tokens.add(new Token(Kind.LT, pos - 1, 1, line, posInLine - 1));
							state = states.START;
							break;
						}
					}
				}
				case NOT -> {
					switch (ch) {
						case '=' -> {
							tokens.add(new Token(Kind.NEQ, pos - 1, 2, line, posInLine - 1));
							pos++;
							posInLine++;
							state = states.START;
							break;
						}
						default -> {
							tokens.add(new Token(Kind.EXCL, pos - 1, 1, line, posInLine - 1));
							state = states.START;
							break;
						}
					}
				}
				case LINE_TERM -> {
					if (ch == '\n') pos++;
					state = states.START;
					break;
				}
				case STRING_LIT -> {
					
				}
				case IDENT -> {
					if (Character.isJavaIdentifierStart(ch) || "0123456789".indexOf(ch) > -1) {
						temp += ch;
						
						if (pos + 1 == chars.length) addIdentConstKWTokenConditionally(temp, pos, line, posInLine);
						
						pos++;
						posInLine++;
					} else {
						addIdentConstKWTokenConditionally(temp, pos - 1, line, posInLine - 1);
						temp = "";
						state = states.START;
					}
					
					break;
				}
				case DIGITS -> {
					switch (ch) {
						case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
							temp += ch;
							
							if (pos + 1 == chars.length) addIntLitToken(temp, pos, line, posInLine);
							
							pos++;
							posInLine++;
							break;
						}
						default -> {
							addIntLitToken(temp, pos - 1, line, posInLine - 1);
							temp = "";
							state = states.START;
							break;
						}
					}
				}
				case R_SLASH -> {
					switch (ch) {
						case '/' -> {
							temp += ch;
							if (pos + 1 == chars.length) tokens.add(new Token(Kind.COMMENT, pos - 1, 2, line, posInLine - 1));
							pos++;
							posInLine++;
							state = states.COMMENT;
							break;
						}
						default -> {
							tokens.add(new Token(Kind.DIV, pos - 1, 1, line, posInLine - 1));
							temp = "";
							state = states.START;
							break;
						}
					}
				}
				case COMMENT -> {
					if (ch == '\n' || ch == '\r' || pos + 1 == chars.length) {
						tokens.add(new Token(Kind.COMMENT, pos - temp.length(), temp.length(), line, posInLine - temp.length()));
						temp = "";
						state = states.START;
					} else {
						temp += ch;
						pos++;
						posInLine++;
					}
					break;
				}
			}
		}
		
		tokens.add(new Token(Kind.EOF, pos, 0, line, posInLine));
		return this;
	}
	

	/**
	 * precondition:  This Token is an INTLIT or CONST
	 * @throws LexicalException 
	 * 
	 * @returns the integer value represented by the token
	 */
	public int intVal(Token t) throws LexicalException {
		if (t.kind == Kind.INTLIT) return Integer.valueOf(String.valueOf(Arrays.copyOfRange(chars, t.pos, t.pos + t.length)));
		else if (t.kind == Kind.CONST) return constants.get(String.valueOf(Arrays.copyOfRange(chars, t.pos, t.pos + t.length)));
		else throw new LexicalException("Tokens of type " + t.kind + " can not be converted to integer type", t.pos);
	}
	
	/**
	 * Hashmap containing the values of the reserved words.
	 * 
	 */
	private static HashMap<String, Kind> keywords;
	static {
		keywords = new HashMap<String, Kind>();	
		keywords.put("X", Scanner.Kind.KW_X);
		keywords.put("Y", Scanner.Kind.KW_Y);
		keywords.put("width", Scanner.Kind.KW_WIDTH);
		keywords.put("height", Scanner.Kind.KW_HEIGHT);
		keywords.put("screen", Scanner.Kind.KW_SCREEN);
		keywords.put("screen_width", Scanner.Kind.KW_SCREEN_WIDTH);
		keywords.put("screen_height", Scanner.Kind.KW_SCREEN_HEIGHT);
		keywords.put("image", Scanner.Kind.KW_image);
		keywords.put("int", Scanner.Kind.KW_int);
		keywords.put("string", Scanner.Kind.KW_string);
		keywords.put("red", Scanner.Kind.KW_RED);
		keywords.put("green", Scanner.Kind.KW_GREEN);
		keywords.put("blue", Scanner.Kind.KW_BLUE);
	}
	
	/**
	 * Hashmap containing the values of the predefined colors.
	 * 
	 */
	private static HashMap<String, Integer> constants;
	static {
		constants = new HashMap<String, Integer>();	
		constants.put("Z", 255);
		constants.put("WHITE", 0xffffffff);
		constants.put("SILVER", 0xffc0c0c0);
		constants.put("GRAY", 0xff808080);
		constants.put("BLACK", 0xff000000);
		constants.put("RED", 0xffff0000);
		constants.put("MAROON", 0xff800000);
		constants.put("YELLOW", 0xffffff00);
		constants.put("OLIVE", 0xff808000);
		constants.put("LIME", 0xff00ff00);
		constants.put("GREEN", 0xff008000);
		constants.put("AQUA", 0xff00ffff);
		constants.put("TEAL", 0xff008080);
		constants.put("BLUE", 0xff0000ff);
		constants.put("NAVY", 0xff000080);
		constants.put("FUCHSIA", 0xffff00ff);
		constants.put("PURPLE", 0xff800080);
	}
	
	/**
	 * Returns a String representation of the list of Tokens.
	 * You may modify this as desired. 
	 */
	public String toString() {
		return tokens.toString();
	}
}
