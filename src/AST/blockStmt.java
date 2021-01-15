package AST;

import Util.position;

import java.util.ArrayList;

public class blockStmt extends StmtNode {
    public ArrayList<StmtNode> stmtList = new ArrayList<>();

    public blockStmt(position pos) {
        super(pos);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
