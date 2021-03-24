package Backend;

import ASM.ASM;
import ASM.Block;
import ASM.Function;

import java.io.PrintStream;

public class ASMPrinter {
    public PrintStream prt;
    public ASM asm;

    public ASMPrinter(PrintStream prt, ASM asm) {
        this.prt = prt;
        this.asm = asm;
    }

    public void prtBlock(Block block) {
        prt.println(block.name + ":");
        block.inst.forEach(inst -> prt.println("\t" + inst.toString()));
    }

    public void prtFunc(Function func) {
        prt.println("\t.globl\t" + func.toString());
        prt.println("\t.type\t" + func.toString() + ", @function");
        prt.println(func.name + ":");
        func.blocks.forEach(this::prtBlock);
        prt.println("\t.size\t" + func.toString() + ", .-" + func.toString());
    }

    public void prtGVar(IR.operand.Register x) {
        prt.println("\t.globl\t" + x.name);
        prt.println("\t.type\t" + x.name + ", @object");
        prt.println(x.name + ":");
        prt.println("\t.zero\t4");
        prt.println("\t.size\t" + x.name + ", 4");
    }

    public void prtConstStr(IR.operand.ConstStr x) {
        prt.println(x.name + ":");
        prt.println("\t.string\t\"" + x.value + "\"");
    }

    public void prt() {
        prt.println("\t.text");
        asm.func.forEach((s, x) -> prtFunc(x));
        prt.println("\t.section\t.bss");
        asm.gVar.forEach((s, x) -> prtGVar(x));
        prt.println("\t.section\t.rodata");
        asm.constStr.forEach((s, x) -> prtConstStr(x));
    }
}
