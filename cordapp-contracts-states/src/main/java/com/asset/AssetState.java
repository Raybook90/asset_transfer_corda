package com.asset;

import com.asset.Schemas.AssetSchemaV1;
import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.schemas.QueryableState;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

// *********
// * State *
// *********
public class AssetState implements LinearState, QueryableState {

    private String name;
    private Party owner;
    private final UniqueIdentifier linearId;

    public Party getPreviousOwner() {
        return previousOwner;
    }

    private Party previousOwner;

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

    public AssetState(Party owner, String name, Party validator, Party previousOwner, UniqueIdentifier linearId) {
        this.owner = owner;
        this.name = name;
        this.validator = validator;
        this.previousOwner = previousOwner;
        this.linearId = linearId;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(owner, validator);
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return this.linearId;
    }

    @NotNull
    @Override
    public Iterable<MappedSchema> supportedSchemas() {
        return ImmutableList.of(new AssetSchemaV1());
    }

    @NotNull
    @Override
    public PersistentState generateMappedObject(MappedSchema schema) {
        if (schema instanceof AssetSchemaV1) {
            String previousOwner = "";
            try {
              previousOwner = this.previousOwner.getName().toString();
            } catch (Exception e){
                System.out.println(e.toString());
            }
            return new AssetSchemaV1.PersistentAsset
            (
                    this.name,
                    this.owner.getName().toString(),
                    previousOwner,
                    this.linearId.getId());
        } else {
            throw new IllegalArgumentException("Unrecognised schema $schema");
        }
    }
}