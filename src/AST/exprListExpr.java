package AST;

import Util.position;

import java.util.ArrayList;

public class exprListExpr extends ExprNode {
    public ArrayList<ExprNode> exprList = new ArrayList<>();

    public exprListExpr(position pos) {
        super(pos);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
