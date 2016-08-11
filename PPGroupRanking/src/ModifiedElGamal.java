import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

import javax.crypto.EncryptedPrivateKeyInfo;

import de.flexiprovider.common.math.codingtheory.GoppaCode;


public class ModifiedElGamal {

	private static final BigInteger ZERO = new BigInteger(String.valueOf(0)); //zero as BigInteger
	private static final BigInteger ONE = new BigInteger(String.valueOf(1)); // one as BigInteger
	private static final BigInteger TWO = new BigInteger(String.valueOf(2)); // two as BigInteger
	
	private GroupElement x; //private key
	private static GroupElement h; //public key
	private static GroupElement g; // a generator of the group
	private static GroupZp Zp; // cyclic group
	static EncMsg e;
	private int l;
	private ArrayList<EncMsg> encBits = new ArrayList<EncMsg>();
	private ArrayList<GroupElement> bits = new ArrayList<GroupElement>();

	public ModifiedElGamal(GroupZp Zp, GroupElement g){
		this.Zp = Zp;
		this.g = g;
		h = new GroupElement(Zp);
		do {
			x = new GroupElement(Zp);
		} while (!x.getElement().equals(new BigInteger(String.valueOf(1))));
		
			
		l = 5; ///clienttan al
	}
	public ModifiedElGamal(GroupZp Zp, GroupElement g, GroupElement x, GroupElement h){
		this.Zp = Zp;
		this.g = g;
		this.h = h;
		this.x = x;
		l = 5; //clienttan al
	}

	public void keyGenerate(){
		x = Zp.getRandomElement();
		h = g.raiseBy(x.getElement());
	}

	public static EncMsg encrypt(GroupElement pk, GroupElement m){
		GroupElement a = new GroupElement(Zp);
		GroupElement b  = new GroupElement(Zp);
		GroupElement r = Zp.getRandomElement();
		a = g.raiseBy(r.getElement()); //g^r
		b = g.raiseBy(m.getElement()).multiplyWith(pk.raiseBy(r.getElement())); //g^m.h^r
		e = new EncMsg(a, b);
		e.setC(new GroupElement(m.getElement().multiply(h.getElement()), Zp));
		return e;
	}

	public ArrayList<EncMsg> encryptBits(GroupElement pk, GroupElement m){
		String bitRep = m.getBitRep(l);
			
		for(int i = l - 1; i >= 0; i--){
			BigInteger bit = new BigInteger(String.valueOf(bitRep.charAt(i)));
			System.out.print(bitRep.charAt(i));
			GroupElement bitG = new GroupElement(bit, Zp);
			bits.add(bitG);
			EncMsg e = encrypt(pk, bitG);
			e.setC(new GroupElement(bit,Zp));
			encBits.add(e);
		}
		System.out.println();
		return encBits;
	}

	public GroupElement decrypt(GroupElement sk, EncMsg e){
		GroupElement a = e.getA();
		GroupElement b = e.getB();
		GroupElement divisor = a.raiseBy(sk.getElement()).getMultInverse();
		GroupElement plaintxt = new GroupElement(Zp);
		plaintxt = b.multiplyWith(divisor);
		return plaintxt;
	}

	public EncMsg thresholdDec(GroupElement xi){
		//c = b / a^xi
		GroupElement c = e.getB().multiplyWith(e.getA().getMultInverse().raiseBy(xi.getElement())); 
		//rerandomize
		GroupElement r = Zp.getRandomElement();
		e.setA(e.getA().raiseBy(r.getElement()));
		e.setB(c.raiseBy(r.getElement()));
		return e;
	}

	public GroupElement getX() {
		return x;
	}

	public GroupElement getH() {
		return h;
	}


	public ArrayList<EncMsg> compare(ArrayList<EncMsg> compBits){
		ArrayList<EncMsg> encCompResult = new ArrayList<EncMsg>();
		ArrayList<EncMsg> gammas = new ArrayList<EncMsg>();
		ArrayList<EncMsg> omegas = new ArrayList<EncMsg>();
		gammas.add(null);
		omegas.add(null);
		if(encBits.size() == 0)
			System.out.println("no msg to compare");
		else{
			for(int t = 0; t < l; t++){
				
				GroupElement EBi = compBits.get(t).getC(); //enc part of bi				
				BigInteger Bj = bits.get(t).getElement(); //compared bit
				BigInteger xor = Bj.add(EBi.getElement()).mod(TWO);
				xor = xor.add(TWO.multiply(Bj.multiply(EBi.getElement())).negate()).mod(TWO);
				System.out.println("xor:"+xor);
				gammas.add(getGamma(encBits.get(t), compBits.get(t), xor)); //creates gammas and adds list
			}
			System.out.println("____________Gammas______________");
			for(int i = 1; i < l + 1; i++)
				System.out.print(decrypt(x, gammas.get(i)).getElement());
			System.out.println("____________Omegas_______________");
			for(int t = 1; t < l + 1 ; t++){
				GroupElement lt = new GroupElement(new BigInteger(String.valueOf(l-t+1)),Zp); // l
				BigInteger binaryElem = lt.getElement().mod(new BigInteger(String.valueOf(2))); //sum mod 2
				lt.setElement(binaryElem);
				EncMsg omega2 = encrypt(h, new GroupElement(ZERO, Zp)); //initialize
				BigInteger sum = ZERO;
				if(t != l){
					for(int v = t + 1; v < l; v++){
						BigInteger first = gammas.get(t).getC().getElement().divide(h.getElement());
						BigInteger second = gammas.get(v).getC().getElement().divide(h.getElement());
						sum = sum.add(first.add(second.negate())).mod(TWO);
						omega2 = omega2.addEncrypt
								(encrypt(h, new GroupElement(sum, Zp)));							
					}
				}			
				sum = lt.getElement().add(sum.negate()).
						add(gammas.get(t).getC().getElement().divide(h.getElement()).negate()).mod(TWO);
				EncMsg omega = encrypt(h, new GroupElement(sum, Zp));
				System.out.println("sum:"+sum);
				omegas.add(omega);
			}
			for(int i = 1; i < l+1; i++)
				System.out.print(decrypt(x, omegas.get(i)).getElement()+" ");
			System.out.println("____________Result_______________");
			for(int t = 1; t < l + 1; t++){
				BigInteger ot = omegas.get(t).getC().getElement().divide(h.getElement());
				GroupElement EBi = compBits.get(t-1).getC(); //enc part of bi	
				encCompResult.add(encrypt(h, new GroupElement(EBi.getElement().add(ot).mod(TWO), Zp)));
				System.out.print(" "+ EBi.getElement().add(ot).mod(TWO));
			}
		}
		return encCompResult;
	}


	public static EncMsg getGamma(EncMsg ei, EncMsg ej, BigInteger bj){
		EncMsg gamma1 = new EncMsg(ej.getA().multiplyWith(ei.getA()), ej.getB().multiplyWith(ei.getB()));
		if(bj.equals(TWO)){
			return gamma1;
		}else{
			EncMsg gamma2 = ej.addEncrypt(ei); //without mod ei + ej
			GroupElement scalar = new GroupElement(new BigInteger("2"),Zp);
			scalar.setElement(scalar.getAddInverse());
			EncMsg gamma = ei.sclarMultEncrpt(scalar.getElement());
			//mod addition
			EncMsg try1 = new EncMsg(gamma1.getA().multiplyWith(gamma.getA()), gamma2.getB().multiplyWith(gamma.getB())); 
			scalar.setElement(scalar.getAddInverse());
			gamma = ei.sclarMultEncrpt(scalar.getElement());
			
			EncMsg try2 = gamma2.substEncrypt(gamma); // without mode division
			BigInteger b = bj.add(ZERO).mod(Zp.getP()); // creates correct element for Zp
			if(try1.getA().getElement().equals(try2.getA().getElement().mod(Zp.getP())) &&
					try1.getB().getElement().equals(try2.getB().getElement().mod(Zp.getP()))
					|| (ei.getA().getElement().equals(g) && ei.getB().getElement().equals(h))){
				EncMsg e = ModifiedElGamal.encrypt(h, new GroupElement(b, Zp));	
				return e;
			}else{
				EncMsg e = ModifiedElGamal.encrypt(h, new GroupElement(bj, Zp));	
				return e;
			}			
			
		}
		
	}
	
//	public static EncMsg getGamma(EncMsg ei, EncMsg ej, int bj){
//		//with mod ei + ej
//		EncMsg gamma1 = new EncMsg(ej.getA().multiplyWith(ei.getA()), ej.getB().multiplyWith(ei.getB()));
//		if(bj == 0){
//			return gamma1;
//		}else{
//			EncMsg gamma2 = ej.addEncrypt(ei); //without mod ei + ej
//			GroupElement scalar = new GroupElement(new BigInteger("2"),Zp);
//			scalar.setElement(scalar.getAddInverse());
//			EncMsg gamma = ei.sclarMultEncrpt(scalar.getElement());
//			//mod addition
//			EncMsg try1 = new EncMsg(gamma1.getA().multiplyWith(gamma.getA()), gamma2.getB().multiplyWith(gamma.getB())); 
//			scalar.setElement(scalar.getAddInverse());
//			gamma = ei.sclarMultEncrpt(scalar.getElement());
//			while(gamma2.substEncrypt(gamma).getA().getElement().equals(ZERO)){
//				BigInteger newA = gamma2.getA().getElement().add(Zp.getP());
//				gamma2.setA(new GroupElement(newA, Zp));
//			}
//			
//			while(gamma2.substEncrypt(gamma).getB().getElement().equals(ZERO)){
//				BigInteger newB = gamma2.getB().getElement().add(Zp.getP());
//				gamma2.setB(new GroupElement(newB, Zp));
//			}
//			EncMsg try2 = gamma2.substEncrypt(gamma); // without mode division
//			
//			//it means bi is 0
//			if(try1.getA().getElement().equals(try2.getA().getElement().mod(Zp.getP())) &&
//					try1.getB().getElement().equals(try2.getB().getElement().mod(Zp.getP()))){
//				return gamma1;
//			}else{
//				return ModifiedElGamal.encrypt(h, new GroupElement(ZERO, Zp));
//			}			
//			
//		}
//	}


}
