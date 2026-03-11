package compiler;

import java.util.*;
import compiler.AST.*;
import compiler.exc.*;
import compiler.lib.*;

public class SymbolTableASTVisitor extends BaseASTVisitor<Void,VoidException> {
	
	private List<Map<String, STentry>> symTable = new ArrayList<>();
	private Map<String, Map<String,STentry>> classTable = new HashMap<>();
    private int nestingLevel=0; // current nesting level
	private int decOffset=-2; // counter for offset of local declarations at current nesting level 
	private int methodOffset = 0;
    int stErrors=0;

	SymbolTableASTVisitor() {}
	SymbolTableASTVisitor(boolean debug) {super(debug);} // enables print for debugging

	private STentry stLookup(String id) {
		int j = nestingLevel;
		STentry entry = null;
		while (j >= 0 && entry == null) 
			entry = symTable.get(j--).get(id);	
		return entry;
	}

	@Override
	public Void visitNode(ProgLetInNode n) {
		if (print) printNode(n);
		Map<String, STentry> hm = new HashMap<>();
		symTable.add(hm);
	    for (Node dec : n.declist) visit(dec);
		visit(n.exp);
		symTable.remove(0);
		return null;
	}

	@Override
	public Void visitNode(ProgNode n) {
		if (print) printNode(n);
		visit(n.exp);
		return null;
	}
	
	@Override
	public Void visitNode(FunNode n) {
		if (print) printNode(n);
		Map<String, STentry> hm = symTable.get(nestingLevel);
		List<TypeNode> parTypes = new ArrayList<>();  
		for (ParNode par : n.parlist) parTypes.add(par.getType()); 
		STentry entry = new STentry(nestingLevel, new ArrowTypeNode(parTypes,n.retType),decOffset--);
		//inserimento di ID nella symtable
		if (hm.put(n.id, entry) != null) {
			System.out.println("Fun id " + n.id + " at line "+ n.getLine() +" already declared");
			stErrors++;
		} 
		//creare una nuova hashmap per la symTable
		nestingLevel++;
		Map<String, STentry> hmn = new HashMap<>();
		symTable.add(hmn);
		int prevNLDecOffset=decOffset; // stores counter for offset of declarations at previous nesting level 
		decOffset=-2;
		
		int parOffset=1;
		for (ParNode par : n.parlist)
			if (hmn.put(par.id, new STentry(nestingLevel,par.getType(),parOffset++)) != null) {
				System.out.println("Par id " + par.id + " at line "+ n.getLine() +" already declared");
				stErrors++;
			}
		for (Node dec : n.declist) visit(dec);
		visit(n.exp);
		//rimuovere la hashmap corrente poiche' esco dallo scope               
		symTable.remove(nestingLevel--);
		decOffset=prevNLDecOffset; // restores counter for offset of declarations at previous nesting level 
		return null;
	}
	
	@Override
	public Void visitNode(VarNode n) {
		if (print) printNode(n);
		visit(n.exp);
		Map<String, STentry> hm = symTable.get(nestingLevel);
		STentry entry = new STentry(nestingLevel,n.getType(),decOffset--);
		//inserimento di ID nella symtable
		if (hm.put(n.id, entry) != null) {
			System.out.println("Var id " + n.id + " at line "+ n.getLine() +" already declared");
			stErrors++;
		}
		return null;
	}

	@Override
	public Void visitNode(PrintNode n) {
		if (print) printNode(n);
		visit(n.exp);
		return null;
	}

	@Override
	public Void visitNode(IfNode n) {
		if (print) printNode(n);
		visit(n.cond);
		visit(n.th);
		visit(n.el);
		return null;
	}
	
	@Override
	public Void visitNode(EqualNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}
	
	@Override
	public Void visitNode(TimesNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

    @Override
    public Void visitNode(DivNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }
	
	@Override
	public Void visitNode(PlusNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

    @Override
    public Void visitNode(MinusNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

	@Override
	public Void visitNode(LessEqualNode n){
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(GreaterEqualNode n){
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

    @Override
    public Void visitNode(NotNode n) {
        if (print) printNode(n);
        visit(n.exp);
        return null;
    }

    @Override
    public Void visitNode(AndNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(OrNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

	@Override
	public Void visitNode(CallNode n) {
		if (print) printNode(n);
		STentry entry = stLookup(n.id);
		if (entry == null) {
			System.out.println("Fun id " + n.id + " at line "+ n.getLine() + " not declared");
			stErrors++;
		} else {
			n.entry = entry;
			n.nl = nestingLevel;
		}
		for (Node arg : n.arglist) visit(arg);
		return null;
	}

	@Override
	public Void visitNode(IdNode n) {
		if (print) printNode(n);
		STentry entry = stLookup(n.id);
		if (entry == null) {
			System.out.println("Var or Par id " + n.id + " at line "+ n.getLine() + " not declared");
			stErrors++;
		} else {
			n.entry = entry;
			n.nl = nestingLevel;
		}
		return null;
	}

	@Override
	public Void visitNode(BoolNode n) {
		if (print) printNode(n, n.val.toString());
		return null;
	}

	@Override
	public Void visitNode(IntNode n) {
		if (print) printNode(n, n.val.toString());
		return null;
	}

    @Override
    public Void visitNode(ClassNode n){
        if(print) printNode(n);
        Map<String, STentry> hm = symTable.get(nestingLevel);
        List<TypeNode> fieldTypes = new ArrayList<>();
        List<ArrowTypeNode> methodTypes = new ArrayList<>();
        ClassTypeNode classType = new ClassTypeNode(fieldTypes, methodTypes);
        STentry entry = new STentry(nestingLevel, classType, decOffset--);
        if(hm.put(n.id, entry) != null){
            System.out.println("Class id " + n.id + " at line "+ n.getLine() +" already declared");
            stErrors++;
        }
        //entering the scope of the class
        nestingLevel++;
        Map<String, STentry> virtualTable = new HashMap<>();
        classTable.put(n.id, virtualTable);
        symTable.add(virtualTable);
        methodOffset = 0; //rise
        int fieldOffset = -1; //decrease
        for (FieldNode field : n.fields) {
            STentry fieldEntry = new STentry(nestingLevel, field.getType(), fieldOffset--);
            if (virtualTable.put(field.id, fieldEntry) != null) {
                System.out.println("Field id " + field.id + " at line " + n.getLine() + " already declared");
                stErrors++;
            }
            fieldTypes.add(-fieldEntry.offset - 1, field.getType());
        }
        for (MethodNode meth : n.methods){
            visit(meth);
            List<TypeNode> parType = new ArrayList<>();
            for ( ParNode par : meth.parlist ){
                parType.add(par.getType());
            }
            methodTypes.add(meth.offset, new ArrowTypeNode(parType, meth.retType));
        }
        symTable.remove(nestingLevel--);
        return null;
    }

    @Override
    public Void visitNode(MethodNode n) {
        if (print) printNode(n);
        Map<String, STentry> virtualTable = symTable.get(nestingLevel);
        List<TypeNode> parTypes = new ArrayList<>();
        for (ParNode par : n.parlist) parTypes.add(par.getType());
        n.offset = methodOffset++;
//        n.type = new ArrowTypeNode(parTypes,n.retType); //settaggio del tipo del metodo
        STentry entry = new STentry(nestingLevel, new ArrowTypeNode(parTypes,n.retType), n.offset);
        //inserimento di ID nella symtable
        if (virtualTable.put(n.id, entry) != null) {
            System.out.println("Method id " + n.id + " at line "+ n.getLine() +" already declared");
            stErrors++;
        }
        //creata una nuova hashmap per lo scope interno al metodo
        nestingLevel++;
        Map<String, STentry> hmn = new HashMap<>();
        symTable.add(hmn);
        //si preserva il decOffset dello scope + esterno
        int prevNLDecOffset=decOffset; // stores counter for offset of declarations at previous nesting level
        decOffset=-2;
        int parOffset=1;
        for (ParNode par : n.parlist)
            if (hmn.put(par.id, new STentry(nestingLevel,par.getType(),parOffset++)) != null) {
                System.out.println("Par id " + par.id + " at line "+ n.getLine() +" already declared");
                stErrors++;
            }
        for (Node dec : n.declist) visit(dec);
        visit(n.exp);
        //rimuovere la hashmap corrente poiche' esco dallo scope
        symTable.remove(nestingLevel--);
        decOffset=prevNLDecOffset; // restores counter for offset of declarations at previous nesting level
        return null;
    }

    @Override
    public Void visitNode(ClassCallNode n) {
        if (print) printNode(n);
        STentry entry = stLookup(n.idObj);
        if (entry == null) {
            System.out.println("Object id " + n.idObj + " at line "+ n.getLine() + " not declared");
            stErrors++;
        } else {
            n.entry = entry;
			n.nl = nestingLevel;
            if (!(entry.type instanceof RefTypeNode)) {
                System.out.println("Id " + n.idObj + " at line " + n.getLine() + " is not an object");
                stErrors++;
            } else {
                RefTypeNode refTypeNode = (RefTypeNode) entry.type;
                Map<String, STentry> virtualTableOfClass = classTable.get(refTypeNode.id);
                STentry methodEntry = virtualTableOfClass.get(n.idMethod);
                if (methodEntry == null) {
                    System.out.println("Method id " + n.idMethod + " at line " + n.getLine() + " not declared");
                    stErrors++;
                }
                n.methodEntry = methodEntry;
            }
        }
            for (Node arg : n.arglist) visit(arg);
        return null;
    }

    @Override
    public Void visitNode(NewNode n) {
        if (print) printNode(n);
        Map<String, STentry> globalScope = symTable.get(0);
        STentry entry = globalScope.get(n.idClass);
        if (entry == null) {
            System.err.println("Class " + n.idClass + " at line " + n.getLine() + " not declared");
        } else {
            if (!(entry.type instanceof ClassTypeNode)) {
                System.err.println("Id " + n.idClass + " at line " + n.getLine() + " is not a class");
            } else {
                n.entry = entry;
				n.nl = nestingLevel;
            }
        }

        return null;
    }

	@Override
	public Void visitNode(EmptyNode n) {
		if (print) printNode(n);
		return null;
	}
}
