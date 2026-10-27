import generated.Splc.SplcBaseListener;
import generated.Splc.SplcParser;
import impl.Scope;
import impl.SplcSemanticError;
import impl.symbol.FunctionSymbol;
import impl.symbol.VariableSymbol;
import impl.type.ArrayType;
import impl.type.PrimitiveType;
import impl.type.Type;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;

public class SyntaxListener extends SplcBaseListener {
    private Scope globalScope = new Scope(null);
    private Scope currentScope = globalScope;

    private final List<SplcSemanticError> errors = new ArrayList<>();

    public Scope getGlobalScope() {
        return globalScope;
    }

    public Scope getCurrentScope() {
        return currentScope;
    }

    public List<SplcSemanticError> getErrors() {
        return errors;
    }

    @Override
    public void enterGlobalDef(SplcParser.GlobalDefContext ctx) {
        if (ctx.LBRACE() != null && ctx.RBRACE() != null) {
            visitFuncDef(ctx);
        } else {
            visitVariableDec(ctx);
        }
    }

    @Override
    public void exitGlobalDef(SplcParser.GlobalDefContext ctx) {
        if (ctx.LBRACE() != null && ctx.RBRACE() != null) {
            this.currentScope = this.currentScope.getParent();
        }
    }

    @Override
    public void enterCombineStmt(SplcParser.CombineStmtContext ctx) {
        this.currentScope = new Scope(this.currentScope);
    }

    @Override
    public void exitCombineStmt(SplcParser.CombineStmtContext ctx) {
        this.currentScope = this.currentScope.getParent();
    }

    @Override
    public void enterLocalVariableStmt(SplcParser.LocalVariableStmtContext ctx) {
        Type baseType = resolveType(ctx.specifier());
        SplcParser.VarDecContext varDec = ctx.varDec();
        TerminalNode identifier = getVarIdentifier(varDec);
        Type type = getVarType(baseType, varDec);

        VariableSymbol variable = this.currentScope.defineVariable(identifier.getText(), type);
        if (variable == null) {
            errors.add(SplcSemanticError.redeclaration(identifier));
            return;
        }

        if (ctx.expression() != null) {
            Type actual = getExpressionType(ctx.expression());
            if (actual != null && !isAssignable(type, actual)) {
                errors.add(SplcSemanticError.incompatibleType(identifier, type, actual));
            }
        }
    }

    @Override
    public void enterExpression(SplcParser.ExpressionContext ctx) {
        if (ctx.Identifier() != null) {
            TerminalNode identifier = ctx.Identifier();
            if (this.currentScope.lookupVariable(identifier.getText()) == null) {
                errors.add(SplcSemanticError.undeclaredUse(identifier));
            }
            return;
        }

        if (ctx.ASSIGN() != null) {
            Type expect = getExpressionType(ctx.expression(0));
            Type actual = getExpressionType(ctx.expression(1));
            TerminalNode identifier = getFirstIdentifier(ctx.expression(0));
            if (expect != null && actual != null && identifier != null && !isAssignable(expect, actual)) {
                errors.add(SplcSemanticError.incompatibleType(identifier, expect, actual));
            }
        }
    }

    private void visitVariableDec(SplcParser.GlobalDefContext ctx) {
        Type baseType = resolveType(ctx.specifier());
        SplcParser.VarDecContext varDec = ctx.varDec();
        TerminalNode identifier = getVarIdentifier(varDec);
        if (this.globalScope.defineVariable(identifier.getText(), getVarType(baseType, varDec)) == null) {
            errors.add(SplcSemanticError.redefinition(identifier));
        }
    }

    private void visitFuncDef(SplcParser.GlobalDefContext ctx) {
        Type returnType = resolveType(ctx.specifier());
        TerminalNode nameNode = ctx.Identifier();
        List<SplcParser.SpecifierContext> paramTypes = ctx.funcArgs().specifier();
        List<TerminalNode> paramNames = ctx.funcArgs().Identifier();

        FunctionSymbol function = this.globalScope.defineFunction(nameNode.getText(), returnType, new ArrayList<>());
        if (function == null) {
            errors.add(SplcSemanticError.redeclaration(nameNode));
            function = new FunctionSymbol(nameNode.getText(), returnType, this.globalScope);
        }
        for (int i = 0; i < paramNames.size(); i++) {
            VariableSymbol param = function.defineVariable(paramNames.get(i).getText(), resolveType(paramTypes.get(i)));
            if (param == null) {
                errors.add(SplcSemanticError.redeclaration(paramNames.get(i)));
            } else {
                function.addParameter(param);
            }
        }
        this.currentScope = function;
    }

    private TerminalNode getVarIdentifier(SplcParser.VarDecContext ctx) {
        if (ctx.Identifier() != null) {
            return ctx.Identifier();
        }
        return getVarIdentifier(ctx.varDec());
    }

    private Type getVarType(Type baseType, SplcParser.VarDecContext ctx) {
        if (ctx.Identifier() != null) {
            return baseType;
        }
        List<Integer> dimensions = new ArrayList<>();
        SplcParser.VarDecContext current = ctx;
        while (current.Identifier() == null) {
            dimensions.add(Integer.parseInt(current.NUMBER().getText()));
            current = current.varDec();
        }
        Type type = baseType;
        for (int dimension : dimensions) {
            type = new ArrayType(type, dimension);
        }
        return type;
    }

    private Type getExpressionType(SplcParser.ExpressionContext ctx) {
        if (ctx.NUMBER() != null) {
            return PrimitiveType.INT;
        }
        if (ctx.CHAR_VALUE() != null) {
            return PrimitiveType.CHAR;
        }
        if (ctx.Identifier() != null) {
            VariableSymbol variable = this.currentScope.lookupVariable(ctx.Identifier().getText());
            return variable == null ? null : variable.getType();
        }
        if (ctx.LBRACK() != null) {
            Type base = getExpressionType(ctx.expression(0));
            return base instanceof ArrayType ? ((ArrayType) base).getElementType() : base;
        }
        if (ctx.LPAREN() != null) {
            return getExpressionType(ctx.expression(0));
        }
        if (ctx.ASSIGN() != null) {
            return getExpressionType(ctx.expression(0));
        }
        if (ctx.STAR() != null || ctx.DIV() != null || ctx.PLUS() != null || ctx.MINUS() != null) {
            Type left = getExpressionType(ctx.expression(0));
            Type right = getExpressionType(ctx.expression(1));
            return left == null || right == null ? null : PrimitiveType.INT;
        }
        return null;
    }

    private TerminalNode getFirstIdentifier(SplcParser.ExpressionContext ctx) {
        if (ctx.Identifier() != null) {
            return ctx.Identifier();
        }
        if (ctx.expression().isEmpty()) {
            return null;
        }
        return getFirstIdentifier(ctx.expression(0));
    }

    private boolean isAssignable(Type expect, Type actual) {
        // SPLC arrays are not first-class values: a whole array cannot be assigned.
        return !(expect instanceof ArrayType) && !(actual instanceof ArrayType);
    }

    private Type resolveType(SplcParser.SpecifierContext ctx) {
        if (ctx.INT() != null) {
            return PrimitiveType.INT;
        } else if (ctx.CHAR() != null) {
            return PrimitiveType.CHAR;
        } else {
            return PrimitiveType.VOID;
        }
    }
}
