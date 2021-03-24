package Backend;

import IR.Block;
import IR.Function;
import IR.IR;
import IR.operand.ConstStr;
import IR.operand.Register;
import IR.type.ClassType;
import IR.type.Pointer;

import java.io.PrintStream;

public class IRPrinter {
    public PrintStream prt;
    public IR ir;
    public String builtin = """
            declare i8* @__mx_builtin_malloc(i32)
            declare void @__mx_builtin_print(i8*)
            declare void @__mx_builtin_println(i8*)
            declare void @__mx_builtin_printInt(i32)
            declare void @__mx_builtin_printlnInt(i32)
            declare i8* @__mx_builtin_getString()
            declare i32 @__mx_builtin_getInt()
            declare i8* @__mx_builtin_toString(i32)
            declare i32 @__mx_builtin_str_length(i8*)
            declare i8* @__mx_builtin_str_substring(i8*, i32, i32)
            declare i32 @__mx_builtin_str_parseInt(i8*)
            declare i32 @__mx_builtin_str_ord(i8*, i32)
            declare i8* @__mx_builtin_str_add(i8*, i8*)
            declare i1 @__mx_builtin_str_lt(i8*, i8*)
            declare i1 @__mx_builtin_str_gt(i8*, i8*)
            declare i1 @__mx_builtin_str_le(i8*, i8*)
            declare i1 @__mx_builtin_str_ge(i8*, i8*)
            declare i1 @__mx_builtin_str_eq(i8*, i8*)
            declare i1 @__mx_builtin_str_ne(i8*, i8*)
            """;

    public IRPrinter(PrintStream prt, IR ir) {
        this.prt = prt;
        this.ir = ir;
    }

    public void prtBlock(Block block) {
        prt.println(block.name + ":");
        block.inst.forEach(x -> prt.println("  " + x.toString()));
    }

    public void prtClass(ClassType c) {
        prt.print(c.toString() + " = type {");
        for (int i = 0; i < c.var.size(); i++) {
            prt.print(c.var.get(i).type.toString());
            if (i != c.var.size() - 1) prt.print(", ");
        }
        prt.println("}");
    }

    public void prtFunc(Function func) {
        prt.print("define " + func.returnType.toString() + " " + func.toString() + "(");
        for (int i = 0; i < func.params.size(); i++) {
            prt.print(func.params.get(i).type.toString() + " " + func.params.get(i).toString());
            if (i != func.params.size() - 1) prt.print(", ");
        }
        prt.println("){");
        func.blocks.forEach(this::prtBlock);
        prt.println("}");
    }

    public void prtGVar(Register x) {
        prt.println(x.toString() + " = global " + ((Pointer) x.type).pointType.toString() + " zeroinitializer");
    }

    public void prtConstStr(ConstStr x) {
        prt.println("@" + x.name + " = private unnamed_addr constant [" + (x.realValue.length() + 1) + " x i8] c\"" + x.convert() + "\\00\", align 1");
    }

    public void prt() {
        prt.print(builtin);
        ir.mxClass.forEach((s, x) -> prtClass(x));
        ir.gVar.forEach((s, x) -> prtGVar(x));
        ir.constStr.forEach((s, x) -> prtConstStr(x));
        ir.func.forEach((s, x) -> prtFunc(x));
    }
}
