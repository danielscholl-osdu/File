package org.opengroup.osdu.file.validation.filemetadata;

import javax.validation.GroupSequence;
import javax.validation.groups.Default;

@GroupSequence({Default.class, BusinessRuleValidation.class})
public interface FileMetadataValidationSequence {
}
