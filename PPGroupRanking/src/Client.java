import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream.GetField;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;

import swing2swt.layout.BorderLayout;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.OpenAndLinkWithEditorHelper;

import com.ibm.icu.text.SpoofChecker;

import de.flexiprovider.core.elgamal.ElGamal;


public class Client implements Serializable {

	protected static Shell shell;
	private static Text q1txt;
	private static Text q2txt;
	private static Text q3txt;
	private static Text q4txt;
	private static Text q5txt;
	private static Text ans1txt;
	private static Text ans2txt;
	private static Text ans3txt;
	private static Button ans5YesBtn;
	private static Button ans4NoBtn;
	private static Button ans4YesBtn;
	private static Button ans5NoBtn;

	private static Text situText;
	private static Text keyText;
	private static Text gaintext;
	private static Text text;


	private static ArrayList<Integer> ansList = new ArrayList<Integer>();
	private static HashMap<Integer, Client> clientList = new HashMap<Integer,Client>();
	private static ArrayList<Client> clientArrLst = new ArrayList<Client>();
	private ArrayList<EncMsg> encMsgs = new ArrayList<EncMsg>();
	private ArrayList<EncMsg> encCompareResult = new ArrayList<EncMsg>();

	private static boolean isAnswered = false;
	private static boolean isKeyGenerated = false;
	String name;
	public int socketNum;
	public static int socketNumSt;
	static Socket socket;
	static Socket socketError;
	public static int l;
	private GroupElement xi;
	private int gain;
	public boolean ischosen = false;
	public static GroupZp Zp;
	public static GroupElement g;
	private static String log = "";
	public GroupElement hi;
	public int order;
	static boolean isFinished = false;
	static int msgCount = 0;
	private static ArrayList<Client> clientKeyList = new ArrayList<Client>();
	private boolean isError = false;
	public boolean chosen;
	public static List list;

	public Client(String name, int socketNum){
		this.name = name;
		this.setSocketNum(socketNum);
		isError = false;
	}

	public Client(String name, int socketNum, GroupElement hi){
		this.name = name;
		this.setSocketNum(socketNum);
		isError = false;
		this.hi = hi;
	}

	public static void main(String[] args) throws InterruptedException {
		Random rand = new Random(); 		
		rand.setSeed(System.currentTimeMillis()); 
		
		String name = null;
		int port;
		try {
			name = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		do{
			port = rand.nextInt(65000);
		} while(port < 7000);

		Client c = new Client(name,port);

		socketNumSt  = port;
		
		new Screen().start();
		Thread.sleep(2000);
		try {
			socket = new Socket("", 9898);
			ObjectOutputStream objOut = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream objIn = new ObjectInputStream(socket.getInputStream());
			int count = 1;
			

			int k = (int) objIn.readObject(); //reads ranking critaria
			
			//			Thread error = new Thread(errorhandle);
			//			error.start();
			log = count+ ") receive "+k+" from Company \n";
			addList(log);
			if(k != 0){//means accepted
				objOut.writeObject(port); //sends client info
				count++;
				log = count+ ") send "+port+" to Company \n";
				ArrayList<String> questions = (ArrayList<String>) objIn.readObject();
				addList(log);
				Zp = (GroupZp) objIn.readObject();

				g = (GroupElement) objIn.readObject();
				count++;
				log = count+ ") receive "+g+" from Company \n";
				addList(log);
				l = (int) objIn.readObject();
				count++;
				log = count+ ") receive "+l+" from Company \n";
				addList(log);
				System.out.println(log);
				for(int i = 0; i < questions.size(); i++)
					System.out.println(questions.get(i));
				count++;
				log = count+ ") receive questions from Company \n";
				addList(log);
				writeQuestions(questions);
				System.out.println("port num:"+port);
				while(!isAnswered)
					Thread.sleep(2000);

				ClientComp comp = new ClientComp(ansList, 3); //creates his answers as a vector
				ClientSending sendCompany = comp.clientCompQx();
				count++;
				log = count+ ") send "+Arrays.deepToString(sendCompany.QX)+" to Company \n";
				addList(log);
				count++;
				log = count+ ") send "+Arrays.toString(sendCompany.c)+" to Company \n";
				addList(log);
				count++;
				log = count+ ") send "+Arrays.toString(sendCompany.g)+" to Company \n";
				addList(log);
				System.out.println(log);
				objOut.writeObject(sendCompany); //sends vector calculation
				int[] companyComp = (int[]) objIn.readObject(); //receives last comp values
				int alpha = (int) objIn.readObject(); //receives alpha
				count++;
				log = count+ ") receive "+Arrays.toString(companyComp)+" from Company \n";
				addList(log);
				c.gain = comp.clientCompGain(comp.clientCompBeta(companyComp), alpha);
				System.out.println("gain:"+c.gain);
				updateGain(c.gain + "");
				updateSituation("Gain Calculated");
				clientArrLst = (ArrayList<Client>) objIn.readObject(); //receives clientList
				count++;
				log = count+ ") receive client List from Company \n";
				addList(log);
				System.out.println("listeyi aldým");
				updateSituation("All Clients arrieve");
				//according to client's socket number clients are added
				for(int i = 0; i < clientArrLst.size(); i++){
					System.out.println(clientArrLst.get(i).name+" "+clientArrLst.get(i).getSocketNum());
					clientList.put(clientArrLst.get(i).getSocketNum(), clientArrLst.get(i));
				}
				clientList.get(c.getSocketNum()).setGain(c.gain);
				objOut.writeObject(clientList);

				/*
				 * Interaction with server ends for now. He begins to interact with other clients
				 */

				while(!isKeyGenerated){
					Thread.sleep(2000);//wait until button
					System.out.println("wait:"+isKeyGenerated);
				}
				System.out.println("iskeyGen:"+isKeyGenerated);
				c.xi = Zp.getRandomElement(); //chooses his private key
				updateKey(c.xi.getElement().toString());
				c.hi = g.raiseBy(c.xi.getElement()); //partial public key
				System.out.println("gen hi :"+c.hi);
				ProofOfKnow pok = new ProofOfKnow();
				ProofOfKnow.Proof proof = pok.prove(Zp, c.xi, g); //generates proof that shows he knows xi
				objOut.writeObject(proof);
				objOut.writeObject(c.hi); //sends partial public key
				
				count++;
				log = count+ ") send "+c.hi.getElement()+ " to Clients \n";
				addList(log);
				count++;
				log = count+ ") send "+proof.getC()+ " to Clients \n";
				addList(log);
				count++;
				log = count+ ") send "+proof.getD()+ " to Clients \n";
				addList(log);
				updateSituation("Enc Keys");
				System.out.println("key xi:"+c.xi.getElement()+" hi:"+c.hi.getElement());

				clientList = (HashMap<Integer, Client>) objIn.readObject(); //reads enc keys of other clients
				Iterator<Integer> i = clientList.keySet().iterator();
				while(i.hasNext()){
					int key = i.next();
					System.out.println(clientList.get(key)+ "enc  bitgain:"+ clientList.get(key).getGain());
				}

				System.out.println("Other's key arrieve");

				GroupElement h = c.hi; //creates joint public key
				for(int j = 0; j< clientKeyList.size(); j++)
					h = h.multiplyWith(clientKeyList.get(j).hi);

				ModifiedElGamal elgamal = new ModifiedElGamal(Zp, g, c.xi, h);
				//encrypts each bits of his gain
				ArrayList<EncMsg> encBits = elgamal.encryptBits(h, new GroupElement(new BigInteger(String.valueOf(c.gain)), Zp));
				objOut.writeObject(encBits);
				for(int j = 0; j < encBits.size(); j++)
				{
					count++;
					log = count+ ") send ("+encBits.get(j).getA().getElement()+ ","+ encBits.get(j).getB().getElement()+" ) to clients \n";
					addList(log);
				}
				System.out.println("send enc bits");
				clientList = (HashMap<Integer, Client>) objIn.readObject(); //reads encrypted keys of others
				i = clientList.keySet().iterator();
				while(i.hasNext()){
					int key = i.next();
					System.out.println(clientList.get(key)+ "enc gain:"+ clientList.get(key).getGain());
				}
				 i = clientList.keySet().iterator();
				while(i.hasNext()){
					int key = i.next();
					ArrayList<EncMsg> encComBits =  clientList.get(key).getEncMsgs();		
					for(int j = 0; j < encComBits.size(); j++){
						count++;
						log = count+ ") receives ("+encComBits.get(j).getA().getElement()+ ","+ encComBits.get(j).getB().getElement()+" ) from clients \n";
						addList(log);
					}
				}

				c.setEncCompareResult(elgamal.compare(encBits)); // makes comparision with encryption
				objOut.writeObject(c.getEncCompareResult()); //send result
				updateSituation("Enc Comparison arrieve");
				for(int j = 0; j < c.getEncCompareResult().size(); j++)
				{
					count++;
					log = count+ ") send ("+c.getEncCompareResult().get(j).getA().getElement()+ ","+ c.getEncCompareResult().get(j).getB().getElement()+" ) to clients \n";
					addList(log);
				}
				clientList = (HashMap<Integer, Client>) objIn.readObject(); //reads others' partial decryption
				i = clientList.keySet().iterator();
				while(i.hasNext()){
					int key = i.next();
					System.out.println(clientList.get(key)+ "partial gain:"+ clientList.get(key).getGain());
				}
				i = clientList.keySet().iterator();
				while(i.hasNext()){
					int key = i.next();
					ArrayList<EncMsg> encComBits =  clientList.get(key).getEncCompareResult();		
					for(int j = 0; 0 < encComBits.size(); j++){
						count++;
						log = count+ ") receives ("+encComBits.get(j).getA().getElement()+ ","+ encComBits.get(j).getB().getElement()+" ) from clients \n";
						addList(log);
					}
				}
				c.PartialDecrypt();
				objOut.writeObject(clientList);

				i = clientList.keySet().iterator();
				while(i.hasNext()){
					int key = i.next();
					ArrayList<EncMsg> encComBits =  clientList.get(key).getEncCompareResult();		
					for(int j = 0; 0 < encComBits.size(); j++){
						count++;
						log = count+ ") sends ("+encComBits.get(j).getA().getElement()+ ","+ encComBits.get(j).getB().getElement()+" ) to clients \n";
						addList(log);
					}
				}
				updateSituation("Partial Decrypt");

				clientList = (HashMap<Integer, Client>) objIn.readObject(); //reads final
				i = clientList.keySet().iterator();
				while(i.hasNext()){
					int key = i.next();
					System.out.println(clientList.get(key)+ "result gain:"+ clientList.get(key).getGain());
				}
				i = clientList.keySet().iterator();
				while(i.hasNext()){
					int key = i.next();
					ArrayList<EncMsg> encComBits =  clientList.get(key).getEncCompareResult();		
					for(int j = 0; 0 < encComBits.size(); j++){
						count++;
						log = count+ ") receives ("+encComBits.get(j).getA().getElement()+ ","+ encComBits.get(j).getB().getElement()+" ) from clients \n";
						addList(log);
					}
				}
				System.out.println("okudum");

				updateSituation("Calculate Result");
				
				i = clientList.keySet().iterator();
				while(i.hasNext()){
					int key = i.next();
					System.out.println(clientList.get(key)+ " gain:"+ clientList.get(key).getGain());
				}

				int rank = c.getRank();
				if(rank == 1)
					objOut.writeObject(0);
				else
					objOut.writeObject(rank);
				updateSituation("Result");
				updateRank(rank + "");
				
			}else{//not accepted
				updateDisplay();
			}
			
			
			



		} catch (IOException | ClassNotFoundException | InterruptedException e) {
			cancelMsg(port);
			e.printStackTrace();

		}
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
			cancelMsg(port);
		}
	}

	public void PartialDecrypt(){
		Iterator<Integer> i = clientList.keySet().iterator();
		while(i.hasNext()){
			int key = i.next();
			ArrayList<EncMsg> encComBits =  clientList.get(key).getEncCompareResult();			
			for(int j = 0; 0 < encComBits.size(); j++){
				ModifiedElGamal elgamal = new ModifiedElGamal(Zp, g, xi, hi);
				EncMsg newEncMsg = elgamal.thresholdDec(xi);
				encComBits.set(j, newEncMsg);
			}
			clientList.get(key).setEncCompareResult(encComBits);
		}
	}

	public void sendKeytoClients(){
		for(int i = 0; i < clientArrLst.size(); i++){
			String nameC = clientArrLst.get(i).name;
			int portC = clientArrLst.get(i).getSocketNum();				
			if(portC != getSocketNum()){
				boolean scanning = true;
				while(scanning)
				{
					try
					{
						Socket cSocket = new Socket(nameC, portC);
						ObjectOutputStream outC= new ObjectOutputStream(cSocket.getOutputStream());
						ObjectInputStream inC = new ObjectInputStream(cSocket.getInputStream());
						outC.writeObject(portC);//sends its port number
						outC.writeObject(hi); //sends partial public key
						ProofOfKnow pok = new ProofOfKnow();
						ProofOfKnow.Proof proof = pok.prove(Zp, xi, g); //generates proof that shows he knows xi

						outC.writeObject(proof); //sends proof			
						System.out.println(portC+" I send proof");
						boolean verification = (boolean) inC.readObject(); //recieves verification
						System.out.println(portC+" I recieved verfication " +verification);
						cSocket.close();
						scanning=false;
					}
					catch(IOException | ClassNotFoundException e)
					{
						System.out.println("Connect failed, waiting and trying again");
						try
						{
							Thread.sleep(2000);//2 seconds
						}
						catch(InterruptedException ie){
							ie.printStackTrace();
						}
					} 
				}
			}

		}
	}

	public void sendEncBits(ArrayList<EncMsg> encBits){
		for(int i = 0; i < clientArrLst.size(); i++){
			String nameC = clientArrLst.get(i).name;
			int portC = clientArrLst.get(i).getSocketNum();				
			if(portC != getSocketNum()){
				boolean scanning = true;
				while(scanning)
				{
					try
					{
						Socket cSocket = new Socket(nameC, portC);
						ObjectOutputStream outC= new ObjectOutputStream(cSocket.getOutputStream());

						outC.writeObject(encBits); //sends partial public key
						System.out.println(portC+" I send bits");
						cSocket.close();
						scanning=false;
					}
					catch(IOException e)
					{
						System.out.println("Connect failed, waiting and trying again");
						try
						{
							Thread.sleep(2000);//2 seconds
						}
						catch(InterruptedException ie){
							ie.printStackTrace();
						}
					} 
				}
			}

		}
	}


	private static class KeyExchange extends Thread {
		private ServerSocket listener;
		private Socket socketKey;
		private final Object lock = new Object();
		public KeyExchange(ServerSocket listener) {
			this.listener = listener;
		}


		public void run() {
			Runnable receiveKeys = new Runnable() {
				public void run() {
					try {
						ObjectOutputStream objOut = new ObjectOutputStream(socketKey.getOutputStream());
						ObjectInputStream objIn = new ObjectInputStream(socketKey.getInputStream());

						String clientName = socketKey.getInetAddress().getHostName();
						int port = (int) objIn.readObject(); //receives port num
						ProofOfKnow pok = new ProofOfKnow();		
						GroupElement hj = (GroupElement) objIn.readObject();
						System.out.println(" I recived pubic key from "+ port + " " +hj.getElement());

						ProofOfKnow.Proof p = (ProofOfKnow.Proof) objIn.readObject();
						System.out.println(" I recived proof from "+ port);
						boolean isVerified = pok.verify(p, g, hj); // checks if the proof is correct

						objOut.writeObject(isVerified); // sends verification result
						System.out.println(" I sent verifcation "+ port+" "+isVerified);
						Client c = new Client(clientName, port, hj);
						synchronized (lock) {
							clientKeyList.add(c); //check here
							clientList.put(port, c); //updates client list
						}

						System.out.println("size: "+clientKeyList.size());
						if(check())
							isFinished = true;
						objIn.close();
						objOut.close();
						socketKey.close();						
					} catch (IOException | ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}					
				}

			};

			while(!isFinished){
				try{
					socketKey = listener.accept();
					Thread rKeys = new Thread(receiveKeys);
					System.out.println(isFinished);
					rKeys.start();
				}catch(IOException e){
					System.out.println("Connect failed, waiting and trying again");
					try
					{
						Thread.sleep(2000);//2 seconds
					}
					catch(InterruptedException ie){
						ie.printStackTrace();
					}
				} 
			}
			msgCount = 0;
			isFinished = false;

			System.out.println("key bitti!!");
		}

	}


	private static class EncBitsExchange extends Thread {
		private ServerSocket listener;
		private Socket socketKey;
		private final Object lock = new Object();
		public EncBitsExchange(ServerSocket listener) {
			this.listener = listener;
		}


		public void run() {
			Runnable receiveEncs = new Runnable() {
				public void run() {
					try {
						ObjectInputStream objIn = new ObjectInputStream(socketKey.getInputStream());

						String clientName = socketKey.getInetAddress().getHostName();

						int port = (int) objIn.readObject();	

						ArrayList<EncMsg> encBits =  (ArrayList<EncMsg>) objIn.readObject();
						System.out.println(InetAddress.getLocalHost().getHostName()+" I recived enc bits from "+ clientName);

						Client c = clientList.get(port);
						c.encMsgs = encBits;
						synchronized (lock) {
							clientList.put(port, c); //updates client list
						}
						if(check())
							isFinished = true;
						objIn.close();
						socketKey.close();				
					} catch (IOException | ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}					
				}

			};

			while(!isFinished){
				try{
					socketKey = listener.accept();
					Thread rKeys = new Thread(receiveEncs);
					rKeys.start();				}catch(IOException e){
						System.out.println("Connect failed, waiting and trying again");
						try
						{
							Thread.sleep(2000);//2 seconds
						}
						catch(InterruptedException ie){
							ie.printStackTrace();
						}
					} 
			}
			msgCount = 0;
			isFinished = false;
		}
	}


	//checks if the whole messages arrieve
	public static synchronized boolean check(){
		msgCount ++;
		if(msgCount == clientList.size() - 1){
			isFinished = true;
			return true;
		}else
			return false;
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	public static void writeQuestions(final ArrayList<String> questions){
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				q1txt.setText(questions.get(0));
				q2txt.setText(questions.get(1));
				q3txt.setText(questions.get(2));
				q4txt.setText(questions.get(3));
				q5txt.setText(questions.get(4));
			}
		});

	}

	public static void cancelMsg(int socketNum){
		try {
			Iterator<Integer> i = clientList.keySet().iterator();
			int rank = 1;
			while(i.hasNext()){
				int key = i.next();
				if(key == socketNum){
					clientList.get(key).setError(true);
				}
			}
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
			cancelMsg(socketNum);
		}
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				MessageDialog.openWarning(shell, "Warning", "The protocol is canceled!");
			}
		});

	}
	public static void updateSituation(final String txt){
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				situText.setText(txt);
			}
		});

	}

	public static void updateGain(final String txt){
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				gaintext.setText(txt);
			}
		});

	}

	public static void updateKey(final String txt){
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				keyText.setText(txt);
			}
		});

	}

	public static void updateRank(final String txt){
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				text.setText(txt);
			}
		});

	}
	
	public static void addList(final String txt){
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				list.add(txt);
			}
		});

	}

	public static void writeAnswers(){
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				ansList.add(Integer.parseInt(ans1txt.getText()));
				ansList.add(Integer.parseInt(ans2txt.getText()));
				ansList.add(Integer.parseInt(ans3txt.getText()));
				if(ans4NoBtn.getSelection())
					ansList.add(0);
				else
					ansList.add(0);
				if(ans5NoBtn.getSelection())
					ansList.add(0);
				else
					ansList.add(0);
			}
		});

	}


	/**
	 * Open the window.
	 * @throws IOException 
	 */
	private static class Screen extends Thread {
		public void run() {
			open();		
		}

	}


	public static void open(){
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
				
			}
			

		}
		Iterator<Integer> i = clientList.keySet().iterator();
		while(i.hasNext()){
			System.out.println("Bitti");
			int key = i.next();
			if(key == socketNumSt){
				clientList.get(key).setError(true);
				System.out.println(clientList.get(key).isError());
			}
		}
		System.exit(0);
		

		

	}


	public static void updateDisplay(){
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				MessageDialog.openError(shell, "Error", "You are not accepted!!.");
			}
		});

	}

	/**
	 * @wbp.parser.entryPoint
	 */
	protected static void createContents() {
		shell = new Shell();
		shell.setSize(499, 369);
		shell.setText("SWT Application");
		shell.setLayout(null);

		final Composite questionsComp = new Composite(shell, SWT.NONE);
		questionsComp.setBounds(10, 10, 373, 298);
		
		final Composite compLog = new Composite(shell, SWT.NONE);
		compLog.setBounds(0, 0, 424, 345);
		compLog.setVisible(false);
		
		list = new List(compLog, SWT.BORDER);
		list.setBounds(10, 28, 404, 307);

		q1txt = new Text(questionsComp, SWT.BORDER | SWT.READ_ONLY);
		q1txt.setBounds(0, 0, 259, 21);

		q2txt = new Text(questionsComp, SWT.BORDER | SWT.READ_ONLY);
		q2txt.setBounds(0, 51, 259, 21);

		q3txt = new Text(questionsComp, SWT.BORDER | SWT.READ_ONLY);
		q3txt.setBounds(0, 95, 259, 21);
		q4txt = new Text(questionsComp, SWT.BORDER | SWT.READ_ONLY);
		q4txt.setBounds(0, 140, 259, 21);

		q5txt = new Text(questionsComp, SWT.BORDER | SWT.READ_ONLY);
		q5txt.setBounds(0, 189, 259, 21);



		ans1txt = new Text(questionsComp, SWT.BORDER);
		ans1txt.setBounds(272, 0, 58, 21);

		ans2txt = new Text(questionsComp, SWT.BORDER);
		ans2txt.setBounds(272, 51, 58, 21);

		ans3txt = new Text(questionsComp, SWT.BORDER);
		ans3txt.setBounds(272, 95, 58, 21);

		Group ans4group = new Group(questionsComp, SWT.NONE);
		ans4group.setBounds(265, 121, 97, 47);
		ans4group.setLayout(null);

		ans4YesBtn = new Button(ans4group, SWT.RADIO);
		ans4YesBtn.setBounds(10, 20, 39, 16);
		
		ans4YesBtn.setText("Yes");

		ans4NoBtn = new Button(ans4group, SWT.RADIO);
		ans4NoBtn.setBounds(55, 20, 39, 16);
		
		ans4NoBtn.setText("No");

		Group ans5Group = new Group(questionsComp, SWT.NONE);
		ans5Group.setBounds(265, 176, 97, 47);

		ans5NoBtn = new Button(ans5Group, SWT.RADIO);
		ans5NoBtn.setBounds(55, 21, 39, 16);
		ans5NoBtn.setText("No");

		ans5YesBtn = new Button(ans5Group, SWT.RADIO);
		ans5YesBtn.setBounds(10, 21, 39, 16);
		ans5YesBtn.setText("Yes");

		final Composite composite = new Composite(shell, SWT.NONE);

		composite.setBounds(10, 10, 424, 252);
		composite.setVisible(false);

		situText = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
		situText.setBounds(10, 10, 132, 21);

		Composite keyComp = new Composite(composite, SWT.NONE);
		keyComp.setBounds(10, 48, 404, 72);

		final Button btnGenerateKey = new Button(keyComp, SWT.NONE);
		btnGenerateKey.setEnabled(false);
		btnGenerateKey.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				isKeyGenerated = true;
				btnGenerateKey.setEnabled(false);
				System.out.println("generate key" + isKeyGenerated);
			}
		});
		btnGenerateKey.setBounds(0, 10, 99, 25);
		btnGenerateKey.setText("Generate Key");

		situText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if(situText.getText().equals("All Clients arrieve"))
					btnGenerateKey.setEnabled(true);
			}
		});
		keyText = new Text(keyComp, SWT.BORDER);
		keyText.setBounds(0, 43, 394, 21);

		gaintext = new Text(composite, SWT.BORDER);
		gaintext.setBounds(325, 10, 76, 21);

		CLabel lblGain = new CLabel(composite, SWT.NONE);
		lblGain.setBounds(276, 10, 43, 21);
		lblGain.setText("Gain");

		Composite resultComp = new Composite(composite, SWT.NONE);
		resultComp.setBounds(10, 129, 404, 84);

		final Button btnShowLog = new Button(resultComp, SWT.NONE);
		btnShowLog.setEnabled(false);
		btnShowLog.setBounds(319, 10, 75, 25);
		btnShowLog.setText("Show Log");

		btnShowLog.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//controls all answers
				compLog.setVisible(true);
				composite.setVisible(false);

			}
		}
				);
		CLabel lblOrder = new CLabel(resultComp, SWT.CENTER);
		lblOrder.setBounds(10, 10, 61, 21);
		lblOrder.setText("Order");

		text = new Text(resultComp, SWT.BORDER | SWT.READ_ONLY);
		text.setBounds(77, 10, 76, 21);
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				btnShowLog.setEnabled(true);
			}
		});

		final Button btnGainCalculation = new Button(questionsComp, SWT.NONE);
		btnGainCalculation.setEnabled(false);

		q5txt.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				btnGainCalculation.setEnabled(true);
			}
		});
		btnGainCalculation.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//controls all answers
				if(ans1txt.getText().equals("") || ans2txt.getText().equals("") || ans3txt.getText().equals("")){
					MessageDialog.openWarning(shell, "Warning", "Please answer all questions.");
				}else if((!ans4NoBtn.getSelection() && !ans4YesBtn.getSelection()) || (!ans5NoBtn.getSelection() && !ans5YesBtn.getSelection())){
					MessageDialog.openWarning(shell, "Warning", "Please answer all questions.");
				}else if(!isNumeric(ans1txt.getText()) || !isNumeric(ans2txt.getText()) || !isNumeric(ans3txt.getText()) ){
					MessageDialog.openWarning(shell, "Warning", "Please answer correctly.");
				}else{
					writeAnswers();
					isAnswered = true;
					questionsComp.setVisible(false);
					composite.setVisible(true);					
				}

			}
		}
				);
		btnGainCalculation.setBounds(0, 236, 97, 25);
		btnGainCalculation.setText("Gain Calculation");
		
		

	}
	public static boolean isNumeric(String str)
	{
		return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
	}

	public ArrayList<EncMsg> getEncMsgs() {
		return encMsgs;
	}

	public void setEncMsgs(ArrayList<EncMsg> encMsgs) {
		this.encMsgs = encMsgs;
	}

	public ArrayList<EncMsg> getEncCompareResult() {
		return encCompareResult;
	}

	public void setEncCompareResult(ArrayList<EncMsg> encCompareResult) {
		this.encCompareResult = encCompareResult;
	}

	public int getGain() {
		return gain;
	}

	public int getRank(){
		Iterator<Integer> i = clientList.keySet().iterator();
		int rank = 1;
		while(i.hasNext()){
			int key = i.next();
			if(key != getSocketNum()){
				System.out.println(gain+" is comp with "+ clientList.get(key).getGain());
				if(gain < clientList.get(key).getGain())
					rank++;
			}
		}

		return rank;

	}

	public void setGain(int gain) {
		this.gain = gain;
	}

	public boolean isError() {
		return isError;
	}

	public void setError(boolean isError) {
		this.isError = isError;
	}

	public boolean isChosen() {
		return chosen;
	}

	public void setChosen(boolean chosen) {
		this.chosen = chosen;
	}

	public GroupElement getHi() {
		return hi;
	}

	public void setHi(GroupElement hi) {
		this.hi = hi;
	}

	public  int getSocketNum() {
		return socketNum;
	}

	public void setSocketNum(int socketNum) {
		this.socketNum = socketNum;
	}
}
