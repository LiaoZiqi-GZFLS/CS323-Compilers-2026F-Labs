package impl.type;

public class ArrayType implements Type {

    private final Type elementType;

    public ArrayType(Type elementType) {
        this.elementType = elementType;
    }

    public Type getElementType() {
        return elementType;
    }

    @Override
    public String getName() {
        return elementType.getName() + "[]";
    }

    @Override
    public String toString() {
        return getName();
    }
}