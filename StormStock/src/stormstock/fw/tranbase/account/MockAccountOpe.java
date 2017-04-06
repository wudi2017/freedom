package stormstock.fw.tranbase.account;

import java.util.ArrayList;
import java.util.List;

import stormstock.fw.base.BLog;
import stormstock.fw.base.BTypeDefine.RefFloat;
import stormstock.fw.tranbase.account.AccountPublicDef.CommissionOrder;
import stormstock.fw.tranbase.account.AccountPublicDef.DealOrder;
import stormstock.fw.tranbase.account.AccountPublicDef.HoldStock;
import stormstock.fw.tranbase.account.AccountPublicDef.TRANACT;
import stormstock.fw.tranbase.account.MockAccountOpeStore.StoreEntity;
import stormstock.fw.tranbase.com.GlobalUserObj;
import stormstock.fw.tranbase.stockdata.StockDataIF;
import stormstock.fw.tranbase.stockdata.StockDataIF.ResultStockTime;
import stormstock.fw.tranbase.stockdata.StockDay;
import stormstock.fw.tranbase.stockdata.StockTime;

public class MockAccountOpe extends IAccountOpe {
	
	public MockAccountOpe(String accountID, String password, boolean connectFlag)
	{
		super();
		
		m_transactionCostsRatio = 0.0016f;
		
		m_accountID = accountID;
		m_password = password;
		
		m_dataSyncFlag = connectFlag;
		m_mockAccountOpeStore = new MockAccountOpeStore(m_accountID, m_password);

		// 交易流数据
		{
			m_money = 200000.00f; // 模拟账户默认20w
			m_holdStockList = new ArrayList<HoldStock>();
			
			//load
			load();
			
			//store
			store();
		}

		BLog.output("ACCOUNT", " @MockAccountOpe Construct AccountID:%s Password:%s money:%.2f transactionCostsRatio:%.4f\n", 
				m_accountID, password, m_money, m_transactionCostsRatio);
	}
	
	@Override
	public int newDayInit(String date, String time) 
	{
		return 0; 
	}

	@Override
	public int newDayTranEnd(String date, String time) {
		
		// 所有持股均可卖
		HoldStock cHoldStock = null;
		for(int i = 0; i< m_holdStockList.size(); i++)
		{
			cHoldStock = m_holdStockList.get(i);
			cHoldStock.availableAmount = cHoldStock.totalAmount;
		}
		
		// 更新现价
		List<HoldStock> list = new ArrayList<HoldStock>();
		getHoldStockList(date, time, list); 
		store(); // 保存
		return 0;
	}
	
	@Override
	public int pushBuyOrder(String date, String time, String stockID, int amount, float price) {
		
		// 买入量标准化
		int maxBuyAmount = (int)(m_money/price);
		int realBuyAmount = Math.min(maxBuyAmount, amount);
		realBuyAmount = realBuyAmount/100*100; 
		if(realBuyAmount <=0 ) 
		{
			return 0;
		}
		
		// 获取持有对象
		HoldStock cHoldStock = null;
		for(int i = 0; i< m_holdStockList.size(); i++)
		{
			HoldStock cTmpHoldStock = m_holdStockList.get(i);
			if(cTmpHoldStock.stockID == stockID)
			{
				cHoldStock = cTmpHoldStock;
				break;
			}
		}
		if(null == cHoldStock)
		{
			HoldStock cNewHoldStock = new HoldStock();
			cNewHoldStock.stockID = stockID;
			cNewHoldStock.curPrice = price;
			m_holdStockList.add(cNewHoldStock);
			cHoldStock = cNewHoldStock;
		}
		
		// 重置对象 (交易费用直接体现在参考成本价里)
		float transactionCosts = m_transactionCostsRatio*price*realBuyAmount;
		int oriTotalAmount = cHoldStock.totalAmount;
		float oriHoldAvePrice = cHoldStock.refPrimeCostPrice;
		cHoldStock.totalAmount = cHoldStock.totalAmount + realBuyAmount;
		cHoldStock.refPrimeCostPrice = (oriHoldAvePrice*oriTotalAmount + price*realBuyAmount + transactionCosts)/cHoldStock.totalAmount;
		cHoldStock.curPrice = price;
		
		m_money = m_money - realBuyAmount*price;
		
		BLog.output("ACCOUNT", " @MockAccountOpe pushBuyOrder [%s %s] [%s %d %.3f %.3f(%.3f) %.3f] \n", 
				date, time,
				stockID, realBuyAmount, price, realBuyAmount*price, transactionCosts, m_money);
		
		store();
		
		return 0;
	}

	@Override
	public int pushSellOrder(String date, String time, String stockID, int amount, float price) {
		
		// 获取持有对象
		HoldStock cHoldStock = null;
		for(int i = 0; i< m_holdStockList.size(); i++)
		{
			HoldStock cTmpHoldStock = m_holdStockList.get(i);
			if(cTmpHoldStock.stockID.equals(stockID))
			{
				cHoldStock = cTmpHoldStock;
				break;
			}
		}
		
		if(null != cHoldStock)
		{
			// 卖出量标准化
			int realSellAmount = Math.min(cHoldStock.totalAmount, amount);
			realSellAmount = realSellAmount/100*100;
			
			// 重置对象 (交易费用在卖出价钱中扣除)
			float transactionCosts = m_transactionCostsRatio*price*realSellAmount;
			int oriTotalAmount = cHoldStock.totalAmount;
			float oriHoldAvePrice = cHoldStock.refPrimeCostPrice;
			cHoldStock.totalAmount = cHoldStock.totalAmount - realSellAmount;
			cHoldStock.curPrice = price;
			m_money = m_money + price*realSellAmount - transactionCosts;
			if(cHoldStock.totalAmount == 0) // 卖光则不计算买入价格 清零
			{
				cHoldStock.refPrimeCostPrice = 0.0f;
			}
			else
			{
				cHoldStock.refPrimeCostPrice = (oriHoldAvePrice*oriTotalAmount - price*realSellAmount - transactionCosts)/cHoldStock.totalAmount;
			}
			
			// 清仓计算
			if(cHoldStock.totalAmount == 0)
			{
				m_holdStockList.remove(cHoldStock);
			}
			
			BLog.output("ACCOUNT", " @MockAccountOpe pushSellOrder [%s %s] [%s %d %.3f %.3f(%.3f) %.3f] \n", 
					date, time,
					stockID, realSellAmount, price, realSellAmount*price, transactionCosts, m_money);

			store();
			
			return 0;
		}
	
		return 0;
	}

	@Override
	public int getAvailableMoney(String date, String time, RefFloat out_availableMoney) {
		out_availableMoney.value = m_money;
		return 0;
	}
	
	@Override
	public int getMoney(String date, String time, RefFloat out_money) {
		out_money.value = m_money;
		return 0;
	}
	
	@Override
	public int getHoldStockList(String date, String time, List<HoldStock> out_list) {
		// 当有日期时间参数时，更新持股现价
		if(null != date && null != time)
		{
			for(int i = 0; i< m_holdStockList.size(); i++)
			{
				HoldStock cHoldStock = m_holdStockList.get(i);
				ResultStockTime cResultStockTime = GlobalUserObj.getCurStockDataIF().getStockTime(cHoldStock.stockID, date, time);
				if(0 == cResultStockTime.error)
				{
					cHoldStock.curPrice = cResultStockTime.stockTime().price;
				}
			}
		}
		out_list.addAll(m_holdStockList);
		return 0;
	}
	
	private void load()
	{
		if(m_dataSyncFlag)
		{
			StoreEntity cStoreEntity = m_mockAccountOpeStore.load();
			if(null != cStoreEntity)
			{
			    m_money = cStoreEntity.money;
			    m_holdStockList.clear();
			    m_holdStockList.addAll(cStoreEntity.holdStockList);
			}
		}
	}
	
	private void store()
	{
		if(m_dataSyncFlag)
		{
			StoreEntity cStoreEntity = new StoreEntity();
			cStoreEntity.money = m_money;
			cStoreEntity.holdStockList = m_holdStockList;
			m_mockAccountOpeStore.store(cStoreEntity);
		}
	}

	/**
	 * 成员-----------------------------------------------------------------------
	 */

	private float m_transactionCostsRatio;
	private String m_accountID;
	private String m_password;
	
	private boolean m_dataSyncFlag;
	private MockAccountOpeStore m_mockAccountOpeStore;
	
	private float m_money;
	private List<HoldStock> m_holdStockList;
}
