package input;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReaderHelper {
	
	public ArrayList<PatternMatchArea> get_match_areas(Pattern pattern, String source, 
			ArrayList<Integer> ignore_contents, int subs_begin, int subs_end){
		ArrayList<PatternMatchArea> match_areas = new ArrayList<PatternMatchArea>();
		Matcher matcher = pattern.matcher(source);
		while(matcher.find()){
			if( 
					//REGIONS
						//no subregion
					((subs_begin == -1 && subs_end == -1) ||
						//or match in subregion
					(subs_begin != -1 && subs_end != -1 && matcher.start() >= subs_begin && matcher.end() <= subs_end ))
					&&
					//IGNORE CONTENTS
						//no ignore contents
					((ignore_contents == null) ||
						//or not in ignore contents
					( ignore_contents != null && !ignore_contents.contains(matcher.start()) && !ignore_contents.contains(matcher.end()))) 
				){
				PatternMatchArea match = new PatternMatchArea(source.substring(matcher.start(), matcher.end()+1), matcher.start(), matcher.end());
				match_areas.add(match);
			}		
		}
		return match_areas;
	}
	
	public ArrayList<Integer> get_element_chars(Pattern pattern, String source, ArrayList<Integer> list_contents, ArrayList<Integer> ignore_contents){
		Matcher matcher = pattern.matcher(source);
		while(matcher.find()){
			if( ignore_contents == null ||
					( ignore_contents != null
					&& !ignore_contents.contains(matcher.start()) 
					&& !ignore_contents.contains(matcher.end())) 
				){
				add_contents_char_numbers(list_contents, matcher.start(), matcher.end());
			}			
		}
		return list_contents;
	}
	
	public void add_contents_char_numbers(ArrayList<Integer> list_chart_numbers, int start, int end){
		for(int i = start; i <= end; i++){
			if(!list_chart_numbers.contains(i)){
				list_chart_numbers.add(i);
			}
		}
	}
	
	public void print_contents(String source, ArrayList<Integer> list_chart_numbers){
		for(int i : list_chart_numbers){
			System.out.print(source.charAt(i));
		}
		System.out.println("\n");
	}

}
