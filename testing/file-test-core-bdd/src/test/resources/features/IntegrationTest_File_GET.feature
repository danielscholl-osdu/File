Feature: File service API integration test

  Background:
    Given I generate user token and set request headers with "PRIVATE_TENANT2"

  #Negative scenario for GET /files/uploadURL
  @File
  Scenario Outline: Verify that File service's GET upload-url API throws correct exception if mandatory headers are blank or invalid
    Given I hit File service GET API with missing or invalid <header> and <headerValue>
    Then service should respond back with error <ReponseStatusCode> or <AlternateStatusCode> and <ResponseMessage>

    Examples:
      | ReponseStatusCode | AlternateStatusCode | ResponseMessage                                        | header              | headerValue    |
      | "401"             | "403"               | "/output_payloads/FileGET_MissingAuthorization.json"   | "authorization"     | ""             |
      | "401"             | "403"               | "/output_payloads/FileGET_UnAuthUserExcp.json"         | "authorization"     | "ExpiredToken" |
      | "401"             | "401"               | "/output_payloads/FileGET_MissingDataPartitionId.json" | "data-partition-id" | ""             |
      | "401"             | "403"               | "/output_payloads/FileGET_UnAuthUserExcp.json"         | "data-partition-id" | "incorrectDPI" |
    
  #Positive scenario for GET /files/uploadURL
  @File
  Scenario Outline: Verify that File service's GET upload-url API is used to upload a file within expiration period and it successfully uploads the file
    Given I hit File service GET uploadURL API
    Then service should respond back with a valid <ReponseStatusCode> and upload input file from <InputFilePath>
    When I try to use signed url within expiration period and file path <InputFilePath>
    Then service should respond back with a valid <ReponseStatusCode> and upload input file from <InputFilePath>

    Examples:
      | ReponseStatusCode | InputFilePath                   |
      | "200"             | "/sample_upload_files/test.csv" |

  #Negative scenario for FILE service
  @File
  Scenario Outline: Verify error message for invalid id input
    Given I hit File service GET metadata signed API with an <InvalidId> and <tenant>
    Then service should respond back with error <ReponseStatusCode> and <ResponseMessage>

    Examples:
      | InvalidId                                               | tenant            | ReponseStatusCode | ResponseMessage                           |
      | ":file:21107053-17bf-405e-9dd3-bb0a1e9b8e0aTEST" | "PRIVATE_TENANT2" | "404"             | "/output_payloads/FileGET_InvalidID.json" |
