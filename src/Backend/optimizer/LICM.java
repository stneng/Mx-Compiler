package Backend.optimizer;

import Backend.DominatorTree;
import IR.Block;
import IR.Function;
import IR.IR;
import IR.inst.*;
import IR.operand.Operand;
import IR.operand.Register;

import java.util.*;

public class LICM {
    public IR ir;
    public Function currentFunction = null;
    public AliasAnalysis alias;

    public LICM(IR ir) {
        this.ir = ir;
    }

    public DominatorTree domTree;

    public HashMap<Register, Inst> regDef;

    public void regDefCollect() {
        regDef = new HashMap<>();
        for (Block block : currentFunction.blocks) {
            for (Inst inst : block.inst) {
                if (inst.reg != null) regDef.put(inst.reg, inst);
            }
        }
    }

    public boolean simpleCheck(HashSet<Block> loopBlock, Inst inst) {
        ArrayList<Operand> use = inst.getUseOperand();
        for (Operand op : use) {
            if (op instanceof Register && regDef.containsKey(op) && loopBlock.contains(regDef.get(op).block)) {
                return false;
            }
        }
        return true;
    }

    public boolean memCheck(HashSet<Block> loopBlock, Load inst) {
        if (!(inst.address instanceof Register)) return true;
        if (!simpleCheck(loopBlock, inst)) return false;
        return alias.memNoConflictCheckInBlocks(loopBlock, inst);
    }

    public void doFunc() {
        for (Block b : domTree.rNodes) {
            if (!(b.getTerminator() instanceof Jump)) continue;
            // get loop
            Block head = ((Jump) b.getTerminator()).dest;
            ArrayList<Block> tails = new ArrayList<>();
            HashSet<Block> sub = domTree.domSubTree.get(b);
            for (Block block : sub) {
                if (block.getTerminator() instanceof Jump && ((Jump) block.getTerminator()).dest == head) {
                    tails.add(block);
                }
            }
            if (tails.isEmpty()) continue;
            HashSet<Block> loopBlock = new HashSet<>();
            loopBlock.add(head);
            loopBlock.addAll(tails);
            Queue<Block> q = new LinkedList<>(tails);
            while (!q.isEmpty()) {
                Block block = q.poll();
                block.pre.forEach(x -> {
                    if (!loopBlock.contains(x)) {
                        loopBlock.add(x);
                        q.add(x);
                    }
                });
            }
            // check
            boolean cond = true;
            for (Block block : loopBlock) {
                if (!sub.contains(block)) {
                    cond = false;
                    break;
                }
            }
            if (!cond) continue;
            // do
            for (Block block : loopBlock) {
                for (int i = 0; i < block.inst.size(); i++) {
                    Inst inst = block.inst.get(i);
                    if (((inst instanceof Binary || inst instanceof BitCast || inst instanceof GetElementPtr) && simpleCheck(loopBlock, inst))
                            || (inst instanceof Load && memCheck(loopBlock, (Load) inst))) {
                        inst.block = b;
                        b.addInstBack(inst);
                        block.inst.remove(i);
                        i--;
                    }
                }
            }
        }
    }

    public void run() {
        (alias = new AliasAnalysis(ir)).run();
        ir.func.forEach((s, x) -> {
            currentFunction = x;
            (domTree = new DominatorTree(currentFunction)).run();
            regDefCollect();
            doFunc();
            currentFunction = null;
        });
    }
}
