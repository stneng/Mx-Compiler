package Backend.optimizer;

import IR.Block;
import IR.Function;
import IR.IR;
import IR.inst.*;
import IR.operand.Register;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class ADCE {
    public IR ir;

    public ADCE(IR ir) {
        this.ir = ir;
    }

    public HashMap<Register, Inst> regDef;

    public void regDefCollect(Function func) {
        regDef = new HashMap<>();
        for (Block block : func.blocks) {
            for (Inst inst : block.inst) {
                if (inst.reg != null) regDef.put(inst.reg, inst);
            }
        }
    }

    public HashSet<Inst> liveInst;
    public Queue<Inst> q;

    public void doFunc(Function func) {
        liveInst = new HashSet<>();
        q = new LinkedList<>();
        regDefCollect(func);
        func.blocks.forEach(block -> block.inst.forEach(inst -> {
            if (inst instanceof Branch || inst instanceof Call || inst instanceof Jump || inst instanceof Return || inst instanceof Store) {
                liveInst.add(inst);
                q.add(inst);
            }
        }));
        while (!q.isEmpty()) {
            Inst inst = q.poll();
            inst.getUseOperand().forEach(op -> {
                if (op instanceof Register && regDef.containsKey(op)) {
                    Inst defInst = regDef.get(op);
                    if (!liveInst.contains(defInst)) {
                        liveInst.add(defInst);
                        q.add(defInst);
                    }
                }
            });
        }
        func.blocks.forEach(block -> block.inst.removeIf(inst -> !liveInst.contains(inst)));
    }

    public void run() {
        ir.func.forEach((s, x) -> doFunc(x));
    }
}
