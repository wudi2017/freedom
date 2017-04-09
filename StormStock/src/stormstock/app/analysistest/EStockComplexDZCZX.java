package stormstock.app.analysistest;

import java.util.List;
import java.util.Random;

import stormstock.app.analysistest.EDIPriceDrop.ResultPriceDrop;
import stormstock.app.analysistest.EDIPricePos.ResultLongDropParam;
import stormstock.app.analysistest.EDIPriceUp.ResultPriceUp;
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
 * �糿֮�ǲ���ģ��
 * 
 * @author wudi
 *
 */

public class EStockComplexDZCZX {
	
	/*
	 * ***************************************************************************
	 * ��ɲ���
	 * 
	 */
	// ���Լ�
	public static class TranStockSet extends ITranStockSetFilter {
		@Override
		public boolean tran_stockset_byLatestStockInfo(StockInfo cStockInfo) {
//			if(cStockInfo.ID.compareTo("601018") >= 0 && cStockInfo.ID.compareTo("601018") <= 0) {	
//				return true;
//			}
			if(cStockInfo.circulatedMarketValue < 1000.0f)
			{
				return true;
			}
			return false;
		}
	}
	// ѡ��
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
			return 30;
		}

	}
	// ����
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
			
			// ��ͣ�����
			float fYC = BUtilsMath.saveNDecimal(fYesterdayClosePrice, 2);
			float fDieTing = BUtilsMath.saveNDecimal(fYC*0.9f, 2);
			if(0 == Float.compare(fDieTing, fNowPrice))
			{
				out_sr.bCreate = false;
				return;
			}
			
			// ���Ҳ���
			CalcParam(ctx);
			if(bCheckFlg)
			{
				// �����Ƿ��������
				float fStdPaZCZX = (fStarHigh + fStarLow)/2;
				float fZhang = (fNowPrice-fStdPaZCZX)/fStdPaZCZX;
				if(fZhang > 0.08)
				{
					out_sr.bCreate = false;
					return;
				}
				
//				// 2���µ�
//				ResultXiaCuoQiWen cResultXiaCuoQiWen = ETDropStable.checkXiaCuoQiWen_2Times(list_stockTime, list_stockTime.size()-1);
//				if (cResultXiaCuoQiWen.bCheck && fNowPrice < fStarHigh)
//				{
//					//BLog.output("TEST", "     --->>> StrategyCreate %s %s \n", ctx.date(), ctx.time());
//					out_sr.bCreate = true;
//					out_sr.fMaxPositionRatio = 0.1f;
//					return;
//				}
				

//				// ����Ϊ����һ��ʱ
//				float checkBuyPrice = fStarHigh - (fStarHigh - fStarLow)/3*2;
//				if(fNowPrice <= checkBuyPrice)
//				{
//					out_sr.bCreate = true;
//					out_sr.fMaxPositionRatio = 0.1f;
//					return;
//				}
				
//				// �����µ�����
//				float fUpRate = (fNowPrice - fYesterdayClosePrice)/fYesterdayClosePrice;
//				if(fUpRate <= -0.04)
//				{
//					out_sr.bCreate = true;
//					out_sr.fMaxPositionRatio = 0.1f;
//					return;
//				}
			}	
			
			out_sr.fMaxPositionRatio = 0.1f;
			out_sr.bCreate = true;
		}
		@Override
		public int strategy_create_max_count() {
			return 10;
		}

	}
	// ���
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
			
			// ��ͣ������
			float fYC = BUtilsMath.saveNDecimal(fYesterdayClosePrice, 2);
			float fZhangTing = BUtilsMath.saveNDecimal(fYC*1.1f, 2);
			if(0 == Float.compare(fZhangTing, fNowPrice))
			{
				out_sr.bClear = false;
				return;
			}
			
			// �ֹɳ�ʱ����
			if(cHoldStock.investigationDays >= 30) 
			{
				out_sr.bClear = true;
				return;
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
			
			// ֹӯֹ������
			if(cHoldStock.profitRatio() > 0.1 || cHoldStock.profitRatio() < -0.12) // ֹӯֹ��x������
			{
				out_sr.bClear = true;
				return;
			}
		}
	}
		
		
	/*
	 * ***************************************************************************
	 * ����ϸ��
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
		
		// һ���г�����
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

		
		// �м����ʮ����
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
		
		// ����г���
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
		
		// ��λ����
		{
			// �м���Ӱ�߲��ܳ����м�ֵ
			if(cStockDayMid.entityHigh() < cCurStockDay.midle()
					&& cStockDayMid.entityHigh() < cCurStockBegin.midle())
			{
				
			}
			else
			{
				return cResultDYCheck;
			}
			
			// ���һ���ո��󲿷ֵ�һ��ʵ��
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
		
		// �����������
		{
			int iBegin = iCheck-10;
			int iEnd = iCheck;
			if(iBegin<0)
			{
				return cResultDZCZXSelectParam;
			}
		}
		
//		// 10��֮���д󡣡���
//		ResultPriceUp cResultPriceUp = new ResultPriceUp();
//		{
//			int iBegin = iCheck-10;
//			int iEnd = iCheck;
//
//			for(int i=iEnd;i>=iBegin;i--)
//			{
//				StockDay cStockDay = list.get(i);
//				cResultPriceUp = EDIPriceUp.getPriceUp(list, i);
//				if (cResultPriceUp.bCheck)
//				{
//					break;
//				}
//			}
//		}
		
		// 5��֮�����糿֮��
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
		//cTranEngine.setHistoryTimeSpan("2009-01-01", "2014-08-01");
		cTranEngine.setHistoryTimeSpan("2014-08-01", "2017-02-01");
		
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
				cStockDataIF.getHistoryData(stockID, "2010-01-01", "2011-01-01");
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
