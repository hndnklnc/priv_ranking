import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.ibm.icu.lang.UCharacter.SentenceBreak;



public class ServerScreen {

	public static Shell shell;
	private static Table table;

	private static boolean isStart = false;
	private static boolean isEnd = false;
	private static ArrayList<Client> clientList = new ArrayList<Client>();
	private static HashMap<Integer, Client> clientHashList = new HashMap<Integer, Client>();
	private static ArrayList<String> questions = new ArrayList<String>();
	private static ArrayList<Integer> impList = new ArrayList<Integer>(); //importance of questions
	private static ArrayList<Integer> valueList = new ArrayList<Integer>(); //value of the questions
	private static ArrayList<String> chosenClients = new ArrayList<String>();
	private static ArrayList<Socket> socketList = new ArrayList<Socket>();
	private static ArrayList<Boolean> orderList = new ArrayList<Boolean>();
	private static boolean isPartialEnd = false;
	private static int MIN_CLIENTS = 3;
	private static int K = 2; //best number of K clients will be chosen
	public static int l = 256;
	private static Table table_1;

	private static int keyCount = 0;
	private static int encbitCount = 0;
	private static int compareCount = 0;
	private static int gainCount = 0;
	private static int SEC_PARAM = 256; //security parameter

	static TableColumn tblclmnConnectedClient;
	static TableColumn tblclmnSituation; 
	static GroupZp Zp; // creates group that will work
	static GroupElement g; //randomly chooses a generator from group Zp

	public static void main(String[] args) throws InterruptedException {
		new Screen().start();
		BigInteger p = BigInteger.probablePrime(SEC_PARAM, new Random()); //finds a prime number that is SEC_PARAM bit length
		Zp = new GroupZp(p); // creates group that will work
		g = Zp.getRandomGenerator(); //randomly chooses a generator from group Zp


		Thread.sleep(2000);
		Runnable error = new Runnable() {
			public void run() {
				while(!isEnd){
					checkClients();
					System.out.println("checking");
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}				
			}
			
		};
		Thread err = new Thread(error);
		err.start();
		ServerSocket listener = null;
		try {
			listener = new ServerSocket(9898);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			System.err.println("Listener couldnt contact with client!");
		}
		try {
			while(!isEnd){				
				try {
					new Beginning(listener.accept()).start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}finally {
			try {
				listener.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


		for(int i = 0; i < K; i++)
			System.out.println(i+1 + "th "+ chosenClients.get(i));
	}


	private static class Beginning extends Thread {
		private Socket socket;
		private final Object lock = new Object();
		int clientSocket = 0;
		public Beginning(Socket socket) {
			this.socket = socket;
			log("New connection with client#  at " + socket);
		}


		public void run() {

			try {

				ObjectOutputStream objOut = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream objIn = new ObjectInputStream(socket.getInputStream());
				String clientName = socket.getInetAddress().getHostName();
				
				if(isStart)
					objOut.writeObject(0);
				else{
					objOut.writeObject(K);
					clientSocket = (int) objIn.readObject(); //receives client info
					Client client = new Client(clientName, clientSocket);				
					clientHashList.put(clientSocket, client);
					clientList.add(client);
					int order = clientList.size() -  1; //order of the client
					orderList.add(false);


					addTableClient(client, "Added");


					objOut.writeObject(questions); //sends questions to the client

					objOut.writeObject(Zp); //publish group
					objOut.writeObject(g); // publish generator
					objOut.writeObject(SEC_PARAM); //publish security paraameter

					//gain computation
					CompanyComp comp = new CompanyComp(impList, valueList, 3); //creates comp vector

					ClientSending clientComp1 = (ClientSending) objIn.readObject(); //receives client's comp result


					int [] companyComp1 = comp.compantCompIntArr(clientComp1.QX, clientComp1.c, clientComp1.g); //first calculation

					objOut.writeObject(companyComp1); // sends  calculation to the client
					objOut.writeObject(comp.getAlpha()); //sends alpha
					addTableClient(client, "Gain Computed");
					if(isStart)
						objOut.writeObject(clientList);
					else{
						while(!isStart){ //wait before sending all client information to other clients
							if(clientList.size() >= MIN_CLIENTS){
								Thread.sleep(2000);
								objOut.writeObject(clientList);
								synchronized (lock) {
									isStart = true;
								}								
							}
							Thread.sleep(2000);

						}
					}

					HashMap<Integer, Client> clientHash = (HashMap<Integer, Client>) objIn.readObject();
					
					
					synchronized (lock) {
						clientHashList.put(clientSocket, clientHash.get(clientSocket));
						gainCount++;
					}
					
					while (gainCount != clientList.size()) {
						Thread.sleep(2000);
					}
					ProofOfKnow pok = new ProofOfKnow();		

					

					
					ProofOfKnow.Proof p = (ProofOfKnow.Proof) objIn.readObject();
					GroupElement hj = (GroupElement) objIn.readObject();
					System.out.println("h"+hj.getElement());
					client.setHi(hj);
					
					synchronized (lock) {
						clientHashList.put(clientSocket, client);
						keyCount++;
					}

					if(keyCount == clientList.size())
						objOut.writeObject(clientHashList);
					else{
						while(keyCount != clientList.size()){ //wait before sending all client information to other clients
							//						if(keyCount == clientList.size()){
							//							Thread.sleep(2000);
							//							System.out.println(clientSocket+" keycountiçi:"+keyCount);
							//							objOut.writeObject(clientHashList);
							//							
							//						}
							Thread.sleep(2000);
							System.out.println(clientSocket+" keycount:"+keyCount);

						}
						objOut.writeObject(clientHashList);

					}
					addTableClient(client, "Key Sent");
					ArrayList<EncMsg>  encbits = (ArrayList<EncMsg>) objIn.readObject();
					client.setEncMsgs(encbits);
					clientHashList.put(clientSocket, client);

					synchronized (lock) {
						encbitCount++;
					}

					if(encbitCount == clientList.size())
						objOut.writeObject(clientHashList);
					else{
						while(encbitCount != clientList.size()){ //wait before sending all client information to other clients
							//						if(encbitCount == clientList.size()){
							//							Thread.sleep(2000);
							//							objOut.writeObject(clientHashList);
							//						}
							Thread.sleep(2000);
						}
						objOut.writeObject(clientHashList);

					}
					addTableClient(client, "Enc Sent");

					ArrayList<EncMsg>  encCompbits = (ArrayList<EncMsg>) objIn.readObject();
					client.setEncCompareResult(encCompbits);
					clientHashList.put(clientSocket, client);

					synchronized (lock) {
						compareCount++;
					}

					if(order == 0){ //if all comparings received sends it to initiator
						if(compareCount == clientList.size())
							objOut.writeObject(clientHashList);
						else{
							while(compareCount != clientList.size()){ //wait before sending all client information to other clients
								//							if(compareCount == clientList.size()){
								//								Thread.sleep(2000);
								//								objOut.writeObject(clientHashList);
								//							}
								Thread.sleep(2000);
							}
							objOut.writeObject(clientHashList);
						}

						clientHashList = (HashMap<Integer, Client>) objIn.readObject(); //gets first partial decryption from P0
						orderList.set(0, true);

						addTableClient(client, "Enc Comp Sent");
					}

					System.out.println("order:"+order+" orderlisize:"+orderList.size());
					if(order != 0){
						System.out.println("order:"+order+" orderlisize:"+orderList.size());
						addTableClient(client, "Enc Comp Sent");
						while(!orderList.get(order - 1)) // until previous party sends partial decryption					
							Thread.sleep(2000);
						objOut.writeObject(clientHashList); //sends partial decryptions
						clientHashList = (HashMap<Integer, Client>) objIn.readObject(); //takes partial decryption
						orderList.set(order, true);

					}



					if(order == clientList.size() - 1){
						isPartialEnd = true;
						objOut.writeObject(clientHashList);
						addTableClient(client, "Partial Sent");
					}else{
						while(!isPartialEnd){
							Thread.sleep(2000);
						}
						objOut.writeObject(clientHashList);
						addTableClient(client, "Partial Sent");
					}
					
					int result = (int) objIn.readObject();
					if(result != 0){
						chosenClients.add(clientName);
						clientHashList.get(clientSocket).setChosen(false);
					}else{
						chosenClients.add(clientName);
						clientHashList.get(clientSocket).setChosen(true);
						System.out.println(clientHashList.get(clientSocket).getSocketNum()+ "is chosen");
					}
					
					while( chosenClients.size() != clientHashList.size()){
						Thread.sleep(2000);
					}
					
					addTableClient(clientHashList.get(clientSocket), clientHashList.get(clientSocket).isChosen()+"");
					isEnd = true;
				}
				


			} catch (IOException | ClassNotFoundException | InterruptedException e) {
				cancelMsg(clientSocket);
				e.printStackTrace();
				
				
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
					log("Couldn't close a socket, what's going on?");
				}
				log("Connection with client# closed");
			}

		}




	}

	public synchronized static void checkClients(){
		Iterator<Integer> i = clientHashList.keySet().iterator();
		System.out.println("client size:"+clientHashList.size());
		while(i.hasNext()){
			int key = i.next();		
			if(clientHashList.get(key).isError()){
				cancelMsg(clientHashList.get(key).getSocketNum());
			}
		}
	}
	public synchronized static void cancelMsg(int clientSocket){		
		if(isStart){
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					MessageDialog.openWarning(shell, "Warning", "The protocol is canceled!");
				}
			});
			System.exit(0);
		}else{
			clientHashList.remove(clientSocket);
			for(int i = 0; i < clientList.size(); i++){
				if(clientList.get(i).getSocketNum() == clientSocket){
					clientList.remove(i);
					break;
				}
			}
			System.out.println("list size:"+clientList.size());
			updateTable();
		}		

	}

	public static void updateTable(){
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				table_1.clearAll();
				for(int i = 0; i < clientList.size(); i++){

					TableItem item = new TableItem(table_1, SWT.NONE);
					item.setText(new String [] {clientList.get(i).name + " " + clientList.get(i).getSocketNum(), "Refresh"});

				}
			}
		});
	}
	public static void addTableClient(final Client c, final String situation){

		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				boolean isThere = false;
				for(int i = 0; i < table_1.getItemCount(); i++){
					TableItem item = table_1.getItem(i);
					if(item.getText(0).equals(c.name + " " + c.getSocketNum())){
						item.setText(new String [] {c.name + " " + c.getSocketNum(), situation});
						isThere = true;
						break;
					}
				}

				if(!isThere){
					TableItem tblItem = new TableItem(table_1, SWT.NONE);
					tblItem.setText(new String [] {c.name + " " + c.getSocketNum(), situation});
				}

			}
		});

	}


	private static void log(String message) {
		System.out.println(message);
	}

	/**
	 * Open the window.
	 * @throws IOException 
	 */
	private static class Screen extends Thread {
		private Socket socket;
		public void run() {

			Display display = Display.getDefault();
			try {
				createContents();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			shell.open();
			shell.layout();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}

			}
			//System.exit(0);
		}

		/**
		 * Create contents of the window.
		 * @throws IOException 
		 */
		protected void createContents() throws IOException {
			//reads questions
			BufferedReader reader = new BufferedReader(new InputStreamReader
					(getClass().getClassLoader().getResourceAsStream("questions")));
			String line = "";
			ArrayList<String> questionInfo = new ArrayList<String>();
			while((line = reader.readLine()) != null){
				questionInfo.add(line);
			}
			System.out.println("read");

			//creates screen
			shell = new Shell();
			shell.setSize(492, 376);
			shell.setText("SWT Application");

			table = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
			table.setBounds(10, 10, 384, 138);
			table.setHeaderVisible(true);
			table.setLinesVisible(true);

			TableColumn tblQuestionsColumn = new TableColumn(table, SWT.NONE);
			tblQuestionsColumn.setWidth(211);
			tblQuestionsColumn.setText("Questions");

			TableColumn tblValueColumn = new TableColumn(table, SWT.NONE);
			tblValueColumn.setWidth(93);
			tblValueColumn.setText("Expected Value");

			TableColumn tblImportColumn = new TableColumn(table, SWT.NONE);
			tblImportColumn.setWidth(74);
			tblImportColumn.setText("Importance");

			table_1 = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
			table_1.setBounds(10, 171, 378, 109);
			table_1.setHeaderVisible(true);
			table_1.setLinesVisible(true);

			TableColumn tblclmnConnectedClient = new TableColumn(table_1, SWT.NONE);
			tblclmnConnectedClient.setWidth(181);
			tblclmnConnectedClient.setText("Connected Client");

			TableColumn tblclmnSituation = new TableColumn(table_1, SWT.NONE);
			tblclmnSituation.setWidth(191);
			tblclmnSituation.setText("Situation");




			for(int i = 0; i < questionInfo.size(); i++){
				String [] array = questionInfo.get(i).split("#");
				TableItem tblItem = new TableItem(table, SWT.NONE);
				tblItem.setText(array);
				questions.add(array[0]);
				if(array[1].equals("Yes"))
					valueList.add(1);
				else if(array[1].equals("No"))
					valueList.add(0);
				else
					valueList.add(Integer.parseInt(array[1]));
				impList.add(Integer.parseInt(array[2]));			
			}


		}
	}
}
