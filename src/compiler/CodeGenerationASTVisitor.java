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
			declCode,
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
                "beq " + l1,
                visit(n.right),
                "push 0",
                "beq " + l1,
                "push 1",
                "b " + l2,
                l1 + ":",
                "push 0",
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
                "beq " + l1,
                visit(n.right),
                "push 1",
                "beq " + l1,
                "push 0",
                "b " + l2,
                l1 + ":",
                "push 1",
                l2 + ":"
        );
    }

	@Override
	public String visitNode(CallNode n) {
		if (print) printNode(n, n.id);
		String argCode = null, getAR = null;

		for (int i = n.arglist.size() - 1; i >= 0; i--)
			argCode = nlJoin(argCode, visit(n.arglist.get(i)));

		for (int i = 0; i < n.nl - n.entry.nl; i++)
			getAR = nlJoin(getAR, "lw");


		String commonCode = nlJoin(
				"lfp",
				argCode,
				"lfp", getAR,
				"stm", "ltm", "ltm"
		);
		if (n.entry.offset >= 0) {
			return nlJoin(
					commonCode,
					"lw",
					"push " + n.entry.offset,
					"add",
					"lw",
					"js"
			);
		} else {

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
			"lfp", getAR,
			"push "+n.entry.offset, "add",
			"lw"
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


		List<String> dispatchTable = new ArrayList<>();
		if (n.superId != null) {

			List<String> superTable = this.dispatchTables.get(-n.superEntry.offset - 2);
			dispatchTable.addAll(new ArrayList<>(superTable));
		}


		for (MethodNode meth : n.methods) {
			visit(meth);
			if (meth.offset < dispatchTable.size()) {
				dispatchTable.set(meth.offset, meth.label);
			} else {
				dispatchTable.add(meth.label);
			}
		}


		this.dispatchTables.add(dispatchTable);


		String dispatchTableCode = "lhp";
		for (String label : dispatchTable) {
			dispatchTableCode = nlJoin(dispatchTableCode,
					"push " + label,
					"lhp", "sw",
					"lhp", "push 1", "add", "shp"
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
				argCode,
				allocCode,
				"push " + ExecuteVM.MEMSIZE,
				"push " + n.entry.offset, "add",
				"lw",
				"lhp", "sw",
				"lhp",
				"lhp", "push 1", "add", "shp"
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
		for (int i = n.arglist.size() - 1; i >= 0; i--)
			argCode = nlJoin(argCode, visit(n.arglist.get(i)));

		for (int i = 0; i < n.nl - n.entry.nl; i++)
			getAR = nlJoin(getAR, "lw");

		return nlJoin(
				"lfp",
				argCode,
				"lfp", getAR,
				"push " + n.entry.offset, "add",
				"lw",
				"stm", "ltm", "ltm",
				"lw",
				"push " + n.methodEntry.offset, "add",
				"lw",
				"js"
		);
	}

	@Override
	public String visitNode(EmptyNode n) {
		if (print) printNode(n);
		return nlJoin("push -1");
	}


}