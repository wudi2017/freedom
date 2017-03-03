package stormstock.app.analysistest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import stormstock.fw.tranbase.stockdata.StockDay;

/**
 * ����ĳ�յĲο�������ֵ TSD
 * ˵����ȥ���쳣���� 60��ƽ������ֵ
 * ��ע������60�����ղ��ܼ����
 * @author wudi
 *
 */
public class EStockDayVolumeThreshold {

	public static float get(List<StockDay> list, int iCheck)
	{
		float fResult = 0.0f;
		
		// �������ȷ��
		int iBegin = iCheck-60;
		int iEnd = iCheck;
		if(iBegin<0)
		{
			iBegin = 0;
		}
		if(iEnd-iBegin<20)
		{
			return fResult;
		}
		
		//���������ȥ������Ĳ���
		List<Float> volumeList = new ArrayList<Float>();
		for(int i=iBegin; i<=iEnd; i++)
		{
			StockDay cStockDay = list.get(i);
			volumeList.add(cStockDay.volume());
		}
		if(volumeList.size()>30)
		{
		}
		else
		{
			return fResult;
		}
		Collections.sort(volumeList);
		for(int i=0; i<3; i++)
		{
			volumeList.remove(volumeList.size()-1);
		}
		for(int i=0; i<7; i++)
		{
			volumeList.remove(0);
		}
		
		// ʣ��ȡ��ֵ
		float fSum = 0.0f;
		int cnt = 0;
		for(int i=0; i<volumeList.size()-1; i++)
		{
			fSum = fSum + volumeList.get(i);
			cnt++;
		}
		fResult = fSum/cnt;
		return fResult;
	}
}
