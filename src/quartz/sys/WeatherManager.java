package quartz.sys;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import model.WeatherHist;
import quartz.BaseJob;

/**
 * 天氣更新
 * @author John
 *
 */
public class WeatherManager extends BaseJob{		
	
	public void doit(JobExecutionContext context)throws JobExecutionException, IOException, ParseException {
		System.out.println("WeatherManager Go!");
		SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sf1=new SimpleDateFormat("yyyy-MM-dd HH:mm");
		URL url = new URL("http://api.apixu.com/v1/forecast.json?q=25.035,121.611&lang=zh_tw&days=14&key=d75f9b4876b444e695580441172810");
		URLConnection con = url.openConnection();	
		
		con.setRequestProperty("User-agent", "	Mozilla/5.0 (Windows NT 6.1; WOW64; rv:33.0) Gecko/20100101 Firefox/33.0");

		
		
		
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
		String inputLine;
		StringBuffer sb = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			sb.append(inputLine);
			//System.out.println(inputLine);
		}
		in.close();
		
		
		JSONObject o=new JSONObject(sb.toString()),o1,o2;
		o=o.getJSONObject("forecast");			
		
		
		JSONArray a=(JSONArray)o.get("forecastday");
		//System.out.println(o.get("forecastday"));
		//JSONArray h;		
		String icon;
		WeatherHist w;
		Calendar c=Calendar.getInstance();
		Date now=new Date();
		for(int i=0; i<a.length(); i++){
			
			
			o=(JSONObject)a.get(i);		
			//h=o.getJSONArray("hour");	
			c.setTime(sf.parse( (o.get("date").toString()) ));
			for(int j=-1; j<22; j++){
				try{
					w=new WeatherHist();
					//o=((JSONObject)h.get(j)).getJSONObject("condition");
					
					o1=o.getJSONObject("day");
					o2=o1.getJSONObject("condition");	
					//System.out.println(o1);
					
					
					//w.setFtime(sf.parse(((JSONObject)h.get(j)).get("time").toString()));
					
					c.add(Calendar.HOUR_OF_DAY, 1);
					w.setFtime(c.getTime());					
					w.setTemp_c(Float.parseFloat((o1.get("avgtemp_c").toString())));					
					//w.setFeelslike_c(Float.parseFloat(((JSONObject)h.get(j)).get("feelslike_c").toString()));
					w.setFeelslike_c(Float.parseFloat((o1.get("avgtemp_c").toString())));
					
					//w.setWind_dir(((JSONObject)h.get(j)).get("wind_dir").toString());					
					//w.setWind_kph(Float.parseFloat(((JSONObject)h.get(j)).get("wind_kph").toString()));					
					//w.setHumidity(Integer.parseInt(((JSONObject)h.get(j)).get("humidity").toString()));
					//w.setCloud(Integer.parseInt(((JSONObject)h.get(j)).get("cloud").toString()));
					//w.setChance_of_rain(Integer.parseInt(((JSONObject)h.get(j)).get("chance_of_rain").toString()));
					//w.setPrecip_mm(Float.parseFloat(((JSONObject)h.get(j)).get("precip_mm").toString()));
					//o=o.getJSONObject("condition");					
					icon=o2.get("icon").toString();
					w.setCode(o2.get("code").toString());
					w.setText(o2.get("text").toString());
					w.setIcon(icon.substring(icon.indexOf("64x64/")+6, icon.lastIndexOf("."))+".png");
					
					if(now.getTime()<w.getFtime().getTime())
					df.update(w);
				}catch(Exception e){
					e.printStackTrace();
				}				
			}
		}	
		
		o=new JSONObject(sb.toString());
		o=o.getJSONObject("current");
		w=new WeatherHist();
		StringBuilder last_updated=new StringBuilder(o.get("last_updated").toString());
		last_updated.delete(last_updated.length()-2, last_updated.length());
		last_updated.append("00");
		
		w.setTemp_c(Float.parseFloat(o.get("temp_c").toString()));
		w.setFtime(sf1.parse(last_updated.toString()));
		w.setFeelslike_c(Float.parseFloat(o.get("feelslike_c").toString()));
		w.setWind_dir(o.get("wind_dir").toString());
		w.setWind_kph(Float.parseFloat(o.get("wind_kph").toString()));
		w.setHumidity(Integer.parseInt(o.get("humidity").toString()));
		w.setCloud(Integer.parseInt(o.get("cloud").toString()));
		w.setPrecip_mm(Float.parseFloat(o.get("precip_mm").toString()));		
		
		o=o.getJSONObject("condition");
		icon=o.get("icon").toString();				
		w.setCode(o.get("code").toString());
		w.setText(o.get("text").toString());
		w.setIcon(icon.substring(icon.indexOf("64x64/")+6, icon.lastIndexOf("."))+".png");
		df.update(w);		
    }
}
