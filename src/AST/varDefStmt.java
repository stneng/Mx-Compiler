package AST;

import Util.position;

import java.util.ArrayList;

public class varDefStmt extends StmtNode {
    public ArrayList<varDefSubStmt> varList = new ArrayList<>();

    public varDefStmt(position pos) {
        super(pos);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
