package bttf;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class Element implements Comparable<Element>{
	public static final String INITIALIZER_TEXT = "initializer_";
	public static final String ORIGTYPE_ENUM = "Enum";
	public static final String ORIGTYPE_ANNOTATION = "Annotation";
	
	private String identifier;
	private ElementType element_type;
	private String modifier;
	private String code;
	private boolean is_terminal;
	private boolean is_hook = false;
	private boolean in_cycle;
	private boolean is_fPrivate = false;
	private boolean is_fPublic = false;
	private ArrayList<Element> refToThis = new ArrayList<Element>();
	private ArrayList<Element> refFromThis = new ArrayList<Element>();
	private Feature feature = null;
	private String package_name;
	private String class_name;
	private String member_name;
	private String user_comment;
	private String method_signature = "";
	private String earliest_bound = "";
	private String latest_bound = "";
	private boolean assigned_by_inference = false;
	private String annotation_text = "";
	private int LOC;
	int commentsLength;
	String origType;
	private FWPlBelongLevel fwplbelong;
	private Boolean needsLocalConstructor;
	private String methodReturnType;
	
	
	public Element(String identifier, ElementType element_type, String modifier, String code, boolean is_terminal, 
			String method_signature, String annotation_text, int LOC, String origType, String methodReturnType) {
		this.identifier = identifier;
		this.element_type = element_type;
		this.modifier = modifier;
		this.code = code;
		this.is_terminal = is_terminal;
		this.method_signature = method_signature;
		this.annotation_text = annotation_text;
		this.LOC = LOC;
		this.origType = origType;
		this.methodReturnType = methodReturnType;
		set_packageclassmember_names();
	}	
	
	
	
	public Element(String identifier, ElementType element_type, String modifier, String code, boolean is_terminal,
			boolean is_hook, boolean in_cycle, boolean is_fPrivate, boolean is_fPublic, ArrayList<Element> refToThis,
			ArrayList<Element> refFromThis, Feature feature, String package_name, String class_name, String member_name,
			String user_comment, String method_signature, String earliest_bound, String latest_bound,
			boolean assigned_by_inference, String annotation_text, int lOC, int commentsLength, String origType,
			FWPlBelongLevel fwplbelong, Boolean needsLocalConstructor) {
		super();
		this.identifier = identifier;
		this.element_type = element_type;
		this.modifier = modifier;
		this.code = code;
		this.is_terminal = is_terminal;
		this.is_hook = is_hook;
		this.in_cycle = in_cycle;
		this.is_fPrivate = is_fPrivate;
		this.is_fPublic = is_fPublic;
		this.refToThis = refToThis;
		this.refFromThis = refFromThis;
		this.feature = feature;
		this.package_name = package_name;
		this.class_name = class_name;
		this.member_name = member_name;
		this.user_comment = user_comment;
		this.method_signature = method_signature;
		this.earliest_bound = earliest_bound;
		this.latest_bound = latest_bound;
		this.assigned_by_inference = assigned_by_inference;
		this.annotation_text = annotation_text;
		LOC = lOC;
		this.commentsLength = commentsLength;
		this.origType = origType;
		this.fwplbelong = fwplbelong;
		this.needsLocalConstructor = needsLocalConstructor;
	}



	public Element(String identifier, ElementType element_type, String modifier, String code, boolean is_terminal,
			boolean is_hook, boolean in_cycle, boolean is_fPrivate, boolean is_fPublic, ArrayList<Element> refToThis,
			ArrayList<Element> refFromThis, Feature feature, String package_name, String class_name, String member_name,
			String user_comment, String method_signature, String earliest_bound, String latest_bound,
			boolean assigned_by_inference, String annotation_text, int lOC, int commentsLength, String origType) {
		this.identifier = identifier;
		this.element_type = element_type;
		this.modifier = modifier;
		this.code = code;
		this.is_terminal = is_terminal;
		this.is_hook = is_hook;
		this.in_cycle = in_cycle;
		this.is_fPrivate = is_fPrivate;
		this.is_fPublic = is_fPublic;
		this.refToThis = refToThis;
		this.refFromThis = refFromThis;
		this.feature = feature;
		this.package_name = package_name;
		this.class_name = class_name;
		this.member_name = member_name;
		this.user_comment = user_comment;
		this.method_signature = method_signature;
		this.earliest_bound = earliest_bound;
		this.latest_bound = latest_bound;
		this.assigned_by_inference = assigned_by_inference;
		this.annotation_text = annotation_text;
		LOC = lOC;
		this.commentsLength = commentsLength;
		this.origType = origType;
		set_packageclassmember_names();
	}



	void resetElement(){
		this.is_fPrivate = false;
		this.is_fPublic = false;
		this.is_hook = false;
		this.feature = null;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public ElementType getElement_type() {
		return element_type;
	}
	
	public String getElement_type_trick() {
		if(element_type.equals(ElementType.ELEM_TYPE_METHOD) && class_name.equals(member_name)){
			return "Constructor";
		}
		if(element_type.equals(ElementType.ELEM_TYPE_METHOD) && member_name.contains("(") 
				&& class_name.equals(member_name.substring(0,member_name.indexOf("(")))
			){
			return "Constructor";
		}
		if(element_type.equals(ElementType.ELEM_TYPE_METHOD) && member_name.startsWith(INITIALIZER_TEXT)){
			return "Initializer";
		}
		return element_type.get_element_type();
	}
	
	public int getElement_type_granularity_trick() {
		if(element_type.equals(ElementType.ELEM_TYPE_METHOD) && class_name.equals(member_name)){
			return element_type.get_element_granularity()+1;
		}
		if(element_type.equals(ElementType.ELEM_TYPE_METHOD) && member_name.contains("(") 
				&& class_name.equals(member_name.substring(0,member_name.indexOf("(")))
			){
			return element_type.get_element_granularity()+1;
		}
		if(element_type.equals(ElementType.ELEM_TYPE_METHOD) && member_name.startsWith(INITIALIZER_TEXT)){
			return element_type.get_element_granularity()+2;
		}
		return element_type.get_element_granularity();
	}
	
	public void setElement_type(ElementType element_type) {
		this.element_type = element_type;
	}
	public boolean isIs_hook() {
		return is_hook;
	}
	public void setIs_hook(boolean is_hook) {
		this.is_hook = is_hook;
	}
	public boolean isIn_cycle() {
		return in_cycle;
	}
	public void setIn_cycle(boolean in_cycle) {
		this.in_cycle = in_cycle;
	}	
	public boolean isIs_fPrivate() {
		return is_fPrivate;
	}
	public void setIs_fPrivate(boolean is_fPrivate) {
		this.is_fPrivate = is_fPrivate;
	}
	public boolean isIs_fPublic() {
		return is_fPublic;
	}
	public void setIs_fPublic(boolean is_fPublic) {
		this.is_fPublic = is_fPublic;
	}
	
	public ArrayList<Element> getRefToThis() {
		return refToThis;
	}
	public void setRefToThis(ArrayList<Element> refToThis) {
		this.refToThis = refToThis;
	}
	public ArrayList<Element> getRefFromThis() {
		return refFromThis;
	}
	public void setRefFromThis(ArrayList<Element> refFromThis) {
		this.refFromThis = refFromThis;
	}
	public void addCallFrom(Element from){
		if(!refToThis.contains(from)){
			refToThis.add(from);
		}
	}
	public void addCallTo(Element to){
		if(!refFromThis.contains(to)){
			refFromThis.add(to);
		}
	}
	public Feature getFeature() {
		return feature;
	}
	public Feature get_feature_in_task(){
		Feature feature_in_task = this.feature;
		while(feature_in_task != null && !feature_in_task.getIn_current_task()){
			feature_in_task = this.feature.getParent_feature();
		}
		return feature_in_task; 
	}
	public void setFeature(Feature feature) {
		this.feature = feature;
	}
	public String getModifier() {
		return modifier;
	}
	public void setModifier(String modifier) {
		this.modifier = modifier;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	
	public boolean isIs_terminal() {
		return is_terminal;
	}
	
	private void set_packageclassmember_names(){
		if(element_type.equals(ElementType.ELEM_TYPE_PACKAGE)){
			this.package_name = identifier;
			this.class_name = "";
			this.member_name = "";
		}
		else if(element_type.equals(ElementType.ELEM_TYPE_CLASS)){
			int name_begin_index = identifier.lastIndexOf(".");
			if(name_begin_index == -1){
				this.package_name = "default";
				this.class_name = identifier;
				this.member_name = "";
			}
			else{
				this.package_name = identifier.substring(0, name_begin_index);
				this.class_name = identifier.substring(name_begin_index + 1);
				this.member_name = "";
			}
		}
		else if(element_type.equals(ElementType.ELEM_TYPE_METHOD)){
			String name = identifier;
			String pack_and_class = "";
			int name_end_index;
			if(identifier.contains(INITIALIZER_TEXT) && identifier.indexOf("(") == -1){
				name_end_index = identifier.length() - 1;
			}
			else{
				name_end_index = identifier.indexOf("(");
			}
			int name_begin_index = identifier.lastIndexOf(".",name_end_index);
			name = identifier.substring(name_begin_index+1);
			
			int class_name_begin_index = -1;
			
			if(name_begin_index != -1 && name_end_index != -1){
				//name = identifier.substring(name_begin_index+1, name_end_index);
				pack_and_class = identifier.substring(0, name_begin_index);
				class_name_begin_index = pack_and_class.lastIndexOf(".");
			}
			
			if(class_name_begin_index == -1){
				this.package_name = "default";
				this.class_name = pack_and_class;
				
			}
			else{
				this.package_name = pack_and_class.substring(0, class_name_begin_index);
				this.class_name = pack_and_class.substring(class_name_begin_index + 1);
			}
			this.member_name = name;
			
			
		}
		
		else if(element_type.equals(ElementType.ELEM_TYPE_FIELD)){
			int name_begin_index = identifier.lastIndexOf(".");
			int class_name_begin_index = -1;
			String name = identifier;
			String pack_and_class = "";
			
			if(name_begin_index != -1){
				name = identifier.substring(name_begin_index + 1);
				pack_and_class = identifier.substring(0, name_begin_index);
				class_name_begin_index = pack_and_class.lastIndexOf(".");
			}
			
			if(class_name_begin_index == -1){
				this.package_name = "default";
				this.class_name = pack_and_class;
			}
			else{
				this.package_name = pack_and_class.substring(0, class_name_begin_index);
				this.class_name = pack_and_class.substring(class_name_begin_index + 1);
			}
			this.member_name = name;
		}
	}
	
	public String displayElement(){
		if(element_type.equals(ElementType.ELEM_TYPE_PACKAGE)){
			return "Package: " + package_name;
		}
		
		else if(element_type.equals(ElementType.ELEM_TYPE_CLASS)){
			return "Package: " + package_name
			+ "\nClass: " + class_name;
		}
		
		else if(element_type.equals(ElementType.ELEM_TYPE_FIELD) || element_type.equals(ElementType.ELEM_TYPE_METHOD)){
			String elem_type = element_type.equals(ElementType.ELEM_TYPE_FIELD) ? "Field" : (class_name.equals(member_name)) ? "Constructor" : "Method";
			
			return "Package: " + package_name
				+ "\nClass: " + class_name
				+ "\n" + elem_type + ": " + member_name;
		}
		
		else {
			return identifier;
		}
	}

	public String simpleDisplayElement(){
		String feature_text = "";
		if(feature != null){
			feature_text = " (" + feature.getFeature_name();
			if(is_fPublic){feature_text = feature_text + "-fpublic";}
			if(is_fPrivate){feature_text = feature_text + "-fprivate";}
			if(is_hook){feature_text = feature_text + "-hook";}
			feature_text = feature_text + ")";
		}
		
		return element_type.get_element_type() + ": " + identifier + feature_text;
	}
	
	public String displayElement_withFeatAttr(){
		String att_text = "";	
		if(is_fPublic){att_text = att_text + " - fpublic";}
		if(is_fPrivate){att_text = att_text + " - fprivate";}
		if(is_hook){att_text = att_text + " - hook";}
		return element_type.get_element_type() + ": " + identifier + att_text;
	}
	
	public String getParentName(){
		if(this.element_type.equals(ElementType.ELEM_TYPE_METHOD)){
			String nameNoParams = identifier.substring(0, identifier.lastIndexOf("("));
			return nameNoParams.substring(0, nameNoParams.lastIndexOf("."));
		}
		if(identifier.lastIndexOf(".") != -1){
			return identifier.substring(0, identifier.lastIndexOf("."));
		}
		return null;
	}
	
	public String getPackageName(){
		return this.package_name;
	}
	
	public String getClassName(){
		return this.class_name;
	}
	
	public String getMemberName(){
		return this.member_name;
	}
	
	public String getMethod_signature() {
		return method_signature;
	}

	public String getEarliest_bound() {
		return earliest_bound;
	}

	public void setEarliest_bound(String earliest_bound) {
		this.earliest_bound = earliest_bound;
	}

	public String getLatest_bound() {
		return latest_bound;
	}

	public void setLatest_bound(String latest_bound) {
		this.latest_bound = latest_bound;
	}

	public String get_assignment_text(){
		String assignment = "";
		if(feature != null){
			assignment = ((is_fPrivate) ? "is fprivate of " : (is_fPublic) ? "is fpublic of " : "belongs to ") + feature.getFeature_name();
		}
		
		return assignment;
	}
	
	public String getUser_comment() {
		return user_comment;
	}

	public void setUser_comment(String user_comment) {
		this.user_comment = user_comment;
	}
	
	public String getAnnotation_text() {
		if( annotation_text == null || (annotation_text!= null && annotation_text.isEmpty()) ){
			Element parentDeclaration = this.getParentDeclaration();
			if(parentDeclaration != null){
				return parentDeclaration.annotation_text;
			}
		}
		return annotation_text;
	}
	
	public void setAnnotation_text(String annotation_text) {
		this.annotation_text = annotation_text;
	}

	public int getLOCAdjusted() {		
		ArrayList<Element> children = this.getChildrenDeclarations();
		if(this.element_type.equals(ElementType.ELEM_TYPE_CLASS)
			&& children != null && children.size() > 0
			&& children.stream().filter(c -> c.getAnnotation_text() != null).count() > 0
		){
			int difLength = this.getLOC() - 
					children.stream().mapToInt(c -> c.getLOC()).sum(); 
			return 3 + (difLength > 0 ? difLength : 0);
		}
		
		return LOC + (this.annotation_text != null ? 1 : 0);
		
	}

	public int getLOC(){
		return LOC;
	}
	public void setLOC(int lOC) {
		LOC = lOC;
	}

	public String getOrigType() {
		return origType;
	}

	@Override
	public String toString() {
		return "Element [identifier=" + identifier + ", element_type=" + element_type + ", is_hook=" + is_hook
				+ ", is_terminal=" + is_terminal + ", is_fPrivate=" + is_fPrivate + ", is_fPublic=" + is_fPublic
				+ ", feature_name=" + (feature == null ? "" : feature.getFeature_name()) + "]\n";
	}
	
	@Override
    public boolean equals(Object object)
    {
        if (object != null && object instanceof Element)
        {
            if( this.identifier.equals( ((Element)object).identifier)){
            	return true;
            }
        }

        return false;
    }
	
	@Override
	public int compareTo(Element elem)
	{
	    return this.getIdentifier().compareTo(elem.getIdentifier());
	}
	
	public ArrayList<Element> getLayeredRefToThis(){
		//omit methods that reference e, but do not omit methods contained in e
		ArrayList<Element> refTo = (ArrayList<Element>) getRefToThis().stream()
				.filter(r -> !r.getElement_type().equals(ElementType.ELEM_TYPE_METHOD) 
						|| r.getParentName().equals(getIdentifier()))
				.collect(Collectors.toList());
		
		return refTo;
	}
	
	public ArrayList<Element> getNonLayeredRefToThis(){
		//only methods that reference e, and are not contained in e
		ArrayList<Element> refTo = (ArrayList<Element>) getRefToThis().stream()
				.filter(r -> r.getElement_type().equals(ElementType.ELEM_TYPE_METHOD) 
						&& !r.getParentName().equals(getIdentifier()))
				.collect(Collectors.toList());
		
		return refTo;
	}
	
	public ArrayList<Feature> getLayeredRefToFeatures(){
		//features in refToElem, omit methods that reference e, but do not omit methods contained in e
		ArrayList<Feature> refToElem = (ArrayList<Feature>) getLayeredRefToThis().stream()
				.map(e -> e.getFeature())
				.collect(Collectors.toList());
		
		return refToElem;
	}
	
	public ArrayList<Feature> getNonLayeredRefToFeatures(){
		ArrayList<Feature> refToElem = (ArrayList<Feature>) getNonLayeredRefToThis().stream()
				.map(e -> e.getFeature())
				.collect(Collectors.toList());
		
		return refToElem;
	}
	
	public ArrayList<Feature> getRefToFeatures(){
		//features in refToElem, omit methods that reference e, but do not omit methods contained in e
		ArrayList<Feature> refToElem = (ArrayList<Feature>) getRefToThis().stream()
				.map(e -> e.getFeature())
				.collect(Collectors.toList());
		
		return refToElem;
	}
	
	public ArrayList<Element> getFprivateRefFrom(){
		ArrayList<Element> privRefFrom = (ArrayList<Element>) getRefFromThis().stream()
					.filter(r -> r.is_fPrivate && r.getFeature() != null)
					.collect(Collectors.toList());
		
		return privRefFrom;
	}
	
	
	public ArrayList<Feature> getFprivateRefFromFeatures(){
		ArrayList<Feature> privRefFrom = (ArrayList<Feature>) getFprivateRefFrom().stream()
				.map(e -> e.getFeature())
				.collect(Collectors.toList());
		
		return privRefFrom;
	}

	public Element getParentDeclaration(){
		if(this.getParentName() != null){
			//use reffromthis to find parent
			if(getRefFromThis() != null && !getRefFromThis().isEmpty()){
				ArrayList<Element> parent = (ArrayList<Element>) this.getRefFromThis().stream()
						.filter(e -> e.identifier.equals(this.getParentName()))
						.collect(Collectors.toList());
				if(parent != null && parent.size() > 0){
					return parent.get(0);
				}
			}
		}
		return null;
	}
	
	
	public Element getParentDeclarationFromList(ArrayList<Element> allelems){
		ArrayList<Element> parent = (ArrayList<Element>) allelems.stream()
				.filter(e -> e.identifier.equals(this.getParentName()))
				.collect(Collectors.toList());
		if(parent != null && parent.size() > 0){
			return parent.get(0);
		}
		
		return null;
	}
	
	public boolean isAssigned_by_inference() {
		return assigned_by_inference;
	}

	public void setAssigned_by_inference(boolean assigned_by_inference) {
		this.assigned_by_inference = assigned_by_inference;
	}
	
	public ArrayList<Element> getChildrenDeclarations(){
		if(this.getRefToThis() != null){
			return (ArrayList<Element>) this.getRefToThis().stream()
			.filter(e -> e.getParentName() != null 
				&& e.getParentName().equals(this.getIdentifier())) 
			.collect(Collectors.toList());
		}
		return null;
		
	}

	public FWPlBelongLevel belongLevelFW(){
		if(fwplbelong != null){
			return fwplbelong;
		}
		else if (feature != null){
			if(element_type.equals(ElementType.ELEM_TYPE_PACKAGE) || element_type.equals(ElementType.ELEM_TYPE_CLASS)){
				if(belongsToFW()){
					if(allChildrenSameFeature(true)){
						this.fwplbelong = FWPlBelongLevel.FULLY_BELONGS_FW;
					}
					else{
						this.fwplbelong = FWPlBelongLevel.PARTIALLY_BELONGS_FW;
					}
				}
				else if(belongsToPL()){
					if(allChildrenSameFeature(false)){
						this.fwplbelong = FWPlBelongLevel.FULLY_BELONGS_PL;
					}
					else{
						this.fwplbelong = FWPlBelongLevel.PARTIALLY_BELONGS_FW;
					}
				}
			} 
			else{
				if(belongsToFW()){
					this.fwplbelong = FWPlBelongLevel.FULLY_BELONGS_FW;
				}
				else if(belongsToPL()){
					this.fwplbelong = FWPlBelongLevel.FULLY_BELONGS_PL;
				}
			}
		}
		return this.fwplbelong;
	}
	
	private boolean belongsToFW(){
		if(feature != null && feature.getFeature_name().equals(Feature.FW_FEATURE_NAME)){
			return true;
		}
		return false;
	}
	
	private boolean belongsToPL(){
		if(feature != null && feature.getFeature_name().equals(Feature.PL_FEATURE_NAME)){
			return true;
		}
		return false;
	}
	
	private boolean allChildrenSameFeature(boolean FW){
		boolean allChildrenSame = true;
		if(getChildrenDeclarations() != null){
			for(Element e : getChildrenDeclarations()){
				if(e.getFeature() == null){
					allChildrenSame = false;
					break;
				}
				else{
					/*if a class child belongs to the framework, but it is a hook, 
					 * it needs a counter-part in the plugin, 
					 * then the class cannot fully belong to the FW 
					 */
					if(FW && ( !e.belongsToFW() || (e.belongsToFW() && e.isIs_hook())) ){
						allChildrenSame = false;
						break;
					}
					if(!FW && e.belongsToFW()){
						allChildrenSame = false;
						break;
					}
				}
			}
		}
		return allChildrenSame;
	}

	public boolean needsLocalConstructor(){
		boolean tempNeedsLocalConstr = false;
		if(this.needsLocalConstructor != null){
			return this.needsLocalConstructor;
		}else{
			if(this.belongsToFW()){
				if(element_type.equals(ElementType.ELEM_TYPE_CLASS)
					&& is_terminal
					&& belongLevelFW() == FWPlBelongLevel.FULLY_BELONGS_FW
					&& this.getChildrenDeclarations().stream()
						.map(e -> e.needsLocalConstructor())
						.collect(Collectors.toList()).contains(true)
					){
					tempNeedsLocalConstr = true;
				}
				if( !this.isConstructor() &&
						(element_type.equals(ElementType.ELEM_TYPE_FIELD) || 
						element_type.equals(ElementType.ELEM_TYPE_METHOD))
				){
					Element superParent = (this.getParentDeclaration() != null) ? this.getParentDeclaration().getParentDeclaration() : null;
					for(Element e : this.getRefFromThis()){
						if(e.isConstructor() 
							&& e.getParentDeclaration() != null
							&& e.getParentDeclaration().belongsToFW() 
							&& !e.getParentDeclaration().isIs_terminal()
							&& !e.getParentDeclaration().equals(superParent) //omit calls to super
								){
							tempNeedsLocalConstr = true;
						}
					}
				}
			}
			this.needsLocalConstructor = tempNeedsLocalConstr;
		}
		return this.needsLocalConstructor;
	}
	
	public boolean isConstructor(){
		if(element_type.equals(ElementType.ELEM_TYPE_METHOD) && class_name.equals(member_name)){
			return true;
		}
		if(element_type.equals(ElementType.ELEM_TYPE_METHOD) && this.origType.toLowerCase().contains("constructor")){
			return true;
		}
		return false;
	}
	
	public boolean isSingleton(){
		if(element_type.equals(ElementType.ELEM_TYPE_METHOD) && class_name.equals(methodReturnType)){
			return true;
		}
		return false;
	}
	
}
