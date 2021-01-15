package AST;

import Util.position;

public class subscriptExpr extends ExprNode {
    public ExprNode base, offset;

    public subscriptExpr(position pos, ExprNode base, ExprNode offset) {
        super(pos, true);
        this.base = base;
        this.offset = offset;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
