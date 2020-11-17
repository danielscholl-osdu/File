package org.opengroup.osdu.file.model.storage;

import java.util.ArrayList;
import java.util.List;

import org.opengroup.osdu.core.common.model.entitlements.Acl;
import org.opengroup.osdu.core.common.model.legal.Legal;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Data;

@Data
public class Record {

    private String id = null;
    private Long version = -1L;
    private String kind = "";
    private Acl acl = new Acl();
    private Legal legal = new Legal();
    private Ancestry ancestry = new Ancestry();
    private JsonObject data = new JsonObject();
    private List<JsonObject> meta = new ArrayList<>();

    public Record() {
    }

    public Record(String kind) {
        this(kind, null);
    }

    public Record(String kind, String id) {
        this.id = id;
        this.kind = kind;
    }

    public Record setOwners(String[] dataGroups) {
        this.acl.setOwners(dataGroups);
        return this;
    }

    public Record setViewers(String[] dataGroups) {
        this.acl.setViewers(dataGroups);
        return this;
    }

    public Record addLegaltag(String legaltag) {
        this.legal.getLegaltags().add(legaltag);
        return this;
    }

    public Record addOrdc(String ordc) {
        this.legal.getOtherRelevantDataCountries().add(ordc);
        return this;
    }

    public Record addParent(String recordId, String version) {
        this.ancestry.getParents().add(String.format("%s:%s", recordId, version));
        return this;
    }

    public <T> Record setDataObject(T object) {
        Gson gson = new Gson();
        this.data = gson.toJsonTree(object).getAsJsonObject();
        return this;
    }
}
