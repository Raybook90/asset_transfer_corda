package com.asset;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.security.PublicKey;
import java.util.List;

// ******************
// * Initiator flow *
//  flow start Initiator owner: PartyA, name: "Test", validator: PartyB
// 	run vaultQuery contractStateType: com.asset.AssetState
// ******************

@InitiatingFlow
@StartableByRPC
public class Initiator extends FlowLogic<SignedTransaction> {

    private  Party owner;
    private Party validator;
    private  String name;

    public Initiator(Party owner, String name, Party validator){
        this.owner = owner;
        this.name = name;
        this.validator = validator;
    }

    private final ProgressTracker progressTracker = new ProgressTracker();

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {
        // We choose our transaction's notary (the notary prevents double-spends).
        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
        // We get a reference to our own identity.
        Party myID = getOurIdentity();

        /* ============================================================================
         *         TODO 1 - Create our state to represent on-ledger asset!
         * ===========================================================================*/
        // We create our new TokenState.
        AssetState tokenState = new AssetState(owner, name, validator);

        /* ============================================================================
         *         TODO 2 - Build our transaction to update the ledger!
         * ===========================================================================*/
        // We build our transaction.
        TransactionBuilder transactionBuilder =  new TransactionBuilder(notary);
        transactionBuilder.addOutputState(tokenState, AssetContract.ID);

        List<PublicKey> requiredSigners = ImmutableList.of(myID.getOwningKey(), validator.getOwningKey());
        transactionBuilder.addCommand(new AssetContract.Commands.Create(), requiredSigners);

        /* ============================================================================
         *          TODO 3 - Verify transaction
         * ===========================================================================*/
        // We check our transaction is valid based on its contracts.
        transactionBuilder.verify(getServiceHub());

        // We sign the transaction with our private key, making it immutable.
        SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(transactionBuilder);

        /* ============================================================================
         *          TODO 4 - Collect signature from Validator
         * ===========================================================================*/
        List<FlowSession> flow = ImmutableList.of(initiateFlow(this.validator));
        final SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(signedTransaction, flow, CollectSignaturesFlow.Companion.tracker()));

        // We get the transaction notarised and recorded automatically by the platform.
        return subFlow(new FinalityFlow(fullySignedTx));
    }



}
