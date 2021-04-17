package Backend.optimizer;

import IR.Block;
import IR.IR;
import IR.inst.Binary;
import IR.inst.Inst;
import IR.operand.ConstInt;
import IR.operand.Operand;

public class ImmInstOpt {
    public IR ir;

    public ImmInstOpt(IR ir) {
        this.ir = ir;
    }

    public void doBlock(Block block) {
        for (int i = 0; i < block.inst.size() - 1; i++) {
            Inst inst = block.inst.get(i), ninst = block.inst.get(i + 1);
            if (inst instanceof Binary && ninst instanceof Binary) {
                if ((((Binary) inst).op.equals("add") || ((Binary) inst).op.equals("mul")) && ((Binary) inst).src1 instanceof ConstInt && !(((Binary) inst).src2 instanceof ConstInt)) {
                    Operand t = ((Binary) inst).src1;
                    ((Binary) inst).src1 = ((Binary) inst).src2;
                    ((Binary) inst).src1 = t;
                }
                if ((((Binary) ninst).op.equals("add") || ((Binary) ninst).op.equals("mul")) && ((Binary) ninst).src1 instanceof ConstInt && !(((Binary) ninst).src2 instanceof ConstInt)) {
                    Operand t = ((Binary) ninst).src1;
                    ((Binary) ninst).src1 = ((Binary) ninst).src2;
                    ((Binary) ninst).src1 = t;
                }
                if (((Binary) inst).op.equals("sub") && ((Binary) inst).src2 instanceof ConstInt) {
                    ((Binary) inst).op = "add";
                    ((Binary) inst).src2 = new ConstInt(-((ConstInt) ((Binary) inst).src2).value, 32);
                }
                if (((Binary) ninst).op.equals("sub") && ((Binary) ninst).src2 instanceof ConstInt) {
                    ((Binary) ninst).op = "add";
                    ((Binary) ninst).src2 = new ConstInt(-((ConstInt) ((Binary) ninst).src2).value, 32);
                }
                if (!(inst.reg == ((Binary) ninst).src1 && ((Binary) inst).src2 instanceof ConstInt && ((Binary) ninst).src2 instanceof ConstInt))
                    continue;
                if (((Binary) inst).op.equals("add") && ((Binary) ninst).op.equals("add")) {
                    ((Binary) ninst).src1 = ((Binary) inst).src1;
                    ((Binary) ninst).src2 = new ConstInt(((ConstInt) ((Binary) inst).src2).value + ((ConstInt) ((Binary) ninst).src2).value, 32);
                }
                if (((Binary) inst).op.equals("mul") && ((Binary) ninst).op.equals("mul")) {
                    ((Binary) ninst).src1 = ((Binary) inst).src1;
                    ((Binary) ninst).src2 = new ConstInt(((ConstInt) ((Binary) inst).src2).value * ((ConstInt) ((Binary) ninst).src2).value, 32);
                }
            }
        }
        for (int i = 0; i < block.inst.size(); i++) {
            Inst inst = block.inst.get(i);
            if (inst instanceof Binary) {
                if (((Binary) inst).op.equals("mul") && ((Binary) inst).src2 instanceof ConstInt) {
                    int value = ((ConstInt) ((Binary) inst).src2).value;
                    if (value <= 0) continue;
                    int log2 = (int) (Math.log(value) / Math.log(2));
                    int delta = value - (1 << log2);
                    if (delta == 0)
                        block.inst.set(i, new Binary(block, inst.reg, "shl", ((Binary) inst).src1, new ConstInt(log2, 32)));
                }
            }
        }
    }

    public void run() {
        ir.func.forEach((s, x) -> x.blocks.forEach(this::doBlock));
    }
}
