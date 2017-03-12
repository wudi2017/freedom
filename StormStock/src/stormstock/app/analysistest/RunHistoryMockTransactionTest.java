package stormstock.app.analysistest;

import java.util.List;

import stormstock.app.analysistest.EStockComplexGFTDEx.ResultComplexGFTDEx;
import stormstock.app.analysistest.EStockDayVolumeLevel.VOLUMELEVEL;
import stormstock.app.analysistest.ETDropStable.ResultXiaCuoQiWen;
import stormstock.fw.base.BLog;
import stormstock.fw.tranbase.account.AccountPublicDef.HoldStock;
import stormstock.fw.tranbase.com.IEigenStock;
import stormstock.fw.tranbase.com.IStrategyClear;
import stormstock.fw.tranbase.com.IStrategyCreate;
import stormstock.fw.tranbase.com.IStrategySelect;
import stormstock.fw.tranbase.com.ITranStockSetFilter;
import stormstock.fw.tranbase.com.TranContext;
import stormstock.fw.tranbase.stockdata.Stock;
import stormstock.fw.tranbase.stockdata.StockDay;
import stormstock.fw.tranbase.stockdata.StockInfo;
import stormstock.fw.tranbase.stockdata.StockTime;
import stormstock.fw.tranengine.TranEngine;
import stormstock.fw.tranengine.TranEngine.TRANACCOUNTTYPE;
import stormstock.fw.tranengine.TranEngine.TRANTIMEMODE;

public class RunHistoryMockTransactionTest {
	// 测试集
	public static class TranStockSet extends ITranStockSetFilter {
		@Override
		public boolean tran_stockset_byLatestStockInfo(StockInfo cStockInfo) {
//			if(cStockInfo.ID.compareTo("002123") >= 0 && cStockInfo.ID.compareTo("002123") <= 0) {	
//				return true;
//			}
			if(cStockInfo.circulatedMarketValue < 300.0f)
			{
				return true;
			}
			return false;
		}
	}
	// 选股
	public static class StrategySelect extends IStrategySelect {

		@Override
		public void strategy_select(TranContext ctx, SelectResult out_sr) {
			
			String stockId = ctx.target().stock().getCurLatestStockInfo().ID;
			List<StockDay> cStockDayList = ctx.target().stock().getCurStockDayData();
			
//			ResultCheckPriceDrop cResultCheckPriceDrop = EStockDayPriceDrop.checkPriceDrop(cStockDayList, cStockDayList.size()-1);
//			if (cResultCheckPriceDrop.bCheck && cResultCheckPriceDrop.fDropRatio() < -0.1f)
//			{
//				out_sr.bSelect = true;
//				out_sr.fPriority = - cResultCheckPriceDrop.fDropAcc();
//			}
			
//			ResultComplexGFTDEx cResultComplexGFTDEx = EStockComplexGFTDEx.get_buy(stockId, cStockDayList, cStockDayList.size()-1);
//			if(cResultComplexGFTDEx.bCheck)
//			{
//				out_sr.bSelect = true;
//				out_sr.fPriority = - EStockDayPriceDrop.getMidDropParam(cStockDayList, cStockDayList.size()-1);
//			}
//			
//			ResultDYCheck cResultDYCheck = EStockComplexDYCheck.get(stockId, cStockDayList, cStockDayList.size()-1);
//			if(cResultDYCheck.bCheck)
//			{
//				out_sr.bSelect = true;
//			}
		}

		@Override
		public int strategy_select_max_count() {
			// TODO Auto-generated method stub
			return 10;
		}

	}
	// 建仓
	public static class StrategyCreate extends IStrategyCreate {

		@Override
		public void strategy_create(TranContext ctx, CreateResult out_sr) {
			List<StockTime> list_stockTime = ctx.target().stock().getLatestStockTimeList();
			float fYesterdayClosePrice = ctx.target().stock().GetLastYesterdayClosePrice();

			// 二次下跌
			ResultXiaCuoQiWen cResultXiaCuoQiWen = ETDropStable.checkXiaCuoQiWen_2Times(list_stockTime, list_stockTime.size()-1);
			if (cResultXiaCuoQiWen.bCheck)
			{
				//BLog.output("TEST", "     --->>> StrategyCreate %s %s \n", ctx.date(), ctx.time());
				out_sr.bCreate = true;
				out_sr.fMaxPositionRatio = 0.2f;
			}
			

			// 建仓为跌幅一定时
			float fNowPrice = ctx.target().stock().getLatestPrice();
			float fRatio = (fNowPrice - fYesterdayClosePrice)/fYesterdayClosePrice;
			if(fRatio < -0.01)
			{
				out_sr.bCreate = true;
				out_sr.fMaxPositionRatio = 0.2f;
				
			}
			
//			// 尾盘不创新低
//			if(ctx.time().compareTo("14:50:00") >= 0
//					&& ctx.time().compareTo("14:55:00")<=0 )
//			{
//				if(list_stockTime.get(list_stockTime.size()-1).price >= fYesterdayClosePrice)
//				{
//					out_sr.bCreate = true;
//					out_sr.fMaxPositionRatio = 0.2f;
//				}
//			}
			
//			out_sr.bCreate = true;
//			out_sr.fMaxPositionRatio = 0.15f;
			
		}
		@Override
		public int strategy_create_max_count() {
			return 5;
		}

	}
	// 清仓
	public static class StrategyClear extends IStrategyClear {
		@Override
		public void strategy_clear(TranContext ctx, ClearResult out_sr) {
			HoldStock cHoldStock = ctx.target().holdStock();
			if(cHoldStock.investigationDays >= 20) // 调查天数控制
			{
				out_sr.bClear = true;
			}
			if(cHoldStock.profitRatio() > 0.03 || cHoldStock.profitRatio() < -0.06) // 止盈止损x个点卖
			{
				out_sr.bClear = true;
			}
		}
	}
	public static void main(String[] args) {
		BLog.output("TEST", "--->>> MainBegin\n");
		TranEngine cTranEngine = new TranEngine();
		
		cTranEngine.setStockSet(new TranStockSet());
		cTranEngine.setSelectStockStrategy(new EStockComplexDZCZX.StrategySelect());
		cTranEngine.setCreatePositonStrategy(new EStockComplexDZCZX.StrategyCreate());
		cTranEngine.setClearPositonStrategy(new EStockComplexDZCZX.StrategyClear());
		
		cTranEngine.setAccountType(TRANACCOUNTTYPE.MOCK); 
		cTranEngine.setTranMode(TRANTIMEMODE.HISTORYMOCK);
		cTranEngine.setHistoryTimeSpan("2012-01-01", "2016-10-15");
		
		cTranEngine.run();
		cTranEngine.mainLoop();
		
		BLog.output("TEST", "--->>> MainEnd\n");
	}
}
