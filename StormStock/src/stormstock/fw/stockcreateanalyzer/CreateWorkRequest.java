package stormstock.fw.stockcreateanalyzer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import stormstock.fw.base.BEventSys;
import stormstock.fw.base.BLog;
import stormstock.fw.base.BQThread.BQThreadRequest;
import stormstock.fw.base.BTypeDefine.RefFloat;
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
	 * CreateResultWrapper类
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
			
			// 构造当时股票数据(昨日日K，今日当前分时)
			
			ResultLatestStockInfo cResultLatestStockInfo = stockDataIF.getLatestStockInfo(stockID);
			String yesterday_date = BUtilsDateTime.getDateStrForSpecifiedDateOffsetD(m_date, -1);
			ResultHistoryData cResultHistoryData = stockDataIF.getHistoryData(stockID, yesterday_date);
			ResultStockTime cResultStockTime = stockDataIF.getStockTime(stockID, m_date, m_time);
			if(0 == cResultLatestStockInfo.error
					&& 0 == cResultHistoryData.error
					&& 0 == cResultStockTime.error)
			{
				// 做成当日股票数据
				StockTimeDataCache.addStockTime(stockID, m_date, cResultStockTime.stockTime());
				
				List<StockTime> cStockTimeList = StockTimeDataCache.getStockTimeList(stockID, m_date);
				StockDay curStockDay = new StockDay();
				curStockDay.set(m_date, cStockTimeList);
				cResultHistoryData.resultList.add(curStockDay);

				Stock cStock = new Stock();
				cStock.setCurLatestStockInfo(cResultLatestStockInfo.stockInfo);
				cStock.setCurStockDayData(cResultHistoryData.resultList);
				
				// 做成 ctx
				AccountAccessor cAccountAccessor = accIF.getAccountAccessor(m_date, m_time);
				StockDataAccessor cStockDataAccessor = stockDataIF.getStockDataAccessor(yesterday_date, m_time);
				Vector<HoldStock> holdStockVector = new Vector<HoldStock>();
				int iRetHoldStock = cAccountAccessor.getHoldStock(stockID, holdStockVector);
				if(0 == iRetHoldStock)
				{
					HoldStock cHoldStock = null;
					if(holdStockVector.size()==1)
					{
						cHoldStock = holdStockVector.get(0);
					}
					Target cTarget = new Target(cStock, cHoldStock);
					TranContext ctx = new TranContext(m_date, m_time, 
							cTarget,  // 目标对象带有 股票数据与持股信息
							cAccountAccessor, // ctx带有账户访问器
							cStockDataAccessor);// ctx带有昨日数据访问器（用户不能查看今天得其他k线）
					
					// 构造CreateResultWrapper
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
						
						BLog.output("CREATE", "    [%s %s] strategy_create %s (%s) (%s %.2f)...(%s %.2f) cnt(%d)\n", 
								ctx.date(), ctx.time(), 
								ctx.target().stock().getCurLatestStockInfo().ID,ctx.target().stock().GetLastDate(),
								stockTimeList.get(iBegin).time, stockTimeList.get(iBegin).price,
								stockTimeList.get(iEnd).time, stockTimeList.get(iEnd).price, 
								cnt);

						// 回调给用户
						cIStrategyCreate.strategy_create(ctx, cCreateResultWrapper.createRes);
						
						if(cCreateResultWrapper.createRes.bCreate)
						{
							cCreateResultWrapperList.add(cCreateResultWrapper);
						}
					}
				}
				else
				{
					BLog.output("CREATE", "    getHoldStock failed\n");
				}
			}
			else
			{
				BLog.output("CREATE", "    Cannot Generate %s %s stock %s, ignore!\n", m_date, m_time, stockID);
			}
		}
			

		// 根据建仓策略，做成建仓项
		int create_max_count = cIStrategyCreate.strategy_create_max_count();
		
		List<HoldStock> cHoldStockList = new ArrayList<HoldStock>();
		int iRetHoldStockList = accIF.getHoldStockList(null, null, cHoldStockList);
		List<CommissionOrder> cCommissionOrderList = new ArrayList<CommissionOrder>();
		int iRetBuyCommissionOrderList = accIF.getBuyCommissionOrderList(cCommissionOrderList);
	
		int alreadyCount = 0;
		int buyStockCount = 0;
		if(0 == iRetHoldStockList 
				&& 0 == iRetBuyCommissionOrderList)
		{
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
			buyStockCount = create_max_count - alreadyCount;
			buyStockCount = Math.min(buyStockCount,cCreateResultWrapperList.size());
		}
		
		StockCreateAnalysis.StockCreateAnalysisCompleteNotify.Builder msg_builder = StockCreateAnalysis.StockCreateAnalysisCompleteNotify.newBuilder();
		msg_builder.setDate(m_date);
		msg_builder.setTime(m_time);
		for(int i = 0; i< buyStockCount; i++)
		{
			CreateResultWrapper cCreateResultWrapper = cCreateResultWrapperList.get(i);

			// 买入量
			RefFloat totalAssets = new RefFloat();
			int iRetTotalAssets = accIF.getTotalAssets(m_date, m_time, totalAssets);
			if(0 == iRetTotalAssets)
			{
				float fMaxPositionRatio = cCreateResultWrapper.createRes.fMaxPositionRatio; 
				float fMaxPositionMoney = totalAssets.value*fMaxPositionRatio; // 最大买入仓位钱
				float fMaxMoney = cCreateResultWrapper.createRes.fMaxMoney; // 最大买入钱
				float buyMoney = Math.min(fMaxMoney, fMaxPositionMoney);
				
				int amount = (int)(buyMoney/cCreateResultWrapper.fPrice);
				amount = amount/100*100; // 买入整手化
				
				StockCreateAnalysis.StockCreateAnalysisCompleteNotify.CreateItem.Builder cItemBuild = msg_builder.addItemBuilder();
				cItemBuild.setStockID(cCreateResultWrapper.stockId);
				cItemBuild.setPrice(cCreateResultWrapper.fPrice);
				cItemBuild.setAmount(amount);
			}
			else
			{
				BLog.output("CREATE", "    getTotalAssets failed\n");
			}
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
