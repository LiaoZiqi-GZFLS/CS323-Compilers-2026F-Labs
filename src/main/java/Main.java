import generated.Splc.SplcLexer;
import generated.Splc.SplcParser;
import impl.Scope;
import impl.SplcSemanticError;
import impl.symbol.FunctionSymbol;
import impl.symbol.VariableSymbol;
import org.antlr.v4.runtime.*;

import java.io.IOException;
import java.util.List;


public class Main {
    public static void main(String[] args) {
        CharStream input = null;
        try {
            input = CharStreams.fromFileName("testcases/symbol/base3.splc");
            SplcLexer lexer = new SplcLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            SplcParser parser = new SplcParser(tokens);
            SplcParser.ProgramContext program = parser.program();
            Lab6Visitor visitor = new Lab6Visitor();
            visitor.visit(program);
            List<SplcSemanticError> errors = visitor.getErrors();
            if(errors.isEmpty()) {
                Scope globalScope = visitor.getGlobalScope();
                printGlobalDeclarations(globalScope);
            }else{
                for(SplcSemanticError error : errors) {
                    System.err.println(error.printBase());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void printGlobalDeclarations(Scope globalScope) {
        System.out.println("Variables:");
        for (VariableSymbol v : globalScope.getVariables().values()) {
            System.out.println(v.getName() + ": " + v.getType());
        }
        System.out.println();
        System.out.println("Functions:");
        for (FunctionSymbol f : globalScope.getFunctions().values()) {
            System.out.println(f);
        }
    }
}