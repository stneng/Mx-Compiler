package AST;

import Util.position;

public class whileStmt extends StmtNode {
    public ExprNode condition;
    public StmtNode body;

    public whileStmt(position pos, ExprNode condition, StmtNode body) {
        super(pos);
        this.condition = condition;
        this.body = body;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
