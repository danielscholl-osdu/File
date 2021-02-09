package org.opengroup.osdu.file.model.filemetadata;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.opengroup.osdu.core.common.model.entitlements.Acl;
import org.opengroup.osdu.core.common.model.entitlements.validation.ValidAcl;
import org.opengroup.osdu.core.common.model.legal.Legal;
import org.opengroup.osdu.core.common.model.storage.validation.ValidKind;
import org.opengroup.osdu.file.model.filemetadata.filedetails.FileData;
import org.opengroup.osdu.file.model.storage.Ancestry;
import org.opengroup.osdu.file.validation.filemetadata.BusinessRuleValidation;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FileMetadata {

    private String id;

    @Valid
    @NotNull(message = "kind must not be null")
    @ValidKind(groups = BusinessRuleValidation.class)
    private String kind;

    @NotNull(message = "acl must not be null")
    @ValidAcl(groups = BusinessRuleValidation.class)
    @JsonProperty("acl")
    private Acl acl;

    @Valid
    @NotNull(message = "legal tag cannot be empty")
    private Legal legal;

    @Valid
    @NotNull(message = "data cannot be empty")
    private FileData data;

    private Ancestry ancestry;
    
    private List<Map<String, Object>> meta;
    
    private Map<String, String> tags;

}
