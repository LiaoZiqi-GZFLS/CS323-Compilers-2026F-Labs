package impl;

import impl.symbol.FunctionSymbol;
import impl.symbol.VariableSymbol;
import impl.type.Type;

import java.util.LinkedHashMap;
import java.util.List;

public class Scope {
    protected Scope parent;
    protected final LinkedHashMap<String, VariableSymbol> variables = new LinkedHashMap<>();
    protected final LinkedHashMap<String, FunctionSymbol> functions = new LinkedHashMap<>();

    public Scope() {
        this(null);
    }

    public Scope(Scope parent) {
        this.parent = parent;
    }

    public Scope getParent() {
        return parent;
    }

    public LinkedHashMap<String, VariableSymbol> getVariables() {
        return variables;
    }

    public LinkedHashMap<String, FunctionSymbol> getFunctions() {
        return functions;
    }

    // define variable
    public VariableSymbol defineVariable(String name, Type type) {
        //TODO:
        //If the current scope does not contain Variable, create one and add it to the current Scope;
        // otherwise, return null.
        if (variables.containsKey(name)) {
            return null;
        } else {
            variables.put(name, new VariableSymbol(name, type));
            return variables.get(name);
        }
    }

    // define function
    public FunctionSymbol defineFunction(String name, Type returnType, List<VariableSymbol> params) {
        // TODO：
        // If the current scope does not contain a Function, create one and add it to the current Scope;
        // otherwise, return null
        if (functions.containsKey(name)) {
            return null;
        } else {
            FunctionSymbol functionSymbol = new FunctionSymbol(name, returnType, params, this);
            functions.put(name, functionSymbol);
            return functions.get(name);
        }
    }

    public VariableSymbol lookupVariable(String name) {
        // TODO:
        //Find name in the current scope; if it exists, return the corresponding VariableSymbol.
        // If it does not exist, search in the parent scope
        // If it cannot be found, return null.
        if (variables.containsKey(name)) {
            return variables.get(name);
        } else {
            if (parent != null) {
                return parent.lookupVariable(name);
            }
            return null;
        }
    }

    public VariableSymbol lookupCurrentVariable(String name) {
        return variables.getOrDefault(name, null);
    }

    public FunctionSymbol lookupFunction(String name) {
        //TODO:
        //Find name in the current scope; if it exists, return the corresponding FunctionSymbol.
        // If it does not exist, search in the parent scope
        // If it cannot be found, return null.
        if (functions.containsKey(name)) {
            return functions.get(name);
        } else {
            if (parent != null) {
                return parent.lookupFunction(name);
            }
            return null;
        }
    }
}
