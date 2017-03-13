package stormstock.app.analysistest;

import java.util.List;

import stormstock.app.analysistest.EDIPriceDrop.ResultCheckPriceDrop;
import stormstock.app.analysistest.EStockComplexDZCZX.ResultDYCheck;
import stormstock.app.analysistest.ETDropStable.ResultXiaCuoQiWen;
import stormstock.fw.base.BLog;
import stormstock.fw.base.BThread;
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
import stormstock.fw.tranbase.stockdata.StockDataIF;
import stormstock.fw.tranbase.stockdata.StockDay;
import stormstock.fw.tranbase.stockdata.StockInfo;
import stormstock.fw.tranbase.stockdata.StockTime;
import stormstock.fw.tranbase.stockdata.StockUtils;
import stormstock.fw.tranbase.stockdata.StockDataIF.ResultHistoryData;
import stormstock.fw.tranengine.TranEngine;
import stormstock.fw.tranengine.TranEngine.TRANACCOUNTTYPE;
import stormstock.fw.tranengine.TranEngine.TRANTIMEMODE;

/**
 * DS下跌企稳反弹操作模型
 * @author wudi
 *
 */
public class EStockComplexDS {
	
	/*
	 * ***************************************************************************
	 * 完成策略
	 * 
	 */
	// 测试集
	public static class TranStockSet extends ITranStockSetFilter {
		@Override
		public boolean tran_stockset_byLatestStockInfo(StockInfo cStockInfo) {
			if(cStockInfo.ID.compareTo("002425") >= 0 && cStockInfo.ID.compareTo("002425") <= 0) {	
				return true;
			}
//			if(cStockInfo.circulatedMarketValue < 300.0f)
//			{
//				return true;
//			}
			return false;
		}
	}
	// 选股
	public static class StrategySelect extends IStrategySelect {

		@Override
		public void strategy_select(TranContext ctx, SelectResult out_sr) {
			
			String stockId = ctx.target().stock().getCurLatestStockInfo().ID;
			List<StockDay> cStockDayList = ctx.target().stock().getCurStockDayData();
			
			ResultDSSelectParam cResultDSSelectParam = EStockComplexDS.isSelect(stockId, cStockDayList, cStockDayList.size()-1);
			if (cResultDSSelectParam.bCheck)
			{
				out_sr.bSelect = true;
				out_sr.fPriority = cResultDSSelectParam.po;
			}
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
			String stockId = ctx.target().stock().getCurLatestStockInfo().ID;
			List<StockDay> list_stocyDay = ctx.target().stock().getCurStockDayData();
			List<StockTime> list_stockTime = ctx.target().stock().getLatestStockTimeList();
			float fYesterdayClosePrice = ctx.target().stock().GetLastYesterdayClosePrice();
			float fNowPrice = ctx.target().stock().getLatestPrice();
			
			ResultDSSelectParam cResultDSSelectParam = EStockComplexDS.isSelect(stockId, list_stocyDay, list_stocyDay.size()-1);
			if (cResultDSSelectParam.bCheck)
			{
				// 一次下跌
//				ResultXiaCuoQiWen cResultXiaCuoQiWen = ETDropStable.checkXiaCuoQiWen_2Times(list_stockTime, list_stockTime.size()-1);
//				if (cResultXiaCuoQiWen.bCheck && fNowPrice < fStarHigh)
//				{
//					//BLog.output("TEST", "     --->>> StrategyCreate %s %s \n", ctx.date(), ctx.time());
//					out_sr.bCreate = true;
//					out_sr.fMaxPositionRatio = 0.2f;
//				}
				
				// 建仓为跌幅一定时
				if(fNowPrice <= cResultDSSelectParam.fBuyCheck)
				{
					out_sr.bCreate = true;
					out_sr.fMaxPositionRatio = 0.2f;
				}
			}	
		}
		@Override
		public int strategy_create_max_count() {
			return 5;
		}
	}
	// 清仓
	public static class StrategyClear extends IStrategyClear {
		public static ResultDSSelectParam findSelectParam(String stockId, List<StockDay> list, int iCheck)
		{
			ResultDSSelectParam cResultDSSelectParam = new ResultDSSelectParam();
			// 检查10日内参数
			int iBegin = iCheck-10;
			int iEnd = iCheck;
			if(iBegin<0)
			{
				return cResultDSSelectParam;
			}
			for(int i=iEnd;i>=iBegin;i--)
			{
				ResultDSSelectParam findResultDSSelectParam = EStockComplexDS.isSelect(stockId, list, iCheck);
				if(findResultDSSelectParam.bCheck)
				{
					cResultDSSelectParam = findResultDSSelectParam;
					break;
				}
			}
			return cResultDSSelectParam;
		}
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

			// 选股参数止损止盈
			ResultDSSelectParam cResultDSSelectParam = findSelectParam(stockId, list_stocyDay, list_stocyDay.size()-1);
			if (cResultDSSelectParam.bCheck)
			{
				float checkSellH = cResultDSSelectParam.fSellHigh;
				if(cHoldStock.curPrice >= checkSellH)
				{
					out_sr.bClear = true;
				}
				
				float checkSellL = cResultDSSelectParam.fSellLow;
				if(cHoldStock.curPrice < checkSellL)
				{
					out_sr.bClear = true;
				}
			}

			// 硬性指标止损止盈
			if(cHoldStock.profitRatio() > 0.10 || cHoldStock.profitRatio() < -0.08) // 止盈止损x个点卖
			{
				out_sr.bClear = true;
			}
		}
	}
	
		
	/*
	 * ***************************************************************************
	 * 策略细节
	 * 
	 */
	public static class ResultDSSelectParam
	{
		public ResultDSSelectParam()
		{
			bCheck = false;
		}
		public boolean bCheck;
		public float fSellHigh;
		public float fSellLow;
		public float fBuyCheck;
		public float po;
	}
	public static ResultDSSelectParam isSelect(String stockId, List<StockDay> list, int iCheck)
	{
		ResultDSSelectParam cResultDSSelectParam = new ResultDSSelectParam();
		
		// 检查日判断
		int iBegin = iCheck-10;
		int iEnd = iCheck;
		if(iBegin<0)
		{
			return cResultDSSelectParam;
		}
		
		// 连续3日收复5日线检查
		boolean bUM3Flg = false;
		float fHigh = 0.0f;
		float fLow = 0.0f;
		float fDropRate = 0.0f;
		float fHigh_2 = 0.0f;
		for(int i=iEnd;i>=iBegin;i--)
		{
			ResultCheckPriceDrop cResultCheckPriceDrop = EDIPriceDrop.checkPriceDrop(list, i);
			if (cResultCheckPriceDrop.bCheck)
			{
				
				s_StockDayListCurve.clearMark(cResultCheckPriceDrop.iLow);
				s_StockDayListCurve.markCurveIndex(cResultCheckPriceDrop.iLow, "L");
				
				
				int iSelectBegin = cResultCheckPriceDrop.iLow;
				int iSelectEnd = iCheck;
				int iCheckUM5Cnt = 0;
				for(int iSIndex =iSelectBegin; iSIndex<=iSelectEnd;iSIndex++)
				{
					StockDay cSStockDay = list.get(iSIndex);
					float fSMA5 = StockUtils.GetMA(list, 5, iSIndex);
					if(cSStockDay.close() > fSMA5)
					{
						iCheckUM5Cnt ++;
					}
					else
					{
						iCheckUM5Cnt = 0;
					}
					if(iCheckUM5Cnt>=3)
					{
						bUM3Flg = true;
						fHigh = list.get(cResultCheckPriceDrop.iHigh).high();
						fLow = list.get(cResultCheckPriceDrop.iLow).low();
						int iIndexH2 = StockUtils.indexHigh(list, cResultCheckPriceDrop.iLow, iCheck);
						fHigh_2 = list.get(iIndexH2).high();
						fDropRate = cResultCheckPriceDrop.fDropRatio();
						break;
					}
				}
				
				break;
			}
		}
		
		cResultDSSelectParam.bCheck = bUM3Flg;
		cResultDSSelectParam.fBuyCheck = (fLow+fHigh_2)/2;
		cResultDSSelectParam.fSellHigh = fLow + (fHigh-fLow)/2;
		cResultDSSelectParam.fSellLow = fLow*(1-0.02f);
		cResultDSSelectParam.po = -fDropRate;
		return cResultDSSelectParam;
	}
	/*
	 * ********************************************************************
	 * Test
	 * ********************************************************************
	 */
	public static void mainx(String[] args) {
		BLog.output("TEST", "--->>> MainBegin\n");
		TranEngine cTranEngine = new TranEngine();
		
		cTranEngine.setStockSet(new TranStockSet());
		cTranEngine.setSelectStockStrategy(new EStockComplexDS.StrategySelect());
		cTranEngine.setCreatePositonStrategy(new EStockComplexDS.StrategyCreate());
		cTranEngine.setClearPositonStrategy(new EStockComplexDS.StrategyClear());
		
		cTranEngine.setAccountType(TRANACCOUNTTYPE.MOCK); 
		cTranEngine.setTranMode(TRANTIMEMODE.HISTORYMOCK);
		cTranEngine.setHistoryTimeSpan("2016-01-01", "2017-01-01");
		
		cTranEngine.run();
		cTranEngine.mainLoop();
		
		BLog.output("TEST", "--->>> MainEnd\n");
	}
	
	public static void main(String[] args)
	{
		BLog.output("TEST", "Main Begin\n");
		StockDataIF cStockDataIF = new StockDataIF();
		
		String stockID = "002425"; // 300163 300165
		ResultHistoryData cResultHistoryData = 
				cStockDataIF.getHistoryData(stockID, "2016-01-01", "2017-01-01");
		List<StockDay> list = cResultHistoryData.resultList;
		BLog.output("TEST", "Check stockID(%s) list size(%d)\n", stockID, list.size());
		
		s_StockDayListCurve.setCurve(list);
		
		for(int i = 0; i < list.size(); i++)  
        {  
			StockDay cCurStockDay = list.get(i);
	
			if(cCurStockDay.date().equals("2016-08-30"))
			{
				BThread.sleep(1);
				
				ResultDSSelectParam cResultDSSelectParam = EStockComplexDS.isSelect(stockID, list, i);
				if (cResultDSSelectParam.bCheck)
				{
					BLog.output("TEST", "### CheckPoint %s\n", 
							cCurStockDay.date());
					s_StockDayListCurve.markCurveIndex(i, "S");
				}
	
			}


        } 
		
		s_StockDayListCurve.generateImage();
		BLog.output("TEST", "Main End\n");
	}
	public static StockDayListCurve s_StockDayListCurve = new StockDayListCurve("EStockComplexDS.jpg");
}
