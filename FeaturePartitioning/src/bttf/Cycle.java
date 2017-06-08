package bttf;

import java.util.ArrayList;

public class Cycle {
	ArrayList<Element> elements;

	public ArrayList<Element> getElements() {
		return elements;
	}

	public void setElements(ArrayList<Element> elements) {
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
            if( this.elements.equals( ((Cycle)object).elements)){
            	return true;
            }
        }

        return false;
    }
	
	
	
}
