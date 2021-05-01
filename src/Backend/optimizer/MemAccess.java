package Backend.optimizer;

import IR.Block;
import IR.Function;
import IR.IR;
import IR.inst.*;
import IR.operand.Operand;

import java.util.ArrayList;

public class MemAccess {
    public IR ir;
    public Function currentFunction = null;
    public AliasAnalysis alias;

    public MemAccess(IR ir) {
        this.ir = ir;
    }

    public void doBlock(Block block) {
        ArrayList<Operand> doList = new ArrayList<>();
        ArrayList<Operand> doListS = new ArrayList<>();
        for (Inst inst : block.inst) {
            if (inst instanceof Load) {
                doList.add(((Load) inst).address);
            } else if (inst instanceof Store) {
                doList.add(((Store) inst).address);
                doListS.add(((Store) inst).address);
            }
        }
        doList.forEach(x -> {
            Operand value = null;
            int lastStore = -1;
            for (int i = 0; i < block.inst.size(); i++) {
                Inst inst = block.inst.get(i);
                if (inst instanceof Load && ((Load) inst).address.equals(x)) {
                    if (value != null) block.inst.set(i, new Assign(block, inst.reg, value));
                    else value = inst.reg;
                } else if (inst instanceof Load && alias.mayConflictData(x, ((Load) inst).address)) {
                    lastStore = -1;
                }
                if (inst instanceof Store && ((Store) inst).address.equals(x)) {
                    if (lastStore != -1) {
                        block.inst.remove(lastStore);
                        i--;
                    }
                    value = ((Store) inst).value;
                    lastStore = i;
                } else if (inst instanceof Store && alias.mayConflictData(x, ((Store) inst).address)) {
                    lastStore = -1;
                    value = null;
                }
                if (inst instanceof Call) {
                    if (alias.funcConflictS(((Call) inst).func, x)) {
                        lastStore = -1;
                        value = null;
                    } else if (alias.funcConflict(((Call) inst).func, x) && ((Call) inst).func.name.startsWith("__mx_builtin_")) {
                        lastStore = -1;
                    }
                }
            }
        });

    }

    public void run() {
        (alias = new AliasAnalysis(ir)).run();
        ir.func.forEach((s, x) -> {
            currentFunction = x;
            x.blocks.forEach(this::doBlock);
            currentFunction = null;
        });
    }
}
