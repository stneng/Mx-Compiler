package Backend.optimizer;

import IR.Block;
import IR.Function;
import IR.IR;
import IR.inst.*;
import IR.operand.Operand;
import IR.operand.Register;
import IR.type.Pointer;

import java.util.*;

public class LICM {
    public IR ir;
    public Function currentFunction = null;

    public LICM(IR ir) {
        this.ir = ir;
    }

    public HashSet<Block> visited;
    public ArrayList<Block> rBlocks;

    public void dfsBlock(Block block) {
        visited.add(block);
        block.nxt.forEach(x -> {
            if (!visited.contains(x)) dfsBlock(x);
        });
        rBlocks.add(0, block);
    }

    public HashMap<Block, Integer> dfn;
    public HashMap<Block, Block> iDom;
    public HashMap<Block, ArrayList<Block>> domSon;

    public Block intersect(Block a, Block b) {
        if (a == null) return b;
        if (b == null) return a;
        while (a != b) {
            while (dfn.get(a) > dfn.get(b)) a = iDom.get(a);
            while (dfn.get(a) < dfn.get(b)) b = iDom.get(b);
        }
        return a;
    }

    public void domTree() {
        for (int i = 0; i < rBlocks.size(); i++) {
            dfn.put(rBlocks.get(i), i);
            iDom.put(rBlocks.get(i), null);
            domSon.put(rBlocks.get(i), new ArrayList<>());
        }
        iDom.replace(currentFunction.beginBlock, currentFunction.beginBlock);
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = 1; i < rBlocks.size(); i++) {
                Block new_iDom = null;
                for (int i1 = 0; i1 < rBlocks.get(i).pre.size(); i1++) {
                    if (iDom.get(rBlocks.get(i).pre.get(i1)) != null)
                        new_iDom = intersect(new_iDom, rBlocks.get(i).pre.get(i1));
                }
                if (iDom.get(rBlocks.get(i)) != new_iDom) {
                    iDom.replace(rBlocks.get(i), new_iDom);
                    changed = true;
                }
            }
        }
        iDom.forEach((x, f) -> {
            if (f != null && x != f) domSon.get(f).add(x);
        });
    }

    public ArrayList<Block> rNodes;
    public HashMap<Block, HashSet<Block>> domSubTree;

    public void dfsTree(Block x) {
        HashSet<Block> sub = new HashSet<>();
        domSon.get(x).forEach(a -> {
            dfsTree(a);
            sub.add(a);
            sub.addAll(domSubTree.get(a));
        });
        rNodes.add(x);
        domSubTree.put(x, sub);
    }

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
        if (((Register) inst.address).isGlobal) {
            for (Block block : loopBlock) {
                for (Inst inst2 : block.inst) {
                    if (inst2 instanceof Store && ((Store) inst2).address.equals(inst.address)) {
                        return false;
                    }
                }
            }
            return true;
        }
        if (inst.address.type instanceof Pointer && ((Pointer) inst.address.type).pointType instanceof Pointer) {
            ArrayList<Operand> use = inst.getUseOperand();
            for (Operand op : use) {
                if (op instanceof Register && regDef.containsKey(op) && loopBlock.contains(regDef.get(op).block)) {
                    return false;
                }
            }
            for (Block block : loopBlock) {
                for (Inst inst2 : block.inst) {
                    if (inst2 instanceof Store && ((Store) inst2).address.type instanceof Pointer && ((Pointer) ((Store) inst2).address.type).pointType instanceof Pointer) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    public void doFunc() {
        for (Block b : rNodes) {
            if (!(b.getTerminator() instanceof Jump)) continue;
            // get loop
            Block head = ((Jump) b.getTerminator()).dest;
            ArrayList<Block> tails = new ArrayList<>();
            HashSet<Block> sub = domSubTree.get(b);
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
                if (!sub.contains(block)) cond = false;
                for (Inst inst : block.inst) {
                    if (inst instanceof Call) {
                        cond = false;
                        break;
                    }
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
        ir.func.forEach((s, x) -> {
            currentFunction = x;
            visited = new HashSet<>();
            rBlocks = new ArrayList<>();
            dfsBlock(currentFunction.beginBlock);
            dfn = new HashMap<>();
            iDom = new HashMap<>();
            domSon = new HashMap<>();
            domTree();
            rNodes = new ArrayList<>();
            domSubTree = new HashMap<>();
            dfsTree(currentFunction.beginBlock);
            regDefCollect();
            doFunc();
            currentFunction = null;
        });
    }
}
