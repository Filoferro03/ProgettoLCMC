package compiler;

import compiler.AST.*;
import compiler.exc.*;
import compiler.lib.*;
import static compiler.TypeRels.*;

//visitNode(n) fa il type checking di un Node n e ritorna:
//- per una espressione, il suo tipo (oggetto BoolTypeNode o IntTypeNode)
//- per una dichiarazione, "null"; controlla la correttezza interna della dichiarazione
//(- per un tipo: "null"; controlla che il tipo non sia incompleto) 
//
//visitSTentry(s) ritorna, per una STentry s, il tipo contenuto al suo interno
public class TypeCheckEASTVisitor extends BaseEASTVisitor<TypeNode,TypeException> {

	TypeCheckEASTVisitor() { super(true); } // enables incomplete tree exceptions 
	TypeCheckEASTVisitor(boolean debug) { super(true,debug); } // enables print for debugging

	//checks that a type object is visitable (not incomplete) 
	private TypeNode ckvisit(TypeNode t) throws TypeException {
		visit(t);
		return t;
	} 
	
	@Override
	public TypeNode visitNode(ProgLetInNode n) throws TypeException {
		if (print) printNode(n);
		for (Node dec : n.declist)
			try {
				visit(dec);
			} catch (IncomplException e) { 
			} catch (TypeException e) {
				System.out.println("Type checking error in a declaration: " + e.text);
			}
		return visit(n.exp);
	}

	@Override
	public TypeNode visitNode(ProgNode n) throws TypeException {
		if (print) printNode(n);
		return visit(n.exp);
	}

	@Override
	public TypeNode visitNode(FunNode n) throws TypeException {
		if (print) printNode(n,n.id);
		for (Node dec : n.declist)
			try {
				visit(dec);
			} catch (IncomplException e) { 
			} catch (TypeException e) {
				System.out.println("Type checking error in a declaration: " + e.text);
			}
		if ( !isSubtype(visit(n.exp),ckvisit(n.retType)) ) 
			throw new TypeException("Wrong return type for function " + n.id,n.getLine());
		return null;
	}

	@Override
	public TypeNode visitNode(VarNode n) throws TypeException {
		if (print) printNode(n,n.id);
		if ( !isSubtype(visit(n.exp),ckvisit(n.getType())) )
			throw new TypeException("Incompatible value for variable " + n.id,n.getLine());
		return null;
	}

	@Override
	public TypeNode visitNode(PrintNode n) throws TypeException {
		if (print) printNode(n);
		return visit(n.exp);
	}

	@Override
	public TypeNode visitNode(IfNode n) throws TypeException {
		if (print) printNode(n);

		if (!(isSubtype(visit(n.cond), new BoolTypeNode())))
			throw new TypeException("Non boolean condition in if", n.getLine());
		TypeNode t = visit(n.th);
		TypeNode e = visit(n.el);
		TypeNode lca = lowestCommonAncestor(t, e);

		if (lca != null) return lca;

		throw new TypeException("Incompatible types in then-else branches", n.getLine());
	}

	@Override
	public TypeNode visitNode(EqualNode n) throws TypeException {
		if (print) printNode(n);
		TypeNode l = visit(n.left);
		TypeNode r = visit(n.right);
		if ( !(isSubtype(l, r) || isSubtype(r, l)) )
			throw new TypeException("Incompatible types in equal",n.getLine());
		return new BoolTypeNode();
	}

	@Override
	public TypeNode visitNode(TimesNode n) throws TypeException {
		if (print) printNode(n);
		if ( !(isSubtype(visit(n.left), new IntTypeNode())
				&& isSubtype(visit(n.right), new IntTypeNode())) )
			throw new TypeException("Non integers in multiplication",n.getLine());
		return new IntTypeNode();
	}

    @Override
    public TypeNode visitNode(DivNode n) throws TypeException {
        if (print) printNode(n);
        if ( !(isSubtype(visit(n.left), new IntTypeNode())
                && isSubtype(visit(n.right), new IntTypeNode())) )
            throw new TypeException("Non integers in division",n.getLine());
        return new IntTypeNode();
    }

	@Override
	public TypeNode visitNode(PlusNode n) throws TypeException {
		if (print) printNode(n);
		if ( !(isSubtype(visit(n.left), new IntTypeNode())
				&& isSubtype(visit(n.right), new IntTypeNode())) )
			throw new TypeException("Non integers in sum",n.getLine());
		return new IntTypeNode();
	}

    @Override
    public TypeNode visitNode(MinusNode n) throws TypeException {
        if (print) printNode(n);
        if ( !(isSubtype(visit(n.left), new IntTypeNode())
                && isSubtype(visit(n.right), new IntTypeNode())) )
            throw new TypeException("Non integers in min",n.getLine());
        return new IntTypeNode();
    }

    @Override
    public TypeNode visitNode(NotNode n) throws TypeException {
        if (print) printNode(n);
        if ( !(isSubtype(visit(n.exp), new BoolTypeNode())))
            throw new TypeException("Non integers in not",n.getLine());
        return new BoolTypeNode();
    }

    @Override
    public TypeNode visitNode(AndNode n) throws TypeException {
        if (print) printNode(n);
        if ( !(isSubtype(visit(n.left), new BoolTypeNode())
                && isSubtype(visit(n.right), new BoolTypeNode())) )
            throw new TypeException("Non booleans in AND", n.getLine());
        return new BoolTypeNode();
    }

    @Override
    public TypeNode visitNode(OrNode n) throws TypeException {
        if (print) printNode(n);
        if ( !(isSubtype(visit(n.left), new BoolTypeNode())
                && isSubtype(visit(n.right), new BoolTypeNode())) )
            throw new TypeException("Non booleans in OR", n.getLine());
        return new BoolTypeNode();
    }

	@Override
	public TypeNode visitNode(LessEqualNode n) throws TypeException{
		if (print) printNode(n);
		if ( !(isSubtype(visit(n.left), new IntTypeNode())
				&& isSubtype(visit(n.right), new IntTypeNode())) )
			throw new TypeException("Non integers in less equal",n.getLine());
		return new BoolTypeNode();
	}

	@Override
	public TypeNode visitNode(GreaterEqualNode n) throws TypeException{
		if (print) printNode(n);
		if ( !(isSubtype(visit(n.left), new IntTypeNode())
				&& isSubtype(visit(n.right), new IntTypeNode())) )
			throw new TypeException("Non integers in greater equal",n.getLine());
		return new BoolTypeNode();
	}

	@Override
	public TypeNode visitNode(CallNode n) throws TypeException {
		if (print) printNode(n,n.id);
		TypeNode t = visit(n.entry); 
		if ( !(t instanceof ArrowTypeNode) )
			throw new TypeException("Invocation of a non-function "+n.id,n.getLine());
		ArrowTypeNode at = (ArrowTypeNode) t;
		if ( !(at.parlist.size() == n.arglist.size()) )
			throw new TypeException("Wrong number of parameters in the invocation of "+n.id,n.getLine());
		for (int i = 0; i < n.arglist.size(); i++)
			if ( !(isSubtype(visit(n.arglist.get(i)),at.parlist.get(i))) )
				throw new TypeException("Wrong type for "+(i+1)+"-th parameter in the invocation of "+n.id,n.getLine());
		return at.ret;
	}

	@Override
	public TypeNode visitNode(IdNode n) throws TypeException {
		if (print) printNode(n,n.id);
		TypeNode t = visit(n.entry); 
		if (t instanceof ArrowTypeNode || t instanceof ClassTypeNode)
			throw new TypeException("Wrong usage of function identifier " + n.id,n.getLine());
		return t;
	}

	@Override
	public TypeNode visitNode(BoolNode n) {
		if (print) printNode(n,n.val.toString());
		return new BoolTypeNode();
	}

	@Override
	public TypeNode visitNode(IntNode n) {
		if (print) printNode(n,n.val.toString());
		return new IntTypeNode();
	}
	
	@Override
	public TypeNode visitNode(ArrowTypeNode n) throws TypeException {
		if (print) printNode(n);
		for (Node par: n.parlist) visit(par);
		visit(n.ret,"->"); //marks return type
		return null;
	}

	@Override
	public TypeNode visitNode(BoolTypeNode n) {
		if (print) printNode(n);
		return null;
	}

	@Override
	public TypeNode visitNode(IntTypeNode n) {
		if (print) printNode(n);
		return null;
	}

	@Override
	public TypeNode visitNode(RefTypeNode n){
		if (print) printNode(n);
		return null;
	}

	@Override
	public TypeNode visitNode(EmptyTypeNode n){
		if(print) printNode(n);
		return null;
	}

	@Override
	public TypeNode visitNode(ClassTypeNode n){
		if (print) printNode(n);
		return null;
	}

	@Override
	public TypeNode visitSTentry(STentry entry) throws TypeException {
		if (print) printSTentry("type");
		return ckvisit(entry.type); 
	}

	@Override
	public TypeNode visitNode(MethodNode n) throws TypeException {
		if (print) printNode(n,n.id);

		for (Node dec : n.declist)
			try {
				visit(dec);
			} catch (IncomplException e) {
			} catch (TypeException e) {
				System.out.println("Type checking error in a declaration: " + e.text);
			}
		if ( !isSubtype(visit(n.exp),ckvisit(n.retType)) )
			throw new TypeException("Wrong return type for method " + n.id,n.getLine());
		return null;
	}

	@Override
	public TypeNode visitNode(ClassNode n) {
		if (print) printNode(n, n.id);

		for (MethodNode methNode : n.methods) {
			try {
				visit(methNode);
			} catch (IncomplException e) {

			} catch (TypeException e) {
				System.out.println("Type checking error in method " + methNode.id + ": " + e.text);
			}
		}
		if (n.superId != null) {
			superType.put(n.id, n.superId);
			ClassTypeNode parentCT = (ClassTypeNode) n.superEntry.type;


			for (FieldNode field : n.fields) {
				int position = -field.offset - 1;
				if (position < parentCT.allFields.size()) {
					TypeNode parentFieldType = parentCT.allFields.get(position);
					if (!isSubtype(field.getType(), parentFieldType)) {
						System.out.println("Type checking error: invalid field overriding for " + field.id +
								" in class " + n.id + " at line " + n.getLine());
					}
				}
			}

			for (MethodNode meth : n.methods) {
				int position = meth.offset;

				if (position < parentCT.allMethods.size()) {
					TypeNode parentMethodType = parentCT.allMethods.get(position);
					if (!isSubtype(meth.getType(), parentMethodType)) {
						System.out.println("Type checking error: invalid method overriding for " + meth.id +
								" in class " + n.id + " at line " + meth.getLine());
					}
				}
			}
		}


		return null;
	}

	@Override
	public TypeNode visitNode(ClassCallNode n) throws TypeException {
		if (print) printNode(n, n.idObj + "." + n.idMethod);

		TypeNode t = visit(n.methodEntry);

		if ( !(t instanceof ArrowTypeNode) )
			throw new TypeException("Member " + n.idMethod + " is not a method", n.getLine());

		ArrowTypeNode at = (ArrowTypeNode) t;

		if (at.parlist.size() != n.arglist.size())
			throw new TypeException("Wrong number of parameters in the invocation of " + n.idMethod, n.getLine());

		for (int i = 0; i < n.arglist.size(); i++) {
			TypeNode argType = visit(n.arglist.get(i));
			TypeNode parType = at.parlist.get(i);
			if ( !isSubtype(argType, parType) )
				throw new TypeException("Wrong type for " + (i+1) + "-th parameter in the invocation of " + n.idMethod, n.getLine());
		}
		return at.ret;
	}

	@Override
	public TypeNode visitNode(NewNode n) throws TypeException {
		if (print) printNode(n, n.idClass);

		TypeNode t = visit(n.entry);

		if (!(t instanceof ClassTypeNode)) {
			throw new TypeException("Invocation of a new on a non-class: " + n.idClass, n.getLine());
		}

		ClassTypeNode ct = (ClassTypeNode) t;


		if (ct.allFields.size() != n.parlist.size()) {
			throw new TypeException("Wrong number of parameters for 'new " + n.idClass + "'", n.getLine());
		}


		for (int i = 0; i < n.parlist.size(); i++) {
			TypeNode argType = visit(n.parlist.get(i));
			TypeNode fieldType = ct.allFields.get(i);

			if (!isSubtype(argType, fieldType)) {
				throw new TypeException("Wrong type for " + (i + 1) + "-th field in 'new " + n.idClass + "'", n.getLine());
			}
		}

		return new RefTypeNode(n.idClass);
	}

	@Override
	public TypeNode visitNode(EmptyNode n){
		return new EmptyTypeNode();
	}

}