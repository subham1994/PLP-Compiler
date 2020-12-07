/**
 * Code developed for the class project in COP5556 Programming Language Principles 
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

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

import cop5556fa20.runtime.PixelOps;
import org.junit.jupiter.api.Test;

import cop5556fa20.AST.Program;
import cop5556fa20.CodeGenUtils.DynamicClassLoader;
import cop5556fa20.runtime.BufferedImageUtils;
import cop5556fa20.runtime.LoggedIO;
import cop5556fa20.runtime.PLPImage;
import cop5556fa20.runtime.PLPImage.PLPImageException;
import cop5556fa20.resources.ImageResources;

class CodeGen6Test {

	static boolean doPrint = true;
	static void show(Object s) {
		if (doPrint) {
			System.out.println(s);
		}
	}


	static boolean doKeepFrames = true;
	static void keepFrames() throws IOException {
		if (doKeepFrames) {
		        System.out.println("enter any char to close frames and exit");
		        int b = System.in.read();
		    }
	}
	
	static boolean writeClassFile = true;
	
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
			String classFileName = ImageResources.binDir + File.separator + className + ".class";
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
	

	String getInputFromFile(String fileName) throws IOException {
		Path path = Path.of(fileName);
		return Files.readString(path);
	}
	
	void genRun(String input, String[] args) throws Exception {
		String classname = name(2);
		byte[] bytecode = genCode(input, classname, writeClassFile);
		runCode(classname, bytecode, args);
	}
	
	void genRun(String input) throws Exception {
		String classname = name(2);
		byte[] bytecode = genCode(input, classname, writeClassFile);
		runCode(classname, bytecode, new String[0]);
	}
	
	
	
	// returns name of method enclosing this one.
	String name() {
		String nameofCurrMethod = new Throwable().getStackTrace()[1].getMethodName();
		return nameofCurrMethod;
	}    
    
    
	// nesting = 0 is method name, 1 is caller, 2 is caller of caller, etc.
	String name(int nesting) {
		String nameofCurrMethod = new Throwable().getStackTrace()[nesting].getMethodName();
		return nameofCurrMethod;
	}
	
	static final int Z=255;
	static final int WHITE = 0xffffffff;
	static final int SILVER = 0xffc0c0c0;
	static final int GRAY=0xff808080;
	static final int BLACK= 0xff000000;
	static final int RED= 0xffff0000;
	static final int MAROON= 0xff800000;
	static final int YELLOW= 0xffffff00;
	static final int OLIVE= 0xff808000;
	static final int LIME= 0xff00ff00;
	static final int GREEN= 0xff008000;
	static final int AQUA= 0xff00ffff;
	static final int TEAL= 0xff008080;
	static final int BLUE= 0xff0000ff;
	static final int NAVY= 0xff000080;
	static final int FUCHSIA= 0xffff00ff;
	static final int PURPLE= 0xff800080;
	
/***********************************************************************/
	
	@Test
	public void loadImage0a() throws Exception {
		String input = """
				image a <- @0;
				a -> screen[640, 480];
				""";
	    String[] args = {ImageResources.urlTower};
		genRun(input,args);
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		PLPImage a = new PLPImage(BufferedImageUtils.fetchBufferedImage(args[0]),null);
		expectedLog.add(a);
		assertEquals(expectedLog, LoggedIO.globalLog);		
//		keepFrames();
	}

	
	@Test
	public void loopExampleFromDesc() throws Exception {
		String input = """
				image[400,500] a;
				a =* [X,Y] : X <= Y : RED;
				a -> screen;
				""";
		genRun(input);
		PLPImage a = new PLPImage(BufferedImageUtils.createBufferedImage(400, 500), new Dimension(400,500));
		a.ensureImageAllocated(0, 0);
		int w = a.getWidth();
		int h = a.getHeight();
		for (int X = 0; X < w; X++) {
			for (int Y = 0; Y < h; Y++) {
				if (X <= Y) {
					a.updatePixel(X, Y, RED);
				}
			}
		}
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add(a);
		assertEquals(expectedLog, LoggedIO.globalLog);		
//		keepFrames();
	}
	

	@Test void weave() throws Exception {
		String input = """
				//Takes two images and creates a square image 
				//by cropping the first, scaling the second, 
				//and weaving them together in an checkerboard pattern.
				image source <- @0;
				//find min dimension
				int w = source#width;
				int h = source#height;
				int size = w <= h ? w : h;
				//get overlay image, scaling to square of needed size
				image[size,size] overlay <- @1;
				int xoffset = (w-size)/2;
				int yoffset = (h-size)/2;
				//copy to new square image, cropping while keeping the center part
				image[size,size] checkerboard;
				checkerboard = *[X,Y] :: source[X+xoffset,Y+yoffset];
				int a = 8;
				int b = a/2;
				checkerboard = *[X,Y]: (X%a < b &  Y%a < b) | (b <= X%a  & b <= Y%a)  : overlay[X,Y];
				checkerboard -> screen;
				""";
	    String[] args = {ImageResources.urlKanapaha, ImageResources.urlTower};
		genRun(input, args);
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		PLPImage source = new PLPImage(BufferedImageUtils.fetchBufferedImage(args[0]), null);
		int w = source.getWidth();
		int h = source.getHeight();
		int size = w <=h ? w : h;
		PLPImage overlay = new PLPImage(BufferedImageUtils.resizeBufferedImage(BufferedImageUtils.fetchBufferedImage(args[1]), size, size), new Dimension(size,size));
		PLPImage checkerboard = new PLPImage(BufferedImageUtils.createBufferedImage(size, size), new Dimension(size,size));
		int xoffset = (w-size)/2;
		int yoffset = (h-size)/2;
		for (int X = 0; X < size; X++) {
			for (int Y = 0; Y < size; Y++) {
				checkerboard.updatePixel(X, Y, source.selectPixel(X+xoffset, Y+yoffset));
			}
		}
		int a = 8;
		int b = a/2;
		for (int X = 0; X < size; X++) {
			for (int Y = 0; Y < size; Y++) {
				if ((X%a < b &&  Y%a < b) || (b <= X%a  && b <= Y%a)) {
					checkerboard.updatePixel(X, Y, overlay.selectPixel(X, Y));
				}
			}
		}
		expectedLog.add(checkerboard);
		assertEquals(expectedLog, LoggedIO.globalLog);
//		keepFrames();
	}
	

	
	@Test void hashWidthfail() throws Exception {
		String input = """
				image a;
				int b = a # width;
				""";
		Exception exception = assertThrows(PLPImageException.class, () -> {
			genRun(input);
		});
		show(exception);	
//		keepFrames();
	}


	@Test
	void hashPass() throws Exception {
		String input = """
				image[640, 480] a <- @0;
				int b = a # width;
				b -> screen;
				image[320, 240] c;
				int d = c # height;
				d -> screen;
				image e <- @0;
				int f = e # width;
				f -> screen;
				int g = 1000 # green;
				g -> screen;
				""";
		String[] args = {ImageResources.urlKanapaha};
		genRun(input, args);
		PLPImage b = new PLPImage(BufferedImageUtils.resizeBufferedImage(
				BufferedImageUtils.fetchBufferedImage(args[0]), 640, 480
		), new Dimension(640, 480));
		PLPImage c = new PLPImage(BufferedImageUtils.fetchBufferedImage(args[0]), null);
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add(b.getWidth());
		expectedLog.add(240);
		expectedLog.add(c.getWidthThrows(0, 0));
		expectedLog.add(PixelOps.getGreen(1000));
		assertEquals(expectedLog, LoggedIO.globalLog);
//		keepFrames();
	}


	@Test
	public void testStatementAssignPass () throws Exception {
		String input = """
    			int args;
    			args = 2 + 3;
    			args -> screen;
				image a <- @0;
				image b = a;
				image temp;
				temp = a;
				b -> screen;
				image[640, 480] c <- @0;
				image[640, 480] d = c;
				image[640, 480] e;
				e = d;
				d -> screen;
				""";
		String[] args = {ImageResources.urlKanapaha};
		genRun(input,args);
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		PLPImage a = new PLPImage(BufferedImageUtils.fetchBufferedImage(args[0]),null);
		PLPImage b = new PLPImage(BufferedImageUtils.resizeBufferedImage(
				BufferedImageUtils.fetchBufferedImage(args[0]), 640, 480
		), new Dimension(640, 480));
		PLPImage c = new PLPImage(b.image, new Dimension(640, 480));
		expectedLog.add(5);
		expectedLog.add(a);
		expectedLog.add(c);
		assertEquals(expectedLog, LoggedIO.globalLog);
//		keepFrames();
	}


	@Test
	void testStatementAssignFailA () throws Exception {
		String input = """
				image a;
				image b;
				b = a;
				b -> screen;
				""";
		Exception exception = assertThrows(PLPImageException.class, () -> {
			genRun(input);
		});
		show(exception);
//		keepFrames();
	}


	@Test
	void testStatementAssignFailB () throws Exception {
		String input = """
				image[320, 240] a;
				image b <- @0;
				a = b;
				b -> screen;
				""";

		String[] args = {ImageResources.urlKanapaha};
		Exception exception = assertThrows(PLPImageException.class, () -> {
			genRun(input, args);
		});
		show(exception);
//		keepFrames();
	}


	@Test
	public void testStatementImageInPass () throws Exception {
		String input = """
				image a <- @0;
				image b;
				b <- a;
				b -> screen;
				image[640, 480] c;
				c <- @0;
				c -> screen;
				""";

		String[] args = {ImageResources.urlKanapaha};
		genRun(input,args);
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		PLPImage a = new PLPImage(BufferedImageUtils.fetchBufferedImage(args[0]),null);
		expectedLog.add(a);
		expectedLog.add(new PLPImage(BufferedImageUtils.resizeBufferedImage(
				BufferedImageUtils.copyBufferedImage(a.image),
				640,
				480
		), new Dimension(640, 480)));
		assertEquals(expectedLog, LoggedIO.globalLog);
//		keepFrames();
	}

	@Test
	public void testStatementImageInFail () throws Exception {
		String input = """
				image a <- "some non existing URL";
				image b;
				b <- a;
				b -> screen;
				""";

		Exception exception = assertThrows(IOException.class, () -> {
			genRun(input);
		});
	}

	@Test
	public void testStatementOutFilePass () throws Exception {
		String input = """
				image a <- @0;
				a -> "out_test";
				image[320, 240] b <- "out_test.png";
				b -> screen;
				""";

		String[] args = {ImageResources.urlKanapaha};
		genRun(input,args);
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		PLPImage a = new PLPImage(BufferedImageUtils.fetchBufferedImage(args[0]),null);
		PLPImage b = new PLPImage(BufferedImageUtils.resizeBufferedImage(
				BufferedImageUtils.fetchBufferedImage("out_test.png"),320, 240
		), new Dimension(320, 240));
		expectedLog.add(new LoggedIO.PLPImageFile(a, "out_test"));
		expectedLog.add(b);
		assertEquals(expectedLog, LoggedIO.globalLog);
//		keepFrames();
	}

	@Test
	public void testPixelSelector() throws Exception {
		String input = """
				image a <- @0;
				int pixel = a[10, 20];
				pixel -> screen;
				""";

		String[] args = {ImageResources.urlKanapaha};
		genRun(input,args);
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		PLPImage a = new PLPImage(BufferedImageUtils.fetchBufferedImage(args[0]),null);
		expectedLog.add(a.selectPixel(10, 20));
		assertEquals(expectedLog, LoggedIO.globalLog);
//		keepFrames();
	}

	@Test
	public void loopTestCase() throws Exception {
		String input = """
    			image other <- @0;
				image[400,500] a;
				a = *[X,Y]: : other[Y, X];
				a -> screen;
				""";

		String[] args = {ImageResources.urlTower};
		genRun(input,args);
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		PLPImage a = new PLPImage(null, new Dimension(400, 500));
		PLPImage other = new PLPImage(BufferedImageUtils.fetchBufferedImage(args[0]), null);
		a.ensureImageAllocated(0, 0);

		int w = a.getWidth();
		int h = a.getHeight();
		for (int X = 0; X < w; X++) {
			for (int Y = 0; Y < h; Y++) {
				a.updatePixel(X, Y, other.selectPixel(Y, X));
			}
		}

	 	expectedLog.add(a);
	 	assertEquals(expectedLog, LoggedIO.globalLog);
//		keepFrames();
	}
}
