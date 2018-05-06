import java.util.*;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
    private int n;
    private boolean[] followees;
    private boolean[] malicious;

    HashSet<Transaction> proposals;

    HashMap<Integer, HashSet<Transaction>> setOf;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
    }

    private HashSet<Transaction> getSet(HashMap<Integer, HashSet<Transaction>> from, int id) {
        if (!from.containsKey(id)) {
            from.put(id, new HashSet<Transaction>());
        }
        return from.get(id);
    }

    private boolean isSubset(HashSet<Transaction> a, HashSet<Transaction> b) {
        for (Transaction tx : a) {
            if (!b.contains(tx))
                return false;
        }
        return true;
    }

    public void setFollowees(boolean[] followees) {
        n = followees.length;
        malicious = new boolean[n];
        setOf = new HashMap<Integer, HashSet<Transaction>>();

        this.followees = Arrays.copyOf(followees, n);
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        this.proposals = new HashSet<Transaction>(pendingTransactions);
    }

    public Set<Transaction> sendToFollowers() {
        return this.proposals;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        HashMap<Integer, HashSet<Transaction>> currentSetOf = new HashMap<Integer, HashSet<Transaction>>();
        for (Candidate candidate : candidates) {
            getSet(currentSetOf, candidate.sender).add(candidate.tx);
        }
        for (int i = 0; i < n; i++) {
            if (followees[i] && !malicious[i]) {
                HashSet<Transaction> prev = getSet(setOf, i);
                HashSet<Transaction> cur = getSet(currentSetOf, i);
                if (!isSubset(prev, cur)) {
                    malicious[i] = true;
                }
                for (Transaction tx : cur)
                    setOf.get(i).add(tx);
            }
        }
        /* for (int i = 0; i < n; i++) {
            if (followees[i] && malicious[i]) {
                for (Transaction tx : getSet(setOf, i))
                    proposals.remove(i);
            }
        } */
        for (Candidate candidate : candidates) {
            if (!malicious[candidate.sender])
                proposals.add(candidate.tx);
        }
    }
}
