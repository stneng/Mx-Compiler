package ASM.inst;

import ASM.Block;
import ASM.operand.Register;

import java.util.HashSet;

public class J extends Inst {
    public Block dest;

    public J(Block dest) {
        this.dest = dest;
    }

    @Override
    public HashSet<Register> getUse() {
        return new HashSet<>();
    }

    @Override
    public HashSet<Register> getDef() {
        return new HashSet<>();
    }


    @Override
    public void replaceUse(Register a, Register b) {

    }

    @Override
    public void replaceDef(Register a, Register b) {

    }

    @Override
    public String toString() {
        return "j " + dest.toString();
    }
}
