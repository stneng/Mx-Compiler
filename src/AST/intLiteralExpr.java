package AST;

import Util.position;

public class intLiteralExpr extends ExprNode {
    public int value;

    public intLiteralExpr(position pos, int value) {
        super(pos);
        this.value = value;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
