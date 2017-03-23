package stormstock.fw.tranbase.stockdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import stormstock.fw.base.BLog;
import stormstock.fw.base.BUtilsDateTime;
import stormstock.fw.tranbase.account.AccountAccessor;
import stormstock.ori.stockdata.DataEngine;
import stormstock.ori.stockdata.DataEngineBase;
import stormstock.ori.stockdata.DataEngineBase.ResultStockBaseData;
import stormstock.ori.stockdata.DataEngineBase.ResultUpdateStock;
import stormstock.ori.stockdata.DataEngineBase.ResultUpdatedStocksDate;
import stormstock.ori.stockdata.DataWebStockAllList.ResultAllStockList;
import stormstock.ori.stockdata.DataWebStockDayK.ResultDayKData;
import stormstock.ori.stockdata.DataWebStockRealTimeInfo;
import stormstock.ori.stockdata.DataWebStockRealTimeInfo.ResultRealTimeInfo;
import stormstock.ori.stockdata.DataEngine.ExKData;
import stormstock.ori.stockdata.DataEngine.ResultMinKDataOneDay;
import stormstock.ori.stockdata.CommonDef.*;

/*
 * ע�⣺ȡ�õ����ݾ�Ϊǰ��Ȩ�۸�
 */

public class StockDataIF {
	
	public StockDataIF()
	{
		m_bDataFitting = false;
	}
	
	/*
	 * property - value
	 * 
	 * "DATA_HISTORY_DAYTIME_DATAFITTING" //��ʷ����������Ͽ���
	 *     true
	 *     false
	 */
	public int config(String property, Object value)
	{
		if(property.equals("DATA_HISTORY_DAYTIME_DATAFITTING"))
		{
			boolean bDataFitting = (boolean)value;
			m_bDataFitting = bDataFitting;
		}
		return 0;
	}
	
	/*
	 * ���ĳ����ʱ��Ĺ�Ʊ���ݷ�����
	 * ���Ի�ȡ��Ʊ������Ϣ
	 */
	public StockDataAccessor getStockDataAccessor(String date, String time)
	{
		return new StockDataAccessor(date, time, this);
	}
	
	/*
	 * �������й�Ʊ����
	 * s_localLatestDateΪ���������������ڣ������ڴ��ڵ���Ҫ������ʱ����������
	 * �������ݸ���ִ�к����л������
	 */
	public boolean updateAllLocalStocks(String dateStr)
	{
		if(null == m_localLatestDate)
		{
			BLog.output("STOCKDATA","DataEngine.getUpdatedStocksDate\n");
			ResultUpdatedStocksDate cResultUpdatedStocksDate = DataEngine.getUpdatedStocksDate();
			if(0 == cResultUpdatedStocksDate.error)
			{
				m_localLatestDate = cResultUpdatedStocksDate.date;
			}
			else
			{
				m_localLatestDate = "0000-00-00";
				BLog.output("STOCKDATA", "DataEngine.getUpdatedStocksDate failed, reset to %s \n", m_localLatestDate);
			}
		}
		
		if(m_localLatestDate.compareTo(dateStr) >= 0)
		{
			BLog.output("STOCKDATA", "update success! (current is newest, local: %s)\n", m_localLatestDate);
		}
		else
		{
			int iUpdateCnt = DataEngine.updateAllLocalStocks(dateStr);
			BLog.output("STOCKDATA", "update success to date: %s (count: %d)\n", m_localLatestDate, iUpdateCnt);
			clearAllCache();
		}
		
		return true;
	}
	/*
	 * ���µ�ֻ��Ʊ����
	 * �˷������ı�s_localLatestDate
	 */
	public boolean updateLocalStocks(String stockID, String dateStr)
	{
		if(null == m_localLatestDate)
		{
			BLog.output("STOCKDATA","DataEngine.getUpdatedStocksDate\n");
			ResultUpdatedStocksDate cResultUpdatedStocksDate = DataEngine.getUpdatedStocksDate();
			if(0 == cResultUpdatedStocksDate.error)
			{
				m_localLatestDate = cResultUpdatedStocksDate.date;
			}
			else
			{
				BLog.output("STOCKDATA", "DataEngine.getUpdatedStocksDate failed! \n", m_localLatestDate);
			}
		}
		
		if(m_localLatestDate.compareTo(dateStr) >= 0)
		{
			BLog.output("STOCKDATA", "update %s success! (current is newest, local: %s)\n",stockID, m_localLatestDate);
		}
		else
		{
			// ���µ�ֻ��Ʊ���� ��Ӱ��s_localLatestDate
			
			ResultUpdateStock cResultUpdateStock = DataEngine.updateStock(stockID);
			
			if(0 == cResultUpdateStock.error)
			{
				BLog.output("STOCKDATA", "update %s success to date: %s (count: %d)\n", stockID, cResultUpdateStock.updateCnt);
			}
			else
			{
				BLog.error("STOCKDATA", "update %s failed \n", cResultUpdateStock.error);
			}
		}
		return true;
	}
	
	/*
	 * ��ȡ���й�ƱId�б�
	 * ���ڵ�ǰ�������ݻ�ȡ������֤�����£����������ݸ��£�
	 * �˽ӿڴ������ݻ������
	 */
	public static class ResultAllStockID
	{
		public ResultAllStockID()
		{
			error = -1000;
			resultList = new ArrayList<String>();
		}
		public int error;
		public List<String> resultList;
	}
	public ResultAllStockID getAllStockID()
	{
		ResultAllStockID cResultAllStockID = new ResultAllStockID();
		
		if(null != m_cache_allStockID)
		{
			cResultAllStockID.error = 0;
			cResultAllStockID.resultList = m_cache_allStockID;
		}
		else
		{
			m_cache_allStockID = new ArrayList<String>();
			
			ResultAllStockList cResultAllStockList = DataEngine.getLocalAllStock();
			if(0 == cResultAllStockList.error)
			{
				for(int i=0; i<cResultAllStockList.resultList.size();i++)
				{
					String stockId = cResultAllStockList.resultList.get(i).id;
					m_cache_allStockID.add(stockId);
				}
				cResultAllStockID.error = 0;
				cResultAllStockID.resultList = m_cache_allStockID;
			}
			else
			{
				BLog.error("STOCKDATA", "DataEngine.getLocalAllStock error(%d) \n", cResultAllStockList.error);
				cResultAllStockID.error = -1;
			}
		}
		
		return cResultAllStockID;
	}

	/*
	 * ��ȡĳֻ��Ʊ������Ϣ
	 * ����֤�����£����������ݸ��£�
	 * �˽ӿڴ������ݻ������
	 */
	public static class ResultLatestStockInfo
	{
		public ResultLatestStockInfo()
		{
			error = -1000;
			stockInfo = new StockInfo();
		}
		public int error;
		public StockInfo stockInfo;
	}
	public ResultLatestStockInfo getLatestStockInfo(String id)
	{
		ResultLatestStockInfo cResultLatestStockInfo = new ResultLatestStockInfo();
		
		// �״ν��л���
		if(null == m_cache_latestStockInfo || !m_cache_latestStockInfo.containsKey(id))
		{
			if(null == m_cache_latestStockInfo)
			{
				m_cache_latestStockInfo = new HashMap<String,StockInfo>();
			}
			
			StockInfo cStockInfo = new StockInfo();
			cStockInfo.ID = id;
			
			ResultStockBaseData cResultStockBaseData = DataEngine.getBaseInfo(id);
			
			if(0 == cResultStockBaseData.error)
			{
				cStockInfo.name = cResultStockBaseData.stockBaseInfo.name;
				cStockInfo.allMarketValue = cResultStockBaseData.stockBaseInfo.allMarketValue; 
				cStockInfo.circulatedMarketValue = cResultStockBaseData.stockBaseInfo.circulatedMarketValue; 
				cStockInfo.peRatio = cResultStockBaseData.stockBaseInfo.peRatio;
				
				m_cache_latestStockInfo.put(id, cStockInfo);
			}
			else
			{
				//BLog.error("STOCKDATA", "DataEngine.getBaseInfo error(%d) \n", cResultStockBaseData.error);
			}
		}
			
		// �ӻ�����ȡ����
		if(null != m_cache_latestStockInfo && m_cache_latestStockInfo.containsKey(id))
		{
			cResultLatestStockInfo.error = 0;
			cResultLatestStockInfo.stockInfo = m_cache_latestStockInfo.get(id);
		}
		else
		{
			cResultLatestStockInfo.error = -1;
		}
		
		return cResultLatestStockInfo;
	}
	
	/*
	 * ��ȡĳֻ��Ʊ����ʷ��K����
	 * ����֤�����£����������ݸ��£�
	 * �˽ӿڴ������ݻ������
	 */
	public static class ResultHistoryData {
		public ResultHistoryData()
		{
			error = -1000;
			resultList = new ArrayList<StockDay>();
		}
		public int error;
		public List<StockDay> resultList;
	}
	public ResultHistoryData getHistoryData(String stockID, String fromDate, String endDate)
	{
		ResultHistoryData cResultHistoryData = new ResultHistoryData();
		
		// �״ν�����ʷ���ݻ���
		if(null == s_cache_stockDayData || !s_cache_stockDayData.containsKey(stockID))
		{
			if(null == s_cache_stockDayData)
			{
				s_cache_stockDayData = new HashMap<String,List<StockDay>>();
			}
			
			List<StockDay> historyData = new ArrayList<StockDay>();
			
			ResultDayKData cResultDayKData = DataEngine.getDayKDataQianFuQuan(stockID);
			
			if(0 == cResultDayKData.error && cResultDayKData.resultList.size() != 0)
			{
				for(int i = 0; i < cResultDayKData.resultList.size(); i++)  
		        {  
					DayKData cDayKData = cResultDayKData.resultList.get(i);  
	
					StockDay cStockDay = new StockDay();
					cStockDay.set(cDayKData.date, 
							cDayKData.open, cDayKData.close, cDayKData.low, cDayKData.high, cDayKData.volume);
					//System.out.println("historyData.add " + cDayKData.date + "," + cDayKData.open + "," + cDayKData.close); 
					historyData.add(cStockDay);
		        } 
				s_cache_stockDayData.put(stockID, historyData);
			}
			else
			{
				BLog.error("STOCKDATA", "DataEngine.getDayKDataQianFuQuan(%s %s %s) error(%d) \n", 
						stockID, fromDate, endDate, cResultDayKData.error);
			}
			
//			BLog.output("TEST", "DataEngine getDayKDataQianFuQuan(%d)\n", retList.size());
//			BLog.output("TEST", "getHistoryData return! historyData(%d)\n", historyData.size());

		}
		
		// �ӻ�����ȡ����
		if(null != s_cache_stockDayData && s_cache_stockDayData.containsKey(stockID))
		{
			List<StockDay> cacheList = s_cache_stockDayData.get(stockID);
			cResultHistoryData.error = 0;
			cResultHistoryData.resultList = StockUtils.subStockDayData(cacheList, fromDate, endDate);
		}
		else
		{
			cResultHistoryData.error = -1;
		}
		
		return cResultHistoryData;
	}
	
	public ResultHistoryData getHistoryData(String id, String endDate)
	{
		return getHistoryData(id, "2000-01-01", endDate);
	}
	public ResultHistoryData getHistoryData(String id)
	{
		return getHistoryData(id, "2000-01-01", "2100-01-01");
	}

	/*
	 * ��ȡĳֻ��Ʊĳ��ĳʱ���ϸ������
	 * ����֤�����£����������ݸ��£�
	 * �˽ӿڴ������ݻ������
	 */
	public static class ResultDayDetail
	{
		public ResultDayDetail()
		{
			error = -1000;
			resultList = new ArrayList<StockTime>();
		}
		public int error;
		public List<StockTime> resultList;
	}
	public ResultDayDetail getDayDetail(String id, String date, String beginTime, String endTime)
	{
		ResultDayDetail cResultDayDetail = new ResultDayDetail();
		
		// �״ν�����ʷ���ݻ���
		String findKey = id + "_" + date;
		if(null == s_cache_stockTimeData || !s_cache_stockTimeData.containsKey(findKey))
		{
			if(null == s_cache_stockTimeData)
			{
				s_cache_stockTimeData = new HashMap<String,List<StockTime>>();
			}
			
			List<StockTime> detailDataList = new ArrayList<StockTime>();
			
			ResultHistoryData cResultHistoryData = getHistoryData(id, date, date);
			if(0 == cResultHistoryData.error && cResultHistoryData.resultList.size()==1)
			{
				StockDay cStockDay = cResultHistoryData.resultList.get(0);
				
				if(null != cStockDay && date.length() == "0000-00-00".length())
				{
					// load new detail data
					ResultMinKDataOneDay cResultMinKDataOneDay = DataEngine.get1MinKDataOneDay(id, date);
					
					if(0 == cResultMinKDataOneDay.error && cResultMinKDataOneDay.exKDataList.size() != 0)
					{
						// ���ڿ����Ǹ�Ȩ��λ����Ҫ���¼�����Լ۸�
						float baseOpenPrice = cStockDay.open();
			            //System.out.println("baseOpenPrice:" + baseOpenPrice);  
			            
						float actruaFirstPrice = cResultMinKDataOneDay.exKDataList.get(0).open;
						//System.out.println("actruaFirstPrice:" + actruaFirstPrice); 
						
						for(int i = 0; i < cResultMinKDataOneDay.exKDataList.size(); i++)  
				        {  
							ExKData cExKData = cResultMinKDataOneDay.exKDataList.get(i);  
//				            System.out.println(cExKData.datetime + "," 
//				            		+ cExKData.open + "," + cExKData.close + "," 
//				            		+ cExKData.low + "," + cExKData.high + "," 
//				            		+ cExKData.volume);  
							
							float actrualprice = cExKData.close;
							float changeper = (actrualprice - actruaFirstPrice)/actruaFirstPrice;
							float changedprice = baseOpenPrice + baseOpenPrice * changeper;
							
							// ��������翪�̵�
							if(cExKData.getTime().compareTo("09:31:00") == 0
									|| cExKData.getTime().compareTo("13:01:00") == 0)
							{
								float actrualprice_open = cExKData.open;
								float changeper_open = (actrualprice_open - actruaFirstPrice)/actruaFirstPrice;
								float changedprice_open = baseOpenPrice + baseOpenPrice * changeper_open;
								
								StockTime cStockDayDetail = new StockTime();
								cStockDayDetail.price = changedprice_open;
								String openTime = BUtilsDateTime.getTimeStrForSpecifiedTimeOffsetM(cExKData.getTime(), -1);
								cStockDayDetail.time = openTime;
								detailDataList.add(cStockDayDetail);
							}
							

							StockTime cStockDayDetail = new StockTime();
							cStockDayDetail.price = changedprice;
							cStockDayDetail.time = cExKData.getTime();
							detailDataList.add(cStockDayDetail);
				        } 
						
						s_cache_stockTimeData.put(findKey, detailDataList);
					}
				}
			}
		}
		
		// �ӻ�����ȡ����
		if(null != s_cache_stockTimeData && s_cache_stockTimeData.containsKey(findKey))
		{
			List<StockTime> cacheList = s_cache_stockTimeData.get(findKey);
			cResultDayDetail.error = 0;
			cResultDayDetail.resultList = StockUtils.subStockTimeData(cacheList, beginTime, endTime);
		}
		else
		{
			cResultDayDetail.error = -1;
		}
		
		return cResultDayDetail;
	}
	
	/*
	 * ��ȡĳֻ��Ʊĳ��ĳʱ��ļ۸�
	 * ���ݻ������������getDayDetail�ӿ�
	 */
	public static class ResultStockTime
	{
		public ResultStockTime()
		{
			error = -1000;
		}
		public StockTime stockTime()
		{
			StockTime cStockTime = new StockTime();
			cStockTime.time = time;
			cStockTime.price = price;
			return cStockTime;
		}
		public int error;
		public String date;
		public String time;
		public float price;
	}
	public ResultStockTime getStockTime(String id, String date, String time)
	{
		ResultStockTime cResultStockTime = new ResultStockTime();
		
		boolean bRealTime = false;
		String curDate = BUtilsDateTime.GetCurDateStr();
		String curTime = "";
		if(date.compareTo(curDate) == 0)
		{
			curTime = BUtilsDateTime.GetCurTimeStr();
			if(Math.abs(BUtilsDateTime.subTime(time,curTime)) < 10) // �뵱ǰʱ��10����
			{
				bRealTime = true;
			}
		}
		
		if(bRealTime)
		{
			ResultRealTimeInfo cResultRealTimeInfo = DataWebStockRealTimeInfo.getRealTimeInfo(id);
		
			if(0 == cResultRealTimeInfo.error)
			{
				cResultStockTime.error = 0;
				cResultStockTime.date = cResultRealTimeInfo.realTimeInfo.date;
				cResultStockTime.time = cResultRealTimeInfo.realTimeInfo.time;
				cResultStockTime.price = cResultRealTimeInfo.realTimeInfo.curPrice;
				if(0 == Float.compare(cResultStockTime.price, 0.00f))
				{
					cResultStockTime.error = -8;
					BLog.error("STOCKDATA", "getStockTime %s price 0.00f!", id); // ����ȡ��ʵʱ�۸�Ϊ0����Ϊ����
				}
				return cResultStockTime;
			}
		}
		else
		{
			boolean bDataFitting = m_bDataFitting;
			
			if(bDataFitting)
			{
				// ������K�ߵ��������
				// 9��30��������֮ǰ��Ϊǰһ�����̼۸�
				// 09:30:00 - 13:00:00�������� ��Ϊ���翪�̼۸�
				// 13:00:00 - 24:00:00 ��������Ϊ�������̼۸�
				if(time.compareTo("09:30:00") >= 0)
				{
					ResultHistoryData cResultHistoryData = getHistoryData(id, date, date);
					List<StockDay> cStockDayList = cResultHistoryData.resultList;
					if(cStockDayList.size() > 0)
					{
						StockDay cStockDay = cStockDayList.get(0);
						float open = cStockDay.open();
						float close = cStockDay.close();
						cResultStockTime.error = 0;
						cResultStockTime.date = cStockDay.date();
						if(time.compareTo("09:30:00") >= 0 && time.compareTo("11:30:00") <= 0)
						{
							cResultStockTime.time = time;
							cResultStockTime.price = open;
						}
						else if(time.compareTo("11:30:00") > 0 && time.compareTo("13:00:00") < 0)
						{
							cResultStockTime.time = "11:30:00";
							cResultStockTime.price = open;
						}
						else if(time.compareTo("13:00:00") >= 0 && time.compareTo("15:00:00") <= 0)
						{
							cResultStockTime.time = time;
							cResultStockTime.price = close;
						}
						else if(time.compareTo("15:00:00") > 0 && time.compareTo("24:00:00") <= 0)
						{
							cResultStockTime.time = "15:00:00";
							cResultStockTime.price = close;
						}
						return cResultStockTime;
					}
				}
				else
				{
					String beforeDate = BUtilsDateTime.getDateStrForSpecifiedDateOffsetD(date, -1);
					ResultHistoryData cResultHistoryData = getHistoryData(id, beforeDate, beforeDate);
					List<StockDay> cStockDayList = cResultHistoryData.resultList;
					if(cStockDayList.size() > 0)
					{
						StockDay cStockDay = cStockDayList.get(0);
						cResultStockTime.error = 0;
						cResultStockTime.date = cStockDay.date();
						cResultStockTime.time = "15:00:00";
						cResultStockTime.price = cStockDay.close();
						return cResultStockTime;
					}
				}
			}
			else
			{
				// ������ʵ����ʷ����
				// 9��30��������֮ǰ��Ϊǰһ�����̼۸�
				// 09:30:00 - 15:00:00 Ϊ�����ڼ���ʵǰ��Ȩ�۸�
				// 15:00:00֮��Ϊ�������̼۸�
				if(time.compareTo("09:30:00") >= 0)
				{
					if(time.compareTo("09:30:00") >= 0 && time.compareTo("15:00:00") <= 0)
					{
						// ����ʱ��
						ResultDayDetail cResultDayDetail = getDayDetail(id, date, "09:25:00", time);
						List<StockTime> cStockTimeList = cResultDayDetail.resultList;
						
						if(null!=cStockTimeList && cStockTimeList.size()>0)
						{
							StockTime cStockTime = cStockTimeList.get(cStockTimeList.size()-1);
							long subTimeMin = BUtilsDateTime.subTime(time, cStockTime.time);
							if(subTimeMin >=0 && subTimeMin<=120) // ��2��������
							{
								cResultStockTime.error = 0;
								cResultStockTime.date = date;
								cResultStockTime.time = cStockTime.time;
								cResultStockTime.price = cStockTime.price;
								return cResultStockTime;
							}
							else
							{
								cResultStockTime.error = -1;
								return cResultStockTime;
							}
						}
						else
						{
							cResultStockTime.error = -2;
							return cResultStockTime;
						}
					}
					else
					{
						ResultHistoryData cResultHistoryData = getHistoryData(id, date, date);
						List<StockDay> cStockDayList = cResultHistoryData.resultList;
						if(cStockDayList.size() > 0)
						{
							StockDay cStockDay = cStockDayList.get(0);
							cResultStockTime.error = 0;
							cResultStockTime.date = cStockDay.date();
							cResultStockTime.time = "15:00:00";
							cResultStockTime.price = cStockDay.close();
							return cResultStockTime;
						}
					}
				}
				else
				{
					String beforeDate = BUtilsDateTime.getDateStrForSpecifiedDateOffsetD(date, -1);
					ResultHistoryData cResultHistoryData = getHistoryData(id, beforeDate, beforeDate);
					List<StockDay> cStockDayList = cResultHistoryData.resultList;
					if(cStockDayList.size() > 0)
					{
						StockDay cStockDay = cStockDayList.get(0);
						cResultStockTime.error = 0;
						cResultStockTime.date = beforeDate;
						cResultStockTime.time = "15:00:00";
						cResultStockTime.price = cStockDay.close();
						return cResultStockTime;
					}
				}
			}
		}
		cResultStockTime.error = -3;
		return cResultStockTime;
	}
	
	// ******************************************************************************************
	
//	public Stock getStock(String id, String fromDate, String endDate)
//	{
//		boolean bEnableCache = false;
//		
//		// cache check
//		if(bEnableCache)
//		{
//			String endDateActual = endDate;
//			String fromDateActual = fromDate;
//			if(null == s_localLatestDate)
//			{
//				s_localLatestDate = DataEngine.getUpdatedStocksDate();
//			}
//			if(endDateActual.compareTo(s_localLatestDate) > 0)
//			{
//				endDateActual = s_localLatestDate;
//			}
//			if(fromDateActual.compareTo("2008-01-01") < 0)
//			{
//				fromDateActual = "2008-01-01";
//			}
//			if(s_stockCacheMap.containsKey(id))
//			{
//				Stock cStock = s_stockCacheMap.get(id);
//				if(fromDateActual.compareTo("2008-01-01")>=0 && 
//						endDateActual.compareTo(s_localLatestDate) <=0)
//				{
//					Stock cNewStock = cStock.subObj(fromDate, endDate);
//					return cNewStock;
//				}
//				else
//				{
//					s_stockCacheMap.remove(id);
//				}
//			}
//		}
//		
//		List<DayKData> retList = new ArrayList<DayKData>();
//		int ret = DataEngine.getDayKDataQianFuQuan(id, retList);
//		if(0 != ret || retList.size() == 0)
//		{
//			return null;
//		}
//			
//		Stock cStock = new Stock();
//		
//		StockInfo cStockInfo = getLatestStockInfo(id);
//		cStock.getCurLatestStockInfo().CopyFrom(cStockInfo);
//		
//		for(int i = 0; i < retList.size(); i++)  
//        {  
//			DayKData cDayKData = retList.get(i);  
//			if(cDayKData.date.compareTo(fromDate) >= 0
//					&& cDayKData.date.compareTo(endDate) <= 0)
//			{
//				StockDay cStockDay = new StockDay();
//				cStockDay.set(cDayKData.date, 
//						cDayKData.open, cDayKData.close, cDayKData.low, cDayKData.high, cDayKData.volume);
////		            System.out.println(cDayKData.date + "," 
////		            		+ cDayKData.open + "," + cDayKData.close); 
//				cStock.getCurStockDayData().add(cStockDay);
//			}
//        } 
//		
//		// cache
////		if(bEnableCache)
////		{
////			s_stockCacheMap.put(id, cStock);
////		}
//		
//		return cStock;
//	}

	
	// ***********************************************************************************
	
	public void clearAllCache()
	{
		m_localLatestDate = null;
		m_cache_allStockID = null;
		m_cache_latestStockInfo = null;
		s_cache_stockDayData = null;
		s_cache_stockTimeData = null;
	}
	
	// ��ǰ���������¸������ڻ���
	private String m_localLatestDate = null;
	// ���ع�Ʊ�б���
	private List<String> m_cache_allStockID = null;
	// ���ع�Ʊ���»�����Ϣ����
	private Map<String,StockInfo> m_cache_latestStockInfo = null;
	// ��K��ʷ���ݻ���
	// key:600001
	private Map<String,List<StockDay>> s_cache_stockDayData = null;
	// ���ڷ�ʱ����  
	// key:600001_2016-01-01
	private Map<String,List<StockTime>> s_cache_stockTimeData = null;
	
	//==========================================================================================
	// config
	private boolean m_bDataFitting;
}
