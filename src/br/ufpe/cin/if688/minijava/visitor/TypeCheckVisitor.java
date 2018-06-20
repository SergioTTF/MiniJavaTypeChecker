package br.ufpe.cin.if688.minijava.visitor;

import br.ufpe.cin.if688.minijava.ast.And;
import br.ufpe.cin.if688.minijava.ast.ArrayAssign;
import br.ufpe.cin.if688.minijava.ast.ArrayLength;
import br.ufpe.cin.if688.minijava.ast.ArrayLookup;
import br.ufpe.cin.if688.minijava.ast.Assign;
import br.ufpe.cin.if688.minijava.ast.Block;
import br.ufpe.cin.if688.minijava.ast.BooleanType;
import br.ufpe.cin.if688.minijava.ast.Call;
import br.ufpe.cin.if688.minijava.ast.ClassDeclExtends;
import br.ufpe.cin.if688.minijava.ast.ClassDeclSimple;
import br.ufpe.cin.if688.minijava.ast.False;
import br.ufpe.cin.if688.minijava.ast.Formal;
import br.ufpe.cin.if688.minijava.ast.Identifier;
import br.ufpe.cin.if688.minijava.ast.IdentifierExp;
import br.ufpe.cin.if688.minijava.ast.IdentifierType;
import br.ufpe.cin.if688.minijava.ast.If;
import br.ufpe.cin.if688.minijava.ast.IntArrayType;
import br.ufpe.cin.if688.minijava.ast.IntegerLiteral;
import br.ufpe.cin.if688.minijava.ast.IntegerType;
import br.ufpe.cin.if688.minijava.ast.LessThan;
import br.ufpe.cin.if688.minijava.ast.MainClass;
import br.ufpe.cin.if688.minijava.ast.MethodDecl;
import br.ufpe.cin.if688.minijava.ast.Minus;
import br.ufpe.cin.if688.minijava.ast.NewArray;
import br.ufpe.cin.if688.minijava.ast.NewObject;
import br.ufpe.cin.if688.minijava.ast.Not;
import br.ufpe.cin.if688.minijava.ast.Plus;
import br.ufpe.cin.if688.minijava.ast.Print;
import br.ufpe.cin.if688.minijava.ast.Program;
import br.ufpe.cin.if688.minijava.ast.This;
import br.ufpe.cin.if688.minijava.ast.Times;
import br.ufpe.cin.if688.minijava.ast.True;
import br.ufpe.cin.if688.minijava.ast.Type;
import br.ufpe.cin.if688.minijava.ast.VarDecl;
import br.ufpe.cin.if688.minijava.ast.While;
import br.ufpe.cin.if688.minijava.symboltable.Method;
import br.ufpe.cin.if688.minijava.symboltable.Class;
import br.ufpe.cin.if688.minijava.symboltable.SymbolTable;

public class TypeCheckVisitor implements IVisitor<Type> {

	private SymbolTable symbolTable;
	private Method currMethod;
	private Class currClass;
	private boolean analyzeVar;
	private boolean analyzeMet;

	TypeCheckVisitor(SymbolTable st) {
		symbolTable = st;
	}

	// MainClass m;
	// ClassDeclList cl;
	public Type visit(Program n) {
		n.m.accept(this);
		for (int i = 0; i < n.cl.size(); i++) {
			n.cl.elementAt(i).accept(this);
		}
		return null;
	}

	// Identifier i1,i2;
	// Statement s;
	public Type visit(MainClass n) {
		this.currClass = this.symbolTable.getClass(n.i1.toString());
		this.currMethod = this.symbolTable.getMethod("main", this.currClass.getId());
		n.i1.accept(this);
		this.analyzeVar = true;
		n.i2.accept(this);
		this.analyzeVar = false;
		n.s.accept(this);
		this.currMethod = null;
		this.currClass = null;
		return null;
	}

	// Identifier i;
	// VarDeclList vl;
	// MethodDeclList ml;
	public Type visit(ClassDeclSimple n) {
		this.currClass = this.symbolTable.getClass(n.i.toString());
		n.i.accept(this);
		for (int i = 0; i < n.vl.size(); i++) {
			n.vl.elementAt(i).accept(this);
		}
		for (int i = 0; i < n.ml.size(); i++) {
			n.ml.elementAt(i).accept(this);
		}
		this.currClass = null;
		return null;
	}

	// Identifier i;
	// Identifier j;
	// VarDeclList vl;
	// MethodDeclList ml;
	public Type visit(ClassDeclExtends n) {
		this.currClass = this.symbolTable.getClass(n.i.toString());
		n.i.accept(this);
		n.j.accept(this);
		for (int i = 0; i < n.vl.size(); i++) {
			n.vl.elementAt(i).accept(this);
		}
		for (int i = 0; i < n.ml.size(); i++) {
			n.ml.elementAt(i).accept(this);
		}
		this.currClass = null;
		return null;
	}

	// Type t;
	// Identifier i;
	public Type visit(VarDecl n) {
		Type varType = n.t.accept(this);
		this.analyzeVar = true;
		n.i.accept(this);
		this.analyzeVar = false;
		return varType;
	}

	// Type t;
	// Identifier i;
	// FormalList fl;
	// VarDeclList vl;
	// StatementList sl;
	// Exp e;
	public Type visit(MethodDecl n) {
		this.currMethod = this.symbolTable.getMethod(n.i.toString(), this.currClass.getId());
		Type typeOfT = n.t.accept(this);
		this.analyzeMet = true;
		n.i.accept(this);
		this.analyzeMet = false;
		for (int i = 0; i < n.fl.size(); i++) {
			n.fl.elementAt(i).accept(this);
		}
		for (int i = 0; i < n.vl.size(); i++) {
			n.vl.elementAt(i).accept(this);
		}
		for (int i = 0; i < n.sl.size(); i++) {
			n.sl.elementAt(i).accept(this);
		}
		Type finalType = n.e.accept(this);
		if(!this.symbolTable.compareTypes(typeOfT, finalType)) {
			System.err.println("Erro: Inconsistência de tipos. Estava aguardando: " + typeOfT.toString() + ", recebi: " + finalType.toString());
			System.exit(0);
		}
		this.currMethod = null;
		return typeOfT;
	}

	// Type t;
	// Identifier i;
	public Type visit(Formal n) {
		Type paramType = n.t.accept(this);
		this.analyzeVar = true;
		n.i.accept(this);
		this.analyzeVar = false;
		return paramType;
	}

	public Type visit(IntArrayType n) {
		return n;
	}

	public Type visit(BooleanType n) {
		return n;
	}

	public Type visit(IntegerType n) {
		return n;
	}

	// String s;
	public Type visit(IdentifierType n) {
		if(this.symbolTable.containsClass(n.toString())) {
			return n;
		} else {
			System.err.println("Erro, o símbolo: " + n.toString() + " não foi reconhecido como tipo");
			System.exit(0);
		}
		return null;
	}

	// StatementList sl;
	public Type visit(Block n) {
		for (int i = 0; i < n.sl.size(); i++) {
			n.sl.elementAt(i).accept(this);
		}
		return null;
	}

	// Exp e;
	// Statement s1,s2;
	public Type visit(If n) {
		Type ifType = n.e.accept(this);
		if(!this.symbolTable.compareTypes(ifType, new BooleanType())) {
			System.err.println("Não é possível converter " + ifType.toString() + " para Boolean" );
			System.exit(0);
		}
		n.s1.accept(this);
		n.s2.accept(this);
		return null;
	}

	// Exp e;
	// Statement s;
	public Type visit(While n) {
		Type whileType = n.e.accept(this);
		if(!this.symbolTable.compareTypes(whileType, new BooleanType())) {
			System.err.println("Não é possível converter " + whileType.toString() + " para Boolean" );
			System.exit(0);
		}
		n.s.accept(this);
		return null;
	}

	// Exp e;
	public Type visit(Print n) {
		n.e.accept(this);
		return null;
	}

	// Identifier i;
	// Exp e;
	public Type visit(Assign n) {
		this.analyzeVar = true;
		Type expecType = n.i.accept(this);
		this.analyzeVar = false;
		Type finalType = n.e.accept(this);
		if(this.symbolTable.compareTypes(expecType, finalType)) {
			return null;
		} else {
			System.err.println("Erro de correspondência de tipos, esperava: " + expecType.toString() + ", recebi " + finalType.toString());
			System.exit(0);
		}
		return null;
	}

	// Identifier i;
	// Exp e1,e2;
	public Type visit(ArrayAssign n) {
		this.analyzeVar = true;
		Type iType = n.i.accept(this);
		this.analyzeVar = false;
		Type e1Type = n.e1.accept(this);
		Type e2Type = n.e2.accept(this);
		//checar se tipos batem
		if(!this.symbolTable.compareTypes(e2Type, new IntegerType())) {
			System.err.println("Erro, estava esperando IntegerType, não: " + e2Type.toString());
			System.exit(0);
		}
		if(!this.symbolTable.compareTypes(iType, new IntArrayType())) {
			System.err.println("Erro, estava esperando IntegerType, não: " + iType.toString());
			System.exit(0);
		}
		if(!this.symbolTable.compareTypes(e1Type, new IntegerType())) {
			System.err.println("Erro, estava esperando IntegerType, não: " + e1Type.toString());
			System.exit(0);
		}
		return null;
	}

	// Exp e1,e2;
	public Type visit(And n) {
		Type firstArg = n.e1.accept(this);
		Type sndArg = n.e2.accept(this);
		// checando se os argumentos são expressões booleanas
		if((!this.symbolTable.compareTypes(firstArg, new BooleanType())) || (!this.symbolTable.compareTypes(sndArg, new BooleanType()))) {
			System.err.println("Erro, operador AND requer argumentos booleanos");
			System.exit(0);
		}
		return new BooleanType();
	}

	// Exp e1,e2;
	public Type visit(LessThan n) {
		Type firstArg = n.e1.accept(this);
		Type sndArg = n.e2.accept(this);
		// checando se os argumentos são valores de tipo inteiro
		if((!this.symbolTable.compareTypes(firstArg, new IntegerType())) || (!this.symbolTable.compareTypes(sndArg, new IntegerType()))) {
			System.err.println("Erro, operador 'Menor que' requer argumentos do tipo Inteiro");
			System.exit(0);
		}
		return new BooleanType();
	}

	// Exp e1,e2;
	public Type visit(Plus n) {
		Type firstArg = n.e1.accept(this);
		Type sndArg = n.e2.accept(this);
		// checando se os argumentos são valores de tipo inteiro
		if((!this.symbolTable.compareTypes(firstArg, new IntegerType())) || (!this.symbolTable.compareTypes(sndArg, new IntegerType()))) {
			System.err.println("Erro, operador 'Soma' requer argumentos do tipo Inteiro");
			System.exit(0);
		}
		return new IntegerType();
	}

	// Exp e1,e2;
	public Type visit(Minus n) {
		Type firstArg = n.e1.accept(this);
		Type sndArg = n.e2.accept(this);
		// checando se os argumentos são valores de tipo inteiro
		if((!this.symbolTable.compareTypes(firstArg, new IntegerType())) || (!this.symbolTable.compareTypes(sndArg, new IntegerType()))) {
			System.err.println("Erro, operador 'Subtração' requer argumentos do tipo Inteiro");
			System.exit(0);
		}
		return new IntegerType();
	}

	// Exp e1,e2;
	public Type visit(Times n) {
		Type firstArg = n.e1.accept(this);
		Type sndArg = n.e2.accept(this);
		// checando se os argumentos são valores de tipo inteiro
		if((!this.symbolTable.compareTypes(firstArg, new IntegerType())) || (!this.symbolTable.compareTypes(sndArg, new IntegerType()))) {
			System.err.println("Erro, operador 'Multiplicação' requer argumentos do tipo Inteiro");
			System.exit(0);
		}
		return new IntegerType();
	}

	// Exp e1,e2;
	public Type visit(ArrayLookup n) {
		Type e1Type = n.e1.accept(this);
		Type e2Type = n.e2.accept(this);
		if(!this.symbolTable.compareTypes(e1Type, new IntArrayType())) {
			System.err.println("Erro de tipo, esperava IntArrayType mas foi recebido: " + e1Type.toString() );
			System.exit(0);
		}
		if(!this.symbolTable.compareTypes(e2Type, new IntegerType())) {
			System.err.println("Erro, aguardava um Inteiro mas recebi: " + e2Type.toString());
			System.exit(0);
		}
		return new IntegerType();
	}

	// Exp e;
	public Type visit(ArrayLength n) {
		Type arrayLenType = n.e.accept(this);
		if(!this.symbolTable.compareTypes(arrayLenType, new IntArrayType())) {
			System.err.println("Erro, aguardava um IntArrayType mas foi recebido um: " + arrayLenType.toString());
			System.exit(0);
		}
		return new IntegerType();
	}

	// Exp e;
	// Identifier i;
	// ExpList el;
	public Type visit(Call n) {
		Type expecIdenType = n.e.accept(this);
		if (expecIdenType instanceof IdentifierType) {
			int loop = 0;
			Class classC = this.symbolTable.getClass(((IdentifierType) expecIdenType).toString());
			Method methC = this.symbolTable.getMethod(n.i.toString(), classC.getId());
			Class returnClass = this.currClass;
			this.currClass = classC;
			this.analyzeMet = true;
			Type iType = n.i.accept(this);
			this.analyzeMet = false;
			this.currClass = returnClass;
			while (loop < n.el.size()) {
				Type elemType = n.el.elementAt(loop).accept(this);
				Type paramType = methC.getParamAt(loop).type();
				if(paramType == null) {
					System.err.println("Erro, parâmetro nulo ou ausente na chamda do método: " + methC.getId());
					System.exit(0);
				} else if(!this.symbolTable.compareTypes(elemType, paramType)) {
					System.err.println("Erro nos tipos, aguardava um: " + paramType.toString() + " recebi um: " + elemType.toString());
					System.exit(0);
				}
				loop++;
			}
			if(methC.getParamAt(loop).type() != null) {
				System.err.println("Erro, parâmetro nulo ou ausente na chamda do método: " + methC.getId());
				System.exit(0);
			}
			return iType;
		} else {
			System.err.println("Erro, aguardava um IdentifierType recebi um:" + expecIdenType.toString());
			System.exit(0);
		}
		return null;
	}

	// int i;
	public Type visit(IntegerLiteral n) {
		return new IntegerType();
	}

	public Type visit(True n) {
		return new BooleanType();
	}

	public Type visit(False n) {
		return new BooleanType();
	}

	// String s;
	public Type visit(IdentifierExp n) {
		Type expType = this.symbolTable.getVarType(this.currMethod, this.currClass, n.s);
		return expType;
	}

	public Type visit(This n) {
		return this.currClass.type();
	}

	// Exp e;
	public Type visit(NewArray n) {
		Type arrinxType = n.e.accept(this);
		if(!this.symbolTable.compareTypes(arrinxType, new IntegerType())) {
			System.err.println("Erro, estava aguardando um inteiro recebi: " + arrinxType.toString());
			System.exit(0);
		}
		return new IntArrayType();
	}

	// Identifier i;
	public Type visit(NewObject n) {
		return n.i.accept(this);
	}

	// Exp e;
	public Type visit(Not n) {
		Type expType = n.e.accept(this);
		if(!this.symbolTable.compareTypes(expType, new BooleanType())) {
			System.err.println("Erro, estava aguardando um valor booleano no operador 'NOT");
			System.exit(0);
		}
		return null;
	}

	// String s;
	public Type visit(Identifier n) {
		if (this.analyzeVar) {
			//estamos analisando uma variável
			return this.symbolTable.getVarType(this.currMethod, this.currClass, n.toString());
		} else if (this.analyzeMet) {
			//estamos analisando um método
			return this.symbolTable.getMethodType(n.toString(), this.currClass.getId());
		} else if (this.symbolTable.getClass(n.toString()) == null) {
			System.err.println("Erro, não existe variável com esse nome: " + n.toString());
			System.exit(0);
		} else {
			return this.symbolTable.getClass(n.toString()).type();
		}
		return null;
	}
}
