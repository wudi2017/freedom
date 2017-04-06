package stormstock.fw.report;

import java.util.ArrayList;
import java.util.List;

import stormstock.fw.base.BEventSys;
import stormstock.fw.base.BLog;
import stormstock.fw.base.BQThread.BQThreadRequest;
import stormstock.fw.base.BTypeDefine.RefFloat;
import stormstock.fw.base.BImageCurve;
import stormstock.fw.event.ReportAnalysis;
import stormstock.fw.tranbase.account.AccountControlIF;
import stormstock.fw.tranbase.account.AccountPublicDef.CommissionOrder;
import stormstock.fw.tranbase.account.AccountPublicDef.DealOrder;
import stormstock.fw.tranbase.account.AccountPublicDef.HoldStock;
import stormstock.fw.tranbase.account.AccountPublicDef.TRANACT;
import stormstock.fw.tranbase.com.GlobalUserObj;
import stormstock.fw.tranbase.stockdata.StockDataIF;
import stormstock.fw.tranbase.stockdata.StockDay;
import stormstock.fw.tranbase.stockdata.StockDataIF.ResultHistoryData;
import stormstock.fw.base.BImageCurve.CurvePoint;
import stormstock.fw.report.InfoCollector.DailyReport;

public class TranInfoCollectWorkRequest extends BQThreadRequest {
	
	public TranInfoCollectWorkRequest(String date, String time, InfoCollector cInfoCollector)
	{
		m_date = date;
		m_time = time;
		m_cInfoCollector = cInfoCollector;
	}

	@Override
	public void doAction() {
		BLog.output("REPORT", "TranInfoCollectWorkRequest.doAction [%s %s]\n", m_date, m_time);
		
		StockDataIF cStockDataIF = GlobalUserObj.getCurStockDataIF();
		AccountControlIF cAccountControlIF = GlobalUserObj.getCurAccountControlIF();
		
		// 创建DailyReport
		DailyReport cDailyReport = new DailyReport();
		cDailyReport.date = m_date;
		
		// 添加当天上证指数
		ResultHistoryData cResultHistoryData = cStockDataIF.getHistoryData("999999", m_date, m_date);
		List<StockDay> cSHCompositeList = cResultHistoryData.resultList;
		cDailyReport.fSHComposite = cSHCompositeList.get(0).close();
		
		RefFloat lockedMoney = new RefFloat();
		int iRetLockedMoney = cAccountControlIF.getLockedMoney(lockedMoney);
		RefFloat availableMoney = new RefFloat();
		int iRetAvailableMoney = cAccountControlIF.getAvailableMoney(m_date, m_time, availableMoney);
		
		RefFloat totalAssets = new  RefFloat();
		int iRetTotalAssets = cAccountControlIF.getTotalAssets(m_date, m_time, totalAssets);
		RefFloat money = new RefFloat();
		int iRetMoney = cAccountControlIF.getMoney(m_date, m_time, money);
		List<HoldStock> cHoldStockList = new ArrayList<HoldStock>();
		int iRetHoldStockList = cAccountControlIF.getHoldStockList(m_date, m_time, cHoldStockList);
		List<String> cStockSelectList = new ArrayList<String>();
		int iRetStockSelectList = cAccountControlIF.getStockSelectList(cStockSelectList);
		List<CommissionOrder> cCommissionOrderList = new ArrayList<CommissionOrder>();
		int iRetCommissionOrderList = cAccountControlIF.getCommissionOrderList(cCommissionOrderList);
		
		if(0 == iRetLockedMoney
				&& 0 == iRetAvailableMoney
				&& 0 == iRetTotalAssets
				&& 0 == iRetMoney
				&& 0 == iRetHoldStockList
				&& 0 == iRetStockSelectList
				&& 0 == iRetCommissionOrderList)
		{
			// 打印资产
			cDailyReport.fTotalAssets = totalAssets.value;
			BLog.output("REPORT", "    [LockedMoney] %.3f\n", lockedMoney.value);
			BLog.output("REPORT", "    [AvailableMoney] %.3f\n", availableMoney.value);
			BLog.output("REPORT", "    -TotalAssets: %.3f\n", totalAssets.value);
			BLog.output("REPORT", "    -Money: %.3f\n", money.value);
			float fStockMarketValue = 0.0f;
			for(int i=0; i<cHoldStockList.size(); i++ )
			{
				HoldStock cHoldStock = cHoldStockList.get(i);
				fStockMarketValue = fStockMarketValue + cHoldStock.totalAmount * cHoldStock.curPrice;
			}
			BLog.output("REPORT", "    -StockMarketValue: %.3f\n", fStockMarketValue);
			
			// 打印持股
			for(int i=0; i<cHoldStockList.size(); i++ )
			{
				HoldStock cHoldStock = cHoldStockList.get(i);
				BLog.output("REPORT", "    -HoldStock: %s %d %d %.3f %.3f %.3f %d\n", 
						cHoldStock.stockID,
						cHoldStock.totalAmount, cHoldStock.availableAmount, 
						cHoldStock.refPrimeCostPrice, cHoldStock.curPrice, cHoldStock.totalAmount*cHoldStock.curPrice, 
						cHoldStock.investigationDays);
			}
			
			// 打印委托单
			for(int i=0; i<cCommissionOrderList.size(); i++ )
			{
				CommissionOrder cCommissionOrder = cCommissionOrderList.get(i);
				String tranOpe = "BUY"; 
				if(cCommissionOrder.tranAct == TRANACT.SELL ) tranOpe = "SELL";
					
				BLog.output("REPORT", "    -CommissionOrder: %s %s %s %d %.3f\n", 
						cCommissionOrder.time, tranOpe, cCommissionOrder.stockID, 
						cCommissionOrder.amount, cCommissionOrder.price);
			}

			// 打印晚间选股
			if(cStockSelectList.size() > 0)
			{
				String logStr = "";
				logStr += String.format("    -SelectList(%d):[ ", cStockSelectList.size());
				for(int i=0; i<cStockSelectList.size(); i++ )
				{
					String stockId = cStockSelectList.get(i);
					logStr += String.format("%s ", stockId);
					if (i >= 7 && cStockSelectList.size()-1 > 16) {
						logStr += String.format("... ", stockId);
						break;
					}
				}
				logStr += String.format("]");
				BLog.output("REPORT", "%s\n", logStr);
			}
			
			// 添加DailyReport
			m_cInfoCollector.addDailyReport(cDailyReport);
		}
		else
		{
			BLog.output("REPORT", "    Get Account Info failed (%d %d %d %d %d %d)\n", 
					iRetLockedMoney, iRetTotalAssets, iRetAvailableMoney,
					iRetHoldStockList, iRetStockSelectList, iRetCommissionOrderList);
		}
		
		ReportAnalysis.TranInfoCollectCompleteNotify.Builder msg_builder = ReportAnalysis.TranInfoCollectCompleteNotify.newBuilder();
		msg_builder.setDate(m_date);
		msg_builder.setTime(m_time);

		ReportAnalysis.TranInfoCollectCompleteNotify msg = msg_builder.build();
		BEventSys.EventSender cSender = new BEventSys.EventSender();
		cSender.Send("BEV_TRAN_TRANINFOCOLLECTCOMPLETENOTIFY", msg);
	}

	private String m_date;
	private String m_time;
	
	private InfoCollector m_cInfoCollector;
}
