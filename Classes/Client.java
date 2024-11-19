package Classes;

import Interfaces.IClient;

import java.io.IOException;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Map;

public class Client implements IClient {


    private long clientID;
    public final static int CONFIRMATION_BLOCK_NUMBER 6;
    private DatagramSocket socket;
    private int minerPort;
    private ArrayList<Transaction> transactions;

    private void createTransaction(){
        transactions.add(new transactions(clientID));
    }

    private int getLastBlockDepth() throws IOException{
        try {
            if (socket != null && socket.isConnected()) {
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                writer.println("DEMANDE_PROFONDEUR");

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
        return -1;
    }

    private int getTxBlockDepth() throws IOException{
        try {
            if (socket != null && socket.isConnected()) {

                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                writer.println("DEMANDE_PROFONDEUR_TRANSACTION:" + transactionId);

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
        return -1;
    }
    private void waitForConfirmation(int txID) {}

    public Client (long IDclient){
        clientID = IDclient;
    }

    public int init()throws IOException {        
        try {
            socket = new Socket("localhost", minerPort);
            System.out.println("Connexion au mineur établie : localhost :" + minerPort);

            OutputStream outputStream = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(outputStream, true);
            writer.println("Bonjour, mineur !");
        } catch (IOException e) {
            System.err.println("Erreur lors de la connexion au mineur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendTransaction(Transaction tx)throws IOException {

    }

    
}
