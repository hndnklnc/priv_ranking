import java.io.Serializable;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

public class GroupElement implements Serializable {


	private static final BigInteger ZERO = new BigInteger(String.valueOf(0)); //zero as BigInteger
	private static final BigInteger ONE = new BigInteger(String.valueOf(1)); // one as BigInteger

	private BigInteger element;
	private BigInteger p;
	private GroupZp Zp;

	public GroupElement(final BigInteger b, GroupZp Zp) {
		this.Zp = Zp;
		this.p = Zp.getP();
		element = b.mod(p);
	}

	//creates a random element in spesified order
	public GroupElement(GroupZp Zp){
		this.Zp = Zp;
		p = Zp.getP();
		Random rand = new SecureRandom();
		element = new BigInteger(String.valueOf(0));
		element = element.add(p); /* set value to the value of p */
		/* value is larger or equal to p or zero, get a new value */
		while ((element.compareTo(p) > -1) | (element.equals(ZERO))) {
			/* new random value */
			element = new BigInteger(p.bitLength(), rand);
		}
	}

	public boolean isGenerator() {
		BigInteger gcd = element.gcd(p);
		// If p and element are relatively prime then the element is generator
		if(gcd.equals(ONE) && !element.equals(ONE))
			return true;
		else
			return false;
	}

	// Compare two group elements.     
	public boolean equals(final GroupElement elt) {

		return (getElement().equals(elt.getElement()));
	}


	public GroupZp getGroup(){
		return Zp;
	}

	public BigInteger getElement() {
		return element;
	}


	public void setElement(BigInteger value) {
		element = value;
	}


	public String toFormattedString() {
		return element + ",Z/Z" + p;
	}


	//adds two element in mod p
	public void addTo(final GroupElement e) {
		element = element.add(e.getElement());
		element = element.mod(p);
	}



	// multiply two elements
	public  GroupElement multiplyWith(final GroupElement e) {
		BigInteger multiply = element.multiply(e.getElement());
		return new GroupElement(multiply.mod(p), Zp);
	}

	// get inverse of the element
	final GroupElement getMultInverse() {
		GroupElement inv = new GroupElement(getElement().modInverse(p), Zp);
		return inv;
	}
	
	// get inverse of the element
		final BigInteger getAddInverse() {		
			element = element.multiply(new BigInteger(String.valueOf(-1))).add(p);
			return element;
		}

	// raise element by exponent
	final GroupElement raiseBy(final BigInteger exponent) {
		/* use BigInteger to do the work */
		return new GroupElement(element.modPow(exponent, p), Zp);
	}

	public String getBitRep(int length){
		String binary = element.toString(2);
		int dif = length - binary.length();
		String zero = "";
		for(int i = 0; i < dif; i++)
			zero += "0";
		binary = zero + binary;
		return binary;
	}

}