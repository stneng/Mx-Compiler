package Backend;

import IR.Block;
import IR.Function;
import IR.IR;
import IR.inst.Assign;
import IR.inst.Inst;
import IR.inst.Jump;
import IR.inst.Phi;
import IR.operand.Operand;
import IR.operand.Register;

import java.util.Iterator;
import java.util.Map;

public class PhiEliminate {
    public IR ir;

    public PhiEliminate(IR ir) {
        this.ir = ir;
    }

    public void eliminate(Function func) {
        int block_id = 0;
        for (int i0 = 0; i0 < func.blocks.size(); i0++) {
            Block b = func.blocks.get(i0);
            // check phi
            boolean cond = false;
            for (int i = 0; i < b.inst.size(); i++) {
                Inst inst = b.inst.get(i);
                if (inst instanceof Phi) {
                    cond = true;
                    break;
                }
            }
            if (!cond) continue;
            // split critical edge
            for (int i = 0; i < b.pre.size(); i++) {
                Block x = b.pre.get(i);
                if (x.nxt.size() > 1) {
                    Block tmp = new Block(0);
                    tmp.name = "block.phi." + (block_id++);
                    func.blocks.add(tmp);
                    tmp.addInst(new Jump(tmp, b));
                    tmp.terminated = true;
                    tmp.nxt.add(b);
                    b.pre.set(i, tmp);
                    b.replaceBlockPre(x, tmp);
                    for (int i1 = 0; i1 < x.nxt.size(); i1++) {
                        if (x.nxt.get(i1) == b) x.nxt.set(i1, tmp);
                    }
                    x.replaceBlockNxt(b, tmp);
                }
            }
            // get parallel copy
            for (int i = 0; i < b.inst.size(); i++) {
                Inst inst = b.inst.get(i);
                if (inst instanceof Phi) {
                    for (int i1 = 0; i1 < ((Phi) inst).blocks.size(); i1++) {
                        ((Phi) inst).blocks.get(i1).pCopy.put(inst.reg, ((Phi) inst).values.get(i1));
                    }
                    b.inst.remove(i);
                    i--;
                }
            }
        }
        // parallel copy
        for (int i0 = 0; i0 < func.blocks.size(); i0++) {
            Block b = func.blocks.get(i0);
            while (!b.pCopy.isEmpty()) {
                boolean cond = true;
                while (cond) {
                    cond = false;
                    Iterator<Map.Entry<Register, Operand>> it = b.pCopy.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<Register, Operand> x = it.next();
                        if (!(x.getValue() instanceof Register) || !b.pCopy.containsKey(x.getValue())) {
                            b.addInstBack(new Assign(b, x.getKey(), x.getValue()));
                            it.remove();
                            cond = true;
                        }
                    }
                }
                Iterator<Map.Entry<Register, Operand>> it = b.pCopy.entrySet().iterator();
                if (it.hasNext()) {
                    Map.Entry<Register, Operand> x = it.next();
                    Register tmp = new Register(x.getKey().type, "tmp");
                    b.addInstBack(new Assign(b, tmp, x.getValue()));
                    b.pCopy.forEach((key, value) -> {
                        if (value == x.getValue()) b.pCopy.replace(key, tmp);
                    });
                }
            }
        }
    }

    public void run() {
        ir.func.forEach((s, x) -> eliminate(x));
    }
}
