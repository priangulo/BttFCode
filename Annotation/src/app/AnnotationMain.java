package app;

import java.io.File;
import java.util.ArrayList;
import org.apache.commons.io.FileUtils;

import annotation.AnnotationElement;
import annotation.JavaAnnotationHelper;
import errors.AnnotationException;
import errors.ReaderException;
import input.JavaReaderHelper;
import input.TextElement;

public class AnnotationMain {
	private JavaReaderHelper javaReaderHelper = new JavaReaderHelper();
	private JavaAnnotationHelper javaAnnotHelper = new JavaAnnotationHelper();
	
	private ArrayList<File> getSourceFiles(String extension, String dir_path){
		ArrayList<File> source_files = new ArrayList<File>();
		String[] ext = new String[]{extension};
		File dir = new File(dir_path);
		
		if(dir != null && dir.exists() && dir.isDirectory()){
			source_files = new ArrayList<File>(FileUtils.listFiles(dir, ext, true));
		}
		else{
			System.out.println("Invalid directory: " + dir_path);
			throw new ReaderException("Invalid directory: " + dir_path);
		}
		
		return source_files;
	}
	
	public String annotate_java(String dir_path, ArrayList<AnnotationElement> annotation_elements){
		try{
			System.out.println("Start annotation.");
			
			System.out.println("\n\n********************************************");
			System.out.println("********************************************");
			System.out.println("********************************************");
			System.out.println("Getting files.");
			ArrayList<File> source_files = new ArrayList<File>();
			source_files = getSourceFiles(javaReaderHelper.JAVA_EXT, dir_path);
			
			System.out.println("\n\n********************************************");
			System.out.println("********************************************");
			System.out.println("********************************************");
			System.out.println("Obtaining text_elements from files.");
			ArrayList<TextElement> all_text_elements = new ArrayList<TextElement>();
			for(File f : source_files){
				all_text_elements.addAll(javaReaderHelper.readJavaSourceFile(f));
			}
			
			System.out.println("\n\n********************************************");
			System.out.println("********************************************");
			System.out.println("********************************************");
			System.out.println("Starting file annotation.");
			javaAnnotHelper.annotateFiles(source_files, annotation_elements, all_text_elements);
			
			System.out.println("\n\n********************************************");
			System.out.println("********************************************");
			System.out.println("********************************************");
			System.out.println("Finish.");
			
			return "Annotation finished.";
			}catch (ReaderException rex){
				return rex.getMessage();
			}catch (AnnotationException eex){
				return eex.getMessage();
			}/*catch (Exception ex){
				return ex.getMessage();
			}*/
		
	}
	
	

}
