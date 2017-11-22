package boc.gcs.batch.report.transfer;

public class FtpParameters {
	private String localRootPath;
	private String remoteRootPath;
	private String date;
	private String bhid;
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		if(date!=null){
			this.date=date.replaceAll("-", "");
		}
	}
	public String getBhid() {
		return bhid;
	}
	public void setBhid(String bhid) {
		this.bhid = bhid;
	}
	public String getLocalRootPath() {
		return localRootPath;
	}
	public void setLocalRootPath(String localRootPath) {
		this.localRootPath = localRootPath;
	}
	public String getRemoteRootPath() {
		return remoteRootPath;
	}
	public void setRemoteRootPath(String remoteRootPath) {
		this.remoteRootPath = remoteRootPath;
	}
	
}
