package stormstock.fw.control;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import stormstock.fw.base.BEventSys;
import stormstock.fw.base.BLog;
import stormstock.fw.base.BThread;
import stormstock.fw.base.BUtilsDateTime;
import stormstock.fw.base.BWaitObj;
import stormstock.fw.event.Transaction;
import stormstock.fw.event.Transaction.ControllerStartNotify;
import stormstock.fw.tranbase.account.AccountControlIF;
import stormstock.fw.tranbase.com.GlobalTranDateTime;
import stormstock.fw.tranbase.com.GlobalUserObj;
import stormstock.fw.tranbase.com.ITranStockSetFilter;
import stormstock.fw.tranbase.stockdata.Stock;
import stormstock.fw.tranbase.stockdata.StockDataIF;
import stormstock.fw.tranbase.stockdata.StockDataIF.ResultAllStockID;
import stormstock.fw.tranbase.stockdata.StockDataIF.ResultHistoryData;
import stormstock.fw.tranbase.stockdata.StockDataIF.ResultLatestStockInfo;
import stormstock.fw.tranbase.stockdata.StockDataIF.ResultStockTime;
import stormstock.fw.tranbase.stockdata.StockDay;
import stormstock.fw.tranbase.stockdata.StockInfo;
import stormstock.fw.tranbase.stockdata.StockTime;
import stormstock.fw.tranbase.stockdata.StockUtils;


public class WorkEntity {
	public WorkEntity(boolean bHistoryTest, String beginDate, String endDate)
	{
		// �����������
		m_bHistoryTest = bHistoryTest;
		m_beginDate = beginDate;
		m_endDate = endDate;
		
		// ��ʼ����ʷ�����ձ�
		if(m_bHistoryTest)
		{
			m_hisTranDate = new ArrayList<String>();
			StockDataIF stockDataIF = GlobalUserObj.getCurStockDataIF();
			ResultHistoryData cResultHistoryData = stockDataIF.getHistoryData("999999");
			List<StockDay> cStocDayListShangZheng = cResultHistoryData.resultList;
			int iB = StockUtils.indexDayKAfterDate(cStocDayListShangZheng, m_beginDate, true);
			int iE = StockUtils.indexDayKBeforeDate(cStocDayListShangZheng, m_endDate, true);
			
			for(int i = iB; i <= iE; i++)  
	        {  
				StockDay cStockDayShangZheng = cStocDayListShangZheng.get(i);  
				String curDateStr = cStockDayShangZheng.date();
				m_hisTranDate.add(curDateStr);
	        }
		}
		
		m_entitySelect = new WorkEntitySelect();
		m_entityCreate = new WorkEntityCreate();
		m_entityClear = new WorkEntityClear();
		m_entityReport = new WorkEntityReport();
	}
	
	void work()
	{
		// ���ز��Լ�
		BLog.output("CTRL", "work call LoadStockIDSet ...\n");
		int stockSetSize = LoadStockIDSet();
		BLog.output("CTRL", "work call LoadStockIDSet OK stockCnt(%d) \n", stockSetSize);
		
		// ÿ�����ѭ��
		String dateStr = getStartDate();
		while(true) 
		{
			BLog.output("CTRL", "[%s] ############################################################################## \n", 
					dateStr);
			
			String timestr = "00:00:00";
			
			// 09:25ȷ���Ƿ��ǽ�����
			boolean bIsTranDate = false;
			timestr = "09:25:00";
			waitForDateTime(dateStr, timestr);
			if(isTranDate(dateStr))
			{
				bIsTranDate = true;
			}
			BLog.output("CTRL", "[%s %s] isTranDate = %b \n", dateStr, timestr, bIsTranDate);
			
			
			if(bIsTranDate)
			{
				// 09:27 �˻��½����ճ�ʼ��
				timestr = "09:27:00";
				waitForDateTime(dateStr, timestr);
				AccountControlIF accIF = GlobalUserObj.getCurAccountControlIF();
				boolean bAccInit = false;
				for(int i=0;i<5;i++) // ��ͼ5�γ�ʼ���˻�
				{
					bAccInit = accIF.newDayInit(dateStr, timestr);
					if(bAccInit)
					{
						break;
					}
					BThread.sleep(3000);
				}
				BLog.output("CTRL", "[%s %s] account newDayInit = %b \n", dateStr, timestr, bAccInit);
				
				if(bAccInit)
				{
					// 9:30-11:30 1:00-3:00 ���ڼ�����ͽ����źţ��ȴ��źŴ������֪ͨ
					int interval_min = 1;
					String timestr_begin = "09:30:00";
					String timestr_end = "11:30:00";
					timestr = timestr_begin;
					while(true)
					{
						if(waitForDateTime(dateStr, timestr))
						{
							BLog.output("CTRL", "[%s %s] stockClearAnalysis & stockCreateAnalysis \n", dateStr, timestr);
							m_entityClear.stockClear(dateStr, timestr);
							m_entityCreate.stockCreate(dateStr, timestr);
						}
						timestr = BUtilsDateTime.getTimeStrForSpecifiedTimeOffsetM(timestr, interval_min);
						if(timestr.compareTo(timestr_end) > 0) break;
					}
					
					timestr_begin = "13:00:00";
					timestr_end = "15:00:00";
					timestr = timestr_begin;
					while(true)
					{
						if(waitForDateTime(dateStr, timestr))
						{
							BLog.output("CTRL", "[%s %s] stockClearAnalysis & stockCreateAnalysis \n", dateStr, timestr);
							m_entityClear.stockClear(dateStr, timestr);
							m_entityCreate.stockCreate(dateStr, timestr);
						}
						timestr = BUtilsDateTime.getTimeStrForSpecifiedTimeOffsetM(timestr, interval_min);
						if(timestr.compareTo(timestr_end) > 0) break;
					}

					// 16:00 �˻����ս��׽���
					timestr = "16:00:00";
					if(waitForDateTime(dateStr, timestr))
					{
						BLog.output("CTRL", "[%s %s] account newDayTranEnd\n", dateStr, timestr);
						accIF.newDayTranEnd(dateStr, timestr);
					}
					
					// 20:00 
					timestr = "20:00:00";
					if(waitForDateTime(dateStr, timestr))
					{
						BLog.output("CTRL", "[%s %s] updateStockData & StockSelectAnalysis & transaction info collection \n", dateStr, timestr);
						
						// ������ʷ����
						StockDataIF stockDataIF = GlobalUserObj.getCurStockDataIF();
						stockDataIF.updateAllLocalStocks(dateStr);
						
						// ѡ�� �ȴ�ѡ�����
						m_entitySelect.selectStock(dateStr, timestr);
						
						// ���ձ�����Ϣ�ռ�
						m_entityReport.tranInfoCollect(dateStr, timestr);
					}
				}
				else
				{
					BLog.output("CTRL", "[%s %s] account newDayInit failed, continue! \n", dateStr, timestr);
				}
			}
			else
			{
				BLog.output("CTRL", "[%s %s] Not transaction date, continue! \n", dateStr, timestr);
			}
			
			// ��ȡ��һ����
			dateStr = getNextDate();
			if(null == dateStr) break;
		}
		
		// ������Ϻ����һ��� "21:30:00" ���ɱ���
		m_entityReport.generateReport(m_endDate, "21:30:00");
		
		BEventSys.EventSender cSender = new BEventSys.EventSender();
		cSender.Send("BEV_TRAN_ENGINEEXIT", Transaction.TranEngineExitNotify.newBuilder().build());
	}
	
	public void onStockSelectAnalysisCompleteNotify(com.google.protobuf.GeneratedMessage m) 
	{
		m_entitySelect.onStockSelectAnalysisCompleteNotify(m);
	}
	public void onStockCreateAnalysisCompleteNotify(com.google.protobuf.GeneratedMessage m)
	{
		m_entityCreate.onStockCreateAnalysisCompleteNotify(m);
	}
	public void onStockClearAnalysisCompleteNotify(com.google.protobuf.GeneratedMessage m) {
		m_entityClear.onStockClearAnalysisCompleteNotify(m);
	}
	public void onTranInfoCollectCompleteNotify(com.google.protobuf.GeneratedMessage m) {
		m_entityReport.onTranInfoCollectCompleteNotify(m);
	}
	public void onGenerateReportCompleteNotify(com.google.protobuf.GeneratedMessage m) {
		m_entityReport.onGenerateReportCompleteNotify(m);
	}
	
	/*
	 * realtimeģʽ
	 * 	һֱ�ȴ���9:25�����Ƿ��ǽ����գ�������ָ֤��ʵʱ�仯ȷ��
	 * historymockģʽ
	 * 	������ָ֤��ֱ��ȷ���Ƿ��ǽ�����
	 */
	private boolean isTranDate(String date)
	{
		if(m_bHistoryTest)
		{
			// ���ݴ����ų�,�������� ���������޷��������ȡ����
			if(
				date.equals("2013-03-08")
				|| date.equals("2015-06-09")
				|| date.equals("2016-10-17")
				|| date.equals("2016-11-25")
				)
			{
				return false;
			}
			return m_hisTranDate.contains(date);
		}
		else
		{
			// ȷ�Ͻ����Ƿ��ǽ�����
			StockDataIF stockDataIF = GlobalUserObj.getCurStockDataIF();
			String yesterdayDate = BUtilsDateTime.getDateStrForSpecifiedDateOffsetD(m_curDate, -1);
			stockDataIF.updateLocalStocks("999999", yesterdayDate);
			ResultHistoryData cResultHistoryData = stockDataIF.getHistoryData("999999");
			List<StockDay> cStockDayShangZhengList = cResultHistoryData.resultList;
			for(int i = 0; i < cStockDayShangZhengList.size(); i++)  
	        {  
				StockDay cStockDayShangZheng = cStockDayShangZhengList.get(i);  
				String checkDateStr = cStockDayShangZheng.date();
				if(checkDateStr.equals(date))
				{
					return true;
				}
	        }
			
			for(int i = 0; i < 5; i++) // ��ͼ5����ȷ��
			{
				ResultStockTime cResultStockTime = stockDataIF.getStockTime("999999", date, BUtilsDateTime.GetCurTimeStr());
				if(0 == cResultStockTime.error)
				{
					if(cResultStockTime.date.compareTo(date) == 0)
					{
						return true;
					}
				}
				BThread.sleep(3000);
			}
			return false;
		}
	}
	
	private String getStartDate()
	{
		if(m_bHistoryTest)
		{
			m_curDate = m_beginDate;
			return m_curDate;
		}
		else
		{
			String curDateStr = BUtilsDateTime.GetDateStr(new Date());
			m_curDate = curDateStr;
			return curDateStr;
		}
	}
	private String getNextDate()
	{
		m_curDate = BUtilsDateTime.getDateStrForSpecifiedDateOffsetD(m_curDate, 1);
		if(m_bHistoryTest)
		{
			if(m_curDate.compareTo(m_endDate) > 0)
			{
				return null;
			}
			else
			{
				return m_curDate;
			}
		}
		else
		{
			return m_curDate;
		}
	}
	
	/*
	 * realtimeģʽ
	 * 	�ȴ�����ʱ��ɹ�������true
	 * 	�ȴ�ʧ�ܣ�����false������ȴ���ʱ���Ѿ�����
	 * historymockģʽ
	 * 	ֱ�ӷ���true
	 */
	private boolean waitForDateTime(String date, String time)
	{
		if(m_bHistoryTest)
		{
			GlobalTranDateTime.setTranDateTime(date, time);
			return true;
		}
		else
		{
			BLog.output("CTRL", "realtime waitting DateTime (%s %s)... \n", date, time);
			boolean bWait = BUtilsDateTime.waitDateTime(date, time);
			GlobalTranDateTime.setTranDateTime(date, time);
			BLog.output("CTRL", "realtime waitting DateTime (%s %s) complete! result(%b)\n", date, time, bWait);
			return bWait;
		}
	}
	
	private int LoadStockIDSet()
	{
		StockDataIF stockDataIF = GlobalUserObj.getCurStockDataIF();
		
		ITranStockSetFilter cTranStockSetFilter = GlobalUserObj.getCurrentTranStockSetFilter();
		
		if(null == cTranStockSetFilter)
		{
			// û�й�Ʊ���׼�����������
			BLog.error("CTRL", "LoadStockIDSet cTranStockSetFilter is null!\n");
			return 0;
		}
		
		List<String> cStockIDSet = new ArrayList<String>();
		
		ResultAllStockID cResultAllStockID = stockDataIF.getAllStockID();
		if(0 == cResultAllStockID.error)
		{
			BLog.output("CTRL", "LoadStockIDSet AllStock count(%d) \n", cResultAllStockID.resultList.size());
			for(int i=0; i<cResultAllStockID.resultList.size();i++)
			{
				String stockID = cResultAllStockID.resultList.get(i);
				ResultLatestStockInfo cResultLatestStockInfo = stockDataIF.getLatestStockInfo(stockID);
				
				if(0 == cResultLatestStockInfo.error)
				{
					StockInfo cStockInfo = cResultLatestStockInfo.stockInfo;
					
					if(null != cStockInfo && cTranStockSetFilter.tran_stockset_byLatestStockInfo(cStockInfo))
					{
						cStockIDSet.add(stockID);
					}
				}
				else
				{
					BLog.warning("CTRL", "LoadStockIDSet stockDataIF.getLatestStockInfo stockID(%s)  error(%d)\n", 
							stockID, cResultLatestStockInfo.error);
				}
			}
		}
		else
		{
			// �������ݽӿڻ�ȡ���й�ƱID����
			BLog.error("CTRL", "LoadStockIDSet stockDataIF.getAllStockID error(%d)\n", cResultAllStockID.error);
			return 0;
		}

		// ��Ʊ���׼�����
		StockObjFlow.setTranStockIDSet(cStockIDSet);
		BLog.output("CTRL", "LoadStockIDSet TranStockIDSet count(%d) \n", cStockIDSet.size());
		return cStockIDSet.size();
	}
	
	// ��������
	private boolean m_bHistoryTest;
	private String m_beginDate;
	private String m_endDate;
	
	// ��ʷ������
	private List<String> m_hisTranDate;
	
	// ��ǰ����
	private String m_curDate;
	
	private WorkEntitySelect m_entitySelect;
	private WorkEntityCreate m_entityCreate;
	private WorkEntityClear m_entityClear;
	private WorkEntityReport m_entityReport;

}
