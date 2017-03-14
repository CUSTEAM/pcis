package quartz;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts2.ServletActionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import service.impl.AccountManager;
import service.impl.CourseManager;
import service.impl.DataFinder;
import service.impl.DataUpdater;
import service.impl.StudAffairManager;
import service.impl.base.BaseAccessImpl;
import service.impl.base.BaseIOImpl;
import service.impl.base.BaseLiteralImpl;
import service.impl.base.BaseMathImpl;


/**
 * 基本排程
 * @author John *
 */
public class BaseJob{	
	
	//判斷是否為debug模式
	protected boolean isDebug=java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;
	private static final long serialVersionUID = 1L;
	ApplicationContext springContext=new ClassPathXmlApplicationContext("classpath:../applicationContext.xml");	
	
	/**
	 * 取得欲使用的beanManager
	 * @param name
	 * @return
	 */
	protected Object get(String name) {
		return springContext.getBean(name);
	}
	
	/**
	 * 獲取原始session
	 * @return
	 */
	public static HttpSession getSession(){
		return ServletActionContext.getRequest().getSession();
    }
	
	/**
	 * 原生request
	 * @return
	 */
    public static HttpServletRequest getRequest(){
        return ServletActionContext.getRequest();
    }
    
    /**
     * 數學
     * @return
     */    
    public BaseMathImpl bm = (BaseMathImpl) get("BaseMathImpl");
    
    /**
     * 文字
     * @return
     */    
    public BaseLiteralImpl bl = (BaseLiteralImpl) get("BaseLiteralImpl");
    
    /**
     * 輸入/出
     * @return
     */
    public BaseIOImpl bio = (BaseIOImpl) get("BaseIOImpl");
    
    /**
     * 資料存取
     * @return
     */
    public BaseAccessImpl dm = (BaseAccessImpl) get("DataManager");    
    
    /**
     * 資料查詢
     * @return
     */
    public DataFinder df = (DataFinder) get("DataFinder");
    
    /**
     * 資料修改
     * @return
     */    
    public DataUpdater du = (DataUpdater) get("DataUpdater");
    
    /**
     * 帳號
     * @return
     */
    public AccountManager am = (AccountManager) get("AccountManager");
    
    /**
     * 課務
     * @return
     */
    public CourseManager cm = (CourseManager) get("CourseManager");
    
    /**
     * 學務
     * @return
     */
    public StudAffairManager sam = (StudAffairManager) get("StudAffairManager");  
	
	
	
	
}
