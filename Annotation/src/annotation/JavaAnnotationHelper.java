package annotation;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import errors.AnnotationException;
import input.TextElement;
import input.JavaReaderHelper;

public class JavaAnnotationHelper {
	private boolean debug = true;
	
	private JavaReaderHelper javaReaderHelper = new JavaReaderHelper();
	private final String r4feature = "R4Feature";
	
	private void annotatePackages(ArrayList<AnnotationElement> a_elems, ArrayList<TextElement> t_elems, String feature_package){
		ArrayList<TextElement> packages = (ArrayList<TextElement>) t_elems.stream()
				.filter(e -> e.type.equals(javaReaderHelper.PACKAGE_TYPE))
				.collect(Collectors.toList());
		
		ArrayList<String> annotated_packs = new ArrayList<String>();
		
		for(TextElement p : packages){
			AnnotationElement pa = getAnnotationElem(a_elems, p.full_identifier);
			if(pa != null && !annotated_packs.contains(pa.full_identifier)){
				Path path = Paths.get(p.file_path).getParent();
				createPackageAnnotationFile(feature_package, path.toString(), pa);
				annotated_packs.add(pa.full_identifier);
			}
		}
	}
	
	private void createPackageAnnotationFile(String feature_package, String dir, AnnotationElement p){
		ArrayList<String> lines = new ArrayList<String>();
		lines.add(getDeclarationAnnotation(feature_package, p));
		lines.add("\r\npackage " + p.full_identifier +";");
		
		Path path = Paths.get(dir + "\\package-info.java");
		try {
			Files.write(path, lines, Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private AnnotationElement getAnnotationElem(ArrayList<AnnotationElement> a_elems, String full_indentifier){
		return a_elems.stream()
				.filter(e -> e != null && e.full_identifier.equals(full_indentifier))
				.collect(Collectors.toList()).get(0);
	}
	
	
	private String createFeatureFile(ArrayList<AnnotationElement> a_elems, ArrayList<TextElement> t_elems){
		ArrayList<String> feature_names = new ArrayList<String>();
		feature_names = (ArrayList<String>) a_elems.stream()
			.filter(e -> e.feature_name != null)
			.map(e -> e.feature_name)
			.distinct()
			.collect(Collectors.toList());
		
		TextElement pack_elem = t_elems.stream()
				.filter(e -> e != null 
					&& e.type != null 
					&& e.type.equals(javaReaderHelper.PACKAGE_TYPE)
				)
				.collect(Collectors.toList()).get(0);
		
		
		Path pack_path = Paths.get(pack_elem.file_path).getParent();
		
		ArrayList<String> lines = new ArrayList<String>();
		lines.add("package " + pack_elem.full_identifier +";");
		lines.add("\r\n");
		lines.add("\r\npublic @interface " + r4feature + " {");
		for(String f : feature_names){
			lines.add("\r\n\tpublic static boolean " + f + " = true;");
		}
		lines.add("\r\n\tboolean value();");
		lines.add("\r\n}");
		
		Path path = Paths.get(pack_path.toString() + "\\" + r4feature + ".java");
		try {
			Files.write(path, lines, Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return pack_elem.full_identifier;
	}
	
	
	public void annotateFiles(ArrayList<File> source_files, ArrayList<AnnotationElement> a_elems, ArrayList<TextElement> t_elems){
		if(debug){
			System.out.println(t_elems.toString());
		}
		
		boolean all_exist = checkElementsExist(a_elems, t_elems);
		if(all_exist){
			//create feature java file, get feature file package 
			String feature_package = createFeatureFile(a_elems, t_elems);
			//create packages annotation file
			annotatePackages(a_elems, t_elems, feature_package);
			
			//iterate each file
			for(File f : source_files){
				if(debug){
					System.out.println("method annotateFiles, current_file: " + f.getPath());
				}
				boolean modified = false;
				String source = javaReaderHelper.getJavaSourceCode(f);
				ArrayList<String> newsource = new ArrayList<String>();
				
				ArrayList<TextElement> classes_in_file = (ArrayList<TextElement>) t_elems.stream()
						.filter(e -> e.file_path.equals(f.getPath()) && e.type.equals(javaReaderHelper.CLASS_TYPE))
						.collect(Collectors.toList());
				
				int min_file_char = classes_in_file.stream()
						.map(c -> c.dec_initial_char)
						.min(Comparator.naturalOrder())
						.get();
				
				//start newsource file with contents before classes
				newsource.add(source.substring(0, min_file_char));
				newsource.add("\n\rimport " + feature_package + "." + r4feature + ";");
				
				//get first class in file and its annotation
				TextElement current_class = getNextElementInFile(min_file_char, classes_in_file);
				
				//iterate each class
				while(current_class != null){
					if(debug){
						System.out.println("\tAnnotate class: " + current_class.full_identifier);
					}
					
					String annotation = getDeclarationAnnotation(feature_package, current_class.full_identifier, a_elems);
					String last_annotation = annotation;
					
					//annotate class
					addAnnotatedDeclarationToSource(newsource, source, current_class.dec_initial_char, current_class.dec_end_char, annotation);
					if(annotation != null){
						modified = true;
					}
					//get class members
					ArrayList<TextElement> class_members = getClassMembers(current_class, t_elems);
					
					int min_class_char = class_members.stream()
							.map(c -> c.dec_initial_char)
							.min(Comparator.naturalOrder())
							.get();
					
					//get first member
					TextElement current_member = getNextElementInFile(min_class_char, class_members);
					
					//iterate class members
					while(current_member != null){
						if(debug){
							System.out.println("\t\tAnnotate member: " + current_member.full_identifier);
						}
						
						//get member annotation
						annotation = getDeclarationAnnotation(feature_package, current_member.full_identifier, a_elems);
						
						//if the annotation is the same than last one, there is no need to annotate
						if( annotation != null && !annotation.equals(last_annotation)){
							last_annotation = annotation;
							modified = true;	
						}
						else{
							annotation = null;
						}
						
						addAnnotatedDeclarationToSource(newsource, source, current_member.dec_initial_char, current_member.body_end_char, annotation);
						
						//get next member
						int start_missing_area = current_member.body_end_char;
						min_class_char = current_member.body_end_char;
						current_member = getNextElementInFile(min_class_char, class_members);
						appendNonMemberAreas(source, newsource, start_missing_area, current_member);
					}
					
					//get next class in file, reset annotation string
					newsource.add("" + source.charAt(current_class.body_end_char));
					int start_missing_area = current_class.body_end_char;
					min_file_char = current_class.body_end_char;
					current_class = getNextElementInFile(min_file_char, classes_in_file);
					appendNonMemberAreas(source, newsource, start_missing_area, current_class);
				}
				
				if(modified){
					try {
						Path path = Paths.get(f.getPath());
						Files.write(path, newsource, Charset.forName("UTF-8"));
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					if(debug){
						System.out.println("FILE: " + f.getPath());
						System.out.println("Original source...");
						System.out.println(source.toString());
						System.out.println("New source...");
						System.out.println(newsource.toString());
					}
				}
			}
		}
		else{
			System.out.println("Error: not all elements exist.");
		}
	}
	
	
	private void appendNonMemberAreas(String orig_source, ArrayList<String> new_source, int index_end_current, TextElement next_element){
		int start_missing_area = index_end_current+1;
		int end_missing_area = -1;
		if(next_element != null){
			end_missing_area = next_element.dec_initial_char;
		}
		else if(index_end_current < orig_source.length()){
			end_missing_area = orig_source.length();
		}
		
		if(start_missing_area > 0 && start_missing_area < orig_source.length() && end_missing_area > 0 && end_missing_area < orig_source.length()){
			new_source.add(orig_source.substring(start_missing_area, end_missing_area));
		}
	}
	
	
	private void addAnnotatedDeclarationToSource(ArrayList<String> newsource, String source, int begin_index, int end_index, String annotation){
		if (annotation != null){
			newsource.add("\r\n" + annotation + "\r\n" + source.substring(begin_index, end_index+1));
		}
		else{
			newsource.add(source.substring(begin_index, end_index+1) + "\r\n" );
		}
		
	}
	
	
	private String getDeclarationAnnotation(String feature_package, String full_identifier, ArrayList<AnnotationElement> a_elems){
		String annotation = null;
		
		try{
			ArrayList<AnnotationElement> find_elem = (ArrayList<AnnotationElement>) a_elems.stream()
					.filter(a -> a.full_identifier != null && a.full_identifier.equals(full_identifier))
					.collect(Collectors.toList()); 
			
			if(find_elem != null && find_elem.size()>0){
				annotation = getAnnotationText(feature_package, find_elem.get(0));
			}
		}catch(NoSuchElementException ex){
			//do nothing
		}
		
		return annotation;
	}
	
	private String getDeclarationAnnotation(String feature_package, AnnotationElement elem){
		String annotation = null;
		
		try{
			if(elem != null){
				annotation = getAnnotationText(feature_package, elem);
			}
		}catch(NoSuchElementException ex){
			//do nothing
		}
		
		return annotation;
	}
	
	private String getAnnotationText(String feature_package, AnnotationElement elem){
		if(elem.type.equals(javaReaderHelper.PACKAGE_TYPE)){
			return "@" + feature_package + "." + r4feature + "(" + feature_package + "." + r4feature + "." + elem.feature_name +")";
		}
		return "@" + r4feature + "(" + feature_package + "." + r4feature + "." + elem.feature_name +")";
		
	}
	
	
	private TextElement getNextElementInFile(int min_char, ArrayList<TextElement> elements){
		TextElement next = null;
		try{
			next = elements.stream()
					.filter(c -> c.dec_initial_char >= min_char)
					.min((c1, c2) -> Integer.compare(c1.dec_initial_char, c2.dec_initial_char))
					.get();
		}catch(NoSuchElementException ex){
			//do nothing
		}
		return next;		
	}
	
	
	private ArrayList<TextElement> getClassMembers(TextElement class_elem, ArrayList<TextElement> all_elements){
		ArrayList<TextElement> members_in_class = null;
		try{
			members_in_class = (ArrayList<TextElement>) all_elements.stream()
				.filter(e -> e.file_path.equals(class_elem.file_path) && e.parent != null && e.parent.equals(class_elem))
				.collect(Collectors.toList());
		}catch(NoSuchElementException ex){
			//do nothing
		}
		return members_in_class;
	}
	
	
	public boolean checkElementsExist(ArrayList<AnnotationElement> a_elems, ArrayList<TextElement> t_elems){
		StringBuilder missing_elems = new StringBuilder();
		ArrayList<String> names_in_telems = (ArrayList<String>) t_elems.stream().map(e -> e.full_identifier).collect(Collectors.toList());
		for(AnnotationElement a_elem : a_elems){
			if(!names_in_telems.contains(a_elem.full_identifier)){
				System.out.println("This identifier does not exist: " + a_elem.full_identifier);
				missing_elems.append("\n"+a_elem.full_identifier);
			}
		}
		if(missing_elems.toString().isEmpty()){
			return true;
		}
		else{
			throw new AnnotationException("These identifiers do not exist: " + missing_elems.toString());
		}
		
	}
}
