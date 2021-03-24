package IR.inst;

import IR.Block;
import IR.operand.Operand;
import IR.operand.Register;

import java.util.ArrayList;

public class Binary extends Inst {
    public String op;
    public Operand src1, src2;

    public Binary(Block block, Register reg, String op, Operand src1, Operand src2) {
        super(block, reg);
        this.op = op;
        this.src1 = src1;
        this.src2 = src2;
    }

    @Override
    public String toString() {
        return reg.toString() + " = " + op + " " + src1.type.toString() + " " + src1.toString() + ", " + src2.toString();
    }

    @Override
    public ArrayList<Operand> getUseOperand() {
        ArrayList<Operand> ans = new ArrayList<>();
        ans.add(src1);
        ans.add(src2);
        return ans;
    }

    @Override
    public void replace(Operand a, Operand b) {
        if (src1 == a) src1 = b;
        if (src2 == a) src2 = b;
    }
}
