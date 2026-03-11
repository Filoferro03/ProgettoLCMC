package compiler;

import compiler.AST.*;
import compiler.lib.*;

public class TypeRels {

	// valuta se il tipo "a" e' <= al tipo "b", dove "a" e "b" sono tipi di base: IntTypeNode o BoolTypeNode
	public static boolean isSubtype(TypeNode a, TypeNode b) {
		// Se sono la stessa classe Java
		if (a.getClass().equals(b.getClass())) {
			// Se sono RefTypeNode, mi assicuro che abbiano lo stesso ID classe
			if (a instanceof RefTypeNode) {
				return ((RefTypeNode) a).id.equals(((RefTypeNode) b).id);
			}
			return true;
		}

		// 2. Regole di sottotipazione speciale
		return ((a instanceof BoolTypeNode) && (b instanceof IntTypeNode)) ||
				((a instanceof EmptyTypeNode) && (b instanceof RefTypeNode));
	}

}
