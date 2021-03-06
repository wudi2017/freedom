package stormstock.app.analysistest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import stormstock.app.analysistest.EStockComplexDZCZX.ResultDYCheck;
import stormstock.app.analysistest.EStockDayVolumeLevel.VOLUMELEVEL;
import stormstock.fw.base.BImageCurve;
import stormstock.fw.base.BLog;
import stormstock.fw.base.BThread;
import stormstock.fw.base.BImageCurve.CurvePoint;
import stormstock.fw.tranbase.com.IEigenStock;
import stormstock.fw.tranbase.stockdata.Stock;
import stormstock.fw.tranbase.stockdata.StockDataIF;
import stormstock.fw.tranbase.stockdata.StockDay;
import stormstock.fw.tranbase.stockdata.StockTime;
import stormstock.fw.tranbase.stockdata.StockUtils;
import stormstock.fw.tranbase.stockdata.StockDataIF.ResultAllStockID;
import stormstock.fw.tranbase.stockdata.StockDataIF.ResultDayDetail;
import stormstock.fw.tranbase.stockdata.StockDataIF.ResultHistoryData;

public class RunSingleTest {
	
	public static void testOneStock(String stockId)
	{
		ResultHistoryData cResultHistoryData = 
				s_cStockDataIF.getHistoryData(stockId, "2016-01-01", "2017-01-01");
		List<StockDay> list = cResultHistoryData.resultList;
		//BLog.output("TEST", "Check stockID(%s) list size(%d)\n", stockId, list.size());
		
		// 日检查
		for(int iDayCheck = 0; iDayCheck < list.size(); iDayCheck++)  
        {  
			StockDay cCurStockDay = list.get(iDayCheck);
			
			ResultDYCheck cResultDYCheck = EStockComplexDZCZX.checkZCZX(stockId, list, iDayCheck);
			if(cResultDYCheck.bCheck)
			{
				BLog.output("TEST", "Check stockID(%s) date(%s)\n", stockId, cCurStockDay.date());
			}
			
//			// 日细节检查
//			for(int iDayDetailCheck = iDayCheck+1; 
//					iDayDetailCheck<=iDayCheck+1 && iDayDetailCheck<list.size(); iDayDetailCheck++)
//			{
//				String dateDetail = list.get(iDayDetailCheck).date();
//				BLog.output("TEST", "    iDayDetailCheck %s\n", dateDetail);			
//				// 日内分时检查
//				ResultDayDetail cResultDayDetail = cStockDataIF.getDayDetail(stockID, dateDetail, "09:30:00", "15:00:00");
//				List<StockTime> listStockTime = cResultDayDetail.resultList;
//				s_StockTimeListCurve.clear();
//				s_StockTimeListCurve.setCurve(listStockTime);
//				for(int iStockTime = 0; iStockTime < listStockTime.size(); iStockTime++)  
//		        {  
//					StockTime cCurStockTime = listStockTime.get(iStockTime);
//					ResultXiaCuoQiWen cResultXiaCuoQiWen = cEStockTimePriceDropStable.checkXiaCuoQiWen_single(listStockTime, iStockTime);
//					if (cResultXiaCuoQiWen.bCheck)
//					{
//						BLog.output("TEST", "    CheckPoint %s\n", cCurStockTime.time);
//						s_StockTimeListCurve.markCurveIndex(iStockTime, "x");
//						iStockTime=iStockTime+10;
//					}
//		        } 
//				s_StockTimeListCurve.generateImage();
//			}
			
        } 
	}
	
	public static void main(String[] args) {
		BLog.output("TEST", "Main Begin\n");
		
		ResultAllStockID allStock = s_cStockDataIF.getAllStockID();
		if(0 == allStock.error)
		{
			for(int i=0;i<allStock.resultList.size();i++)
			{
				String stockID = allStock.resultList.get(i);
				//if(stockID.equals("000151"))
					testOneStock(stockID);
			}
		}
		
		BLog.output("TEST", "Main End\n");
	}
	
	public static StockDataIF s_cStockDataIF = new StockDataIF();
	public static StockDayListCurve s_StockDayListCurve = new StockDayListCurve("RunSingleTest.jpg");
	public static StockTimeListCurve s_StockTimeListCurve = new StockTimeListCurve("RunSingleTestDetail.jpg");
}
