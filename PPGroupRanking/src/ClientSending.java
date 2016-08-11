import java.io.Serializable;


public class ClientSending implements Serializable {
	int[][] QX;
	int[] c;
	int[] g;
	
	public ClientSending(int[][] qX, int[] c, int[] g) {
		QX = qX;
		this.c = c;
		this.g = g;
	}
	
}

