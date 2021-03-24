package IR.inst;

import IR.Block;
import IR.operand.Operand;
import IR.operand.Void;

import java.util.ArrayList;

public class Return extends Inst {
    public Operand value;

    public Return(Block block, Operand value) {
        super(block, null);
        this.value = value;
    }

    @Override
    public String toString() {
        if (value instanceof Void) return "ret void";
        else return "ret " + value.type.toString() + " " + value.toString();
    }

    @Override
    public ArrayList<Operand> getUseOperand() {
        ArrayList<Operand> ans = new ArrayList<>();
        ans.add(value);
        return ans;
    }

    @Override
    public void replace(Operand a, Operand b) {
        if (value == a) value = b;
    }
}
