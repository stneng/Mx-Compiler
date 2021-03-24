package IR.inst;

import IR.Block;
import IR.operand.Operand;
import IR.operand.Register;

import java.util.ArrayList;

public class Assign extends Inst {
    public Operand value;

    public Assign(Block block, Operand reg, Operand value) {
        super(block, (Register) reg);
        this.value = value;
    }

    @Override
    public String toString() {
        return reg.toString() + " = " + (value.type != null ? value.type.toString() : "") + " " + value.toString();
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
