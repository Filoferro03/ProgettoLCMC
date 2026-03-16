package compiler;

import compiler.AST.*;
import compiler.lib.*;

import java.util.HashMap;
import java.util.Map;

public class TypeRels {

    // catena di extends in modo da associare la sottoclasse (chiave) con la classe che estende (valore)
    static Map<String, String> superType = new HashMap<>();

	// valuta se il tipo "a" e' <= al tipo "b", dove "a" e "b" sono tipi di base: IntTypeNode o BoolTypeNode
	public static boolean isSubtype(TypeNode a, TypeNode b) {

        // Protezione contro i null
        if (a == null || b == null) return false;

		// Se sono la stessa classe Java
		if (a.getClass().equals(b.getClass())) {
			// Se sono RefTypeNode, mi assicuro che abbiano lo stesso ID classe o che a sia sottoclasse di b
			if (a instanceof RefTypeNode) {
                String idNameSubClass = ((RefTypeNode) a).id;
                String idNameSuperClass = ((RefTypeNode) b).id;
                // verifica se a e b sono riferimenti alla stessa classe
                //if(idNameSubClass.equals(idNameSuperClass))
                //    return true;

                // verifica scorrendo la mappa se a partire dalla sottoclasse si riesce risalendo
                // la catena di extends a raggiungere la superclasse
                while (!idNameSubClass.equals(idNameSuperClass)){
                   String superId = superType.get(idNameSubClass);
                   // caso in cui non si riesce a raggiungere l'id della superclasse
                   if(superId == null){
                       return false;
                   }
                    idNameSubClass = superId;
                }
                return true;
			}
            // caso in cui a e b siano due tipi funzionali
            if(a instanceof ArrowTypeNode){
                // verifica della co-varianza del tipo di ritorno
                if(!isSubtype(((ArrowTypeNode) a).ret, ((ArrowTypeNode) b).ret)){
                    return false;
                }
                // verifica della contro-varianza dei parametri
                int index = 0;
                for(TypeNode subClassParType : ((ArrowTypeNode) a).parlist){
                    if(!isSubtype(((ArrowTypeNode) b).parlist.get(index), subClassParType)){
                        return false;
                    }
                }
            }
			return true;
		}


		// 2. Regole di sottotipazione speciale
		return ((a instanceof BoolTypeNode) && (b instanceof IntTypeNode)) ||
				((a instanceof EmptyTypeNode) && (b instanceof RefTypeNode));
	}

    public static TypeNode lowestCommonAncestor(TypeNode a, TypeNode b) {
        // Caso tipi riferimento o null (EmptyTypeNode) [cite: 421]
        if ((a instanceof RefTypeNode || a instanceof EmptyTypeNode) &&
                (b instanceof RefTypeNode || b instanceof EmptyTypeNode)) {

            // Se uno tra "a" e "b" è EmptyTypeNode torna l'altro [cite: 422]
            if (a instanceof EmptyTypeNode) return b;
            if (b instanceof EmptyTypeNode) return a;

            // Entrambi sono RefTypeNode: risalita della gerarchia
            RefTypeNode refA = (RefTypeNode) a;
            RefTypeNode refB = (RefTypeNode) b;

            // All'inizio considera la classe di "a"
            String currentA = refA.id;

            while (currentA != null) {
                // Controlla se "b" è sottotipo della classe considerata
                if (isSubtype(refB, new RefTypeNode(currentA))) {
                    // Torna un RefTypeNode a tale classe qualora il controllo abbia successo [cite: 424]
                    return new RefTypeNode(currentA);
                }
                // Risale le superclassi tramite la funzione "superType"
                currentA = superType.get(currentA);
            }
            return null;
        }

        // Caso per a e b tipi bool/int
        if ((a instanceof IntTypeNode || a instanceof BoolTypeNode) &&
                (b instanceof IntTypeNode || b instanceof BoolTypeNode)) {

            // Torna int se almeno uno è int, bool altrimenti
            if (a instanceof IntTypeNode || b instanceof IntTypeNode) {
                return new IntTypeNode();
            }
            return new BoolTypeNode();
        }

        // In ogni altro caso torna null
        return null;
    }

}
