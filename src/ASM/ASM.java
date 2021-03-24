package ASM;

import ASM.operand.PReg;

import java.util.ArrayList;
import java.util.HashMap;

public class ASM {
    public HashMap<String, Function> func = new HashMap<>();
    public HashMap<String, IR.operand.Register> gVar = new HashMap<>();
    public HashMap<String, IR.operand.ConstStr> constStr = new HashMap<>();
    public HashMap<String, PReg> PRegNameMap = new HashMap<>();
    public HashMap<Integer, PReg> PRegIdMap = new HashMap<>();
    public String[] regName = new String[]{"zero", "ra", "sp", "gp", "tp", "t0", "t1", "t2", "s0", "s1", "a0", "a1", "a2", "a3", "a4", "a5", "a6", "a7", "s2", "s3", "s4", "s5", "s6", "s7", "s8", "s9", "s10", "s11", "t3", "t4", "t5", "t6"};

    public ASM() {
        for (int i = 0; i < 32; i++) {
            PReg a = new PReg(regName[i]);
            PRegNameMap.put(regName[i], a);
            PRegIdMap.put(i, a);
        }
    }

    public PReg getPReg(String name) {
        return PRegNameMap.get(name);
    }

    public PReg getPReg(int id) {
        return PRegIdMap.get(id);
    }

    public ArrayList<PReg> getCallerSave() {
        ArrayList<PReg> ans = new ArrayList<>();
        for (int i = 1; i <= 1; i++) ans.add(getPReg(i));
        for (int i = 5; i <= 7; i++) ans.add(getPReg(i));
        for (int i = 10; i <= 17; i++) ans.add(getPReg(i));
        for (int i = 28; i <= 31; i++) ans.add(getPReg(i));
        return ans;
    }

    public ArrayList<PReg> getCalleeSave() {
        ArrayList<PReg> ans = new ArrayList<>();
        for (int i = 8; i <= 9; i++) ans.add(getPReg(i));
        for (int i = 18; i <= 27; i++) ans.add(getPReg(i));
        return ans;
    }

    public ArrayList<PReg> getColors() {
        ArrayList<PReg> ans = new ArrayList<>();
        for (int i = 5; i <= 7; i++) ans.add(getPReg(i));
        for (int i = 10; i <= 17; i++) ans.add(getPReg(i));
        for (int i = 28; i <= 31; i++) ans.add(getPReg(i));
        for (int i = 8; i <= 9; i++) ans.add(getPReg(i));
        for (int i = 18; i <= 27; i++) ans.add(getPReg(i));
        for (int i = 1; i <= 1; i++) ans.add(getPReg(i));
        return ans;
    }

    public ArrayList<PReg> getPreg() {
        ArrayList<PReg> ans = new ArrayList<>();
        for (int i = 0; i < 32; i++) ans.add(getPReg(i));
        return ans;
    }
}
