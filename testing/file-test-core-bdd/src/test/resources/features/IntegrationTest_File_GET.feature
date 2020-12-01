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

  #Negative scenario for GET /files/uploadURL
  @File
  Scenario Outline: Verify that File service's GET upload-url API throws an appropriate exception if the signed-url has expired and the same url is used to upload a file
    Given I hit File service GET uploadURL API
    Then service should respond back with a valid <ReponseStatusCode> and upload input file from <InputFilePath>
    When I try to use signed url after expiration period <ExpiredURL> and file path <InputFilePath>
    Then service should respond back with error code <ErrorResponseCode>

    Examples:
      | ReponseStatusCode | ErrorResponseCode | InputFilePath                   | ExpiredURL                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
      | "200"             | "400"             | "/sample_upload_files/test.csv" | "https://storage.googleapis.com/tenant1-staging-area/akumar290-slb-com-bdf5ad93@desid.delfi.slb.com/8e2c46a1-5ba3-4431-bcc1-fa79dd31780e/test.csv?GoogleAccessId=datafier%40evd-ddl-us-tenant1.iam.gserviceaccount.com&Expires=1596696360&Signature=gBgH36BdpijzCmdeWIBq5GyTM45kbqBIYIfdPSPxGXSBWWSBkoFJyk5IThTJn%2BedbMLvWP%2B9s%2BAWZp0XCrzN3o3o6NGBnK5SekRQZcTm13UV9fQ%2FAblFhUSFzKqnvZvf1l8DxuoY4RxntOhJiExNLsTngDbsHUTae%2FsIdmp1gJncyOt20w8o9lqbp9oC5ngzxyZnrc54jDJMQTxnQ8ilRMtrpwGAX%2BhX%2BeUrvButc62%2Fz%2FCLPR2AR8IujPbVAuXvwL0fxl4fmgTAHw9xzhRfm0%2BrqnKEhqS12lnnAhvxKFFJDXW61k2WNeU%2BkHzEd%2BIxKgcADrmHCySDU4vmFI3QwQ%3D%3D" |

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
    Given I hit File service GET metadata signed API with an <InvalidId>
    Then service should respond back with error <ReponseStatusCode> and <ResponseMessage>

    Examples:
      | InvalidId                                               | ReponseStatusCode | ResponseMessage                           |
      | "opendes:file:21107053-17bf-405e-9dd3-bb0a1e9b8e0aTEST" | "404"             | "/output_payloads/FileGET_InvalidID.json" |
