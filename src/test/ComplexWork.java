package test;

import java.util.Random;

public class ComplexWork {
	public static void main(String[] args) {
		work();
	}
	static Random rd = new Random();
	public static void work() 
	{
//		System.out.println(System.currentTimeMillis());
//		int xmax = 2000000,
//				ymax = 2000000,
//				zmax = 20000000;
//		int i=0;
//		for (int x = 0; x < xmax; x++)
//			for (int y = 0; y < ymax; y++)
//				for (int z = 0; z < zmax; z++) i++;
//		System.out.println(System.currentTimeMillis());
		for (int v = 0; v < 10; v++)
			try {
				Thread.sleep(50);
			} catch(Exception e){}
	}
}
