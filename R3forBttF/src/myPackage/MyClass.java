package myPackage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.omg.CORBA.INITIALIZE;

import bttf.Element;
import bttf.ElementType;
import bttf.FWPlBelongLevel;
import bttf.Partition;
import bttf.PartitionHelper;
import gui.GuiConstants;
import gui.InputFile;
import p.actions.*;

import java.awt.Frame;
import java.io.BufferedReader;
import java.io.FileReader;

public class MyClass {
	Partition partition;
	ArrayList<String> notFound = new ArrayList<String>();
	ArrayList<String> lifted = new ArrayList<String>();
	private String start_path = System.getProperty("user.home") + "\\Desktop\\BttF";
	
	public void myMethod() {
		InputFile inputFile = new InputFile();
		Frame parentWindow = new Frame("Bttf-R3");
		//String file_name = "C:\\Users\\Priscila\\Desktop\\BttF\\ImageTaskGangApplication\\20170622 00.34\\FactsAndInferences.csv";
		//String file_name = "C:\\Users\\Priscila\\Desktop\\BttF\\ImageTaskGangApplication\\FactsAndInferences.csv";
		
		String file_name = InputFile.getFileFromFileDialog(parentWindow, start_path, GuiConstants.CSV_EXTENSION, "");	
		this.partition = inputFile.get_elements_from_csv_file_nocheck(file_name); 
		
		ArrayList<Element> packages = (ArrayList<Element>) partition.get_elements().stream()
				.filter(e -> e.getElement_type().equals(ElementType.ELEM_TYPE_PACKAGE))
				.collect(Collectors.toList());
			
		for(Element packag : packages){
			RPackage original = RProject.getRPackage(packag.getIdentifier());
			// returns framework package; original is now plugin
	
			// FW is the list of plugin classes and interfaces
			// that will belong to the framework
			ArrayList<RType> FW    = new ArrayList<RType>(); 
			RPackage frame = null;	// framework package
			RPackage plugn = null;	// plugin package
			RClass abstractFactory = null;	// (abstract) factory class of the framework
			RClass concreteFactory = null;	// (concrete) factory class of the plugin
			
			// Step 1: create framework package and rename original
			RPackage parent   = original.getParent(); //get parent package
			String origName   = original.getName();
			String pluginName = origName+"_plugin";
			String frameName  = origName+"_framework";
				
			original.rename(pluginName);
			frame = parent.makePackage(frameName);
			plugn = original;
			
			// Step 2: create an empty framework abstract factory class and empty 
			//      plugin concrete factory class that have a subclassing relationship
			abstractFactory = frame.makeAbstractClass("Factory");
			concreteFactory = plugn.makeSubClass("Factory", abstractFactory);

			// Step 3: populate FW with original/plugin classes
			for (RPackageMember pm : plugn.getMembers()) {
				
				Element pm_bttf = getBttFElementFromR3PM(pm, origName, pluginName, frameName);
				
				if(pm_bttf != null && pm_bttf.getIdentifier() != null){
					boolean fully_belongs_to_framework = pm_bttf.belongLevelFW().equals(FWPlBelongLevel.FULLY_BELONGS_FW);
					boolean only_parts_belong_to_framework = pm_bttf.belongLevelFW().equals(FWPlBelongLevel.PARTIALLY_BELONGS_FW);
					
					//pm could be a class, interface, enum, @Interface, package
				
					// Step 3.a: if members of a FW concrete class have constructor calls, 
					// the class must be processed as it partially belongs to the framework 
					// so we can apply liftConstructorCalls later 
					if((fully_belongs_to_framework == true) 
						&& !pm.isInterface() 
						&& pm.isConcrete()
						&& pm_bttf.needsLocalConstructor()
						){
							fully_belongs_to_framework = false;
							only_parts_belong_to_framework = true;  
						}
					 
					// Step 3.b: create abstract classes for classes that partially
					// belongs to the framework
					if (only_parts_belong_to_framework == true) {
						if (!pm.isInterface()) {
							//System.out.println("make FW abstract class: " + pm.getName());
							RClass ac = frame.makeAbstractClass(pm.getName());
							
							//if(pm.getExtends() != null){
							//	System.out.println("current extends of pm: " + pm.fullName + " " + pm.getExtends().fullName);
							//}
							//if(ac.getExtends() != null){
							//	System.out.println("current extends of ac: " + ac.fullName + " " + ac.getExtends().fullName);
							//}
							
							ac.makeExtends(pm.getExtends());
							pm.makeExtends(ac);
							FW.add((RType)pm);
						}
						else if (pm.isInterface()) {
				   			RInterface i = frame.makeInterface(pm.getName());
				   			//if(pm.getExtends() != null){
				   			//	System.out.println("current extends of pm: " + pm.fullName + " " + pm.getExtends().fullName);
				   			//}
				   			//if(i.getExtends() != null){
				   			//	System.out.println("current extends of i: " + i.fullName + " " + i.getExtends().fullName);
				   			//}
				   			
				   			i.makeExtends(pm.getExtends());
							pm.makeExtends(i); 
							FW.add((RType)pm);
						}
						else if (pm instanceof REnum || pm instanceof RAnnotation) {
							//BTTF WILL NOT ALLOW THIS because @interface and enum cannot extend
						}
					}
				}//END OF   if(pm_bttf != null && pm_bttf.getIdentifier() != null){
				
				
				// Step 4: lift constructor calls
				for (RType t : FW) {
					for(RTypeMember m : t.getMembers()){
						Element m_bttf = getBttFElementFromR3PM(m, origName, pluginName, frameName);
						if(m_bttf != null && m_bttf.getIdentifier() != null){
							if(!m_bttf.isIs_hook()){ // !m.isTemplateMethod()){ 
								m.liftConstructorCalls(FW); //this method does the hard work
							}
						}
					}
				}
				
			}//END OF   for (RPackageMember pm : plugn.getMembers()) {
			
			
			System.out.println("List of types in FW");
			for(RType t : FW){
				System.out.println(t.fullName);
			}
			
			// Step 5: move or pull apart types that partially belong to the framework
			for (RPackageMember pm : plugn.getMembers()) {
				Element pm_bttf = getBttFElementFromR3PM(pm, origName, pluginName, frameName);
				if(pm_bttf != null && pm_bttf.getIdentifier() != null){
					boolean fully_belongs_to_framework = pm_bttf.belongLevelFW().equals(FWPlBelongLevel.FULLY_BELONGS_FW);
					boolean only_parts_belong_to_framework = pm_bttf.belongLevelFW().equals(FWPlBelongLevel.PARTIALLY_BELONGS_FW);
					
					if((fully_belongs_to_framework == true) 
						&& !pm.isInterface() 
						&& pm.isConcrete()
						&& pm_bttf.needsLocalConstructor()
					){
						fully_belongs_to_framework = false;
						only_parts_belong_to_framework = true;  
					}
					
				    
					if(fully_belongs_to_framework == true){ 
						lifted.add(pm.getFullName());
						pm.liftAllTypes(FW);
					
						//move all contents to framework package
						pm.move(frame);
						pm.setPublic();
						for(RTypeMember m : pm.getMembers()){
							m.setPublic();
						}
						continue;
					}
					
					if (only_parts_belong_to_framework == true){ 
						RType absT = pm.getParent();  //created previously
						for(RTypeMember m : pm.getMembers()){
							Element m_bttf = getBttFElementFromR3PM(m, origName, pluginName, frameName);
							if(m_bttf != null && m_bttf.getIdentifier() != null){
								boolean m_fully_belongs_to_framework = m_bttf.belongLevelFW().equals(FWPlBelongLevel.FULLY_BELONGS_FW);
								if(m_fully_belongs_to_framework){
									if(!m.isConstructor() && ( m_bttf.isIs_hook() || m.isLocalFactory()) ){
									//if(m.isTemplateMethod() || m.isLocalFactory()){
										absT.makeAbstractMethodWithLiftedSignatureTypes(m,FW);
									}
							  		else if(m.isConstructor()){ 
							  			if (m.isPublic()) {
							  				RMethod f = concreteFactory.makeFactoryWithLiftedSignatureTypes(m, FW);
							  				abstractFactory.makeAbstractMethod(f);
							  				lifted.add(pm.getFullName());
							  				abstractFactory.liftAllTypes(FW);
							  			}
							  		}			
							  		else {
							  			lifted.add(m.getFullName());
							  			m.liftAllTypes(FW);
							  			m.promote();
							  			if(!m.isPublic()){
							  				m.setPublic();
							  			}
							  		}
								}//END OF   if(m.fully_belongs_to_framework){
								else{
									lifted.add(m.getFullName());
									m.liftAllTypes(FW);
								}
							}//END OF   if(m_bttf != null && m_bttf.getIdentifier() != null){
						}//END OF   for(RTypeMember m : pm.getMembers()){
						lifted.add(absT.getFullName());
						absT.liftAllTypes(FW);
					}//END OF   if (pm.only_parts_belong_to_framework == true){
					
					
				}//END OF   if(pm_bttf != null && pm_bttf.getIdentifier() != null){
			}//END OF   for (RPackageMember pm : plugn.getMembers()) { 
			
		}//END OF for(Element packag : packages){
		printNotFound();
		printLifted();
	}
	
	private Element getBttFElementFromR3PM(RPackageMember pm, String origPName, String plPName, String fwPName){
		Element pm_bttf = null;
		String fullName = null;
		//String fullName2 = null;
		if (pm instanceof RType){
			fullName = ((RType)pm).fullName;
			//if(((RType)pm).getParent()!= null){
			//	String parent = ((RType)pm).getParent().fullName;
			//	fullName2 = parent + "." + ((RType)pm).simpleName;
			//}
		}
		else if (pm instanceof REnum){
			fullName = ((REnum)pm).fullName;
		}
		else if (pm instanceof RAnnotation){
			fullName = ((RAnnotation)pm).fullName;
		}
		else{
			fullName = pm.fullName;
		}
		
		if(fullName.startsWith(plPName + ".") && !fullName.startsWith(origPName + ".")){
			fullName = fullName.replaceFirst(plPName + ".", origPName + ".");
		}
		pm_bttf = PartitionHelper.get_element_from_string(partition.get_elements(), fullName);
		//if(pm_bttf == null && fullName2 != null){
		//	pm_bttf = PartitionHelper.get_element_from_string(partition.get_elements(), fullName2);
		//}
		if(pm_bttf == null){
			addToNotFound(fullName);
			//addToNotFound(fullName2);
		}
		return pm_bttf;
		
	}
	
	private Element getBttFElementFromR3PM(RTypeMember tm, String origPName, String plPName, String fwPName){
		String fullName = null;
		
		if (tm instanceof RMethod){
			//fullName = ((RMethod)tm).getFullName();
			fullName = ((RMethod)tm).getSignature();
			fullName = fullName.replace("\\n\\r", " ");
			fullName = fullName.replace("\\r\\n", " ");
			fullName = fullName.replace("\\r", " ");
			fullName = fullName.replace("\\t", " ");
			if(fullName.contains(" throws ")){
				int endIndex = fullName.lastIndexOf(")", fullName.indexOf(" throws "));
				fullName = fullName.substring(0, endIndex+1);
			}
			
			fullName = fullName.replaceAll("(\\s+\\()", "(");
			fullName = fullName.replaceAll("(\\s){2,}", " ");
			fullName = fullName.trim();
			
			String parentFullName = ((RMethod)tm).getRType().fullName;
			if(!fullName.startsWith(parentFullName)){
				fullName = parentFullName + "." + fullName;
			}
			//System.out.println("method: " + fullName);
		}
		else if (tm instanceof RFieldDeclaration){
			fullName = ((RFieldDeclaration)tm).getFullName();
			String parentFullName = ((RFieldDeclaration)tm).getRType().fullName;
			if(!fullName.contains(parentFullName)){
				fullName = parentFullName + "." + fullName;
			}
		}
		else{
			fullName = tm.getFullName();
		}
		
		if(fullName.startsWith(plPName + ".") && !fullName.startsWith(origPName + ".")){
			fullName = fullName.replaceFirst(plPName + ".", origPName + ".");
		}
		Element tm_bttf = PartitionHelper.get_element_from_string(partition.get_elements(), fullName);
		if(tm_bttf == null){
			addToNotFound(fullName);
		}
		return tm_bttf;
	}
	
	private void addToNotFound(String fullName){
		if(!notFound.contains(fullName)){
			notFound.add(fullName);
		}
	}
	
	private void printNotFound(){
		for(String s : notFound){
			System.out.println("Not found in BttF: " + s);
		}
	}
	
	private void printLifted(){
		for(String s : lifted){
			System.out.println("liftAllTypes(): " + s);
		}
	}

}

