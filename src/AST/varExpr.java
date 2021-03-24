package AST;

import AST.symbol.varSymbol;
import Util.position;

public class varExpr extends ExprNode {
    public String name;
    public varSymbol varNode;

    public varExpr(position pos, String name) {
        super(pos, true);
        this.name = name;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
