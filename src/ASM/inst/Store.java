package ASM.inst;

import ASM.operand.Imm;
import ASM.operand.Register;

import java.util.Arrays;
import java.util.HashSet;

public class Store extends Inst {
    public Register value, address;
    public Imm offset;
    public int size;

    public Store(Register value, Register address, Imm offset, int size) {
        this.value = value;
        this.address = address;
        this.offset = offset;
        this.size = size;
    }

    @Override
    public HashSet<Register> getUse() {
        return new HashSet<>(Arrays.asList(value, address));
    }

    @Override
    public HashSet<Register> getDef() {
        return new HashSet<>();
    }


    @Override
    public void replaceUse(Register a, Register b) {
        if (value == a) value = b;
        if (address == a) address = b;
    }

    @Override
    public void replaceDef(Register a, Register b) {

    }

    @Override
    public String toString() {
        return "sw " + value.toString() + ", " + offset.toString() + "(" + address.toString() + ")";
    }
}
