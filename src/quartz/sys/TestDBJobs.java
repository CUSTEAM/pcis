package quartz.sys;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import quartz.BaseJob;

/**
 * 資料庫狀態測試
 * 停用
 * @author John
 *
 */
public class TestDBJobs extends BaseJob implements Job {
	
	private String ip="...";
	//private String port="3306";

	public void execute(JobExecutionContext context)throws JobExecutionException {
		
		
		SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sft = new SimpleDateFormat("HH:mm:ss");
		Date date=new Date();
		File file = new File("/home/log/"+sfd.format(date)+".txt");
		if(!ping(ip)){
			write(file, sft.format(date)+" FAILURE - ping "+ip);
		}
	}
	
	private void write(File file, String log){
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		FileWriter fw;
		BufferedWriter bw;
		try {
			fw = new FileWriter(file, true);
	        bw = new BufferedWriter(fw);
	        bw.write(log+"\n");
	        bw.close();	
		} catch (IOException e) {			
			e.printStackTrace();
		}		
	}
	
	private boolean ping(String ip){
		try {			 
            InetAddress address = InetAddress.getByName(ip);
            return address.isReachable(500);            
        } catch (Exception e) {
            return false;
        }
	}
	
	/*private void telnet(String ip, String port){
		Socket server = null;
		try {
		    server = new Socket();
		    InetSocketAddress address = new InetSocketAddress("192.168.0.201",8899);
		    server.connect(address, 5000);
		}catch (Exception e) {
		    System.out.println("telnet失败");
		}finally{
		    if(server!=null)
		        try {
		            	server.close();
		        	} catch (Exception e) {
		        		
		        }
		}
	}*/
	

}
