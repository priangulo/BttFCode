package adapter;

import java.util.ArrayList;
import bttf.Fact;
import bttf.FactInference;
import bttf.Inference;

public class FactInferenceAdapter {
	public static ArrayList<FactInference> getFlatFacts(ArrayList<Fact> facts){
		ArrayList<FactInference> flatfacts = new ArrayList<FactInference>();
		if(facts != null & facts.size() > 0){
			for(Fact f : facts){
				if(f != null){
					FactInference newf = new FactInference(f);
					flatfacts.add(newf);
					if(f.getInferences() != null && !f.getInferences().isEmpty()){
						for(Inference i : f.getInferences()){
							if(i != null){
								FactInference newi = new FactInference(i);
								flatfacts.add(newi);
							}
						}
					}
				}
			}
		}
		
		return flatfacts;
	}
}
