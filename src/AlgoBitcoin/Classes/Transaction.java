package AlgoBitcoin.Classes;

import AlgoBitcoin.Interfaces.ITransaction;

public class Transaction implements ITransaction {
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

}

