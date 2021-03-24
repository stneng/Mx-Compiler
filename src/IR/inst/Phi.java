package IR.inst;

import IR.Block;
import IR.operand.Operand;
import IR.operand.Register;

import java.util.ArrayList;

public class Phi extends Inst {
    public ArrayList<Block> blocks = new ArrayList<>();
    public ArrayList<Operand> values = new ArrayList<>();
    public Register phiReg;
    public boolean domPhi = false;

    public Phi(Block block, Register reg) {
        super(block, reg);
        phiReg = reg;
    }

    public void add(Block block, Operand value) {
        blocks.add(block);
        values.add(value);
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder(reg.toString() + " = phi " + reg.type.toString() + " ");
        for (int i = 0; i < blocks.size(); i++) {
            ans.append("[ ").append(values.get(i).toString()).append(", ").append(blocks.get(i).toString()).append(" ]");
            if (i != blocks.size() - 1) ans.append(", ");
        }
        return ans.toString();
    }

    @Override
    public ArrayList<Operand> getUseOperand() {
        return values;
    }

    @Override
    public void replace(Operand a, Operand b) {
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i) == a) {
                values.set(i, b);
            }
        }
    }
}
