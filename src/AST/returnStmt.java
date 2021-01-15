package AST;

import Util.position;

public class returnStmt extends StmtNode {
    public ExprNode returnValue;

    public returnStmt(position pos, ExprNode returnValue) {
        super(pos);
        this.returnValue = returnValue;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
