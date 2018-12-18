package com.asset.Schemas;
import com.asset.Schemas.AssetSchema;
import com.google.common.collect.ImmutableList;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.UUID;

/**
 * An IOUState schema.
 */
public class AssetSchemaV1 extends MappedSchema {

    public AssetSchemaV1() {
        super(AssetSchema.class, 1, ImmutableList.of(PersistentAsset.class));
    }

    @Entity
    @Table(name = "asset_states")
    public static class PersistentAsset extends PersistentState {
        @Column(name = "name") private final String name;
        @Column(name = "owner") private final String owner;
        @Column(name = "previousOwner") private final String previousOwner;
        @Column(name = "linear_id") private final UUID linearId;


        public PersistentAsset(String name, String owner, String previousOwner, UUID linearId) {
            this.name = name;
            this.owner = owner;
            this.previousOwner = previousOwner;
            this.linearId = linearId;
        }

        // Default constructor required by hibernate.
        public PersistentAsset() {
            this.name = null;
            this.owner = null;
            this.previousOwner = null;
            this.linearId = null;
        }

        public String getName() {
            return name;
        }

        public String getOwner() {
            return owner;
        }

        public String getPreviousOwner() {
            return previousOwner;
        }

        public UUID getId() {
            return linearId;
        }
    }
}