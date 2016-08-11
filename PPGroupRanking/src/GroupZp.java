import java.io.Serializable;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

import javax.lang.model.element.Element;



public class GroupZp implements Serializable{

	private BigInteger p; //order -1 of the group
	private static final BigInteger ZERO = new BigInteger(String.valueOf(0)); //zero as BigInteger
	private static final BigInteger ONE = new BigInteger(String.valueOf(1)); // one as BigInteger

	public GroupZp(BigInteger p){
		this.p = p;
	}

	
	public BigInteger getP(){
		return p;
	}
	
	public BigInteger getOrder(){
		return p.add(ONE);
	}
	
	final GroupElement getRandomGenerator() {
		GroupElement tmp = new GroupElement(this);
		while (!tmp.isGenerator()) {
			tmp = new GroupElement(this);
		}
		return tmp;
	} 
	
	public GroupElement getRandomElement(){
		GroupElement rand = new GroupElement(this);
		return rand;
	}
	
	//returns identity element of the Group order p + 1
	public GroupElement getIdentity() {
        return new GroupElement(new BigInteger(String.valueOf(1)), this);
    }

	public final String toString() {
        return "Z/Z" + p.toString();
    }
	
	

	

}
