package featurepartitioning.actions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IRegion;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import bttf.Element;
import bttf.ElementType;
import bttf.Partition;
import bttf.Reference;
import gui.BttFMain;

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
	private final boolean debug = false;
	private Set<String> packages;
	private ArrayList<CompilationUnit> compilationUnits;
	private IJavaProject iJavaProject;
	private ArrayList<String> allSuperClasses;
	private double total_classes = 0;
	private double count_classes = 0;
	
	/**
	 * The constructor.
	 */
	public PartitionAction() {}

	/**
	 * The action has been activated. The argument of the
	 * method represents the 'real' action sitting
	 * in the workbench UI.
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {		
		IWorkspace iWorkspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot iWorkspaceRoot = iWorkspace.getRoot();
		IProject[] iProjectList = iWorkspaceRoot.getProjects();
		boolean open_project = false;
		
		packages = new HashSet<String>();
		compilationUnits = new ArrayList<CompilationUnit>();
		allSuperClasses = new ArrayList<String>();
		total_classes = 0;
		count_classes = 0;
		
		
		for(IProject iProject : iProjectList) {
			if(iProject.isOpen()){
				open_project = true;
				String project_path = iProject.getLocation().toString();
				//create bttf window
				BttFMain bttf_main_window = new BttFMain(project_path, iProject.getName());
				
				iJavaProject = JavaCore.create(iProject);
				final ArrayList<Reference> references = new ArrayList<Reference>();
				
				try {
					IPackageFragment[] iPackageFragmentList = iJavaProject.getPackageFragments();
					for(IPackageFragment iPackageFragment : iPackageFragmentList) {

						if(iPackageFragment.getKind() != IPackageFragmentRoot.K_SOURCE) {
							continue;
						}
						
						ICompilationUnit[] iCompilationUnitList = iPackageFragment.getCompilationUnits();
						for(ICompilationUnit iCompilationUnit : iCompilationUnitList) {
							for(IPackageDeclaration iPackageDeclaration : iCompilationUnit.getPackageDeclarations()){
								packages.add(iPackageDeclaration.getElementName());
							}
						}
						
						for(ICompilationUnit iCompilationUnit : iCompilationUnitList) {
							total_classes += iCompilationUnit.getAllTypes().length;  
							for(IType type : iCompilationUnit.getAllTypes()){
								if(type != null && type.getSuperclassName() != null){
									String superclassname = iCompilationUnit.getType(type.getSuperclassName()).getFullyQualifiedName();
									if(superclassname != null && !allSuperClasses.contains(superclassname)){
										allSuperClasses.add(superclassname);
									}
								}
							}
							
							ASTParser astParser = ASTParser.newParser(AST.JLS3);
							astParser.setResolveBindings(true);
							astParser.setSource(iCompilationUnit);						
							CompilationUnit compilationUnit = (CompilationUnit)astParser.createAST(null);
							
							compilationUnits.add(compilationUnit);
							
						}
						
						//System.out.println(allSuperClasses.toString());
						
						for(CompilationUnit compilationUnit : compilationUnits){
							StringBuilder progress_text = new StringBuilder();
							compilationUnit.accept(new ASTVisitor(){
								/*
								 * VISIT PACKAGE
								 */
								public boolean visit(PackageDeclaration node) {	
									return true;
								}
								
								/*
								 * VISIT TYPE DECLARATION
								 */
								public boolean visit(TypeDeclaration node){
									CodeElement from = new CodeElement(node.resolveBinding(), node.toString(), packages, isClassTerminal(node.resolveBinding()));
									
									//get references to interfaces
									List<Type> class_interfaces = new ArrayList<Type>();
									class_interfaces = node.superInterfaceTypes();
									for (Type t : class_interfaces){
										CodeElement to = new CodeElement(t.resolveBinding(), packages, isClassTerminal(t.resolveBinding()));
										add_reference(references, from, to);
									}
									
									//add references for type binding
									get_typebinding_references(node.resolveBinding(), references, from);
									
									count_classes++;
									progress_text.append(from.name);
									
									return true;
								}
								
								/*
								 * VISIT ENUM DECLARATION
								 */
								public boolean visit(EnumDeclaration node) {
									CodeElement from = new CodeElement(node.resolveBinding(), node.toString(), packages, isClassTerminal(node.resolveBinding()));
									//add references for type binding
									get_typebinding_references(node.resolveBinding(), references, from);
									
									count_classes++;
									progress_text.append(from.name);
									
									return true;
								}
								
								/*
								 * VISIT METHOD DECLARATION
								 */
								public boolean visit(MethodDeclaration node) {
									CodeElement from = new CodeElement(node.resolveBinding(), node, node.toString(), packages);
									
									//reference to declaring class
									get_methodbinding_references(node.resolveBinding(), references, from);
									
									//reference to overrided methods
									if(node.resolveBinding() != null && node.resolveBinding().getDeclaringClass()!= null){
										ITypeBinding[] declaring_class_interfaces = node.resolveBinding().getDeclaringClass().getInterfaces();
										for(ITypeBinding class_interface : declaring_class_interfaces){
											for (IMethodBinding method_interface : class_interface.getDeclaredMethods()){
												if(node.resolveBinding().overrides(method_interface)){
													MethodDeclaration method_interface_dec = findMethodDeclaration(method_interface);
													if(method_interface_dec != null){
														CodeElement method_interface_celem = new CodeElement(method_interface, method_interface_dec, packages);
														if(method_interface_celem != null){
															add_reference(references, from, method_interface_celem);
														}
															
													}
													CodeElement class_interface_celem = new CodeElement(class_interface, null, packages, isClassTerminal(class_interface));
													if(class_interface_celem != null){
														add_reference(references, from, class_interface_celem);
													}
												}
											}
										}
									}
									
									//reference to return type
									if(node.getReturnType2() != null){
										add_reference(references, from, new CodeElement(node.getReturnType2().resolveBinding(), packages, isClassTerminal(node.getReturnType2().resolveBinding())));
									}
									
									//references to parameter types
									List<SingleVariableDeclaration> method_params = new ArrayList<SingleVariableDeclaration>();
									method_params = node.parameters();
									for(SingleVariableDeclaration param : method_params){
										if(param != null && param.getType() != null && param.getType().resolveBinding() != null){
											CodeElement to = new CodeElement(param.getType().resolveBinding(), packages, isClassTerminal(param.getType().resolveBinding()));
											//System.out.println("from: " + from.toString());
											//System.out.println("to: " + to.toString());
											add_reference(references, from, to);
										}
									}
									
									//look the elements inside the method and get corresponding references
									node.accept(new ASTVisitor() {
										public boolean visit(SimpleName var_node) {
											visit_SimpleName(var_node, references, from);
											return true;
										}
									});
									
									return true;
								}
								
								/*
								 * VISIT METHOD INVOCATION
								 */
								public boolean visit(MethodInvocation node){
									MethodDeclaration to_decl = findMethodDeclaration(node.resolveMethodBinding());
									if(to_decl != null){
										CodeElement to = new CodeElement(node.resolveMethodBinding(), to_decl, node.toString(), packages);
										
										if(!to.is_null && !to.is_primitive_or_proprietary){
											//references to caller methods
											ASTNode parentNode = node.getParent();
											while ( parentNode != null && parentNode.getNodeType() != ASTNode.METHOD_DECLARATION) {
										        parentNode = parentNode.getParent();
										    }
										    MethodDeclaration from_method = (MethodDeclaration) parentNode;
										    if(from_method != null && from_method.resolveBinding() != null && from_method.resolveBinding().getDeclaringClass() != null){
										    	CodeElement from = new CodeElement(from_method.resolveBinding(), from_method,  packages);
											    add_reference(references, from, to);
										    }
										}
									}
									return true;
								}
								
								/*
								 * VISIT INITIALIZERS
								 */
								public boolean visit(Initializer node){
									String class_name = node.getClass().getName();
									String package_name = node.getClass().getPackage().getName();
									String initializer_name = PartitioningConstants.INITIALIZER_PREFIX + node.getStartPosition();
									CodeElement from = new CodeElement(package_name + "." + class_name + "." + initializer_name,
											"Public", ElementType.ELEM_TYPE_METHOD, package_name + "." + class_name, node.getBody().toString(), packages);
									
									//look the elements inside the initializer and get corresponding references
									node.getBody().accept(new ASTVisitor() {
										public boolean visit(SimpleName var_node) {
											visit_SimpleName(var_node, references, from);
											return true;
										}
									});
									
									return true;
								}
								
								/*
								 * VISIT ENUM CONSTANT DECLARATION
								 */
								public boolean visit(EnumConstantDeclaration node){
									if(!node.resolveConstructorBinding().isDefaultConstructor()){
										CodeElement from_method = new CodeElement(node.resolveConstructorBinding(), findMethodDeclaration(node.resolveConstructorBinding()), packages);
										//method binding references
										get_methodbinding_references(node.resolveConstructorBinding(), references, from_method);
									}
									
									if(!node.resolveVariable().isSynthetic()){
										CodeElement from_field = new CodeElement(node.resolveVariable(), packages);
										//variable binding references
										get_variablebinding_references(node.resolveVariable(), references, from_field);
									}
									
									return true;
								}
								
								/*
								 * VISIT FIELD DECLARATION
								 */
								public boolean visit(FieldDeclaration node) {
									VariableDeclarationFragment inner_vardec = (VariableDeclarationFragment)node.fragments().get(0);
									CodeElement from = new CodeElement(inner_vardec.resolveBinding(), node.toString(), packages);
									
									//get references to type, superclass and interfaces
									get_typebinding_references(node.getType().resolveBinding(), references, from);

									node.getType().accept(new ASTVisitor(){
										public boolean visit(SimpleName var_node) {
											visit_SimpleName(var_node, references, from);
											return true;
										}
									});
									
									List fragments = node.fragments();
									for (Iterator iterator = fragments.iterator(); iterator.hasNext();) {
										inner_vardec = (VariableDeclarationFragment) iterator.next();
										CodeElement inner_from = new CodeElement(inner_vardec.resolveBinding(), packages);
										
										//variable binding references
										get_variablebinding_references(inner_vardec.resolveBinding(), references, inner_from);
										
										if(inner_vardec.resolveBinding() != null && inner_vardec.resolveBinding().getType() != null){
											//get references to type, superclass and interfaces
											get_typebinding_references(inner_vardec.resolveBinding().getType(), references, inner_from);
										}
											
									}									
									return true;
								}
								
								/*
								 * VISIT VARIABLE DECLARATION
								 */
								public boolean visit(VariableDeclarationFragment node){
									return true;
								}
								
							});

							bttf_main_window.update_progress((int)Math.ceil((count_classes/total_classes)*100), progress_text.toString(), false);
						}
					}
					
					if(debug){
						System.out.println("****************");
						System.out.println("REFERENCES");
						System.out.println(references.toString());
						System.out.println("****************");
					}
					System.out.println("PartitionAction -  Finished getting CRG.");
					Partition partition = new Partition(references, iProject.getName());
					bttf_main_window.start_partitioning(partition);
				}
				catch (JavaModelException e) 
				{
					e.printStackTrace();
					bttf_main_window.update_progress((int)Math.ceil((count_classes/total_classes)*100), e.toString() + getErrorMessage(e.getStackTrace()), true);
					
				}
				catch (Exception ex){
					ex.printStackTrace();
					bttf_main_window.update_progress((int)Math.ceil((count_classes/total_classes)*100), ex.toString() + getErrorMessage(ex.getStackTrace()), true);
				}
				break;
			}
		}
		if(!open_project){
			//create bttf window and send no project error
			BttFMain bttf_main_window = new BttFMain();
		}
		this.dispose();
	}
	
	private String getErrorMessage(StackTraceElement[] errorElements){
		StringBuilder sb = new StringBuilder();
		if(errorElements != null){
			for(StackTraceElement el : errorElements){
				sb.append("\r\n"+el.toString());
			}
		}
		return sb.toString();
	}
	
	void visit_SimpleName(SimpleName node, ArrayList<Reference> references, CodeElement elem){
		IBinding binding = node.resolveBinding();
		if (binding != null) {  
			if (binding.getKind() == IBinding.VARIABLE){
				get_variablebinding_references((IVariableBinding)binding, references, elem);
			}
			if (binding.getKind() == IBinding.METHOD){
				get_methodbinding_references((IMethodBinding)binding, references, elem);
			}
			else if (binding.getKind() == IBinding.TYPE){
				get_typebinding_references((ITypeBinding)binding, references, elem);
			}
		}
	}
	
	private void get_variablebinding_references(IVariableBinding variableBinding, ArrayList<Reference> references, CodeElement from){
		if(variableBinding != null){
			//reference to declaring class
			if(variableBinding.getDeclaringClass() != null){
				add_reference(references, from, new CodeElement(variableBinding.getDeclaringClass(), packages, isClassTerminal(variableBinding.getDeclaringClass())));
			}
			
			//reference to variable type
			if(variableBinding.getVariableDeclaration() != null && variableBinding.getVariableDeclaration().getType() != null){
				add_reference(references, from, new CodeElement(variableBinding.getVariableDeclaration().getType(), packages, isClassTerminal(variableBinding.getDeclaringClass())));
			}
			
			//reference to fields
			if(variableBinding.isField()){
				CodeElement to = new CodeElement(variableBinding, packages);
				//System.out.println(from.toString());
				//System.out.println(to.toString());
				add_reference(references, from, to);
			}
		}
	}
	
	private void get_methodbinding_references(IMethodBinding methodBinding, ArrayList<Reference> references, CodeElement from ){
		//reference to declaring class
		if(methodBinding != null && methodBinding.getDeclaringClass() != null){
			add_reference(references, from, new CodeElement(methodBinding.getDeclaringClass(), packages, isClassTerminal(methodBinding.getDeclaringClass())));
		}
	}
	
	private void get_typebinding_references(ITypeBinding typeBinding, ArrayList<Reference> references, CodeElement from){
		if(typeBinding != null){
			//reference to package
			add_reference(references, from, new CodeElement(typeBinding.getPackage(), packages));
			
			//reference to type
			if (typeBinding.getQualifiedName() != null && !typeBinding.getQualifiedName().equals(from.name))
			{
				add_reference(references, from, new CodeElement(typeBinding, packages, isClassTerminal(typeBinding)));
			}
			
			//reference to wildcard
			if(typeBinding.getWildcard() != null && typeBinding.getWildcard().getQualifiedName() != null){
				add_reference(references, from, new CodeElement(typeBinding.getWildcard(), packages, isClassTerminal(typeBinding.getWildcard())));
			}
			//reference to wildcard bound
			if(typeBinding.getBound() != null && typeBinding.getBound().getQualifiedName() != null){
				add_reference(references, from, new CodeElement(typeBinding.getBound(), packages, isClassTerminal(typeBinding.getBound())));
			}
			
			//reference to superclass
			if (typeBinding.getSuperclass() != null && typeBinding.getSuperclass().getQualifiedName() != null)
			{
				CodeElement superclass = new CodeElement(typeBinding.getSuperclass(), packages, isClassTerminal(typeBinding.getSuperclass()));
				add_reference(references, from, superclass);
				
				//reference to implemented/overrided methods in this class
				if(typeBinding.getSuperclass().getDeclaredMethods()!= null){
					IMethodBinding[] methods = typeBinding.getDeclaredMethods();
					for(IMethodBinding method : methods){
						if(!method.isDefaultConstructor()){
							MethodDeclaration frommethod_dec = findMethodDeclaration(method);
							if(frommethod_dec != null){
								CodeElement from_method = new CodeElement(method, findMethodDeclaration(method), packages); 
								for(IMethodBinding superclass_method : typeBinding.getSuperclass().getDeclaredMethods()){
									MethodDeclaration supermethod_dec = findMethodDeclaration(superclass_method);
									if(supermethod_dec != null){
										CodeElement to_method = new CodeElement(superclass_method, supermethod_dec, packages);
										if( method.overrides(superclass_method) || method.isSubsignature(superclass_method) ){
											add_reference(references, from_method, to_method);
											add_reference(references, from_method, superclass);
										}
									}
								}
							}
						}
					}
				}
			}
			
			//references to interfaces
			ITypeBinding[] interfaces = typeBinding.getInterfaces();
			if (interfaces != null && interfaces.length > 0){
				for (ITypeBinding inter : interfaces){
					if(inter != null){
						CodeElement to = new CodeElement(inter, packages, isClassTerminal(inter));
						if(to != null){
							add_reference(references, from, to);
						}
					}
				}
			}
			
			//references to annotations
			if (typeBinding.getTypeAnnotations() != null){
				for(IAnnotationBinding annotation : typeBinding.getTypeAnnotations()){
					if(annotation.getAnnotationType() != null && annotation.getAnnotationType().getQualifiedName() != null){
						add_reference(references, from, new CodeElement(annotation.getAnnotationType(), packages, isClassTerminal(annotation.getAnnotationType())));
					}
				}
			}
		}
	}
	
	private MethodDeclaration findMethodDeclaration(IMethodBinding method_binding){
		for(CompilationUnit compilationUnit: compilationUnits){
			if (method_binding != null && method_binding.getKey() != null && compilationUnit.findDeclaringNode(method_binding.getKey()) != null 
					&& compilationUnit.findDeclaringNode(method_binding.getKey()) instanceof MethodDeclaration){
				return (MethodDeclaration)compilationUnit.findDeclaringNode(method_binding.getKey());
			}
		}
		return null;
	}
	
	private boolean isClassTerminal(ITypeBinding type_binding){
		if (type_binding != null && type_binding.getQualifiedName() != null && allSuperClasses.contains(type_binding.getQualifiedName())){
			return false;
		}
		return true;
	}
	
	private void add_reference(ArrayList<Reference> references, CodeElement from, CodeElement to){
		if(from != null && to != null && from.is_null == false && to.is_null == false && from.is_primitive_or_proprietary == false && to.is_primitive_or_proprietary == false && !from.name.equals(to.name)){
			Reference reference = new Reference(from.name, to.name, from.type, to.type, from.modifier, to.modifier, from.code, to.code, from.is_terminal, to.is_terminal);
			if(!references.contains(reference)){ 
				references.add(reference); 
			}
			else{
				Reference ref = references.stream().filter(r -> r.getCall_from().equals(from.name) && r.getCall_to().equals(to.name)).collect(Collectors.toList()).get(0);
				if (ref != null){
					if(ref.getCall_from_code() == null && from.code != null){
						ref.setCall_from_code(from.code);
					}
					if(ref.getCall_to_code() == null && to.code != null){
						ref.setCall_to_code(to.code);
					}
					if(from.code != null && ref.getCall_from_code() != null && ref.getCall_from_code().length() < from.code.length()){
						ref.setCall_from_code(from.code);
					}
					if(to.code != null && ref.getCall_to_code() != null && ref.getCall_to_code().length() < to.code.length()){
						ref.setCall_to_code(to.code);
					}
				}
			}
		}
	}
	
	public Set<String> get_packages(){
		return this.packages;
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