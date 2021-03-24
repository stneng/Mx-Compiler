package IR;

import IR.inst.Branch;
import IR.inst.Inst;
import IR.inst.Jump;
import IR.inst.Phi;
import IR.operand.Operand;
import IR.operand.Register;

import java.util.ArrayList;
import java.util.HashMap;


public class Block {
    public String name = null;
    public ArrayList<Inst> inst = new ArrayList<>();
    public ArrayList<Block> pre = new ArrayList<>();
    public ArrayList<Block> nxt = new ArrayList<>();
    public HashMap<Register, Operand> pCopy = new HashMap<>();
    public Phi branchPhi = null;
    public boolean terminated = false;
    public int loopDepth;

    public Block(int loopDepth) {
        this.loopDepth = loopDepth;
    }

    public void addInst(Inst a) {
        inst.add(a);
    }

    public void addInstFront(Inst a) {
        inst.add(0, a);
    }

    public void addInstBack(Inst a) {
        inst.add(inst.size() - 1, a);
    }

    public void removeInst(Inst a) {
        for (int i = 0; i < inst.size(); i++) {
            if (inst.get(i) == a) {
                inst.remove(i);
                return;
            }
        }
    }

    public void addTerminator(Inst a) {
        if (terminated) return;
        addInst(a);
        if (a instanceof Jump) {
            nxt.add(((Jump) a).dest);
            ((Jump) a).dest.pre.add(this);
        } else if (a instanceof Branch) {
            nxt.add(((Branch) a).trueDest);
            nxt.add(((Branch) a).falseDest);
            ((Branch) a).trueDest.pre.add(this);
            ((Branch) a).falseDest.pre.add(this);
        }
        terminated = true;
    }

    public void removeTerminator() {
        if (!terminated) return;
        inst.remove(inst.size() - 1);
        terminated = false;
    }

    public Inst getBack() {
        if (inst.size() >= 2) return inst.get(inst.size() - 2);
        else return null;
    }

    public Inst getTerminator() {
        if (!terminated) return null;
        return inst.get(inst.size() - 1);
    }

    public String toString() {
        return "%" + name;
    }
}
