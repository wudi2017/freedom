package stormstock.app.analysistest;

import java.util.List;
import java.util.Random;

import stormstock.app.analysistest.EDITryPress.EDITryPressResult;
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

/**
 * 早晨之星操作模型
 * 
 * @author wudi
 *
 */

public class EStockComplexDZCZX {
	
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
					&& shangying < shiti/2 
					&& xiaying < shiti/2
					&& shitiRatio > fAveWave*0.8)
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
					&& shitiRatio < fAveWave*0.5)
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
					&& shangying < shiti/2 
					&& xiaying < shiti/2
					&& shitiRatio > fAveWave*0.8)
			{
				
			}
			else
			{
				return cResultDYCheck;
			}
		}
		
		// 价位控制
		{
			if(cStockDayMid.high() < cCurStockDay.midle()
					&& cStockDayMid.high() < cCurStockBegin.midle())
			{
				
			}
			else
			{
				return cResultDYCheck;
			}
			
			float fcheck = cCurStockBegin.entityLow() + (cCurStockBegin.entityHigh() - cCurStockBegin.entityLow())/2;
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
	
	public static boolean isSelect(String stockId, List<StockDay> list, int iCheck)
	{
		boolean bSelect = false;
		
		int iBegin = iCheck-5;
		int iEnd = iCheck;
		
		for(int i=iEnd;i>=iBegin;i--)
		{
			ResultDYCheck cResultDYCheck = checkZCZX(stockId,list,i);
			if(cResultDYCheck.bCheck)
			{
				bSelect = true;
			}
		}
		
		return bSelect;
	}
	
	/*
	 * ***************************************************************************
	 * 完成策略
	 * 
	 */
		// 选股
		public static class StrategySelect extends IStrategySelect {

			@Override
			public void strategy_select(TranContext ctx, SelectResult out_sr) {
				
				String stockId = ctx.target().stock().getCurLatestStockInfo().ID;
				List<StockDay> cStockDayList = ctx.target().stock().getCurStockDayData();
				
				boolean bCheck = EStockComplexDZCZX.isSelect(stockId, cStockDayList, cStockDayList.size()-1);
				if(bCheck)
				{
					out_sr.bSelect = true;
					out_sr.fPriority = BUtilsMath.randomFloat();
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
				
				CalcParam(ctx);
				
				if(bCheckFlg)
				{
					// 一次下跌
					ResultXiaCuoQiWen cResultXiaCuoQiWen = ETDropStable.checkXiaCuoQiWen_2Times(list_stockTime, list_stockTime.size()-1);
					if (cResultXiaCuoQiWen.bCheck && fNowPrice < fStarHigh)
					{
						//BLog.output("TEST", "     --->>> StrategyCreate %s %s \n", ctx.date(), ctx.time());
						out_sr.bCreate = true;
						out_sr.fMaxPositionRatio = 0.2f;
					}
					

					// 建仓为跌幅一定时
					float checkBuyPrice = fStarHigh - (fStarHigh - fStarLow)/3*2;
					if(fNowPrice <= checkBuyPrice)
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
				
				if(cHoldStock.investigationDays >= 20) // 调查天数控制
				{
					out_sr.bClear = true;
				}

				float checkSellH = fStarHigh + (fStarHigh-fStarLow)/2;
				if(cHoldStock.curPrice >= checkSellH)
				{
					out_sr.bClear = true;
				}
				
				float checkSellL = fStarLow - (fStarHigh-fStarLow)/3;
				if(cHoldStock.curPrice < checkSellL)
				{
					out_sr.bClear = true;
				}
				
				if(cHoldStock.profitRatio() > 0.10 || cHoldStock.profitRatio() < -0.08) // 止盈止损x个点卖
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
	public static void main(String[] args)
	{
		BLog.output("TEST", "Main Begin\n");
		StockDataIF cStockDataIF = new StockDataIF();
		
		String stockID = "002123"; // 300163 300165
		ResultHistoryData cResultHistoryData = 
				cStockDataIF.getHistoryData(stockID, "2012-01-01", "2017-01-01");
		List<StockDay> list = cResultHistoryData.resultList;
		BLog.output("TEST", "Check stockID(%s) list size(%d)\n", stockID, list.size());
		
		s_StockDayListCurve.setCurve(list);
		
		for(int i = 0; i < list.size(); i++)  
        {  
			StockDay cCurStockDay = list.get(i);
			//BLog.output("TEST", "cCurStockDay %s \n", cCurStockDay.date());

			if(cCurStockDay.date().equals("2016-06-15"))
			{
				BThread.sleep(1);



			}
			
			boolean bCheck = EStockComplexDZCZX.isSelect(stockID, list, i);
			if(bCheck)
			{
				BLog.output("TEST", "CheckPoint %s\n", cCurStockDay.date());
				s_StockDayListCurve.markCurveIndex(i, "X");
			}
        } 
		
		s_StockDayListCurve.generateImage();
		BLog.output("TEST", "Main End\n");
	}
	public static StockDayListCurve s_StockDayListCurve = new StockDayListCurve("EStockComplexDYCheck.jpg");
}
