package ASM.inst;

import ASM.Block;
import ASM.operand.Register;

import java.util.Arrays;
import java.util.HashSet;

public class Branch extends Inst {
    public String op;
    public Register rs1, rs2;
    public Block dest;

    public Branch(String op, Register rs1, Register rs2, Block dest) {
        this.op = op;
        this.rs1 = rs1;
        this.rs2 = rs2;
        this.dest = dest;
    }

    @Override
    public HashSet<Register> getUse() {
        return new HashSet<>(Arrays.asList(rs1, rs2));
    }

    @Override
    public HashSet<Register> getDef() {
        return new HashSet<>();
    }

    @Override
    public void replaceUse(Register a, Register b) {
        if (rs1 == a) rs1 = b;
        if (rs2 == a) rs2 = b;
    }

    @Override
    public void replaceDef(Register a, Register b) {

    }

    @Override
    public String toString() {
        return op + " " + rs1.toString() + ", " + rs2.toString() + ", " + dest.toString();
    }
}
