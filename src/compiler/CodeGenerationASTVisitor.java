package compiler;

import compiler.AST.*;
import compiler.lib.*;
import compiler.exc.*;
import svm.ExecuteVM;

import java.util.ArrayList;
import java.util.List;

import static compiler.lib.FOOLlib.*;
import static compiler.lib.FOOLlib.freshLabel;

public class CodeGenerationASTVisitor extends BaseASTVisitor<String, VoidException> {

  CodeGenerationASTVisitor() {}
  CodeGenerationASTVisitor(boolean debug) {super(false,debug);} //enables print for debugging

	private List<List<String>> dispatchTables = new ArrayList<>();

	@Override
	public String visitNode(ProgLetInNode n) {
		if (print) printNode(n);
		String declCode = null;
		for (Node dec : n.declist) declCode=nlJoin(declCode,visit(dec));
		return nlJoin(
			"push 0",	
			declCode, // generate code for declarations (allocation)			
			visit(n.exp),
			"halt",
			getCode()
		);
	}

	@Override
	public String visitNode(ProgNode n) {
		if (print) printNode(n);
		return nlJoin(
			visit(n.exp),
			"halt"
		);
	}

	@Override
	public String visitNode(FunNode n) {
		if (print) printNode(n,n.id);
		String declCode = null, popDecl = null, popParl = null;
		for (Node dec : n.declist) {
			declCode = nlJoin(declCode,visit(dec));
			popDecl = nlJoin(popDecl,"pop");
		}
		for (int i=0;i<n.parlist.size();i++) popParl = nlJoin(popParl,"pop");
		String funl = freshFunLabel();
		putCode(
			nlJoin(
				funl+":",
				"cfp", // set $fp to $sp value
				"lra", // load $ra value
				declCode, // generate code for local declarations (they use the new $fp!!!)
				visit(n.exp), // generate code for function body expression
				"stm", // set $tm to popped value (function result)
				popDecl, // remove local declarations from stack
				"sra", // set $ra to popped value
				"pop", // remove Access Link from stack
				popParl, // remove parameters from stack
				"sfp", // set $fp to popped value (Control Link)
				"ltm", // load $tm value (function result)
				"lra", // load $ra value
				"js"  // jump to to popped address
			)
		);
		return "push "+funl;		
	}

	@Override
	public String visitNode(VarNode n) {
		if (print) printNode(n,n.id);
		return visit(n.exp);
	}

	@Override
	public String visitNode(PrintNode n) {
		if (print) printNode(n);
		return nlJoin(
			visit(n.exp),
			"print"
		);
	}

	@Override
	public String visitNode(IfNode n) {
		if (print) printNode(n);
	 	String l1 = freshLabel();
	 	String l2 = freshLabel();		
		return nlJoin(
			visit(n.cond),
			"push 1",
			"beq "+l1,
			visit(n.el),
			"b "+l2,
			l1+":",
			visit(n.th),
			l2+":"
		);
	}

	@Override
	public String visitNode(EqualNode n) {
		if (print) printNode(n);
	 	String l1 = freshLabel();
	 	String l2 = freshLabel();
		return nlJoin(
			visit(n.left),
			visit(n.right),
			"beq "+l1,
			"push 0",
			"b "+l2,
			l1+":",
			"push 1",
			l2+":"
		);
	}

	@Override
	public String visitNode(LessEqualNode n){

	  if (print) printNode(n);
	  String l1 = freshLabel();
	  String l2 = freshLabel();
	  return nlJoin(
			  visit(n.left),
			  visit(n.right),
			  "bleq "+l1,
			  "push 0",
			  "b "+l2,
			  l1+":",
			  "push 1",
			  l2+":"
	  );
	}

	@Override
	public String visitNode(GreaterEqualNode n){

		if (print) printNode(n);
		String l1 = freshLabel();
		String l2 = freshLabel();
		return nlJoin(
				visit(n.right),
				visit(n.left),
				"bleq "+l1,
				"push 0",
				"b "+l2,
				l1+":",
				"push 1",
				l2+":"
		);
	}

	@Override
	public String visitNode(TimesNode n) {
		if (print) printNode(n);
		return nlJoin(
			visit(n.left),
			visit(n.right),
			"mult"
		);	
	}

    @Override
    public String visitNode(DivNode n) {
        if (print) printNode(n);
        return nlJoin(
                visit(n.left),
                visit(n.right),
                "div"
        );
    }

	@Override
	public String visitNode(PlusNode n) {
		if (print) printNode(n);
		return nlJoin(
			visit(n.left),
			visit(n.right),
			"add"				
		);
	}

    @Override
    public String visitNode(MinusNode n) {
        if (print) printNode(n);
        return nlJoin(
                visit(n.left),
                visit(n.right),
                "sub"
        );
    }

    @Override
    public String visitNode(NotNode n) {
        if (print) printNode(n);
        String l1 = freshLabel();
        String l2 = freshLabel();
        return nlJoin(
                "push 0",
                visit(n.exp),
                "beq " + l1,
                "push 0",
                "b " + l2,
                l1 + ":",
                "push 1",
                l2 + ":"
        );
    }

    @Override
    public String visitNode(AndNode n) {
        if (print) printNode(n);
        String l1 = freshLabel();
        String l2 = freshLabel();
        return nlJoin(
                visit(n.left),
                "push 0",
                "beq " + l1,    // Se left == 0, è falso, salta a l1
                visit(n.right),
                "push 0",
                "beq " + l1,    // Se right == 0, è falso, salta a l1
                "push 1",       // Entrambi sono veri, restituisci 1
                "b " + l2,      // Salta la parte "falso"
                l1 + ":",
                "push 0",       // Risultato falso
                l2 + ":"
        );
    }

    @Override
    public String visitNode(OrNode n) {
        if (print) printNode(n);
        String l1 = freshLabel();
        String l2 = freshLabel();
        return nlJoin(
                visit(n.left),
                "push 1",
                "beq " + l1,    // Se left == 1, è vero, salta a l1
                visit(n.right),
                "push 1",
                "beq " + l1,    // Se right == 1, è vero, salta a l1
                "push 0",       // Entrambi falsi, restituisci 0
                "b " + l2,      // Salta la parte "vero"
                l1 + ":",
                "push 1",       // Risultato vero
                l2 + ":"
        );
    }

	@Override
	public String visitNode(CallNode n) {
		if (print) printNode(n, n.id);
		String argCode = null, getAR = null;

		// 1. Generazione codice per gli argomenti (in ordine inverso)
		for (int i = n.arglist.size() - 1; i >= 0; i--)
			argCode = nlJoin(argCode, visit(n.arglist.get(i)));

		// 2. Risalita della catena statica
		for (int i = 0; i < n.nl - n.entry.nl; i++)
			getAR = nlJoin(getAR, "lw");

		// Codice comune iniziale: risalita catena e duplicazione indirizzo (Object Pointer o AR)
		String commonCode = nlJoin(
				"lfp",
				argCode,
				"lfp", getAR,
				"stm", "ltm", "ltm"
		);

		// 3. Distinzione tra Funzione e Metodo
		if (n.entry.offset >= 0) {
			// CASO METODO (chiamata interna alla classe)
			return nlJoin(
					commonCode,
					"lw",                   // Carica il Dispatch Pointer (che è a offset 0 dell'oggetto)
					"push " + n.entry.offset,
					"add",                  // Calcola indirizzo del metodo nella Dispatch Table
					"lw",                   // Carica l'indirizzo del codice del metodo
					"js"                    // Salto
			);
		} else {
			// CASO FUNZIONE (invariato)
			return nlJoin(
					commonCode,
					"push " + n.entry.offset,
					"add",
					"lw",
					"js"
			);
		}
	}

	@Override
	public String visitNode(IdNode n) {
		if (print) printNode(n,n.id);
		String getAR = null;
		for (int i = 0;i<n.nl-n.entry.nl;i++) getAR=nlJoin(getAR,"lw");
		return nlJoin(
			"lfp", getAR, // retrieve address of frame containing "id" declaration
			              // by following the static chain (of Access Links)
			"push "+n.entry.offset, "add", // compute address of "id" declaration
			"lw" // load value of "id" variable
		);
	}

	@Override
	public String visitNode(BoolNode n) {
		if (print) printNode(n,n.val.toString());
		return "push "+(n.val?1:0);
	}

	@Override
	public String visitNode(IntNode n) {
		if (print) printNode(n,n.val.toString());
		return "push "+n.val;
	}

	@Override
	public String visitNode(ClassNode n) {
		if (print) printNode(n, n.id);

		// 1. Costruzione LOGICA della tabella (ArrayList)
		List<String> dispatchTable = new ArrayList<>();
		if (n.superId != null) {
			// Copia dalla superclasse usando l'offset [cite: 340, 342]
			List<String> superTable = this.dispatchTables.get(-n.superEntry.offset - 2);
			dispatchTable.addAll(new ArrayList<>(superTable));
		}

		// 2. Visita i metodi locali per aggiornare le label e gestire l'overriding [cite: 343, 347]
		for (MethodNode meth : n.methods) {
			visit(meth); // Genera la label e mette il codice in FOOLlib [cite: 344, 345]
			if (meth.offset < dispatchTable.size()) {
				dispatchTable.set(meth.offset, meth.label); // Override [cite: 347]
			} else {
				dispatchTable.add(meth.label); // Nuovo metodo [cite: 250, 340]
			}
		}

		// 3. Salvataggio nel visitor per le sottoclassi future [cite: 329, 330]
		this.dispatchTables.add(dispatchTable);

		// 4. Generazione CODICE FISICO per allocazione in HEAP
		String dispatchTableCode = "lhp"; // Il Dispatch Pointer da ritornare [cite: 349]
		for (String label : dispatchTable) {
			dispatchTableCode = nlJoin(dispatchTableCode,
					"push " + label, // Carica l'indirizzo del metodo
					"lhp", "sw",      // Scrivi nello heap
					"lhp", "push 1", "add", "shp" // Incrementa $hp
			);
		}

		return dispatchTableCode;
	}

	@Override
	public String visitNode(NewNode n) {
		if (print) printNode(n, n.idClass);

		String argCode = null;
		for (int i = 0; i < n.parlist.size(); i++) {
			argCode = nlJoin(argCode, visit(n.parlist.get(i)));
		}

		String allocCode = null;
		for (int i = 0; i < n.parlist.size(); i++) {
			allocCode = nlJoin(allocCode,
					"lhp",
					"sw",
					"lhp", "push 1", "add", "shp"
			);
		}

		String getAR = null;
		for (int i = 0; i < n.nl - n.entry.nl; i++) getAR = nlJoin(getAR, "lw");

		return nlJoin(
				argCode,    // Valori dei campi sullo stack
				allocCode,  // Sposta i campi nello heap, $hp ora punta alla cella dopo l'ultimo campo

				// Recupero Dispatch Pointer dall'AR globale (MEMSIZE + offset)
				"push " + ExecuteVM.MEMSIZE,
				"push " + n.entry.offset, "add",
				"lw",       // Carica il dispatch pointer della classe

				"lhp", "sw", // Lo scrive nello heap nella posizione corrente di $hp

				"lhp",       // Questo è l'OBJECT POINTER da ritornare (punta al dispatch pointer)
				"lhp", "push 1", "add", "shp" // Incrementa $hp finale
		);
	}

	@Override
	public String visitNode(MethodNode n) {
		if (print) printNode(n, n.id);
		String declCode = null, popDecl = null, popParl = null;
		for (Node dec : n.declist) {
			declCode = nlJoin(declCode, visit(dec));
			popDecl = nlJoin(popDecl, "pop");
		}
		for (int i = 0; i < n.parlist.size(); i++) popParl = nlJoin(popParl, "pop");

		String funl = freshFunLabel();
		putCode(
				nlJoin(
						funl + ":",
						"cfp",
						"lra",
						declCode,
						visit(n.exp),
						"stm",
						popDecl,
						"sra",
						"pop",
						popParl,
						"sfp",
						"ltm",
						"lra",
						"js"
				)
		);
		n.label = funl;
		return null;
	}

	@Override
	public String visitNode(ClassCallNode n) {
		if (print) printNode(n, n.idObj + "." + n.idMethod);

		String argCode = null, getAR = null;
		// 1. Caricamento argomenti [cite: 140, 367, 368]
		for (int i = n.arglist.size() - 1; i >= 0; i--)
			argCode = nlJoin(argCode, visit(n.arglist.get(i)));

		// 2. Risalita catena statica per trovare l'oggetto [cite: 360, 368]
		for (int i = 0; i < n.nl - n.entry.nl; i++)
			getAR = nlJoin(getAR, "lw");

		return nlJoin(
				"lfp",
				argCode,          // Parametri del metodo [cite: 367]
				"lfp", getAR,     // Risalita fino all'AR dell'ID1 [cite: 368]
				"push " + n.entry.offset, "add",
				"lw",             // Carica l'Object Pointer (ID1) sullo stack [cite: 368]

				// --- INIZIO DUPLICAZIONE ---
				"stm", "ltm", "ltm", // Se la tua SVM non ha 'copy', questo duplica il valore
				// --- FINE DUPLICAZIONE ---

				"lw",             // Il primo ltm viene usato per caricare il Dispatch Pointer
				"push " + n.methodEntry.offset, "add",
				"lw",             // Carica l'indirizzo del metodo dalla Dispatch Table
				"js"              // Salta al metodo. Il secondo ltm è rimasto sotto ed è l'AL
		);
	}

	@Override
	public String visitNode(EmptyNode n) {
		if (print) printNode(n);
		return nlJoin("push -1");
	}


}