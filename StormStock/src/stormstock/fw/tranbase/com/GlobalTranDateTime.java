package stormstock.fw.tranbase.com;

public class GlobalTranDateTime {
	public static String getTranDate()
	{
		return s_date;
	}
	public static String getTranTime()
	{
		return s_time;
	}
	public static void setTranDateTime(String date, String time)
	{
		s_date = date;
		s_time = time;
	}
	private static String s_date = "2008-01-01";
	private static String s_time = "00:00:00";
}
