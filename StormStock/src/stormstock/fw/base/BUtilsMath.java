package stormstock.fw.base;

import java.util.Random;

public class BUtilsMath {
	// 保留2位小数，非四舍五入
	public static float saveNDecimal(float val, int n)
	{
		float newVal = 0.0f;
		int iScale = (int) Math.pow(10,n);
		newVal = (int)(val*iScale)/(float)iScale;
		return newVal;
	}
	
	// 获取0.0-1.0之间的float随机
	public static float randomFloat()
	{
		return s_random.nextFloat();
	}
	
	private static Random s_random = new Random(BUtilsDateTime.GetCurrentTimeMillis());
}
