package impl.symbol;

import impl.Scope;
import impl.type.Type;

import java.util.ArrayList;
import java.util.List;

public class FunctionSymbol extends Scope implements Symbol {
    private final String name;
    private final Type returnType;
    private final List<VariableSymbol> parameters;
    static StringBuilder sb = new StringBuilder();

    public FunctionSymbol(String name, Type returnType, Scope parent) {
        super(parent);
        this.name = name;
        this.returnType = returnType;
        this.parameters = new ArrayList<>();
    }

    public FunctionSymbol(String name, Type returnType, List<VariableSymbol> parameters, Scope parent) {
        super(parent);
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Type getType() {
        return this.returnType;
    }

    public void addParameter(VariableSymbol param) {
        parameters.add(param);
        //将参数添加到Scope里
        defineVariable(param.getName(), param.getType());
    }

    public String signature() {
        sb.setLength(0);
        sb.append(name).append(": ").append(returnType.getName()).append('(');
        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(parameters.get(i).getType().getName());
        }
        sb.append(')');
        return sb.toString();
    }

    public String toString() {
        return signature();
    }

}
