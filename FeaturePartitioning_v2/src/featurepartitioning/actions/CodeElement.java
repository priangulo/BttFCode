package featurepartitioning.actions;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Type;

import bttf.ElementType;

public class CodeElement {
	private static Set<String> primitive = new HashSet<String>();
    static {		
		primitive.add("byte");
		primitive.add("short");
		primitive.add("int");
		primitive.add("long");
		primitive.add("float");
		primitive.add("double");
		primitive.add("char");
		primitive.add("String");
		primitive.add("boolean");
    }
    
	String name;
	String modifier;
	ElementType type;
	String declaring_class;
	String code;
	boolean is_primitive_or_proprietary;
	boolean is_terminal = true;
	String superclass;
	boolean is_null = false;
	
	public CodeElement(String name, String modifier, ElementType type, String declaring_class, String code, Set<String> packages) {
		this.name = name;
		this.modifier = modifier;
		this.type = type;
		this.declaring_class = declaring_class;
		this.code = code;
		if(checkName(this.name, false, true, packages)){
			this.is_primitive_or_proprietary = false;
		}
		else{
			this.is_primitive_or_proprietary = true;
		}
		
	}

	CodeElement(IPackageBinding package_binding, Set<String> packages){
		if(package_binding != null){
			this.name = (package_binding.isUnnamed()) ? "default" : package_binding.getName();
			this.modifier = get_modifier(package_binding.getModifiers());
			this.type = ElementType.ELEM_TYPE_PACKAGE;
			this.declaring_class = null;
			this.code = PartitioningConstants.PACKAGE_CODE_STRING;
			if(checkName(this.name, true, false, packages)){
				this.is_primitive_or_proprietary = false;
			}
			else{
				this.is_primitive_or_proprietary = true;
			}
		}
		else{this.is_null = true; }
	}
	
	CodeElement(ITypeBinding type_binding, Set<String> packages, boolean is_terminal){
		if(type_binding != null){
			setClassAttributes(type_binding, packages, is_terminal);
		}
		else{this.is_null = true; }
	}
	
	CodeElement(ITypeBinding type_binding, String code, Set<String> packages, boolean is_terminal){
		if(type_binding != null){
			setClassAttributes(type_binding, packages, is_terminal);
			this.code = code;
		}
		else{this.is_null = true; }
	}
	
	private void setClassAttributes(ITypeBinding type_binding, Set<String> packages, boolean is_terminal){
		if(type_binding != null){
			if(type_binding.isArray()){
				this.name = type_binding.getElementType().getQualifiedName();
			}
			else{
				this.name = type_binding.getQualifiedName();
			}
			this.modifier = get_modifier(type_binding.getModifiers());
			this.type = ElementType.ELEM_TYPE_CLASS;
			this.declaring_class = null;
			this.code = type_binding.toString();
			this.is_terminal = is_terminal;
			
			if(type_binding.getSuperclass() != null){
				this.superclass = type_binding.getSuperclass().getQualifiedName();
			}
			
			if(checkNotBindingPrimitive(type_binding) && checkName(this.name, false, true, packages)){
				this.is_primitive_or_proprietary = false;
			}
			else{
				this.is_primitive_or_proprietary = true;
			}
		}
	}
	
	CodeElement(IMethodBinding method_binding, MethodDeclaration node, Set<String> packages){
		if(method_binding != null && method_binding.getDeclaringClass() != null){
			setMethodAttributes(method_binding, node, packages);
		}
		else{this.is_null = true; }
	}
	
	CodeElement(IMethodBinding method_binding, MethodDeclaration node, String code, Set<String> packages){
		if(method_binding != null && method_binding.getDeclaringClass() != null){
			setMethodAttributes(method_binding, node, packages);
			this.code = code;
		}
		else{this.is_null = true; }
	}
	
	private void setMethodAttributes(IMethodBinding method_binding, MethodDeclaration node, Set<String> packages){
		if(method_binding != null && method_binding.getDeclaringClass() != null && !node.resolveBinding().getDeclaringClass().isAnonymous()) {
			this.declaring_class = method_binding.getDeclaringClass().getQualifiedName();
			this.name = declaring_class + "." + method_binding.getName() + getMethodParams(node);
			this.modifier = get_modifier(method_binding.getModifiers());
			this.type = ElementType.ELEM_TYPE_METHOD;
			this.code = method_binding.toString();
			
			if(checkName(this.name, false, true, packages)){
				this.is_primitive_or_proprietary = false;
			}
			else{
				this.is_primitive_or_proprietary = true;
			}
			
			/*
			 * //anonymous classes are out of scope, annotating them is horrible
			 * //this is the code for getting the actual declaring class
				IBinding anonymous_binding = node.resolveBinding().getDeclaringClass().getDeclaringMember();
				if (anonymous_binding.getKind() == IBinding.VARIABLE){
					IVariableBinding var_anon_binding =  (IVariableBinding) anonymous_binding;
					this.declaring_class = var_anon_binding.getDeclaringClass().getQualifiedName();
				}
				if (anonymous_binding.getKind() == IBinding.METHOD){
					IMethodBinding method_anon_binding = (IMethodBinding)  anonymous_binding;
					this.declaring_class = method_anon_binding.getDeclaringClass().getQualifiedName();
				}
			 */
		}
		else{this.is_null = true; }
	}
	
	private String getMethodParams(MethodDeclaration node){
		StringBuilder params = new StringBuilder("(");
		if(node.parameters() != null && !node.parameters().isEmpty()){
			//String[] parameters = node.parameters().toString().replace("[", "").replace("]", "").split(",");
			String[] parameters = node.parameters().toString().substring(1, (node.parameters().toString().length()-1)).split(",");
			String comma = "";
			for(int i = 0; i < parameters.length; i++){
				params.append(comma);
				comma = ", ";
				params.append(parameters[i].trim());
			}
		}
		params.append(")");
		return params.toString();
	}
	
	CodeElement(IVariableBinding variable_binding, Set<String> packages){
		if(variable_binding != null && variable_binding.getDeclaringClass() != null && variable_binding.getVariableDeclaration() != null){
			setFieldAttributes(variable_binding, packages);
		}
		else{this.is_null = true; }
	}
	
	CodeElement(IVariableBinding variable_binding, String code, Set<String> packages){
		if(variable_binding != null && variable_binding.getDeclaringClass() != null && variable_binding.getVariableDeclaration() != null){
			setFieldAttributes(variable_binding, packages);
			this.code = code;
		}
		else{this.is_null = true; }
	}
	
	private void setFieldAttributes(IVariableBinding variable_binding, Set<String> packages){
		if(variable_binding != null && variable_binding.getName() != null && variable_binding.getDeclaringClass() != null && variable_binding.getVariableDeclaration() != null){
			this.declaring_class = variable_binding.getDeclaringClass().getQualifiedName();
			this.name = declaring_class + "." + variable_binding.getName();
			this.modifier = get_modifier(variable_binding.getVariableDeclaration().getModifiers());
			this.type = ElementType.ELEM_TYPE_FIELD;
			this.code = variable_binding.getVariableDeclaration().toString();
			
			if(checkName(this.name, false, false, packages)){
				this.is_primitive_or_proprietary = false;
			}
			else{
				this.is_primitive_or_proprietary = true;
			}
		}
	}
	
	private String get_modifier(int flags_mod){
		if(Modifier.isPrivate(flags_mod)) return PartitioningConstants.PRIVATE_MOD;
		else if(Modifier.isPublic(flags_mod)) return PartitioningConstants.PUBLIC_MOD;
		else return "";
	}
	
	private boolean checkNotBindingPrimitive(ITypeBinding binding ){
		if (binding.isPrimitive()){
			return false;
		}
		return true;
	}
	
	private boolean checkNotTypePrimitive(Type type){
		if( type == null ){
			return true;
		}
		if( !type.isPrimitiveType() )
		{
			return true;
		}
		return false;
	}
	
	private boolean checkName(String name, Boolean is_package, Boolean check_primitive, Set<String> packages){
		//System.out.println(packages.toString());
		for(String pack : packages){
			if ((is_package && name.startsWith(pack)) || (!is_package && name.startsWith(pack + ".")) ){
				if(check_primitive && !primitive.contains(name)){
					return true;
				}
				if(!check_primitive){
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "CodeElement [name=" + name + ", is_primitive_or_proprietary=" + is_primitive_or_proprietary + "]";
	}
}
