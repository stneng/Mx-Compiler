import ASM.ASM;
import AST.ProgramNode;
import AST.symbol.Scope;
import Backend.*;
import Frontend.ASTBuilder;
import Frontend.SemanticChecker;
import Frontend.SymbolCollector;
import Frontend.TypeCollector;
import IR.IR;
import Parser.MxLexer;
import Parser.MxParser;
import Util.MxErrorListener;
import Util.error.Error;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.InputStream;

public class Main {
    public static void main(String[] args) throws Exception {
        boolean codegen = true, optimize = false;
        if (args.length > 0)
            for (String arg : args) {
                switch (arg) {
                    case "-semantic" -> codegen = false;
                    case "-codegen" -> codegen = true;
                    case "-optimize" -> optimize = true;
                }
            }

        InputStream input = System.in;

        try {
            ProgramNode ASTRoot;
            MxLexer lexer = new MxLexer(CharStreams.fromStream(input));
            lexer.removeErrorListeners();
            lexer.addErrorListener(new MxErrorListener());
            MxParser parser = new MxParser(new CommonTokenStream(lexer));
            parser.removeErrorListeners();
            parser.addErrorListener(new MxErrorListener());
            ParseTree parseTreeRoot = parser.program();
            ASTBuilder astBuilder = new ASTBuilder();
            ASTRoot = (ProgramNode) astBuilder.visit(parseTreeRoot);
            Scope global = new Scope(null);
            new SymbolCollector(global).visit(ASTRoot);
            new TypeCollector(global).visit(ASTRoot);
            global.varMap.clear();
            new SemanticChecker(global).visit(ASTRoot);
            if (!codegen) return;
            IR ir = new IR();
            new IRBuilder(ir).visit(ASTRoot);
            new IRBuilder(ir).run();
            // new IRPrinter(System.out, ir).prt();
            ASM asm = new ASM();
            new PhiEliminate(ir).run();
            new ASMBuilder(ir, asm).run();
            new RegAllocator(asm).run();
            new ASMPrinter(System.out, asm).prt();
        } catch (Error er) {
            System.err.println(er.toString());
            throw new RuntimeException();
        }
    }
}