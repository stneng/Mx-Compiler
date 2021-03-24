package IR.inst;

import IR.Block;
import IR.operand.ConstInt;
import IR.operand.Operand;
import IR.operand.Register;
import IR.type.Pointer;

import java.util.ArrayList;

public class GetElementPtr extends Inst {
    public Operand base, index;
    public ConstInt offset = null;

    public GetElementPtr(Block block, Operand reg, Operand base, Operand index) {
        super(block, (Register) reg);
        this.base = base;
        this.index = index;
    }

    public GetElementPtr(Block block, Operand reg, Operand base, Operand index, ConstInt offset) {
        super(block, (Register) reg);
        this.base = base;
        this.index = index;
        this.offset = offset;
    }

    @Override
    public String toString() {
        return reg.toString() + " = getelementptr inbounds " + ((Pointer) base.type).pointType.toString() + ", " + base.type.toString() + " " + base.toString() + ", " + index.type.toString() + " " + index.toString() + (offset != null ? ", " + offset.type.toString() + " " + offset.toString() : "");
    }

    @Override
    public ArrayList<Operand> getUseOperand() {
        ArrayList<Operand> ans = new ArrayList<>();
        ans.add(base);
        ans.add(index);
        return ans;
    }

    @Override
    public void replace(Operand a, Operand b) {
        if (base == a) base = b;
        if (index == a) index = b;
    }
}
