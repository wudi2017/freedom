package stormstock.app.analysistest;

import java.util.List;

import stormstock.app.analysistest.EStockTimePriceDropStable.ResultXiaCuoQiWen;
import stormstock.fw.base.BLog;
import stormstock.fw.base.BThread;
import stormstock.fw.tranbase.stockdata.StockDataIF;
import stormstock.fw.tranbase.stockdata.StockDay;
import stormstock.fw.tranbase.stockdata.StockTime;
import stormstock.fw.tranbase.stockdata.StockUtils;
import stormstock.fw.tranbase.stockdata.StockDataIF.ResultDayDetail;
import stormstock.fw.tranbase.stockdata.StockDataIF.ResultHistoryData;

public class EStockTimePriceHighRun {
	
	public static boolean checkHighRun(String StockId, String date, List<StockTime> list, int iCheck)
	{
		return false;
	}
	
	/*
	 * ********************************************************************
	 * Test
	 * ********************************************************************
	 */
	public static void main(String[] args) {
		BLog.output("TEST", "Main Begin\n");
		StockDataIF cStockDataIF = new StockDataIF();
		
		String stockID = "300217"; // 300163 300165 000401 600439
		String date = "2016-09-06";
		ResultHistoryData cResultHistoryData = 
				cStockDataIF.getHistoryData(stockID, "2016-09-01", "2017-03-01");
		List<StockDay> listDay = cResultHistoryData.resultList;
		BLog.output("TEST", "Check stockID(%s) list size(%d)\n", stockID, listDay.size());
		
		for(int iDayCheck = 0; iDayCheck < listDay.size(); iDayCheck++)  
        {  
			StockDay cCurStockDay = listDay.get(iDayCheck);
		
			if(cCurStockDay.date().equals(date))
			{
				
				/// 查看固定某日
				ResultDayDetail cResultDayDetail = cStockDataIF.getDayDetail(stockID, date, "09:30:00", "15:00:00");
				List<StockTime> listTime = cResultDayDetail.resultList;
				
				
				
				s_StockTimeListCurve.setCurve(listTime);
				//////////////////////////////////////////////////////////////////////
				
				
				for(int i = 0; i < listTime.size(); i++)  
		        {
					StockTime cCurStockTime = listTime.get(i);
					if(cCurStockTime.time.equals("15:00:00"))
					{
						BThread.sleep(1);
						
						boolean bCheck = EStockTimePriceHighRun.checkHighRun(stockID, date, listTime, i);
						if(bCheck)
						{
							s_StockTimeListCurve.markCurveIndex(i, "x");
						}
						
					}
					
		        }
				
	
				//////////////////////////////////////////////////////////////////////
				s_StockTimeListCurve.generateImage();
			}
        }

		
		BLog.output("TEST", "Main End\n");
	}
	
	public static StockTimeListCurve s_StockTimeListCurve = new StockTimeListCurve("EStockTimePriceHighRun.jpg");
}
