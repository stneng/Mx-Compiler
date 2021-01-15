package AST;

import Util.position;

public class TypeNode extends ASTNode {
    public String simpleType;
    public int dim;

    public TypeNode(position pos, String simpleType, int dim) {
        super(pos);
        this.simpleType = simpleType;
        this.dim = dim;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
