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

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import cop5556fa20.AST.Program;
import cop5556fa20.CodeGenUtils.DynamicClassLoader;
import cop5556fa20.runtime.LoggedIO;

class CodeGen5Test {

	static boolean doPrint = true;
	static void show(Object s) {
		if (doPrint) {
			System.out.println(s);
		}
	}
	
	/**
	 * Generates and returns byte[] containing classfile implmenting given input program.
	 * 
	 * Throws exceptions for Lexical, Syntax, and Type checking errors
	 * 
	 * @param input   		String containing source code
	 * @param className		className and fileName of generated code
	 * @return        		Generated bytecode
	 * @throws Exception
	 */
	byte[] genCode(String input, String className, boolean doCreateFile) throws Exception {
		show(input);
		//scan, parse, and type check
		Scanner scanner = new Scanner(input);
		
		scanner.scan();
		Parser parser = new Parser(scanner);
		Program program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, className);
		show(program);

		//generate code
		CodeGenVisitorComplete cv = new CodeGenVisitorComplete(className);
		byte[] bytecode = (byte[]) program.visit(cv, null);
		//output the generated bytecode
		show(CodeGenUtils.bytecodeToString(bytecode));
		
		//write byte code to file 
		if (doCreateFile) {
			String classFileName = "bin/" + className + ".class";
			OutputStream output = new FileOutputStream(classFileName);
			output.write(bytecode);
			output.close();
			System.out.println("wrote classfile to " + classFileName);
		}
		
		//return generated classfile as byte array
		return bytecode;
	}
	
	/**
	 * Dynamically loads and executes the main method defined in the provided bytecode.
	 * If there are no command line arguments, commandLineArgs shoudl be an empty string (not null).
	 * 
	 * @param className
	 * @param bytecode
	 * @param commandLineArgs
	 * @throws Exception
	 */
	void runCode(String className, byte[] bytecode, String[] commandLineArgs) throws Exception  {
		LoggedIO.clearGlobalLog(); //initialize log used for testing.
		DynamicClassLoader loader = new DynamicClassLoader(Thread.currentThread().getContextClassLoader());
		Class<?> testClass = loader.define(className, bytecode);
		@SuppressWarnings("rawtypes")
		Class[] argTypes = {commandLineArgs.getClass()};
		Method m = testClass.getMethod("main", argTypes );
		show("Command line args: " + Arrays.toString(commandLineArgs));
		show("Output from " + m + ":");  //print name of method to be executed
		Object passedArgs[] = {commandLineArgs};  //create array containing params, in this case a single array.
		try {
		m.invoke(null, passedArgs);	
		}
		catch (Exception e) {
			Throwable cause = e.getCause();
			if (cause instanceof Exception) {
				Exception ec = (Exception) e.getCause();
				throw ec;
			}
			throw  e;
		}
	}
	
	/** Hello, World
	 */
	@Test
	public void helloWorld() throws Exception {
		String className = "HelloWorld";
		String input = """
				string s = "Hello, World!";
				int i = 2;
				s -> screen;
				i -> screen;
				""";
		byte[] bytecode = genCode(input, className, false);
		String[] args = {};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add("Hello, World!");
		expectedLog.add(2);
		assertEquals(expectedLog, LoggedIO.globalLog);
	}
	
	@Test
	public void commandLineArg0() throws Exception {
		String className = "CommandLineArg0";
		String input = """
				string s;
				s = @@@0;
				int i = @3;
				s -> screen;
				i -> screen;
				""";
				
		byte[] bytecode = genCode(input, className, false);
		String[] args =  {"1", "2", "Hello from the command line!", "2"};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add(args[2]);
		expectedLog.add(Integer.parseInt(args[3]));
		assertEquals(expectedLog, LoggedIO.globalLog);
	}


	@Test
	public void testStatementAssign() throws Exception {
		String className = "HelloWorld";
		String input = """
				string s;
				int i;
				s = "Hello, World!";
				i = 50;
				i -> screen;
				s -> screen;
				""";

		byte[] bytecode = genCode(input, className, false);
		String[] args = {};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add(50);
		expectedLog.add("Hello, World!");
		assertEquals(expectedLog, LoggedIO.globalLog);
	}

	@Test
	public void testExprUnary() throws Exception {
		String className = "HelloWorld";
		String input = """
				int i = +50;
				int j;
				j = (50 > 60) ? -3 : 4;
				i -> screen;
				j -> screen;
				""";

		byte[] bytecode = genCode(input, className, false);
		String[] args = {};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add(50);
		expectedLog.add(4);
		assertEquals(expectedLog, LoggedIO.globalLog);
	}

	@Test
	public void testExprBinary() throws Exception {
		String className = "HelloWorld";
		String input = """
				int i = 2 + (3 - 4) * 6 * 80 / (9 + 1) + 10;
				string s = "Subham" + " Gaurav";
				i -> screen;
				s -> screen;
				""";

		byte[] bytecode = genCode(input, className, false);
		String[] args = {};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add(-36);
		expectedLog.add("Subham Gaurav");
		assertEquals(expectedLog, LoggedIO.globalLog);
	}

	@Test
	public void testBoolean() throws Exception {
		String className = "HelloWorld";
		String input = """
				int i = !(2 > 3) & (5 < 6) & ("subam" != "gaurav" | "equal" == "equal") ? 10 : 20;
				i -> screen;
				""";

		byte[] bytecode = genCode(input, className, false);
		String[] args = {};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add(10);
		assertEquals(expectedLog, LoggedIO.globalLog);
	}

	@Test
	public void testFailedA() throws Exception {
		String className = "HelloWorld";
		String input = """
    			string a = "a";
    			string s = "a" != a ? "true" : "false";
    			s -> screen;
				""";

		byte[] bytecode = genCode(input, className, false);
		String[] args = {};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add("false");
		assertEquals(expectedLog, LoggedIO.globalLog);
	}
}
