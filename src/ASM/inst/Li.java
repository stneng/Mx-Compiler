package ASM.inst;

import ASM.operand.Imm;
import ASM.operand.Register;

import java.util.Collections;
import java.util.HashSet;

public class Li extends Inst {
    public Register reg;
    public Imm imm;

    public Li(Register reg, Imm imm) {
        this.reg = reg;
        this.imm = imm;
    }

    @Override
    public HashSet<Register> getUse() {
        return new HashSet<>();
    }

    @Override
    public HashSet<Register> getDef() {
        return new HashSet<>(Collections.singletonList(reg));
    }


    @Override
    public void replaceUse(Register a, Register b) {

    }

    @Override
    public void replaceDef(Register a, Register b) {
        if (reg == a) reg = b;
    }

    @Override
    public String toString() {
        return "li " + reg + ", " + imm;
    }
}
