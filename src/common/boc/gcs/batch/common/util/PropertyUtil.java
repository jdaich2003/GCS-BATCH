package boc.gcs.batch.common.util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;

import boc.gcs.batch.common.db.C3P0ConnectionProvider;

/**
 * 读取properties文件
 * 
 * @author daic
 * 
 */
public class PropertyUtil {
	private static final ResourceBundle JDBC_BUNDLE = ResourceBundle
			.getBundle("jdbc");
	private static final ResourceBundle REPORT_BUNDLE = ResourceBundle
			.getBundle("report");
	private static final ResourceBundle COLL_BUNDLE = ResourceBundle
			.getBundle("coll");
	private static final ResourceBundle FTP_BUNDLE = ResourceBundle
			.getBundle("ftp");
//	private static final ResourceBundle BATCH_BUNDLE = ResourceBundle
//	        .getBundle("batch");
    private static String filePath;   
    private Properties objProperties; //属性对象 
    private static PropertyUtil propetyUtil = null;


	public static PropertyUtil getInstance(String filepath)
			throws Exception {
		if (propetyUtil == null) {
			propetyUtil = new PropertyUtil(filepath);
			return propetyUtil;
		} else
			return propetyUtil;
	}
    /**  
     * @name PropertiesUtil  
     * @title 构造函数  
     * @desc 加载属性资源文件  
     * @param String,boolean  
     * @return   
     * @throws Exception   
     */  
    private PropertyUtil(String filePath) throws Exception {   
        this.filePath = filePath;   
        File file = new File(filePath);   
        FileInputStream inStream = new FileInputStream(file);   
        try{   
            objProperties = new Properties();   
            objProperties.load(inStream);   
        }   
        catch(FileNotFoundException e){   
            e.printStackTrace();   
        }   
        catch(Exception e){   
            e.printStackTrace();   
        }finally{   
            inStream.close();   
        }   
    }   
    /**
	 * 功能：获得数据库连接<br>
	 * 函数名：getJDBC<br>
	 * 
	 * @param key
	 * @return String
	 */
	public static String getJDBC(String key) {
		try {
			if (StringUtils.isBlank(key)) {
				return "";
			}
			return JDBC_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return "";
		}
	}

	/**
	 * 功能：获得报表模板参数<br>
	 * 函数名：getJDBC<br>
	 * 
	 * @param key
	 * @return String
	 */
	public static String getReport(String key) {
		try {
			if (StringUtils.isBlank(key)) {
				return "";
			}
			return REPORT_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return "";
		}
	}

	/**
	 * 功能：获得ftp参数<br>
	 * 函数名：getJDBC<br>
	 * 
	 * @param key
	 * @return String
	 */
	public static String getFtp(String key) {
		try {
			if (StringUtils.isBlank(key)) {
				return "";
			}
			return FTP_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return "";
		}
	}
	
	/**
	 * 功能：获得coll参数<br>
	 * 函数名：getJDBC<br>
	 * 
	 * @param key
	 * @return String
	 */
	public static String getColl(String key) {
		try {
			if (StringUtils.isBlank(key)) {
				return "";
			}
			return COLL_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return "";
		}
	}
	/**
	 * 功能：获得coll参数<br>
	 * 函数名：getJDBC<br>
	 * 
	 * @param key
	 * @return String
	 */
//	public static String getBatch(String key) {
//		try {
//			if (StringUtils.isBlank(key)) {
//				return "";
//			}
//			return BATCH_BUNDLE.getString(key);
//		} catch (MissingResourceException e) {
//			return "";
//		}
//	}
	public synchronized void updateCycle_day(String key) {
		Integer cycle_day = 0;
		Integer cycle_dayR = 0;
		if(StringUtils.isBlank(key)){
			return ;
		}
		cycle_day = Integer.valueOf(getValue(key));
		if(cycle_day==28){
			cycle_dayR = 1;
		}else{
			cycle_dayR = cycle_day+1;
		}
		setValue(key, String.valueOf(cycle_dayR));
		try {
			savefile("");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public  Properties getPropObj(String filepath) throws IOException{
		FileInputStream fis = null;
		Properties properties = null;
		try{
//			String filepath = this.getClass().getResource("/batch.properties").getPath();
			File file = new File(filepath);
		    fis = new FileInputStream(file);   
		    properties = new Properties();
		    properties.load(fis);  
		}
		catch(Exception e){
			throw new IllegalArgumentException("properties parser failed because workflow.properties not found",e);
		}finally{
			fis.close();
		}
		return properties;
	}

  /**  
   * @name savefile  
   * @title 持久化属性文件  
   * @desc 使用setValue()方法后，必须调用此方法才能将属性持久化到存储文件中  
   * @param String, String  
   * @return   
   * @throws Exception  
   */  
  public void savefile(String desc) throws Exception{   
      FileOutputStream outStream = null;   
      try{   
          File file = new File(filePath);   
          outStream = new FileOutputStream(file);   
          objProperties.store(outStream, desc);//保存属性文件   
      }catch(Exception e){   
          e.printStackTrace();   
      }finally{   
          outStream.close();   
      }   
  } 
   /**  
    * @name getVlue  
    * @title 获取属性值  
    * @desc 指定Key值，获取value  
    * @param String  
    * @return String  
    */  
	public String getValue(String key){
		return String.valueOf(objProperties.getProperty(key));
	}
  /**  
     * @name removeVlue  
     * @title 删除属性  
     * @desc 根据Key,删除属性  
     * @param String  
     * @return   
     */  
    public void removeValue(String key){   
        objProperties.remove(key);   
    }   
       
    /**  
     * @name setValue  
     * @title 设置属性  
     * @desc   
     * @param String,String  
     * @return   
     */  
    public void setValue(String key, String value){   
        objProperties.setProperty(key, value);   
    }   
    public static void main(String args[]) throws Exception{
    	String filePath = PropertyUtil.class.getResource("/batch.properties").getPath();
    	System.out.println(filePath);
    	PropertyUtil proUtil =  PropertyUtil.getInstance(filePath);
    	System.out.println(proUtil.getValue("cycle_day_catch"));
//    	proUtil.getCycle_day("cycle_day_catch");
    	
    }
}
