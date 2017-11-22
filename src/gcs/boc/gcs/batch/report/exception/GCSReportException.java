package boc.gcs.batch.report.exception;

public class GCSReportException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String EXCEPTION_SCOPE="GCS_Report_BATCH Exception!";

	public GCSReportException() {
		super(EXCEPTION_SCOPE);
	}

	public GCSReportException(String message) {
		super(EXCEPTION_SCOPE+message);
	}

	public GCSReportException(String message, Throwable cause) {
		super(EXCEPTION_SCOPE+message, cause);
	}

	public GCSReportException(Throwable cause) {
		this(EXCEPTION_SCOPE,cause);
	}
}
