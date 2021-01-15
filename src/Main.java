import AST.ProgramNode;
import Frontend.ASTBuilder;
import Frontend.SemanticChecker;
import Frontend.SymbolCollector;
import Frontend.TypeCollector;
import Parser.MxLexer;
import Parser.MxParser;
import Util.MxErrorListener;
import Util.error.Error;
import Util.symbol.Scope;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.InputStream;

public class Main {
    public static void main(String[] args) throws Exception {

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
        } catch (Error er) {
            System.err.println(er.toString());
            throw new RuntimeException();
        }
    }
}