package org.opengroup.osdu.file.validation.filemetadata;

import jakarta.validation.GroupSequence;
import jakarta.validation.groups.Default;

@GroupSequence({Default.class, BusinessRuleValidation.class})
public interface FileMetadataValidationSequence {
}
