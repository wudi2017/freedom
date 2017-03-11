package stormstock.fw.base;

public class BUtilsMath {
	// 保留2位小数，非四舍五入
	public static float saveNDecimal(float val, int n)
	{
		float newVal = 0.0f;
		int iScale = (int) Math.pow(10,n);
		newVal = (int)(val*iScale)/(float)iScale;
		return newVal;
	}
}
