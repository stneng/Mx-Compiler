package ASM;

import ASM.inst.Inst;

import java.util.ArrayList;

public class Block {
    public String name = null;
    public ArrayList<Inst> inst = new ArrayList<>();
    public ArrayList<Block> pre = new ArrayList<>();
    public ArrayList<Block> nxt = new ArrayList<>();
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

    public Inst getTerminator() {
        return inst.get(inst.size() - 1);
    }

    public void removeTerminator() {
        inst.remove(inst.size() - 1);
    }

    public String toString() {
        return name;
    }
}
