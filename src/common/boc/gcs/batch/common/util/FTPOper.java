package boc.gcs.batch.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;
import org.apache.tools.zip.ZipOutputStream;

import boc.gcs.batch.report.exception.GCSReportException;
import boc.gcs.batch.report.render.BCSPReportZip;
import boc.gcs.batch.report.transfer.FtpParameters;

/**
 * 名称 : ftp<br>
 * 功能描述: ftp的连接，发送，断开<br>
 * 创建时间: 2008-8-13<br>
 * 作者: Wang Huichao
 */
public final class FTPOper {

	/**
	 * 属性名称: log <br>
	 * 属性类型: Logger <br>
	 */
	private static final Logger log = Logger.getLogger(FTPOper.class);

	/**
	 * 属性名称: client <br>
	 * 属性类型: FTPClient <br>
	 */
	private FTPClient client = null;

	/**
	 * 函数名：FTPOPer<br>
	 * 功能：<br>
	 * 
	 * @param ftpKey
	 *            void
	 * @throws ApsException
	 */
	public FTPOper(String ftpKey) throws GCSReportException {
		String serverName = PropertyUtil.getFtp(ftpKey + "_ftp_address");
		String port = PropertyUtil.getFtp(ftpKey + "_ftp_port");
		String userName = PropertyUtil.getFtp(ftpKey + "_ftp_user");
		String password = PropertyUtil.getFtp(ftpKey + "_ftp_password");
		client = new FTPClient();
		try {
			client.connect(serverName, Integer.valueOf(port));
			client.login(userName, password);
		} catch (SocketException e) {
			log.error(e);
			throw new GCSReportException(e);
		} catch (IOException e) {
			log.error(e);
			throw new GCSReportException(e);
		}
	}

	/**
	 * 初始化服务器目录
	 * 
	 * @param params
	 * @param dirNames
	 * @throws Exception
	 */
	public void initServerDir(FtpParameters params, List<String> dirNames)
			throws Exception {
		String[] systemDirs = client.listNames(params.getRemoteRootPath());
		for (String dirName : dirNames) {
			boolean isExist = false;
			if (systemDirs == null || systemDirs.length == 0) {
				client
						.makeDirectory(params.getRemoteRootPath() + "/"
								+ dirName);
			} else {
				for (String exitDir : systemDirs) {
					if (exitDir.equals(params.getRemoteRootPath() + "/"
							+ dirName)) {
						isExist = true;
						break;
					}
				}
				if (!isExist) {
					client.makeDirectory(params.getRemoteRootPath() + "/"
							+ dirName);
				}
			}
		}
	}

	/**
	 * 批量下载报表文件
	 * 
	 * @param params
	 * @throws Exception
	 */
	public void ftpGetForReport(FtpParameters params) throws Exception {
		client.setFileType(FTP.BINARY_FILE_TYPE);
		client.enterLocalPassiveMode();
		FTPFile[] systemDirs = client.listFiles(params.getRemoteRootPath());
		for (FTPFile systemDir : systemDirs) {
			if (systemDir.isDirectory()) {
				FTPFile[] dateDirs = client.listFiles(params
						.getRemoteRootPath()
						+ "/" + systemDir.getName());
				for (FTPFile dateDir : dateDirs) {
					if (dateDir.isDirectory()
							&& dateDir.getName().equals(params.getDate())) {
						String localTempPath = params.getLocalRootPath()
								+ File.separator + systemDir.getName()
								+ File.separator + params.getDate();
						File localTemp = new File(localTempPath);
						localTemp.mkdirs();
						String remoteTempPath = params.getRemoteRootPath()
								+ "/" + systemDir.getName() + "/"
								+ params.getDate();
						ftpGetBatch(localTempPath, remoteTempPath);
					}
				}

			}
		}
	}

	/**
	 * 功能：从ftp服务器上下载文件<br>
	 * 函数名：ftpGet<br>
	 * 输入参数: 本地文件，远程文件<br>
	 * 返回参数: 是否成功<br>
	 * 
	 * @throws GCSReportException
	 */
	private boolean ftpGetBatch(String localFile, String remote)
			throws GCSReportException {
		boolean flag = false;
		try {
			FTPFile[] ftpfile = client.listFiles(remote);
			if (ftpfile != null && ftpfile.length > 0) {
				for (FTPFile file : ftpfile) {
					String localfileName = FilenameUtils.concat(localFile, file
							.getName());
					if (file.isDirectory()) {
						File f = new File(localfileName);
						f.mkdirs();
						this.ftpGetBatch(localfileName, remote + "/"
								+ file.getName());
					} else if (file.isFile()) {

						File f = new File(localfileName);
						// 如果文件已存在且大小相等则不再下载
						if (!f.exists() || (file.getSize() != getFileSizes(f))) {
							flag = ftpGet(client, localfileName, remote + "/"
									+ file.getName());
						}
					}
				}
			} else {
				flag = true;
			}
		} catch (Exception e) {
			log.error(e);
			throw new GCSReportException(e);
		}
		return flag;
	}

	/**
	 * 批量上传文件到BCSP ftp目录中 传输过程中文件名：4850000000YYYYMMDD.zip.tmp
	 * 传输完成后文件名：4850000000YYYYMMDD.zip
	 * 
	 * @param params
	 * @return
	 * @throws GCSReportException
	 * @throws IOException
	 */
	public boolean ftpPutForBCSP(FtpParameters params) throws Exception {
		boolean flag = false;
		client.setFileType(FTP.BINARY_FILE_TYPE);
		client.enterLocalPassiveMode();
		File fileRoot = new File(params.getLocalRootPath());
		File[] files = fileRoot.listFiles();
		// 循环遍历本文件
		for (File zipFile : files) {
			if (zipFile.isFile()
					&& zipFile.getName().indexOf(params.getDate()) >= 0) {
				flag = client.storeFile(params.getRemoteRootPath() + "/"
						+ zipFile.getName() + ".tmp", new FileInputStream(
						zipFile));
				if (flag) {
					client.rename(params.getRemoteRootPath() + "/"
							+ zipFile.getName() + ".tmp", params
							.getRemoteRootPath()
							+ "/" + zipFile.getName());
					log.info("ftpPutForBCSP filename:"
							+ params.getRemoteRootPath() + zipFile.getName());
				}
			}
		}
		return flag;
	}

	/**
	 * 批量上传文件到远程ftp目录中 本地目录结构：root/系统简称(GCS)/日期(20110101)/机构号(4850000000)
	 * 远程目录结构：root/机构号(4850000000)/日期(20110101)/系统简称！！
	 * 
	 * @param params
	 * @return
	 * @throws GCSReportException
	 * @throws IOException
	 */
	public boolean ftpPutForReport(FtpParameters params) throws Exception {
		client.setFileType(FTP.BINARY_FILE_TYPE);
		client.enterLocalPassiveMode();
		// 在服务器上每个机构目录下，建立本次日期目录
		FTPFile[] bhidDir = client.listFiles(params.getRemoteRootPath());
		// 遍历循环服务器下“机构编号”目录
		for (FTPFile bhid : bhidDir) {
			if (bhid.isDirectory()) {
				boolean dateExist = false;
				FTPFile[] dateDir = client.listFiles(params.getRemoteRootPath()
						+ "/" + bhid.getName());
				if (dateDir == null || dateDir.length == 0) {
					makeDir(params.getRemoteRootPath() + "/" + bhid.getName()
							+ "/" + params.getDate());
				} else {
					for (FTPFile date : dateDir) {
						if (date.isDirectory()
								&& date.getName().equals(params.getDate())) {
							dateExist = true;
							break;
						}
					}
					if (!dateExist) {
						makeDir(params.getRemoteRootPath() + "/"
								+ bhid.getName() + "/" + params.getDate());
					}
				}
				FtpParameters childParams = new FtpParameters();
				childParams.setBhid(bhid.getName());
				childParams.setDate(params.getDate());
				childParams.setLocalRootPath(params.getLocalRootPath());
				childParams.setRemoteRootPath(params.getRemoteRootPath() + "/"
						+ bhid.getName() + "/" + params.getDate());
				ftpPutLoop(childParams);
			}
		}
		boolean flag = false;
		return flag;
	}

	public boolean ftpPutLoop(FtpParameters params) throws Exception {
		boolean flag = false;
		File fileRoot = new File(params.getLocalRootPath());
		File[] files = fileRoot.listFiles();
		InputStream input;
		BCSPReportZip.init(params);
		// 循环遍历本地第一层，系统简称：如GCS
		for (File systemDir : files) {
			if (systemDir.isDirectory()) {
				// 根据机构号、日期指定本地操作目录
				String temp_local_path = systemDir.getPath() + File.separator
						+ params.getDate() + File.separator + params.getBhid();
				File fileRootTemp = new File(temp_local_path);
				File[] filesTemp = fileRootTemp.listFiles();
				if (filesTemp != null && filesTemp.length > 0) {
					for (File reportFile : filesTemp) {
						if (reportFile.isDirectory()) {
							continue;
						} else if (reportFile.isFile()
						// EI对账报文过滤
								&& reportFile
										.getName()
										.indexOf(
											PropertyUtil
													.getFtp("report_ftp_localpath_ei_interfaceFile")) < 0
								// ISS对账报文过滤
								&& reportFile
										.getName()
										.indexOf(
											PropertyUtil
													.getFtp("report_ftp_localpath_iss_interfaceFile")) < 0
								// 控制文件*.stat过滤
								&& reportFile.getName().toLowerCase().indexOf(
									".stat") < 0) {
							// 如果文件已存在且大小相等则不再上传
							FTPFile[] remoteFile = client.listFiles(params
									.getRemoteRootPath()
									+ "/" + systemDir.getName()
									+ "/" + reportFile.getName());
							if (remoteFile == null
									|| remoteFile.length == 0
									|| (remoteFile[0].getSize() != getFileSizes(reportFile))) {
								input = new FileInputStream(reportFile);
								// 增加了系统目录！！
								client.makeDirectory(params.getRemoteRootPath()
										+ "/" + systemDir.getName());
								flag = client.storeFile(params
										.getRemoteRootPath()
										+ "/"
										+ systemDir.getName()
										+ "/"
										+ reportFile.getName(), input);
								// 打包并上传BCSP
								
								//郭斌
								
								BCSPReportZip.zipForReport(reportFile);

								log.info("ftpPut:" + params
									.getRemoteRootPath()
									+ "/"
									+ systemDir.getName()
										+ ":" + reportFile.getName());
								// System.out.println("ftpPut:"
								// + params.getRemoteRootPath() + ":"
								// + reportFile.getName());
								input.close();
							}
						}
					}
				}
			}
		}

		BCSPReportZip.end();
		return flag;
	}

	/**
	 * 函数名：ftpGet<br>
	 * 功能：获得远程文件<br>
	 * 
	 * @param ftp
	 * @param localFileNameAndPath
	 * @param remote
	 * @return
	 * @throws IOException
	 *             boolean
	 */
	private boolean ftpGet(FTPClient ftp, String localFileNameAndPath,
			String remote) throws IOException {
		// ftp.setFileType(FTP.BINARY_FILE_TYPE);
		// ftp.enterLocalPassiveMode();
		OutputStream output = new FileOutputStream(localFileNameAndPath);
		// long a = System.currentTimeMillis();
		boolean flag = ftp.retrieveFile(remote, output);
		// long b = System.currentTimeMillis();
		// System.out.println("ftpGet:" + localFileNameAndPath + ":" + (b - a)
		// / 1000 + "seconds");
		log.info("ftpGet:" + localFileNameAndPath);
		output.close();
		return flag;
	}

	/**
	 * 功能：<br>
	 * 函数名：makeDir<br>
	 * 输入参数: <br>
	 * 返回参数: <br>
	 * 
	 * @throws GCSReportException
	 */
	public boolean makeDir(String pathName) throws GCSReportException {
		boolean flag = false;
		try {
			flag = client.makeDirectory(pathName);
		} catch (IOException e) {
			log.error(e);
			throw new GCSReportException(e);
		}
		return flag;
	}

	public long getFileSizes(File f) throws Exception {// 取得文件大小
		long s = 0;
		if (f.exists()) {
			FileInputStream fis = null;
			fis = new FileInputStream(f);
			s = fis.available();
			if (fis != null) {
				fis.close();
			}
		}
		// else {
		// f.createNewFile();
		// System.out.println("文件不存在");
		// }
		return s;
	}

	/**
	 * 功能：关闭ftp连接<br>
	 * 函数名：ftpClose<br>
	 * 输入参数:ftp
	 * 
	 * @throws GCSReportException
	 */
	public void ftpClose() {
		if (client != null && client.isConnected()) {
			try {
				client.logout();
				client.disconnect();
			} catch (IOException e) {
				log.error(e);
			}
		}
	}

	public void deleteDir(FtpParameters params) throws Exception {
		// client.setFileType(FTP.BINARY_FILE_TYPE);
		client.enterLocalPassiveMode();
		FTPFile[] bhidDir = client.listFiles(params.getRemoteRootPath());
		for (FTPFile date : bhidDir) {
			if (date.isDirectory()) {
				FTPFile[] dateDirs = client.listFiles(params
						.getRemoteRootPath()
						+ "/" + date.getName());
				if (dateDirs == null || dateDirs.length == 0) {
					client.deleteFile(params.getRemoteRootPath() + "/"
							+ date.getName());
					System.out
							.println("delete file(dir):"
									+ params.getRemoteRootPath() + "/"
									+ date.getName());
				}
			}
		}
	}

	public void mkDateDir(FtpParameters params) throws Exception {
		client.enterLocalPassiveMode();
		String startS = "20160301";
		String endS = "20160331";
		SimpleDateFormat format = new SimpleDateFormat("yyyymmdd");
		Calendar start = Calendar.getInstance();
		Calendar end = Calendar.getInstance();
		start.setTime(format.parse(startS));
		end.setTime(format.parse(endS));
		while (start.getTime().before(end.getTime())) {
			start.add(Calendar.DAY_OF_YEAR, 1);
			String temp = format.format(start.getTime());
			// client.makeDirectory(params.getRemoteRootPath() +"/"+temp);
		}
	}

	public static void main(String[] args) {
		FTPOper ftp;
		FileOutputStream fileOut = null;
		ZipOutputStream outputStream = null;
		try {

			// ftp = new FTPOper("display");
			// FtpParameters params = new FtpParameters();
			// params.setRemoteRootPath("/gcs_report/4850000000");
			// ftp.deleteDir(params);

			ftp = new FTPOper("report");
			FtpParameters params = new FtpParameters();
			params.setRemoteRootPath("/ISS/4850000000");
			// ftp.mkDateDir(params);
			ftp.deleteDir(params);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
		}

	}
}
