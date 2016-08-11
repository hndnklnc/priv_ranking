import java.util.Random;


public class MatrixOperations {
	// sums two vector and returns new vector
		public static int [] sumTwoVector(int[] x, int[] y){
			int [] z = new int[x.length];
			if(x.length != y.length){
				System.out.println("Can not add two vector");
				return null;
			}else{
				for(int i = 0; i< x.length; i++)
					z[i] = x[i] + y[i];
				return z;
			}
		}
		
		//multiply vector x with constant c
		public static int[] multWithConst(int[] x, int c){
			int[] z = new int[x.length];
			for(int i = 0; i < x.length; i++)
				z[i] = x[i] * c;
			return z;
		}
		
		//creates leng dimentional random vector
		public static int[] randomVector(int leng){
			int[] z = new int[leng];
			Random rand = new Random(); 
			rand.setSeed(System.currentTimeMillis()); 
			for(int i = 0; i < leng; i++)
				z[i] = rand.nextInt(500);
			return z;
		}
		
		public static int[][] multMatrix(int[][] A, int[][] B){
			int[][] C = new int[A.length][B[0].length];
			for(int i = 0; i < A.length; i++){
				int [] row = A[i];
				for(int j = 0; j < B[0].length; j++)
					C[i][j] = dotProduct(row, getColumn(B, j));
			}
			return C;
			
		}
		
		public static int[] getColumn(int[][] matrix, int column){
			int[] z = new int[matrix.length];
			for(int i = 0; i < matrix.length; i++)
				z[i] = matrix[i][column];
			return z;
		}
		
		public static int dotProduct(int[] x, int[] y){
			int sum = 0;
			for(int i = 0; i < x.length; i++)
				sum += x[i] * y[i];
			return sum;
		}

}
