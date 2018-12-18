package com.asset;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.security.PublicKey;
import java.util.List;
import java.util.UUID;

// ******************
// * Initiator flow *
//  rm -rf build && ./gradlew.bat deployNodes
//  ./build/nodes/runnodes.bat
//  flow start Initiator owner: PartyA, name: "Test", validator: Validator
// 	run vaultQuery contractStateType: com.asset.AssetState
// ******************

@InitiatingFlow
@StartableByRPC
public class Initiator extends FlowLogic<SignedTransaction> {

    private  Party owner;
    private Party validator;
    private  String name;
    private final ProgressTracker.Step GENERATING_TRANSACTION = new ProgressTracker.Step("Generating transaction based on new Asset.");
    private final ProgressTracker.Step VERIFYING_TRANSACTION = new ProgressTracker.Step("Verifying contract constraints.");
    private final ProgressTracker.Step SIGNING_TRANSACTION = new ProgressTracker.Step("Signing transaction with our private key.");
    private final ProgressTracker.Step GATHERING_SIGS = new ProgressTracker.Step("Gathering the counterparty's signature.") {
        @Override
        public ProgressTracker childProgressTracker() {
            return CollectSignaturesFlow.Companion.tracker();
        }
    };
    private final ProgressTracker.Step FINALISING_TRANSACTION = new ProgressTracker.Step("Obtaining notary signature and recording transaction.") {
        @Override
        public ProgressTracker childProgressTracker() {
            return FinalityFlow.Companion.tracker();
        }
    };
    private final ProgressTracker progressTracker = new ProgressTracker(
            GENERATING_TRANSACTION,
            VERIFYING_TRANSACTION,
            SIGNING_TRANSACTION,
            GATHERING_SIGS,
            FINALISING_TRANSACTION
    );

    public Initiator(Party owner, String name, Party validator){
        this.owner = owner;
        this.name = name;
        this.validator = validator;
    }


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
        UniqueIdentifier id = UniqueIdentifier.Companion.fromString(UUID.nameUUIDFromBytes(name.getBytes()).toString());
        AssetState tokenState = new AssetState(owner, name, validator, null, id);

        /* ============================================================================
         *         TODO 2 - Build our transaction to update the ledger!
         * ===========================================================================*/
        progressTracker.setCurrentStep(GENERATING_TRANSACTION);
        // We build our transaction.
        TransactionBuilder transactionBuilder =  new TransactionBuilder(notary);
        transactionBuilder.addOutputState(tokenState, AssetContract.ID);

        List<PublicKey> requiredSigners = ImmutableList.of(myID.getOwningKey(), validator.getOwningKey());
        transactionBuilder.addCommand(new AssetContract.Commands.Create(), requiredSigners);

        /* ============================================================================
         *          TODO 3 - Verify transaction
         * ===========================================================================*/
        progressTracker.setCurrentStep(VERIFYING_TRANSACTION);
        // We check our transaction is valid based on its contracts.
        transactionBuilder.verify(getServiceHub());

        // We sign the transaction with our private key, making it immutable.
        progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(transactionBuilder);

        /* ============================================================================
         *          TODO 4 - Collect signature from Validator
         * ===========================================================================*/
        progressTracker.setCurrentStep(GATHERING_SIGS);
        List<FlowSession> flow = ImmutableList.of(initiateFlow(this.validator));
        final SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(signedTransaction, flow, CollectSignaturesFlow.Companion.tracker()));

        // We get the transaction notarised and recorded automatically by the platform.
        progressTracker.setCurrentStep(FINALISING_TRANSACTION);
        return subFlow(new FinalityFlow(fullySignedTx));
    }



}
