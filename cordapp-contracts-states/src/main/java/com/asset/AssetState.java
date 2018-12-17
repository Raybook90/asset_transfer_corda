package com.asset;

import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;

import java.util.Arrays;
import java.util.List;

// *********
// * State *
// *********
public class AssetState implements ContractState {

    private String name;
    private Party owner;

    public Party getValidator() {
        return validator;
    }

    private Party validator;

    public String getName() {
        return name;
    }

    public Party getOwner() {
        return owner;
    }

    public AssetState(Party owner, String name, Party validator) {
        this.owner = owner;
        this.name = name;
        this.validator = validator;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(owner);
    }
}