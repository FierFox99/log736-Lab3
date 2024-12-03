package AlgoBitcoin.Classes;



import AlgoBitcoin.Interfaces.IBlock;
import AlgoBitcoin.Interfaces.IClient;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Objects;

public class Client implements IClient {

    private int clientId;
    public final static int CONFIRMATION_BLOCK_NUMBER = 6;
    public final static InetAddress minerAdress;

    static {
        try {
            minerAdress = InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private DatagramSocket socket;
    private int minerPort;
    private ArrayList<Transaction> transactions = new ArrayList<>();

    public Client(int clientId, int minerPort) {
        this.clientId = clientId;
        this.minerPort = minerPort;

        try {
            if (init() == 0) {
                System.err.println("La connexion du client #" + clientId + " à son mineur a échoué, mais aucune erreur s'est produite.");
            }
        } catch (Exception e) {
            System.err.println("Erreur de connexion du client #" + clientId + " à son mineur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTransaction(){
        transactions.add(new Transaction(clientId));
    }

    private int getLastBlockDepth() throws IOException{
        ArrayList<IBlock> blockchain = Miner.getBlockchain();
        return ((Block) blockchain.get(blockchain.size() - 1)).depth;
    }

    private int getTxBlockDepth(int txId) throws IOException{
        ArrayList<IBlock> blockchain = Miner.getBlockchain();

        for (IBlock block: blockchain) {
            Block realBlock = (Block) block;

            if (realBlock.transactions.contains(txId)) {
                return realBlock.depth;
            }
        }

        return 0;
    }
    private String waitForConfirmation(int txID) throws IOException {
        String response = waitToReceiveResponseToRequest();

        if (response.contains(txID + "")) {
            return response;
        }

        return null;
    }

    public int init()throws IOException {
        socket = new DatagramSocket();

        // on se connecte au port du mineur associé à ce client
        socket.connect(minerAdress, minerPort);

        return socket.isConnected() ? 1 : 0;
    }

    public void sendTransaction()throws IOException {
        createTransaction();

        Transaction tx = transactions.get(transactions.size() - 1);

        if (socket == null || !socket.isConnected()) {
            System.err.println("Socket non connectée. Veuillez initialiser la connexion au mineur.");
            return;
        }

        try {
            trySendingMessage("TRANSACTION:" + tx.serializeThisTransaction() + ":" + socket.getLocalPort());

            String response = waitForConfirmation(tx.transactionId);

            if (response.startsWith("OK")) {
                System.out.println("La transaction a été confirmé par le mineur.");
                confirmAllTransactionsInTheBlock(response);
            } else {
                System.err.println("Échec de l'envoi de la transaction : " + response);
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de l'envoi de la transaction : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void trySendingMessage(String message) throws IOException {
        DatagramPacket datagramPacketToSendRequest = new DatagramPacket(new byte[1024], 1024);
        datagramPacketToSendRequest.setPort(minerPort);
        datagramPacketToSendRequest.setAddress(minerAdress);
        datagramPacketToSendRequest.setLength(message.length());
        datagramPacketToSendRequest.setData(message.getBytes());

        socket.send(datagramPacketToSendRequest);
    }

    private String waitToReceiveResponseToRequest() throws IOException {
        DatagramPacket datagramPacketOfRequestReceived = new DatagramPacket(new byte[1024], 1024);

        // this functions makes the client wait for a response from the miner
        socket.receive(datagramPacketOfRequestReceived);

        return new String(datagramPacketOfRequestReceived.getData(), 0, datagramPacketOfRequestReceived.getLength());
    }

    private void confirmAllTransactionsInTheBlock(String response) {
        for (String transactionId: response.split(":")) {
            if (Objects.equals(transactionId, "OK")) {
                continue;
            }

            for (Transaction t: transactions) {
                if (Integer.parseInt(transactionId) == t.transactionId) {
                    t.setConfirmed();
                }
            }
        }
    }
}
