package IR.inst;

import IR.Block;
import IR.operand.Operand;
import IR.operand.Register;

import java.util.ArrayList;

public class BitCast extends Inst {
    public Operand value;

    public BitCast(Block block, Operand reg, Operand value) {
        super(block, (Register) reg);
        this.value = value;
    }

    @Override
    public String toString() {
        return reg.toString() + " = bitcast " + value.type.toString() + " " + value.toString() + " to " + reg.type.toString();
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
