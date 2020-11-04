package cop5556fa20;

import cop5556fa20.AST.*;
import cop5556fa20.Scanner.Kind;
import cop5556fa20.Scanner.Token;

import java.util.Arrays;
import java.util.HashMap;

public class TypeCheckVisitor implements ASTVisitor {
	private final HashMap<String, Dec> symbolTable;


	@SuppressWarnings("serial")
	class TypeException extends Exception {
		Token first;
		String message;
		
		public TypeException (Token first, String message) {
			super();
			this.first = first;
			this.message = "Semantic error: [Line " + first.line() + ", pos " + first.posInLine() + "] " + message;
		}
		
		public String toString() {
			return message;
		}	
	}
	
	
	public TypeCheckVisitor () {
		super();

		symbolTable = new HashMap<>();
		Token t = new Token(Kind.IDENT, 0, 1, 1, 1);
		symbolTable.put("X", new DecVar(t, Type.Int, "X", new ExprVar(t, "X")));
		symbolTable.put("Y", new DecVar(t, Type.Int, "Y", new ExprVar(t, "Y")));
	}

	private Dec getDecForEntry (String name, Token first) throws TypeException {
		if (!symbolTable.containsKey(name))
			throw new TypeException(first, "Undefined variable '" + name + "'");

		return symbolTable.get(name);
	}

	@Override
	public Object visitDecImage (DecImage decImage, Object arg) throws Exception {
		Type tEW = (Type) decImage.width().visit(this, Type.Int);
		Type tEH = (Type) decImage.height().visit(this, Type.Int);
		Type tES = (Type) decImage.source().visit(this, Type.String);

		if (symbolTable.containsKey(decImage.name()))
			throw new TypeException(decImage.first(), "variable '" + decImage.name() + "' already declared");
		if (tEH != Type.Int && tEH != Type.Void || tEH != tEW)
			throw new TypeException(decImage.first(), "Type mismatch for width/height in Image declaration");

		if (decImage.op() == Kind.LARROW && tES != Type.String)
			throw new TypeException(decImage.first(), "Type mismatch; Expected String, got " + tES.toString());
		else if (decImage.op() == Kind.ASSIGN && tES != Type.Image)
			throw new TypeException(decImage.first(), "Type mismatch; Expected Image, got " + tES.toString());
		else if (decImage.op() == Kind.NOP && tES != Type.Void)
			throw new TypeException(decImage.first(), "Operator Error: Expected NOP");

		symbolTable.put(decImage.name(), decImage);
		return decImage.type();
	}

	@Override
	public Object visitDecVar (DecVar decVar, Object arg) throws Exception {
		Type expected = decVar.type();
		Type got = (Type) decVar.expression().visit(this, expected);

		if (symbolTable.containsKey(decVar.name()))
			throw new TypeException(decVar.first(), "Variable '" + decVar.name() + "' already declared");
		else if (got != Type.Void && expected != got)
			throw new TypeException(decVar.first(), "Expected Type " + expected + ", got " + got + " instead");

		symbolTable.put(decVar.name(), decVar);
		return decVar.type();
	}

	@Override
	public Object visitExprArg (ExprArg exprArg, Object arg) throws Exception {
		Type tE = (Type) exprArg.e().visit(this, arg);
		Type tRet = (Type) arg;

		if (tE != Type.Int)
			throw new TypeException(exprArg.first(), "Incorrect type for arg: expected int, got " + tE.toString());
		else if (tRet != Type.Int && tRet != Type.String)
			throw new TypeException(exprArg.first(), "Incorrect expected cast type for arg: " + tRet.toString());

		exprArg.setType(tRet);
		return exprArg.type();
	}

	@Override
	public Object visitExprBinary (ExprBinary exprBinary, Object arg) throws Exception {
		Type tE0 = (Type) exprBinary.e0().visit(this, arg);
		Type tE1 = (Type) exprBinary.e1().visit(this, arg);
		Kind op = exprBinary.op();

		if ((op == Kind.AND || op == Kind.OR) && tE0 == Type.Boolean && tE0 == tE1)
			exprBinary.setType(Type.Boolean);
		else if ((op == Kind.EQ || op == Kind.NEQ) && tE0 == tE1)
			exprBinary.setType(Type.Boolean);
		else if (Arrays.asList(Kind.LT, Kind.GT, Kind.GE, Kind.LE).contains(op) && tE0 == Type.Int && tE0 == tE1)
			exprBinary.setType(Type.Boolean);
		else if (tE0 == tE1
				&& ((op == Kind.PLUS && (tE0 == Type.Int || tE1 == Type.String))
				||op == Kind.MINUS && tE0 == Type.Int))
			exprBinary.setType(tE0);
		else if (tE0 == tE1 && tE0 == Type.Int)
			exprBinary.setType(Type.Int);
		else
			throw new TypeException(exprBinary.first(), "Type mismatch in binary expression");

		return exprBinary.type();
	}

	@Override
	public Object visitExprConditional (ExprConditional exprConditional, Object arg) throws Exception {
		Type tC = (Type) exprConditional.condition().visit(this, arg);
		Type tT = (Type) exprConditional.trueCase().visit(this, arg);
		Type tF = (Type) exprConditional.falseCase().visit(this, arg);

		if (tC == Type.Boolean && tT == tF)
			exprConditional.setType(tT);
		else
			throw new TypeException(exprConditional.first(), "Type mismatch in conditional expression");

		return exprConditional.type();
	}

	@Override
	public Object visitExprConst (ExprConst exprConst, Object arg) throws Exception {
		exprConst.setType(Type.Int);
		return exprConst.type();
	}

	@Override
	public Object visitExprHash (ExprHash exprHash, Object arg) throws Exception {
		String attr = exprHash.attr();
		Type t = (Type) exprHash.e().visit(this, arg);

		if (t != Type.Int && t != Type.Image)
			throw new TypeException(exprHash.first(), "Type mismatch; expected Int | Image, got " + t.toString());

		if (t == Type.Int && !Arrays.asList("red" ,"green", "blue").contains(attr)) {
			throw new TypeException(exprHash.first(), "Incorrect attribute for epxression of type Int: " + attr);
		} else if (t == Type.Image && !Arrays.asList("width" ,"height").contains(attr)) {
			throw new TypeException(exprHash.first(), "Incorrect attribute for epxression of type Image: " + attr);
		}

		exprHash.setType(Type.Int);
		return exprHash.type();
	}

	@Override
	public Object visitExprIntLit (ExprIntLit exprIntLit, Object arg) throws Exception {
		exprIntLit.setType(Type.Int);
		return exprIntLit.type();
	}

	@Override
	public Object visitExprPixelConstructor (ExprPixelConstructor exprPixelConstructor, Object arg) throws Exception {
		Type typeR = (Type) exprPixelConstructor.redExpr().visit(this, arg);
		Type typeG = (Type) exprPixelConstructor.greenExpr().visit(this, arg);
		Type typeB = (Type) exprPixelConstructor.blueExpr().visit(this, arg);

		if (typeR != Type.Int || typeG != Type.Int || typeB != Type.Int)
			throw new TypeException(exprPixelConstructor.first(), "Type mismatch in pixel constructor");

		exprPixelConstructor.setType(Type.Int);
		return exprPixelConstructor.type();
	}

	@Override
	public Object visitExprPixelSelector (ExprPixelSelector exprPixelSelector, Object arg) throws Exception {
		Type typeEI = (Type) exprPixelSelector.image().visit(this, arg);
		Type typeEX = (Type) exprPixelSelector.X().visit(this, arg);
		Type typeEY = (Type) exprPixelSelector.Y().visit(this, arg);

		if (typeEX != Type.Int || typeEY != Type.Int || typeEI != Type.Image)
			throw new TypeException(exprPixelSelector.first(), "Type mismatch in pixel selector");

		exprPixelSelector.setType(Type.Int);
		return exprPixelSelector.type();
	}

	@Override
	public Object visitExprStringLit (ExprStringLit exprStringLit, Object arg) throws Exception {
		exprStringLit.setType(Type.String);
		return exprStringLit.type();
	}

	@Override
	public Object visitExprUnary (ExprUnary exprUnary, Object arg) throws Exception {
		Type t = (Type) exprUnary.e().visit(this, arg);

		if ((exprUnary.op() == Kind.PLUS || exprUnary.op() == Kind.MINUS) && t == Type.Int)
			exprUnary.setType(Type.Int);
		else  if (exprUnary.op() == Kind.EXCL && t == Type.Boolean)
			exprUnary.setType(Type.Boolean);
		else
			throw new TypeException(exprUnary.first(), "Incorrect Type/Operatior passed to unary expresison");

		return exprUnary.type();
	}

	@Override
	public Object visitExprVar (ExprVar exprVar, Object arg) throws Exception {
		if (!symbolTable.containsKey(exprVar.name()))
			throw new TypeException(exprVar.first(), "Undefined variable '" + exprVar.name() + "'");

		exprVar.setType(symbolTable.get(exprVar.name()).type());
		return exprVar.type();
	}


	/**
	 * First visit method that is called.  It simply visits its children and
	 * returns null if no type errors were encountered.
	 */
	@Override
	public Object visitProgram (Program program, Object arg) throws Exception {
		for (ASTNode node: program.decOrStatement()) {
			node.visit(this, arg);
		}
		return null;
	}
	

	@Override
	public Object visitStatementAssign (StatementAssign statementAssign, Object arg) throws Exception {
		Token f = statementAssign.first();
		Dec dec = getDecForEntry(statementAssign.name(), statementAssign.first());
		Type tExp = (Type) statementAssign.expression().visit(this, dec.type());

		if (dec.type() != tExp)
			throw new TypeException(f, "Type mismatch in StatementAssign");

		statementAssign.setDec(dec);
		return Type.Void;
	}

	@Override
	public Object visitStatementImageIn (StatementImageIn statementImageIn, Object arg) throws Exception {
		Token f = statementImageIn.first();
		Dec dec = getDecForEntry(statementImageIn.name(), f);
		Type tSource = (Type) statementImageIn.source().visit(this, Type.String);

		if (dec.type() != Type.Image || (tSource != Type.String && tSource != Type.Image))
			throw new TypeException(f, "Type mismatch in StatementImageIn");

		statementImageIn.setDec(dec);
		return Type.Void;
	}

	@Override
	public Object visitStatementLoop (StatementLoop statementLoop, Object arg) throws Exception {
		Token f = statementLoop.first();
		Dec dec = getDecForEntry(statementLoop.name(), statementLoop.first());
		Type tE = (Type) statementLoop.e().visit(this, Type.Int);
		Type tCond = (Type) statementLoop.cond().visit(this, Type.Boolean);

		if (dec.type() != Type.Image || (tCond != Type.Boolean && tCond != Type.Void) || tE != Type.Int)
			throw new TypeException(f, "Type mismatch in StatementLoop");

		statementLoop.setDec(dec);
		return Type.Void;
	}

	@Override
	public Object visitExprEmpty (ExprEmpty exprEmpty, Object arg) throws Exception {
		exprEmpty.setType(Type.Void);
		return exprEmpty.type();
	}

	@Override
	public Object visitStatementOutFile (StatementOutFile statementOutFile, Object arg) throws Exception {
		Token f = statementOutFile.first();
		Dec dec = getDecForEntry(statementOutFile.name(), f);
		Type tFile = (Type) statementOutFile.filename().visit(this, Type.String);

		if (dec.type() != Type.Image || tFile != Type.String)
			throw new TypeException(f, "Type mismatch in StatementOutFile");

		statementOutFile.setDec(dec);
		return Type.Void;
	}

	@Override
	public Object visitStatementOutScreen (StatementOutScreen statementOutScreen, Object arg) throws Exception {
		Token f = statementOutScreen.first();
		Dec dec = getDecForEntry(statementOutScreen.name(), f);
		Type tX = (Type) statementOutScreen.X().visit(this, Type.Int);
		Type tY = (Type) statementOutScreen.Y().visit(this, Type.Int);

		if (tX != tY) throw new TypeException(f, "Type mismatch error for X and Y");

		if (dec.type() == Type.Int || dec.type() == Type.String) {
			if (tX != Type.Void) throw new TypeException(f, "Expected Type Void, got " + tX + " instead");
		} else if (dec.type() == Type.Image) {
			if (tX != Type.Void && tX != Type.Int)
				throw new TypeException(f, "Expected Type int, got " + tX + " instead");
		} else {
			throw new TypeException(f, "Expected Type Int | String | Image, got " + dec.type() + " instead");
		}

		statementOutScreen.setDec(dec);
		return Type.Void;
	}

}
