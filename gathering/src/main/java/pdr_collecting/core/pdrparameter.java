package pdr_collecting.core;

public class pdrparameter{
	
	
	//텍스트파일을 통해서 PDR 파라미터를 수정함 2012-12-10
	//수정될 파라미터
	//1. 보행자 걸음걸이 (SL0) Step length zero
	//2. 지도 베어링 값 
	
	
	
	
	static void inputparameter(String readtext){
		
		
		String[] tokenedStr = readtext.split(" ");
		
		pdrvariable.nominal_step_length = Integer.parseInt(tokenedStr[0].trim());
		
		
	}
	
}
/*
public class pdrparameter{
	
	
	//�ؽ�Ʈ������ ���ؼ� PDR �Ķ���͸� ������ 2012-12-10
	//������ �Ķ����
	//1. ������ �������� (SL0) Step length zero
	//2. ���� ��� �� 
	
	
	
	
	static void inputparameter(String readtext){
		
		
		String[] tokenedStr = readtext.split(" ");
		
		pdrvariable.nominal_step_length = Integer.parseInt(tokenedStr[0].trim())/10;
		
		
	}
	
}
*/