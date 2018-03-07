package cop5556sp17;

import static cop5556sp17.AST.Type.TypeName.BOOLEAN;
import static cop5556sp17.AST.Type.TypeName.FILE;
import static cop5556sp17.AST.Type.TypeName.FRAME;
import static cop5556sp17.AST.Type.TypeName.IMAGE;
import static cop5556sp17.AST.Type.TypeName.INTEGER;
import static cop5556sp17.AST.Type.TypeName.NONE;
import static cop5556sp17.AST.Type.TypeName.URL;
import static cop5556sp17.Scanner.Kind.ARROW;
import static cop5556sp17.Scanner.Kind.BARARROW;
import static cop5556sp17.Scanner.Kind.DIV;
import static cop5556sp17.Scanner.Kind.EQUAL;
import static cop5556sp17.Scanner.Kind.GE;
import static cop5556sp17.Scanner.Kind.GT;
import static cop5556sp17.Scanner.Kind.KW_HIDE;
import static cop5556sp17.Scanner.Kind.KW_MOVE;
import static cop5556sp17.Scanner.Kind.KW_SCALE;
import static cop5556sp17.Scanner.Kind.KW_SHOW;
import static cop5556sp17.Scanner.Kind.KW_XLOC;
import static cop5556sp17.Scanner.Kind.KW_YLOC;
import static cop5556sp17.Scanner.Kind.LE;
import static cop5556sp17.Scanner.Kind.LT;
import static cop5556sp17.Scanner.Kind.MINUS;
import static cop5556sp17.Scanner.Kind.NOTEQUAL;
import static cop5556sp17.Scanner.Kind.OP_BLUR;
import static cop5556sp17.Scanner.Kind.OP_CONVOLVE;
import static cop5556sp17.Scanner.Kind.OP_GRAY;
import static cop5556sp17.Scanner.Kind.OP_HEIGHT;
import static cop5556sp17.Scanner.Kind.OP_WIDTH;
import static cop5556sp17.Scanner.Kind.PLUS;
import static cop5556sp17.Scanner.Kind.TIMES;
import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
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
import cop5556sp17.AST.Type;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

public class TypeCheckVisitor implements ASTVisitor {

	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception {
		TypeCheckException(String message) {
			super(message);
		}
	}

	SymbolTable symtab = new SymbolTable();

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg)
			throws Exception {
		// TODO Auto-generated method stub

		Chain chain = binaryChain.getE0();
		chain.visit(this, arg);
		ChainElem chainElem = binaryChain.getE1();
		chainElem.visit(this, arg);

		if (chain.getTypeField() == null || chainElem.getTypeField() == null)
			throw new TypeCheckException("Error " + chain.getTypeField() + " "
					+ chainElem.getTypeField());

		if (binaryChain.getArrow().isKind(ARROW)) {
			if (chain.getTypeField().equals(URL)
					&& chainElem.getTypeField().equals(IMAGE)) {
				binaryChain.setTypeField(IMAGE);
			} else if (chain.getTypeField().equals(FILE)
					&& chainElem.getTypeField().equals(IMAGE)) {
				binaryChain.setTypeField(IMAGE);
			} else if (chain.getTypeField().equals(FRAME)) {

				if (chainElem instanceof FrameOpChain
						&& (chainElem.getFirstToken().isKind(KW_XLOC) || chainElem
								.getFirstToken().isKind(KW_YLOC))) {

					binaryChain.setTypeField(INTEGER);

				} else if (chainElem instanceof FrameOpChain
						&& (chainElem.getFirstToken().isKind(KW_SHOW)
								|| chainElem.getFirstToken().isKind(KW_HIDE) || chainElem
								.getFirstToken().isKind(KW_MOVE))) {

					binaryChain.setTypeField(FRAME);

				}
			} else if (chain.getTypeField().equals(INTEGER)) {

				if (chainElem instanceof IdentChain
						&& chainElem.getTypeField().equals(INTEGER)) {
					binaryChain.setTypeField(INTEGER);
				}
			} else if (chain.getTypeField().equals(IMAGE)) {
				if (chainElem instanceof ImageOpChain
						&& (chainElem.getFirstToken().isKind(OP_WIDTH) || chainElem
								.getFirstToken().isKind(OP_HEIGHT))) {

					binaryChain.setTypeField(INTEGER);

				} else if (chainElem instanceof FilterOpChain
						&& (chainElem.getFirstToken().isKind(OP_GRAY)
								|| chainElem.getFirstToken()
										.isKind(OP_CONVOLVE) || chainElem
								.getFirstToken().isKind(OP_BLUR))) {

					binaryChain.setTypeField(IMAGE);
				} else if (chainElem instanceof IdentChain
						&& chainElem.getTypeField().equals(IMAGE)) {
					binaryChain.setTypeField(IMAGE);
				} else if (chainElem.getTypeField().equals(FRAME)) {
					binaryChain.setTypeField(FRAME);
				} else if (chainElem.getTypeField().equals(FILE)) {
					binaryChain.setTypeField(NONE);
				} else if (chainElem instanceof ImageOpChain
						&& chainElem.getFirstToken().isKind(KW_SCALE)) {
					binaryChain.setTypeField(IMAGE);
				} else if (chainElem instanceof IdentChain) {
					binaryChain.setTypeField(IMAGE);

				}

			} else
				throw new TypeCheckException("There is Bug in Parser");
		}

		else if (binaryChain.getArrow().isKind(BARARROW)) {
			if (chain.getTypeField().equals(IMAGE)) {

				if (chainElem instanceof FilterOpChain
						&& (chainElem.getFirstToken().isKind(OP_GRAY)
								|| chainElem.getFirstToken()
										.isKind(OP_CONVOLVE) || chainElem
								.getFirstToken().isKind(OP_BLUR))) {

					binaryChain.setTypeField(IMAGE);
				}
			}
		} else
			throw new TypeCheckException("There is Bug in Parser");

		return binaryChain.getTypeField();
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression,
			Object arg) throws Exception {
		// TODO Auto-generated method stub

		Expression expr1 = binaryExpression.getE0();
		expr1.visit(this, arg);
		Expression expr2 = binaryExpression.getE1();
		expr2.visit(this, arg);
		Token op = binaryExpression.getOp();

		TypeName expr1Type = expr1.getTypeField();
		TypeName expr2Type = expr2.getTypeField();

		if (expr1Type == null || expr2Type == null)
			throw new TypeCheckException("Error");

		if (op.isKind(PLUS) || op.isKind(MINUS)) {

			if (expr1Type.equals(INTEGER) && expr2Type.equals(INTEGER)) {
				binaryExpression.setTypeField(INTEGER);
			} else if (expr1Type.equals(IMAGE) && expr2Type.equals(IMAGE)) {
				binaryExpression.setTypeField(IMAGE);
			} else {
				throw new TypeCheckException(
						"PLUS/MINUS wrong operator for INTEGER or IMAGE.");
			}
		} else if (op.isKind(TIMES)) {
			if (expr1Type.equals(INTEGER) && expr2Type.equals(INTEGER)) {
				binaryExpression.setTypeField(INTEGER);
			} else if (expr1Type.equals(INTEGER) && expr2Type.equals(IMAGE)) {
				binaryExpression.setTypeField(IMAGE);
			} else if (expr1Type.equals(IMAGE) && expr2Type.equals(INTEGER)) {
				binaryExpression.setTypeField(IMAGE);
			} else {
				throw new TypeCheckException(
						"TIMES operated on invalid combination of INTEGER or IMAGE.");
			}
		} else if (op.isKind(DIV)) {
			if (expr1Type.equals(INTEGER) && expr2Type.equals(INTEGER)) {
				binaryExpression.setTypeField(INTEGER);
			} else if (expr1Type.equals(IMAGE) && expr2Type.equals(INTEGER)) {
				binaryExpression.setTypeField(IMAGE);
			} else {
				throw new TypeCheckException(
						"DIV operated on other than INTEGER.");
			}
		} else if (op.isKind(LE) || op.isKind(LT) || op.isKind(GE)
				|| op.isKind(GT)) {
			if (expr1Type.equals(INTEGER) && expr2Type.equals(INTEGER)) {
				binaryExpression.setTypeField(BOOLEAN);
			} else if (expr1Type.equals(BOOLEAN) && expr2Type.equals(BOOLEAN)) {
				binaryExpression.setTypeField(BOOLEAN);
			} else {
				throw new TypeCheckException(
						"LE/GE/GT/LT operated on other than INTEGER or BOOLEAN.");
			}

		} else if (op.isKind(EQUAL) || op.isKind(NOTEQUAL)) {
			if (expr1Type.equals(expr2Type)) {
				binaryExpression.setTypeField(BOOLEAN);
			} else {
				throw new TypeCheckException(
						"EQUAL/NOT EQUAL operated on expressions of different types.");
			}
		} else if (op.isKind(Kind.AND)) {
			if (expr1Type.equals(BOOLEAN) && expr2Type.equals(BOOLEAN)) {
				binaryExpression.setTypeField(BOOLEAN);
			} else {
				throw new TypeCheckException(
						"AND operated on other than BOOLEAN.");
			}
		} else if (op.isKind(Kind.OR)) {
			if (expr1Type.equals(BOOLEAN) && expr2Type.equals(BOOLEAN)) {
				binaryExpression.setTypeField(BOOLEAN);
			} else {
				throw new TypeCheckException(
						"OR operated on other than BOOLEAN.");
			}
		} else if (op.isKind(Kind.MOD)) {
			if (expr1Type.equals(INTEGER) && expr2Type.equals(INTEGER)) {
				binaryExpression.setTypeField(INTEGER);
			} else if (expr1Type.equals(IMAGE) && expr2Type.equals(INTEGER)) {
				binaryExpression.setTypeField(IMAGE);
			} else {
				throw new TypeCheckException(
						"MOD operated on other than BOOLEAN.");
			}
		}

		else
			throw new TypeCheckException("Invalid Operand");
		return binaryExpression.getTypeField();
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO Auto-generated method stub

		symtab.enterScope();

		for (Dec dec : block.getDecs())
			dec.visit(this, arg);

		for (Statement statement : block.getStatements())
			statement.visit(this, arg);

		symtab.leaveScope();

		return null;
	}

	@Override
	public Object visitBooleanLitExpression(
			BooleanLitExpression booleanLitExpression, Object arg)
			throws Exception {
		// TODO Auto-generated method stub

		booleanLitExpression.setTypeField(BOOLEAN);
		return booleanLitExpression.getTypeField();
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Tuple tuple = filterOpChain.getArg();
		tuple.visit(this, arg);

		if (tuple.getExprList().size() != 0)
			throw new TypeCheckException("Argument size "
					+ tuple.getExprList().size() + "expected " + "0");

		filterOpChain.setTypeField(IMAGE);

		return filterOpChain.getTypeField();
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg)
			throws Exception {
		// TODO Auto-generated method stub

		Token firstToken = frameOpChain.getFirstToken();
		Tuple tuple = frameOpChain.getArg();
		tuple.visit(this, arg);

		if (firstToken.isKind(KW_SHOW) || firstToken.isKind(KW_HIDE)) {

			if (tuple.getExprList().size() != 0)
				throw new TypeCheckException("saw Tuple.length :"
						+ tuple.getExprList().size() + "expected 0");

			frameOpChain.setTypeField(NONE);

		} else if (firstToken.isKind(KW_XLOC) || firstToken.isKind(KW_YLOC)) {

			if (tuple.getExprList().size() != 0)
				throw new TypeCheckException("saw Tuple.length :"
						+ tuple.getExprList().size() + "expected 0");

			frameOpChain.setTypeField(INTEGER);

		} else if (firstToken.isKind(KW_MOVE)) {

			if (tuple.getExprList().size() != 2)
				throw new TypeCheckException("saw Tuple.length :"
						+ tuple.getExprList().size() + "expected 0");

			frameOpChain.setTypeField(NONE);

		} else
			throw new TypeCheckException("Argument length "
					+ tuple.getExprList().size() + "expected 0 or 2");

		return frameOpChain.getTypeField();
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg)
			throws Exception {
		// TODO Auto-generated method stub

		Token identValue = identChain.getFirstToken();
		if (symtab.lookup(identValue.getText()) == null)
			throw new TypeCheckException(identValue.getText() + " not declared");

		TypeName identType = Type.getTypeName(symtab.lookup(
				identValue.getText()).getType());
		identChain.setTypeField(identType);
		identChain.setDec(symtab.lookup(identValue.getText()));

		return identChain.getTypeField();
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression,
			Object arg) throws Exception {
		// TODO Auto-generated method stub

		Token identValue = identExpression.getFirstToken();
		if (symtab.lookup(identValue.getText()) == null)
			throw new TypeCheckException(identValue.getText() + " not declared");

		TypeName identType = Type.getTypeName(symtab.lookup(
				identValue.getText()).getType());
		identExpression.setTypeField(identType);
		identExpression.setDec(symtab.lookup(identValue.getText()));

		return identExpression.getTypeField();
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg)
			throws Exception {
		// TODO Auto-generated method stub

		Expression expr = ifStatement.getE();
		expr.visit(this, arg);

		Block block = ifStatement.getB();
		block.visit(this, arg);
		if (ifStatement.getE().getTypeField() != (BOOLEAN))
			throw new TypeCheckException("Saw "
					+ ifStatement.getE().getTypeField() + "expected " + BOOLEAN);

		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression,
			Object arg) throws Exception {
		// TODO Auto-generated method stub

		intLitExpression.setTypeField(INTEGER);
		return intLitExpression.getTypeField();
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg)
			throws Exception {
		// TODO Auto-generated method stub

		Expression expr = sleepStatement.getE();

		expr.visit(this, arg);

		if (sleepStatement.getE().getTypeField() != INTEGER)
			throw new TypeCheckException("Saw "
					+ sleepStatement.getE().getTypeField() + "expected "
					+ INTEGER);

		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg)
			throws Exception {
		// TODO Auto-generated method stub

		Expression expr = whileStatement.getE();
		expr.visit(this, arg);

		Block block = whileStatement.getB();
		block.visit(this, arg);

		if (whileStatement.getE().getTypeField() != BOOLEAN)
			throw new TypeCheckException("Saw "
					+ whileStatement.getE().getTypeField() + "expected "
					+ BOOLEAN);
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		// TODO Auto-generated method stub

		declaration.setTypeField(Type.getTypeName(declaration.getFirstToken()));
		boolean isSuccess = symtab.insert(declaration.getIdent().getText(),
				declaration);

		if (!isSuccess)
			throw new TypeCheckException("Redeclared : "
					+ declaration.getIdent().getText());
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		symtab.enterScope();

		for (int i = 0; i < program.getParams().size(); i++) {
			ParamDec paramDec = program.getParams().get(i);
			paramDec.visit(this, arg);
		}
		visitBlock(program.getB(), program.getB().getDecs());
		
		symtab.leaveScope();

		return null;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		IdentLValue identLValue = assignStatement.getVar();
		identLValue.visit(this, arg);

		Expression expr = assignStatement.getE();
		expr.visit(this, arg);

		if (expr.getTypeField() != identLValue.getTypeField())
			throw new TypeCheckException("Type Mismatch " + expr.getTypeField()
					+ " and " + identLValue.getTypeField());

		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg)
			throws Exception {
		// TODO Auto-generated method stub

		Token identValue = identX.getFirstToken();
		if (symtab.lookup(identValue.getText()) == null)
			throw new TypeCheckException(identValue.getText() + " not declared");

		TypeName identType = Type.getTypeName(symtab.lookup(
				identValue.getText()).getType());

		identX.setTypeField(identType);
		identX.setDec(symtab.lookup(identValue.getText()));

		return identType;
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		// TODO Auto-generated method stub

		paramDec.setTypeField(Type.getTypeName(paramDec.getFirstToken()));
		boolean isSuccess = symtab.insert(paramDec.getIdent().getText(),
				paramDec);
		if (!isSuccess)
			throw new TypeCheckException("Redeclared : "
					+ paramDec.getIdent().getText());

		return null;
	}

	@Override
	public Object visitConstantExpression(
			ConstantExpression constantExpression, Object arg) {
		// TODO Auto-generated method stub
		constantExpression.setTypeField(INTEGER);
		return constantExpression.getTypeField();
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg)
			throws Exception {
		// TODO Auto-generated method stub

		Token firstToken = imageOpChain.getFirstToken();
		Tuple tuple = imageOpChain.getArg();

		tuple.visit(this, arg);

		if (firstToken.isKind(OP_WIDTH) || firstToken.isKind(OP_HEIGHT)) {

			if (tuple.getExprList().size() == 0)
				imageOpChain.setTypeField(INTEGER);

		} else if (firstToken.isKind(KW_SCALE)) {

			if (tuple.getExprList().size() == 1)
				imageOpChain.setTypeField(IMAGE);
		}

		return imageOpChain.getTypeField();
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		// TODO Auto-generated method stub

		for (Expression expr : tuple.getExprList()) {

			expr.visit(this, arg);
			if (!(expr.getTypeField().equals(INTEGER)))
				throw new TypeCheckException("saw type  " + expr.getTypeField()
						+ "expected " + INTEGER.toString());

		}

		return null;
	}

}
