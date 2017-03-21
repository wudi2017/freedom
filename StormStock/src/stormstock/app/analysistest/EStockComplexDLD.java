package stormstock.app.analysistest;

import java.util.List;

import stormstock.fw.base.BLog;
import stormstock.fw.base.BUtilsMath;
import stormstock.fw.tranbase.account.AccountPublicDef.HoldStock;
import stormstock.fw.tranbase.com.IStrategyClear;
import stormstock.fw.tranbase.com.IStrategyCreate;
import stormstock.fw.tranbase.com.IStrategySelect;
import stormstock.fw.tranbase.com.ITranStockSetFilter;
import stormstock.fw.tranbase.com.TranContext;
import stormstock.fw.tranbase.com.IStrategyClear.ClearResult;
import stormstock.fw.tranbase.com.IStrategyCreate.CreateResult;
import stormstock.fw.tranbase.com.IStrategySelect.SelectResult;
import stormstock.fw.tranbase.stockdata.StockDay;
import stormstock.fw.tranbase.stockdata.StockInfo;
import stormstock.fw.tranbase.stockdata.StockTime;
import stormstock.fw.tranbase.stockdata.StockUtils;
import stormstock.fw.tranengine.TranEngine;
import stormstock.fw.tranengine.TranEngine.TRANACCOUNTTYPE;
import stormstock.fw.tranengine.TranEngine.TRANTIMEMODE;

public class EStockComplexDLD {
	/*
	 * ***************************************************************************
	 * 完成策略
	 * 
	 */
	// 测试集
	public static class TranStockSet extends ITranStockSetFilter {
		@Override
		public boolean tran_stockset_byLatestStockInfo(StockInfo cStockInfo) {
			if(cStockInfo.ID.compareTo("300000") >= 0 && cStockInfo.ID.compareTo("399999") <= 0) {	
				return false;
			}
			if(cStockInfo.circulatedMarketValue < 50.0f)
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

			float fVal = EDILunDongParam.getLunDongVal(cStockDayList, cStockDayList.size()-1);
			//if(fVal<-0.2)
			{
				out_sr.bSelect = true;
				out_sr.fPriority = - fVal;
			}	
		}

		@Override
		public int strategy_select_max_count() {
			// TODO Auto-generated method stub
			return 20;
		}

	}
	// 建仓
	public static class StrategyCreate extends IStrategyCreate {
		
		@Override
		public void strategy_create(TranContext ctx, CreateResult out_sr) {
			String stockId = ctx.target().stock().getCurLatestStockInfo().ID;
			String time = ctx.time();
			List<StockDay> list_stocyDay = ctx.target().stock().getCurStockDayData();
			List<StockTime> list_stockTime = ctx.target().stock().getLatestStockTimeList();
			float fYesterdayClosePrice = ctx.target().stock().GetLastYesterdayClosePrice();
			float fNowPrice = ctx.target().stock().getLatestPrice();
			
			out_sr.bCreate = true;
			out_sr.fMaxPositionRatio = 0.05f;
			
		}
		@Override
		public int strategy_create_max_count() {
			return 20;
		}
	}
	// 清仓
	public static class StrategyClear extends IStrategyClear {
		@Override
		public void strategy_clear(TranContext ctx, ClearResult out_sr) {
			HoldStock cHoldStock = ctx.target().holdStock();
			String stockId = ctx.target().stock().getCurLatestStockInfo().ID;
			List<StockDay> list_stocyDay = ctx.target().stock().getCurStockDayData();
			List<StockTime> list_stockTime = ctx.target().stock().getLatestStockTimeList();
			float fYesterdayClosePrice = ctx.target().stock().GetLastYesterdayClosePrice();
			float fNowPrice = ctx.target().stock().getLatestPrice();
			
			// 持有天数止损止盈
			if(cHoldStock.investigationDays >= 20) // 调查天数控制
			{
				out_sr.bClear = true;
			}
			// 硬性指标止损止盈
			if(cHoldStock.profitRatio() < -0.10) // 止盈止损x个点卖
			{
				out_sr.bClear = true;
			}
		}
	}

	/*
	 * ********************************************************************
	 * Test
	 * ********************************************************************
	 */
	public static void main(String[] args) {
		BLog.output("TEST", "--->>> MainBegin\n");
		TranEngine cTranEngine = new TranEngine();
		
		cTranEngine.setStockSet(new TranStockSet());
		cTranEngine.setSelectStockStrategy(new StrategySelect());
		cTranEngine.setCreatePositonStrategy(new StrategyCreate());
		cTranEngine.setClearPositonStrategy(new StrategyClear());
		
		cTranEngine.setAccountType(TRANACCOUNTTYPE.MOCK); 
		cTranEngine.setTranMode(TRANTIMEMODE.HISTORYMOCK);
		cTranEngine.setHistoryTimeSpan("2016-09-01", "2017-01-18");
		
		cTranEngine.run();
		cTranEngine.mainLoop();
		
		BLog.output("TEST", "--->>> MainEnd\n");
	}
}
