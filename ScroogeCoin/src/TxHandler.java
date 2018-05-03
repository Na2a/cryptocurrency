import java.util.HashSet;
import java.util.ArrayList;

public class TxHandler {

    private UTXOPool pool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        pool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool,
     * (2) the signatures on each input of {@code tx} are valid,
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        double inputSum = 0;
        double outputSum = 0;
        for (Transaction.Output output : tx.getOutputs()) {
            if (output.value < 0) {
                return false;
            }
            outputSum += output.value;
        }
        HashSet<UTXO> used = new HashSet<UTXO>();
        for (int index = 0; index < tx.numInputs(); index++) {
            Transaction.Input input = tx.getInput(index);
            UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
            if (used.contains(utxo)) {
                return false;
            }
            used.add(utxo);
            if (!pool.contains(utxo)) {
                return false;
            }
            Transaction.Output correspondingOutput = pool.getTxOutput(utxo);
            if (!Crypto.verifySignature(correspondingOutput.address, tx.getRawDataToSign(index), input.signature)) {
                return false;
            }
            inputSum += correspondingOutput.value;
        }
        if (outputSum > inputSum)
            return false;
        return true;
    }

    /**
     * Updates UTXO pool, should be called each time we want to make
     * transaction happen
     */
    private void acceptTx(Transaction tx) {
        for (int index = 0; index < tx.numInputs(); index++) {
            Transaction.Input input = tx.getInput(index);
            UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
            pool.removeUTXO(utxo);
        }
        for (int index = 0; index < tx.numOutputs(); index++) {
            Transaction.Output output = tx.getOutput(index);
            UTXO utxo = new UTXO(tx.getHash(), index);
            pool.addUTXO(utxo, output);
        }
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        boolean[] added = new boolean[possibleTxs.length];
        ArrayList<Transaction> validTxSet = new ArrayList<Transaction>();
        boolean flag = true;
        while (flag) {
            flag = false;
            for (int i = 0; i < possibleTxs.length; i++) {
                if (!added[i] && isValidTx(possibleTxs[i])) {
                    flag = true;
                    added[i] = true;
                    acceptTx(possibleTxs[i]);
                    validTxSet.add(possibleTxs[i]);
                }
            }
        }
        int i = 0;
        Transaction[] txs = new Transaction[validTxSet.size()];
        for (Transaction tx : validTxSet)
            txs[i++] = tx;
        return txs;
    }

}
