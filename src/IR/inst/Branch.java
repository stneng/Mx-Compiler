package IR.inst;

import IR.Block;
import IR.operand.Operand;

import java.util.ArrayList;

public class Branch extends Inst {
    public Operand condition;
    public Block trueDest, falseDest;

    public Branch(Block block, Operand condition, Block trueDest, Block falseDest) {
        super(block, null);
        this.condition = condition;
        this.trueDest = trueDest;
        this.falseDest = falseDest;
    }

    @Override
    public String toString() {
        return "br " + condition.type.toString() + " " + condition.toString() + ", label " + trueDest.toString() + ", label " + falseDest.toString();
    }

    @Override
    public ArrayList<Operand> getUseOperand() {
        ArrayList<Operand> ans = new ArrayList<>();
        ans.add(condition);
        return ans;
    }

    @Override
    public void replace(Operand a, Operand b) {
        if (condition == a) condition = b;
    }
}
