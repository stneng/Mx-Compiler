package ASM.inst;

import ASM.operand.Register;

import java.util.Collections;
import java.util.HashSet;

public class Mv extends Inst {
    public Register reg, src;

    public Mv(Register reg, Register src) {
        this.reg = reg;
        this.src = src;
    }

    @Override
    public HashSet<Register> getUse() {
        return new HashSet<>(Collections.singletonList(src));
    }

    @Override
    public HashSet<Register> getDef() {
        return new HashSet<>(Collections.singletonList(reg));
    }

    @Override
    public void replaceUse(Register a, Register b) {
        if (src == a) src = b;
    }

    @Override
    public void replaceDef(Register a, Register b) {
        if (reg == a) reg = b;
    }

    @Override
    public String toString() {
        return "mv " + reg.toString() + ", " + src.toString();
    }
}
