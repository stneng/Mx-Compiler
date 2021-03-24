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

    public String toString() {
        return name;
    }
}
