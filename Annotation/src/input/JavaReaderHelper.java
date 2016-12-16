package input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JavaReaderHelper{
	private static Boolean debug = true;
	
	private ReaderHelper readHelper = new ReaderHelper();
	
	public final String JAVA_EXT = "java";
	
	public final String PACKAGE_TYPE = "PACKAGE";
	public final String CLASS_TYPE = "CLASS";
	public final String METHOD_TYPE = "METHOD";
	public final String CONSTRUCTOR_TYPE = "CONSTRUCTOR";
	public final String INITIALIZER_TYPE = "INITIALIZER";
	public final String FIELD_TYPE = "FIELD";
	public final String MULTIVAL_ENUMVAL_TYPE = "MULTIVAL_ENUMVAL";
	public final String SIMPLE_ENUMVAL_TYPE = "SIMPLE_ENUMVAL";
	
	public final Pattern PATTERN_COMMENTSSINGLE = Pattern.compile("\\/\\/.*");
	public final Pattern PATTERN_COMMENTSMULTI = Pattern.compile("\\/\\*.*?\\*\\/", Pattern.DOTALL);
	public final Pattern PATTERN_STRINGS = Pattern.compile("((?<!\\\\)\\\"(.*?)\\\"(?<!\\\\\\\"))");
	public final Pattern PATTERN_CLASSES = Pattern.compile("(abstract\\s)*(public|private)*\\s*(abstract)*\\s*((class|interface|enum)|@interface)\\s+(\\w+)\\s*((extends\\s+\\w+)|(implements\\s+\\w+( ,\\w+)*))*\\s*\\{");
	public final Pattern PATTERN_PACKAGES = Pattern.compile("package\\s+([a-zA_Z_]*[\\.\\w]*);");
	//public final Pattern PATTERN_METHODS = Pattern.compile("((public|private|protected|static|final|native|synchronized|abstract|transient)*\\s)+\\s*[\\w\\d\\,\\<\\>\\[\\]\\s]+(\\w+)+\\s*\\([\\w\\d\\,\\<\\>\\[\\]\\s]*\\)\\s*(\\{?|[^;])");
	//public final Pattern PATTERN_CONSTRUCTORS = Pattern.compile("((public|private|protected|static|final|native|synchronized|abstract|transient)*\\s)+\\s*(\\w+)+\\s*\\([^\\)]*\\)*\\s*(\\{?|[^;])");
	
	public final Pattern PATTERN_METHODS = Pattern.compile("((public|private|protected|static|final|native|synchronized|abstract|transient|)*\\s)+(\\w+)\\s+[\\w\\d\\,\\<\\>\\[\\]\\s]+(\\w+)+\\s*\\([\\w\\d\\,\\<\\>\\[\\]\\s]*\\)\\s*(\\{?|[^;])");
	//((public|private|protected|static|final|native|synchronized|abstract|transient|)*\s)+(\w+)\s+[\w\d\,\<\>\[\]\s]+(\w+)+\s*\([\w\d\,\<\>\[\]\s]*\)\s*(\{?|[^;])
	public final Pattern PATTERN_CONSTRUCTORS = Pattern.compile("^((public|private|protected|static|final|native|synchronized|abstract|transient)*\\s)*\\s*(\\w+)+\\s*\\([^\\)]*\\)*\\s*(\\{?|[^;])", Pattern.MULTILINE);
	//^((public|private|protected|static|final|native|synchronized|abstract|transient)*\s)*\s*(\w+)+\s*\([^\)]*\)*\s*(\{?|[^;])
		
	public final Pattern PATTERN_CLASS_INITIALIZERS = Pattern.compile("(static)*\\s*\\{");
	public final Pattern PATTERN_MULTIVAL_ENUMVAL = Pattern.compile("(\\w)+[\\(]");
	
	private final String CLASS_TEXT = "class";
	private final String ANNOTATION_TEXT = "@interface";
	private final String INTERFACE_TEXT = "interface";
	private final String ENUM_TEXT = "enum";
	
	
	/*
	 * finds a word surrounded by spaces
	 */
	private Indexes util_findWord(String source, int word_end){
		while(source.charAt(word_end) == ' ' || source.charAt(word_end) == ','){
			word_end--;
		}
		int word_start = word_end;
		while(source.charAt(word_start) != ' ' && source.charAt(word_start) != ','){
			word_start--;
		}
		
		if(source.charAt(word_start) == ',' && word_start < word_end){
			word_start++;
		}
		
		return new Indexes(word_start, word_end);
	}
	
	/*
	 * start and end index
	 * of a method or a class
	 */
	public Indexes util_getBodyIndexes(String source, int from_index, ArrayList<Integer> ignore_contents){
		int last_closure = from_index;
		int index = from_index;
		int count_open = 0;
		int count_closure = 0;
		
		while( (count_open == 0 && count_closure == 0) || (count_open != count_closure) ){
			if(!ignore_contents.contains(index)){
				if(source.charAt(index) == '{'){
					count_open++;
				}
				if(source.charAt(index) == '}'){
					last_closure = index;
					count_closure++;
				}
			}
			index++;
		}
		
		Indexes indexes = new Indexes(from_index+1, last_closure);
		
		return indexes;
	}
	
	private int util_findNextTermination(String source, ArrayList<Integer> ignore_contents, String symbol, int from, int end){
		int ini_phrase = from;
		int end_phrase = source.indexOf(symbol, ini_phrase);
		
		if( (end_phrase == -1 || end_phrase > end) && ini_phrase < end ){
			end_phrase = end;
		}
		else{
			if(ignore_contents != null){
				while(end_phrase != -1 && ignore_contents.contains(end_phrase) && end_phrase < end){
					end_phrase = source.indexOf(symbol, end_phrase+1);
				}
			}
		}
		
		if(ini_phrase > 0 && end_phrase > 0 && ini_phrase < end_phrase && end_phrase <= end && !source.substring(ini_phrase, end_phrase + 1).trim().isEmpty()){
			return end_phrase;
		}
		
		return -1;
	}
	
	/*
	 * get declaration's name from string
	 */
	private String getIdentifier(String type, String contents, int begin_index){
		if(type.equals(CLASS_TYPE)){
			int beginIndex = -1;
			//it is a class
			if(contents.indexOf(CLASS_TEXT) != -1){
				beginIndex = contents.indexOf(CLASS_TEXT) + CLASS_TEXT.length() + 1;
			}
			//it is an annotation
			else if(contents.indexOf(ANNOTATION_TEXT) != -1){
				beginIndex = contents.indexOf(ANNOTATION_TEXT) + ANNOTATION_TEXT.length() + 1;
			}
			//it is an interface
			else if(contents.indexOf(INTERFACE_TEXT) != -1){
				beginIndex = contents.indexOf(INTERFACE_TEXT) + INTERFACE_TEXT.length() + 1;
			}
			//it is an enum
			else if(contents.indexOf(ENUM_TEXT) != -1){
				beginIndex = contents.indexOf(ENUM_TEXT) + ENUM_TEXT.length() + 1;
			}
			
			char c = contents.charAt(beginIndex);
			while (c == ' '){
				beginIndex++;
				c = contents.charAt(beginIndex);
			}
			
			int endIndex = contents.indexOf(" ", beginIndex);
			return contents.substring(beginIndex, endIndex).trim();
		}
		if(type.equals(PACKAGE_TYPE)){
			int beginIndex = contents.indexOf("package") + 8;
			int endIndex = contents.indexOf(";", beginIndex);
			return contents.substring(beginIndex, endIndex).trim();
		}
		if(type.equals(METHOD_TYPE) || type.equals(CONSTRUCTOR_TYPE)){
			Indexes name_indexes =  util_findWord(contents, contents.indexOf("("));
			return contents.substring(name_indexes.start_index, name_indexes.end_index).trim() + getMethodParametersString(contents, name_indexes.end_index);
		}
		if(type.equals(INITIALIZER_TYPE)){
			return "initializer_at_char_" + begin_index;
		}
		if(type.equals(FIELD_TYPE)){
			
			int endIndex = (contents.indexOf("=") == -1) ? contents.indexOf(";")-1 : contents.indexOf("=")-1;
			Indexes name_indexes = util_findWord(contents, endIndex);
			return contents.substring(name_indexes.start_index, name_indexes.end_index+1).trim();
		}
		if(type.equals(MULTIVAL_ENUMVAL_TYPE)){
			return contents.substring(0, contents.indexOf("(")); 
		}
		
		return null;
	}
	
	/*
	 * get fully qualified name
	 */
	public String getFullName(String type, TextElement parent, String identifier){
		if(parent!=null){
			return parent.full_identifier+"."+identifier;
		}
		return identifier;
	}
	
	
	
	/*
	 * get string containing 
	 * a method's parameters with format "(type param1, type param2, ...)"
	 * if there are no parameters the return will be "()"
	 */
	public String getMethodParametersString(String contents, int nameEndIndex){
		int paramsEndIndex = contents.lastIndexOf(")");
		String[] parameters = contents.substring(nameEndIndex+1,paramsEndIndex).split(",");
		StringBuilder params = new StringBuilder("(");
		if(parameters.length > 0){
			for(int i = 0; i < parameters.length; i++){
				params.append(parameters[i].trim());
				if(i < (parameters.length - 1)){
					params.append(", ");
				}
			}
		}
		params.append(")");
		return params.toString();
	}
	
	/*
	 * identify declarations using regex
	 */
	public void get_text_elements(Pattern pattern, String source, ArrayList<TextElement> all_elements, ArrayList<Integer> ignore_contents, String type, TextElement parent, String file_path, int subs_begin, int subs_end, boolean add_to_ignore){
		ArrayList<PatternMatchArea> declarations = readHelper.get_match_areas(pattern, source, ignore_contents, subs_begin, subs_end);
		for(PatternMatchArea dec : declarations){
			String identifier = getIdentifier(type, dec.contents, dec.start_index);
			String full_name = getFullName(type, parent, identifier);
			Indexes body_indexes = new Indexes(dec.end_index, -1);
			
			if(type.equals(PACKAGE_TYPE)){
				body_indexes.end_index = body_indexes.start_index;
			}
			
			if(type.equals(CLASS_TYPE) || type.equals(INITIALIZER_TYPE)){
				body_indexes = util_getBodyIndexes(source, dec.end_index-1, ignore_contents);
			}
			
			if(type.equals(METHOD_TYPE) || type.equals(CONSTRUCTOR_TYPE) ){
				if(dec.contents.contains("{")){ //method has body
					body_indexes = util_getBodyIndexes(source, dec.end_index-1, ignore_contents);
				}
				else{ //it's a method declaration without body
					body_indexes.start_index = dec.end_index;
					body_indexes.end_index = dec.end_index;
				}
			}
			
			TextElement elem = new TextElement(identifier, parent, file_path, dec.start_index, dec.end_index, dec.contents, 
					type, full_name, body_indexes.start_index, body_indexes.end_index);
			
			all_elements.add(elem);
			
			if(debug){
				System.out.println("get_text_elements  add: " + elem.toString());
			}
			
			if(add_to_ignore){
				readHelper.add_contents_char_numbers(ignore_contents, elem.dec_initial_char, elem.body_end_char);
			}
		}
	}
	
	/*
	 * get a string containing the source of the file
	 */
	public String getJavaSourceCode(File file){
		StringBuilder sb = new StringBuilder();
		try{
			FileReader fr = new FileReader(file);
			BufferedReader reader = new BufferedReader(fr);
			String line;
			
			//pass the source file contents to a string
			while ((line = reader.readLine()) != null) {
		        sb.append(line + "\r\n");
		    }
			reader.close();
			fr.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return sb.toString();
	}
	
	
	private TextElement file_get_package(String source, File file, ArrayList<TextElement> all_elements, ArrayList<Integer> ignore_contents){
		//obtain package declaration, should be just one
		get_text_elements(PATTERN_PACKAGES, source, all_elements, ignore_contents, PACKAGE_TYPE, null, file.getPath(), -1, -1, true);
		
		TextElement parent_package = all_elements.stream()
				.filter(e -> e.type.equals(PACKAGE_TYPE) && e.file_path.equals(file.getPath()))
				.collect(Collectors.toList()).get(0);
			
		return parent_package;
	}
	
	private ArrayList<TextElement> file_get_classes(String source, File file, ArrayList<TextElement> all_elements, ArrayList<Integer> ignore_contents, TextElement package_parent){
		//obtain classes (class, enum, interface, annotation) declarations and bodies
		get_text_elements(PATTERN_CLASSES, source, all_elements, ignore_contents, CLASS_TYPE, package_parent, file.getPath(), -1, -1, false);
		ArrayList<TextElement> classes = (ArrayList<TextElement>) all_elements.stream()
				.filter(e -> e.type.equals(CLASS_TYPE) && e.file_path.equals(file.getPath()))
				.collect(Collectors.toList());
		
		return classes;
	}
	
	private void file_get_methods(String source, File file, ArrayList<TextElement> all_elements, ArrayList<Integer> ignore_contents, TextElement class_parent){
		//get methods and their bodies
		get_text_elements(PATTERN_METHODS, source, all_elements, ignore_contents, METHOD_TYPE, class_parent, file.getPath(), class_parent.body_initial_char, class_parent.body_end_char, true);
	}
	
	private void file_get_constructors(String source, File file, ArrayList<TextElement> all_elements, ArrayList<Integer> ignore_contents, TextElement class_parent){
		//get constructors and their bodies
		get_text_elements(PATTERN_CONSTRUCTORS, source, all_elements, ignore_contents, CONSTRUCTOR_TYPE, class_parent, file.getPath(), class_parent.body_initial_char, class_parent.body_end_char, true);
	}
	
	private void file_get_initializers(String source, File file, ArrayList<TextElement> all_elements, ArrayList<Integer> ignore_contents, TextElement class_parent){
		//get initializers and their bodies
		if(debug){
			System.out.println("START TEXT FOR SEARCHING INITIALIZERS...\n");
			for(int c = class_parent.body_initial_char; c < class_parent.body_end_char; c++){
				if(!ignore_contents.contains(c)){
					System.out.print(source.charAt(c));
				}
			}
			System.out.println("\nEND TEXT FOR SEARCHING INITIALIZERS...\n");
		}
		
		get_text_elements(PATTERN_CLASS_INITIALIZERS, source, all_elements, ignore_contents, INITIALIZER_TYPE, class_parent, file.getPath(), class_parent.body_initial_char, class_parent.body_end_char, true);
	}
	
	private void file_get_fields(String source, File file, ArrayList<TextElement> all_elements, ArrayList<Integer> ignore_contents, TextElement class_parent){
		//get fields
		//the remaining lines inside the class should be fields - no comments, methods, constructors or inits
		if(debug){
			System.out.println("START TEXT FOR SEARCHING FIELDS...\n");
			for(int c = class_parent.body_initial_char; c < class_parent.body_end_char; c++){
				if(!ignore_contents.contains(c)){
					System.out.print(source.charAt(c));
				}
			}
			System.out.println("\nEND TEXT FOR SEARCHING FIELDS...\n");
		}
		
		Indexes phrase_indexes = search_phrase(source, ignore_contents, class_parent.body_initial_char, class_parent.body_end_char, class_parent.body_initial_char); 
		int ini_phrase = phrase_indexes.start_index ;
		int end_phrase = phrase_indexes.end_index;
		while(ini_phrase != -1 && end_phrase != -1){
			if(!source.substring(ini_phrase, end_phrase+1).toString().trim().isEmpty()){
				if(debug){
					System.out.println("fields phrase: " + source.substring(ini_phrase, end_phrase+1).toString() + " end phrase.");
				}
				
				int last_semicolon = -1;
				
				//case 1 - enum, no semicolon in body -> only simple enum values
				if(!source.substring(ini_phrase, end_phrase+1).contains(";")){
					if(debug){
						System.out.println("phrase do not contain ; search for simple enum values.");
					}
					if( ini_phrase < end_phrase && !source.substring(ini_phrase, end_phrase + 1).trim().isEmpty() && class_parent.dec_contents.contains(" enum ")){
						file_get_simple_enumvalues(source, file, all_elements, ignore_contents, class_parent, ini_phrase, end_phrase);
					}
				}
				//case 2 - semicolon in body -> at least one regular field declaration
				else{
					if(debug){
						System.out.println("phrase contains ; search for at least one field declaration.");
					}
					
					last_semicolon = util_findNextTermination(source, null, ";", ini_phrase, end_phrase);
					while (last_semicolon != -1 && last_semicolon <= end_phrase){
						String field_contents = source.substring(ini_phrase, last_semicolon+1);

						//multivalue enum value
						if(class_parent.dec_contents.contains(" enum ") && field_contents.contains("(")){
							get_text_elements(PATTERN_MULTIVAL_ENUMVAL, source, all_elements, ignore_contents, MULTIVAL_ENUMVAL_TYPE, class_parent, 
									file.getPath(), ini_phrase, last_semicolon, true);
						}
						//regular fields
						else if( !class_parent.dec_contents.contains(" enum ")){
							file_get_regularfield(source, file, all_elements, ignore_contents, class_parent, ini_phrase, last_semicolon);
						}
						//simple enum value
						else if( class_parent.dec_contents.contains(" enum ") && field_contents.contains(",")){
							file_get_simple_enumvalues(source, file, all_elements, ignore_contents, class_parent, ini_phrase, last_semicolon);
						}
						
						ini_phrase = last_semicolon + 1;
						last_semicolon = util_findNextTermination(source, null, ";", ini_phrase, end_phrase);
					}
				}
			}
			
			phrase_indexes = search_phrase(source, ignore_contents, class_parent.body_initial_char, class_parent.body_end_char, end_phrase+1); 
			ini_phrase = phrase_indexes.start_index;
			end_phrase = phrase_indexes.end_index;
		}
	}
	
	private Indexes search_phrase(String source, ArrayList<Integer> ignore_contents, int begin, int end, int from){
		int i = from;
		int ini_phrase = -1;
		int end_phrase = -1;
		
		while(i < end && (ini_phrase == -1 || end_phrase == -1) ){
			if(!ignore_contents.contains(i) && ini_phrase == -1){
				ini_phrase = i;
			}
			
			if (ignore_contents.contains(i) && end_phrase == -1 && ini_phrase != -1){
				end_phrase = i - 1;
			}
			
			if (i == (end-1) && end_phrase == -1 && ini_phrase != -1){
				end_phrase = i;
			}
			i++;
		}
		return new Indexes(ini_phrase, end_phrase);
	}
		
	private void add_regular_field(String source, File file, ArrayList<TextElement> all_elements, ArrayList<Integer> ignore_contents, String identifier, TextElement class_parent, int start, int end ){
		if(identifier == null){
			identifier = getIdentifier(FIELD_TYPE, source.substring(start, end+1), start);
		}
		
		String full_name = getFullName(FIELD_TYPE, class_parent, identifier);
		TextElement elem = new TextElement(identifier, class_parent, file.getPath(), start, end, 
				source.substring(start, end+1), FIELD_TYPE, full_name, end, end);
		all_elements.add(elem);	
		if(debug){
			System.out.println("add_regular_field : " + elem.toString());
		}
		readHelper.add_contents_char_numbers(ignore_contents, elem.dec_initial_char, elem.body_end_char);
	}
	
	private void file_get_regularfield(String source, File file, ArrayList<TextElement> all_elements, ArrayList<Integer> ignore_contents, TextElement class_parent, int start, int end){
		ArrayList<Integer> stringlits_and_braks= new ArrayList<Integer>();
		stringlits_and_braks = readHelper.get_element_chars(PATTERN_STRINGS, source, stringlits_and_braks, ignore_contents);
		stringlits_and_braks = readHelper.get_element_chars(Pattern.compile("\\<(\\w+\\,*\\s*)+\\>"), source, stringlits_and_braks, ignore_contents);
		
		int from = start;
		int last_comma = util_findNextTermination(source, stringlits_and_braks, ",", from, end);
		if( (last_comma == -1 || last_comma > end) && from < end){
			if(debug){
				System.out.println("there are no commmas.");
			}
			add_regular_field(source, file, all_elements, ignore_contents, null, class_parent, from, end);
		}
		else{
			while(last_comma != -1 && last_comma <= end){
				if(source.substring(from, last_comma+1).contains("=")){					
					Indexes name_indexes = util_findWord(source, source.indexOf("=", from)-1);
					String name = source.substring(name_indexes.start_index, name_indexes.end_index+1).trim();
					add_regular_field(source, file, all_elements, ignore_contents, name, class_parent, from, last_comma);
				}
				
				else{
					Indexes name_indexes = util_findWord(source, last_comma - 1);
					String name = source.substring(name_indexes.start_index, name_indexes.end_index+1).trim();
					add_regular_field(source, file, all_elements, ignore_contents, name, class_parent, from, last_comma);
				}
				
				from = last_comma + 1;
				last_comma = util_findNextTermination(source, stringlits_and_braks, ",", from, end);
			}
		}
	}
	
	private void file_get_simple_enumvalues(String source, File file, ArrayList<TextElement> all_elements, ArrayList<Integer> ignore_contents, TextElement class_parent, int start, int end){
		int last_comma = util_findNextTermination(source, null, ",", start, end);
		//just one value
		if(last_comma == -1 && source.substring(start, end).length() > 0){
			add_simple_enumvalues(source, file, all_elements, ignore_contents, class_parent, start, end);
		}
		//multiple values
		else{
			String next_word = source.substring(start, last_comma);
			while(next_word != null){
				add_simple_enumvalues(source, file, all_elements, ignore_contents, class_parent, start, last_comma);
				
				start = last_comma+1;
				
				last_comma = source.indexOf(",", start);
				last_comma = util_findNextTermination(source, null, ",", start, end);
				
				if(start > 0 && last_comma > 0 && start < end && last_comma <= end){
					next_word = source.substring(start, last_comma);
				}
				else{
					next_word = null;
				}
			}
		}
	}
	
	private void add_simple_enumvalues(String source, File file, ArrayList<TextElement> all_elements, ArrayList<Integer> ignore_contents, TextElement class_parent, int start, int end){
		String contents = source.substring(start, end).trim();
		String identifier = contents;
		String full_name = getFullName(SIMPLE_ENUMVAL_TYPE, class_parent, identifier);
		TextElement elem = new TextElement(identifier, class_parent, file.getPath(), start, end, contents, SIMPLE_ENUMVAL_TYPE, full_name, start, end);
		all_elements.add(elem);
		if(debug){
			System.out.println("add_simple_enumvalues  add: " + elem.toString());
		}
		readHelper.add_contents_char_numbers(ignore_contents, elem.dec_initial_char, elem.body_end_char);
	}
	
	/*
	 * process method to search
	 * for possibly declarations in a file
	 */
	public ArrayList<TextElement> readJavaSourceFile(File file){
		ArrayList<TextElement> all_elements = new ArrayList<TextElement>();
		ArrayList<Integer> ignore_contents = new ArrayList<Integer>();
		
		if(debug){
			System.out.println("\n\n********************************************");
			System.out.println("********************************************");
			System.out.println("FILE: " + file.getPath());
		}
		
		String source = getJavaSourceCode(file);
		ignore_contents = new ArrayList<Integer>();	//reset ignore_contents
		
		//get the source file areas to ignore: comments 
		ignore_contents = readHelper.get_element_chars(PATTERN_COMMENTSSINGLE, source, ignore_contents, null);
		ignore_contents = readHelper.get_element_chars(PATTERN_COMMENTSMULTI, source, ignore_contents, null);

		if(debug){
			if(!ignore_contents.isEmpty()){
				System.out.println("COMMENTS CONTENTS TO IGNORE:");
				readHelper.print_contents(source, ignore_contents);
			}
		}
		
		TextElement parent_package = file_get_package(source, file, all_elements, ignore_contents);
		ArrayList<TextElement> classes = file_get_classes(source, file, all_elements, ignore_contents, parent_package);
		if(classes != null){
			//obtain class members
			for(TextElement c : classes){
				file_get_constructors(source, file, all_elements, ignore_contents, c);
				file_get_methods(source, file, all_elements, ignore_contents,  c);
				file_get_initializers(source, file, all_elements, ignore_contents, c);
				file_get_fields(source, file, all_elements, ignore_contents, c);
			}
		}
		
		return all_elements;
	}
}
	


class Indexes{
	public int start_index = -1;
	public int end_index = -1;
	public Indexes(int start_index, int end_index) {
		this.start_index = start_index;
		this.end_index = end_index;
	}
	
}