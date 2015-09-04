package quartz.score;

import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;

import service.impl.base.BaseAccessImpl;
import service.impl.base.BaseMathImpl;
import service.impl.StudAffairManager;

/**
 * 重新計算近4學年中Stavg中的排名、平均成績
 * @author John
 *
 */
public class StavgRerank {
	
	ApplicationContext springContext;
	
	public StavgRerank(ApplicationContext springContext){
		this.springContext=springContext;
	}	
	public void doit(){		
		BaseAccessImpl df= (BaseAccessImpl) springContext.getBean("DataManager");
		BaseMathImpl bi = (BaseMathImpl) springContext.getBean("BaseMathImpl");
		StudAffairManager sam = (StudAffairManager) springContext.getBean("StudAffairManager");		
		int school_year=Integer.parseInt(sam.school_year());		
		for(int i=school_year-4; i<=school_year; i++){	
			df.exSql("DELETE FROM Stavg WHERE school_year="+i);
			for(int j=1; j<=2; j++){				
				df.exSql("INSERT INTO Stavg (student_no, score, total_credit, school_year, school_term) "
				+ "(SELECT student_no, IFNULL(SUM(score*credit)/SUM(credit),0) as score, IFNULL(SUM(credit),0)as "
				+ "total_credit, school_year, school_term FROM ScoreHist WHERE evgr_type NOT IN(5, 6)AND "
				+ "(school_year="+i+" AND school_term="+j+") GROUP BY student_no)");
			}			
		}		
		
		List<Map>list=df.sqlGet("SELECT s.school_year, s.school_term, depart_class FROM Stavg s " +
		"WHERE s.school_year>='"+(school_year-4)+"' AND s.depart_class!=''GROUP BY s.school_year, s.school_term, s.depart_class");
		List<Map>scores;			
		
		for(int i=0; i<list.size(); i++){			
			scores=df.sqlGet("SELECT Oid, score FROM " +
			"Stavg s WHERE s.school_year='"+list.get(i).get("school_year")+"' AND " +
			"s.school_term='"+list.get(i).get("school_term")+"' AND s.depart_class='"+list.get(i).get("depart_class")+"'");			
			scores=bi.sortListByKey(scores, "score", true);			
			for(int j=0; j<scores.size(); j++){
				df.exSql("UPDATE Stavg SET rank="+(j+1)+" WHERE Oid="+scores.get(j).get("Oid"));
			}
		}	
		
		
		df.exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('學生排名維護','完成');");
	}

}
