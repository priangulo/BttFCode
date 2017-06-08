package featurepartitioning.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Our sample action implements workbench action delegate.
 * The action proxy will be created by the workbench and
 * shown in the UI. When the user tries to use the action,
 * this delegate will be created and execution will be 
 * delegated to it.
 * @see IWorkbenchWindowActionDelegate
 */
public class PartitionAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;
	/**
	 * The constructor.
	 */
	public PartitionAction() {
	}

	/**
	 * The action has been activated. The argument of the
	 * method represents the 'real' action sitting
	 * in the workbench UI.
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	
	boolean checkBindingPrimitive(ITypeBinding binding ){
		if (!binding.isPrimitive()){
			return true;
		}
		return false;
	}
	
	boolean checkTypePrimitive(Type type){
		if(!type.isPrimitiveType())
		{
			return true;
		}
		return false;
	}
	
	boolean checkName (String name){
		if (!name.startsWith("java."))
		{
				return true;
			
		}
		return false;
	}
	
	void addDependency (Map<String, List<String>> dependencies, String key, String value){
		if(dependencies.containsKey(key)){
			List<String> values = new ArrayList<String>();
			values = dependencies.get(key);
			if(!values.contains(value)){
				values.add(value);
				dependencies.put(key, values);
			}
		}
		else{
			List<String> values = new ArrayList<String>();
			values.add(value);
			dependencies.put(key, values);
		}
	}
	
	void printDependencies(Map<String, List<String>> dependencies){
		for(Map.Entry<String, List<String>> entry : dependencies.entrySet()){
			for(String value : entry.getValue()){
				System.out.println(entry.getKey() + " -> " + value );
			}
		}
	}
	
	void partition_horizontal(Map<String, List<String>> dependencies){
		if(dependencies != null && !dependencies.isEmpty()){
			HashSet<String> partition = new HashSet<String>();
			
			for(List<String> values_list : dependencies.values()){
				for(String value : values_list){
					//System.out.println(value);
					if(!dependencies.containsKey(value)){
						partition.add(value);
					}
					//System.out.println("dependencies.containsKey(value)" + value);
				}
			}

			System.out.println(partition);
			
			for(String removeEntry: partition){
				Iterator<Map.Entry<String, List<String>>> iterator = dependencies.entrySet().iterator() ;
		        while(iterator.hasNext()){
		            Map.Entry<String, List<String>> entry = iterator.next();
		            entry.getValue().remove(removeEntry);
		            if(entry.getValue().isEmpty()){
		            	iterator.remove();
		            }
		        }
			}
						
			//printDependencies(dependencies);
			
			partition_horizontal(dependencies);
		}
	}
	
	void partition_vertical(Map<String, List<String>> dependencies){
		/*for(String key : dependencies.keySet()){
			System.out.println(key);
			System.out.println(dependencies.get(key));
		}*/
		append_new_partition(dependencies, "", "");
		
		//System.out.println(partitions);
	}

	void append_new_partition(Map<String, List<String>> dependencies, String current_partition, String value){
		for(String key : dependencies.keySet()){
			//System.out.println(key);
			//System.out.println(value.isEmpty());
			if(key.equals(value) || value.isEmpty()){
				//System.out.println(dependencies.get(key));
				for(String inner_value: dependencies.get(key)){
					//System.out.println("    " + inner_value);
					current_partition = current_partition + " -> " + inner_value;
					System.out.println(current_partition);
					append_new_partition(dependencies, current_partition, inner_value );
				}
				
			}
		}
	}
	
	public void run(IAction action) {
		IWorkspace iWorkspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot iWorkspaceRoot = iWorkspace.getRoot();
		IProject[] iProjectList = iWorkspaceRoot.getProjects();
		for(IProject iProject : iProjectList) {
			final IJavaProject iJavaProject = JavaCore.create(iProject);
			final Map<String, List<String>> dependencies = new HashMap<String, List<String>>();
			
			//if(iJavaProject.getElementName().compareTo("P") != 0) continue;
			
			try {
				IPackageFragment[] iPackageFragmentList = iJavaProject.getPackageFragments();
				for(IPackageFragment iPackageFragment : iPackageFragmentList) {
					if(iPackageFragment.getKind() != IPackageFragmentRoot.K_SOURCE) {
						continue;
					}
					
					
					ICompilationUnit[] iCompilationUnitList = iPackageFragment.getCompilationUnits();
					for(ICompilationUnit iCompilationUnit : iCompilationUnitList) {
						ASTParser astParser = ASTParser.newParser(AST.JLS3);
						astParser.setResolveBindings(true);
						astParser.setSource(iCompilationUnit);						
						CompilationUnit compilationUnit = (CompilationUnit)astParser.createAST(null);
						
						compilationUnit.accept(new ASTVisitor(){
							public boolean visit(PackageDeclaration node) {								
								return true;
							}
							
							public boolean visit(TypeDeclaration node){
								String class_name = node.resolveBinding().getQualifiedName();
								List<Type> class_interfaces = new ArrayList<Type>();
								class_interfaces = node.superInterfaceTypes();
								for (Type t : class_interfaces){
									if(checkBindingPrimitive(t.resolveBinding()) && checkName(t.resolveBinding().getQualifiedName()))
									{
										//System.out.println(class_name + " -> " + t.resolveBinding().getQualifiedName());
										addDependency(dependencies, class_name, t.resolveBinding().getQualifiedName());
									}
								}
								return true;
							}
							
							public boolean visit(MethodDeclaration node) {
								String method_name = node.resolveBinding().getDeclaringClass().getQualifiedName() + "." + node.getName().getFullyQualifiedName();
								if(checkTypePrimitive(node.getReturnType2())){
									//System.out.println( method_name + " -> " +  node.getReturnType2());
									addDependency(dependencies, method_name, node.getReturnType2().toString());
								}
								
								List<SingleVariableDeclaration> method_params = new ArrayList<SingleVariableDeclaration>();
								method_params = node.parameters();
								for(SingleVariableDeclaration param : method_params){
									if(checkBindingPrimitive(param.getType().resolveBinding()) && checkName(param.getType().resolveBinding().getQualifiedName()))
									{
										//System.out.println( method_name + " -> " +  param.getType().resolveBinding().getQualifiedName());
										addDependency(dependencies, method_name, param.getType().resolveBinding().getQualifiedName());
									}
								}
								return true;

							}
							
							public boolean visit(VariableDeclarationFragment node){
								final String variable_key = node.resolveBinding().getKey();
								
								node.accept(new ASTVisitor() {
									public boolean visit(SimpleName node) {
										IBinding binding = node.resolveBinding();
										if (binding != null) {
											if (binding.getKind() == IBinding.VARIABLE){
												IVariableBinding variableBinding = (IVariableBinding)binding;
												if(checkName(variableBinding.getVariableDeclaration().getType().getQualifiedName()) && checkBindingPrimitive(variableBinding.getVariableDeclaration().getType())){
													//System.out.println(variable_key + " -> " + variableBinding.getVariableDeclaration().getType().getQualifiedName());
													addDependency(dependencies, variable_key, variableBinding.getVariableDeclaration().getType().getQualifiedName());
												}
											}
											else if (binding.getKind() == IBinding.METHOD){
												IMethodBinding methodBinding = (IMethodBinding)binding;
												if(checkName(methodBinding.getDeclaringClass().getQualifiedName())){
													//System.out.println(variable_key + " -> " + methodBinding.getDeclaringClass().getQualifiedName());
													addDependency(dependencies, variable_key, methodBinding.getDeclaringClass().getQualifiedName());
												}
											}
											else if (binding.getKind() == IBinding.TYPE){
												ITypeBinding typeBinding = (ITypeBinding)binding;
												if (checkName(typeBinding.getSuperclass().getQualifiedName()))
												{
													//System.out.println(variable_key + " -> " + typeBinding.getSuperclass().getQualifiedName());
													addDependency(dependencies, variable_key, typeBinding.getSuperclass().getQualifiedName());
												}
												if (checkName(typeBinding.getQualifiedName()))
												{
													//System.out.println(variable_key + " -> " + typeBinding.getQualifiedName());
													addDependency(dependencies, variable_key, typeBinding.getQualifiedName());
												}
												
												try {
													IType type = iJavaProject.findType(typeBinding.getQualifiedName());
													if(type != null){
														ITypeHierarchy typeHierarchie = type.newTypeHierarchy(new NullProgressMonitor());
														for(IType t : typeHierarchie.getSuperInterfaces(type)){
															if (checkName(t.getFullyQualifiedName()))
															{
																//System.out.println(variable_key + " -> " + t.getFullyQualifiedName());
																addDependency(dependencies, variable_key, t.getFullyQualifiedName());
															}
															
														}
													}
												} 
												catch (JavaModelException ex) {
													System.out.println("  error on getting interfaces");
													ex.printStackTrace();
													
												}
											}
										}
										return true;
									}
								});
								return true;
							}
							
							public boolean visit(FieldDeclaration node) {
								return true;
							}
						});
					}
				}
				
				printDependencies(dependencies);
				System.out.println("****************");
				Map<String, List<String>> dep_copy_hor = new HashMap<String, List<String>>(dependencies);
				Map<String, List<String>> dep_copy_ver = new HashMap<String, List<String>>(dependencies);
				
				partition_vertical(dep_copy_ver);
				System.out.println("****************");
				partition_horizontal(dep_copy_hor);
			}
			catch (JavaModelException e) 
			{
				e.printStackTrace();
			}		
		}
	}

	/**
	 * Selection in the workbench has been changed. We 
	 * can change the state of the 'real' action here
	 * if we want, but this can only happen after 
	 * the delegate has been created.
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * We can use this method to dispose of any system
	 * resources we previously allocated.
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to
	 * be able to provide parent shell for the message dialog.
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}