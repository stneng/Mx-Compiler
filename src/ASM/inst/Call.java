package ASM.inst;

import ASM.ASM;
import ASM.Function;
import ASM.operand.Register;

import java.util.HashSet;

public class Call extends Inst {
    public Function func;
    public ASM asm;

    public Call(Function func, ASM asm) {
        this.func = func;
        this.asm = asm;
    }

    @Override
    public HashSet<Register> getUse() {
        HashSet<Register> ans = new HashSet<>();
        for (int i = 0; i < Integer.min(func.params.size(), 8); i++) {
            ans.add(asm.getPReg(10 + i));
        }
        return ans;
    }

    @Override
    public HashSet<Register> getDef() {
        return new HashSet<>(asm.getCallerSave());
    }

    @Override
    public void replaceUse(Register a, Register b) {

    }

    @Override
    public void replaceDef(Register a, Register b) {

    }

    @Override
    public String toString() {
        return "call " + func.toString();
    }
}
