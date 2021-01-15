package AST;

import Util.position;

public class stringLiteralExpr extends ExprNode {
    public String value;

    public stringLiteralExpr(position pos, String value) {
        super(pos);
        this.value = value;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
