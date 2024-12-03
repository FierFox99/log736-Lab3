package AlgoBitcoin.Classes;



import AlgoBitcoin.Interfaces.IClient;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

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
        /*try {
            if (socket != null && socket.isConnected()) {
                //PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                //writer.println("DEMANDE_PROFONDEUR");
                trySendingMessage("DEMANDE_PROFONDEUR");

                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String response = reader.readLine();

                if (response != null) {
                    try {
                        int profondeur = Integer.parseInt(response);
                        System.out.println("Profondeur reçue du mineur : " + profondeur);
                        return profondeur;
                    } catch (NumberFormatException e) {
                        System.err.println("Réponse invalide reçue du mineur : " + response);
                    }
                } else {
                    System.err.println("Aucune réponse reçue du mineur.");
                }
            } else {
                System.err.println("Socket non connectée.");
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la communication avec le mineur : " + e.getMessage());
            e.printStackTrace();
        }
        return -1;*/
        return 0; // TODO to change
    }

    private int getTxBlockDepth() throws IOException{
        /*try {
            if (socket != null && socket.isConnected()) {

                //PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                //writer.println("DEMANDE_PROFONDEUR_TRANSACTION:" + transactionId);
                trySendingMessage("DEMANDE_PROFONDEUR_TRANSACTION:" + transactionId);


                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String response = reader.readLine();

                if (response != null) {
                    try {
                        int profondeur = Integer.parseInt(response);
                        System.out.println("Profondeur du bloc contenant la transaction " + transactionId + " : " + profondeur);
                        return profondeur;
                    } catch (NumberFormatException e) {
                        System.err.println("Réponse invalide reçue du mineur pour la transaction : " + response);
                    }
                } else {
                    System.err.println("Aucune réponse reçue du mineur pour la transaction.");
                }
            } else {
                System.err.println("Socket non connectée.");
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la communication avec le mineur : " + e.getMessage());
            e.printStackTrace();
        }
        return -1;*/
        return 0; // TODO to change
    }
    private void waitForConfirmation(int txID) {
        try {
            int initialDepth = getLastBlockDepth();
            if (initialDepth < 0) {
                System.err.println("Impossible d'obtenir la profondeur initiale du bloc.");
                return;
            }

            while (true) {
                int currentDepth = getLastBlockDepth();
                if (currentDepth - initialDepth >= CONFIRMATION_BLOCK_NUMBER) {
                    System.out.println("Transaction " + txID + " confirmée après " + CONFIRMATION_BLOCK_NUMBER + " blocs.");
                    break;
                }
                System.out.println("En attente de confirmation pour la transaction " + txID + "...");
                Thread.sleep(5000); // Attendre 5 secondes avant de vérifier à nouveau
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'attente de confirmation : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public int init()throws IOException {
        socket = new DatagramSocket();

        // on se connecte au port du mineur associé à ce client
        socket.connect(minerAdress, minerPort);

        /*
        // EXEMPLE POUR TESTER
        trySendingMessage("Bonjour" + clientId);

        System.out.println(waitToReceiveResponseToRequest());*/

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
            trySendingMessage("TRANSACTION:" + tx.serializeThisTransaction());

        System.out.println("test");
            String response = waitToReceiveResponseToRequest();

            if ("OK".equals(response)) {
                System.out.println("Transaction envoyée avec succès au mineur : " + response);
                waitForConfirmation(tx.transactionId);
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
}
