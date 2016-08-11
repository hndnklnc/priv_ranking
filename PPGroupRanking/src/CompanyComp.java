import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;


public class CompanyComp {
	private static int D;
	private static int alpha;
	private static int h; //h bit integer
	private int [] v;
	//w is weight, v_e is expected value, g is greater than expected values
	public CompanyComp (ArrayList<Integer> w, ArrayList<Integer> v_e, int g) {
		int sizeOfv = g + 2*(w.size() - g) + 1;
		D = sizeOfv + 1;
		v = new int[sizeOfv];
		int p = 5;
		int t = 0;
			for(int i = 0; i < g; i++){
				v[t] =  p * w.get(i);
			System.out.print(v[t]+ " ");
			t++;
			}
			for(int i = g; i < w.size(); i++){
				v[t] =  -1 * p * w.get(i);
				System.out.print(v[t]+ " ");
				t++;
			}
			for(int i = g; i < w.size(); i++){
				v[t] =  2 * p * w.get(i) * v_e.get(i);
				System.out.print(v[t]+ " ");
				t++;
			}
			v[t] = p;
			System.out.print(v[t]+ " ");
			System.out.println();

	}

	public int [] compantCompIntArr(int[][] QX, int[] c_, int[] g){

		Random rand = new Random(); 
		rand.setSeed(System.currentTimeMillis()); 
		alpha = rand.nextInt(20);

		// v_ = [v, alpha]
		int [][] v_ = new int[D][1];
		System.out.println(Arrays.deepToString(v_));
		for(int i = 0; i < D - 1; i++){
			v_[i][0] = v[i];
			System.out.println(Arrays.deepToString(v_));
		}
		v_[D-1][0] = alpha;

		int[][] y = MatrixOperations.multMatrix(QX, v_);

		int z = 0;
		for(int i = 0; i < y.length; i++)
			z += y[i][0];

		//sending Bob
		int a = z - (MatrixOperations.dotProduct(c_, MatrixOperations.getColumn(v_, 0)));
		int h = MatrixOperations.dotProduct(g, MatrixOperations.getColumn(v_, 0));
		int [] output = {a,h};
		return output;		
	}

	public static int getAlpha(){
		return alpha;
	}

}
