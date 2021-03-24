package AST;

import AST.symbol.Type;
import IR.Block;
import IR.operand.Operand;
import Util.position;

public abstract class ExprNode extends ASTNode {
    public Type type;
    public boolean assignable = false;
    public Block trueBlock = null, falseBlock = null;
    public Operand operand;

    public ExprNode(position pos) {
        super(pos);
    }

    public ExprNode(position pos, boolean assignable) {
        super(pos);
        this.assignable = assignable;
    }
}
