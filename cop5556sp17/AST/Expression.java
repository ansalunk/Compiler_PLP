package cop5556sp17.AST;

import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.Type.TypeName;

public abstract class Expression extends ASTNode {
	
	public TypeName typeField;
	
	
	protected Expression(Token firstToken) {
		super(firstToken);
	}

	@Override
	abstract public Object visit(ASTVisitor v, Object arg) throws Exception;

	public TypeName getTypeField() {
		return typeField;
	}

	public void setTypeField(TypeName typeField) {
		this.typeField = typeField;
	}

}
