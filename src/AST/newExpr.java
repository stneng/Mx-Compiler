package AST;

import Util.position;

import java.util.ArrayList;

public class newExpr extends ExprNode {
    public TypeNode typeNode;
    public ArrayList<ExprNode> exprList;

    public newExpr(position pos, TypeNode type, int dim, ArrayList<ExprNode> exprList) {
        super(pos);
        this.typeNode = type;
        this.typeNode.dim = dim;
        this.exprList = exprList;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
