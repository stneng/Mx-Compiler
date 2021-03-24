package AST;

import IR.Block;
import Util.position;

public class forStmt extends StmtNode {
    public ExprNode init, condition, inc;
    public StmtNode body;
    public Block destBlock, incBlock;

    public forStmt(position pos, ExprNode init, ExprNode condition, ExprNode inc, StmtNode body) {
        super(pos);
        this.init = init;
        this.condition = condition;
        this.inc = inc;
        this.body = body;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
