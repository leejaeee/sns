package etc;

public class AIUtil {
	public static String propertyPath= "";
	
	public static double percentageCal(int nowCount, int total){
    	double percentage = (Math.round(((double)nowCount/(double)total)*1000)/1000.0)*100;
    	String temp = String.valueOf(percentage);
    	if(temp.length()>8){
    		String[] tempArray = temp.split("[.]");
    		String temp2 = tempArray[1].substring(0,2);
    		if("00".equals(temp2)) temp2 = "0";
    		temp = tempArray[0] + "." + temp2;
    		percentage = Double.parseDouble(temp);
    	}
    	return percentage;
    }
}
