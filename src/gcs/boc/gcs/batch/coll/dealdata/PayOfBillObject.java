package boc.gcs.batch.coll.dealdata;
/**
 * 支付信息实体
 * @author daic
 *
 */
public class PayOfBillObject {
	//最小应缴
	private float min_pay;
	//总应缴
	private float tot_pay ;
	//已缴
	private float pay_amount ;
	//承诺缴
	private float acpt_pay_amount;
	//逾期数据初始最小还款额
	private float org_min ;
	//逾期数据初始应还款额
	private float org_tot ;
	
	private String bchid;
	
	private String m_type;
	
	private String customer_id;
	
	private String cycle;
	
	public String getCustomer_id() {
		return customer_id;
	}
	public void setCustomer_id(String customer_id) {
		this.customer_id = customer_id;
	}
	public void initialize(){
		min_pay = 0;
		tot_pay = 0;
		pay_amount = 0;
		org_min = 0;
		org_tot = 0;
	}
	public float getMin_pay() {
		return min_pay;
	}
	public void setMin_pay(float min_pay) {
		this.min_pay = min_pay;
	}
	public float getTot_pay() {
		return tot_pay;
	}
	public void setTot_pay(float tot_pay) {
		this.tot_pay = tot_pay;
	}
	public float getPay_amount() {
		return pay_amount;
	}
	public void setPay_amount(float pay_amount) {
		this.pay_amount = pay_amount;
	}
	public float getOrg_min() {
		return org_min;
	}
	public void setOrg_min(float org_min) {
		this.org_min = org_min;
	}
	public float getOrg_tot() {
		return org_tot;
	}
	public void setOrg_tot(float org_tot) {
		this.org_tot = org_tot;
	}
	public String getBchid() {
		return bchid;
	}
	public void setBchid(String bchid) {
		this.bchid = bchid;
	}
	public String getM_type() {
		return m_type;
	}
	public void setM_type(String m_type) {
		this.m_type = m_type;
	}
	public float getAcpt_pay_amount() {
		return acpt_pay_amount;
	}
	public void setAcpt_pay_amount(float acpt_pay_amount) {
		this.acpt_pay_amount = acpt_pay_amount;
	}
	public String getCycle() {
		return cycle;
	}
	public void setCycle(String cycle) {
		this.cycle = cycle;
	}
	
}
