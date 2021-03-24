package ASM.inst;

import ASM.ASM;
import ASM.operand.Register;

import java.util.Collections;
import java.util.HashSet;

public class Ret extends Inst {
    public ASM asm;

    public Ret(ASM asm) {
        this.asm = asm;
    }

    @Override
    public HashSet<Register> getUse() {
        return new HashSet<>(Collections.singletonList(asm.getPReg("ra")));
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
        return "ret";
    }
}
