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
		m_holdStockInvestigationDaysMap = new HashMap<String, Integer>();
		m_stockSelectList = new ArrayList<String>();
		m_accountStore = new AccountStore(accountID, password);
		
		preLoad(); // ����һ�ι�Ʊ����������
		
		load(); // ����ѡ�ɱ�
		store(); // �洢ѡ�ɱ�
	}
	
	// ***********************************************************************
	// �����ӿڣ�ֱ�ӵ��ò����ӿ�
	
	// ���տ�ʼ�˻���ʼ��
	public int newDayInit(String date, String time)
	{
		int iNewDayInit = m_cIAccountOpe.newDayInit(date, time);
		
		load();
		
		return iNewDayInit;
	}
	
	public int newDayTranEnd(String date, String time)
	{
		int iNewDayTranEnd = m_cIAccountOpe.newDayTranEnd(date, time);
		
		if(0 == iNewDayTranEnd)
		{
			// ���µ�������map
			Map<String, Integer> newHoldStockInvestigationDaysMap = new HashMap<String, Integer>();
			
			List<HoldStock> cHoldStockList = new ArrayList<HoldStock>();
			int iGetHoldStockList = getHoldStockList(date, time, cHoldStockList);
			if(0 == iGetHoldStockList)
			{
				for(int i=0; i<cHoldStockList.size();i++)
				{
					HoldStock cHoldStock = cHoldStockList.get(i);
					newHoldStockInvestigationDaysMap.put(cHoldStock.stockID, 0);
				}
				for(Map.Entry<String, Integer> entry:newHoldStockInvestigationDaysMap.entrySet()){   
					String key = entry.getKey();
					int iInvestigationDays = 0;
					if(m_holdStockInvestigationDaysMap.containsKey(key))
					{
						iInvestigationDays = m_holdStockInvestigationDaysMap.get(key);
					}
					entry.setValue(iInvestigationDays);
				} 
				for(Map.Entry<String, Integer> entry:newHoldStockInvestigationDaysMap.entrySet()){   
					int iInvestigationDays = entry.getValue();
					entry.setValue(iInvestigationDays+1);
				} 
				m_holdStockInvestigationDaysMap.clear();
				m_holdStockInvestigationDaysMap.putAll(newHoldStockInvestigationDaysMap);
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
		return m_cIAccountOpe.pushBuyOrder(date, time, id, amount, price);
	}
	
	// ��������ί�У�����ʵ���µ���
	public int pushSellOrder(String date, String time, String id, int amount, float price)
	{
		return m_cIAccountOpe.pushSellOrder(date, time, id, amount, price);
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
	
	// ���ί���б�(δ�ɽ��ģ����������������)
	public int getCommissionOrderList(List<CommissionOrder> out_list)
	{
		return m_cIAccountOpe.getCommissionOrderList(out_list);
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
	
	// ��õ��ս���б��ѳɽ��ģ���������������ģ�
	public int getDealOrderList(List<DealOrder> out_list)
	{
		return m_cIAccountOpe.getDealOrderList(out_list);
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
	
	// ����򽻸�б�(�ѳɽ���)
	public List<DealOrder> getBuyDealOrderList()
	{
		List<DealOrder> cBuyDealOrderList = new ArrayList<DealOrder>();
		List<DealOrder> cDealOrderList = new ArrayList<DealOrder>();
		getDealOrderList(cDealOrderList);
		for(int i= 0;i<cDealOrderList.size();i++)
		{
			DealOrder cDealOrder = cDealOrderList.get(i);
			if(cDealOrder.tranAct == TRANACT.BUY)
			{
				DealOrder cNewcDealOrder = new DealOrder();
				cNewcDealOrder.CopyFrom(cDealOrder);
				cBuyDealOrderList.add(cNewcDealOrder);
			}
		}
		return cBuyDealOrderList;
	}
	
	// ���������б�(�ѳɽ���)
	public List<DealOrder> getSellDealOrderList()
	{
		List<DealOrder> cSellDealOrderList = new ArrayList<DealOrder>();
		List<DealOrder> cDealOrderList = new ArrayList<DealOrder>();
		getDealOrderList(cDealOrderList);
		for(int i= 0;i<cDealOrderList.size();i++)
		{
			DealOrder cDealOrder = cDealOrderList.get(i);
			if(cDealOrder.tranAct == TRANACT.SELL)
			{
				DealOrder cNewDealOrder = new DealOrder();
				cNewDealOrder.CopyFrom(cDealOrder);
				cSellDealOrderList.add(cNewDealOrder);
			}
		}
		return cSellDealOrderList;
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
	
	// ֻ���ع�Ʊ����������
	private void preLoad()
	{
		StoreEntity cStoreEntity = m_accountStore.load();
		if(null != cStoreEntity)
		{
			// load holdStockInvestigationDaysMap
			m_holdStockInvestigationDaysMap.clear();
			if(null != cStoreEntity.initHoldStockInvestigationDaysMap)
		    	m_holdStockInvestigationDaysMap.putAll(cStoreEntity.initHoldStockInvestigationDaysMap);
		}
	}
	// ����ѡ�ɱ�
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
		m_accountStore.store(cStoreEntity);
	}
	
	public void printAccount(String date, String time)
	{
		BLog.output("ACCOUNT", "    ---ACCOUNT---INFO---\n");
		
		RefFloat lockedMoney = new RefFloat();
		this.getLockedMoney(lockedMoney);
		float fTotalAssets = this.getTotalAssets(date, time);
		RefFloat availableMoney = new RefFloat();
		this.getAvailableMoney(availableMoney);
		List<HoldStock> cHoldStockList = new ArrayList<HoldStock>();
		this.getHoldStockList(date, time, cHoldStockList);
		List<DealOrder> cDealOrderList = new ArrayList<DealOrder>();
		this.getDealOrderList(cDealOrderList);
		
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
		
		// ��ӡ�ɽ���
		for(int i=0; i<cDealOrderList.size(); i++ )
		{
			DealOrder cDealOrder = cDealOrderList.get(i);
			String tranOpe = "BUY"; 
			if(cDealOrder.tranAct == TRANACT.SELL ) tranOpe = "SELL";
				
			BLog.output("ACCOUNT", "    -DealOrder: %s %s %s %d %.3f\n", 
					cDealOrder.time, tranOpe, cDealOrder.stockID, 
					cDealOrder.amount, cDealOrder.price);
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
	private Map<String, Integer> m_holdStockInvestigationDaysMap;
	private List<String> m_stockSelectList; // ѡ���б�
	private AccountStore m_accountStore;
}
