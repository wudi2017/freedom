package stormstock.fw.base;

public class BUtilsMath {
	// ����2λС��������������
	public static float saveNDecimal(float val, int n)
	{
		float newVal = 0.0f;
		int iScale = (int) Math.pow(10,n);
		newVal = (int)(val*iScale)/(float)iScale;
		return newVal;
	}
}
