/**
 * This code was developed for the class project in COP5556 Programming Language Principles 
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

import cop5556fa20.AST.Type;
import cop5556fa20.AST.*;
import cop5556fa20.runtime.LoggedIO;
import org.objectweb.asm.*;

import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

public class CodeGen5 implements ASTVisitor, Opcodes {
	
	final String className;
	final boolean isInterface = false;
	ClassWriter cw;
	MethodVisitor mv;
	
	public CodeGen5(String className) {
		super();
		this.className = className;
	}
	
	
	@Override
	public Object visitDecImage(DecImage decImage, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}
	
	/**
	 * Add a static field to the class for this variable.
	 */
	@Override
	public Object visitDecVar(DecVar decVar, Object arg) throws Exception {
		String varName = decVar.name();
		Type type = decVar.type();
		String desc;

		FieldVisitor fieldVisitor;
		if (type == Type.String) {
			desc = "Ljava/lang/String;";
			fieldVisitor = cw.visitField(ACC_STATIC, varName, desc, null, null);
		} else {
			desc = "I";
			fieldVisitor = cw.visitField(ACC_STATIC, varName, desc, null, 0);
		}

		fieldVisitor.visitEnd();

		//evaluate initial value and store in variable, if one is given.
		Expression e = decVar.expression();
		if (e != Expression.empty) {
			e.visit(this, type); // generates code to evaluate expression and leave value on top of the stack
			mv.visitFieldInsn(PUTSTATIC, className, varName, desc);
		}

		return null;
	}

	@Override
	public Object visitExprArg(ExprArg exprArg, Object arg) throws Exception {
		mv.visitVarInsn(ALOAD, 0);
		exprArg.e().visit(this, arg);
		mv.visitInsn(AALOAD);

		if (exprArg.type() == Type.Int) {
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer","parseInt","(Ljava/lang/String;)I",false);
		}

		return null;
	}

	@Override
	public Object visitExprBinary(ExprBinary exprBinary, Object arg) throws Exception {
		Map<Scanner.Kind, Integer> opMap = Map.ofEntries(
				entry(Scanner.Kind.STAR, IMUL),
				entry(Scanner.Kind.DIV, IDIV),
				entry(Scanner.Kind.MOD, IREM),
				entry(Scanner.Kind.MINUS, ISUB),
				entry(Scanner.Kind.AND, IAND),
				entry(Scanner.Kind.OR, IOR),
				entry(Scanner.Kind.EQ, exprBinary.type() == Type.Int ? IF_ICMPEQ : IF_ACMPEQ),
				entry(Scanner.Kind.NEQ, exprBinary.type() == Type.Int ? IF_ICMPNE : IF_ACMPNE),
				entry(Scanner.Kind.LT, IF_ICMPLT),
				entry(Scanner.Kind.GT, IF_ICMPGT),
				entry(Scanner.Kind.GE, IF_ICMPGE),
				entry(Scanner.Kind.LE, IF_ICMPLE)
		);

		switch (exprBinary.op()) {
			case PLUS -> {
				if (exprBinary.type() == Type.Int) {
					exprBinary.e0().visit(this, arg);
					exprBinary.e1().visit(this, arg);
					mv.visitInsn(IADD);
				} else if (exprBinary.type() == Type.String) {
					String descS = "java/lang/String";
					String descSB = "java/lang/StringBuilder";

					mv.visitTypeInsn(NEW, descSB);
					mv.visitInsn(DUP);
					mv.visitMethodInsn(INVOKESPECIAL, descSB,"<init>","()V",false);
					exprBinary.e0().visit(this, arg);

					mv.visitMethodInsn(INVOKEVIRTUAL, descSB,"append", "(L" + descS + ";)L" + descSB + ";",false);
					exprBinary.e1().visit(this, arg);
					mv.visitMethodInsn(INVOKEVIRTUAL, descSB,"append", "(L" + descS + ";)L" + descSB + ";",false);



					mv.visitMethodInsn(INVOKEVIRTUAL, descSB, "toString","()L" + descS + ";",false);
				}
			}
			case STAR, DIV, MOD, MINUS, AND, OR -> {
				exprBinary.e0().visit(this, arg);
				exprBinary.e1().visit(this, arg);
				mv.visitInsn(opMap.get(exprBinary.op()));
			}
			case EQ, NEQ, LT, GT, GE, LE -> {
				exprBinary.e0().visit(this, arg);
				exprBinary.e1().visit(this, arg);

				Label l0 = new Label();
				Label l1 = new Label();

				mv.visitJumpInsn(opMap.get(exprBinary.op()), l0);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, l1);
				mv.visitLabel(l0);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(l1);
			}
			default -> {
				throw new UnsupportedOperationException("not yet implemented");
			}
		}
		return null;
	}

	@Override
	public Object visitExprConditional(ExprConditional exprConditional, Object arg) throws Exception {
		exprConditional.condition().visit(this, arg);

		Label l0 = new Label();
		Label l1 = new Label();

		mv.visitJumpInsn(IFEQ, l0);
		exprConditional.trueCase().visit(this, arg);
		mv.visitJumpInsn(GOTO, l1);
		mv.visitLabel(l0);
		exprConditional.falseCase().visit(this, arg);
		mv.visitLabel(l1);
		return null;
	}

	@Override
	public Object visitExprConst(ExprConst exprConst, Object arg) throws Exception {
		mv.visitLdcInsn(exprConst.value());
		return null;
	}

	@Override
	public Object visitExprHash(ExprHash exprHash, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object visitExprIntLit(ExprIntLit exprIntLit, Object arg) throws Exception {
		mv.visitLdcInsn(exprIntLit.value());
		return null;
	}

	@Override
	public Object visitExprPixelConstructor(ExprPixelConstructor exprPixelConstructor, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object visitExprPixelSelector(ExprPixelSelector exprPixelSelector, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}
	
	/**
	 * generate code to put the value of the StringLit on the stack.
	 */
	@Override
	public Object visitExprStringLit(ExprStringLit exprStringLit, Object arg) throws Exception {
		mv.visitLdcInsn(exprStringLit.text());
		return null;
	}

	@Override
	public Object visitExprUnary(ExprUnary exprUnary, Object arg) throws Exception {
		exprUnary.e().visit(this, arg);

		switch (exprUnary.op()) {
			case PLUS -> {}
			case MINUS -> {
				mv.visitInsn(INEG);
			}
			case EXCL -> {
				Label l0 = new Label();
				Label l1 = new Label();

				mv.visitJumpInsn(IFEQ, l0);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, l1);
				mv.visitLabel(l0);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(l1);
			}
			default -> {
				throw new UnsupportedOperationException("not yet implemented");
			}
		}

		return null;
	}

	@Override
	public Object visitExprVar(ExprVar exprVar, Object arg) throws Exception {
		String desc = exprVar.type() == Type.Int ? "I" : "Ljava/lang/String;";
		mv.visitFieldInsn(GETSTATIC, className, exprVar.name(), desc);
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
//		cw = new ClassWriter(0); //If the call to methodVisitor.visitMaxs crashes, it
		// is
		// sometime helpful to
		// temporarily run it without COMPUTE_FRAMES. You
		// won't get a completely correct classfile, but
		// you will be able to see the code that was
		// generated.

		// String sourceFileName = className; //TODO Temporary solution, FIX THIS
		int version = -65478;
		cw.visit(version, ACC_PUBLIC | ACC_SUPER, className, null, "java/lang/Object", null);
		cw.visitSource(null, null);
		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();
		// insert label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);
		// visit children to add instructions to method
		List<ASTNode> nodes = program.decOrStatement();
		for (ASTNode node : nodes) {
			node.visit(this, null);
		}
		// add  required (by the JVM) return statement to main
		mv.visitInsn(RETURN);

		// adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		// handles parameters and local variables of main. The only local var is args
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		// Sets max stack size and number of local vars.
		// Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the constructor,
		// asm will calculate this itself and the parameters are ignored.
		// If you have trouble with failures in this routine, it may be useful
		// to temporarily set the parameter in the ClassWriter constructor to 0.
		// The generated classfile will not pass verification, but you will at least be
		// able to see what instructions it contains.
		mv.visitMaxs(0, 0);

		// finish construction of main method
		mv.visitEnd();

		// finish class construction
		cw.visitEnd();

		// generate classfile as byte array and return
		return cw.toByteArray();

	}

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws Exception {
		String desc = statementAssign.dec().type() == Type.Int ? "I" : "Ljava/lang/String;";
		statementAssign.expression().visit(this, arg);
		mv.visitFieldInsn(PUTSTATIC, className, statementAssign.name(), desc);
		return null;
	}

	@Override
	public Object visitStatementImageIn(StatementImageIn statementImageIn, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object visitStatementLoop(StatementLoop statementLoop, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object visitExprEmpty(ExprEmpty exprEmpty, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object visitStatementOutFile(StatementOutFile statementOutFile, Object arg) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object visitStatementOutScreen(StatementOutScreen statementOutScreen, Object arg) throws Exception {
		String name = statementOutScreen.name();
		Dec dec = statementOutScreen.dec();
		Type type = dec.type();
		String desc;

		switch (type) {
			case String -> {
				desc = "Ljava/lang/String;";
				mv.visitFieldInsn(GETSTATIC, className, name, desc);
				mv.visitMethodInsn(INVOKESTATIC, LoggedIO.className, "stringToScreen", LoggedIO.stringToScreenSig,
						isInterface);
			}
			case Int -> {
				desc = "I";
				mv.visitFieldInsn(GETSTATIC, className, name, desc);
				mv.visitMethodInsn(INVOKESTATIC, LoggedIO.className, "intToScreen", LoggedIO.intToScreenSig,
						isInterface);
			}
			default -> {
				throw new UnsupportedOperationException("not yet implemented");
			}
		}

		return null;
	}
}
