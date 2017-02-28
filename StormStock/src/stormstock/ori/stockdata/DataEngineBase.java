package stormstock.ori.stockdata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.List;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;

import stormstock.ori.stockdata.DataWebStockDayDetail.ResultDayDetail;
import stormstock.ori.stockdata.DataWebStockDayK.ResultDayKData;
import stormstock.ori.stockdata.DataWebStockDividendPayout.ResultDividendPayout;
import stormstock.ori.stockdata.DataWebStockRealTimeInfo.ResultRealTimeInfo;
import stormstock.ori.stockdata.DataWebStockRealTimeInfo.ResultRealTimeInfoMore;

import stormstock.ori.stockdata.CommonDef.*;

public class DataEngineBase {
	
	/*
	 * ��ȡĳֻ��Ʊ����K����
	 * ֻ�ӱ��ػ�ȡ
	 */
	public static ResultDayKData getDayKData(String id)
	{
		ResultDayKData cResultDayKData = new ResultDayKData();
		
		String stockDayKFileName = s_DataDir + "/" + id + "/" + s_daykFile;
		File cfile=new File(stockDayKFileName);
//		if(!cfile.exists())
//		{
//			int iDownload = downloadStockDayk(id);
//			if(0 != iDownload)
//			{
//				cResultDayKData.error = -21;
//				return cResultDayKData;
//			}
//		}
		if(!cfile.exists())
		{
			cResultDayKData.error = -10;
			return cResultDayKData;
		}
		
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(cfile));
			int line = 1;
			String tempString = null;
            while ((tempString = reader.readLine()) != null) {
            	
//                System.out.println("line " + line + ": " + tempString);
//                if(tempString.contains("2017-01-05"))
//                {
//                	System.out.println("line " + line + ": " + tempString);
//                }
                
            	DayKData cDayKData = new DayKData();
            	String[] cols = tempString.split(",");
            	
            	cDayKData.date = cols[0];
	        	cDayKData.open = Float.parseFloat(cols[1]);
	        	cDayKData.close = Float.parseFloat(cols[2]);
	        	cDayKData.low = Float.parseFloat(cols[3]);
	        	cDayKData.high = Float.parseFloat(cols[4]);
	        	cDayKData.volume = Float.parseFloat(cols[5]);
	        	cResultDayKData.resultList.add(cDayKData);
	        	
                line++;
            }
            reader.close();
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage()); 
			cResultDayKData.error = -1;
			return cResultDayKData;
		}
		return cResultDayKData;
	}
	public static int saveDayKData(String id, List<DayKData> in_list)
	{
		String stockDayKFileName = s_DataDir + "/" + id + "/" + s_daykFile;
		File cfile=new File(stockDayKFileName);
		try
		{
			FileOutputStream cOutputStream = new FileOutputStream(cfile);
			for(int i = 0; i < in_list.size(); i++)  
	        {  
				DayKData cDayKData = in_list.get(i);  
//		            System.out.println(cDayKData.date + "," 
//		            		+ cDayKData.open + "," + cDayKData.close);  
	            cOutputStream.write((cDayKData.date + ",").getBytes());
	            cOutputStream.write((cDayKData.open + ",").getBytes());
	            cOutputStream.write((cDayKData.close + ",").getBytes());
	            cOutputStream.write((cDayKData.low + ",").getBytes());
	            cOutputStream.write((cDayKData.high + ",").getBytes());
	            cOutputStream.write((cDayKData.volume + "\n").getBytes());
	        } 
			cOutputStream.close();
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage()); 
			return -1;
		}
		return 0;
	}
	
	/*
	 * ��ȡĳֻ��Ʊ�ķֺ���Ϣ����
	 * ֻ�ӱ��ػ�ȡ
	 */
	public static ResultDividendPayout getDividendPayout(String id)
	{
		ResultDividendPayout cResultDividendPayout = new ResultDividendPayout();
		
		String stockDividendPayoutFileName = s_DataDir + "/" + id + "/" + s_DividendPayoutFile;
		File cfile=new File(stockDividendPayoutFileName);
//		if(!cfile.exists())
//		{
//			int iDownLoad = downloadStockDividendPayout(id);
//			if(0 != iDownLoad)
//			{
//				cResultDividendPayout.error = -21;
//				return cResultDividendPayout;
//			}
//		}
		if(!cfile.exists()) 
		{
			cResultDividendPayout.error = -10;
			return cResultDividendPayout;
		}
		
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(cfile));
			int line = 1;
			String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                //System.out.println("line " + line + ": " + tempString);
            	String[] cols = tempString.split(",");
            	
            	DividendPayout cDividendPayout = new DividendPayout();
            	cDividendPayout.date = cols[0];
                cDividendPayout.songGu = Float.parseFloat(cols[1]);
                cDividendPayout.zhuanGu = Float.parseFloat(cols[2]);
                cDividendPayout.paiXi = Float.parseFloat(cols[3]);
                cResultDividendPayout.resultList.add(cDividendPayout);
                
                line++;
            }
            reader.close();
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage()); 
			cResultDividendPayout.error = -1;
			return cResultDividendPayout;
		}
		return cResultDividendPayout;
	}
	
	/*
	 * ��ȡĳ��Ʊ���ڽ�����ϸ
	 * ������������ݴӱ��ػ�ȡ��������������غ��ٴӱ��ػ�ȡ
	 */
	public static ResultDayDetail getDayDetail(String id, String date)
	{
		ResultDayDetail cResultDayDetail = new ResultDayDetail();
				
		String stockDataDetailFileName = s_DataDir + "/" + id + "/" + date + ".txt";
		File cfile=new File(stockDataDetailFileName);
		if(!cfile.exists())
		{
			int iDownload = downloadStockDataDetail(id, date);
			if(0 != iDownload)
			{
				cResultDayDetail.error = -21;
				return cResultDayDetail;
			}
		}
		if(!cfile.exists()) 
		{
			cResultDayDetail.error = -10;
			return cResultDayDetail;
		}
		
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(cfile));
			int line = 1;
			String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                // System.out.println("line " + line + ": " + tempString);
            	DayKData cDayKData = new DayKData();
            	String[] cols = tempString.split(",");

            	DayDetailItem cDayDetailItem = new DayDetailItem();
	        	cDayDetailItem.time = cols[0];
	        	cDayDetailItem.price = Float.parseFloat(cols[1]);
	        	cDayDetailItem.volume = Float.parseFloat(cols[2]);
	        	
	        	cResultDayDetail.resultList.add(cDayDetailItem);
	        	
                line++;
            }
            reader.close();
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage()); 
			cResultDayDetail.error = -1;
			return cResultDayDetail;
		}
		return cResultDayDetail;
	}
	public static int downloadStockDayk(String id)
	{
		if(0 != mkStocDataDir(id)) return -10;
		String stockDayKFileName = s_DataDir + "/" + id + "/" + s_daykFile;
		
		ResultRealTimeInfo cResultRealTimeInfo = DataWebStockRealTimeInfo.getRealTimeInfo(id);
		if(0 != cResultRealTimeInfo.error) return -20;
		String curAvailidDate = cResultRealTimeInfo.realTimeInfo.date;
		String curAvailidTime = cResultRealTimeInfo.realTimeInfo.time;
		
		File cfile =new File(stockDayKFileName);
		//System.out.println("updateStocData_Dayk:" + id);
		String paramToDate = curAvailidDate.replace("-", "");
		ResultDayKData cResultDayKData = DataWebStockDayK.getDayKData(id, "20080101", paramToDate);
		if(0 == cResultDayKData.error)
		{
			try
			{
				FileOutputStream cOutputStream = new FileOutputStream(cfile);
				for(int i = 0; i < cResultDayKData.resultList.size(); i++)  
		        {  
					DayKData cDayKData = cResultDayKData.resultList.get(i);  
//		            System.out.println(cDayKData.date + "," 
//		            		+ cDayKData.open + "," + cDayKData.close);  
		            cOutputStream.write((cDayKData.date + ",").getBytes());
		            cOutputStream.write((cDayKData.open + ",").getBytes());
		            cOutputStream.write((cDayKData.close + ",").getBytes());
		            cOutputStream.write((cDayKData.low + ",").getBytes());
		            cOutputStream.write((cDayKData.high + ",").getBytes());
		            cOutputStream.write((cDayKData.volume + "\n").getBytes());
		        } 
				cOutputStream.close();
			}
			catch(Exception e)
			{
				System.out.println(e.getMessage()); 
				return -1;
			}
		}
		else
		{
			System.out.println("ERROR:" + cResultDayKData.error);
		}
		return 0;
	}
	public static int downloadBaseInfo(String id)
	{
		if(0 != mkStocDataDir(id)) return -10;
		String stockBaseInfoFileName = s_DataDir + "/" + id + "/" + s_BaseInfoFile;
		File cfile =new File(stockBaseInfoFileName);
		// System.out.println("saveStockBaseData:" + id);
		try
		{
			ResultRealTimeInfoMore cResultRealTimeInfoMore = DataWebStockRealTimeInfo.getRealTimeInfoMore(id);
			
			if(0 == cResultRealTimeInfoMore.error)
			{
				FileOutputStream cOutputStream = new FileOutputStream(cfile);
				String s = String.format("%s,%.3f,%.3f,%.3f,%.3f", 
						cResultRealTimeInfoMore.realTimeInfoMore.name, 
						cResultRealTimeInfoMore.realTimeInfoMore.curPrice, 
						cResultRealTimeInfoMore.realTimeInfoMore.allMarketValue, 
						cResultRealTimeInfoMore.realTimeInfoMore.circulatedMarketValue, 
						cResultRealTimeInfoMore.realTimeInfoMore.peRatio);
				cOutputStream.write(s.getBytes());
				cOutputStream.close();
			}
			else
			{
				return -20;
			}
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage()); 
			return -1;
		}
		return 0;
	}
	public static int downloadStockDividendPayout(String id)
	{
		if(0 != mkStocDataDir(id)) return -10;
		String stockDividendPayoutFileName = s_DataDir + "/" + id + "/" + s_DividendPayoutFile;
		
		ResultRealTimeInfo cResultRealTimeInfo = DataWebStockRealTimeInfo.getRealTimeInfo(id);
		if(0 != cResultRealTimeInfo.error) return -20;
		String curAvailidDate = cResultRealTimeInfo.realTimeInfo.date;
		String curAvailidTime = cResultRealTimeInfo.realTimeInfo.time;
		
		File cfile =new File(stockDividendPayoutFileName);
		// System.out.println("updateStocData_DividendPayout:" + id);
		ResultDividendPayout cResultDividendPayout = DataWebStockDividendPayout.getDividendPayout(id);
		if(0 == cResultDividendPayout.error)
		{
			try
			{
				FileOutputStream cOutputStream = new FileOutputStream(cfile);
				for(int i = 0; i < cResultDividendPayout.resultList.size(); i++)  
		        {  
					DividendPayout cDividendPayout = cResultDividendPayout.resultList.get(i);
					// System.out.println(cDividendPayout.date); 
					cOutputStream.write((cDividendPayout.date + ",").getBytes());
					cOutputStream.write((cDividendPayout.songGu + ",").getBytes());
					cOutputStream.write((cDividendPayout.zhuanGu + ",").getBytes());
					cOutputStream.write((cDividendPayout.paiXi + "\n").getBytes());
		        } 
				cOutputStream.close();
			}
			catch(Exception e)
			{
				System.out.println(e.getMessage()); 
				return -1;
			}
		}
		else
		{
			System.out.println("ERROR:" + cResultDividendPayout.error);
			return -10;
		}
		return 0;
	}
	
	/*
	 * ����ĳֻ��Ʊ���ڽ������ݵ�����
	 * ����0Ϊ�ɹ�
	 */
	public static int downloadStockDataDetail(String id, String date) {
		s_fmt.format("@downloadStockDataDetail stockID(%s) date(%s)\n",id,date);
		if(0 != mkStocDataDir(id)) return -20;
		String stockDataDetailFileName = s_DataDir + "/" + id + "/" + date + ".txt";
		
		ResultRealTimeInfo cResultRealTimeInfo = DataWebStockRealTimeInfo.getRealTimeInfo(id);
		if(0 != cResultRealTimeInfo.error) return -20;
		String curAvailidDate = cResultRealTimeInfo.realTimeInfo.date;
		String curAvailidTime = cResultRealTimeInfo.realTimeInfo.time;
		
		ResultDayDetail cResultDayDetail = DataWebStockDayDetail.getDayDetail(id, date);
		if(0 == cResultDayDetail.error)
		{
			try
			{
				File cfile =new File(stockDataDetailFileName);
				FileOutputStream cOutputStream = new FileOutputStream(cfile);
				for(int i = 0; i < cResultDayDetail.resultList.size(); i++)  
		        {  
					DayDetailItem cDayDetailItem = cResultDayDetail.resultList.get(i);  
//			            System.out.println(cDayDetailItem.time + "," 
//			            		+ cDayDetailItem.price + "," + cDayDetailItem.volume);  
					cOutputStream.write((cDayDetailItem.time + ",").getBytes());
					cOutputStream.write((cDayDetailItem.price + ",").getBytes());
					cOutputStream.write((cDayDetailItem.volume + "\n").getBytes());
		        } 
				cOutputStream.close();
			}
			catch(Exception e)
			{
				System.out.println(e.getMessage()); 
				return -1;
			}
		}
		else
		{
			System.out.println("ERROR:" + cResultDayDetail.error);
			return -30;
		}
		return 0;
	}
	public static int saveStockBaseData(String id, StockBaseInfo baseData) 
	{
		if(0 != mkStocDataDir(id)) return -10;
		String stockBaseInfoFileName = s_DataDir + "/" + id + "/" + s_BaseInfoFile;
		File cfile =new File(stockBaseInfoFileName);
		// System.out.println("saveStockBaseData:" + id);
		try
		{
			FileOutputStream cOutputStream = new FileOutputStream(cfile);
			String s = String.format("%s,%.3f,%.3f,%.3f,%.3f", 
					baseData.name, baseData.price, 
					baseData.allMarketValue, baseData.circulatedMarketValue, baseData.peRatio);
			cOutputStream.write(s.getBytes());
			cOutputStream.close();
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage()); 
			return -1;
		}
		return 0;
	}
	
	/*
	 * ��ȡĳֻ��Ʊ�Ļ�����Ϣ
	 * ֻ�ӱ��ػ�ȡ
	 */
	public static class ResultStockBaseData
	{
		public ResultStockBaseData()
		{
			error = 0;
			stockBaseInfo = new StockBaseInfo();
		}
		public int error;
		public StockBaseInfo stockBaseInfo;
	}
	public static ResultStockBaseData getBaseInfo(String id) 
	{
		ResultStockBaseData cResultStockBaseData = new ResultStockBaseData();
		
		String stockBaseInfoFileName = s_DataDir + "/" + id + "/" + s_BaseInfoFile;
		File cfile=new File(stockBaseInfoFileName);
		if(!cfile.exists()) 
		{
			cResultStockBaseData.error = -10;
			return cResultStockBaseData;
		}
		
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(cfile));
			String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                //System.out.println("line " + line + ": " + tempString);
            	String[] cols = tempString.split(",");
            	
            	cResultStockBaseData.stockBaseInfo.name = cols[0];
            	cResultStockBaseData.stockBaseInfo.price = Float.parseFloat(cols[1]);
            	cResultStockBaseData.stockBaseInfo.allMarketValue = Float.parseFloat(cols[2]);
            	cResultStockBaseData.stockBaseInfo.circulatedMarketValue = Float.parseFloat(cols[3]);
            	cResultStockBaseData.stockBaseInfo.peRatio = Float.parseFloat(cols[4]);

                break;
            }
            reader.close();
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage()); 
			cResultStockBaseData.error = -1;
			return cResultStockBaseData;
		}
		return cResultStockBaseData;
	}
	
	public static class ResultUpdateStock
	{
		public ResultUpdateStock()
		{
			error = 0;
			updateCnt = 0;
		}
		public int error;
		public int updateCnt;
	}
	public static ResultUpdateStock updateStock(String id)
	{
		ResultUpdateStock cResultUpdateStock = new ResultUpdateStock();
		
		// ��ȡ��ǰ��Ч���ڣ������գ����������գ�
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//�������ڸ�ʽ
		String CurrentDate = df.format(new Date());
		int curyear = Integer.parseInt(CurrentDate.split("-")[0]);
		int curmonth = Integer.parseInt(CurrentDate.split("-")[1]);
		int curday = Integer.parseInt(CurrentDate.split("-")[2]);
		Calendar xcal = Calendar.getInstance();
		xcal.set(curyear, curmonth-1, curday);
		int cw = xcal.get(Calendar.DAY_OF_WEEK);
		while(cw == 1 || cw == 7)
		{
			xcal.add(Calendar.DATE, -1);
			cw = xcal.get(Calendar.DAY_OF_WEEK);
		}
		Date curValiddate = xcal.getTime();
		String curValiddateStr = df.format(curValiddate);
		// System.out.println("CurrentValidDate:" + curValiddateStr);
		
		// ��ȡ������k������ֺ���Ϣ����
		ResultDayKData cResultDayKDataLocal = DataEngineBase.getDayKData(id);
		ResultDividendPayout cResultDividendPayout = DataEngineBase.getDividendPayout(id);
		if(0 == cResultDayKDataLocal.error 
			&& 0 == cResultDividendPayout.error 
			&& cResultDayKDataLocal.resultList.size() != 0 
			/*&& retListLocalDividend.size() != 0 */)
		// ��������K����
		{
			// ��ȡ���������������
			DayKData cDayKDataLast = cResultDayKDataLocal.resultList.get(cResultDayKDataLocal.resultList.size()-1);
			String localDataLastDate = cDayKDataLast.date; 
			//System.out.println("localDataLastDate:" + localDataLastDate);
			
			// �����ǰ���ڴ��ڱ�������������ڣ���Ҫ�������
			if(curValiddateStr.compareTo(localDataLastDate) > 0)
			{
				// ��ȡ��ǰ����ʵʱ��Ϣ
				ResultRealTimeInfoMore cResultRealTimeInfoMore = DataWebStockRealTimeInfo.getRealTimeInfoMore(id);
				if(0 == cResultRealTimeInfoMore.error)
				{
					// �����Ʊ������Ϣ
					StockBaseInfo cStockBaseData = new StockBaseInfo();
					cStockBaseData.name = cResultRealTimeInfoMore.realTimeInfoMore.name;
					cStockBaseData.price = cResultRealTimeInfoMore.realTimeInfoMore.curPrice;
					cStockBaseData.allMarketValue = cResultRealTimeInfoMore.realTimeInfoMore.allMarketValue;
					cStockBaseData.circulatedMarketValue = cResultRealTimeInfoMore.realTimeInfoMore.circulatedMarketValue;
					cStockBaseData.peRatio = cResultRealTimeInfoMore.realTimeInfoMore.peRatio;
					saveStockBaseData(id, cStockBaseData);
					
					// ��ǰʱ��������֮ǰ������������Ч����Ϊǰһ�죨���������գ�
					String webValidLastDate = cResultRealTimeInfoMore.realTimeInfoMore.date;
					if(cResultRealTimeInfoMore.realTimeInfoMore.time.compareTo("15:00:00") < 0)
					{
						int year = Integer.parseInt(cResultRealTimeInfoMore.realTimeInfoMore.date.split("-")[0]);
						int month = Integer.parseInt(cResultRealTimeInfoMore.realTimeInfoMore.date.split("-")[1]);
						int day = Integer.parseInt(cResultRealTimeInfoMore.realTimeInfoMore.date.split("-")[2]);
						int hour = Integer.parseInt(cResultRealTimeInfoMore.realTimeInfoMore.time.split(":")[0]);
						int min = Integer.parseInt(cResultRealTimeInfoMore.realTimeInfoMore.time.split(":")[1]);
						int sec = Integer.parseInt(cResultRealTimeInfoMore.realTimeInfoMore.time.split(":")[2]);
						Calendar cal0 = Calendar.getInstance();
						cal0.set(year, month-1, day, hour, min, sec);
						// ��ȡ��һ������ĩ������
						cal0.add(Calendar.DATE, -1);
						int webwk = cal0.get(Calendar.DAY_OF_WEEK);
						while(webwk == 1 || webwk == 7)
						{
							cal0.add(Calendar.DATE, -1);
							webwk = cal0.get(Calendar.DAY_OF_WEEK);
						}
						
						Date vdate = cal0.getTime();
						webValidLastDate = df.format(vdate);
					}
					// System.out.println("webValidLastDate:" + webValidLastDate);
					
					// ����������Ч���ڱȱ��������£���Ҫ׷�Ӹ���
					if(webValidLastDate.compareTo(cDayKDataLast.date) > 0)
					{
						// �����������Ҫ��ȡ��һ��ʱ������
						int year = Integer.parseInt(localDataLastDate.split("-")[0]);
						int month = Integer.parseInt(localDataLastDate.split("-")[1]);
						int day = Integer.parseInt(localDataLastDate.split("-")[2]);
						Calendar cal1 = Calendar.getInstance();
						cal1.set(year, month-1, day);
						cal1.add(Calendar.DATE, 1);
						Date fromDate = cal1.getTime();
						String fromDateStr = df.format(fromDate).replace("-", "");
						String toDateStr = webValidLastDate.replace("-", "");
						//System.out.println("fromDateStr:" + fromDateStr);
						//System.out.println("toDateStr:" + toDateStr);
						
						// ��ȡ������K����
						ResultDayKData cResultDayKDataMore = DataWebStockDayK.getDayKData(id, fromDateStr, toDateStr);
						if(0 == cResultDayKDataMore.error)
						// ������K���ݻ�ȡ�ɹ�
						{
							// �򱾵������б���׷���µĸ�������
							for(int i = 0; i < cResultDayKDataMore.resultList.size(); i++)  
					        {  
								DayKData cDayKData = cResultDayKDataMore.resultList.get(i);  
								cResultDayKDataLocal.resultList.add(cDayKData);
					        } 
							// ׷�Ӻ�ı����б���K���ݱ���������
							int retsetDayKData = DataEngineBase.saveDayKData(id, cResultDayKDataLocal.resultList);
							if(0 == retsetDayKData)
							// ����ɹ�
							{
								// ���¸�Ȩ��������
								if(0 == DataEngineBase.downloadStockDividendPayout(id))
								{
									// ׷�ӳɹ�
									cResultUpdateStock.error = 0;
									cResultUpdateStock.updateCnt = cResultDayKDataMore.resultList.size();
									return cResultUpdateStock;
								}
								else
								{
									// ���¸�Ȩ����ʧ��
									cResultUpdateStock.error = -80;
									return cResultUpdateStock;
								}
							}
							else
							{
								//���汾������ʧ��
								cResultUpdateStock.error = -50;
								return cResultUpdateStock;
							}
						}
						else
						{
							// �����ȡ׷������ʧ��
							cResultUpdateStock.error = -40;
							return cResultUpdateStock;
						}
						
					}
					else
					{
						// �Ѿ�������������Ч����һ��
						cResultUpdateStock.error = 0;
						return cResultUpdateStock;
					}
				}
				else
				{
					// ��ȡ����������Ч��������ʧ��
					cResultUpdateStock.error = -20;
					return cResultUpdateStock;
				}
			}
			else
			{
				// ���������Ѿ�������
				cResultUpdateStock.error = 0;
				return cResultUpdateStock;
			}
		}
		else
		// ����û�����ݣ���Ҫ��ͼ��������
		{
			// ������K���ֺ���Ϣ��������Ϣ
			int retdownloadStockDayk =  DataEngineBase.downloadStockDayk(id);
			int retdownloadStockDividendPayout =  DataEngineBase.downloadStockDividendPayout(id);
			int retdownloadBaseInfo =  DataEngineBase.downloadBaseInfo(id);
			if(0 == retdownloadStockDayk 
					&& 0 == retdownloadStockDividendPayout 
					&& 0 == retdownloadBaseInfo)
			// ������K���ֺ���Ϣ��������Ϣ �ɹ�
			{
				ResultDayKData cResultDayKDataLocalNew = DataEngineBase.getDayKData(id);
				if(cResultDayKDataLocalNew.error == 0)
				{
					//�����������سɹ���������
					cResultUpdateStock.error = 0;
					cResultUpdateStock.updateCnt = cResultDayKDataLocalNew.resultList.size();
					return cResultUpdateStock;
				}
				else
				{
					cResultUpdateStock.error = -23;
					return cResultUpdateStock;
				}
			}
			else
			// ������K���ֺ���Ϣ��������Ϣ ʧ��
			{
				cResultUpdateStock.error = -10;
				return cResultUpdateStock;
			}
		}
	}
	
	/*
	 * ���¹�Ʊ����������
	 * �ɹ�����true
	 */
	public static boolean updateStocksFinish(String dateStr)
	{
		if(0 != mkStocDataDir()) return false;
		String updateFinishFile = s_DataDir + "/" + s_updateFinish;
	
		File cfile =new File(updateFinishFile);
		try
		{
			FileOutputStream cOutputStream = new FileOutputStream(cfile);
	        cOutputStream.write(dateStr.getBytes());
			cOutputStream.close();
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage()); 
			return false;
		}
		return true;
	}
	
	/*
	 * ��ȡ��ǰ���������������
	 * ����nullʧ��
	 * ��ȡ�ɹ��������� e.g: 2016-01-01
	 */
	public static class ResultUpdatedStocksDate
	{
		public ResultUpdatedStocksDate()
		{
			error = 0;
			date = "0000-00-00";
		}
		public int error;
		public String date;
	}
	public static ResultUpdatedStocksDate getUpdatedStocksDate()
	{
		ResultUpdatedStocksDate cResultUpdatedStocksDate = new ResultUpdatedStocksDate();
		
		String dateStr = null;
		String updateFinishFile = s_DataDir + "/" + s_updateFinish;
		File cfile =new File(updateFinishFile);
		try
		{
			String encoding = "utf-8";
			InputStreamReader read = new InputStreamReader(new FileInputStream(cfile),encoding);//���ǵ������ʽ
            BufferedReader bufferedReader = new BufferedReader(read);
            String lineTxt = bufferedReader.readLine();
            lineTxt = lineTxt.trim().replace("\n", "");
            if(lineTxt.length() == "0000-00-00".length())
            {
            	cResultUpdatedStocksDate.date = lineTxt;
            }
            read.close();
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage()); 
			cResultUpdatedStocksDate.error = -1;
		}
		return cResultUpdatedStocksDate;
	}
	
	/*
	 * ��������·��
	 * �ɹ�����0
	 * e.g: data
	 */
	public static int mkStocDataDir()
	{
		File dataDir =new File(s_DataDir);
		if  (!dataDir .exists() && !dataDir.isDirectory())      
		{        
			dataDir.mkdir();    
		}
		if(dataDir.exists())
		{
			return 0;
		}
		else
		{
			return -1;
		}
	}
	
	/*
	 * ������Ʊ����·��
	 * �ɹ�����0
	 * e.g: data/600001
	 */
	public static int mkStocDataDir(String id)
	{
		File dataDir =new File(s_DataDir);
		if  (!dataDir .exists() && !dataDir.isDirectory())      
		{        
			dataDir.mkdir();    
		}
		File stockIdDir =new File(s_DataDir + "/" + id);
		if  (!stockIdDir .exists() && !stockIdDir.isDirectory())      
		{        
			stockIdDir.mkdir();    
		}
		if(stockIdDir.exists())
		{
			return 0;
		}
		else
		{
			return -1;
		}
	}
	
	/*
	 * ɾ����Ʊ����·��
	 * �ɹ�����0
	 * e.g: data/600001
	 */
	public static int rmStockDataDir(String stockID)
	{
		File stockIdDir =new File(s_DataDir + "/" + stockID);
		if(!stockIdDir.exists())      
		{        
			return 0;
		}
		if(0 == help_deleteFile(stockIdDir))
		{
			return 0;
		}
		if(stockIdDir.exists())
		{
			return -1;
		}
		else
		{
			return 0;
		}
	}
	private static int help_deleteFile(File file) {  
	    if (file.exists()) 
	    {
	    	
			if (file.isFile()) 
			{
				//���ļ�  
			    if(!file.delete()) //ɾ���ļ�   
			    {
			    	return -1;
			    }
			} 
			else if (file.isDirectory()) 
			{
				//��һ��Ŀ¼  
			    File[] files = file.listFiles();//����Ŀ¼�����е��ļ� files[];  
			    for (int i = 0;i < files.length;i ++) {//����Ŀ¼�����е��ļ�  
			    	help_deleteFile(files[i]);//��ÿ���ļ�������������е���  
			    }  
			    if(!file.delete()) //ɾ���ļ���  
			    {
			    	return -1;
			    }
			 }  
	    } 
	    return 0;
	}  
	
	
	private static String s_DataDir = "data";
	private static String s_updateFinish = "updateFinish.txt";
	
	private static String s_daykFile = "dayk.txt";
	private static String s_DividendPayoutFile = "dividendPayout.txt";
	private static String s_BaseInfoFile = "baseInfo.txt";
	
	static private Formatter s_fmt = new Formatter(System.out);
}
