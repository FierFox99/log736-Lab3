package AlgoBitcoin.Classes;

import AlgoBitcoin.Classes.Custom.ConnectionInterMiners;
import AlgoBitcoin.Interfaces.IBlock;
import AlgoBitcoin.Interfaces.IMiner;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

public class Miner implements IMiner {

    public final static int BLOCK_SIZE = 10; // le # de transactions maximum pouvant être stockés dans un seul bloc
    private static int portCounter = 25000;
    private static ArrayList<Miner> allMiners = new ArrayList<>();
    private int id;
    private DatagramSocket socketOfThisMiner;
    private int port;
    private static ArrayList<IBlock> blockchain = new ArrayList<>();

    public static ArrayList<IBlock> getBlockchain() {
        return blockchain;
    }

    private static ArrayList<ArrayList<IBlock>> branches = new ArrayList<>();
    private ArrayList<Integer> neighborNodes = new ArrayList<>();
    private HashMap<Integer, ConnectionInterMiners> connectionstoOtherMiners = new HashMap<>(); // clé = id d'un autre mineur, la valeur = les informations de connexion à ce mineur en question
    private volatile List<Transaction> mempool = new ArrayList<>(); // les transactions que nous avons reçu en attente d'être confirmés et insérés dans un bloc
    private ArrayList<Thread> threadConnexions = new ArrayList<>();
    private HashMap<Integer,Integer> associationTransactionIdAvecInfosClient = new HashMap<>(); // ce dictionnaire associe l'id d'une transaction au port du client pour retourner une réponse au client ayant envoyé cette transaction
    private volatile HashMap<String,Integer> nbConfirmationsParBloc = new HashMap<>(); // pour la validation des blocs par au moins la moitié des mineurs

    public Miner(int id) throws IOException {
        this.id = id;
        this.port = portCounter;
        portCounter += 100;
        allMiners.add(this);

        Thread threadListener = new Thread(() -> {
            try {
                listenToNetwork();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, "threadMineur" + port + "ListenerAuxRequêtes");
        threadConnexions.add(threadListener);
        threadListener.start();

        Thread threadMineur = new Thread(() -> {
            // ce thread essaie continuellement de miner des blocs de transactions
            // (Celui est en action seulement lorsqu'il y a des transactions à mettre en blocs dans le mempool)
            try {
                listenToMempool();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, "threadMineur" + port + "EssaiDeMiner");
        threadConnexions.add(threadMineur);
        threadMineur.start();
    }

    public ArrayList<Miner> getAllOtherMiners() {
        ArrayList<Miner> otherMiners = new ArrayList<>();
        for (Miner miner : allMiners) {
            if (miner != this) {
                otherMiners.add(miner);
            }
        }
        return otherMiners;
    }

    //used whenever a miner finds a branch longer than its blockchain
    private void setLongestChain(){
        for (ArrayList<IBlock> branch : branches) {
            if (branch.size() > blockchain.size()) {
                blockchain = new ArrayList<>(branch);
                logInConsole("Chaîne la plus longue définie par le mineur " + id);
            }
        }

        branches.clear(); // on abandonne toutes les autres branches après avoir défini celle officielle
    }

    private IBlock mineBlock() {
        // Mining logic goes here.
        ArrayList<Transaction> transactionsToConfirm = new ArrayList<>(mempool);

        logInConsole("Création d'un nouveau bloc en cours");

        if (transactionsToConfirm.size() > BLOCK_SIZE) {
            // trop de transactions pour un seul bloc, donc il faut seulement mettre BLOCK_SIZE (nombre) de transactions dans le bloc
            transactionsToConfirm = new ArrayList<>();

            for (int i = 0; i < BLOCK_SIZE; i++) {
                transactionsToConfirm.add(mempool.get(i));
            }
        }

        mempool.removeAll(transactionsToConfirm); // on fait ça de cette manière pour que cette opération supporte le fait qu'on puisse recevoir de nouvelles transactions même durant celle-ci

        // on converti les transactions à juste leurs ids
        List<Integer> idsOfTransactionsToConfirm = new ArrayList<>();

        for (Transaction t: transactionsToConfirm) {
            idsOfTransactionsToConfirm.add(t.transactionId);
        }

        Block newBlockToMine = null;

        if (blockchain.size() == 0) {
            // il n'y a pas d'autres blocks, donc nous allons créé/miné un bloc genèse
            newBlockToMine = new Block(new ArrayList<>());
        } else {
            // un bloc normal (pas de genèse)
            Block lastBlock = (Block) blockchain.get(blockchain.size() - 1);

            // on peut prendre directement son hash (puisque celui-ci doit déjà avoir été hashé lors de son proof of work/lors qu'il a été miné)
            newBlockToMine = new Block(lastBlock.blockHash, idsOfTransactionsToConfirm, lastBlock.depth);
        }

        /**
         * On mine
         */

        // proof-of-worf/on mine
        newBlockToMine.blockHash = newBlockToMine.calculateBlockHash();

        return newBlockToMine; // Replace with an actual IBlock implementation.
    }

    private void addBlock(Block block) {
        // Logic to add a block to the end of the chain.
        blockchain.add(block);
        logInConsole("Bloc ajouté par le mineur " + id + " (hash du nouveau bloc: " + block.blockHash + ")");
    }

    private void addToMemPool(Transaction tx){
        logInConsole("Transaction ajoutée à la mempool par le mineur " + id);
        mempool.add(tx);
    }

    // crée le bloc genèse
    // à seulement besoin d'être appeler une fois pour initialiser tout le blockchain
    public ArrayList<IBlock> init() {
        // Initialize the genesis block.
        Block newGenesisBlock = (Block) mineBlock();

        logInConsole("Bloc de genèse créé par le mineur " + id);
        addBlock(newGenesisBlock);

        return blockchain; // Return initialized blockchain.
    }

    public ArrayList<Integer> connect() throws IOException {
        ArrayList<Integer> connectedNodes = new ArrayList<>();

        for (Miner miner : getAllOtherMiners()) {
            connectedNodes.add(miner.id);

            DatagramSocket socketOtherMiner = new DatagramSocket();
            InetAddress address = InetAddress.getByName("localhost");

            // on se connecte au port du mineur associé à ce client
            socketOtherMiner.connect(address, miner.port);

            connectionstoOtherMiners.put(miner.id, (new ConnectionInterMiners(socketOtherMiner, address, miner.port)));
        }

        this.neighborNodes = connectedNodes;


        logInConsole("Mineur " + id + " connecté aux mineurs : " + connectedNodes);

        return connectedNodes;
    }

    //calls mineBlock method whenever it collects transactions and validates received blocks and adds it to the current chain
    public void listenToNetwork()throws IOException{
        socketOfThisMiner = new DatagramSocket(port); // crée un socket écoutant sur ce port de localhost
        byte bufferToReceive[] = new byte[1024];
        // this datagramPacket represents a request received by the miner
        DatagramPacket datagramPacketOfRequestReceived = new DatagramPacket(bufferToReceive, 1024);

        logInConsole("Prepared to accept requests");

        while (true) {
            // this function waits until a request is received
            socketOfThisMiner.receive(datagramPacketOfRequestReceived);

            String messageOfRequest = new String(datagramPacketOfRequestReceived.getData(), 0, datagramPacketOfRequestReceived.getLength());

            // on gère les requêtes dans un autre thread afin de pouvoir gérer plusieurs requêtes en même temps
            Thread thread = new Thread(() -> {
                handleRequest(messageOfRequest);
            }, "ThreadMiner#" + id + "HandleRequest");
            threadConnexions.add(thread);
            thread.start();

            // EXEMPLE POUR TEST: sendResponseMessageToARequest("Response: " + messageOfRequest, datagramPacketOfRequestReceived);
        }
    }

    // gère les transactions en attente de confirmation
    private void listenToMempool() throws IOException {
        while (true) {
            if (mempool.size() == 0) {
                // aucunes transactions à mettre en blocs)
                continue;
            }

            // mine block
            Block newBlock = (Block) mineBlock();

            // seeking validation/confirmation of block
            logInConsole("Requêtes de validation envoyées.");

            nbConfirmationsParBloc.put(newBlock.blockHash, 0);
            sendNewBlockToOtherMiners(newBlock);

            if (validateBlock(newBlock)) {
                // le bloc est valide selon ce mineur
                nbConfirmationsParBloc.put(newBlock.blockHash, nbConfirmationsParBloc.get(newBlock.blockHash) + 1);
                logInConsole("Validations pour le bloc (" + newBlock.blockHash + "): (" + nbConfirmationsParBloc.get(newBlock.blockHash) + "/" + allMiners.size() + ")");
            }

            while (true) {
                if (nbConfirmationsParBloc.get(newBlock.blockHash) > (allMiners.size() / 2)) {
                    // nous avons accumulé assez de validation pour continuer notre calcul
                    break;
                }
            }

            logInConsole("Le bloc " + newBlock.blockHash + " a été validé par plus de la moitié des mineurs.");

            // on check si le blockchain avec ce block est bien le plus long blockchain qu'on connait
            ArrayList<IBlock> branche = new ArrayList<>(blockchain);
            branche.add(newBlock);

            branches.add(branche); // cette branche est une nouvelle version possible du blockchain
            setLongestChain(); // on check si cette branche est la meilleure

            if (branche.equals(blockchain)) {
                logInConsole("Ma branche a été choisie pour devenir le blockchain (donc mon block a été rajouté au blockchain).");
            }

            /*addBlock(newBlock);*/
            logInConsole("Il y a dorénavant " + blockchain.size() + " blocks dans le blockchain.");

            // on diffuse ce nouveau blockchain aux autres mineurs
            logInConsole("On envoie le nouveau block du blockchain aux autres mineurs.");
            synchronise();

            // répondre au client
            logInConsole("On répond aux clients ayant effectués les requêtes contenus dans ce bloc.");

            String responseToClient = "OK";
            HashSet<Integer> clientsToRespondTo = new HashSet<>();

            for (Integer i: newBlock.transactions) {
                responseToClient += (":" + i); // l'id d'une des transactions du nouveau bloc

                clientsToRespondTo.add(associationTransactionIdAvecInfosClient.get(i));
            }

            for (Integer port : clientsToRespondTo) {
                sendResponseMessageToClient(responseToClient, port);
            }
        }
    }

    public ArrayList<Block> synchronise() {
        for (Integer i: neighborNodes) {
            ConnectionInterMiners connection = connectionstoOtherMiners.get(i);

            try {
                trySendingMessage("ADDBLOCK:" + ((Block) blockchain.get(blockchain.size() - 1)).blockHash, connection.inetAddressOfTheOtherMiner, connection.portOfTheOtherMiner, connection.socketConnection);
            } catch (Exception e) {
                logInConsole("An error occured during the deserialization of a block: " + e.getMessage());
            }
        }

        return new ArrayList<>(); // pas important
    }

    private boolean validateBlock(IBlock currentBlock){
        IBlock previousBlock = blockchain.get(blockchain.size() - 1);

        return ((Block) currentBlock).getPreviousHash().equals(((Block) previousBlock).getCurrentHash()) && ((Block) currentBlock).blockHash.startsWith("0".repeat(Miner.difficulty));
    }

    public int getPort() {
        return port;
    }

    private void handleRequest(String message) {
        logInConsole("Requête reçue (" + message + ")");

        // syntaxe: "TRANSACTION:{transactionSerialize}:{portOfClient}"
        if (message.contains(":") && (Objects.equals(message.split(":")[0], "TRANSACTION"))) {
            // la requête s'agit d'une transaction
            try {
                Transaction transactionObtenue = Transaction.deserializeTransaction(message.split(":")[1]);

                addToMemPool(transactionObtenue);
                associationTransactionIdAvecInfosClient.put(transactionObtenue.transactionId, Integer.parseInt(message.split(":")[2]));

                logInConsole("Transaction reçu #" + transactionObtenue.transactionId);
            } catch (Exception e) {
                logInConsole("An error occured during the deserialization of a transaction: " + e.getMessage());
            }
        }

        // reçoit un bloc d'un autre mineur
        // syntaxe: "VALIDATION:{blockHash}:{minerSenderID}"
        if (message.contains(":") && (Objects.equals(message.split(":")[0], "VALIDATION"))) {
            try {
                Block blockObtenu = Block.deserializeBlock(message.split(":")[1]);

                boolean result = validateBlock(blockObtenu);

                logInConsole("La validation du bloc " + blockObtenu.blockHash + " retourne: " + result);

                ConnectionInterMiners connection = connectionstoOtherMiners.get(Integer.parseInt(message.split(":")[2]));

                // on répond à la requête de validation
                trySendingMessage(blockObtenu.blockHash + ":" + result, connection.inetAddressOfTheOtherMiner, connection.portOfTheOtherMiner, connection.socketConnection);
            } catch (Exception e) {
                logInConsole("An error occured during the deserialization of a block: " + e.getMessage());
            }
        }

        // reçoit la réponse de validation (d'un autre mineur) à ton bloc
        if (message.contains(":") && ((Objects.equals(message.split(":")[1], "true")) || (Objects.equals(message.split(":")[1], "false")))) {
            if (Objects.equals(message.split(":")[1], "false")) {
                return; // la validation est négative, donc on l'ignore
            }

            String blockHash = message.split(":")[0];

            // Si ce n'est pas faux, cela signfie que la validation est vrai (donc on incrémente le # de validations pour ce bloc)
            nbConfirmationsParBloc.put(blockHash, nbConfirmationsParBloc.get(blockHash) + 1);
            logInConsole("Validations pour le bloc (" + blockHash + "): (" + nbConfirmationsParBloc.get(blockHash) + "/" + allMiners.size() + ")");
        }

        // indique qu'il faut ajouté ce block dans notre blockchain
        // syntaxe: "ADDBLOCK:{blockHash}"
        if (message.contains(":") && (Objects.equals(message.split(":")[0], "ADDBLOCK"))) {
            logInConsole("Le bloc " + message.split(":")[1] + " a été rajouté à notre blockchain");

            // pas besoin de répondre
        }
    }

    private void sendResponseMessageToClient(String message, int port) throws IOException {
        // On envoie le message au client ayant envoyé cette requête (selon les infos du client envoyés dans celle-ci)
        trySendingMessageToClient(message, InetAddress.getByName("localhost"), port,socketOfThisMiner);
    }

    private void trySendingMessageToClient(String message, InetAddress address, int port, DatagramSocket socket) throws IOException {
        trySendingMessage(message, address, port, socket);
    }

    private void trySendingMessage(String message, InetAddress address, int port, DatagramSocket specificSocket) throws IOException {
        DatagramPacket datagramPacketToSendRequest = new DatagramPacket(new byte[1024], 1024);
        datagramPacketToSendRequest.setPort(port);
        datagramPacketToSendRequest.setAddress(address);
        datagramPacketToSendRequest.setLength(message.length());
        datagramPacketToSendRequest.setData(message.getBytes());

        specificSocket.send(datagramPacketToSendRequest);
    }

    private void logInConsole(String message) {
        System.out.println("Dans miner #" + this.id + " : " + message);
    }

    private void sendNewBlockToOtherMiners(Block block) {
        for (Integer i: neighborNodes) {
            ConnectionInterMiners connection = connectionstoOtherMiners.get(i);

            try {
                trySendingMessage("VALIDATION:" + block.serializeThisBlock() + ":" + id, connection.inetAddressOfTheOtherMiner, connection.portOfTheOtherMiner, connection.socketConnection);
            } catch (Exception e) {
                logInConsole("An error occured during the deserialization of a block: " + e.getMessage());
            }
        }
    }
}
