package stormstock.app.analysistest;

import java.util.List;
import java.util.Random;

import stormstock.app.analysistest.EDIPricePos.ResultLongDropParam;
import stormstock.app.analysistest.EDITryPress.EDITryPressResult;
import stormstock.app.analysistest.EStockComplexDS.TranStockSet;
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
 * 早晨之星操作模型
 * 
 * @author wudi
 *
 */

public class EStockComplexDZCZX {
	
	/*
	 * ***************************************************************************
	 * 完成策略
	 * 
	 */
	// 测试集
	public static class TranStockSet extends ITranStockSetFilter {
		@Override
		public boolean tran_stockset_byLatestStockInfo(StockInfo cStockInfo) {
//			if(cStockInfo.ID.compareTo("002123") >= 0 && cStockInfo.ID.compareTo("002123") <= 0) {	
//				return true;
//			}
			if(cStockInfo.circulatedMarketValue < 100.0f)
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
			
			ResultDZCZXSelectParam cResultDZCZXSelectParam = EStockComplexDZCZX.isSelect(stockId, cStockDayList, cStockDayList.size()-1);
			if(cResultDZCZXSelectParam.bCheck)
			{
				out_sr.bSelect = true;
				
				ResultLongDropParam cResultLongDropParam = EDIPricePos.getLongDropParam(cStockDayList, cStockDayList.size()-1);
				out_sr.fPriority = -cResultLongDropParam.refHigh;
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
		
		public boolean bCheckFlg;
		public float fStarHigh;
		public float fStarLow;
		
		public void CalcParam(TranContext ctx)
		{
			this.bCheckFlg = false;
			
			String stockId = ctx.target().stock().getCurLatestStockInfo().ID;
			List<StockDay> list = ctx.target().stock().getCurStockDayData();
			int iCheck = list.size()-1;
			
			int iBegin = iCheck-5;
			int iEnd = iCheck;
			
			for(int i=iEnd;i>=iBegin;i--)
			{
				ResultDYCheck cResultDYCheck = checkZCZX(stockId,list,i);
				if(cResultDYCheck.bCheck)
				{
					this.bCheckFlg = true;
					this.fStarHigh = cResultDYCheck.fStarHigh;
					this.fStarLow = cResultDYCheck.fStarLow;
					break;
				}
			}
		}

		@Override
		public void strategy_create(TranContext ctx, CreateResult out_sr) {
			List<StockTime> list_stockTime = ctx.target().stock().getLatestStockTimeList();
			float fYesterdayClosePrice = ctx.target().stock().GetLastYesterdayClosePrice();
			float fNowPrice = ctx.target().stock().getLatestPrice();
			
//			CalcParam(ctx);
//			
//			if(bCheckFlg)
//			{
//				// 一次下跌
//				ResultXiaCuoQiWen cResultXiaCuoQiWen = ETDropStable.checkXiaCuoQiWen_2Times(list_stockTime, list_stockTime.size()-1);
//				if (cResultXiaCuoQiWen.bCheck && fNowPrice < fStarHigh)
//				{
//					//BLog.output("TEST", "     --->>> StrategyCreate %s %s \n", ctx.date(), ctx.time());
//					out_sr.bCreate = true;
//					out_sr.fMaxPositionRatio = 0.2f;
//				}
//				
//
//				// 建仓为跌幅一定时
//				float checkBuyPrice = fStarHigh - (fStarHigh - fStarLow)/3*2;
//				if(fNowPrice <= checkBuyPrice)
//				{
//					out_sr.bCreate = true;
//					out_sr.fMaxPositionRatio = 0.2f;
//				}
//			}	
			
			out_sr.fMaxPositionRatio = 0.1f;
			out_sr.bCreate = true;
		}
		@Override
		public int strategy_create_max_count() {
			return 10;
		}

	}
	// 清仓
	public static class StrategyClear extends IStrategyClear {
		
		public boolean bCheckFlg;
		public float fStarHigh;
		public float fStarLow;
		
		public void CalcParam(TranContext ctx)
		{
			this.bCheckFlg = false;
			
			String stockId = ctx.target().stock().getCurLatestStockInfo().ID;
			List<StockDay> list = ctx.target().stock().getCurStockDayData();
			int iCheck = list.size()-1;
			
			int iBegin = iCheck-5;
			int iEnd = iCheck;
			
			for(int i=iEnd;i>=iBegin;i--)
			{
				ResultDYCheck cResultDYCheck = checkZCZX(stockId,list,i);
				if(cResultDYCheck.bCheck)
				{
					this.bCheckFlg = true;
					this.fStarHigh = cResultDYCheck.fStarHigh;
					this.fStarLow = cResultDYCheck.fStarLow;
					break;
				}
			}
		}
		
		@Override
		public void strategy_clear(TranContext ctx, ClearResult out_sr) {
			List<StockTime> list_stockTime = ctx.target().stock().getLatestStockTimeList();
			float fYesterdayClosePrice = ctx.target().stock().GetLastYesterdayClosePrice();
			float fNowPrice = ctx.target().stock().getLatestPrice();
			HoldStock cHoldStock = ctx.target().holdStock();
			
			CalcParam(ctx);
			
			if(cHoldStock.investigationDays >= 30) // 调查天数控制
			{
				out_sr.bClear = true;
			}

//			float checkSellH = fStarHigh + (fStarHigh-fStarLow)/2;
//			if(cHoldStock.curPrice >= checkSellH)
//			{
//				out_sr.bClear = true;
//			}
//			
//			float checkSellL = fStarLow - (fStarHigh-fStarLow)/3;
//			if(cHoldStock.curPrice < checkSellL)
//			{
//				out_sr.bClear = true;
//			}
			
			if(cHoldStock.profitRatio() > 0.1 || cHoldStock.profitRatio() < -0.1) // 止盈止损x个点卖
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
	
	public static class ResultDYCheck
	{
		public ResultDYCheck()
		{
			bCheck = false;
		}
		public boolean bCheck;
		public float fStarHigh;
		public float fStarLow;
	}
	
	public static ResultDYCheck checkZCZX(String stockId, List<StockDay> list, int iCheck)
	{
		ResultDYCheck cResultDYCheck = new ResultDYCheck();
		
		int iBegin = iCheck-2;
		int iMid = iCheck-1;
		int iEnd = iCheck;
		if(iBegin<0)
		{
			return cResultDYCheck;
		}
		
		float fAveWave = EStockDayPriceWaveThreshold.get(list, iCheck);
		
		StockDay cCurStockDay = list.get(iEnd);
		StockDay cStockDayMid = list.get(iMid);
		StockDay cCurStockBegin = list.get(iBegin);
		
		//BLog.output("TEST", "%s fAveWave %.3f\n", cCurStockDay.date(), fAveWave);
		
		// 一天中长阴线
		{
			float shangying = cCurStockBegin.high() - cCurStockBegin.entityHigh();
			float xiaying = cCurStockBegin.entityLow() - cCurStockBegin.low();
			float shiti = cCurStockBegin.entityHigh() - cCurStockBegin.entityLow();
			float shitiRatio = shiti/cCurStockBegin.low();
			if(cCurStockBegin.open() > cCurStockBegin.close()
					&& shangying < shiti/3*2 
					&& xiaying < shiti/3*2 
					&& shitiRatio > fAveWave*0.5)
			{
				
			}
			else
			{
				return cResultDYCheck;
			}
		}

		
		// 中间横盘十字星
		{
			float shangying = cStockDayMid.high() - cStockDayMid.entityHigh();
			float xiaying = cStockDayMid.entityLow() - cStockDayMid.low();
			float shiti = cStockDayMid.entityHigh() - cStockDayMid.entityLow();
			float shitiRatio = shiti/cStockDayMid.low();
			if((shangying > shiti/3 || xiaying > shiti/3)
					&& shitiRatio < fAveWave)
			{
				
			}
			else
			{
				return cResultDYCheck;
			}
		}
		
		// 最后中长阳
		{
			float shangying = cCurStockDay.high() - cCurStockDay.entityHigh();
			float xiaying = cCurStockDay.entityLow() - cCurStockDay.low();
			float shiti = cCurStockDay.entityHigh() - cCurStockDay.entityLow();
			float shitiRatio = shiti/cCurStockDay.low();
			if(cCurStockDay.open() < cCurStockDay.close()
					&& shangying < shiti/3*2  
					&& xiaying < shiti/3*2 
					&& shitiRatio > fAveWave*0.5)
			{
				
			}
			else
			{
				return cResultDYCheck;
			}
		}
		
		// 价位控制
		{
			// 中间上影线不能超过中间值
			if(cStockDayMid.entityHigh() < cCurStockDay.midle()
					&& cStockDayMid.entityHigh() < cCurStockBegin.midle())
			{
				
			}
			else
			{
				return cResultDYCheck;
			}
			
			// 最后一天收复大部分第一天实体
			float fcheck = cCurStockBegin.entityLow() + (cCurStockBegin.entityHigh() - cCurStockBegin.entityLow())/3*2;
			if(cCurStockDay.entityHigh() > fcheck)
			{
				
			}
			else
			{
				return cResultDYCheck;
			}
		}
		
		cResultDYCheck.bCheck = true;
		cResultDYCheck.fStarHigh = cCurStockDay.entityHigh();
		cResultDYCheck.fStarLow = cCurStockDay.entityLow();
		return cResultDYCheck;
	}
	
	public static class ResultDZCZXSelectParam
	{
		public ResultDZCZXSelectParam()
		{
			bCheck = false;
		}
		public boolean bCheck;
		public float po;
	}
	public static ResultDZCZXSelectParam isSelect(String stockId, List<StockDay> list, int iCheck)
	{
		ResultDZCZXSelectParam cResultDZCZXSelectParam = new ResultDZCZXSelectParam();
		
		// 至少天数检查
		{
			int iBegin = iCheck-10;
			int iEnd = iCheck;
			if(iBegin<0)
			{
				return cResultDZCZXSelectParam;
			}
		}
		
//		// 10天之内有大跌幅
//		ResultCheckPriceDrop cResultCheckPriceDrop = new ResultCheckPriceDrop();
//		{
//			int iBegin = iCheck-10;
//			int iEnd = iCheck;
//
//			for(int i=iEnd;i>=iBegin;i--)
//			{
//				StockDay cStockDay = list.get(i);
//				cResultCheckPriceDrop = EDIPriceDrop.checkPriceDrop(list, i);
//				if (cResultCheckPriceDrop.bCheck)
//				{
////						BLog.output("TEST", "### CheckPoint %s H(%s %.2f) L(%s %.2f) Ratio(%.3f)\n", 
////								cStockDay.date(), 
////								list.get(cResultCheckPriceDrop.iHigh).date(),
////								cResultCheckPriceDrop.fHighPrice,
////								list.get(cResultCheckPriceDrop.iLow).date(),
////								cResultCheckPriceDrop.fLowPrice,
////								cResultCheckPriceDrop.fDropRatio());
//					//s_StockDayListCurve.markCurveIndex(i, "D");
//					//i=i+20;
//					break;
//				}
//			}
//		}
		
		// 5天之内有早晨之星
		boolean bZCZXFlg = false;
		{
			int iBegin = iCheck-5;
			int iEnd = iCheck;
			for(int i=iEnd;i>=iBegin;i--)
			{
				StockDay cStockDay = list.get(i);
				ResultDYCheck cResultDYCheck = checkZCZX(stockId,list,i);
				if(cResultDYCheck.bCheck)
				{
					bZCZXFlg = true;
				}
			}
		}
		
		if(bZCZXFlg)
		{
			cResultDZCZXSelectParam.bCheck = true;
			cResultDZCZXSelectParam.po = BUtilsMath.randomFloat();
		}
		
		return cResultDZCZXSelectParam;
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
		cTranEngine.setHistoryTimeSpan("2016-06-01", "2017-02-01");
		
		cTranEngine.run();
		cTranEngine.mainLoop();
		
		BLog.output("TEST", "--->>> MainEnd\n");
	}
	
	public static void mainx(String[] args)
	{
		BLog.output("TEST", "Main Begin\n");
		StockDataIF cStockDataIF = new StockDataIF();
		
		String stockID = "601566"; // 300163 300165
		ResultHistoryData cResultHistoryData = 
				cStockDataIF.getHistoryData(stockID, "2016-01-01", "2017-03-14");
		List<StockDay> list = cResultHistoryData.resultList;
		BLog.output("TEST", "Check stockID(%s) list size(%d)\n", stockID, list.size());
		
		s_StockDayListCurve.setCurve(list);
		
		for(int i = 0; i < list.size(); i++)  
        {  
			StockDay cCurStockDay = list.get(i);
			//BLog.output("TEST", "cCurStockDay %s \n", cCurStockDay.date());

			if(cCurStockDay.date().equals("2017-03-06"))
			{
				BThread.sleep(1);




			}
			ResultDYCheck cResultDYCheck = checkZCZX(stockID, list, i);
			if(cResultDYCheck.bCheck)
			{
				BLog.output("TEST", "CheckPoint %s\n", cCurStockDay.date());
				s_StockDayListCurve.markCurveIndex(i, "X");
			}
//			ResultDZCZXSelectParam cResultDZCZXSelectParam = EStockComplexDZCZX.isSelect(stockID, list, i);
//			if(cResultDZCZXSelectParam.bCheck)
//			{
//				BLog.output("TEST", "CheckPoint %s po(%.3f)\n", cCurStockDay.date(), cResultDZCZXSelectParam.po);
//				s_StockDayListCurve.markCurveIndex(i, "X");
//			}
        } 
		
		s_StockDayListCurve.generateImage();
		BLog.output("TEST", "Main End\n");
	}
	public static StockDayListCurve s_StockDayListCurve = new StockDayListCurve("EStockComplexDYCheck.jpg");
}
