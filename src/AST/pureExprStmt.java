package AST;

import Util.position;

public class pureExprStmt extends StmtNode {
    public ExprNode expr;

    public pureExprStmt(position pos, ExprNode expr) {
        super(pos);
        this.expr = expr;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
