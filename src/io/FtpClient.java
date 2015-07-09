package io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
//import org.apache.log4j.Logger;

/**
 * 遠端操作FTP
 * @author JOHN
 * 
 */
public class FtpClient {

	private static String FTPHost;
	private static String username;
	private static String password;
	private String LocalDir;
	private String ServerDir;
	private FTPClient ftp;

	private boolean binaryTransfer = false;

	//private final static Logger log = Logger.getLogger(FtpClient.class);

	/**
	 * @param server FTP主機位址
	 * @param username 帳號
	 * @param password 密碼
	 */
	public FtpClient(String FTPHost, String username, String password,
			String LocalDir, String ServerDir) {

		this.FTPHost = FTPHost;
		this.username = username;
		this.password = password;
		this.LocalDir = LocalDir;
		this.ServerDir = ServerDir;
		ftp = new FTPClient();
		/*
		 * if(Configuration.PrintFTPCommandLog){ //印出命令字元
		 * ftp.addProtocolCommandListener(new PrintCommandListener()); }
		 */
	}

	public FtpClient() {
		this(FTPHost, username, password, FTPHost, FTPHost);
	}

	public boolean connect() {
		try {
			int reply;
			ftp.connect(FTPHost);

			// 是否成功
			reply = ftp.getReplyCode();

			if (FTPReply.isPositiveCompletion(reply)) {
				if (ftp.login(username, password)) {
					// 設為passive模式
					ftp.enterLocalPassiveMode();
					return true;
				}
			} else {
				ftp.disconnect();
				//log.error("FTP server refused connection.");
				System.out.println("FTP server refused connection.");
			}
		} catch (IOException e) {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (IOException f) {
				}
			}
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 下載遠端檔案
	 * @param fileName 檔案名稱不含路徑
	 * @param delFile 完成後是否刪除
	 * @return
	 */
	public boolean get(String fileName, boolean delFile) {
		String remote = ServerDir + fileName;
		String local = LocalDir + fileName;
		return get(remote, local, delFile);
	}

	/**
	 * 上傳檔案
	 * @param fileName 檔案名稱不含路徑
	 * @param delFile 完成後是否刪除
	 * @return
	 */
	public boolean put(String fileName, boolean delFile) {
		// String remote = ServdrDir + fileName;
		String remote = ServerDir + fileName;
		String local = LocalDir + fileName;

		return put(remote, local, delFile);
	}

	/**
	 * 上傳多個檔案
	 */
	public boolean[] put(String[] fileNames, boolean delFile) {
		boolean[] result = new boolean[fileNames.length];
		for (int j = 0; j < result.length; j++) {
			result[j] = false;
		}
		//String remoteFile;
		String localFile;
		for (int i = 0; i < fileNames.length; i++) {
			localFile = fileNames[i];
			result[i] = put(localFile, delFile);
		}
		return result;
	}

	/**
	 * 上傳檔案
	 */
	private boolean put(String remoteAbsoluteFile, String localAbsoluteFile, boolean delFile) {
		InputStream input = null;
		try {
			ftp.makeDirectory(ServerDir);
			// 設置檔案類型(2進制或文字)
			if (binaryTransfer) {
				ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
			} else {
				ftp.setFileType(FTPClient.ASCII_FILE_TYPE);
			}
			// 處理傳輸
			input = new FileInputStream(localAbsoluteFile);
			ftp.storeFile(remoteAbsoluteFile, input);
			input.close();
			if (delFile) {
				(new File(localAbsoluteFile)).delete();
			}
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			try {
				if (input != null) {
					input.close();
				}
				//ftp.disconnect();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * 下載檔案
	 */
	public boolean get(String remoteAbsoluteFile, String localAbsoluteFile,
			boolean delFile) {
		OutputStream output = null;
		try {
			// 設置檔案類型
			if (binaryTransfer) {
				ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
			} else {
				ftp.setFileType(FTPClient.ASCII_FILE_TYPE);
			}
			// 處理傳輸
			output = new FileOutputStream(localAbsoluteFile);
			ftp.retrieveFile(remoteAbsoluteFile, output);
			output.close();
			if (delFile) { // 刪除遠端檔案
				ftp.deleteFile(remoteAbsoluteFile);
			}
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			try {
				if (output != null) {
					output.close();
				}
			} catch (IOException e2) {
			}
		}
		return false;
	}

	/**
	 * 列出遠端路徑所有檔案
	 */
	public String[] listNames(String remotePath) {
		String[] fileNames = null;
		try {
			FTPFile[] remotefiles = ftp.listFiles(remotePath);
			fileNames = new String[remotefiles.length];
			for (int i = 0; i < remotefiles.length; i++) {
				fileNames[i] = remotefiles[i].getName();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileNames;
	}

	/**
	 * 結束FTP
	 */
	public void disconnect() {
		try {
			ftp.logout();
			if (ftp.isConnected()) {
				ftp.disconnect();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 查看伺服器是否為Binary傳輸模式
	 */
	public boolean isBinaryTransfer() {
		return binaryTransfer;
	}

	public void setBinaryTransfer(boolean binaryTransfer) {
		this.binaryTransfer = binaryTransfer;
	}

	public static String getFTPHost() {
		return FTPHost;
	}

	public static void setFTPHost(String host) {
		FTPHost = host;
	}

	public String getLocalDir() {
		return LocalDir;
	}

	public void setLocalDir(String localDir) {
		LocalDir = localDir;
	}

	public String getServerDir() {
		return ServerDir;
	}

	public void setServerDir(String servdrDir) {
		ServerDir = servdrDir;
	}

	public static String getUsername() {
		return username;
	}

	public static void setUsername(String username) {
		FtpClient.username = username;
	}

	public static String getPassword() {
		return password;
	}

	public static void setPassword(String password) {
		FtpClient.password = password;
	}

	public FTPClient getFtp() {
		return ftp;
	}

	public void setFtp(FTPClient ftp) {
		this.ftp = ftp;
	}

	//public static Logger getLog() {
		//return log;
	//}

}
