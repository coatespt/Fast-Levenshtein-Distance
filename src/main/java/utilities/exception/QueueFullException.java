package utilities.exception;

	public class QueueFullException extends RuntimeException {
	     
		private static final long serialVersionUID = 1L;

		public QueueFullException(){
	        super();
	    }
	    
	    public QueueFullException(String message){
	        super(message);
	    }
	    
	}
	 
