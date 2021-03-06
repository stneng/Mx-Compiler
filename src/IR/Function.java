package IR;

import IR.inst.Return;
import IR.operand.Operand;
import IR.operand.Register;
import IR.type.BaseType;
import IR.type.VoidType;

import java.util.ArrayList;
import java.util.HashSet;

public class Function {
    public String name;
    public Block beginBlock = new Block(0);
    public ArrayList<Block> blocks = new ArrayList<>();
    public ArrayList<Register> params = new ArrayList<>();
    public HashSet<Register> vars = new HashSet<>();
    public BaseType returnType = new VoidType();
    public ArrayList<Return> returnInst = new ArrayList<>();
    public boolean inClass = false;
    public Operand classPtr = null;

    public Function(String name) {
        this.name = name;
    }

    public String toString() {
        return "@" + name;
    }
}
