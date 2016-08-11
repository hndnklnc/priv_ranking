import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;


public class Test {
	
	public static void main(final String[] args) throws IOException {
		BigInteger p = BigInteger.probablePrime(1024, new Random());
		GroupZp Zp = new GroupZp(p);
		GroupElement g = Zp.getRandomGenerator();
		System.out.println("g: "+g.getElement());
		System.out.println("p: "+p);
	//	ModifiedElGamal elgamal = new ModifiedElGamal(Zp, g);
	//	elgamal.keyGenerate();
		
		GroupElement x = Zp.getRandomElement();
		GroupElement h = g.raiseBy(x.getElement());
		System.out.println("h: "+h.getElement());
		System.out.println("x: "+x.getElement());
//		GroupElement msg = new GroupElement(new BigInteger(String.valueOf(12)), Zp);
//		System.out.println("msg:"+msg.getElement());
//		EncMsg enc = elgamal.encrypt(h, msg);
//		System.out.println("a:"+enc.getA().getElement());
//		System.out.println("b: "+enc.getB().getElement());
//		GroupElement decmsg = elgamal.decrypt(x, enc);
//		System.out.println("dec msg is "+decmsg.getElement());
//	
//		ModifiedElGamal elgamal2 = new ModifiedElGamal(Zp, g, x, h);
//		GroupElement msg2 = new GroupElement(new BigInteger(String.valueOf(10)), Zp);
//		System.out.println("msg 2:"+ msg2.getElement());
//		ArrayList<EncMsg> bits = elgamal2.encryptBits(h, msg2);
//		elgamal.encryptBits(h, msg);
//		elgamal.compare(bits);
//		
//		//test homo oprations
//		System.out.println();
//		EncMsg e2 = elgamal2.encrypt(h, msg2);
//		System.out.println("e2: a= "+ e2.getA().getElement() +" b= "+ e2.getB().getElement());
//		EncMsg result = e2.addEncrypt(enc);
//		System.out.println("Addition: a= "+ result.getA().getElement() +" b= "+ result.getB().getElement());
//		result = e2.substEncrypt(enc);
//		System.out.println("Subs: a= "+ result.getA().getElement() +" b= "+ result.getB().getElement());
//		result = e2.sclarMultEncrpt(new BigInteger(String.valueOf(2)));
//		System.out.println("Scalar: a= "+ result.getA().getElement() +" b= "+ result.getB().getElement());
		
		ProofOfKnow pok = new ProofOfKnow();
		ProofOfKnow.Proof p1 = pok.prove(Zp, x, g);
		System.out.println(pok.verify(p1, g, h));
		
	}

}
