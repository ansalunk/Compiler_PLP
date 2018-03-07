package cop5556sp17;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Scanner {

	static HashMap<Kind, String> keywordDictionary;

	/**
	 * Static Block to store Keywords
	 */
	static {

		keywordDictionary = new HashMap<Kind, String>();

		keywordDictionary.put(Kind.KW_IF, "if");
		keywordDictionary.put(Kind.KW_WHILE, "while");
		keywordDictionary.put(Kind.KW_BOOLEAN, "boolean");
		keywordDictionary.put(Kind.KW_FRAME, "frame");
		keywordDictionary.put(Kind.KW_IMAGE, "image");
		keywordDictionary.put(Kind.KW_INTEGER, "integer");
		keywordDictionary.put(Kind.KW_SHOW, "show");
		keywordDictionary.put(Kind.KW_SCALE, "scale");
		keywordDictionary.put(Kind.KW_HIDE, "hide");
		keywordDictionary.put(Kind.KW_MOVE, "move");
		keywordDictionary.put(Kind.KW_FILE, "file");
		keywordDictionary.put(Kind.KW_URL, "url");
		keywordDictionary.put(Kind.KW_TRUE, "true");
		keywordDictionary.put(Kind.KW_FALSE, "false");
		keywordDictionary.put(Kind.KW_SCREENHEIGHT, "screenheight");
		keywordDictionary.put(Kind.KW_SCREENWIDTH, "screenwidth");
		keywordDictionary.put(Kind.KW_XLOC, "xloc");
		keywordDictionary.put(Kind.KW_YLOC, "yloc");
		keywordDictionary.put(Kind.OP_HEIGHT, "height");
		keywordDictionary.put(Kind.OP_WIDTH, "width");
		keywordDictionary.put(Kind.OP_BLUR, "blur");
		keywordDictionary.put(Kind.OP_GRAY, "gray");
		keywordDictionary.put(Kind.OP_CONVOLVE, "convolve");
		keywordDictionary.put(Kind.OP_SLEEP, "sleep");
	}

	/**
	 * Kind enum
	 */

	public static enum Kind {
		IDENT(""), INT_LIT(""), KW_INTEGER("integer"), KW_BOOLEAN("boolean"), KW_IMAGE(
				"image"), KW_URL("url"), KW_FILE("file"), KW_FRAME("frame"), KW_WHILE(
				"while"), KW_IF("if"), KW_TRUE("true"), KW_FALSE("false"), SEMI(
				";"), COMMA(","), LPAREN("("), RPAREN(")"), LBRACE("{"), RBRACE(
				"}"), ARROW("->"), BARARROW("|->"), OR("|"), AND("&"), EQUAL(
				"=="), NOTEQUAL("!="), LT("<"), GT(">"), LE("<="), GE(">="), PLUS(
				"+"), MINUS("-"), TIMES("*"), DIV("/"), MOD("%"), NOT("!"), ASSIGN(
				"<-"), OP_BLUR("blur"), OP_GRAY("gray"), OP_CONVOLVE("convolve"), KW_SCREENHEIGHT(
				"screenheight"), KW_SCREENWIDTH("screenwidth"), OP_WIDTH(
				"width"), OP_HEIGHT("height"), KW_XLOC("xloc"), KW_YLOC("yloc"), KW_HIDE(
				"hide"), KW_SHOW("show"), KW_MOVE("move"), OP_SLEEP("sleep"), KW_SCALE(
				"scale"), EOF("eof");

		Kind(String text) {
			this.text = text;
		}

		final String text;

		String getText() {
			return text;
		}
	}

	/**
	 * Method to get the Key from the corresponding Value of Hashmap
	 */
	public static Object getKeyFromValue(Map hm, Object value) {
		for (Object o : hm.keySet()) {
			if (hm.get(o).equals(value)) {
				return o;
			}
		}
		return null;
	}

	/**
	 * Thrown by Scanner when an illegal character is encountered
	 */
	@SuppressWarnings("serial")
	public static class IllegalCharException extends Exception {
		public IllegalCharException(String message) {
			super(message);
		}
	}

	/**
	 * Thrown by Scanner when an int literal is not a value that can be
	 * represented by an int.
	 */
	@SuppressWarnings("serial")
	public static class IllegalNumberException extends Exception {
		public IllegalNumberException(String message) {
			super(message);
		}
	}

	/**
	 * Holds the line and position in the line of a token.
	 */
	static class LinePos {
		public final int line;
		public final int posInLine;

		public LinePos(int line, int posInLine) {
			super();
			this.line = line;
			this.posInLine = posInLine;
		}

		@Override
		public String toString() {
			return "LinePos [line=" + line + ", posInLine=" + posInLine + "]";
		}
	}

	/**
	 * Token class : Holds kind, position in the input and length of the token
	 */
	public class Token {
		public final Kind kind;
		public final int pos; // position in input array
		public final int length;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((kind == null) ? 0 : kind.hashCode());
			result = prime * result + length;
			result = prime * result + pos;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof Token)) {
				return false;
			}
			Token other = (Token) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			if (kind != other.kind) {
				return false;
			}
			if (length != other.length) {
				return false;
			}
			if (pos != other.pos) {
				return false;
			}
			return true;
		}

		private Scanner getOuterType() {
			return Scanner.this;
		}

		// returns the text of this Token
		public String getText() {
			// TODO IMPLEMENT THIS
			return chars.substring(pos, pos + length);
		}

		// returns a LinePos object representing the line and column of this
		// Token
		LinePos getLinePos() {
			// TODO IMPLEMENT THIS

			int lineNumber = java.util.Arrays.binarySearch(linePos.toArray(),
					pos);
			if (lineNumber < 0) {
				lineNumber = Math.abs(lineNumber);
				lineNumber -= 2;
			}

			int posInLine = pos - linePos.get(lineNumber);

			return new LinePos(lineNumber, posInLine);

		}

		Token(Kind kind, int pos, int length) {
			this.kind = kind;
			this.pos = pos;
			this.length = length;
		}

		/**
		 * Precondition: kind = Kind.INT_LIT, the text can be represented with a
		 * Java int. Note that the validity of the input should have been
		 * checked when the Token was created. So the exception should never be
		 * thrown.
		 * 
		 * @return int value of this token, which should represent an INT_LIT
		 * @throws NumberFormatException
		 */
		public int intVal() throws NumberFormatException {
			// TODO IMPLEMENT THIS
			int value = Integer.parseInt(getText());
			return value;

		}

		public boolean isKind(Kind kind) {
			// TODO Auto-generated method stub

			return (kind == this.kind);
		}

	}

	Scanner(String chars) {
		this.chars = chars;
		tokens = new ArrayList<Token>();

	}

	/**
	 * Initializes Scanner object by traversing chars and adding tokens to
	 * tokens list.
	 * 
	 * @return this scanner
	 * @throws IllegalCharException
	 * @throws IllegalNumberException
	 */
	public Scanner scan() throws IllegalCharException, IllegalNumberException {
		int pos = 0;
		// TODO IMPLEMENT THIS!!!!

		int length = chars.length();
		State state = State.START;
		int startPos = 0;

		int ch;

		linePos.add(0);

		while (pos <= length) {

			ch = pos < length ? chars.charAt(pos) : -1;

			switch (state) {

			case START: {

				pos = skipWhiteSpace(pos);
				ch = pos < length ? chars.charAt(pos) : -1;
				startPos = pos;

				switch (ch) {

				case -1: {
					tokens.add(new Token(Kind.EOF, pos, 0));
					pos++;
				}
					break;

				/*
				 * case ' ': { pos++; } break;
				 */
				case '\n': {
					pos++;
				}
					break;

				case '\r': {
					pos++;
				}

					break;

				case '(': {
					tokens.add(new Token(Kind.LPAREN, startPos, 1));
					pos++;
				}
					break;

				case ')': {
					tokens.add(new Token(Kind.RPAREN, startPos, 1));
					pos++;
				}
					break;

				case '{': {
					tokens.add(new Token(Kind.LBRACE, startPos, 1));
					pos++;
				}
					break;

				case '}': {
					tokens.add(new Token(Kind.RBRACE, startPos, 1));
					pos++;
				}
					break;

				case ';': {
					tokens.add(new Token(Kind.SEMI, startPos, 1));
					pos++;
				}
					break;

				case ',': {
					tokens.add(new Token(Kind.COMMA, startPos, 1));
					pos++;
				}
					break;

				case '+': {
					tokens.add(new Token(Kind.PLUS, startPos, 1));
					pos++;
				}

					break;
				case '-': {
					pos++;
					state = State.AFTER_MINUS;
				}

					break;
				case '*': {
					tokens.add(new Token(Kind.TIMES, startPos, 1));
					pos++;
				}

					break;
				case '/': {
					state = State.AFTER_DIV;
					pos++;
				}
					;
					break;
				case '%': {
					tokens.add(new Token(Kind.MOD, startPos, 1));
					pos++;
				}
					;
					break;
				case '|': {
					state = State.AFTER_OR;
					pos++;
				}
					;
					break;
				case '&': {
					tokens.add(new Token(Kind.AND, startPos, 1));
					pos++;
				}
					;
					break;
				case '>': {
					state = State.AFTER_GT;
					pos++;
				}
					;
					break;
				case '<': {
					state = State.AFTER_LT;
					pos++;
				}
					;
					break;
				case '!': {
					state = State.AFTER_NOT;
					pos++;
				}
					;
					break;
				case '=': {
					state = State.AFTER_EQ;
					pos++;
				}
					;
					break;
				case '0': {
					tokens.add(new Token(Kind.INT_LIT, startPos, 1));
					pos++;
				}
					;
					break;
				default: {
					if (Character.isDigit(ch)) {
						state = State.IN_DIGIT;
						pos++;
					} else if (Character.isJavaIdentifierStart(ch)) {
						state = State.IN_IDENT;
						pos++;
					} else {

						Character errorString = new Character(chars.charAt(pos));

						String error = new String(errorString.toString());
						throw new IllegalCharException(error
								+ ": is an illegal character");
					}

				}
				}
			}
				break;

			case IN_DIGIT: {

				if (Character.isDigit(ch)) {
					pos++;

				} else {

					try {
						Token t = new Token(Kind.INT_LIT, startPos, pos
								- startPos);
						int integerValue = t.intVal();

						tokens.add(t);
						state = State.START;
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						throw new IllegalNumberException("Error");
					}
				}

			}
				break;

			case IN_IDENT: {
				if (Character.isJavaIdentifierPart(ch)) {
					pos++;
				} else {

					String keywordString = (String) chars.subSequence(startPos,
							pos);

					Kind kind = (Kind) getKeyFromValue(keywordDictionary,
							keywordString);

					if (keywordDictionary.containsKey(kind)) {
						tokens.add(new Token(kind, startPos, pos - startPos));
						state = State.START;
						break;
					} else {
						tokens.add(new Token(Kind.IDENT, startPos, pos
								- startPos));
						state = State.START;
						break;
					}
				}
			}
				break;

			case AFTER_EQ: {

				switch (ch) {
				case '=': {
					tokens.add(new Token(Kind.EQUAL, startPos, 2));
					pos++;
					state = state.START;
				}
					break;

				default: {
					throw new IllegalCharException("Error");

				}
				}
			}
				break;

			case AFTER_NOT: {

				switch (ch) {
				case '=': {
					tokens.add(new Token(Kind.NOTEQUAL, startPos, 2));
					pos++;
					state = State.START;
				}
					break;
				default: {
					tokens.add(new Token(Kind.NOT, startPos, 1));
					state = State.START;

				}
				}
			}
				break;

			case AFTER_MINUS: {

				switch (ch) {
				case '>': {
					tokens.add(new Token(Kind.ARROW, startPos, 2));
					pos++;
					state = State.START;
				}
					break;
				default: {
					tokens.add(new Token(Kind.MINUS, startPos, 1));
					state = State.START;
				}
				}
			}
				break;

			case AFTER_OR: {

				if (chars.subSequence(pos,
						(pos + 2) < chars.length() ? pos + 2 : chars.length())
						.equals("->")) {
					tokens.add(new Token(Kind.BARARROW, startPos, 3));
					pos += 2;
					state = State.START;
				} else {
					tokens.add(new Token(Kind.OR, startPos, 1));
					state = State.START;
				}
			}
				break;

			case AFTER_GT: {

				switch (ch) {
				case '=': {
					tokens.add(new Token(Kind.GE, startPos, 2));
					pos++;
					state = State.START;
				}
					break;
				default: {
					tokens.add(new Token(Kind.GT, startPos, 1));
					state = State.START;
				}
				}
			}
				break;

			case AFTER_LT: {

				switch (ch) {
				case '=': {
					tokens.add(new Token(Kind.LE, startPos, 2));
					pos++;
					state = State.START;
				}
					break;

				case '-': {
					tokens.add(new Token(Kind.ASSIGN, startPos, 2));
					pos++;
					state = State.START;
				}
					break;

				default: {
					tokens.add(new Token(Kind.LT, startPos, 1));
					state = State.START;
				}
				}
			}
				break;

			case AFTER_DIV: {

				switch (ch) {
				case '*': {

					pos++;
					while (pos < chars.length()) {
						if (chars.charAt(pos) == '/'
								&& chars.charAt(pos - 1) == '*') {
							break;
						} else if (chars.charAt(pos) == '\n') {
							linePos.add(pos + 1);
						}
						pos++;
					}

					if (pos >= chars.length()) {
						tokens.add(new Token(Kind.EOF, startPos, 1));
						break;
					}

					else {
						state = State.START;
						pos++;
					}

				}
					break;
				default: {
					tokens.add(new Token(Kind.DIV, startPos, 1));
					state = State.START;
				}
				}
			}
				break;

			}
		}

		return this;
	}

	final ArrayList<Token> tokens;
	final String chars;
	int tokenNum;
	ArrayList<Integer> linePos = new ArrayList<Integer>();

	/*
	 * Return the next token in the token list and update the state so that the
	 * next call will return the Token..
	 */
	public Token nextToken() {
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum++);
	}

	/*
	 * Return the next token in the token list without updating the state. (So
	 * the following call to next will return the same token.)
	 */
	public Token peek() {

		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum);
	}

	/**
	 * Returns a LinePos object containing the line and position in line of the
	 * given token.
	 * 
	 * Line numbers start counting at 0
	 * 
	 * @param t
	 * @return
	 */
	public LinePos getLinePos(Token t) {
		// TODO IMPLEMENT THIS

		return t.getLinePos();
	}

	/**
	 * Enum State maintains the state of each step while scanning
	 */

	public static enum State {

		START("START"), AFTER_EQ("AFTER_EQ"), IN_DIGIT("IN_DIGIT"), IN_IDENT(
				"IN_IDENT"), AFTER_NOT("AFTER_NOT"), AFTER_MINUS("AFTER_MINUS"), AFTER_OR(
				"AFTER_OR"), AFTER_LT("AFTER_LT"), AFTER_GT("AFTER_GT"), AFTER_DIV(
				"AFTER_DIV"), COMMENT("COMMENT");

		State(String text) {
			this.text = text;
		}

		final String text;

		String getText() {
			return text;
		}

	}

	/**
	 * Skips the White spaces in the input while scanning
	 */

	private int skipWhiteSpace(int pos) {

		String str = this.chars;

		while (pos < str.length()) {

			char ch = str.charAt(pos);

			if (!(Character.isWhitespace(ch)))
				break;
			else {
				if (ch == '\n')
					linePos.add(pos + 1);
			}
			pos++;

		}

		return pos;
	}

}
