import java.util.ArrayList;
import java.util.Random;


public class ClientComp {

	private int R1;
	private int R2;
	private int R3;
	private int b;

	static int S = 2;
	static int D;

	static int [] w;

	public ClientComp(ArrayList<Integer> answers, int g){
		int size = g + 2 * (answers.size() - g) + 1;
		D = size + 1;
		w = new int[size];
		int t = 0;
		for(int i = 0; i < g; i++){
			w[t] =  answers.get(i);
			System.out.print(w[t]+" ");
			t++;
		}
		for(int i = g; i < answers.size(); i++){
			w[t] =  answers.get(i) * answers.get(i);
			System.out.print(w[t]+" ");
			t++;
		}
		for(int i = g; i < answers.size(); i++){
			w[t] =   answers.get(i);
			System.out.print(w[t]+" ");
			t++;
		}
		w[t] = 1;
		System.out.print(w[t]+" ");
		System.out.println("w");

	}

	//creates SxS matrix that includes random numbers
	public static int[][] RandomMatrixQ() {
		int[][] randomMatrix = new int[S][S];
		Random rand = new Random(); 
		rand.setSeed(System.currentTimeMillis()); 

		for (int i = 0; i < S; i++) {   

			for (int j = 0; j < S; j++) {
				Integer r = rand.nextInt(20); 
				randomMatrix[i][j] = Math.abs(r);
			}

		}

		return randomMatrix;
	}

	public static int[][] RandomMatrixX(int r){
		int[][] randomMatrix = new int[S][D];
		Random rand = new Random(); 

		for (int i = 0; i < S; i++) {  

			for (int j = 0; j < D; j++) {
				if(i != r){
					Integer random = rand.nextInt(20); 
					randomMatrix[i][j] = Math.abs(random);
				}else{ // if it is rth row, add matrix w to this row.
					if(j != D -1)
						randomMatrix[i][j] = w[j];
					else
						randomMatrix[i][j] = 1;
				}
			}

		}

		return randomMatrix;
	}

	public  ClientSending clientCompQx(){

		Random rand = new Random(); 
		int r = rand.nextInt(S) ; //choose random place in matrix to add vector w

		int[][] Q = RandomMatrixQ();
		int[][] X = RandomMatrixX(r);

		b = 0; // b = sum Q_ir	
		for(int i = 0; i < S; i++)
			b += Q[i][r];

		int [] c = new int[D]; // c = sum (x_i.sum Q_ji)
		for(int i = 0; i < S; i++){
			if(i != r){
				int Qji = 0;
				for(int j = 0; j < S; j++)
					Qji += Q[j][i];
				c = MatrixOperations.sumTwoVector(c, MatrixOperations.multWithConst(X[i], Qji));				
			}
		}

		int[] f = MatrixOperations.randomVector(D); //d dimensional random vector

		R1 = rand.nextInt(20);
		R2 = rand.nextInt(20);
		R3 = rand.nextInt(20);

		//sends below to Alice
		int[][] QX = MatrixOperations.multMatrix(Q, X);
		int[] c_ = MatrixOperations.sumTwoVector(c, MatrixOperations.multWithConst(f, R1*R2));
		int[] g = MatrixOperations.multWithConst(f, R1*R3);

		ClientSending first = new ClientSending(QX, c_, g);
		return first;

	}


	//second phase of P0's calculation, according to Pj's answer
	public int clientCompBeta(int [] Pj){

		double beta = ((double)Pj[0] + (double)Pj[1] * ((double) R2 / (double) R3)) / (double) b;
		return (int)beta;
	}

	public static int clientCompGain(int beta, int alpha){
		return beta - alpha;
	}
}
