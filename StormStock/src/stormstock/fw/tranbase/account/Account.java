package stormstock.fw.tranbase.account;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import stormstock.fw.base.BLog;
import stormstock.fw.base.BThread;
import stormstock.fw.base.BTypeDefine.RefFloat;
import stormstock.fw.tranbase.account.AccountPublicDef.ACCOUNTTYPE;
import stormstock.fw.tranbase.account.AccountPublicDef.CommissionOrder;
import stormstock.fw.tranbase.account.AccountPublicDef.DealOrder;
import stormstock.fw.tranbase.account.AccountPublicDef.HoldStock;
import stormstock.fw.tranbase.account.AccountPublicDef.TRANACT;
import stormstock.fw.tranbase.account.AccountStore.StoreEntity;

public class Account {
	
	// �����˻�ʵ��ʱ����Ҫ��������ӿڣ�ģ�⣬��ʵ��
	public Account(ACCOUNTTYPE eAccType, String accountID, String password)
	{
		IAccountOpe cIAccountOpe = null;
		if(eAccType == ACCOUNTTYPE.MOCK)
		{
			cIAccountOpe = new MockAccountOpe(accountID, password, true);
		} 
		else if(eAccType == ACCOUNTTYPE.REAL)
		{
			cIAccountOpe = new RealAccountOpe(accountID, password);
		}
		m_cIAccountOpe = cIAccountOpe;
		m_lockedMoney = 100000.0f; // Ĭ������10w
		m_stockSelectList = new ArrayList<String>();
		m_commissionOrderList = new ArrayList<CommissionOrder>();
		m_holdStockInvestigationDaysMap = new HashMap<String, Integer>();
		m_accountStore = new AccountStore(accountID, password);

		load(); // ��������
		store(); // �洢����
	}
	
	// ***********************************************************************
	// �����ӿڣ�ֱ�ӵ��ò����ӿ�
	
	// ���տ�ʼ�˻���ʼ��
	public int newDayInit(String date, String time)
	{
		load();
		int iNewDayInit = m_cIAccountOpe.newDayInit(date, time);
		m_commissionOrderList.clear();
		store();
		return iNewDayInit;
	}
	
	public int newDayTranEnd(String date, String time)
	{
		int iNewDayTranEnd = m_cIAccountOpe.newDayTranEnd(date, time);
		
		if(0 == iNewDayTranEnd)
		{
			// ���µ�������map
			Map<String, Integer> newholdStockInvestigationDaysMap = new HashMap<String, Integer>();
			
			List<HoldStock> cHoldStockList = new ArrayList<HoldStock>();
			int iGetHoldStockList = getHoldStockList(date, time, cHoldStockList);
			if(0 == iGetHoldStockList)
			{
				for(int i=0; i<cHoldStockList.size();i++)
				{
					HoldStock cHoldStock = cHoldStockList.get(i);
					newholdStockInvestigationDaysMap.put(cHoldStock.stockID, 0);
				}
				for(Map.Entry<String, Integer> entry:newholdStockInvestigationDaysMap.entrySet()){   
					String key = entry.getKey();
					int iInvestigationDays = 0;
					if(m_holdStockInvestigationDaysMap.containsKey(key))
					{
						iInvestigationDays = m_holdStockInvestigationDaysMap.get(key);
					}
					entry.setValue(iInvestigationDays);
				} 
				for(Map.Entry<String, Integer> entry:newholdStockInvestigationDaysMap.entrySet()){   
					int iInvestigationDays = entry.getValue();
					entry.setValue(iInvestigationDays+1);
				} 
				m_holdStockInvestigationDaysMap.clear();
				m_holdStockInvestigationDaysMap.putAll(newholdStockInvestigationDaysMap);
				
				store();
			}
			else
			{
				iNewDayTranEnd = -201;
			}
		}
		
		return iNewDayTranEnd; 
	}
	
	// ������ί�У�����ʵ���µ���
	public int pushBuyOrder(String date, String time, String id, int amount, float price)
	{
		int ret = m_cIAccountOpe.pushBuyOrder(date, time, id, amount, price);
		if(0 == ret)
		{
			CommissionOrder cCommissionOrder = new CommissionOrder();
			cCommissionOrder.time = time;
			cCommissionOrder.tranAct = TRANACT.BUY;
			cCommissionOrder.stockID = id;
			cCommissionOrder.amount = amount;
			cCommissionOrder.price = price;
			m_commissionOrderList.add(cCommissionOrder);
		}
		store();
		return ret;
	}
	
	// ��������ί�У�����ʵ���µ���
	public int pushSellOrder(String date, String time, String id, int amount, float price)
	{
		int ret = m_cIAccountOpe.pushSellOrder(date, time, id, amount, price);
		if(0 == ret)
		{
			CommissionOrder cCommissionOrder = new CommissionOrder();
			cCommissionOrder.time = time;
			cCommissionOrder.tranAct = TRANACT.SELL;
			cCommissionOrder.stockID = id;
			cCommissionOrder.amount = amount;
			cCommissionOrder.price = price;
			m_commissionOrderList.add(cCommissionOrder);
		}
		store();
		return ret;
	}
	
	// ����˻������ʽ��ֽ�
	public int getLockedMoney(RefFloat out_lockedMoney)
	{
		out_lockedMoney.value = m_lockedMoney;
		return 0;
	}
	
	// ����˻������ʽ��ֽ�
	public int getAvailableMoney(RefFloat out_availableMoney)
	{
		RefFloat availableMoney= new RefFloat();
		int iRetGetAvailableMoney = m_cIAccountOpe.getAvailableMoney(availableMoney);
		
		RefFloat lockedMoney= new RefFloat();
		int iRetGetLockedMoney = this.getLockedMoney(lockedMoney);
		
		out_availableMoney.value = availableMoney.value - lockedMoney.value;
		if(out_availableMoney.value < 0)
		{
			out_availableMoney.value = 0.0f;
		}
		
		return iRetGetAvailableMoney + iRetGetLockedMoney;
	}
	
	public int getCommissionOrderList(List<CommissionOrder> out_list)
	{
		out_list.clear();
		out_list.addAll(m_commissionOrderList);
		return 0;
	}
	// �����ί���б�(δ�ɽ���)
	public List<CommissionOrder> getBuyCommissionOrderList()
	{
		List<CommissionOrder> cBuyCommissionOrderList = new ArrayList<CommissionOrder>();
		List<CommissionOrder> cCommissionOrderList = new ArrayList<CommissionOrder>();
		this.getCommissionOrderList(cCommissionOrderList);
		for(int i= 0;i<cCommissionOrderList.size();i++)
		{
			CommissionOrder cCommissionOrder = cCommissionOrderList.get(i);
			if(cCommissionOrder.tranAct == TRANACT.BUY)
			{
				CommissionOrder cNewCommissionOrder = new CommissionOrder();
				cNewCommissionOrder.CopyFrom(cCommissionOrder);
				cBuyCommissionOrderList.add(cNewCommissionOrder);
			}
		}
		return cBuyCommissionOrderList;
	}
	
	// ��óֹ��б������Ѿ����еģ��뵱���µ��ɽ��ģ�
	public int getHoldStockList(String date, String time, List<HoldStock> out_list)
	{
		int iGetHoldStockList = m_cIAccountOpe.getHoldStockList(date, time, out_list);
		if(0 == iGetHoldStockList)
		{
			for(int i=0;i<out_list.size();i++)
	        {
	        	HoldStock cHoldStock = out_list.get(i);
	        	if(m_holdStockInvestigationDaysMap.containsKey(cHoldStock.stockID))
	        	{
	        		cHoldStock.investigationDays = m_holdStockInvestigationDaysMap.get(cHoldStock.stockID);
	        	}
	        	else
	        	{
	        		cHoldStock.investigationDays = 0;
	        	}
	        }
		}
		return iGetHoldStockList;
	}
		
	// ***********************************************************************
	// ��չ�ӿڣ�����ʵ���ڻ�������֮�ϵ���չ
	
	// ѡ���б�����
	public int setStockSelectList(List<String> stockIDList)
	{
		m_stockSelectList.clear();
		for(int i=0; i<stockIDList.size();i++)
		{
			String newstockID = stockIDList.get(i);
			m_stockSelectList.add(newstockID);
		}
		
		// ѡ�����ų��Ѿ����е�
		List<HoldStock> cHoldStockList = new ArrayList<HoldStock>();
		getHoldStockList(null,null,cHoldStockList);
		for(int i=0;i<cHoldStockList.size();i++)
		{
			m_stockSelectList.remove(cHoldStockList.get(i).stockID);
		}
		
		store();
		
		return 0;
	}
	// ѡ���б��ȡ
	public int getStockSelectList(List<String> out_list)
	{
		out_list.clear();
		for(int i=0; i< m_stockSelectList.size();i++)
		{
			String stockID = m_stockSelectList.get(i);
			if(!help_inAccount(stockID))  // ѡ���б��ų����Ѿ��������б��
			{
				out_list.add(stockID);
			}
		}
		return 0;
	}
	// ���������жϹ�Ʊ�Ƿ������ ������ί���б������б���
	private boolean help_inAccount(String stockID)
	{
		List<CommissionOrder> cCommissionOrderList = new ArrayList<CommissionOrder>();
		this.getCommissionOrderList(cCommissionOrderList);
		for(int i=0;i<cCommissionOrderList.size();i++)
		{
			if(cCommissionOrderList.get(i).stockID.equals(stockID))
			{
				return true;
			}
		}
		
		List<HoldStock> cHoldStockList = new ArrayList<HoldStock>();
		this.getHoldStockList(null,null,cHoldStockList);
		for(int i=0;i<cHoldStockList.size();i++)
		{
			if(cHoldStockList.get(i).stockID.equals(stockID))
			{
				return true;
			}
		}
		
		return false;
	}
	

	
	// �����ί���б�(δ�ɽ���)
	public List<CommissionOrder> getSellCommissionOrderList()
	{
		List<CommissionOrder> cSellCommissionOrderList = new ArrayList<CommissionOrder>();
		List<CommissionOrder> cCommissionOrderList = new ArrayList<CommissionOrder>();
		this.getCommissionOrderList(cCommissionOrderList);
		for(int i= 0;i<cCommissionOrderList.size();i++)
		{
			CommissionOrder cCommissionOrder = cCommissionOrderList.get(i);
			if(cCommissionOrder.tranAct == TRANACT.SELL)
			{
				CommissionOrder cNewCommissionOrder = new CommissionOrder();
				cNewCommissionOrder.CopyFrom(cCommissionOrder);
				cSellCommissionOrderList.add(cNewCommissionOrder);
			}
		}
		return cSellCommissionOrderList;
	}
	
	// ����˻����ʲ�
	public float getTotalAssets(String date, String time) {
		
		float all_marketval = 0.0f;
		List<HoldStock> cHoldStockList = new ArrayList<HoldStock>();
		getHoldStockList(date, time, cHoldStockList);
		for(int i=0;i<cHoldStockList.size();i++)
		{
			HoldStock cHoldStock = cHoldStockList.get(i);
			all_marketval = all_marketval + cHoldStock.curPrice*cHoldStock.totalAmount;
		}
		RefFloat availableMoney = new RefFloat();
		getAvailableMoney(availableMoney);
		float all_asset = all_marketval + availableMoney.value;
		return all_asset;
	}
	
	// ���������ʽ�ѡ�ɱ���Ʊ����������
	private void load()
	{
		StoreEntity cStoreEntity = m_accountStore.load();
		if(null != cStoreEntity)
		{
			// load lockedMoney
			if(null != cStoreEntity.lockedMoney)
				m_lockedMoney = cStoreEntity.lockedMoney;
			
			// load stockSelectList
		    m_stockSelectList.clear();
		    if(null != cStoreEntity.stockSelectList)
		    	m_stockSelectList.addAll(cStoreEntity.stockSelectList);
		    
		    // 
		    m_commissionOrderList.clear();
		    if(null != cStoreEntity.commissionOrderList)
		    	m_commissionOrderList.addAll(cStoreEntity.commissionOrderList);
		    
		    // load holdStockInvestigationDaysMap
		    m_holdStockInvestigationDaysMap.clear();
			if(null != cStoreEntity.holdStockInvestigationDaysMap)
		    	m_holdStockInvestigationDaysMap.putAll(cStoreEntity.holdStockInvestigationDaysMap);
		}
	}
	// ����ѡ�ɱ�
	private void store()
	{
		StoreEntity cStoreEntity = new StoreEntity();
		// locked money
		cStoreEntity.lockedMoney = m_lockedMoney;
		// stockSelectList
		cStoreEntity.stockSelectList = m_stockSelectList;
		// commissionOrderList
		cStoreEntity.commissionOrderList = m_commissionOrderList;
		// holdStockInvestigationDaysMap
		cStoreEntity.holdStockInvestigationDaysMap = m_holdStockInvestigationDaysMap;
		m_accountStore.store(cStoreEntity);
	}
	
	public void printAccount(String date, String time)
	{
		BLog.output("ACCOUNT", "    ---ACCOUNT---INFO--- %s %s\n", date, time);
		
		RefFloat lockedMoney = new RefFloat();
		this.getLockedMoney(lockedMoney);
		float fTotalAssets = this.getTotalAssets(date, time);
		RefFloat availableMoney = new RefFloat();
		this.getAvailableMoney(availableMoney);
		List<HoldStock> cHoldStockList = new ArrayList<HoldStock>();
		this.getHoldStockList(date, time, cHoldStockList);
		
		// ��ӡ�ʲ�
		BLog.output("ACCOUNT", "    -LockedMoney: %.3f\n", lockedMoney.value);
		BLog.output("ACCOUNT", "    -TotalAssets: %.3f\n", fTotalAssets);
		BLog.output("ACCOUNT", "    -AvailableMoney: %.3f\n", availableMoney.value);
		
		// ��ӡ�ֹ�
		for(int i=0; i<cHoldStockList.size(); i++ )
		{
			HoldStock cHoldStock = cHoldStockList.get(i);
			BLog.output("ACCOUNT", "    -HoldStock: %s %d %d %.3f %.3f %.3f %d\n", 
					cHoldStock.stockID,
					cHoldStock.totalAmount, cHoldStock.availableAmount,
					cHoldStock.refPrimeCostPrice, cHoldStock.curPrice, cHoldStock.totalAmount*cHoldStock.curPrice, 
					cHoldStock.investigationDays);
		}
		
		// ��ӡί�е�
		for(int i=0; i<m_commissionOrderList.size(); i++ )
		{
			CommissionOrder cCommissionOrder = m_commissionOrderList.get(i);
			String tranOpe = "BUY"; 
			if(cCommissionOrder.tranAct == TRANACT.SELL ) tranOpe = "SELL";
				
			BLog.output("ACCOUNT", "    -CommissionOrder: %s %s %s %d %.3f\n", 
					cCommissionOrder.time, tranOpe, cCommissionOrder.stockID, 
					cCommissionOrder.amount, cCommissionOrder.price);
		}
		
		// ѡ��
		if(m_stockSelectList.size() > 0)
		{
			String logStr = "";
			logStr += String.format("    -SelectList:[ ");
			for(int i=0; i<m_stockSelectList.size(); i++ )
			{
				String stockId = m_stockSelectList.get(i);
				logStr += String.format("%s ", stockId);
				if (i >= 7 && m_stockSelectList.size()-1 > 16) {
					logStr += String.format("... ", stockId);
					break;
				}
			}
			logStr += String.format("]");
			BLog.output("ACCOUNT", "%s\n", logStr);
		}
	}

	/** **********************************************************************
	 * �˻������ӿڣ���������Ϊģ�����ʵ
	 */
	private IAccountOpe m_cIAccountOpe;
	
	private float m_lockedMoney;
	private List<String> m_stockSelectList; // ѡ���б�
	private List<CommissionOrder> m_commissionOrderList; // ί���б�
	private Map<String, Integer> m_holdStockInvestigationDaysMap; // �ֹɵ����
	private AccountStore m_accountStore;
}
