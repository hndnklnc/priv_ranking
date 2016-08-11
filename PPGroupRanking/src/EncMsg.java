import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;



public class EncMsg implements Serializable{

	private GroupElement a; //first part of the encrypted message
	private GroupElement b; //second part of the encrypted message
	private GroupElement c;
	private EncMsg e;

	public EncMsg(GroupElement a, GroupElement b) {
		this.a = a;
		this.b = b;
		e = this;
	}

	public GroupElement getA() {
		return a;
	}

	public void setA(GroupElement a) {
		//e.setA(a);
		this.a = a;
	}

	public GroupElement getB() {
		return b;
	}

	public void setB(GroupElement b) {
		//e.setB(b);
		this.b = b;
	}
	public EncMsg addEncrypt(EncMsg e2){
		GroupElement a1 = new GroupElement(new BigInteger("0"), a.getGroup());
		GroupElement b1 = new GroupElement(new BigInteger("0"), b.getGroup());
		
		a1.setElement(a.getElement().multiply(e2.getA().getElement()));
		b1.setElement(b.getElement().multiply(e2.getB().getElement()));
		EncMsg result = new EncMsg(a1, b1);
		//result.setA(setElement(a.getElement().multiply(e2.getA().getElement())));
		//result.getB().setElement(b.getElement().multiply(e2.getB().getElement()));
		
//		a = e.getA().multiplyWith(e2.getA());
//		b = e.getB().multiplyWith(e2.getB());
//		
		return result;
	}
	
	public EncMsg substEncrypt(EncMsg e2){
		GroupElement a1 = new GroupElement(new BigInteger("0"), a.getGroup());
		GroupElement b1 = new GroupElement(new BigInteger("0"), b.getGroup());
		a1.setElement(a.getElement().divide(e2.getA().getElement()));
		b1.setElement(b.getElement().divide(e2.getB().getElement()));
		EncMsg result = new EncMsg(a1, b1);
		
//		a = e.getA().multiplyWith(e2.getA().getMultInverse());
//		b = e.getB().multiplyWith(e2.getB().getMultInverse());
		
		return result;
	}
	
	public EncMsg sclarMultEncrpt(BigInteger c){
		GroupElement a = e.getA().raiseBy(c);
		GroupElement b = e.getB().raiseBy(c);
		return new EncMsg(a, b);
	}

	public GroupElement getC() {
		return c;
	}

	public void setC(GroupElement c) {
		this.c = c;
	}

}
