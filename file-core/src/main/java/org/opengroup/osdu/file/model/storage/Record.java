package org.opengroup.osdu.file.model.storage;

import java.util.List;
import java.util.Map;

import org.opengroup.osdu.core.common.model.entitlements.Acl;
import org.opengroup.osdu.core.common.model.legal.Legal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Record {

    private String id;
    private Long version;
    private String kind;
    private Acl acl;
    private Legal legal;
    private Ancestry ancestry;
    private Map<String, Object> data;
    private List<Map<String, Object>> meta;
    private Map<String, String> tags;

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
}
