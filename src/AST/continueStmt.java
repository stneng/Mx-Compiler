package AST;

import Util.position;

public class continueStmt extends StmtNode {
    public StmtNode loop;

    public continueStmt(position pos) {
        super(pos);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
