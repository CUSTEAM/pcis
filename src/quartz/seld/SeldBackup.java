package quartz.seld;

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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import model.Seld;

import org.springframework.context.ApplicationContext;

import service.impl.base.BaseAccessImpl;


public class SeldBackup {
	
	ApplicationContext springContext;
	
	public SeldBackup(ApplicationContext springContext){
		this.springContext=springContext;
	}	
	
	public void doit(){		
		System.out.println("working...");
		String base = "/home/tmp/SeldBackup/";//本機暫存絕對路徑
		String baseWin="C:/home/SeldBackup/";
		//String base = "backup/";//本機暫存路徑
		//Dao dao = new Dao();//資料連接池
		GenFile c = new GenFile();//文件連接池
		
		Date date = new Date();
		SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
		BaseAccessImpl df= (BaseAccessImpl) springContext.getBean("DataManager");
				
		//遠端備份檔案位置
		//List<Map<String,String>>ftps=dao.QueryForList("SELECT * FROM SYS_BACKUP_HOST WHERE type='FTP'");
		List<Map<String,String>>ftps=df.sqlGet("SELECT * FROM SYS_HOST WHERE useid='SeldBackup' AND protocol='FTP'");

		// 建立作業資料夾		
		//c.mkDir(base);
		File folder=new File(base);
		if(!folder.mkdir()){
			folder=new File(baseWin);
			folder.mkdir();
			base=baseWin;
		}
		
		String file="/"+sf.format(date);
		
		
		List<Seld>selds=df.hqlGetListBy("FROM Seld");
		
		
		try {
			c.writeText_UTF8("---------------\n", folder.getPath()+file);
			for(int i=0; i<selds.size(); i++){
				c.writeText_UTF8_Apend(selds.get(i).getCoansw()+
						","+selds.get(i).getStudentNo()+","+
						selds.get(i).getDtime_teacher()+
						selds.get(i).getDtimeOid()+","+
						selds.get(i).getElearnDilg()+","+
						selds.get(i).getOid()+","+
						selds.get(i).getScore()+","+
						selds.get(i).getScore01()+","+
						selds.get(i).getScore02()+","+
						selds.get(i).getScore03()+","+
						selds.get(i).getScore04()+","+
						selds.get(i).getScore05()+","+
						selds.get(i).getScore06()+","+
						selds.get(i).getScore07()+","+
						selds.get(i).getScore08()+","+
						selds.get(i).getScore09()+","+
						selds.get(i).getScore10()+","+
						selds.get(i).getScore11()+","+
						selds.get(i).getScore12()+","+
						selds.get(i).getScore13()+","+
						selds.get(i).getScore14()+","+
						selds.get(i).getScore15()+","+
						selds.get(i).getScore16()+","+
						selds.get(i).getScore17()+","+
						selds.get(i).getScore18()+","+
						selds.get(i).getScore2()+","+
						selds.get(i).getScore3()+","+						
						"\n", folder.getPath()+file);
			}
			
			//dump作業 TODO 執行緒
			/*
			System.out.println("mysqldump --default-character-set=utf8 -uroot - - CIS Seld > "+base+file);
			cmd=new String("mysqldump --default-character-set=utf8 -uroot - - CIS Seld > "+base+file);				
			try{
				runexec("cmd /c"+cmd.replace("/", "\\"));//window
			}catch(Exception e){
				e.printStackTrace();
			}
			
			try{
				runexec(cmd);//unix
			}catch(Exception e){
				e.printStackTrace();
			}*/
			
			
		} catch (Exception e) {
			//log.append(e+"\n");
			e.printStackTrace();
		}
		
		//FtpClient ftp;
		//compress(path, base+sf.format(date.getTime())+".zip");//建立壓縮檔
		ftp(ftps, folder.getPath(), file);//異地儲存
		c.deleteAll(base);//刪除工作資料夾
		Date date1 = new Date();
		df.exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('選課資料備份作業','完成共費時:" + ((date1.getTime() - date.getTime()) / 1000)+"秒');");
		//System.out.println("共費時:" + ((date1.getTime() - date.getTime()) / 1000)+ "秒");		
	}
	
	static void ftp(List<Map<String,String>>ftps, String baseDir, String fileName){
		FtpClient ftp;
		for(int i=0; i<ftps.size(); i++){
			
			try{				
				//System.out.println("con to "+ftps.get(i).get("host_runtime"));
				ftp=new FtpClient(ftps.get(i).get("host_runtime"), ftps.get(i).get("username"), ftps.get(i).get("password"), null, null);				
				ftp.connect();				
				ftp.setLocalDir(baseDir);
			}catch(Exception e){
				//System.out.println("con to "+ftps.get(i).get("host_debug"));
				ftp=new FtpClient(ftps.get(i).get("host_debug"), ftps.get(i).get("username"), ftps.get(i).get("password"), null, null);				
				ftp.connect();				
				ftp.setLocalDir(baseDir);			
			}	
				
			ftp.setServerDir(ftps.get(i).get("path"));				
			if(!ftp.isBinaryTransfer()){
				ftp.setBinaryTransfer(true);
			}
			ftp.put(fileName, false);//傳輸完成刪不刪除檔案
			ftp.disconnect();				
			
		}
	}
	
	/**
	 * 執行外部方法-mySQLdump
	 */
	private static void runexec(String cmd) throws IOException,
			InterruptedException {
		 
		
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
	
	
	

}
