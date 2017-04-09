package stormstock.app.progtran;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

public class Strategy_D1 {
	
	public static class EStockDayPriceWaveThreshold {

		public static float get(List<StockDay> list, int iCheck)
		{
			float fResultCheckPriceWave = 0.0f;
			
			// 检查区间确定
			int iBegin = iCheck-60;
			int iEnd = iCheck;
			if(iBegin<0)
			{
				iBegin = 0;
			}
			if(iEnd-iBegin<20)
			{
				return fResultCheckPriceWave;
			}
			
			//波动排序后去除过大的波动
			List<Float> waveList = new ArrayList<Float>();
			for(int i=iBegin; i<=iEnd; i++)
			{
				StockDay cStockDay = list.get(i);
				waveList.add(cStockDay.wave());
			}
			if(waveList.size()>30)
			{
			}
			else
			{
				return fResultCheckPriceWave;
			}
			Collections.sort(waveList);
			for(int i=0; i<5; i++)
			{
				waveList.remove(waveList.size()-1);
			}
			
			// 靠前20天振幅的均值
			float fSum = 0.0f;
			int cnt = 0;
			for(int i=waveList.size()-1; i>waveList.size()-20; i--)
			{
				fSum = fSum + waveList.get(i);
				cnt++;
			}
			float wave = fSum/cnt;
			
//			int iW = (int)(wave*1000)-3;
//			iW = iW / 10;
//			cResultCheckPriceWave.wave = iW/(float)100.f;

			fResultCheckPriceWave = wave;
			return fResultCheckPriceWave;
		}
	}
	

	public static class EDIPricePos {

		public static class ResultLongDropParam
		{
			public ResultLongDropParam()
			{
				bCheck = false;
			}
			public boolean bCheck;
			public float refLow;
			public float refHigh;
		}
		public static ResultLongDropParam getLongDropParam(List<StockDay> list, int iCheck)
		{
			ResultLongDropParam cResultLongDropParam = new ResultLongDropParam();
			
			int iBegin = iCheck-500;
			int iEnd = iCheck;
			if(iBegin<0)
			{
				return cResultLongDropParam;
			}
			
			StockDay cCurStockDay = list.get(iEnd);
			
			int iIndexH = StockUtils.indexHigh(list, iBegin, iEnd);
			StockDay cStockDayH = list.get(iIndexH);
			int iIndexL = StockUtils.indexLow(list, iBegin, iEnd);
			StockDay cStockDayL = list.get(iIndexL);
			
			cResultLongDropParam.refHigh = (cCurStockDay.close() - cStockDayH.close())/cStockDayH.close();
			cResultLongDropParam.refLow = (cCurStockDay.close() - cStockDayL.close())/cStockDayL.close();
			
			return cResultLongDropParam;
		}
	}
	
	public static class EDIPriceDrop {
		
		public static float getMidDropParam(List<StockDay> list, int iCheck)
		{
			int iBegin = iCheck-60;
			int iEnd = iCheck;
			if(iBegin<0)
			{
				return 0.0f;
			}
			
			StockDay cCurStockDay = list.get(iEnd);
			
			int iIndex60H = StockUtils.indexHigh(list, iBegin, iEnd);
			StockDay cStockDay60H = list.get(iIndex60H);
			int iIndex60L = StockUtils.indexLow(list, iBegin, iEnd);
			StockDay cStockDay60L = list.get(iIndex60L);
			float fMA20 = StockUtils.GetMA(list, 20, iEnd);
			
			float Pa = (cCurStockDay.close() - cStockDay60H.close())
					+ (cCurStockDay.close() - cStockDay60L.close())
					+ (cCurStockDay.close() - fMA20);
			return Pa;
		}
	
		public static class ResultPriceDrop
		{
			public ResultPriceDrop()
			{
				bCheck = false;
			}
			public boolean bCheck;
			public int iHigh;
			public float fHighPrice;
			public int iLow;
			public float fLowPrice;
			public float fDropRatio()
			{
				return (fLowPrice - fHighPrice)/fHighPrice;
			}
			public float fDropAcc()
			{
				return fDropRatio()/(iLow-iHigh+1);
			}
		}
		public static ResultPriceDrop getPriceDrop(List<StockDay> list, int iCheck)
		{
			String curDate = list.get(iCheck).date();
			float fAveWave = EStockDayPriceWaveThreshold.get(list, iCheck);
			//BLog.output("TEST", " (%s) %.4f\n", curDate, fAveWave);
			
			ResultPriceDrop cResultPriceDrop = new ResultPriceDrop();
			
			// 检查日判断(至少要有一定的交易天数)
			int iBegin = iCheck-30;
			int iEnd = iCheck;
			if(iBegin<0)
			{
				return cResultPriceDrop;
			}
			StockDay cEndStockDay = list.get(iEnd);
			
			// 急跌最高最低判断（当日下挫到短期最低）
			int checkTimes_drop = 0;
			int checkTimes_inc = 0;
			int indexCheckHigh = 0;
			for(int i = iEnd; i>iBegin+4; i--)
			{
				StockDay cStockDayI = list.get(i);
				StockDay cStockDayB1 = list.get(i-1);
				StockDay cStockDayB2 = list.get(i-2);
				StockDay cStockDayB3 = list.get(i-3);
				
				if(cStockDayI.midle() < cStockDayB3.midle())
				{
					checkTimes_drop++;
					checkTimes_inc=0;
				}
				
				if(cStockDayB3.midle() <= cStockDayI.high()
						&& cStockDayB2.midle() <= cStockDayI.high()
						&& cStockDayB1.midle() <= cStockDayI.high())
				{
					indexCheckHigh = i;
					break;
				}	
			}
			StockDay cB1StockDay = list.get(iEnd-1);
			StockDay cB2StockDay = list.get(iEnd-2);
			StockDay cHStockDay = list.get(indexCheckHigh);
			if(checkTimes_drop >= 3 
					&& cEndStockDay.close() < cB1StockDay.close()
					&& cEndStockDay.low() < cB1StockDay.low()
					&& cEndStockDay.close() < cB2StockDay.low()
					)
			{
			}
			else
			{
				return cResultPriceDrop;
			}
			//BLog.output("TEST", "(%s) %s\n", curDate, list.get(indexCheckHigh).date());
	
			
			// 最大跌幅检查（最大跌幅需要符合股性）
			float MaxDropRate = (cEndStockDay.low()-cHStockDay.high())/cHStockDay.high();
			if(MaxDropRate < -1.5*fAveWave)
			{
			}
			else
			{
				return cResultPriceDrop;
			}
			
	//		// 最后一天非跌停
	//		float fDieTingPrice = cB1StockDay.close() * 0.90f;
	//		fDieTingPrice = BUtilsMath.saveNDecimal(fDieTingPrice, 2);
	//		float fcloseCompare = BUtilsMath.saveNDecimal(cEndStockDay.close(), 2);
	//		if(Float.compare(fDieTingPrice, fcloseCompare) != 0)
	//		{
	//		}
	//		else
	//		{
	//			return cResultCheck;
	//		}
			
			// 跌速检查(跌速需要大于一定幅度)
			float fDropRate = (cEndStockDay.midle() - cHStockDay.midle())/cHStockDay.midle();
			float fDropAcc = fDropRate/(iEnd-indexCheckHigh+1);
			if(fDropAcc<-0.01f)
			{
			}
			else
			{
				return cResultPriceDrop;
			}
			//BLog.output("TEST", " (%s) fDropAcc %.4f fAveWave %.4f \n", curDate, fDropAcc, fAveWave);
			
			
			cResultPriceDrop.bCheck = true;
			cResultPriceDrop.iHigh = indexCheckHigh;
			cResultPriceDrop.fHighPrice = cHStockDay.high();
			cResultPriceDrop.iLow = StockUtils.indexLow(list, iEnd-3, iEnd);
			cResultPriceDrop.fLowPrice = list.get(cResultPriceDrop.iLow).low();
			
			
	//		// 最低点在临近日判断
	//		int iCheckForm = iCheck-10;
	//		int iCheckTo = iCheck;
	//		int indexLow = StockUtils.indexLow(list, iCheckForm, iCheckTo);
	//		StockDay cStockDayLow = list.get(indexLow);
	//		float fStockDayLow_midle = cStockDayLow.midle();
	//		if(iCheckTo - indexLow <= 5 && iCheckTo - indexLow > 0)
	//		{
	//		}
	//		else
	//		{
	//			return cResultCheck;
	//		}
	//		//BLog.output("TEST", " %d %d \n", indexHigh, indexLow);
			
	
			
	
			
			// 最高最低之间存在大阴线
			//BLog.output("TEST", "%s fAveWave %.4f\n", list.get(iCheck).date(), fAveWave);
	//		boolean bDaYin = false;
	//		for(int i = indexHigh; i<=indexLow; i++)
	//		{
	//			if(i-1<0) continue;
	//			StockDay cCheckDay = list.get(i);
	//			StockDay cCheckDayBefore = list.get(i-1);
	//			float fCheckRateOpen = (cCheckDay.close() - cCheckDay.open())/cCheckDay.open();
	//			float fCheckRateYesterday = (cCheckDay.close() - cCheckDayBefore.close())/cCheckDayBefore.close();
	//			if(fCheckRateYesterday < -fAveWave
	//					&& fCheckRateOpen < -fAveWave*0.6)
	//			{
	//				bDaYin = true;
	//			}
	//		}
	//		if(bDaYin)
	//		{
	//		}
	//		else
	//		{
	//			return cResultCheck;
	//		}
			
	//		// 当前日价格不能反弹过高
	//		StockDay cCurStockDay = list.get(iCheck);
	//		float fCurStockDay_close =  cCurStockDay.close();
	//		float CurPriceRate = (fCurStockDay_close - fStockDayLow_close)/fStockDayLow_close;
	//		CurPriceRate = Math.abs(CurPriceRate);
	//		if(CurPriceRate <= 0.03f)
	//		{
	//		}
	//		else
	//		{
	//			return cResultCheck;
	//		}
	
			return cResultPriceDrop;
		}
	}

	public static class EStockComplexDZCZX {
		
		/*
		 * ***************************************************************************
		 * 完成策略
		 * 
		 */
		// 测试集
		public static class TranStockSet extends ITranStockSetFilter {
			@Override
			public boolean tran_stockset_byLatestStockInfo(StockInfo cStockInfo) {
//				if(cStockInfo.ID.compareTo("002123") >= 0 && cStockInfo.ID.compareTo("002123") <= 0) {	
//					return true;
//				}
				if(cStockInfo.circulatedMarketValue < 1000.0f)
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
					
					EDIPricePos.ResultLongDropParam cResultLongDropParam = EDIPricePos.getLongDropParam(cStockDayList, cStockDayList.size()-1);
					out_sr.fPriority = -cResultLongDropParam.refHigh;
				}
			}

			@Override
			public int strategy_select_max_count() {
				// TODO Auto-generated method stub
				return 30;
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
				int iCheck = list.size()-2;
				
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
				
				// 跌停不买进
				float fYC = BUtilsMath.saveNDecimal(fYesterdayClosePrice, 2);
				float fDieTing = BUtilsMath.saveNDecimal(fYC*0.9f, 2);
				if(0 == Float.compare(fDieTing, fNowPrice))
				{
					out_sr.bCreate = false;
					return;
				}

				// 查找参数
				CalcParam(ctx);
				if(bCheckFlg)
				{
					// 近期涨幅过大不买进
					float fStdPaZCZX = (fStarHigh + fStarLow)/2;
					float fZhang = (fNowPrice-fStdPaZCZX)/fStdPaZCZX;
					if(fZhang > 0.08)
					{
						out_sr.bCreate = false;
						return;
					}
					
//					// 一次下跌
//					ResultXiaCuoQiWen cResultXiaCuoQiWen = ETDropStable.checkXiaCuoQiWen_2Times(list_stockTime, list_stockTime.size()-1);
//					if (cResultXiaCuoQiWen.bCheck && fNowPrice < fStarHigh)
//					{
//						//BLog.output("TEST", "     --->>> StrategyCreate %s %s \n", ctx.date(), ctx.time());
//						out_sr.bCreate = true;
//						out_sr.fMaxPositionRatio = 0.2f;
//					}
//					
	//
//					// 建仓为跌幅一定时
//					float checkBuyPrice = fStarHigh - (fStarHigh - fStarLow)/3*2;
//					if(fNowPrice <= checkBuyPrice)
//					{
//						out_sr.bCreate = true;
//						out_sr.fMaxPositionRatio = 0.2f;
//					}
				}	
				
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
				int iCheck = list.size()-2;
				
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

				// 涨停不卖出
				float fYC = BUtilsMath.saveNDecimal(fYesterdayClosePrice, 2);
				float fZhangTing = BUtilsMath.saveNDecimal(fYC*1.1f, 2);
				if(0 == Float.compare(fZhangTing, fNowPrice))
				{
					out_sr.bClear = false;
					return;
				}
				
				// 持股超时卖出
				if(cHoldStock.investigationDays >= 30) 
				{
					out_sr.bClear = true;
					return;
				}

//				float checkSellH = fStarHigh + (fStarHigh-fStarLow)/2;
//				if(cHoldStock.curPrice >= checkSellH)
//				{
//					out_sr.bClear = true;
//				}
//				
//				float checkSellL = fStarLow - (fStarHigh-fStarLow)/3;
//				if(cHoldStock.curPrice < checkSellL)
//				{
//					out_sr.bClear = true;
//				}
				
				// 止盈止损卖出
				if(cHoldStock.profitRatio() > 0.1 || cHoldStock.profitRatio() < -0.12) // 止盈止损x个点卖
				{
					out_sr.bClear = true;
					return;
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
			
//			// 10天之内有大。。。
//			ResultPriceUp cResultPriceUp = new ResultPriceUp();
//			{
//				int iBegin = iCheck-10;
//				int iEnd = iCheck;
	//
//				for(int i=iEnd;i>=iBegin;i--)
//				{
//					StockDay cStockDay = list.get(i);
//					cResultPriceUp = EDIPriceUp.getPriceUp(list, i);
//					if (cResultPriceUp.bCheck)
//					{
//						break;
//					}
//				}
//			}
			
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
			}
			
			return cResultDZCZXSelectParam;
		}
	}
}
