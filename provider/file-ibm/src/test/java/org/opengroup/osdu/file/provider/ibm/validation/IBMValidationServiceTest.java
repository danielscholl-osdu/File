/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.file.provider.ibm.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.tuple;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.internal.cfg.context.DefaultConstraintMapping;
import org.hibernate.validator.internal.properties.javabean.JavaBeanHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.core.common.model.file.FileLocationRequest;
import org.opengroup.osdu.file.ReplaceCamelCase;
import org.opengroup.osdu.file.config.RequestConstraintMappingContributor;
import org.opengroup.osdu.file.provider.ibm.validation.IBMFileLocationRequestValidator;
import org.opengroup.osdu.file.provider.interfaces.IValidationService;
import org.opengroup.osdu.file.validation.CommonFileLocationRequestValidator;
import org.opengroup.osdu.file.validation.FileIdValidator;
import org.opengroup.osdu.file.validation.FileLocationRequestValidatorWrapper;
import org.opengroup.osdu.file.validation.ValidationServiceImpl;

@DisplayNameGeneration(ReplaceCamelCase.class)
class IBMValidationServiceTest {

  private static final String FILE_ID_FIELD = "FileID";

  private static final String NOT_BLANK_MESSAGE = "must not be blank";

  private static final String FILE_ID = "temp-file.tmp";

  private static Validator validator;
  private IValidationService validationService;
  private static JavaBeanHelper javaBeanHelper;

  @BeforeAll
  static void initAll() {
    HibernateValidatorConfiguration configuration = (HibernateValidatorConfiguration) Validation.byDefaultProvider()
        .configure();

    RequestConstraintMappingContributor requestConstraintMappingContributor
        = new RequestConstraintMappingContributor();
    requestConstraintMappingContributor.createConstraintMappings(() -> {
      DefaultConstraintMapping mapping = new DefaultConstraintMapping(javaBeanHelper);
      configuration.addMapping(mapping);
      return mapping;
    });

    ValidatorFactory factory = configuration
        .constraintValidatorFactory(new TestConstraintValidatorFactory())
        .buildValidatorFactory();
    validator = factory.getValidator();
  }

  @BeforeEach
  void setUp() {
    validationService = new ValidationServiceImpl(validator);
  }

  @Nested
  class ValidateFileLocationRequest {

    @Test
    void shouldSuccessfullyValidateWhenRequestHasValidFileId() {
      // given
      FileLocationRequest request = FileLocationRequest.builder()
          .fileID(FILE_ID)
          .build();

      // when
      Throwable thrown = catchThrowable(() -> validationService.validateFileLocationRequest(request));

      // then
      assertThat(thrown).isNull();
    }

    @Test
    void shouldNotExecuteGcpSpecificValidationWhenCommonValidationIsFailed() {
      // given
      FileLocationRequest request = FileLocationRequest.builder()
          .fileID(" ")
          .build();

      // when
      Throwable thrown = catchThrowable(() -> validationService.validateFileLocationRequest(request));

      // then
      assertThat(thrown)
          .isInstanceOf(ConstraintViolationException.class)
          .hasMessage("Invalid FileLocationRequest");

      ConstraintViolationException ex = (ConstraintViolationException) thrown;
      assertThat(ex.getConstraintViolations())
          .extracting(v -> tuple(v.getPropertyPath().toString(), v.getMessage()))
          .containsExactly(tuple(FILE_ID_FIELD, NOT_BLANK_MESSAGE));
    }

    @Test
    void shouldFailValidationWhenRequestHasToLargeFileId() {
      // given
      FileLocationRequest request = FileLocationRequest.builder()
          .fileID(RandomStringUtils.randomAlphanumeric(1050))
          .build();

      // when
      Throwable thrown = catchThrowable(() -> validationService.validateFileLocationRequest(request));

      // then
      assertThat(thrown)
          .isInstanceOf(ConstraintViolationException.class)
          .hasMessage("Invalid FileLocationRequest");

      ConstraintViolationException ex = (ConstraintViolationException) thrown;
      assertThat(ex.getConstraintViolations())
          .extracting(v -> tuple(v.getPropertyPath().toString(), v.getMessage()))
          .containsExactly(tuple(FILE_ID_FIELD, "length should be less than resulted GCS filepath. Max: 1024"));
    }

  }

  static class TestConstraintValidatorFactory implements ConstraintValidatorFactory {

    ConstraintValidatorFactory constraintValidatorFactory = Validation
        .buildDefaultValidatorFactory().getConstraintValidatorFactory();

    @Override
    public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {

      if (FileLocationRequestValidatorWrapper.class.equals(key)) {
        CommonFileLocationRequestValidator fileLocationRequestValidator =
            new IBMFileLocationRequestValidator(new FileIdValidator());
        return (T) new FileLocationRequestValidatorWrapper(fileLocationRequestValidator);
      }

      return constraintValidatorFactory.getInstance(key);
    }

    @Override
    public void releaseInstance(ConstraintValidator<?, ?> instance) {

    }
  }

}
