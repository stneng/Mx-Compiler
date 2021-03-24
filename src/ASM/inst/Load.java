package ASM.inst;

import ASM.operand.Imm;
import ASM.operand.Register;

import java.util.Collections;
import java.util.HashSet;

public class Load extends Inst {
    public Register reg, address;
    public Imm offset;
    public int size;

    public Load(Register reg, Register address, Imm offset, int size) {
        this.reg = reg;
        this.address = address;
        this.offset = offset;
        this.size = size;
    }

    @Override
    public HashSet<Register> getUse() {
        return new HashSet<>(Collections.singletonList(address));
    }

    @Override
    public HashSet<Register> getDef() {
        return new HashSet<>(Collections.singletonList(reg));
    }


    @Override
    public void replaceUse(Register a, Register b) {
        if (address == a) address = b;
    }

    @Override
    public void replaceDef(Register a, Register b) {
        if (reg == a) reg = b;
    }

    @Override
    public String toString() {
        return "lw " + reg.toString() + ", " + offset.toString() + "(" + address.toString() + ")";
    }
}
