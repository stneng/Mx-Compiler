package ASM.inst;

import ASM.operand.Register;

import java.util.HashSet;

public abstract class Inst {
    public abstract HashSet<Register> getUse();

    public abstract HashSet<Register> getDef();

    public abstract void replaceUse(Register a, Register b);

    public abstract void replaceDef(Register a, Register b);

    public abstract String toString();
}
