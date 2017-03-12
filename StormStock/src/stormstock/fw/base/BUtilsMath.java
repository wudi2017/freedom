package stormstock.fw.base;

import java.util.Random;

public class BUtilsMath {
	// ����2λС��������������
	public static float saveNDecimal(float val, int n)
	{
		float newVal = 0.0f;
		int iScale = (int) Math.pow(10,n);
		newVal = (int)(val*iScale)/(float)iScale;
		return newVal;
	}
	
	// ��ȡ0.0-1.0֮���float���
	public static float randomFloat()
	{
		return s_random.nextFloat();
	}
	
	private static Random s_random = new Random(BUtilsDateTime.GetCurrentTimeMillis());
}
