package Backend.optimizer;

import IR.Block;
import IR.IR;
import IR.inst.Binary;
import IR.inst.Inst;
import IR.operand.ConstInt;

public class ImmInstMerge {
    public IR ir;

    public ImmInstMerge(IR ir) {
        this.ir = ir;
    }

    public void doBlock(Block block) {
        for (int i = 0; i < block.inst.size() - 1; i++) {
            Inst inst = block.inst.get(i), ninst = block.inst.get(i + 1);
            if (inst instanceof Binary && ninst instanceof Binary && inst.reg == ((Binary) ninst).src1 && ((Binary) inst).src2 instanceof ConstInt && ((Binary) ninst).src2 instanceof ConstInt) {
                if (((Binary) inst).op.equals("add") && ((Binary) ninst).op.equals("add")) {
                    ((Binary) ninst).src1 = ((Binary) inst).src1;
                    ((Binary) ninst).src2 = new ConstInt(((ConstInt) ((Binary) inst).src2).value + ((ConstInt) ((Binary) ninst).src2).value, 32);// ;
                    block.inst.remove(i);
                    i--;
                }
            }
        }
    }

    public void run() {
        ir.func.forEach((s, x) -> x.blocks.forEach(this::doBlock));
    }
}
