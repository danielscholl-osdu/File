/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.file.provider.reference.validation;

import javax.validation.ConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.opengroup.osdu.core.common.model.file.FileLocationRequest;
import org.opengroup.osdu.file.validation.CommonFileLocationRequestValidator;
import org.opengroup.osdu.file.validation.FileIdValidator;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class ReferenceFileLocationRequestValidator extends CommonFileLocationRequestValidator {

  private static final String FILE_ID_MAX_LENGTH_TEMPLATE =
      "length should be less than resulted filepath. Max: ${max_length}";

  public ReferenceFileLocationRequestValidator(FileIdValidator fileIdValidator) {
    super(fileIdValidator);
  }

  @Override
  public boolean isValid(FileLocationRequest request, ConstraintValidatorContext context) {
    if (!super.isValid(request, context)) {
      return false;
    } else if (request.getFileID().length() > 1024) {
      updateHibernateContext(context);
      return false;
    } else {
      return true;
    }
  }

  private void updateHibernateContext(ConstraintValidatorContext context) {
    HibernateConstraintValidatorContext hibernateContext =
        context.unwrap(HibernateConstraintValidatorContext.class);
    hibernateContext.disableDefaultConstraintViolation();
    hibernateContext
        .addExpressionVariable("max_length", 1024)
        .buildConstraintViolationWithTemplate(FILE_ID_MAX_LENGTH_TEMPLATE)
        .addPropertyNode(FILE_ID_FIELD)
        .addConstraintViolation();
  }
}
