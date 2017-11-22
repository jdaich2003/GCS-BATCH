package boc.gcs.batch.report.exe;

import java.util.ArrayList;
import java.util.List;

import boc.gcs.batch.common.util.FTPOper;
import boc.gcs.batch.common.util.PropertyUtil;
import boc.gcs.batch.report.transfer.FtpParameters;
/**
 * 初始化ftp目录结构
 * @author daic
 *
 */
public class InitFtpDir {
	public static void main(String[] args) {
		FTPOper ftpOper = null;
		try {
			ftpOper = new FTPOper("display");
			FtpParameters params = new FtpParameters();
			params.setRemoteRootPath(PropertyUtil
					.getFtp("report_ftp_remoteRootPath_display"));
			List<String> dirNames = new ArrayList<String>();
			dirNames.add("4890000000"); //纽约分行    
			dirNames.add("4880000000"); //加拿大中行  
			dirNames.add("4870000000"); //巴拿马分行  
			dirNames.add("4860000000"); //巴西子行    
			dirNames.add("4850000000"); //英国子行    
			dirNames.add("4840000000"); //法兰克福分行
			dirNames.add("4830000000"); //巴黎分行    
			dirNames.add("4820000000"); //卢森堡分行  
			dirNames.add("4800000000"); //米兰分行    
			dirNames.add("4790000000"); //匈牙利中行  
			dirNames.add("4780000000"); //俄罗斯中行  
			dirNames.add("4770000000"); //哈萨克中行  
			dirNames.add("4760000000"); //新加坡分行  
			dirNames.add("4720000000"); //东京分行    
			dirNames.add("4750000000"); //首尔分行    
			dirNames.add("4740000000"); //曼谷分行    
			dirNames.add("4730000000"); //马来西亚中行
			dirNames.add("4700000000"); //胡志明市分行
			dirNames.add("4690000000"); //马尼拉分行  
			dirNames.add("4680000000"); //雅加达分行  
			dirNames.add("4600000000"); //悉尼分行    
			dirNames.add("4590000000"); //赞比亚中行  
			dirNames.add("4580000000"); //约堡分行    
			dirNames.add("4570000000"); //澳门分行    
			dirNames.add("4560000000"); //金边分行
			ftpOper.initServerDir(params, dirNames);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (ftpOper != null)
				ftpOper.ftpClose();
		}

	}
}
