package AST;

import Util.position;

public class thisExpr extends ExprNode {

    public thisExpr(position pos) {
        super(pos);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
