package boc.gcs.batch.coll.exception;
/**
 * 异常处理
 * @author daic
 *
 */
public class DealDataException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String EXCEPTION_SCOPE="COLL_DealData_BATCH Exception!";

	public DealDataException() {
		super(EXCEPTION_SCOPE);
	}

	public DealDataException(String message) {
		super(EXCEPTION_SCOPE+message);
	}

	public DealDataException(String message, Throwable cause) {
		super(EXCEPTION_SCOPE+message, cause);
	}

	public DealDataException(Throwable cause) {
		this(EXCEPTION_SCOPE,cause);
	}
}
