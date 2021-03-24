package IR.inst;

import IR.Block;
import IR.operand.Operand;
import IR.operand.Register;

import java.util.ArrayList;

public class Load extends Inst {
    public Operand address;

    public Load(Block block, Register reg, Operand address) {
        super(block, reg);
        this.address = address;
    }

    @Override
    public String toString() {
        return reg.toString() + " = load " + reg.type.toString() + ", " + address.type.toString() + " " + address.toString();
    }

    @Override
    public ArrayList<Operand> getUseOperand() {
        ArrayList<Operand> ans = new ArrayList<>();
        ans.add(address);
        return ans;
    }

    @Override
    public void replace(Operand a, Operand b) {
        if (address == a) address = b;
    }
}
