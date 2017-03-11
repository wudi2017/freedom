package stormstock.app.analysistest;

import java.util.List;

import stormstock.app.analysistest.EDITryPress.EDITryPressResult;
import stormstock.app.analysistest.ETDropStable.ResultXiaCuoQiWen;
import stormstock.fw.base.BLog;
import stormstock.fw.base.BThread;
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
 * 应用于选择60日内跌幅最大企稳的股票
 * 持续持有20日，设定止盈止损，轮动运行。
 */

public class EStockComplexDZCZX {
	
	public static class ResultDYCheck
	{
		public ResultDYCheck()
		{
			bCheck = false;
		}
		public boolean bCheck;
	}
	
	public static ResultDYCheck get(String stockId, List<StockDay> list, int iCheck)
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
			
		}
		
		cResultDYCheck.bCheck = true;
		return cResultDYCheck;
	}
	
	
	/*
	 * ***************************************************************************
	 * 完成策略
	 * 
	 */
	// 测试集
		public static class TranStockSet extends ITranStockSetFilter {
			@Override
			public boolean tran_stockset_byLatestStockInfo(StockInfo cStockInfo) {
//				if(cStockInfo.ID.compareTo("000000") >= 0 && cStockInfo.ID.compareTo("002000") <= 0) {	
//					
//				}
//				if(cStockInfo.circulatedMarketValue < 100.0f)
//				{
//					return true;
//				}
				return true;
				//return false;
			}
		}
		// 选股
		public static class StrategySelect extends IStrategySelect {

			@Override
			public void strategy_select(TranContext ctx, SelectResult out_sr) {
				
				String stockId = ctx.target().stock().getCurLatestStockInfo().ID;
				List<StockDay> cStockDayList = ctx.target().stock().getCurStockDayData();
				
//				ResultCheckPriceDrop cResultCheckPriceDrop = EStockDayPriceDrop.checkPriceDrop(cStockDayList, cStockDayList.size()-1);
//				if (cResultCheckPriceDrop.bCheck && cResultCheckPriceDrop.fDropRatio() < -0.1f)
//				{
//					out_sr.bSelect = true;
//					out_sr.fPriority = - cResultCheckPriceDrop.fDropAcc();
//				}
				
//				ResultComplexGFTDEx cResultComplexGFTDEx = EStockComplexGFTDEx.get_buy(stockId, cStockDayList, cStockDayList.size()-1);
//				if(cResultComplexGFTDEx.bCheck)
//				{
//					out_sr.bSelect = true;
//					out_sr.fPriority = - EStockDayPriceDrop.getMidDropParam(cStockDayList, cStockDayList.size()-1);
//				}
//				
				ResultDYCheck cResultDYCheck = EStockComplexDZCZX.get(stockId, cStockDayList, cStockDayList.size()-1);
				if(cResultDYCheck.bCheck)
				{
					out_sr.bSelect = true;
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
				
//				// 尾盘不创新低
//				if(ctx.time().compareTo("14:50:00") >= 0
//						&& ctx.time().compareTo("14:55:00")<=0 )
//				{
//					if(list_stockTime.get(list_stockTime.size()-1).price >= fYesterdayClosePrice)
//					{
//						out_sr.bCreate = true;
//						out_sr.fMaxPositionRatio = 0.2f;
//					}
//				}
				
//				out_sr.bCreate = true;
//				out_sr.fMaxPositionRatio = 0.15f;
				
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
			
			ResultDYCheck cResultDYCheck = EStockComplexDZCZX.get(stockID, list, i);
			if(cResultDYCheck.bCheck)
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
