package stormstock.fw.stockcreateanalyzer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import stormstock.fw.base.BEventSys;
import stormstock.fw.base.BLog;
import stormstock.fw.base.BQThread.BQThreadRequest;
import stormstock.fw.base.BUtilsDateTime;
import stormstock.fw.event.StockCreateAnalysis;
import stormstock.fw.event.Transaction;
import stormstock.fw.tranbase.account.AccountAccessor;
import stormstock.fw.tranbase.account.AccountControlIF;
import stormstock.fw.tranbase.account.AccountPublicDef.CommissionOrder;
import stormstock.fw.tranbase.account.AccountPublicDef.HoldStock;
import stormstock.fw.tranbase.com.GlobalUserObj;
import stormstock.fw.tranbase.com.IStrategyCreate;
import stormstock.fw.tranbase.com.IStrategyCreate.CreateResult;
import stormstock.fw.tranbase.com.TranContext;
import stormstock.fw.tranbase.com.TranContext.Target;
import stormstock.fw.tranbase.stockdata.Stock;
import stormstock.fw.tranbase.stockdata.StockDataAccessor;
import stormstock.fw.tranbase.stockdata.StockDataIF;
import stormstock.fw.tranbase.stockdata.StockDay;
import stormstock.fw.tranbase.stockdata.StockInfo;
import stormstock.fw.tranbase.stockdata.StockTime;
import stormstock.fw.tranbase.stockdata.StockDataIF.ResultHistoryData;
import stormstock.fw.tranbase.stockdata.StockDataIF.ResultLatestStockInfo;
import stormstock.fw.tranbase.stockdata.StockDataIF.ResultStockTime;

public class CreateWorkRequest extends BQThreadRequest {
	
	/*
	 * CreateResultWrapper��
	 */
	static private class CreateResultWrapper {
		public CreateResultWrapper(){
			createRes = new CreateResult();
		}
		public String stockId;
		public float fPrice;
		public CreateResult createRes;
	}
	
	public CreateWorkRequest(String date, String time, List<String> stockIDList)
	{
		m_date = date;
		m_time = time;
		m_stockIDList = stockIDList;
	}
	
	@Override
	public void doAction() {
		BLog.output("CREATE", "CreateWorkRequest.doAction [%s %s]\n", m_date, m_time);

		StockDataIF stockDataIF = GlobalUserObj.getCurStockDataIF();
		AccountControlIF accIF = GlobalUserObj.getCurAccountControlIF();
		
		
		IStrategyCreate cIStrategyCreate = GlobalUserObj.getCurrentStrategyCreate();
		List<String> stockIDSelectList = m_stockIDList;
		
		BLog.output("CREATE", "    stockIDSelectList count(%s)\n", stockIDSelectList.size());
		
		List<CreateResultWrapper> cCreateResultWrapperList = new ArrayList<CreateResultWrapper>();
		
		for(int i=0; i<stockIDSelectList.size(); i++)
		{
			String stockID = stockIDSelectList.get(i);
			
			// ���쵱ʱ��Ʊ����(������K�����յ�ǰ��ʱ)
			
			ResultLatestStockInfo cResultLatestStockInfo = stockDataIF.getLatestStockInfo(stockID);
			String yesterday_date = BUtilsDateTime.getDateStrForSpecifiedDateOffsetD(m_date, -1);
			ResultHistoryData cResultHistoryData = stockDataIF.getHistoryData(stockID, yesterday_date);
			ResultStockTime cResultStockTime = stockDataIF.getStockTime(stockID, m_date, m_time);
			if(0 == cResultLatestStockInfo.error
					&& 0 == cResultHistoryData.error
					&& 0 == cResultStockTime.error)
			{
				// ���ɵ��չ�Ʊ����
				StockTimeDataCache.addStockTime(stockID, m_date, cResultStockTime.stockTime());
				
				List<StockTime> cStockTimeList = StockTimeDataCache.getStockTimeList(stockID, m_date);
				StockDay curStockDay = new StockDay();
				curStockDay.set(m_date, cStockTimeList);
				cResultHistoryData.resultList.add(curStockDay);

				Stock cStock = new Stock();
				cStock.setCurLatestStockInfo(cResultLatestStockInfo.stockInfo);
				cStock.setCurStockDayData(cResultHistoryData.resultList);
				
				// ���� ctx
				AccountAccessor cAccountAccessor = accIF.getAccountAccessor(m_date, m_time);
				StockDataAccessor cStockDataAccessor = stockDataIF.getStockDataAccessor(yesterday_date, m_time);
				Target cTarget = new Target(cStock, cAccountAccessor.getHoldStock(stockID));
				TranContext ctx = new TranContext(m_date, m_time, 
						cTarget,  // Ŀ�������� ��Ʊ������ֹ���Ϣ
						cAccountAccessor, // ctx�����˻�������
						cStockDataAccessor);// ctx�����������ݷ��������û����ܲ鿴���������k�ߣ�
				
				// ����CreateResultWrapper
				CreateResultWrapper cCreateResultWrapper = new CreateResultWrapper();
				cCreateResultWrapper.stockId = stockID;
				cCreateResultWrapper.fPrice = cResultStockTime.stockTime().price;
				
				List<StockTime> stockTimeList = ctx.target().stock().getLatestStockTimeList();
				if(stockTimeList.size()>0)
				{
					// log
					int iBegin = 0;
					int iEnd = stockTimeList.size()-1;
					int cnt = stockTimeList.size();
					
					BLog.output("CREATE", "    [%s %s] strategy_create stockID:%s (%s) (%s %.2f)...(%s %.2f) cnt(%d)\n", 
							ctx.date(), ctx.time(), 
							ctx.target().stock().getCurLatestStockInfo().ID,ctx.target().stock().GetLastDate(),
							stockTimeList.get(iBegin).time, stockTimeList.get(iBegin).price,
							stockTimeList.get(iEnd).time, stockTimeList.get(iEnd).price, 
							cnt);
					if(m_date.equals("2016-03-29") && m_time.equals("13:08:00"))
					{
						BLog.output("CREATE", "TEST!\n");
					}
					// �ص����û�
					cIStrategyCreate.strategy_create(ctx, cCreateResultWrapper.createRes);
					
					if(cCreateResultWrapper.createRes.bCreate)
					{
						cCreateResultWrapperList.add(cCreateResultWrapper);
					}
				}
			}
			else
			{
				BLog.output("CREATE", "    Cannot Generate %s %s stock %s, ignore!\n", m_date, m_time, stockID);
			}
		}
			

		// ���ݽ��ֲ��ԣ����ɽ�����
		int create_max_count = cIStrategyCreate.strategy_create_max_count();
		
		List<HoldStock> cHoldStockList = new ArrayList<HoldStock>();
		accIF.getHoldStockList(null, null, cHoldStockList);
		List<CommissionOrder> cCommissionOrderList = accIF.getBuyCommissionOrderList();
		int alreadyCount = 0;
		for(int i=0;i<cHoldStockList.size();i++)
		{
			HoldStock cHoldStock = cHoldStockList.get(i);
			if(cHoldStock.totalAmount > 0)
			{
				alreadyCount++;
			}
		}
		for(int i=0;i<cCommissionOrderList.size();i++)
		{
			CommissionOrder cCommissionOrder = cCommissionOrderList.get(i);
			boolean bExitInHold = false;
			for(int j=0;j<cHoldStockList.size();j++)
			{
				HoldStock cHoldStock = cHoldStockList.get(j);
				if(cHoldStock.stockID.equals(cCommissionOrder.stockID))
				{
					bExitInHold = true;
					break;
				}
			}
			if(!bExitInHold)
			{
				alreadyCount++;
			}
		}
		int buyStockCount = create_max_count - alreadyCount;
		buyStockCount = Math.min(buyStockCount,cCreateResultWrapperList.size());
		
		StockCreateAnalysis.StockCreateAnalysisCompleteNotify.Builder msg_builder = StockCreateAnalysis.StockCreateAnalysisCompleteNotify.newBuilder();
		msg_builder.setDate(m_date);
		msg_builder.setTime(m_time);
		for(int i = 0; i< buyStockCount; i++)
		{
			CreateResultWrapper cCreateResultWrapper = cCreateResultWrapperList.get(i);
			StockCreateAnalysis.StockCreateAnalysisCompleteNotify.CreateItem.Builder cItemBuild = msg_builder.addItemBuilder();
			cItemBuild.setStockID(cCreateResultWrapper.stockId);
			cItemBuild.setPrice(cCreateResultWrapper.fPrice);
			
			// ������
			float totalAssets = accIF.getTotalAssets(m_date, m_time);
			float fMaxPositionRatio = cCreateResultWrapper.createRes.fMaxPositionRatio; 
			float fMaxPositionMoney = totalAssets*fMaxPositionRatio; // ��������λǮ
			float fMaxMoney = cCreateResultWrapper.createRes.fMaxMoney; // �������Ǯ
			float buyMoney = Math.min(fMaxMoney, fMaxPositionMoney);
			
			int amount = (int)(buyMoney/cCreateResultWrapper.fPrice);
			amount = amount/100*100; // �������ֻ�
			cItemBuild.setAmount(amount);
		}
		StockCreateAnalysis.StockCreateAnalysisCompleteNotify msg = msg_builder.build();
		BEventSys.EventSender cSender = new BEventSys.EventSender();
		
		BLog.output("CREATE", "    stockIDCreateList count(%d)\n", msg.getItemList().size());
		for(int i=0; i< msg.getItemList().size(); i++)
		{
			String stockID = msg.getItemList().get(i).getStockID();
			float price = msg.getItemList().get(i).getPrice();
			int amount = msg.getItemList().get(i).getAmount();
			BLog.output("CREATE", "        -Stock(%s) price(%.2f) amount(%d)\n", stockID,price,amount);
		}
		
		cSender.Send("BEV_TRAN_STOCKCREATEANALYSISCOMPLETENOTIFY", msg);
		
	}
	
	private String m_date;
	private String m_time;
	private List<String> m_stockIDList;
}
