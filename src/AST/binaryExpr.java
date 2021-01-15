package AST;

import Util.position;

public class binaryExpr extends ExprNode {
    public ExprNode src1, src2;
    public String op;

    public binaryExpr(position pos, ExprNode src1, ExprNode src2, String op) {
        super(pos);
        this.src1 = src1;
        this.src2 = src2;
        this.op = op;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
