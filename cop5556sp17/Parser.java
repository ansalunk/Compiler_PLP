package cop5556sp17;

import static cop5556sp17.Scanner.Kind.AND;
import static cop5556sp17.Scanner.Kind.ARROW;
import static cop5556sp17.Scanner.Kind.ASSIGN;
import static cop5556sp17.Scanner.Kind.BARARROW;
import static cop5556sp17.Scanner.Kind.COMMA;
import static cop5556sp17.Scanner.Kind.DIV;
import static cop5556sp17.Scanner.Kind.EOF;
import static cop5556sp17.Scanner.Kind.EQUAL;
import static cop5556sp17.Scanner.Kind.GE;
import static cop5556sp17.Scanner.Kind.GT;
import static cop5556sp17.Scanner.Kind.IDENT;
import static cop5556sp17.Scanner.Kind.KW_BOOLEAN;
import static cop5556sp17.Scanner.Kind.KW_FILE;
import static cop5556sp17.Scanner.Kind.KW_FRAME;
import static cop5556sp17.Scanner.Kind.KW_HIDE;
import static cop5556sp17.Scanner.Kind.KW_IF;
import static cop5556sp17.Scanner.Kind.KW_IMAGE;
import static cop5556sp17.Scanner.Kind.KW_INTEGER;
import static cop5556sp17.Scanner.Kind.KW_MOVE;
import static cop5556sp17.Scanner.Kind.KW_SCALE;
import static cop5556sp17.Scanner.Kind.KW_SHOW;
import static cop5556sp17.Scanner.Kind.KW_URL;
import static cop5556sp17.Scanner.Kind.KW_WHILE;
import static cop5556sp17.Scanner.Kind.KW_XLOC;
import static cop5556sp17.Scanner.Kind.KW_YLOC;
import static cop5556sp17.Scanner.Kind.LBRACE;
import static cop5556sp17.Scanner.Kind.LE;
import static cop5556sp17.Scanner.Kind.LPAREN;
import static cop5556sp17.Scanner.Kind.LT;
import static cop5556sp17.Scanner.Kind.MINUS;
import static cop5556sp17.Scanner.Kind.MOD;
import static cop5556sp17.Scanner.Kind.NOTEQUAL;
import static cop5556sp17.Scanner.Kind.OP_BLUR;
import static cop5556sp17.Scanner.Kind.OP_CONVOLVE;
import static cop5556sp17.Scanner.Kind.OP_GRAY;
import static cop5556sp17.Scanner.Kind.OP_HEIGHT;
import static cop5556sp17.Scanner.Kind.OP_SLEEP;
import static cop5556sp17.Scanner.Kind.OP_WIDTH;
import static cop5556sp17.Scanner.Kind.OR;
import static cop5556sp17.Scanner.Kind.PLUS;
import static cop5556sp17.Scanner.Kind.RBRACE;
import static cop5556sp17.Scanner.Kind.RPAREN;
import static cop5556sp17.Scanner.Kind.SEMI;
import static cop5556sp17.Scanner.Kind.TIMES;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.ASTNode;
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
import cop5556sp17.AST.WhileStatement;

public class Parser {

	/**
	 * Exception to be thrown if a syntax error is detected in the input. You
	 * will want to provide a useful error message.
	 *
	 */
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		public SyntaxException(String message) {
			super(message);
		}
	}

	/**
	 * Useful during development to ensure unimplemented routines are not
	 * accidentally called during development. Delete it when the Parser is
	 * finished.
	 *
	 */
	@SuppressWarnings("serial")
	public static class UnimplementedFeatureException extends RuntimeException {
		public UnimplementedFeatureException() {
			super();
		}
	}

	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	private static List<Kind> firstParamDec = new ArrayList<Scanner.Kind>(
			Arrays.asList(KW_BOOLEAN, KW_URL, KW_INTEGER, KW_FILE));
	private static List<Kind> fDec = new ArrayList<>(Arrays.asList(
			Kind.KW_INTEGER, KW_BOOLEAN, KW_IMAGE, KW_FRAME));
	private static List<Kind> setArrowOps = new ArrayList<>(Arrays.asList(
			ARROW, BARARROW));
	private static List<Kind> fRelOps = new ArrayList<>(Arrays.asList(LT, LE,
			GE, GT, EQUAL, NOTEQUAL));
	private static List<Kind> fWeakOps = new ArrayList<>(Arrays.asList(PLUS,
			MINUS, OR));
	private static List<Kind> fStrongOps = new ArrayList<>(Arrays.asList(TIMES,
			DIV, AND, MOD));
	private static List<Kind> fFilterOps = new ArrayList<>(Arrays.asList(
			OP_GRAY, OP_BLUR, OP_CONVOLVE));
	private static List<Kind> fFrameOps = new ArrayList<>(Arrays.asList(
			KW_MOVE, KW_SHOW, KW_XLOC, KW_HIDE, KW_YLOC));
	private static List<Kind> fImageOps = new ArrayList<>(Arrays.asList(
			KW_SCALE, OP_WIDTH, OP_HEIGHT));

	/**
	 * parse the input using tokens from the scanner. Check for EOF (i.e. no
	 * trailing junk) when finished
	 * 
	 * @return
	 * 
	 * @throws SyntaxException
	 */
	ASTNode parse() throws SyntaxException {

		ArrayList<Kind> firstDec = new ArrayList<Kind>();

		ASTNode p = program();
		matchEOF();
		return p;
	}

	Expression expression() throws SyntaxException {

		Token firstExprToken = t;
		Expression e0 = null;
		Expression e1 = null;

		e0 = term();

		while (fRelOps.contains(t.kind)) {
			Token op = t;
			consume();
			e1 = term();
			e0 = new BinaryExpression(firstExprToken, e0, op, e1);
		}

		return e0;
		// throw new UnimplementedFeatureException();
	}

	Expression term() throws SyntaxException {
		Expression e0 = null;
		Expression e1 = null;
		Token firstTermToken = t;
		e0 = elem();
		while (fWeakOps.contains(t.kind)) {
			Token op = t;
			consume();
			e1 = elem();
			e0 = new BinaryExpression(firstTermToken, e0, op, e1);
		}
		return e0;// throw new UnimplementedFeatureException();
	}

	Expression elem() throws SyntaxException {
		// TODO
		Expression e0 = null;
		Expression e1 = null;
		Token firstElemToken = t;
		e0 = factor();
		while (fStrongOps.contains(t.kind)) {
			Token op = t;
			consume();
			e1 = factor();
			e0 = new BinaryExpression(firstElemToken, e0, op, e1);

		}

		return e0;
		// throw new UnimplementedFeatureException();
	}

	Expression factor() throws SyntaxException {
		Kind kind = t.kind;

		Expression e = null;
		Token firstFactorToken = t;
		switch (kind) {
		case IDENT: {
			e = new IdentExpression(firstFactorToken);
			consume();
		}
			break;
		case INT_LIT: {
			e = new IntLitExpression(firstFactorToken);
			consume();
		}
			break;
		case KW_TRUE:
		case KW_FALSE: {
			e = new BooleanLitExpression(firstFactorToken);
			consume();
		}
			break;
		case KW_SCREENWIDTH:
		case KW_SCREENHEIGHT: {
			e = new ConstantExpression(firstFactorToken);
			consume();
		}
			break;
		case LPAREN: {

			consume();
			e = expression();
			match(RPAREN);
		}
			break;
		default:
			// you will want to provide a more useful error message
			throw new SyntaxException("illegal factor");
		}

		return e;
	}

	Block block() throws SyntaxException {

		ArrayList<Dec> decs = new ArrayList<Dec>();
		ArrayList<Statement> statements = new ArrayList<Statement>();
		Block b;
		Token firstBlockToken = t;

		if (t.isKind(LBRACE)) {

			consume();

			while (fImageOps.contains(t.kind) || fDec.contains(t.kind)
					|| t.isKind(OP_SLEEP) || t.isKind(KW_WHILE)
					|| t.isKind(IDENT) || fFrameOps.contains(t.kind)
					|| t.isKind(KW_IF) || fFilterOps.contains(t.kind)) {

				if (fImageOps.contains(t.kind) || t.isKind(KW_WHILE)
						|| t.isKind(KW_IF) || t.isKind(IDENT)
						|| fFilterOps.contains(t.kind) || t.isKind(OP_SLEEP)
						|| fFrameOps.contains(t.kind)) {
					statements.add(statement());
				}

				else {
					decs.add(dec());
				}
			}

			b = new Block(firstBlockToken, decs, statements);
			match(RBRACE);
		} else
			throw new SyntaxException("saw " + t.kind + "expected LBRACE");

		return b;
	}

	Program program() throws SyntaxException {

		Program p = null;
		Token firstProgramToken = t;
		if (t.isKind(Kind.IDENT)) {

			consume();
			List<ParamDec> listParamDec = new ArrayList<ParamDec>();
			Block b = null;

			if (t.isKind(Kind.LBRACE)) {
				b = block();
				//listParamDec.add(null);
			} else {

				listParamDec.add(paramDec());
				while (t.isKind(COMMA)) {
					consume();
					listParamDec.add(paramDec());
				}
				b = block();

			}
			p = new Program(firstProgramToken,
					(ArrayList<ParamDec>) listParamDec, b);

		} else
			throw new SyntaxException("saw " + t.kind + " expected IDENT");

		return p;
	}

	ParamDec paramDec() throws SyntaxException {
		// TODO

		ParamDec d = null;
		Token firstParamDecToken = t;
		if (firstParamDec.contains(t.kind)) {
			consume();
			d = new ParamDec(firstParamDecToken, t);
			match(Kind.IDENT);
		} else
			throw new SyntaxException("saw " + t.kind
					+ "expected type of ParamDec");

		return d;
	}

	Dec dec() throws SyntaxException {
		Dec d = null;
		Token firstDecToken = t;
		if (fDec.contains(t.kind)) {

			consume();
			d = new Dec(firstDecToken, t);
			match(Kind.IDENT);
		} else
			throw new SyntaxException("saw " + t.kind + "expected type of Dec");

		return d;
	}

	Statement statement() throws SyntaxException {
		// TODO
		Statement s = null;
		Token firstStatementToken = t;
		if (t.isKind(OP_SLEEP)) {

			consume();
			Expression e1 = expression();
			s = new SleepStatement(firstStatementToken, e1);
			match(SEMI);
		} else if (t.isKind(KW_WHILE)) {
			consume();
			Expression e = null;
			Block b = null;
			if (t.isKind(LPAREN)) {
				consume();
			}

			e = expression();

			if (t.isKind(RPAREN)) {
				consume();

			}
			b = block();

			s = new WhileStatement(firstStatementToken, e, b);

		} else if (t.isKind(KW_IF)) {

			consume();
			if (t.isKind(LPAREN)) {

				consume();
			}

			Expression e = null;
			Block b = null;
			e = expression();

			if (t.isKind(RPAREN)) {
				consume();
			}

			b = block();

			s = new IfStatement(firstStatementToken, e, b);
		}

		else {
			Token lookAheadToken = scanner.peek();

			if (t.isKind(IDENT) && lookAheadToken.isKind(ASSIGN)) {
				IdentLValue i1 = new IdentLValue(firstStatementToken);
				Expression e1 = null;
				match(IDENT);
				match(ASSIGN);
				e1 = expression();
				s = new AssignmentStatement(firstStatementToken, i1, e1);
				match(SEMI);

			} else if (fFilterOps.contains(t.kind) || t.isKind(IDENT)
					|| fFrameOps.contains(t.kind) || fImageOps.contains(t.kind)) {

				Chain c1 = null;

				c1 = chain();
				s = c1;
				match(SEMI);
			}

			else
				throw new SyntaxException("saw" + t.kind
						+ " expected of FilterOps ");
		}

		return s;
	}

	Chain chain() throws SyntaxException {
		// TODO
		Chain c1 = null;
		c1 = chainElem();
		Token op1, op2;
		Token firstChainToken = t;
		if (setArrowOps.contains(t.kind)) {
			op1 = t;
			consume();
		} else
			throw new SyntaxException("saw " + t.kind
					+ " expected of type ArrowOps");

		ChainElem c2 = chainElem();
		c1 = new BinaryChain(firstChainToken, c1, op1, c2);

		while (t.isKind(ARROW) || t.isKind(BARARROW)) {
			op2 = t;
			consume();
			ChainElem c3 = chainElem();
			c1 = new BinaryChain(firstChainToken, c1, op2, c3);
		}

		return c1;
	}

	ChainElem chainElem() throws SyntaxException {
		// TODO
		ChainElem ce1 = null;
		Token firstChainElemToken = t;
		if (t.isKind(IDENT)) {
			ce1 = new IdentChain(firstChainElemToken);
			consume();
		} else if (fFilterOps.contains(t.kind)) {
			consume();
			Tuple tp1 = arg();
			ce1 = new FilterOpChain(firstChainElemToken, tp1);
		} else if (fFrameOps.contains(t.kind)) {
			consume();
			Tuple tp1 = arg();
			ce1 = new FrameOpChain(firstChainElemToken, tp1);

		} else if (fImageOps.contains(t.kind)) {
			consume();
			Tuple tp1 = arg();
			ce1 = new ImageOpChain(firstChainElemToken, tp1);
		}

		else
			throw new SyntaxException("Error");
		return ce1;
	}

	Tuple arg() throws SyntaxException {
		// TODO
		List<Expression> argList = new ArrayList<Expression>();
		Tuple arg = null;
		Token firstArgToken = t;

		if (t.isKind(LPAREN)) {
			consume();
			argList.add(expression());
			while (t.isKind(Kind.COMMA)) {
				consume();
				argList.add(expression());
			}

			arg = new Tuple(firstArgToken, argList);
			match(RPAREN);
		} else {

			return new Tuple(firstArgToken, argList);

		}

		return arg;

	}

	/**
	 * Checks whether the current token is the EOF token. If not, a
	 * SyntaxException is thrown.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.isKind(EOF)) {
			return t;
		}
		throw new SyntaxException("expected EOF");
	}

	/**
	 * Checks if the current token has the given kind. If so, the current token
	 * is consumed and returned. If not, a SyntaxException is thrown.
	 * 
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		if (t.isKind(kind)) {
			return consume();
		}
		throw new SyntaxException("saw " + t.kind + "expected " + kind);
	}

	/**
	 * Checks if the current token has one of the given kinds. If so, the
	 * current token is consumed and returned. If not, a SyntaxException is
	 * thrown.
	 * 
	 * * Precondition: for all given kinds, kind != EOF
	 * 
	 * @param kinds
	 *            list of kinds, matches any one
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind... kinds) throws SyntaxException {
		// TODO. Optional but handy
		return null; // replace this statement
	}

	/**
	 * Gets the next token and returns the consumed token.
	 * 
	 * Precondition: t.kind != EOF
	 * 
	 * @return
	 * 
	 */
	private Token consume() throws SyntaxException {
		Token tmp = t;
		t = scanner.nextToken();
		return tmp;
	}

}
