/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.file.provider.ibm.validation;

import javax.validation.ConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.opengroup.osdu.core.common.model.file.FileLocationRequest;
import org.opengroup.osdu.file.provider.ibm.model.constant.StorageConstant;
import org.opengroup.osdu.file.validation.CommonFileLocationRequestValidator;
import org.opengroup.osdu.file.validation.FileIdValidator;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class IBMFileLocationRequestValidator extends CommonFileLocationRequestValidator {

  private static final String FILE_ID_MAX_GCP_LENGTH =
      "length should be less than resulted GCS filepath. Max: ${max_length}";

  public IBMFileLocationRequestValidator(FileIdValidator fileIdValidator) {
    super(fileIdValidator);
  }

  @Override
  public boolean isValid(FileLocationRequest request,
      ConstraintValidatorContext context) {
    boolean isValid = super.isValid(request, context);

    if (!isValid) {
      return isValid;
    }

    HibernateConstraintValidatorContext hibernateContext =
        context.unwrap(HibernateConstraintValidatorContext.class);

    if (request.getFileID().length() > StorageConstant.GCS_MAX_FILEPATH) {
      hibernateContext.disableDefaultConstraintViolation();
      hibernateContext
          .addExpressionVariable("max_length", StorageConstant.GCS_MAX_FILEPATH)
          .buildConstraintViolationWithTemplate(FILE_ID_MAX_GCP_LENGTH)
          .addPropertyNode(FILE_ID_FIELD)
          .addConstraintViolation();
      return false;
    }

    return true;
  }
}
