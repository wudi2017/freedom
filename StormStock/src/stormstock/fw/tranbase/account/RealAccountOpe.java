package stormstock.fw.tranbase.account;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import stormstock.fw.base.BLog;
import stormstock.fw.base.BTypeDefine.RefFloat;
import stormstock.fw.tranbase.account.AccountPublicDef.HoldStock;
import stormstock.ori.capi.CATHSAccount;
import stormstock.ori.capi.CATHSAccount.ResultAvailableMoney;
import stormstock.ori.capi.CATHSAccount.ResultHoldStockList;
import stormstock.ori.capi.CATHSAccount.ResultTotalAssets;

public class RealAccountOpe extends IAccountOpe {

	public RealAccountOpe(String accountID, String password)
	{
		int iInitRet = CATHSAccount.initialize();
		BLog.output("ACCOUNT", " @RealAccountOpe Construct AccountID:%s Password:%s err(%d)\n", 
				accountID, password, iInitRet);
		
	}
	
	@Override
	public int newDayInit(String date, String time) {
		int iInitRet = CATHSAccount.initialize();
		BLog.output("ACCOUNT", " @RealAccountOpe newDayInit err(%d) [%s %s]\n", 
				iInitRet,
				date, time);
		return iInitRet;
	}

	@Override
	public int newDayTranEnd(String date, String time) {
		// do nothing
		return 0;
	}
	
	@Override
	public int pushBuyOrder(String date, String time, String id, int amount, float price) {
		int iBuyRet = CATHSAccount.buyStock(id, amount, price);
		BLog.output("ACCOUNT", " @RealAccountOpe pushBuyOrder err(%d) [%s %s] [%s %d %.3f %.3f] \n", 
				iBuyRet, 
				date, time,
				id, amount, price, amount*price);
		return iBuyRet;
	}

	@Override
	public int pushSellOrder(String date, String time, String id, int amount, float price) {
		int iSellRet = CATHSAccount.sellStock(id, amount, price);
		BLog.output("ACCOUNT", " @RealAccountOpe pushSellOrder err(%d) [%s %s] [%s %d %.3f %.3f] \n", 
				iSellRet, 
				date, time,
				id, amount, price, amount*price);
		return 0;
	}

	@Override
	public int getAvailableMoney(String date, String time, RefFloat out_availableMoney) {
		ResultAvailableMoney cResultAvailableMoney = CATHSAccount.getAvailableMoney();
		out_availableMoney.value = cResultAvailableMoney.availableMoney;
		BLog.output("ACCOUNT", " @RealAccountOpe getAvailableMoney err(%d) availableMoney(%.3f) \n", 
				cResultAvailableMoney.error, cResultAvailableMoney.availableMoney);
		return cResultAvailableMoney.error;
	}
	
	@Override
	public int getMoney(String date, String time, RefFloat out_totalAssets) {
		ResultHoldStockList cResultHoldStockList = CATHSAccount.getHoldStockList();
		BLog.output("ACCOUNT", " @RealAccountOpe getHoldStockList err(%d) HoldStockList size(%d) \n", 
				cResultHoldStockList.error, cResultHoldStockList.resultList.size());
		ResultTotalAssets cResultTotalAssets = CATHSAccount.getTotalAssets();
		BLog.output("ACCOUNT", " @RealAccountOpe getHoldStockList err(%d) TotalAssets(%.3f) \n", 
				cResultTotalAssets.error, cResultTotalAssets.totalAssets);
		
		if(0 == cResultHoldStockList.error && 0 == cResultTotalAssets.error)
		{
			out_totalAssets.value = cResultTotalAssets.totalAssets;
			 for(int i=0;i<cResultHoldStockList.resultList.size();i++)
	        {
	        	stormstock.ori.capi.CATHSAccount.HoldStock cHoldStock = cResultHoldStockList.resultList.get(i);
	        	out_totalAssets.value = out_totalAssets.value - cHoldStock.totalAmount * cHoldStock.curPrice;
	        }
			return 0;
		}
		else
		{
			return -98;
		}
	}
	
	@Override
	public int getHoldStockList(String date, String time, List<HoldStock> out_list) {
		
		out_list.clear();
		
		ResultHoldStockList cResultHoldStockList = CATHSAccount.getHoldStockList();
		BLog.output("ACCOUNT", " @RealAccountOpe getHoldStockList err(%d) HoldStockList size(%d) \n", 
				cResultHoldStockList.error, cResultHoldStockList.resultList.size());
		
        for(int i=0;i<cResultHoldStockList.resultList.size();i++)
        {
        	stormstock.ori.capi.CATHSAccount.HoldStock cHoldStock = cResultHoldStockList.resultList.get(i);
        	
        	HoldStock cNewItem = new HoldStock();
        	cNewItem.stockID = cHoldStock.stockID;
        	cNewItem.totalAmount = cHoldStock.totalAmount;
        	cNewItem.availableAmount = cHoldStock.availableAmount;
        	cNewItem.refPrimeCostPrice = cHoldStock.refPrimeCostPrice;
        	cNewItem.curPrice = cHoldStock.curPrice;
        	cNewItem.investigationDays = 0;

			out_list.add(cNewItem);
        }
	        
		return cResultHoldStockList.error;
		
	}
}
