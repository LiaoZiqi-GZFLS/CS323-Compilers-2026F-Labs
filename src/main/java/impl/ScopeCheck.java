package impl;

import impl.symbol.FunctionSymbol;
import impl.symbol.VariableSymbol;
import impl.type.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Scope self-check (no JUnit required).
 *
 * Run the main method of this class:
 *   - each assertion prints a PASS / FAIL line; a FAIL line also shows expected / actual;
 *   - the summary at the end prints how many checks passed / failed.
 *
 * Scope nesting: at most 3 levels, at least 1 level (current scope only).
 *   global(level 1) -> blockA(level 2) -> blockB(level 3)
 *
 * 3 groups of checks for variables and 3 for functions.
 */
public class ScopeCheck {

    private static int passed = 0;
    private static int failed = 0;

    /** Minimal test type, so we do not depend on a concrete PrimitiveType class. */
    private enum TestType implements Type {
        INT, CHAR, VOID;

        @Override
        public String getName() {
            return name().toLowerCase();
        }
    }

    public static void main(String[] args) {
        System.out.println("======== Scope self-check (max 3 levels, min 1) ========");

        System.out.println("\n-- Variable --");
        v1_defineAndLookupInCurrentScope();
        v2_lookupVariableWalksUpParentChain();
        v3_shadowingAcrossScopes();

        System.out.println("\n-- Function --");
        f1_defineAndLookupFunctionInCurrentScope();
        f2_lookupFunctionWalksUpParentChain();
        f3_functionParentShouldBeDefiningScope();

        System.out.println("\n--------------------------------------------------------");
        System.out.printf("Result: %d passed, %d failed%n", passed, failed);
        System.out.println("========================================================");
    }

    // ==================================================================
    // Variables
    // ==================================================================

    /** V1: current level only (1 level) - define then lookup, duplicate returns null, undefined returns null. */
    static void v1_defineAndLookupInCurrentScope() {
        Scope current = new Scope(); // minimum nesting: current level only

        VariableSymbol a = current.defineVariable("a", TestType.INT);
        checkNotNull("V1.1 define returns non-null", a);
        check("V1.2 variable name", "a", a == null ? null : a.getName());
        check("V1.3 variable type", TestType.INT, a == null ? null : a.getType());
        checkSame("V1.4 lookup in current scope returns the same object", a, current.lookupVariable("a"));

        // Redefinition in the same scope returns null and must not overwrite the original
        checkNull("V1.5 redefinition returns null", current.defineVariable("a", TestType.CHAR));
        check("V1.6 original definition is not overwritten", TestType.INT,
                current.lookupVariable("a") == null ? null : current.lookupVariable("a").getType());

        // Undefined name returns null
        checkNull("V1.7 undefined variable returns null", current.lookupVariable("notExist"));
    }

    /** V2: 3 levels - a variable is visible to inner scopes via the parent chain, not to outer scopes. */
    static void v2_lookupVariableWalksUpParentChain() {
        Scope[] s = buildThreeLevels();
        Scope global = s[0];
        Scope blockA = s[1];
        Scope blockB = s[2];

        global.defineVariable("gv", TestType.INT);   // level 1
        blockA.defineVariable("mv", TestType.CHAR);  // level 2
        blockB.defineVariable("iv", TestType.INT);   // level 3

        // From the innermost level, variables defined on all three levels resolve
        check("V2.1 innermost sees level-1 gv", "gv", nameOf(blockB.lookupVariable("gv")));
        check("V2.2 innermost sees level-2 mv", "mv", nameOf(blockB.lookupVariable("mv")));
        check("V2.3 innermost sees level-3 iv", "iv", nameOf(blockB.lookupVariable("iv")));

        // Outer scopes cannot see inner definitions
        checkNull("V2.4 level-1 cannot see level-2 mv", global.lookupVariable("mv"));
        checkNull("V2.5 level-1 cannot see level-3 iv", global.lookupVariable("iv"));
        checkNull("V2.6 level-2 cannot see level-3 iv", blockA.lookupVariable("iv"));
    }

    /** V3: 3 levels - an inner same-named variable shadows the outer one, scopes do not pollute each other. */
    static void v3_shadowingAcrossScopes() {
        Scope[] s = buildThreeLevels();
        Scope global = s[0];
        Scope blockA = s[1];
        Scope blockB = s[2];

        global.defineVariable("x", TestType.INT);                          // outer int
        VariableSymbol shadow = blockB.defineVariable("x", TestType.CHAR); // inner char
        checkNotNull("V3.1 same name allowed in a different scope (shadowing)", shadow);

        checkSame("V3.2 innermost resolves to its own definition", shadow, blockB.lookupVariable("x"));
        check("V3.3 innermost type is char", TestType.CHAR,
                typeOf(blockB.lookupVariable("x")));
        check("V3.4 middle level walks up to the global int", TestType.INT,
                typeOf(blockA.lookupVariable("x")));
        check("V3.5 global level keeps its own int", TestType.INT,
                typeOf(global.lookupVariable("x")));
        checkNull("V3.6 unknown name returns null", blockB.lookupVariable("missing"));
    }

    // ==================================================================
    // Functions
    // ==================================================================

    /** F1: current level only (1 level) - define/lookup, duplicate returns null, correct signature. */
    static void f1_defineAndLookupFunctionInCurrentScope() {
        Scope current = new Scope(); // minimum nesting: current level only

        FunctionSymbol main = current.defineFunction("main", TestType.INT, new ArrayList<>());
        checkNotNull("F1.1 define returns non-null", main);
        check("F1.2 function name", "main", main == null ? null : main.getName());
        check("F1.3 return type", TestType.INT, main == null ? null : main.getType());
        checkSame("F1.4 lookup in current scope returns the same object", main, current.lookupFunction("main"));
        check("F1.5 no-parameter signature", "main: int()", main == null ? null : main.signature());

        // Duplicate declaration returns null
        checkNull("F1.6 redeclaration returns null",
                current.defineFunction("main", TestType.VOID, new ArrayList<>()));
        // Undeclared function returns null
        checkNull("F1.7 undeclared function returns null", current.lookupFunction("noFun"));

        // Parameterized function signature
        List<VariableSymbol> params = new ArrayList<>(Arrays.asList(
                new VariableSymbol("a", TestType.INT),
                new VariableSymbol("b", TestType.INT)));
        FunctionSymbol add = current.defineFunction("add", TestType.INT, params);
        checkNotNull("F1.8 parameterized function define returns non-null", add);
        check("F1.9 parameterized signature", "add: int(int, int)", add == null ? null : add.signature());
    }

    /** F2: 3 levels - a function is visible to inner scopes via the parent chain, not to outer scopes. */
    static void f2_lookupFunctionWalksUpParentChain() {
        Scope[] s = buildThreeLevels();
        Scope global = s[0];
        Scope blockA = s[1];
        Scope blockB = s[2];

        global.defineFunction("fGlobal", TestType.VOID, new ArrayList<>()); // level 1
        blockA.defineFunction("fMid", TestType.INT, new ArrayList<>());     // level 2
        blockB.defineFunction("fInner", TestType.INT, new ArrayList<>());   // level 3

        checkNotNull("F2.1 innermost sees level-1 fGlobal", blockB.lookupFunction("fGlobal"));
        checkNotNull("F2.2 innermost sees level-2 fMid", blockB.lookupFunction("fMid"));
        checkNotNull("F2.3 innermost sees level-3 fInner", blockB.lookupFunction("fInner"));

        checkNull("F2.4 level-1 cannot see level-2 fMid", global.lookupFunction("fMid"));
        checkNull("F2.5 level-1 cannot see level-3 fInner", global.lookupFunction("fInner"));
        checkNull("F2.6 level-2 cannot see level-3 fInner", blockA.lookupFunction("fInner"));
    }

    /** F3: Regression - a function scope's parent must be the scope that defines it (define should pass this). */
    static void f3_functionParentShouldBeDefiningScope() {
        Scope global = new Scope();
        global.defineVariable("globalVar", TestType.INT);

        FunctionSymbol main = global.defineFunction("main", TestType.INT, new ArrayList<>());

        // If define mistakenly uses new FunctionSymbol(..., parent), the parent here would be null and this fails
        checkSame("F3.1 function parent is the global scope that defines it",
                global, main == null ? null : main.getParent());

        // With the correct parent, a global variable resolves from inside the function body
        VariableSymbol found = main == null ? null : main.lookupVariable("globalVar");
        checkNotNull("F3.2 global variable globalVar resolves inside the function", found);
        check("F3.3 resolved variable name", "globalVar", nameOf(found));
    }

    // ==================================================================
    // Helpers
    // ==================================================================

    /** Builds a 3-level scope chain and returns [global, blockA, blockB]. */
    private static Scope[] buildThreeLevels() {
        Scope global = new Scope();        // level 1
        Scope blockA = new Scope(global);  // level 2
        Scope blockB = new Scope(blockA);  // level 3
        return new Scope[]{global, blockA, blockB};
    }

    private static String nameOf(VariableSymbol v) {
        return v == null ? null : v.getName();
    }

    private static Type typeOf(VariableSymbol v) {
        return v == null ? null : v.getType();
    }

    // ---------- Lightweight assertions ----------
    private static void check(String label, Object expected, Object actual) {
        record(Objects.equals(expected, actual), label,
                "expected=" + expected + ", actual=" + actual);
    }

    private static void checkSame(String label, Object expected, Object actual) {
        record(expected == actual, label,
                "expected same instance=" + expected + ", actual=" + actual);
    }

    private static void checkNull(String label, Object actual) {
        record(actual == null, label, "expected=null, actual=" + actual);
    }

    private static void checkNotNull(String label, Object actual) {
        record(actual != null, label, "expected non-null, actual=" + actual);
    }

    private static void record(boolean ok, String label, String detail) {
        if (ok) {
            passed++;
            System.out.println("[PASS] " + label);
        } else {
            failed++;
            System.out.println("[FAIL] " + label + "  (" + detail + ")");
        }
    }
}
