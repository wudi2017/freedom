package stormstock.ori.capi;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Properties;

/*
 * http://blog.csdn.net/qinjuning/article/details/7607214
 * http://blog.csdn.net/buleriver/article/details/26577895
 * 
 * ǩ���鿴��
 * http://blog.csdn.net/qq_17387361/article/details/52701481
 * ��bin�£�ִ�� javap -s stormstock.ori.capi.CATHSAccount
 * 
 * jni��C++�ļ�����
 * �ڴ���src��,ִ�� javah stormstock.ori.capi.CATHSAccount
 */
 
public class CATHSAccount {
	
	static{
		// add libpath
		String yourPath = "lib";
		System.setProperty("java.library.path", yourPath);
		Field sysPath = null;
		try {
			sysPath = ClassLoader.class.getDeclaredField("sys_paths");
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sysPath.setAccessible( true );
		try {
			sysPath.set( null, null );
		} catch (IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// load c++ API dll
		String libraryName = "AutoStockTransaction_x86";
		Properties properties = System.getProperties();
		String jvmName = properties.getProperty("java.vm.name");
		if(jvmName.contains("64"))
		{
			libraryName = "AutoStockTransaction_x64";
		}
		try {
			sysPath = ClassLoader.class.getDeclaredField("sys_paths");
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println("System.loadLibrary: " + libraryName);
		System.loadLibrary(libraryName);
    }
	
	/*
	 * **************************************************************************
	 * public class define
	 */
	
	/*
	 * �����ʽ���
	 */
	public static class ResultAvailableMoney
	{
		public ResultAvailableMoney()
		{
			error = -1;
			availableMoney = 0.0f;
		}
		public int error;
		public float availableMoney;
	}
	
	/*
	 * ���ʲ����
	 */
	public static class ResultTotalAssets
	{
		public ResultTotalAssets()
		{
			error = -1;
			totalAssets = 0.0f;
		}
		public int error;
		public float totalAssets;
	}
	
	/*
	 * �ֹ�����ֵ���
	 */
	public static class ResultAllStockMarketValue
	{
		public ResultAllStockMarketValue()
		{
			error = -1;
			allStockMarketValue = 0.0f;
		}
		public int error;
		public float allStockMarketValue;
	}
	
	/*
	 * ���׶���ö��
	 */
	public enum TRANACT 
	{
		BUY,
		SELL,
	}
	
	/*
	 * ���ճֹɶ���
	 */
	public static class HoldStock 
	{
		public String stockID; // ��ƱID
		public int totalAmount; // �����������ɣ�
		public int availableAmount; // ��������
		public float refProfitLoss; // �ο�ӯ��
		public float refPrimeCostPrice; // �ο��ɱ���
		public float curPrice; // ��ǰ��
	}
	
	/*
	 * ���չ�Ʊί�е�����
	 */
	public static class CommissionOrder 
	{
		public String time;
		public String stockID;
		public TRANACT tranAct;
		public int commissionAmount; // ί������
		public float commissionPrice; // ί�м۸�
		public int dealAmount; // �ɽ�����
		public float dealPrice; // �ɽ��۸�
	}
	
	/*
	 * ���ճɽ�������
	 */
	public static class DealOrder 
	{
		public String time;
		public String stockID;        // ��ƱID
		public TRANACT tranAct;       // ���׶���
		public int dealAmount; // �ɽ�����
		public float dealPrice; // �ɽ��۸�
	}
	
	/*
	 * �ֹ��б���
	 */
	public static class ResultHoldStockList
	{
		public ResultHoldStockList()
		{
			error = -1;
			resultList = null;
		}
		public int error;
		public List<HoldStock> resultList;
	}
	
	/*
	 * ����ί���б���
	 */
	public static class ResultCommissionOrderList
	{
		public ResultCommissionOrderList()
		{
			error = -1;
			resultList = null;
		}
		public int error;
		public List<CommissionOrder> resultList;
	}
	
	/*
	 * ���ճɽ��б���
	 */
	public static class ResultDealOrderList
	{
		public ResultDealOrderList()
		{
			error = -1;
			resultList = null;
		}
		public int error;
		public List<DealOrder> resultList;
	}
	
	/*
	 * **************************************************************************
	 * public IF define
	 */
	
	/*
	 * TongHuaShun Initialize
	 * ͬ��˳��ʼ��
	 * return ErrorCode
	 * 0 �ɹ�
	 */
	public static native int initialize();
	
	/*
	 * getAvailableMoney
	 * ��ȡ�����ʽ�
	 */
	public static native ResultAvailableMoney getAvailableMoney();
	
	/*
	 * getTotalAssets
	 * ��ȡ���ʲ�
	 */
	public static native ResultTotalAssets getTotalAssets();
	
	/*
	 * getAllStockMarketValue
	 * ��ȡ��Ʊ����ֵ
	 */
	public static native ResultAllStockMarketValue getAllStockMarketValue();
	
	/*
	 * getHoldStockList
	 * ��ȡ�ֹ��б�
	 */
	public static native ResultHoldStockList getHoldStockList();

	/*
	 * getCommissionOrderList
	 * ��ȡ����ί���б�
	 */
	public static native ResultCommissionOrderList getCommissionOrderList();

	/*
	 * getDealOrderList
	 * ��ȡ���ճɽ��б�
	 */
	public static native ResultDealOrderList getDealOrderList();
	
	/*
	 * buyStock
	 * ί�������µ�
	 * return ErrorCode
	 * 0 �ɹ�
	 */
	public static native int buyStock(String stockId, int amount, float price);
	
	/*
	 * buyStock
	 * ί�������µ�
	 * return ErrorCode
	 * 0 �ɹ�
	 */
	public static native int sellStock(String stockId, int amount, float price);
}
