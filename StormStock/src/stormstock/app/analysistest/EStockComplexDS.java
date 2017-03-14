package stormstock.app.analysistest;

import java.util.List;

import stormstock.app.analysistest.EDIPriceDrop.ResultPriceDrop;
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
 * DS短期下跌反弹模型
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
//			if(cStockInfo.ID.compareTo("002425") >= 0 && cStockInfo.ID.compareTo("002425") <= 0) {	
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
			String time = ctx.time();
			List<StockDay> list_stocyDay = ctx.target().stock().getCurStockDayData();
			List<StockTime> list_stockTime = ctx.target().stock().getLatestStockTimeList();
			float fYesterdayClosePrice = ctx.target().stock().GetLastYesterdayClosePrice();
			float fNowPrice = ctx.target().stock().getLatestPrice();
			
			// 昨天被选
			ResultDSSelectParam cResultDSSelectParam = EStockComplexDS.isSelect(stockId, list_stocyDay, list_stocyDay.size()-2);
			if (cResultDSSelectParam.bCheck)
			{
				StockDay cDayH = list_stocyDay.get(cResultDSSelectParam.indexH);
				StockDay cDayL = list_stocyDay.get(cResultDSSelectParam.indexL);
				StockDay cInterL = list_stocyDay.get(cResultDSSelectParam.iInterL);
				StockDay cInterH = list_stocyDay.get(cResultDSSelectParam.iInterH);
				if(time.compareTo("14:50:00") >= 0)
				{
					float fZhang = (fNowPrice-fYesterdayClosePrice)/fYesterdayClosePrice;
					if(fZhang < 0.09f && fZhang>0.0f)
					{
						int iTLow = StockUtils.indexStockTimeLow(list_stockTime, 0, list_stockTime.size()-1);
						StockTime cStockTime = list_stockTime.get(iTLow);
						if(cStockTime.price > cInterL.low()
								&& fNowPrice > cInterL.entityLow()
								&& fNowPrice < (cInterH.high() + cInterL.low())/2)
						{
							//out_sr.bCreate = true;
							//out_sr.fMaxPositionRatio = 0.2f;
						}
					}
				}
				// 日内急跌抄底
				ResultXiaCuoQiWen cResultXiaCuoQiWen = ETDropStable.checkXiaCuoQiWen_2Times(list_stockTime, list_stockTime.size()-1);
				if (cResultXiaCuoQiWen.bCheck)
				{
					//BLog.output("TEST", "     --->>> StrategyCreate %s %s \n", ctx.date(), ctx.time());
					out_sr.bCreate = true;
					out_sr.fMaxPositionRatio = 0.2f;
				}
				
//				// 建仓为跌幅一定时
//				if(fNowPrice <= cResultDSSelectParam.fBuyCheck)
//				{
//					out_sr.bCreate = true;
//					out_sr.fMaxPositionRatio = 0.2f;
//				}
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
			if(cHoldStock.investigationDays >= 5) // 调查天数控制
			{
				out_sr.bClear = true;
			}

			// 选股参数止损止盈
//			ResultDSSelectParam cResultDSSelectParam = findSelectParam(stockId, list_stocyDay, list_stocyDay.size()-1);
//			if (cResultDSSelectParam.bCheck)
//			{
//				StockDay cDayH = list_stocyDay.get(cResultDSSelectParam.indexH);
//				StockDay cDayL = list_stocyDay.get(cResultDSSelectParam.indexL);
//				StockDay cInterL = list_stocyDay.get(cResultDSSelectParam.iInterL);
//				StockDay cInterH = list_stocyDay.get(cResultDSSelectParam.iInterH);
//				
//				float checkSellH = cInterH.high()*(1+0.02f);
//				if(cHoldStock.curPrice >= checkSellH)
//				{
//					out_sr.bClear = true;
//				}
//				
//				float checkSellL = cInterL.low();
//				if(cHoldStock.curPrice < checkSellL)
//				{
//					out_sr.bClear = true;
//				}
//			}

			// 硬性指标止损止盈
			if(cHoldStock.profitRatio() > 0.05 || cHoldStock.profitRatio() < -0.05) // 止盈止损x个点卖
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
		public int indexH;
		public int indexL;
		public int iInterH;
		public int iInterL;
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
		
//		// 最后日再60日上
//		float fMA60 = StockUtils.GetMA(list, 60, iEnd);
//		StockDay cStockDayEnd = list.get(iEnd);
//		if(cStockDayEnd.close() > fMA60)
//		{
//		}
//		else
//		{
//			return cResultDSSelectParam;
//		}
		
		// 5日内存在下短期下跌安全点
		boolean bPriceSafe = false;
		int indexH = 0;
		int indexL = 0;
		float fDrop = 0.0f;
		int iInterH = 0;
		int iInterL = 0;
		for(int i=iEnd;i>=iBegin&&i>iEnd-5;i--)
		{
			ResultPriceDrop cResultPriceDrop = EDIPriceDrop.getPriceDrop(list, i);
			if (cResultPriceDrop.bCheck)
			{
				indexH=cResultPriceDrop.iHigh;
				StockDay sdH = list.get(indexH);
				indexL=cResultPriceDrop.iLow;
				StockDay sdL = list.get(indexL);
				fDrop = (sdL.low() - sdH.high())/sdL.low();
				
				iInterH = StockUtils.indexHigh(list, indexL, iCheck);
				StockDay sdInterH = list.get(iInterH);
				iInterL = StockUtils.indexLow(list, indexL, iCheck);
				StockDay sdInterL = list.get(iInterL);
				
				// 区间不能过高
				if(sdInterH.high() < (sdH.high() + sdL.low())/2
						)
				{
					bPriceSafe = true;
					break;
				}
			}
		}
		if(bPriceSafe)
		{
		}
		else
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
			ResultPriceDrop cResultPriceDrop = EDIPriceDrop.getPriceDrop(list, i);
			if (cResultPriceDrop.bCheck)
			{
				
				s_StockDayListCurve.clearMark(cResultPriceDrop.iLow);
				s_StockDayListCurve.markCurveIndex(cResultPriceDrop.iLow, "L");
				
				
				int iSelectBegin = cResultPriceDrop.iLow;
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
						fHigh = list.get(cResultPriceDrop.iHigh).high();
						fLow = list.get(cResultPriceDrop.iLow).low();
						int iIndexH2 = StockUtils.indexHigh(list, cResultPriceDrop.iLow, iCheck);
						fHigh_2 = list.get(iIndexH2).high();
						fDropRate = cResultPriceDrop.fDropRatio();
						break;
					}
				}
				
				break;
			}
		}
		
//		cResultDSSelectParam.bCheck = bUM3Flg;
//		cResultDSSelectParam.fBuyCheck = (fLow+fHigh_2)/2;
//		cResultDSSelectParam.fSellHigh = fLow + (fHigh-fLow)/2;
//		cResultDSSelectParam.fSellLow = fLow*(1-0.02f);
//		cResultDSSelectParam.po = -fDropRate;
		
		s_StockDayListCurve.clearMark(iCheck);
		s_StockDayListCurve.markCurveIndex(iCheck, "S");
		cResultDSSelectParam.bCheck = true;
		cResultDSSelectParam.indexH = indexH;
		cResultDSSelectParam.indexL = indexL;
		cResultDSSelectParam.iInterH = iInterH;
		cResultDSSelectParam.iInterL = iInterL;
		cResultDSSelectParam.po = -fDrop;

		return cResultDSSelectParam;
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
	
	public static void mainx(String[] args)
	{
		BLog.output("TEST", "Main Begin\n");
		StockDataIF cStockDataIF = new StockDataIF();
		
		String stockID = "300165"; // 300163 300165
		ResultHistoryData cResultHistoryData = 
				cStockDataIF.getHistoryData(stockID, "2016-01-01", "2017-01-01");
		List<StockDay> list = cResultHistoryData.resultList;
		BLog.output("TEST", "Check stockID(%s) list size(%d)\n", stockID, list.size());
		
		s_StockDayListCurve.setCurve(list);
		
		for(int i = 0; i < list.size(); i++)  
        {  
			StockDay cCurStockDay = list.get(i);
	
			if(cCurStockDay.date().equals("2016-09-12"))
			{
				BThread.sleep(1);
				

			}

			ResultDSSelectParam cResultDSSelectParam = EStockComplexDS.isSelect(stockID, list, i);
			if (cResultDSSelectParam.bCheck)
			{
				BLog.output("TEST", "### CheckPoint %s (%s %s) (%s %s) po(%.3f)\n", 
						cCurStockDay.date(),
						list.get(cResultDSSelectParam.indexH).date(),list.get(cResultDSSelectParam.indexL).date(),
						list.get(cResultDSSelectParam.iInterH).date(),list.get(cResultDSSelectParam.iInterL).date(),
						cResultDSSelectParam.po);
				s_StockDayListCurve.markCurveIndex(i, "S");
			}

        } 
		
		s_StockDayListCurve.generateImage();
		BLog.output("TEST", "Main End\n");
	}
	public static StockDayListCurve s_StockDayListCurve = new StockDayListCurve("EStockComplexDS.jpg");
}
