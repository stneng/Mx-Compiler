package Backend;

import ASM.ASM;
import ASM.Block;
import ASM.Function;
import ASM.inst.*;
import ASM.operand.Imm;
import ASM.operand.PReg;
import ASM.operand.Register;
import ASM.operand.VReg;

import java.util.*;

public class RegAllocator {
    public ASM asm;
    public Function currentFunction = null;

    public RegAllocator(ASM asm) {
        this.asm = asm;
    }

    public HashMap<Block, HashSet<Register>> buses = new HashMap<>(), bdefs = new HashMap<>(), blivein = new HashMap<>(), bliveout = new HashMap<>();

    public void liveness_analysis() {
        buses = new HashMap<>();
        bdefs = new HashMap<>();
        blivein = new HashMap<>();
        bliveout = new HashMap<>();
        currentFunction.blocks.forEach(b -> {
            HashSet<Register> uses = new HashSet<>(), defs = new HashSet<>();
            b.inst.forEach(x -> {
                HashSet<Register> t = x.getUse();
                t.removeAll(defs);
                uses.addAll(t);
                defs.addAll(x.getDef());
            });
            buses.put(b, uses);
            bdefs.put(b, defs);
            blivein.put(b, new HashSet<>());
            bliveout.put(b, new HashSet<>());
        });
        // livein=use+(out-def) liveout={nxt.livein}
        HashSet<Block> vis = new HashSet<>();
        Queue<Block> q = new LinkedList<>();
        currentFunction.blocks.forEach(b -> {
            if (b.nxt.isEmpty()) {
                vis.add(b);
                q.add(b);
            }
        });
        while (!q.isEmpty()) {
            Block x = q.poll();
            vis.remove(x);
            HashSet<Register> liveout = new HashSet<>();
            x.nxt.forEach(a -> liveout.addAll(blivein.get(a)));
            bliveout.replace(x, liveout);
            HashSet<Register> livein = new HashSet<>(liveout);
            livein.removeAll(bdefs.get(x));
            livein.addAll(buses.get(x));
            if (!livein.equals(blivein.get(x))) {
                blivein.replace(x, livein);
                x.pre.forEach(a -> {
                    if (!vis.contains(a)) {
                        vis.add(a);
                        q.add(a);
                    }
                });
            }
        }
    }


    public static class edge {
        Register x, y;

        public edge(Register x, Register y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public int hashCode() {
            return x.hashCode() ^ y.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof edge && ((edge) obj).x == x && ((edge) obj).y == y);
        }
    }

    public int spOffset = 0;
    public HashMap<Register, HashSet<Mv>> moveList = new HashMap<>();
    public HashMap<Register, HashSet<Register>> adjList = new HashMap<>();
    public HashMap<Register, Double> weight = new HashMap<>();
    public HashMap<Register, Integer> degree = new HashMap<>();
    public HashMap<Register, Register> alias = new HashMap<>();
    public HashMap<Register, Integer> offset = new HashMap<>();
    public HashSet<edge> adjSet = new HashSet<>();

    public int K;
    public HashSet<Mv> workListMoves, activeMoves, coalescedMoves, constrainedMoves, frozenMoves;
    public HashSet<Register> preColored, initial, simplifyWorkList, freezeWorkList, spillWorkList, spilledNodes, coalescedNodes, coloredNodes, canNotSpillNodes;
    public Stack<Register> selectStack;

    public void init() {
        K = asm.getColors().size();
        workListMoves = new HashSet<>();
        activeMoves = new HashSet<>();
        coalescedMoves = new HashSet<>();
        constrainedMoves = new HashSet<>();
        frozenMoves = new HashSet<>();
        preColored = new HashSet<>(asm.getPreg());
        initial = new HashSet<>();
        simplifyWorkList = new HashSet<>();
        freezeWorkList = new HashSet<>();
        spillWorkList = new HashSet<>();
        spilledNodes = new HashSet<>();
        coalescedNodes = new HashSet<>();
        coloredNodes = new HashSet<>();
        selectStack = new Stack<>();
        canNotSpillNodes = new HashSet<>();
        moveList = new HashMap<>();
        adjList = new HashMap<>();
        weight = new HashMap<>();
        degree = new HashMap<>();
        alias = new HashMap<>();
        offset = new HashMap<>();
        adjSet = new HashSet<>();
        currentFunction.blocks.forEach(block -> {
            block.inst.forEach(inst -> {
                initial.addAll(inst.getUse());
                initial.addAll(inst.getDef());
            });
        });
        for (Register x : initial) {
            moveList.put(x, new HashSet<>());
            adjList.put(x, new HashSet<>());
            weight.put(x, 0.0);
            degree.put(x, 0);
            alias.put(x, x);
            x.color = null;
        }
        initial.removeAll(preColored);
        for (Register x : preColored) {
            degree.put(x, 233333333);
            x.color = (PReg) x;
        }
        currentFunction.blocks.forEach(block -> {
            block.inst.forEach(inst -> {
                inst.getUse().forEach(x -> {
                    double t = weight.get(x) + Math.pow(10.0, block.loopDepth);
                    weight.replace(x, t);
                });
                inst.getDef().forEach(x -> {
                    double t = weight.get(x) + Math.pow(10.0, block.loopDepth);
                    weight.replace(x, t);
                });
            });
        });
    }

    public void add_edge(Register x, Register y) {
        if (x != y && !adjSet.contains(new edge(x, y))) {
            adjSet.add(new edge(x, y));
            adjSet.add(new edge(y, x));
            if (!preColored.contains(x)) {
                adjList.get(x).add(y);
                int t = degree.get(x);
                degree.replace(x, t + 1);
            }
            if (!preColored.contains(y)) {
                adjList.get(y).add(x);
                int t = degree.get(y);
                degree.replace(y, t + 1);
            }
        }
    }

    public HashSet<Mv> nodeMoves(Register x) {
        HashSet<Mv> ans = new HashSet<>(activeMoves);
        ans.addAll(workListMoves);
        ans.retainAll(moveList.get(x));
        return ans;
    }

    public boolean moveRelated(Register x) {
        return !nodeMoves(x).isEmpty();
    }

    public HashSet<Register> adjacent(Register x) {
        HashSet<Register> tmp = new HashSet<>(selectStack);
        tmp.addAll(coalescedNodes);
        HashSet<Register> ans = new HashSet<>(adjList.get(x));
        ans.removeAll(tmp);
        return ans;
    }

    public void enableMoves(HashSet<Register> nodes) {
        nodes.forEach(n -> nodeMoves(n).forEach(m -> {
            if (activeMoves.contains(m)) {
                activeMoves.remove(m);
                workListMoves.add(m);
            }
        }));
    }

    public void decrementDegree(Register x) {
        int d = degree.get(x);
        degree.replace(x, d - 1);
        if (d == K) {
            HashSet<Register> t = adjacent(x);
            t.add(x);
            enableMoves(t);
            spillWorkList.remove(x);
            if (moveRelated(x)) freezeWorkList.add(x);
            else simplifyWorkList.add(x);
        }
    }

    public void simplify() {
        Register x = simplifyWorkList.iterator().next();
        simplifyWorkList.remove(x);
        selectStack.push(x);
        adjacent(x).forEach(this::decrementDegree);
    }


    public Register getAlias(Register x) {
        if (coalescedNodes.contains(x)) return getAlias(alias.get(x));
        else return x;
    }

    public void add_workList(Register x) {
        if (!preColored.contains(x) && !moveRelated(x) && degree.get(x) < K) {
            freezeWorkList.remove(x);
            simplifyWorkList.add(x);
        }
    }

    public boolean ok(Register x, Register y) {
        return degree.get(x) < K || preColored.contains(x) || adjSet.contains(new edge(x, y));
    }

    public boolean ok(ArrayList<Register> t, Register u) {
        for (Register x : t) {
            if (!ok(x, u)) return false;
        }
        return true;
    }

    public boolean conservative(ArrayList<Register> nodes, ArrayList<Register> y) {
        nodes.removeAll(y);
        nodes.addAll(y);
        int k = 0;
        for (Register node : nodes) if (degree.get(node) >= K) k++;
        return (k < K);
    }

    public void combine(Register x, Register y) {
        if (freezeWorkList.contains(y)) freezeWorkList.remove(y);
        else spillWorkList.remove(y);
        coalescedNodes.add(y);
        alias.put(y, x);
        moveList.get(x).addAll(moveList.get(y));
        enableMoves(new HashSet<>(Collections.singletonList(y)));
        adjacent(y).forEach(t -> {
            add_edge(t, x);
            decrementDegree(t);
        });
        if (degree.get(x) >= K && freezeWorkList.contains(x)) {
            freezeWorkList.remove(x);
            spillWorkList.add(x);
        }
    }

    public void coalesce() {
        Mv m = workListMoves.iterator().next();
        workListMoves.remove(m);
        Register x = getAlias(m.reg), y = getAlias(m.src);
        if (preColored.contains(y)) {
            Register t = x;
            x = y;
            y = t;
        }
        if (x == y) {
            coalescedMoves.add(m);
            add_workList(x);
        } else if (preColored.contains(y) || adjSet.contains(new edge(x, y))) {
            constrainedMoves.add(m);
            add_workList(x);
            add_workList(y);
        } else if ((preColored.contains(x) && ok(new ArrayList<>(adjacent(y)), x)) ||
                (!preColored.contains(x) && conservative(new ArrayList<>(adjacent(x)), new ArrayList<>(adjacent(y))))) {
            coalescedMoves.add(m);
            combine(x, y);
            add_workList(x);
        } else {
            activeMoves.add(m);
        }
    }

    public void freezeMoves(Register x) {
        nodeMoves(x).forEach(inst -> {
            Register u = inst.reg, v = inst.src, y;
            if (getAlias(x) == getAlias(v)) y = getAlias(u);
            else y = getAlias(v);
            activeMoves.remove(inst);
            frozenMoves.add(inst);
            if (nodeMoves(y).isEmpty() && degree.get(y) < K) {
                freezeWorkList.remove(y);
                simplifyWorkList.add(y);
            }
        });
    }

    public void freeze() {
        Register x = freezeWorkList.iterator().next();
        freezeWorkList.remove(x);
        simplifyWorkList.add(x);
        freezeMoves(x);
    }

    public void selectSpill() {
        Register m = null;
        double min = Double.POSITIVE_INFINITY;
        for (Register x : spillWorkList) {
            if (canNotSpillNodes.contains(x) || preColored.contains(x))
                continue; // can not choose the tmpNode made by other spillNode
            double t = weight.get(x) / degree.get(x);
            if (t < min) {
                m = x;
                min = t;
            }
        }
        spillWorkList.remove(m);
        simplifyWorkList.add(m);
        freezeMoves(m);
    }

    public void assignColors() {
        while (!selectStack.isEmpty()) {
            Register n = selectStack.pop();
            ArrayList<PReg> okColors = new ArrayList<>(asm.getColors());
            HashSet<Register> colored = new HashSet<>(preColored);
            colored.addAll(coloredNodes);
            adjList.get(n).forEach(w -> {
                if (colored.contains(getAlias(w))) okColors.remove(getAlias(w).color);
            });
            if (okColors.isEmpty()) spilledNodes.add(n);
            else {
                coloredNodes.add(n);
                n.color = okColors.get(0);
            }
        }
        for (Register x : coalescedNodes) {
            x.color = getAlias(x).color;
        }
    }

    public void build() {
        currentFunction.blocks.forEach(b -> {
            HashSet<Register> live = new HashSet<>(bliveout.get(b));
            for (int i = b.inst.size() - 1; i >= 0; i--) {
                Inst inst = b.inst.get(i);
                if (inst instanceof Mv) {
                    live.removeAll(inst.getUse());
                    HashSet<Register> t = inst.getDef();
                    t.addAll(inst.getUse());
                    t.forEach(x -> moveList.get(x).add((Mv) inst));
                    workListMoves.add((Mv) inst);
                }
                live.addAll(inst.getDef());
                inst.getDef().forEach(a -> live.forEach(c -> add_edge(a, c)));
                live.removeAll(inst.getDef());
                live.addAll(inst.getUse());
            }
        });
    }

    public void make_workList() {
        initial.forEach(x -> {
            if (degree.get(x) >= K) spillWorkList.add(x);
            else if (moveRelated(x)) freezeWorkList.add(x);
            else simplifyWorkList.add(x);
        });
    }

    public void rewriteProgram() {
        spilledNodes.forEach(v -> {
            offset.put(v, spOffset);
            spOffset += 4;
        });
        for (int i0 = 0; i0 < currentFunction.blocks.size(); i0++) {
            Block block = currentFunction.blocks.get(i0);
            for (int i = 0; i < block.inst.size(); i++) {
                Inst inst = block.inst.get(i);
                if (inst instanceof Mv && spilledNodes.contains(((Mv) inst).reg) && spilledNodes.contains(((Mv) inst).src)) {
                    VReg tmp = new VReg("tmp");
                    block.inst.set(i, new Load(tmp, asm.getPReg("sp"), new Imm(offset.get(((Mv) inst).src)), 4));
                    block.inst.add(i + 1, new Store(tmp, asm.getPReg("sp"), new Imm(offset.get(((Mv) inst).reg)), 4));
                    i++;
                    continue;
                }
                for (Register x : inst.getUse())
                    if (spilledNodes.contains(x)) {
                        if (inst instanceof Mv) {
                            block.inst.set(i, new Load(((Mv) inst).reg, asm.getPReg("sp"), new Imm(offset.get(x)), 4));
                        } else {
                            VReg tmp = new VReg("tmp");
                            canNotSpillNodes.add(tmp);
                            inst.replaceUse(x, tmp);
                            block.inst.add(i, new Load(tmp, asm.getPReg("sp"), new Imm(offset.get(x)), 4));
                            i++;
                        }
                    }
                for (Register x : inst.getDef())
                    if (spilledNodes.contains(x)) {
                        if (inst instanceof Mv) {
                            block.inst.set(i, new Store(((Mv) inst).src, asm.getPReg("sp"), new Imm(offset.get(x)), 4));
                        } else {
                            VReg tmp = new VReg("tmp");
                            canNotSpillNodes.add(tmp);
                            inst.replaceDef(x, tmp);
                            block.inst.add(i + 1, new Store(tmp, asm.getPReg("sp"), new Imm(offset.get(x)), 4));
                            i++;
                        }
                    }
            }
        }
    }

    public void runFunc(Function func) {
        currentFunction = func;
        init();
        liveness_analysis();
        build();
        make_workList();
        while (!simplifyWorkList.isEmpty() || !workListMoves.isEmpty() || !freezeWorkList.isEmpty() || !spillWorkList.isEmpty()) {
            if (!simplifyWorkList.isEmpty()) simplify();
            else if (!workListMoves.isEmpty()) coalesce();
            else if (!freezeWorkList.isEmpty()) freeze();
            else selectSpill();
        }
        assignColors();
        if (!spilledNodes.isEmpty()) {
            rewriteProgram();
            runFunc(func);
        } else {
            addSp();
            removeDeadMv();
            BlockMerge();
        }
        currentFunction = null;
    }

    public void removeDeadMv() {
        for (Block block : currentFunction.blocks) {
            for (int i = 0; i < block.inst.size(); i++) {
                Inst inst = block.inst.get(i);
                if (inst instanceof Mv && ((Mv) inst).reg.color == ((Mv) inst).src.color) {
                    block.inst.remove(i);
                    i--;
                }
            }
        }
    }

    public void BlockMerge() {
        for (int i0 = 0; i0 < currentFunction.blocks.size(); i0++) {
            Block block = currentFunction.blocks.get(i0);
            if (block.pre.size() == 1 && block.pre.get(0).getTerminator() instanceof J && ((J) block.pre.get(0).getTerminator()).dest == block) {
                Block mainBlock = block.pre.get(0);
                mainBlock.nxt = block.nxt;
                mainBlock.removeTerminator();
                mainBlock.inst.addAll(block.inst);
                for (Block b : mainBlock.nxt) {
                    for (int i = 0; i < b.pre.size(); i++) {
                        if (b.pre.get(i) == block) b.pre.set(i, mainBlock);
                    }
                }
                currentFunction.blocks.remove(i0);
                i0--;
            }
        }
    }

    public void addSp() {
        int realOffset = spOffset + Integer.max(0, currentFunction.params.size() - 8) * 4;
        if (realOffset > 0) {
            currentFunction.beginBlock.addInstFront(new Calc(asm.getPReg("sp"), "addi", asm.getPReg("sp"), new Imm(-realOffset)));
            currentFunction.endBlock.addInstBack(new Calc(asm.getPReg("sp"), "addi", asm.getPReg("sp"), new Imm(realOffset)));
        }
        for (Inst inst : currentFunction.beginBlock.inst) {
            if (inst instanceof Load && ((Load) inst).offset.inParam) {
                ((Load) inst).offset.value += spOffset;
            }
        }
    }

    public void run() {
        asm.func.forEach((s, func) -> {
            spOffset = 0;
            runFunc(func);
        });
    }
}
