package boc.gcs.batch.report.exe;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class TestDate {

	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		Calendar   calendar   =   Calendar.getInstance();
		while(1==1){
			calendar.add(Calendar.DAY_OF_YEAR,   1); //第二个参数1表示明天 -1就是昨天
			Date   date   =   calendar.getTime();   
			SimpleDateFormat format=new SimpleDateFormat("yyyyMMdd");
			System.out.println(format.format(date));
			Thread.sleep(100);
		}
	}

}
