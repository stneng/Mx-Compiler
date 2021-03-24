package ASM;

import ASM.operand.Register;
import ASM.operand.VReg;

import java.util.ArrayList;

public class Function {
    public String name;
    public Block beginBlock = null, endBlock = null;
    public ArrayList<Register> params = new ArrayList<>();
    public ArrayList<Block> blocks = new ArrayList<>();
    public ArrayList<VReg> calleeSaveVReg = new ArrayList<>();
    public VReg raSaveVReg = null;

    public Function(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}
