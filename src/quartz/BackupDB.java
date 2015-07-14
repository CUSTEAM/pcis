package quartz;

import io.FtpClient;
import io.GenFile;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import service.impl.base.BaseAccessImpl;

/**
 * 資料庫異地備份
 * @author John
 *
 */
public class BackupDB implements Job{		
	
	static final int BUFFER = 8192;	
	
	static String cmd;
	
	public void execute(JobExecutionContext context)throws JobExecutionException {
		ApplicationContext springContext=new ClassPathXmlApplicationContext("classpath:../applicationContext.xml");		
		BaseAccessImpl df= (BaseAccessImpl) springContext.getBean("DataManager");	
				
		String base = "/var/backup/";//本機暫存絕對路徑
		//String base = "backup/";//本機暫存路徑
		//Dao dao = new Dao();//資料連接池
		GenFile c = new GenFile();//文件連接池
		
		Date date = new Date();
		SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmm");
		
		// 所有資料表
		List<Map>list=df.sqlGet("SHOW TABLES");
		
		// 排除的資料表
		List<Map<String,String>>single=df.sqlGet("SELECT table_name FROM SYS_BACKUP_TABLE WHERE type='single'");
		List<Map<String,String>>complex=df.sqlGet("SELECT table_name FROM SYS_BACKUP_TABLE WHERE type='mixed'");
		Calendar cal=Calendar.getInstance();
		//2月1日與7月1日完整備份
		if((cal.get(Calendar.MONTH)==8 && cal.get(Calendar.DAY_OF_MONTH)==14)|| (cal.get(Calendar.MONTH)==1 && cal.get(Calendar.DAY_OF_MONTH)==1) ){
			single=new ArrayList();
			complex=new ArrayList();
		}
		//遠端備份資料庫位置
		//List<Map>dbs=dao.QueryForList("SELECT * FROM SYS_BACKUP_HOST WHERE type='DB'");
		List<Map>dbs=df.sqlGet("SELECT * FROM SYS_HOST WHERE useid='DBbackup' AND protocol='SQLDUMP'");
		Map dbr=df.sqlGetMap("SELECT * FROM SYS_HOST WHERE useid='DBSource' AND protocol='SQLDUMP'");
		//遠端備份檔案位置
		//List<Map<String,String>>ftps=dao.QueryForList("SELECT * FROM SYS_BACKUP_HOST WHERE type='FTP'");
		List<Map<String,String>>ftps=df.sqlGet("SELECT * FROM SYS_HOST WHERE useid='DBbackup' AND protocol='FTP'");

		// 建立作業資料夾
		if(System.getProperty("os.name").toLowerCase().indexOf("win")>-1){
			base="C:/backup/";
		}		
		String path=base+sf.format(date.getTime())+"/";
		//System.out.println("建立資料表"+path+":"+c.mkDir(path));
		File f=new File(path);
		f.mkdirs();
		if(!f.exists()){
			c.mkDir(path);
		}
		try {
			Runtime.getRuntime().exec("chmod 777 " + path);//linux下變更權限，最終此資料夾會被刪除
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		System.out.println(path+"是否存在:"+f.exists());
		
		String table;//資料表名稱&檔名
		
		boolean garbage;//是否為排除資料表
		
		StringBuilder log=new StringBuilder();
		
		//資料表作業
		for (int i = 0; i < list.size(); i++) {	
		//for (int i = 0; i < 2; i++) {	
			
			table = list.get(i).get("Tables_in_CIS").toString();// 淦為什麼叫這個			
			garbage = false;	
			
			
			//排除單一資料表			
			for (int x = 0; x < single.size(); x++) {				
				if (table.equals(single.get(x).get("table_name"))) {					
					garbage = true;
					continue;
				}
			}			
			if (garbage)continue;//若為排除資料表立即放棄作業
			
			//排除多資料表
			for (int x = 0; x < complex.size(); x++) {
				if (table.indexOf(complex.get(x).get("table_name"))>-1) {					
					garbage = true;
					continue;
				}
			}	
			
			
			if (garbage)continue;//若為排除資料表立即放棄作業
			
			String cmd;		
			String[] lmd;
			try {
				//dump作業 TODO 執行緒
				if(System.getProperty("os.name").toLowerCase().indexOf("win")>-1){//windows
					cmd=new String("mysqldump --default-character-set=utf8 -uroot -h"+dbr.get("host_debug")+" -pspring CIS "+ table + " > " + "\""+path+table+ "\"");				
					runexec("cmd /c"+cmd.replace("/", "\\"));//window系統下的轉換
					System.out.println(cmd);
				}else{
					//linux
					lmd=new String[]{"/bin/bash","-c","/usr/bin/mysqldump --default-character-set=utf8 -uroot -h"+dbr.get("host_runtime")+" -pspring CIS "+table+">"+path+table};
					Runtime.getRuntime().exec(lmd);  
				}
				//System.out.println(""+cmd);
				
				//回存作業 TODO 執行緒
				for(int j=0; j<dbs.size(); j++){
					if(System.getProperty("os.name").toLowerCase().indexOf("win")>-1){//windows
						cmd=new String("mysql -h"+dbs.get(j).get("host_debug")+" -u"+dbs.get(j).get("username")+
						" -p"+dbs.get(j).get("password")+" -f -D "+dbs.get(j).get("path")+" < "+ "\""+path+table+ "\"");					
						runexec("cmd /c"+cmd.replace("/", "\\"));//window系統下的轉換	
					}else{
						//linux
						lmd=new String[]{"/bin/bash","-c","/usr/mysql -uroot -h"+dbr.get("host_runtime")+" -pspring CIS "+ table + " > "+path+table};
						Runtime.getRuntime().exec(lmd);
					}
				}				
				
			} catch (Exception e) {
				log.append(e+"\n");
				e.printStackTrace();
			}
		}
		
		compress(path, base+sf.format(date.getTime())+".zip");//建立壓縮檔
		ftp(ftps, base, sf.format(date.getTime())+".zip");//異地儲存
		c.deleteAll(path);//刪除工作資料夾
		df.exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('主資料庫備份作業','備份至"+dbs.size()+"個遠端資料庫, "+ftps.size()+"個檔案伺服器\n"+log+"');");
	}
	
	static void ftp(List<Map<String,String>>ftps, String baseDir, String fileName){
		FtpClient ftp;
		for(int i=0; i<ftps.size(); i++){
			
			try{				
				if(System.getProperty("os.name").toLowerCase().indexOf("win")>-1){
					ftp=new FtpClient(ftps.get(i).get("host_debug"), ftps.get(i).get("username"), ftps.get(i).get("password"), null, null);	
				}else{
					ftp=new FtpClient(ftps.get(i).get("host_runtime"), ftps.get(i).get("username"), ftps.get(i).get("password"), null, null);	
				}
							
				ftp.connect();				
				ftp.setLocalDir(baseDir);
				ftp.setServerDir(ftps.get(i).get("path"));				
				if(!ftp.isBinaryTransfer()){
					ftp.setBinaryTransfer(true);
				}
				ftp.put(fileName, true);//傳輸完成刪不刪除檔案
				ftp.disconnect();				
			}catch(Exception e){
				e.printStackTrace();				
			}	
		}
	}
	
	/**
	 * 執行外部方法-mySQLdump
	 * TODO LINUX版更新後是否廢止此方法
	 */
	private static void runexec(String cmd) throws IOException, InterruptedException {
		Process process;
		try { // 使用Runtime來執行command，生成Process對象 Runtime
			 
			Runtime runtime = Runtime.getRuntime(); process = runtime.exec(cmd); //取得命令结果的输出流
			 
			InputStream is = process.getInputStream(); // 取得命令結果的輸出流
			 
			InputStreamReader isr = new InputStreamReader(is); // 用緩衝器讀行
			 
			BufferedReader br = new BufferedReader(isr); String line = null;
			 
			while ((line = br.readLine()) != null) { 
				System.out.println(line); 
			}
		 
			is.close(); isr.close(); br.close(); 
		} catch (IOException e) { 
			 //TODO Auto-generated catch block e.printStackTrace(); }
		}			
		/*
		Process p = Runtime.getRuntime().exec(cmd);
		// 等待編譯結束
		p.waitFor();
		// 檢查返回碼，看編譯是否出錯。
		int ret = p.exitValue();
		if (ret > 0) {
			System.out.println(cmd);
			System.out.println(ret);
		}
		*/
		
	}
	
	/**
	 * TODO 多執行緒工作
	public void run() {
		try{
			
			System.out.println(cmd);
			Process p = Runtime.getRuntime().exec(cmd);
			// 等待編譯結束
			p.waitFor();
			// 檢查返回碼，看編譯是否出錯。
			int ret = p.exitValue();
			if (ret > 0) {
				System.out.println(cmd);
				System.out.println(ret);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	*/ 
	
	/**
	 * 檔案壓縮
	 */
	public static void compress(String srcPathName, String zipFile) {
		File file = new File(srcPathName);
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(zipFile);
			CheckedOutputStream cos = new CheckedOutputStream(fileOutputStream,new CRC32());			
			ZipOutputStream out = new ZipOutputStream(cos);
			String basedir = "";//zip內的目錄結構
			compress(file, out, basedir);
			out.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 判斷目錄或檔案
	 */
	private static void compress(File file, ZipOutputStream out, String basedir) {		
		if(file.isDirectory()) {compressDirectory(file, out, basedir);}else{compressFile(file, out, basedir);}
	}

	/**
	 * 處理目錄
	 */
	private static void compressDirectory(File dir, ZipOutputStream out, String basedir) {
		//if (!dir.exists())return;
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {			
			compress(files[i], out, basedir + dir.getName() + "/");
		}
	}

	/**
	 * 壓縮檔案
	 */
	private static void compressFile(File file, ZipOutputStream out, String basedir) {
		//if (!file.exists()) {return;}		
		try {
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
			ZipEntry entry = new ZipEntry(basedir + file.getName());			
			out.putNextEntry(entry);
			int count;
			byte data[] = new byte[BUFFER];
			while ((count = bis.read(data, 0, BUFFER)) != -1) {
				out.write(data, 0, count);
			}
			bis.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}