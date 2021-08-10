package a140.beans;

import java.util.Comparator;

/**
 * Represents the name and location of a file of any type.
 * This is for constructing a list of files to run--it is not 
 * the major definition of a book file.
 * 
 * @author peterc
 *
 */
public class DocFile implements Comparable<Object> {
	private int id;
	private String filename;
	private String suffix;
	private int readInOrder;
	private String path;
	private String title;
	private String friendlyTitle;
	private String creator;
	private String description;

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append( "DocFile[");
	//	sb.append(getFullyQualifiedName());
		sb.append( ", idx:");
		sb.append(getReadInOrder());
		sb.append(", title:") ;
		sb.append(getTitle() );
		sb.append(", friendly-title:") ;
		sb.append(getFriendlyTitle());
		sb.append(", creator:") ;
		sb.append(getCreator());
		sb.append(", dscr:") ;
		sb.append(getDescription());
		sb.append("]");
		return  sb.toString();
	}
	
	public String getFriendlyTitle() {
		return friendlyTitle;
	}
	public void setFriendlyTitle(String friendlyTitle) {
		this.friendlyTitle = friendlyTitle;
	}
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	/**
	 * The order in which the document name was read in.
	 * @return
	 */
	public int getReadInOrder() {
		return readInOrder;
	}
	public void setReadInOrder(int o) {
		readInOrder = 0;
	}
		
	/* compareTo() compares by only the filename, not the suffix or path!
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Object o) {
		DocFile that = (DocFile) o;
		return this.filename.compareTo(that.filename);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((filename == null) ? 0 : filename.hashCode());
		result = prime * result + id;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + ((suffix == null) ? 0 : suffix.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DocFile other = (DocFile) obj;
		if (filename == null) {
			if (other.filename != null)
				return false;
		} else if (!filename.equals(other.filename))
			return false;
		if (id != other.id)
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (suffix == null) {
			if (other.suffix != null)
				return false;
		} else if (!suffix.equals(other.suffix))
			return false;
		return true;
	}
	public DocFile(int id, String filename, String suffix, String path, int order) {
		super();
		this.id = id;
		this.filename = filename;
		this.suffix = suffix;
		this.path = path;
		this.readInOrder = order;
	}
/**
 * Construct a docfile from another, substituting a new path and suffix.
 * This is used to apply differnt suffixes and paths for generated file types.
 * @param df
 * @param path
 * @param suffix
 */
	public DocFile(DocFile df, String path, String suffix, int order){
		id=0;
		this.filename = df.getFilename();
		this.suffix = suffix;
		this.path = path;
		this.readInOrder = order;
	}
	
	public String getNameWithSuffix(){
		StringBuffer sb = new StringBuffer();
		sb.append(filename);
		if(suffix!=null && suffix!=""){ 
			sb.append(".");
			sb.append(suffix);
		}
		return sb.toString();
	}
	
//	public String getFullyQualifiedName(){ 
//		return AFileWriter.fullPathnameForFilename(path, filename + "." + suffix);
//	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		if(filename!=null){
			filename=filename.trim();
		}
		this.filename = filename;
	}
	public String getSuffix() {
		return suffix;
	}
	public void setSuffix(String suffix) {
		if(suffix!=null){
			suffix=suffix.trim();
		}
		this.suffix = suffix;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		if(path!=null){
			path=path.trim();
		}
		this.path = path;
	}
	
	public static class ReadOrderCompare implements Comparator<Object>{
		private boolean descending=false;
		
		public ReadOrderCompare(){}
		public ReadOrderCompare(boolean descend){
			descending=descend;
		}
		/**
		 * Defined sort order, false means ascending
		 * @param ascending
		 */
		public boolean isDescending() {
			return descending;
		}

		/**
		 * Define sort order, false means ascending
		 * @param ascending
		 */
		public void setDescending(boolean ascending) {
			this.descending = ascending;
		}

		public int compare(Object o1, Object o2) {
			if(o1.getClass()!=o2.getClass()){
				System.out.println("WTF? Bad objects in DocFile.ReadOrderCompare()");
			}
			DocFile df1 = (DocFile)o1;
			DocFile df2 = (DocFile)o2;
				if(df1.getReadInOrder()<df2.getReadInOrder()){
					return descending ?  -1 : 1;
				}
				if(df1.getReadInOrder()>df2.getReadInOrder()){
					return descending ? 1 : -1;
				}
			return 0;
		}
		
	}
	
}
