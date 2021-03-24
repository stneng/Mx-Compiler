package IR.inst;

import IR.Block;
import IR.Function;
import IR.operand.Operand;
import IR.operand.Register;
import IR.type.VoidType;

import java.util.ArrayList;

public class Call extends Inst {
    public Function func;
    public ArrayList<Operand> param = new ArrayList<>();

    public Call(Block block, Register reg, Function func) {
        super(block, reg);
        this.func = func;
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder();
        if (!func.returnType.equals(new VoidType())) ans.append(reg.toString()).append(" = ");
        ans.append("call ").append(func.returnType.toString()).append(" ").append(func.toString()).append("(");
        for (int i = 0; i < param.size(); i++) {
            ans.append(param.get(i).type.toString()).append(" ").append(param.get(i).toString());
            if (i != param.size() - 1) ans.append(", ");
        }
        ans.append(")");
        return ans.toString();
    }

    @Override
    public ArrayList<Operand> getUseOperand() {
        return param;
    }

    @Override
    public void replace(Operand a, Operand b) {
        for (int i = 0; i < param.size(); i++) {
            if (param.get(i) == a) {
                param.set(i, b);
            }
        }
    }
}
