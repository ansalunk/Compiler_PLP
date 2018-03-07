package cop5556sp17.AST;

import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.Type.TypeName;


public abstract class Chain extends Statement {
	
	TypeName typeField;
	
	public Chain(Token firstToken) {
		super(firstToken);
	}

	public TypeName getTypeField() {
		return typeField;
	}

	public void setTypeField(TypeName typeField) {
		this.typeField = typeField;
	}

	
}
