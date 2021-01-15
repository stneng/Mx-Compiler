package AST;

import Util.position;

public class nullLiteralExpr extends ExprNode {
    public nullLiteralExpr(position pos) {
        super(pos);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
