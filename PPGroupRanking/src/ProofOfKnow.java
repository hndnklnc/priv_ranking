import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.Security;

import de.flexiprovider.core.FlexiCoreProvider;


public class ProofOfKnow implements Serializable {
	
	public Proof prove(GroupZp Zp, GroupElement x, GroupElement g){
		GroupElement r = Zp.getRandomElement();
		GroupElement W = g.raiseBy(r.getElement());
		BigInteger c = hash(W);
		BigInteger D = r.getElement().subtract(x.getElement().multiply(c));
		//D = D.mod(Zp.getP());
		Proof p = new Proof(c, D);
		return p;		
	}
	
	public boolean verify(Proof p, GroupElement g, GroupElement y){
		GroupElement a = g.raiseBy(p.D);
		GroupElement b = y.raiseBy(p.c);
		GroupElement v = a.multiplyWith(b);
		if(p.c.equals(hash(v)))
			return true;
		else
			return false;
		
	}
	
	 static BigInteger hash(final GroupElement m) {
	        Security.addProvider(new FlexiCoreProvider());
	        byte[] output = null;
	        try {
	            MessageDigest md = MessageDigest.getInstance("SHA256", "FlexiCore");
	            md.reset();
	            md.update(m.toFormattedString().getBytes());
	            output = md.digest();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return new BigInteger(output);
	    }

	
	public class Proof implements Serializable{
		private BigInteger c; //challenge
		private BigInteger D;
		
		public Proof(BigInteger c, BigInteger D){
			this.c = c;
			this.D = D;
		}
		
		public BigInteger getC() {
			return c;
		}
		public BigInteger getD() {
			return D;
		} 		
	}

}
