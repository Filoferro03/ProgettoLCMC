package compiler;

import compiler.AST.*;
import compiler.lib.*;

import java.util.HashMap;
import java.util.Map;

public class TypeRels {

    static Map<String, String> superType = new HashMap<>();

	public static boolean isSubtype(TypeNode a, TypeNode b) {
        if (a == null || b == null) return false;

		if (a.getClass().equals(b.getClass())) {
			if (a instanceof RefTypeNode) {
                String idNameSubClass = ((RefTypeNode) a).id;
                String idNameSuperClass = ((RefTypeNode) b).id;
                if(idNameSubClass.equals(idNameSuperClass))
                    return true;
                while (!idNameSubClass.equals(idNameSuperClass)){
                   String superId = superType.get(idNameSubClass);
                   if(superId == null){
                       return false;
                   }
                    idNameSubClass = superId;
                }
                return true;
			}
            if(a instanceof ArrowTypeNode){
                if(!isSubtype(((ArrowTypeNode) a).ret, ((ArrowTypeNode) b).ret)){
                    return false;
                }
                int index = 0;
                for(TypeNode subClassParType : ((ArrowTypeNode) a).parlist){
                    if(!isSubtype(((ArrowTypeNode) b).parlist.get(index), subClassParType)){
                        return false;
                    }
                }
            }
			return true;
		}

		return ((a instanceof BoolTypeNode) && (b instanceof IntTypeNode)) ||
				((a instanceof EmptyTypeNode) && (b instanceof RefTypeNode));
	}

    public static TypeNode lowestCommonAncestor(TypeNode a, TypeNode b) {
        if ((a instanceof RefTypeNode || a instanceof EmptyTypeNode) &&
                (b instanceof RefTypeNode || b instanceof EmptyTypeNode)) {
            if (a instanceof EmptyTypeNode) return b;
            if (b instanceof EmptyTypeNode) return a;

            RefTypeNode refA = (RefTypeNode) a;
            RefTypeNode refB = (RefTypeNode) b;
            String currentA = refA.id;

            while (currentA != null) {
                if (isSubtype(refB, new RefTypeNode(currentA))) {
                    return new RefTypeNode(currentA);
                }
                currentA = superType.get(currentA);
            }
            return null;
        }

        if ((a instanceof IntTypeNode || a instanceof BoolTypeNode) &&
                (b instanceof IntTypeNode || b instanceof BoolTypeNode)) {

            if (a instanceof IntTypeNode || b instanceof IntTypeNode) {
                return new IntTypeNode();
            }
            return new BoolTypeNode();
        }
        return null;
    }

}
