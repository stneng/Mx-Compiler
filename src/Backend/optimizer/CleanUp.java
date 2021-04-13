package Backend.optimizer;

import IR.Block;
import IR.Function;
import IR.IR;
import IR.inst.Assign;
import IR.inst.Call;
import IR.inst.Inst;
import IR.inst.Phi;
import IR.operand.ConstStr;
import IR.operand.Operand;
import IR.operand.Register;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class CleanUp {
    public IR ir;
    public Function currentFunction = null;

    public CleanUp(IR ir) {
        this.ir = ir;
    }

    public void doBlock(Block block) {
        block.name = "block." + currentFunction.blocks.size();
        currentFunction.blocks.add(block);
        block.nxt.forEach(x -> {
            if (!currentFunction.blocks.contains(x)) doBlock(x);
        });
    }

    public void removeDeadBlock() {
        currentFunction.blocks.forEach(x -> {
            for (int i = 0; i < x.pre.size(); i++) {
                if (!currentFunction.blocks.contains(x.pre.get(i))) {
                    x.pre.remove(i);
                    i--;
                }
            }
        });
    }

    public HashSet<Register> regUse;

    public void regUseCollect() {
        regUse = new HashSet<>();
        for (Block block : currentFunction.blocks) {
            for (Inst inst : block.inst) {
                for (Operand reg : inst.getUseOperand())
                    if (reg instanceof Register) {
                        regUse.add((Register) reg);
                    }
            }
        }
    }

    public void removeDeadInst() {
        boolean cond = true;
        while (cond) {
            cond = false;
            regUseCollect();
            for (Block block : currentFunction.blocks) {
                for (int i = 0; i < block.inst.size(); i++) {
                    Inst inst = block.inst.get(i);
                    if (inst.reg != null && !regUse.contains(inst.reg) && !(inst instanceof Call)) {
                        block.inst.remove(i);
                        i--;
                        cond = true;
                    }
                }
            }
        }
    }

    public void removeAssign() {
        HashMap<Register, Operand> assignMap = new HashMap<>();
        currentFunction.blocks.forEach(b -> b.inst.forEach(x -> {
            if (x instanceof Assign) assignMap.put(x.reg, ((Assign) x).value);
        }));
        currentFunction.blocks.forEach(b -> b.inst.forEach(x -> {
            if (x instanceof Phi) {
                ((Phi) x).values.forEach(t -> {
                    if (t instanceof Register) assignMap.remove(t);
                });
            }
        }));
        currentFunction.blocks.forEach(t -> {
            for (int i = 0; i < t.inst.size(); i++) {
                Inst x = t.inst.get(i);
                if (assignMap.containsKey(x.reg)) {
                    t.removeInst(x);
                    i--;
                } else {
                    ArrayList<Operand> ops = x.getUseOperand();
                    ops.forEach(op -> {
                        if (op instanceof Register) {
                            Operand replace = op;
                            while (replace instanceof Register && assignMap.get((Register) replace) != null) {
                                replace = assignMap.get((Register) replace);
                            }
                            if (op != replace) x.replace(op, replace);
                        }
                    });
                }
            }
        });
    }

    public HashSet<Function> funcUse = new HashSet<>();

    public void doEachInst() {
        for (Block block : currentFunction.blocks) {
            for (int i = 0; i < block.inst.size(); i++) {
                Inst inst = block.inst.get(i);
                // remove dead block in phi
                if (inst instanceof Phi) {
                    for (int i1 = 0; i1 < ((Phi) inst).blocks.size(); i1++) {
                        if (!currentFunction.blocks.contains(((Phi) inst).blocks.get(i1))) {
                            ((Phi) inst).blocks.remove(i1);
                            ((Phi) inst).values.remove(i1);
                            i1--;
                        }
                    }
                    if (((Phi) inst).blocks.size() == 1) {
                        block.inst.set(i, new Assign(block, inst.reg, ((Phi) inst).values.get(0)));
                    }
                }
                // collect constStr
                inst.getUseOperand().forEach(a -> {
                    if (a instanceof ConstStr && !ir.constStr.containsValue(a)) {
                        ((ConstStr) a).name = "const_str_" + ir.constStr.size();
                        ir.constStr.put(((ConstStr) a).name, (ConstStr) a);
                    }
                });
                // collect func
                if (inst instanceof Call) funcUse.add(((Call) inst).func);
            }
        }
    }

    public void doFunc(Function func) {
        currentFunction = func;
        currentFunction.blocks = new ArrayList<>();
        doBlock(func.beginBlock);
        removeDeadBlock();
        removeDeadInst();
        doEachInst();
        removeAssign();
        currentFunction = null;
    }

    public void run() {
        ir.constStr = new HashMap<>();
        ir.func.forEach((s, x) -> doFunc(x));
        funcUse.add(ir.func.get("main"));
        ir.func.entrySet().removeIf(x -> !funcUse.contains(x.getValue()));
    }
}
