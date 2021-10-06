/*
 * Copyright 2021 Google LLC
 * Copyright 2021 EPAM Systems, Inc
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.model.file.FileLocationRequest;
import org.opengroup.osdu.file.validation.FileIdValidator;

@ExtendWith(MockitoExtension.class)
class ReferenceFileLocationRequestValidatorTest {

  private static final String FILE_ID_MAX_LENGTH_TEMPLATE =
      "length should be less than resulted filepath. Max: ${max_length}";
  private static final String NOT_BLANK_MESSAGE = "{javax.validation.constraints.NotBlank"
      + ".message}";
  private static final String INVALID_FILE_ID = "Invalid FileID";
  private static final String FILE_ID_FIELD = "FileID";
  private static final String FILE_ID_VALUE = "file";

  @InjectMocks
  ReferenceFileLocationRequestValidator requestValidator;

  @Mock
  FileLocationRequest request;
  @Mock
  ConstraintValidatorContext context;
  @Mock
  ConstraintViolationBuilder constraintViolationBuilder;
  @Mock
  NodeBuilderCustomizableContext customizableContext;
  @Mock
  FileIdValidator fileIdValidator;
  @Mock
  HibernateConstraintValidatorContext hibernateContext;

  @Test
  void testIsValid_True() {
    when(fileIdValidator.checkFileID(FILE_ID_VALUE))
        .thenReturn(true);
    when(request.getFileID()).thenReturn(FILE_ID_VALUE);
    boolean isValid = requestValidator.isValid(request, context);
    assertTrue(isValid);
  }

  @Test
  void testIsValid_BlankFileId() {
    when(context.buildConstraintViolationWithTemplate(NOT_BLANK_MESSAGE))
        .thenReturn(constraintViolationBuilder);
    when(constraintViolationBuilder.addPropertyNode(FILE_ID_FIELD))
        .thenReturn(customizableContext);
    when(request.getFileID()).thenReturn("");
    boolean isValid = requestValidator.isValid(request, context);
    assertFalse(isValid);
  }

  @Test
  void testIsValid_NotMatchingPatternFileId() {
    when(context.buildConstraintViolationWithTemplate(INVALID_FILE_ID))
        .thenReturn(constraintViolationBuilder);
    when(constraintViolationBuilder.addPropertyNode(FILE_ID_FIELD))
        .thenReturn(customizableContext);
    when(request.getFileID()).thenReturn("////");
    when(fileIdValidator.checkFileID("////")).thenReturn(false);
    boolean isValid = requestValidator.isValid(request, context);
    assertFalse(isValid);
  }

  @Test
  void testIsValid_ExceedMaxLengthFileId() {
    String longString = new String(new char[2048]);
    when(fileIdValidator.checkFileID(longString))
        .thenReturn(true);
    when(request.getFileID()).thenReturn(longString);
    when(context.unwrap(any())).thenReturn(hibernateContext);
    when(hibernateContext.addExpressionVariable("max_length", 1024))
        .thenReturn(hibernateContext);
    when(hibernateContext.buildConstraintViolationWithTemplate(FILE_ID_MAX_LENGTH_TEMPLATE))
        .thenReturn(constraintViolationBuilder);
    when(constraintViolationBuilder.addPropertyNode(FILE_ID_FIELD)).thenReturn(customizableContext);
    boolean isValid = requestValidator.isValid(request, context);
    assertFalse(isValid);
  }
}
