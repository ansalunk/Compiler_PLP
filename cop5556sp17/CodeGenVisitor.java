package cop5556sp17;

import java.util.ArrayList;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.Chain;
import cop5556sp17.AST.ChainElem;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.FilterOpChain;
import cop5556sp17.AST.FrameOpChain;
import cop5556sp17.AST.IdentChain;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IdentLValue;
import cop5556sp17.AST.IfStatement;
import cop5556sp17.AST.ImageOpChain;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.ParamDec;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.SleepStatement;
import cop5556sp17.AST.Statement;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;
	int slotNo = 1;
	int count = 0;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.getName();
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null,
				"java/lang/Object", new String[] { "java/lang/Runnable" });
		cw.visitSource(sourceFileName, null);

		// generate constructor code
		// get a MethodVisitor
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "([Ljava/lang/String;)V",
				null, null);
		mv.visitCode();
		// Create label at start of code
		Label constructorStart = new Label();
		mv.visitLabel(constructorStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering <init>");
		// generate code to call superclass constructor
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V",
				false);
		// visit parameter decs to add each as field to the class
		// pass in mv so decs can add their initialization code to the
		// constructor.
		ArrayList<ParamDec> params = program.getParams();
		int i = 0;
		for (ParamDec dec : params)
			dec.visit(this, i++);
		mv.visitInsn(RETURN);
		// create label at end of code
		Label constructorEnd = new Label();
		mv.visitLabel(constructorEnd);
		// finish up by visiting local vars of constructor
		// the fourth and fifth arguments are the region of code where the local
		// variable is defined as represented by the labels we inserted.
		mv.visitLocalVariable("this", classDesc, null, constructorStart,
				constructorEnd, 0);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null,
				constructorStart, constructorEnd, 1);
		// indicates the max stack size for the method.
		// because we used the COMPUTE_FRAMES parameter in the classwriter
		// constructor, asm
		// will do this for us. The parameters to visitMaxs don't matter, but
		// the method must
		// be called.
		mv.visitMaxs(1, 1);
		// finish up code generation for this method.
		mv.visitEnd();
		// end of constructor

		// create main method which does the following
		// 1. instantiate an instance of the class being generated, passing the
		// String[] with command line arguments
		// 2. invoke the run method.
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main",
				"([Ljava/lang/String;)V", null, null);
		mv.visitCode();
		Label mainStart = new Label();
		mv.visitLabel(mainStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering main");
		mv.visitTypeInsn(NEW, className);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, className, "<init>",
				"([Ljava/lang/String;)V", false);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "run", "()V", false);
		mv.visitInsn(RETURN);
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart,
				mainEnd, 0);
		mv.visitLocalVariable("instance", classDesc, null, mainStart, mainEnd,
				1);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		// create run method
		mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
		mv.visitCode();
		Label startRun = new Label();
		mv.visitLabel(startRun);
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering run");
		program.getB().visit(this, null);
		mv.visitInsn(RETURN);
		Label endRun = new Label();
		mv.visitLabel(endRun);
		mv.visitLocalVariable("this", classDesc, null, startRun, endRun, 0);
		// TODO visit the local variables

		ArrayList<Dec> decs = program.getB().getDecs();
		for (Dec d : decs) {
			String localVar = d.getIdent().getText();
			String localDesc = d.getTypeField().getJVMTypeDesc();
			int dslot = d.getSlot();
			mv.visitLocalVariable(localVar, localDesc, null, startRun, endRun,
					dslot);
		}

		mv.visitMaxs(1, 1);
		mv.visitEnd(); // end of run method

		cw.visitEnd();// end of class

		// generate classfile and return it
		return cw.toByteArray();
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement,
			Object arg) throws Exception {
		assignStatement.getE().visit(this, arg);
		CodeGenUtils.genPrint(DEVEL, mv,
				"\nassignment: " + assignStatement.var.getText() + "=");
		CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE()
				.getTypeField());

		assignStatement.getVar().visit(this, arg);
		/*
		 * if (assignStatement.getVar().getDec().getTypeField()
		 * .isType(TypeName.IMAGE)) { mv.visitMethodInsn(INVOKESTATIC,
		 * PLPRuntimeImageOps.JVMName, "copyImage",
		 * PLPRuntimeImageOps.copyImageSig, false); }
		 */
		return null;
	}

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg)
			throws Exception {

		Chain leftExpr = binaryChain.getE0();
		leftExpr.visit(this, "fromLeft");

		if (leftExpr.getTypeField().isType(TypeName.URL)) {
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className,
					"readFromURL", PLPRuntimeImageIO.readFromURLSig, false);
		} else if (leftExpr.getTypeField().isType(TypeName.FILE)) {
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className,
					"readFromFile", PLPRuntimeImageIO.readFromFileDesc, false);
		}

		mv.visitInsn(DUP);

		ChainElem rightExpr = binaryChain.getE1();
		rightExpr.visit(this, "fromRight");

		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression,
			Object arg) throws Exception {
		// TODO Implement this

		Label isTrue = new Label();
		Label end = new Label();
		Expression expr0 = binaryExpression.getE0();
		Expression expr1 = binaryExpression.getE1();
		binaryExpression.getE0().visit(this, arg);
		binaryExpression.getE1().visit(this, arg);
		String operator = binaryExpression.getOp().getText();

		switch (operator) {
		case "+":
			if (expr0.getTypeField().isType(TypeName.IMAGE)
					&& expr1.getTypeField().isType(TypeName.IMAGE)) {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName,
						"add", PLPRuntimeImageOps.addSig, false);
			} else
				mv.visitInsn(IADD);
			break;
		case "-":
			if (expr0.getTypeField().isType(TypeName.IMAGE)
					&& expr1.getTypeField().isType(TypeName.IMAGE)) {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName,
						"sub", PLPRuntimeImageOps.subSig, false);
			} else
				mv.visitInsn(ISUB);
			break;
		case "/":
			if (expr0.getTypeField().isType(TypeName.IMAGE)
					&& expr1.getTypeField().isType(TypeName.INTEGER)) {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName,
						"div", PLPRuntimeImageOps.divSig, false);
			} else
				mv.visitInsn(IDIV);
			break;
		case "&":
			mv.visitInsn(IAND);
			break;
		case "|":
			mv.visitInsn(IOR);
			break;
		case "*":
			if ((expr0.getTypeField().isType(TypeName.IMAGE) && expr1
					.getTypeField().isType(TypeName.INTEGER))
					|| (expr1.getTypeField().isType(TypeName.IMAGE) && expr0
							.getTypeField().isType(TypeName.INTEGER))) {
				if (expr1.getTypeField().isType(TypeName.IMAGE))
					mv.visitInsn(SWAP);

				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName,
						"mul", PLPRuntimeImageOps.mulSig, false);
			} else
				mv.visitInsn(IMUL);
			break;
		case "==":
			mv.visitJumpInsn(IF_ICMPEQ, isTrue);
			mv.visitLdcInsn(false);
			break;
		case "!=":
			mv.visitJumpInsn(IF_ICMPNE, isTrue);
			mv.visitLdcInsn(false);
			break;
		case ">=":
			mv.visitJumpInsn(IF_ICMPGE, isTrue);
			mv.visitLdcInsn(false);
			break;
		case "<=":
			mv.visitJumpInsn(IF_ICMPLE, isTrue);
			mv.visitLdcInsn(false);
			break;
		case "<":
			mv.visitJumpInsn(IF_ICMPLT, isTrue);
			mv.visitLdcInsn(false);
			break;
		case ">":
			mv.visitJumpInsn(IF_ICMPGT, isTrue);
			mv.visitLdcInsn(false);
			break;
		case "%":
			if (expr0.getTypeField().isType(TypeName.IMAGE)
					&& expr1.getTypeField().isType(TypeName.INTEGER)) {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName,
						"mod", PLPRuntimeImageOps.modSig, false);
			} else
				mv.visitInsn(IREM);
		default:
			break;
		}
		mv.visitJumpInsn(GOTO, end);

		mv.visitLabel(isTrue);
		mv.visitLdcInsn(true);
		mv.visitLabel(end);

		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO Implement this
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering block");
		// startlabel and endlabel
		Label blckStart = new Label();
		Label blckEnd = new Label();
		mv.visitLabel(blckStart);

		ArrayList<Dec> decs = block.getDecs();
		for (Dec dec : decs) {
			dec.setSlot(slotNo++);
			dec.visit(this, arg);
		}

		ArrayList<Statement> statements = block.getStatements();
		for (Statement statement : statements) {
						statement.visit(this, arg);
			/*if (statement instanceof AssignmentStatement) {
				if (((AssignmentStatement) statement).getVar().getDec() instanceof ParamDec) {
					mv.visitVarInsn(ALOAD, 0);
				}
			}*/
			if (statement instanceof BinaryChain) {
				mv.visitInsn(POP);
			}

		}

		mv.visitLabel(blckEnd);

		return null;
	}

	@Override
	public Object visitBooleanLitExpression(
			BooleanLitExpression booleanLitExpression, Object arg)
			throws Exception {
		// TODO Implement this
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering booleanlit");
		//mv.visitLdcInsn(booleanLitExpression.getValue());
		
		 if(booleanLitExpression.getValue()) {
	            mv.visitInsn(ICONST_1);
	        }
	        else {
	            mv.visitInsn(ICONST_0);
	        }

		return null;
	}

	@Override
	public Object visitConstantExpression(
			ConstantExpression constantExpression, Object arg) {

		if (constantExpression.getFirstToken().kind.equals(Kind.KW_SCREENWIDTH)) {
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName,
					"getScreenWidth", PLPRuntimeFrame.getScreenWidthSig, false);
		} else if (constantExpression.getFirstToken().kind.equals(
				Kind.KW_SCREENHEIGHT)) {
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName,
					"getScreenHeight", PLPRuntimeFrame.getScreenHeightSig,
					false);
		}
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		// TODO Implement this
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering dec");
		//declaration.setSlot(slotNo++);
		TypeName typeName = declaration.getTypeField();

		if (typeName.equals(TypeName.FRAME) || typeName.equals(TypeName.IMAGE)) {
			mv.visitInsn(ACONST_NULL);
			mv.visitVarInsn(ASTORE, declaration.getSlot());
		}

		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg)
			throws Exception {
		Kind kind = filterOpChain.getFirstToken().kind;

		mv.visitInsn(POP);

		mv.visitInsn(ACONST_NULL);

		switch (kind) {
		case OP_GRAY:
			mv.visitInsn(POP);
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName,
					"grayOp", PLPRuntimeFilterOps.opSig, false);
			break;
		case OP_BLUR:
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName,
					"blurOp", PLPRuntimeFilterOps.opSig, false);

			break;
		case OP_CONVOLVE:
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName,
					"convolveOp", PLPRuntimeFilterOps.opSig, false);

			break;
		default:
			break;
		}

		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg)
			throws Exception {
		Kind kind = frameOpChain.getFirstToken().kind;

		frameOpChain.getArg().visit(this, arg);

		switch (kind) {
		case KW_SHOW:
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName,
					"showImage", PLPRuntimeFrame.showImageDesc, false);
			break;

		case KW_HIDE:
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName,
					"hideImage", PLPRuntimeFrame.hideImageDesc, false);
			break;
		case KW_XLOC:
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName,
					"getXVal", PLPRuntimeFrame.getXValDesc, false);
			break;
		case KW_YLOC:
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName,
					"getYVal", PLPRuntimeFrame.getYValDesc, false);
			break;
		case KW_MOVE:
			// frameOpChain.getArg().visit(this, arg);
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName,
					"moveFrame", PLPRuntimeFrame.moveFrameDesc, false);
			break;
		default:
			break;
		}

		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg)
			throws Exception {

		TypeName type = identChain.getTypeField();
		String isLeft = (String) arg;

		Dec dec = identChain.getDec();
		String fieldTypeLeft = dec.getTypeField().getJVMTypeDesc();
		String fieldNameLeft = dec.getIdent().getText();
		// String typeName=identChain.getTypeField().getJVMTypeDesc();

		if (isLeft.equalsIgnoreCase("fromLeft")) {
			// Global type
			if (identChain.getDec() instanceof ParamDec) {
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, className, fieldNameLeft, fieldTypeLeft);
			} else {
				// local type
				switch (type) {
				case INTEGER:
					mv.visitVarInsn(ILOAD, dec.getSlot());
					break;
				case IMAGE:
				case FILE:
				case FRAME:
				default:
					mv.visitVarInsn(ALOAD, dec.getSlot());
					break;
				}
			}
		} else {
			
			String fieldTypeRight = dec.getTypeField().getJVMTypeDesc();
			String fieldNameRight = dec.getIdent().getText();
			
			if (identChain.getDec() instanceof ParamDec) {
				if (identChain.getDec().getTypeField().equals(TypeName.FILE)) {
					mv.visitInsn(POP);
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, className, fieldNameRight, fieldTypeRight);
					mv.visitMethodInsn(INVOKESTATIC,
							PLPRuntimeImageIO.className, "write",
							PLPRuntimeImageIO.writeImageDesc, false);
				} else {
					mv.visitVarInsn(ALOAD, 0);
					mv.visitInsn(SWAP);
					mv.visitFieldInsn(PUTFIELD, className, fieldNameRight, fieldTypeRight);
				}

			} else {

				switch (identChain.getDec().getTypeField()) {
				case INTEGER:
					mv.visitVarInsn(ISTORE, dec.getSlot());
					break;
				case IMAGE:
					mv.visitVarInsn(ASTORE, dec.getSlot());
					break;
				/*case FILE :
                    mv.visitInsn(POP);
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, className, identChain.getDec().getIdent().getText(),
                            identChain.getDec().getTypeField().getJVMTypeDesc());
                    mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "write",
                            PLPRuntimeImageIO.writeImageDesc, false);
                    break;*/
				case FRAME:
					mv.visitInsn(POP);
					mv.visitVarInsn(ALOAD, dec.getSlot());
					mv.visitMethodInsn(INVOKESTATIC,
							PLPRuntimeFrame.JVMClassName, "createOrSetFrame",
							PLPRuntimeFrame.createOrSetFrameSig, false);
					mv.visitInsn(DUP);
					mv.visitVarInsn(ASTORE, dec.getSlot());
					break;
				default:
					// mv.visitVarInsn(ALOAD, dec.getSlot());
					break;
				}
			}

		}
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression,
			Object arg) throws Exception {
		// TODO Implement this
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering ident exp");

		if (identExpression.getDec() instanceof ParamDec) {

			mv.visitVarInsn(ALOAD, 0);

			// if (identExpression.getDec().getTypeField() == TypeName.INTEGER)
			mv.visitFieldInsn(GETFIELD, className, identExpression.getDec()
					.getIdent().getText(), identExpression.getDec()
					.getTypeField().getJVMTypeDesc());
			// else if (identExpression.getDec().getTypeField() ==
			// TypeName.BOOLEAN)
			// mv.visitFieldInsn(GETFIELD, className, identExpression
			// .getFirstToken().getText(), "Z");
		} else {
			String type = identExpression.getTypeField().getJVMTypeDesc();
			if (type.equals("I") || type.equals("Z")) {
				mv.visitVarInsn(ILOAD, identExpression.getDec().getSlot());
			} else
				mv.visitVarInsn(ALOAD, identExpression.getDec().getSlot());
		}
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg)
			throws Exception {
		// TODO Implement this
		if (identX.getDec() instanceof ParamDec) {
			mv.visitVarInsn(ALOAD, 0);
			mv.visitInsn(SWAP);
			mv.visitFieldInsn(PUTFIELD, className, identX.getDec().getIdent()
					.getText(), identX.getDec().getTypeField().getJVMTypeDesc());
		} else {

			switch (identX.getDec().getTypeField()) {
			case INTEGER:
			case BOOLEAN:
				mv.visitVarInsn(ISTORE, identX.getDec().getSlot());
				break;
			case IMAGE:
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName,
						"copyImage", PLPRuntimeImageOps.copyImageSig, false);
			default:
				mv.visitVarInsn(ASTORE, identX.getDec().getSlot());
				break;
			}
		}
		return null;

	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg)
			throws Exception {
		// TODO Implement this
		Label afterExp = new Label();

		ifStatement.getE().visit(this, arg);
		mv.visitJumpInsn(IFEQ, afterExp);
		ifStatement.getB().visit(this, arg);
		mv.visitLabel(afterExp);

		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg)
			throws Exception {

		imageOpChain.getArg().visit(this, arg);
		Kind kind = imageOpChain.getFirstToken().kind;

		switch (kind) {
		case OP_WIDTH:
			mv.visitMethodInsn(INVOKEVIRTUAL,
					PLPRuntimeImageIO.BufferedImageClassName, "getWidth",
					PLPRuntimeImageOps.getWidthSig, false);
			break;
		case OP_HEIGHT:
			mv.visitMethodInsn(INVOKEVIRTUAL,
					PLPRuntimeImageIO.BufferedImageClassName, "getHeight",
					PLPRuntimeImageOps.getHeightSig, false);
			break;

		case KW_SCALE:
			// imageOpChain.getArg().visit(this, arg);
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName,
					"scale", PLPRuntimeImageOps.scaleSig, false);
			break;

		default:
			break;
		}

		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression,
			Object arg) throws Exception {
		// TODO Implement this
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering intlit");
		mv.visitLdcInsn(intLitExpression.getFirstToken().intVal());
		return null;
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		// TODO Implement this
		// For assignment 5, only needs to handle integers and booleans

		String fieldType = paramDec.getTypeField().getJVMTypeDesc();
		String fieldName = paramDec.getIdent().getText();
		paramDec.setSlot(-1);
		FieldVisitor fieldVisitor = cw.visitField(ACC_PUBLIC, fieldName,
				fieldType, null, null);
		fieldVisitor.visitEnd();

		TypeName typeField = paramDec.getTypeField();

		switch (typeField) {
		case INTEGER: {
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(count);
			count++;
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt",
					"(Ljava/lang/String;)I", false);
			mv.visitFieldInsn(PUTFIELD, className, fieldName, fieldType);
		}
			break;
		case BOOLEAN: {
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(count);
			count++;
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean",
					"parseBoolean", "(Ljava/lang/String;)Z", false);
			mv.visitFieldInsn(PUTFIELD, className, fieldName, fieldType);
		}
			break;
		case FILE: {
			mv.visitVarInsn(ALOAD, 0);
			mv.visitTypeInsn(NEW, "java/io/File");
			mv.visitInsn(DUP);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(count);
			count++;
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>",
					"(Ljava/lang/String;)V", false);
			mv.visitFieldInsn(PUTFIELD, className, fieldName, fieldType);
		}
			break;
		case URL: {
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(count);
			count++;
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className,
					"getURL", PLPRuntimeImageIO.getURLSig, false);
			mv.visitFieldInsn(PUTFIELD, className, fieldName, fieldType);
		}
			break;
		default:
			break;
		}

		return null;

	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg)
			throws Exception {
		sleepStatement.getE().visit(this, arg);
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V",
				false);

		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		for (Expression expr : tuple.getExprList())
			expr.visit(this, arg);
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg)
			throws Exception {
		// TODO Implement this

		Label guard = new Label();
		Label body = new Label();

		mv.visitJumpInsn(GOTO, guard);

		mv.visitLabel(body);
		whileStatement.getB().visit(this, null);

		mv.visitLabel(guard);
		whileStatement.getE().visit(this, arg);

		mv.visitJumpInsn(IFNE, body);

		return null;
	}

}
