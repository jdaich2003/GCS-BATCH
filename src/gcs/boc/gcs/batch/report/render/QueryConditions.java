package boc.gcs.batch.report.render;


/**
 * 报表查询条件类
 * @author daic
 *
 */
public class QueryConditions{
	
	private String branchId="";  //机构id
	
	private String branchName = ""; //机构名称
	
	private String branchScope= "";  //机构范围 1为本机构，2为辖内机构
	
	private String roleId="";  //角色id,指冒组
	
	private String userId="";  //用户id
	
	private String dateStart = ""; //日期区间开始
	
	private String dateEnd = "";  //日期区间结束

	private String reportId =""; //报表ID
	
	private String reportName = ""; //报表名称
	
	private int pageIndex = 1; //当前页
	
	private int pageSize = 1;  //每页行数
	
	private int pageCount = 0; //总页数
	
	private Boolean splitPage = true; //是否需要分页
	
	private String bhName = null ; //省份名称
	
	private String bhId = null ; //省份机构号
	private String fileName = null; //细明报表下载文件名
	private String filePath = null; //细明报表下载文件路径
	
	private int fileNum = 1 ;
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getBhId() {
		return bhId;
	}

	public void setBhId(String bhId) {
		this.bhId = bhId;
	}

	public String getBhName() {
		return bhName;
	}

	public void setBhName(String bhName) {
		this.bhName = bhName;
	}

	public String getReportId() {
		return reportId;
	}

	public void setReportId(String reportId) {
		if(reportId!=null){
			this.reportId = reportId.trim();
		}
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getPageCount() {
		return pageCount;
	}

	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		if(userId!=null){
			this.userId = userId.trim();
		}
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		if(roleId!=null){
			this.roleId = roleId.trim();
		}
	}

	public String getBranchId() {
		return branchId;
	}

	public void setBranchId(String branchId) {
		if(branchId!=null){
			this.branchId = branchId.trim();
		}
	}

	public String getDateStart() {
		return dateStart;
	}

	public void setDateStart(String dateStart) {
		if(dateStart!=null){
			this.dateStart = dateStart.trim();
		}
	}

	public String getDateEnd() {
		return dateEnd;
	}

	public void setDateEnd(String dateEnd) {
		if(dateEnd!=null){
			this.dateEnd = dateEnd.trim();
		}
	}

	public int getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}

	public Boolean getSplitPage() {
		return splitPage;
	}

	public void setSplitPage(Boolean splitPage) {
		this.splitPage = splitPage;
	}

	public int getStartRow(){
		return pageSize*(pageIndex-1)+1;
	}
	
	public int getEndRow(){
		return pageSize*pageIndex;
	}

	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		if(branchName!=null){
			this.branchName = branchName.trim();
		}
	}

	public String getReportName() {
		return reportName;
	}

	public void setReportName(String reportName) {
		if(reportName!=null){
			this.reportName = reportName.trim();
		}
	}

	public String getBranchScope() {
		return branchScope;
	}

	public void setBranchScope(String branchScope) {
		if(branchScope!=null){
			this.branchScope = branchScope.trim();
		}
	}

	public int getFileNum() {
		return fileNum;
	}

	public void setFileNum(int fileNum) {
		this.fileNum = fileNum;
	}

}

