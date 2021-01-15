package AST;

import Util.position;

import java.util.ArrayList;

public class funcCallExpr extends ExprNode {
    public ExprNode base;
    public ArrayList<ExprNode> exprList;

    public funcCallExpr(position pos, ExprNode base, exprListExpr exprList) {
        super(pos);
        this.base = base;
        this.exprList = exprList.exprList;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
