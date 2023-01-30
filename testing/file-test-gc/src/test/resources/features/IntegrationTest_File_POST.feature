Feature: File service API integration test

  Background: Common steps for all tests are executed
    Given I generate user token and set request headers with "PRIVATE_TENANT1"

  #Negative Validation scenarios for POST /files/metadata
  @File
  Scenario Outline: Verify that File returns expected output in case of Invalid payload on hitting POST /files/metadata
    When I hit File metadata service POST API with <InputPayload> and data-partition-id as <tenant> for validations
    Then Service should respond back with error <ReponseStatusCode> and <ResponseMessage>

    Examples:
      | InputPayload                                           | ReponseStatusCode | ResponseMessage                                             | tenant            |
      | "/input_payloads/File_missing_kind.json"            | "400"             | "/output_payloads/File_missing_kind_msg.json"            | "PRIVATE_TENANT1" |
      | "/input_payloads/File_missing_viewers.json"         | "400"             | "/output_payloads/File_missing_viewers_msg.json"         | "PRIVATE_TENANT1" |
      | "/input_payloads/File_missing_owners.json"          | "400"             | "/output_payloads/File_missing_owners_msg.json"          | "PRIVATE_TENANT1" |
      | "/input_payloads/File_missing_acl.json"             | "400"             | "/output_payloads/File_missing_acl_msg.json"             | "PRIVATE_TENANT1" |
      | "/input_payloads/File_missing_legal.json"           | "400"             | "/output_payloads/File_missing_legal_msg.json"           | "PRIVATE_TENANT1" |
      | "/input_payloads/File_missing_data.json"            | "400"             | "/output_payloads/File_missing_data_msg.json"            | "PRIVATE_TENANT1" |
      | "/input_payloads/File_empty_fileSource.json"        | "400"             | "/output_payloads/File_empty_fileSource_msg.json"        | "PRIVATE_TENANT1" |
      | "/input_payloads/File_missing_fileSource.json"      | "400"             | "/output_payloads/File_missing_fileSource_msg.json"      | "PRIVATE_TENANT1" |
      | "/input_payloads/File_invalid_fileSource.json"      | "400"             | "/output_payloads/File_invalid_fileSource_msg.json"      | "PRIVATE_TENANT1" |
      | "/input_payloads/File_invalid_Endian.json"          | "400"             | "/output_payloads/File_invalid_Endian_msg.json"          | "PRIVATE_TENANT1" |
#      | "/input_payloads/File_invalid_ScalarIndicator.json" | "400"             | "/output_payloads/File_invalid_ScalarIndicator_msg.json" | "PRIVATE_TENANT1" |
      #| "/input_payloads/File_Datatype_Mismatch.json"       | "400"             | "/output_payloads/File_Datatype_Mismatch_msg.json"       | "PRIVATE_TENANT1" |
