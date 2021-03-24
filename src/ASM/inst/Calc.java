package ASM.inst;

import ASM.operand.Operand;
import ASM.operand.Register;

import java.util.Collections;
import java.util.HashSet;

public class Calc extends Inst {
    public Register rd;
    public String op;
    public Operand rs1, rs2;

    public Calc(Register rd, String op, Operand rs1, Operand rs2) {
        this.rd = rd;
        this.op = op;
        this.rs1 = rs1;
        this.rs2 = rs2;
    }

    @Override
    public HashSet<Register> getUse() {
        HashSet<Register> ans = new HashSet<>();
        if (rs1 instanceof Register) ans.add((Register) rs1);
        if (rs2 instanceof Register) ans.add((Register) rs2);
        return ans;
    }

    @Override
    public HashSet<Register> getDef() {
        return new HashSet<>(Collections.singletonList(rd));
    }

    @Override
    public void replaceUse(Register a, Register b) {
        if (rs1 == a) rs1 = b;
        if (rs2 == a) rs2 = b;
    }

    @Override
    public void replaceDef(Register a, Register b) {
        if (rd == a) rd = b;
    }

    @Override
    public String toString() {
        return op + " " + rd.toString() + ", " + rs1.toString() + ", " + rs2.toString();
    }
}
