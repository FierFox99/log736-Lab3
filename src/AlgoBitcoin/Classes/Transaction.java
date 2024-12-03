package AlgoBitcoin.Classes;

import AlgoBitcoin.Interfaces.ITransaction;

import java.io.*;
import java.util.Base64;

public class Transaction implements ITransaction, Serializable {
        private static int counterForIdOfTransactions = 0;

        public int transactionId, clientId;
        private boolean confirmationState = false;

        public Transaction(int clientId){
                this.clientId = clientId;
                transactionId = counterForIdOfTransactions;
                counterForIdOfTransactions++; // pour que la prochaine transaction ait un id différent
        }

        // la méthode qu'on appel lorsque la transaction a été confirmé (Elle n'est pas confirmé par défaut, jusqu'à temps que cette fonction soit appelée)
        public void setConfirmed(){
                confirmationState = true;
        }
        
        public boolean isConfirmed(){
                return confirmationState;
        }

        public String serializeThisTransaction() throws IOException {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream outputStream = new ObjectOutputStream( byteArrayOutputStream );
                outputStream.writeObject( this ); // on passe l'objet à sérializer
                outputStream.close();
                return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
        }

        public static Transaction deserializeTransaction(String serializedTransaction) throws IOException, ClassNotFoundException {
                byte [] data = Base64.getDecoder().decode( serializedTransaction );
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
                Object objet  = ois.readObject();
                ois.close();

                return (Transaction) objet;
        }

}

