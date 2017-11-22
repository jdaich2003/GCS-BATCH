package boc.gcs.batch.common.util.aop;

public class AOPRuntimeException extends Throwable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public AOPRuntimeException(Exception e){
		super(e);
	}
	public AOPRuntimeException(Throwable e){
		super(e);
	}
}
