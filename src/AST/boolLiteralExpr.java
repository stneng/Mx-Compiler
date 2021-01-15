package AST;

import Util.position;

public class boolLiteralExpr extends ExprNode {
    public boolean value;

    public boolLiteralExpr(position pos, boolean value) {
        super(pos);
        this.value = value;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
