package Backend.optimizer;

import IR.Block;
import IR.IR;
import IR.inst.*;

public class CSE {
    public IR ir;

    public CSE(IR ir) {
        this.ir = ir;
    }

    public boolean same(Binary a, Binary b) {
        return a.op.equals(b.op) && a.src1.equals(b.src1) && a.src2.equals(b.src2);
    }

    public boolean same(BitCast a, BitCast b) {
        return a.value.equals(b.value) && a.reg.type.equals(b.reg.type);
    }

    public boolean same(Cmp a, Cmp b) {
        return a.op.equals(b.op) && a.src1.equals(b.src1) && a.src2.equals(b.src2);
    }

    public boolean same(GetElementPtr a, GetElementPtr b) {
        return a.base.equals(b.base) && a.index.equals(b.index) && ((a.offset == null && b.offset == null) || (a.offset != null && a.offset.equals(b.offset)));
    }

    public void doBlock(Block block) {
        for (int i = 0; i < block.inst.size(); i++) {
            Inst inst = block.inst.get(i);
            for (int j = i + 1; j < block.inst.size(); j++) {
                Inst inst2 = block.inst.get(j);
                if ((inst instanceof Binary && inst2 instanceof Binary && same((Binary) inst, (Binary) inst2)) ||
                        (inst instanceof BitCast && inst2 instanceof BitCast && same((BitCast) inst, (BitCast) inst2)) ||
                        (inst instanceof Cmp && inst2 instanceof Cmp && same((Cmp) inst, (Cmp) inst2)) ||
                        (inst instanceof GetElementPtr && inst2 instanceof GetElementPtr && same((GetElementPtr) inst, (GetElementPtr) inst2))) {
                    block.inst.set(j, new Assign(block, inst2.reg, inst.reg));
                }
            }
        }
    }

    public void run() {
        ir.func.forEach((s, x) -> x.blocks.forEach(this::doBlock));
    }
}
