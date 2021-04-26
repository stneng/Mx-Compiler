package Backend.optimizer;

import IR.Block;
import IR.Function;
import IR.IR;
import IR.inst.*;
import IR.operand.Operand;
import IR.operand.Register;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class TailRecursionOpt {
    public IR ir;

    public TailRecursionOpt(IR ir) {
        this.ir = ir;
    }

    public void doFunc(Function func) {
        Block endBlock = func.beginBlock;
        for (Block block : func.blocks) {
            for (Inst inst : block.inst) {
                if (inst instanceof Return) {
                    endBlock = block;
                    break;
                }
            }
        }
        if (endBlock.pre.size() < 2) return;
        ArrayList<Call> doList = new ArrayList<>();
        for (Block block : endBlock.pre) {
            if (block.getBack() instanceof Call && ((Call) block.getBack()).func == func) {
                doList.add((Call) block.getBack());
            }
        }
        if (doList.isEmpty()) return;
        Block beginBlock = func.beginBlock, newBeginBlock = new Block(0);
        func.beginBlock = newBeginBlock;
        newBeginBlock.addTerminator(new Jump(newBeginBlock, beginBlock));
        HashMap<Register, ArrayList<Block>> phiBlocks = new HashMap<>();
        HashMap<Register, ArrayList<Operand>> phiValues = new HashMap<>();
        for (Register x : func.params) {
            phiBlocks.put(x, new ArrayList<>(Collections.singletonList(newBeginBlock)));
            phiValues.put(x, new ArrayList<>(Collections.singletonList(x)));
        }
        for (int i = 0; i < doList.size(); i++) {
            Call inst = doList.get(i);
            inst.block.removeTerminator();
            inst.block.removeInst(inst);
            for (int j = 0; j < func.params.size(); j++) {
                Register x = func.params.get(j);
                Register t = new Register(x.type, x.name + ".TR." + (i + 1));
                inst.block.addInst(new Assign(inst.block, t, inst.param.get(j)));
                phiBlocks.get(x).add(inst.block);
                phiValues.get(x).add(t);
            }
            inst.block.addTerminator(new Jump(inst.block, beginBlock));
        }
        for (int i = 0; i < func.params.size(); i++) {
            Register x = func.params.get(i);
            Phi phi = new Phi(beginBlock, x);
            phi.blocks = phiBlocks.get(x);
            phi.values = phiValues.get(x);
            Register t = new Register(x.type, x.name + ".TR.0");
            phi.values.set(0, t);
            func.params.set(i, t);
            beginBlock.addInstFront(phi);
        }
    }

    public void run() {
        ir.func.forEach((s, x) -> doFunc(x));
    }
}
