package com.asset;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.security.PublicKey;
import java.util.List;

// ******************
// * Initiator flow *
//  flow start Transfer newOwner: PartyB, name: "Test"
// 	run vaultQuery contractStateType: com.asset.AssetState
// ******************

@InitiatingFlow
@StartableByRPC
public class Transfer extends FlowLogic<SignedTransaction> {

    private  Party newOwner;
    private  String name;

    public Transfer(String name, Party newOwner){
        this.newOwner = newOwner;
        this.name = name;
    }


    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }
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
    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {
        // We choose our transaction's notary (the notary prevents double-spends).
        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
        // We get a reference to our own identity.
        Party myID = getOurIdentity();

        /* ============================================================================
         *         TODO 1 - Get states from Ledger
         * ===========================================================================*/
        List<StateAndRef<AssetState>> states = getServiceHub().getVaultService().queryBy(AssetState.class).getStates();
        // We find the right asset
        StateAndRef<AssetState> inputStateAndRef = states
                    .stream().filter(artStateAndRef -> {
                        AssetState assetState = artStateAndRef.getState().getData();
                        return assetState.getName().equals(name) && assetState.getOwner().equals(myID);
                    }).findAny().orElseThrow(() -> new IllegalArgumentException("The piece of asset was not found."));

        AssetState inputAssetState = inputStateAndRef.getState().getData();

        /* ============================================================================
         *      TODO 2 - Build our transaction to update the ledger!
         * ===========================================================================*/
        progressTracker.setCurrentStep(GENERATING_TRANSACTION);
        // We build our transaction.
        TransactionBuilder transactionBuilder =  new TransactionBuilder(notary);
        transactionBuilder.addInputState(inputStateAndRef);
        AssetState currentState = new AssetState(this.newOwner, inputAssetState.getName(), inputAssetState.getValidator(), inputAssetState.getOwner());
        transactionBuilder.addOutputState(currentState, AssetContract.ID);
        List<PublicKey> requiredSigners = ImmutableList.of(
                inputAssetState.getOwner().getOwningKey(),
                inputAssetState.getValidator().getOwningKey(),
                this.newOwner.getOwningKey());
        transactionBuilder.addCommand(new AssetContract.Commands.Transfer(), requiredSigners);

        /* ============================================================================
         *          TODO 3 - Check if transaction is valid
         * ===========================================================================*/
        progressTracker.setCurrentStep(VERIFYING_TRANSACTION);
        // We check our transaction is valid based on its contracts.
        transactionBuilder.verify(getServiceHub());

        /* ============================================================================
         *          TODO 4 - Signing Transaction
         * ===========================================================================*/
        // We sign the transaction with our private key, making it immutable.
        progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(transactionBuilder);

        /* ============================================================================
         *          TODO 4 - Collect signature from Validator and new Owner
         * ===========================================================================*/
        progressTracker.setCurrentStep(GATHERING_SIGS);
        //FlowSession validatorFlow = initiateFlow(inputAssetState.getValidator());
        List<FlowSession> flow = ImmutableList.of(initiateFlow(this.newOwner), initiateFlow(inputAssetState.getValidator()));
        final SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(signedTransaction, flow, CollectSignaturesFlow.Companion.tracker()));

        // We get the transaction notarised and recorded automatically by the platform.
        progressTracker.setCurrentStep(FINALISING_TRANSACTION);
        return subFlow(new FinalityFlow(fullySignedTx));
    }
}
