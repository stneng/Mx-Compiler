package AST;

import Util.position;

public class suffixExpr extends ExprNode {
    public ExprNode src;
    public String op;

    public suffixExpr(position pos, ExprNode src, String op) {
        super(pos);
        this.src = src;
        this.op = op;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
