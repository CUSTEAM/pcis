package quartz.dilg;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;

import quartz.BaseJob;
import service.impl.StudAffairManager;
import service.impl.base.BaseIOImpl;
import service.impl.base.BaseLiteralImpl;

/**
 * 未點名通知
 * @author John
 *
 */
public class RollCallAlert extends BaseJob{
	
	ApplicationContext springContext;
	
	public RollCallAlert(ApplicationContext springContext){
		this.springContext=springContext;
	}	
	
	public void doit(){	
		StudAffairManager sam= (StudAffairManager) springContext.getBean("StudAffairManager");	
		
		Calendar c=Calendar.getInstance();
		SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd");	
		
		String RollCall_begin=sam.RollCall_begin();
		String RollCall_end=sam.RollCall_end();
		
		Date begin;
		Date end;
		try{
			begin=sf.parse(RollCall_begin);
			end=sf.parse(RollCall_end);
		}catch(Exception e){
			begin=new Date();
			end=new Date();
		}
		
		String school_term=sam.school_term();
		BaseLiteralImpl si= (BaseLiteralImpl) springContext.getBean("BaseLiteralImpl");
		BaseIOImpl im = (BaseIOImpl) springContext.getBean("BaseIOImpl");
    	
		List<Map>empl=sam.getDataFinder().sqlGet("SELECT cl.ClassName, c.chi_name, d.techid, e.cname, " +
    	"e.Email FROM Class cl, Dtime d, empl e, Csno c WHERE cl.ClassNo=d.depart_class AND " +
    	"c.cscode=d.cscode AND d.techid=e.idno AND d.Sterm='"+school_term+"' GROUP BY d.techid");
		
		List dilguneed=sam.dilguneed(RollCall_begin, RollCall_end);//不點名
    	
    	List tmp;    	
    	List myCs=null;
    	String Email;    	
    	
		StringBuilder sb;
		StringBuilder a=new StringBuilder();
		StringBuilder f=new StringBuilder();
		
		Date today=new Date();
    	
		//若為點名期間		
		if(today.getTime()>=begin.getTime() && today.getTime()<=end.getTime()){
			for(int i=0; i<empl.size(); i++){	    		
	    		try{
	    			tmp=sam.getDataFinder().sqlGet("SELECT c.chi_name, d.Oid as dOid, cl.CampusNo,  cl.SchoolNo, " +
	            	"dc.*, cl.ClassName FROM Dtime d, Class cl, Dtime_class dc, Csno c " +
	            	"WHERE c.cscode=d.cscode AND d.depart_class=cl.ClassNo AND d.Oid=dc.Dtime_oid AND " +
	            	"d.Sterm='"+school_term+"' AND d.techid='"+empl.get(i).get("techid")+"' ORDER BY dc.begin");
	        		
	        		try {
	        			myCs=getCallInfo(tmp, dilguneed, 4, begin, end);				
	    			} catch (ParseException e) {
	    				e.printStackTrace();
	    			}  
	        		if(myCs.size()<1){
	        			continue;
	        		}else{	
	        			
	        			Email=empl.get(i).get("Email").toString();	        			
	        			//Email="hsiao@cc.cust.edu.tw";
	        			
	        			if(!si.validateEmail(Email)||Email.trim().equals("")||Email.trim().length()<10){
	        				f.append(empl.get(i).get("cname")+",");
	        				continue;
	        			}        			
	        			sb=new StringBuilder("<p>"+empl.get(i).get("cname")+"老師您好:</p><br>"  );
	        			sb.append("<p>本學期點名期間自:"+RollCall_begin+"至"+RollCall_end+"</p>");
	        			sb.append("<p>目前尚未編輯缺曠的課程如下:</p>");        			
	        			
	        			for(int j=0; j<myCs.size(); j++){
		        			sb.append( ((Map)myCs.get(j)).get("date")+"星期"+((Map)myCs.get(j)).get("week")+", 第"+
		        			((Map)myCs.get(j)).get("begin")+"節至"+((Map)myCs.get(j)).get("end")+"節, "+((Map)myCs.get(j)).get("ClassName")+" - "+
		        			((Map)myCs.get(j)).get("chi_name")+"<br>");
	        			} 
	        			
	        			this.sendMail(Email, "點名記錄異常通知", sb.toString());	        			     			
	        			a.append(empl.get(i).get("cname")+",");
	        		}
	    		}catch(Exception e){
	    			f.append(empl.get(i).get("cname")+",");
	    			e.printStackTrace();
	    			continue;
	    		}
	    	}
	    	sam.getDataFinder().exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('教師點名通知','寄送完成:"+a+"<br>寄送失敗:"+f+"');");
			
		}else{
			sam.getDataFinder().exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('教師點名通知','非點名期間');");
		}
	}
	
	/**
	 * 超過幾天寄送？
	 * @param list
	 * @param day
	 * @return
	 * @throws ParseException
	 */
	private List getCallInfo(List list, List dilguneed, int day, Date begin, Date end) throws ParseException{
		
		StudAffairManager sam= (StudAffairManager) springContext.getBean("StudAffairManager");
		SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd");	
		
		Date today=new Date();
		Calendar c=Calendar.getInstance();
    	c.add(Calendar.DAY_OF_YEAR, -5);
		Map map;
		int week;
		
		String DilgLog_date;
		String Dtime_oid;
		int DilgLog_date_due;		
		
		List myCs=new ArrayList();
		
		
		String nd;
		//要執行的天數
		for(int i=1; i<=day; i++){			
			
			//學期前後日子不點名
			if(c.getTimeInMillis()<begin.getTime() || c.getTimeInMillis()>end.getTime()){
				continue;
			}
			
			//星期與排課星期同步
			week=c.get(Calendar.DAY_OF_WEEK)-1;
			if(week==0){
				week=7;
			}	
			
			//所有課程中排課星期相同的課程加入點名
			for(int j=0; j<list.size(); j++){
				
				if(!sam.Dilg_uneed(dilguneed, sf.format(c.getTime()))){
					continue;
				}
				
				if((int)((Map)list.get(j)).get("week")==week){
					map=new HashMap();
					map.putAll((Map)list.get(j));					
					DilgLog_date=sf.format(c.getTime());
					Dtime_oid=map.get("dOid").toString();					
					
					try{
						//點名記錄
						DilgLog_date_due=sam.DilgLog_date_due(Dtime_oid, DilgLog_date);
					}catch(Exception e){
						DilgLog_date_due=0;
					}					
							
					//map.put("wdate", DilgLog_date);	
					map.put("date", DilgLog_date);						
					//檔日有點名記錄
					if(DilgLog_date_due==0){
						//map.put("select", DilgLog_date_due);
						//map.put("info", sam.Dilg_info(Dtime_oid, DilgLog_date));
						//map.put("log", true);
						myCs.add(map);
					}
				}		
								
			}
			//1天完畢下1天
			c.add(Calendar.DAY_OF_YEAR, -1);
		}
		
		
		return myCs;
	}
	
}
