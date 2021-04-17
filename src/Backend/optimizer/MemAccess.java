package Backend.optimizer;

import IR.Block;
import IR.IR;
import IR.inst.*;
import IR.operand.Operand;

public class MemAccess {
    public IR ir;

    public MemAccess(IR ir) {
        this.ir = ir;
    }

    public void doBlock(Block block) {
        ir.gVar.forEach((s, x) -> {
            Operand value = null;
            boolean needStore = false;
            for (int i = 0; i < block.inst.size(); i++) {
                Inst inst = block.inst.get(i);
                if (inst instanceof Load && ((Load) inst).address.equals(x)) {
                    if (value != null) block.inst.set(i, new Assign(block, inst.reg, value));
                    else value = inst.reg;
                }
                if (inst instanceof Store && ((Store) inst).address.equals(x)) {
                    value = ((Store) inst).value;
                    needStore = true;
                    block.inst.remove(i);
                    i--;
                }
                if (inst instanceof Call || i == block.inst.size() - 1) {
                    if (needStore) {
                        block.inst.add(i, new Store(block, x, value));
                        i++;
                        needStore = false;
                    }
                    value = null;
                }
            }
        });

    }

    public void run() {
        ir.func.forEach((s, x) -> x.blocks.forEach(this::doBlock));
    }
}
