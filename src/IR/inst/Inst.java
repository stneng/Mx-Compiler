package IR.inst;

import IR.Block;
import IR.operand.Operand;
import IR.operand.Register;

import java.util.ArrayList;

public abstract class Inst {
    public Block block;
    public Register reg;

    public Inst(Block block, Register reg) {
        this.block = block;
        this.reg = reg;
    }

    public abstract String toString();

    public ArrayList<Operand> getUseOperand() {
        return new ArrayList<>();
    }

    public abstract void replace(Operand a, Operand b);
}
