package AST;

import Util.position;

public class ifStmt extends StmtNode {
    public ExprNode condition;
    public StmtNode trueStmt, falseStmt;

    public ifStmt(position pos, ExprNode condition, StmtNode trueStmt, StmtNode falseStmt) {
        super(pos);
        this.condition = condition;
        this.trueStmt = trueStmt;
        this.falseStmt = falseStmt;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
