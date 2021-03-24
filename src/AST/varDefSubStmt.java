package AST;

import AST.symbol.varSymbol;
import Util.position;

public class varDefSubStmt extends StmtNode {
    public TypeNode type;
    public String name;
    public ExprNode expr;
    public varSymbol varNode;

    public varDefSubStmt(position pos, String name, ExprNode expr) {
        super(pos);
        this.name = name;
        this.expr = expr;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
