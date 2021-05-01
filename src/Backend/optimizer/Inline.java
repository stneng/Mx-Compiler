package Backend.optimizer;

import IR.Block;
import IR.Function;
import IR.IR;
import IR.inst.*;
import IR.operand.Operand;
import IR.operand.Register;
import IR.operand.Void;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Inline {
    public IR ir;

    public Inline(IR ir) {
        this.ir = ir;
    }

    public HashMap<Function, ArrayList<Function>> edge;
    public HashMap<Function, ArrayList<Call>> reEdge;
    public HashMap<Function, ArrayList<Function>> reEdgeF;

    public void edgeCollect() {
        edge = new HashMap<>();
        reEdge = new HashMap<>();
        reEdgeF = new HashMap<>();
        ir.func.forEach((s, x) -> edge.put(x, new ArrayList<>()));
        ir.func.forEach((s, x) -> reEdge.put(x, new ArrayList<>()));
        ir.func.forEach((s, x) -> reEdgeF.put(x, new ArrayList<>()));
        ir.func.forEach((s, x) -> {
            for (Block block : x.blocks) {
                for (Inst inst : block.inst) {
                    if (inst instanceof Call && !((Call) inst).func.name.startsWith("__mx_builtin_")) {
                        edge.get(x).add(((Call) inst).func);
                        reEdge.get(((Call) inst).func).add((Call) inst);
                        reEdgeF.get(((Call) inst).func).add(x);
                    }
                }
            }
        });
    }

    public HashSet<Function> canNotInline = new HashSet<>();
    public HashSet<Function> visited = new HashSet<>();
    public ArrayList<Function> stack = new ArrayList<>();
    public HashSet<Function> canInline = new HashSet<>();

    public void dfs(Function func) {
        visited.add(func);
        stack.add(func);
        boolean ring = false;
        for (Function x : stack) {
            if (edge.get(func).contains(x)) ring = true;
            if (ring) canNotInline.add(x);
        }
        for (Function x : edge.get(func)) {
            if (!visited.contains(x)) dfs(x);
        }
        stack.remove(stack.size() - 1);
    }

    public void inlineCheck() {
        canNotInline.add(ir.func.get("main"));
        dfs(ir.func.get("main"));
        ir.func.forEach((s, x) -> {
            if (!canNotInline.contains(x)) {
                int instNum = x.blocks.stream().mapToInt(b -> b.inst.size()).sum();
                if (x.blocks.size() <= 30 && instNum <= 300) {
                    canInline.add(x);
                }
            }
        });
    }

    public Function currentFunction;

    public void doBlock(Block block) {
        currentFunction.blocks.add(block);
        block.nxt.forEach(x -> {
            if (!currentFunction.blocks.contains(x)) doBlock(x);
        });
    }

    public void blockCollect(Function func) {
        currentFunction = func;
        currentFunction.blocks = new ArrayList<>();
        doBlock(func.beginBlock);
    }

    public int tot = 0;
    public String prefix;
    public HashMap<Block, Block> inlineBlock;
    public HashMap<Operand, Operand> inlineOperand;

    public Operand getReplace(Operand x) {
        if (x == null) return null;
        if (!inlineOperand.containsKey(x)) {
            if (x instanceof Register && !((Register) x).isGlobal)
                inlineOperand.put(x, new Register(x.type, prefix + ((Register) x).name, ((Register) x).isGlobal, ((Register) x).isConstPtr));
            else inlineOperand.put(x, x);
        }
        return inlineOperand.get(x);
    }

    public Block getReplace(Block x) {
        return inlineBlock.get(x);
    }

    public void inline(Call call, Function caller) {
        Function callee = call.func;
        int instNum = callee.blocks.stream().mapToInt(b -> b.inst.size()).sum();
        if (callee.blocks.size() > 30 || instNum > 300) return;
        inlineBlock = new HashMap<>();
        inlineOperand = new HashMap<>();
        prefix = "inline." + callee.name + "." + (++tot) + ".";
        callee.blocks.forEach(b -> {
            Block nb = new Block(b.loopDepth);
            nb.name = prefix + b.name;
            nb.terminated = b.terminated;
            inlineBlock.put(b, nb);
        });
        Block beginBlock = getReplace(callee.beginBlock), endBlock = new Block(-1);
        for (Block b : callee.blocks) {
            Block nb = getReplace(b);
            for (Block x : b.pre) nb.pre.add(getReplace(x));
            for (Block x : b.nxt) nb.nxt.add(getReplace(x));
            for (Inst inst : b.inst) {
                if (inst instanceof Assign) {
                    nb.inst.add(new Assign(nb, getReplace(inst.reg), getReplace(((Assign) inst).value)));
                } else if (inst instanceof Binary) {
                    nb.inst.add(new Binary(nb, (Register) getReplace(inst.reg), ((Binary) inst).op, getReplace(((Binary) inst).src1), getReplace(((Binary) inst).src2)));
                } else if (inst instanceof BitCast) {
                    nb.inst.add(new BitCast(nb, getReplace(inst.reg), getReplace(((BitCast) inst).value)));
                } else if (inst instanceof Branch) {
                    nb.inst.add(new Branch(nb, getReplace(((Branch) inst).condition), getReplace(((Branch) inst).trueDest), getReplace(((Branch) inst).falseDest)));
                } else if (inst instanceof Call) {
                    Call ni = new Call(nb, (Register) getReplace(inst.reg), ((Call) inst).func);
                    ((Call) inst).param.forEach(x -> ni.param.add(getReplace(x)));
                    nb.inst.add(ni);
                } else if (inst instanceof Cmp) {
                    nb.inst.add(new Cmp(nb, (Register) getReplace(inst.reg), ((Cmp) inst).op, getReplace(((Cmp) inst).src1), getReplace(((Cmp) inst).src2)));
                } else if (inst instanceof GetElementPtr) {
                    nb.inst.add(new GetElementPtr(nb, getReplace(inst.reg), getReplace(((GetElementPtr) inst).base), getReplace(((GetElementPtr) inst).index), ((GetElementPtr) inst).offset));
                } else if (inst instanceof Jump) {
                    nb.inst.add(new Jump(nb, getReplace(((Jump) inst).dest)));
                } else if (inst instanceof Load) {
                    nb.inst.add(new Load(nb, (Register) getReplace(inst.reg), getReplace(((Load) inst).address)));
                } else if (inst instanceof Phi) {
                    Phi ni = new Phi(nb, (Register) getReplace(inst.reg));
                    ni.domPhi = ((Phi) inst).domPhi;
                    for (int i = 0; i < ((Phi) inst).blocks.size(); i++)
                        ni.add(getReplace(((Phi) inst).blocks.get(i)), getReplace(((Phi) inst).values.get(i)));
                    nb.inst.add(ni);
                } else if (inst instanceof Return) {
                    nb.inst.add(new Return(nb, getReplace(((Return) inst).value)));
                    endBlock = nb;
                } else if (inst instanceof Store) {
                    nb.inst.add(new Store(nb, getReplace(((Store) inst).address), getReplace(((Store) inst).value)));
                }
            }
        }

        Block callerBlock = call.block;
        int pos = 0;
        for (int i = 0; i < callerBlock.inst.size(); i++) {
            if (call == callerBlock.inst.get(i)) {
                pos = i;
                break;
            }
        }
        Block CB1 = new Block(callerBlock.loopDepth);
        CB1.name = callerBlock.name + ".inline1";
        CB1.inst = new ArrayList<>(callerBlock.inst.subList(0, pos));
        for (int i = 0; i < call.param.size(); i++)
            CB1.addInst(new Assign(CB1, getReplace(callee.params.get(i)), call.param.get(i)));
        CB1.inst.addAll(beginBlock.inst);
        CB1.pre = callerBlock.pre;
        CB1.nxt = beginBlock.nxt;
        CB1.terminated = true;
        for (Inst inst : CB1.inst) inst.block = CB1;
        CB1.pre.forEach(b -> {
            for (int i = 0; i < b.nxt.size(); i++) if (b.nxt.get(i) == callerBlock) b.nxt.set(i, CB1);
            b.replaceBlockNxt(callerBlock, CB1);
        });
        CB1.nxt.forEach(b -> {
            for (int i = 0; i < b.pre.size(); i++) if (b.pre.get(i) == beginBlock) b.pre.set(i, CB1);
            b.replaceBlockPre(beginBlock, CB1);
        });
        if (caller.beginBlock == callerBlock) caller.beginBlock = CB1;
        if (endBlock == beginBlock) endBlock = CB1;
        Block CB2 = new Block(callerBlock.loopDepth);
        CB2.name = callerBlock.name + ".inline2";
        Return ret = (Return) endBlock.getTerminator();
        endBlock.removeTerminator();
        CB2.inst = new ArrayList<>(endBlock.inst);
        if (!(ret.value instanceof Void)) CB2.inst.add(new Assign(CB2, call.reg, ret.value));
        CB2.inst.addAll(callerBlock.inst.subList(pos + 1, callerBlock.inst.size()));
        CB2.pre = endBlock.pre;
        CB2.nxt = callerBlock.nxt;
        CB2.terminated = true;
        for (Inst inst : CB2.inst) inst.block = CB2;
        Block finalEndBlock = endBlock;
        CB2.pre.forEach(b -> {
            for (int i = 0; i < b.nxt.size(); i++) if (b.nxt.get(i) == finalEndBlock) b.nxt.set(i, CB2);
            b.replaceBlockNxt(finalEndBlock, CB2);
        });
        CB2.nxt.forEach(b -> {
            for (int i = 0; i < b.pre.size(); i++) if (b.pre.get(i) == callerBlock) b.pre.set(i, CB2);
            b.replaceBlockPre(callerBlock, CB2);
        });
        if (caller.beginBlock == endBlock) caller.beginBlock = CB2;
        blockCollect(caller);
    }

    public HashSet<Function> inlineDone = new HashSet<>();

    public void inlineFunc(Function x) {
        if (inlineDone.contains(x)) return;
        inlineDone.add(x);
        edge.get(x).forEach(this::inlineFunc);
        for (int i = 0; i < reEdge.get(x).size(); i++) {
            inline(reEdge.get(x).get(i), reEdgeF.get(x).get(i));
        }
    }

    public void run() {
        edgeCollect();
        inlineCheck();
        for (Function func : canInline) inlineFunc(func);
        for (Map.Entry<String, Function> entry : ir.func.entrySet()) {
            Function x = entry.getValue();
            if (edge.get(x).contains(x)) {
                for (int i = 0; i < reEdge.get(x).size(); i++) {
                    inline(reEdge.get(x).get(i), reEdgeF.get(x).get(i));
                }
            }
        }
    }
}
