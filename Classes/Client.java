package Classes;

import Interfaces.IClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Map;

public class Client implements IClient {

    private int clientId;
    public final static int CONFIRMATION_BLOCK_NUMBER = 6;
    private DatagramSocket socket;
    private int minerPort;
    private ArrayList<Transaction> transactions = new ArrayList<>();

    public Client(int clientId, int minerPort) {
        this.clientId = clientId;
        this.minerPort = minerPort;
    }

    private void createTransaction(){
        transactions.add(new Transaction(clientId));
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
        if (socket == null || !socket.isConnected()) {
            System.err.println("Socket non connectée. Veuillez initialiser la connexion au mineur.");
            return;
        }

        try {
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println("TRANSACTION:" + tx.toString());

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = reader.readLine();

            if ("ACK".equals(response)) {
                System.out.println("Transaction envoyée avec succès au mineur : " + tx.toString());
                waitForConfirmation(tx.transactionId);
            } else {
                System.err.println("Échec de l'envoi de la transaction : " + response);
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de l'envoi de la transaction : " + e.getMessage());
            e.printStackTrace();
        }
    }

    
}
