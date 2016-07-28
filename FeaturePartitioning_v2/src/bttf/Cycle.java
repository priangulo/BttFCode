package bttf;

import java.util.ArrayList;

public class Cycle {
	private ArrayList<Element> elements;

	public ArrayList<Element> getElements() {
		return elements;
	}

	public void setElements(ArrayList<Element> elements) {
		for(Element elem : elements){
			elem.setIn_cycle(true);
		}
		this.elements = elements;
	}
		
	@Override
	public String toString() {
		return "Cycle [elements=" + elements + "]";
	}

	@Override
    public boolean equals(Object object)
    {
        if (object != null && object instanceof Cycle)
        {
        	if( this.elements.size() == ((Cycle)object).elements.size() && this.elements.containsAll(((Cycle)object).elements) ) {
        		return true;
        	}
        }

        return false;
    }
	
	
	
}
