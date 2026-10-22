package impl.type;

public class ArrayType implements Type {

    private final Type elementType;
    private final int length;

    public ArrayType(Type elementType, int length) {
        this.elementType = elementType;
        this.length = length;
    }
    public Type getElementType() {
        return elementType;
    }
    @Override
    public String toString() {
        return String.format("%s[%d]", elementType, length);
    }

    @Override
    public String getName() {
        return this.toString();
    }
    public int getLength() {
        return length;
    }

}