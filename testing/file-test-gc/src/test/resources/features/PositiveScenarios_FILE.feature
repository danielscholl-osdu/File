Feature: File Service API integration test

  Background:
    Given I generate user token and set request headers with "PRIVATE_TENANT1"

  #Positive scenario for FILE service
  @File
  Scenario Outline: Verify that file is uploaded to landing zone and downloaded from persistent zone successfully and content is verified
    Given I hit File service GET uploadURL API
    Then service should respond back with a valid <getReponseStatusCode> and upload input file from <inputFilePath>
    When I hit File service metadata service POST API with <inputPayload> and data-partition-id as <tenant>
    Then Service should respond back with <postReponseStatusCode>
    When I hit File service GET download signed API with a valid Id
    Then download service should respond back with a valid <getReponseStatusCode>

    #When I hit signed url to download a file within expiration period at <outPathToCreateFile>
    #And content of the file uploaded <outputFilePath> and downloaded <inputFilePath> files is same
    Examples:
      | getReponseStatusCode | inputPayload                               | postReponseStatusCode | tenant            | inputFilePath                              | outputFilePath                                            | outPathToCreateFile                                                          |
      | "200"                | "/input_payloads/File_CorrectPayload.json" | "201"                 | "PRIVATE_TENANT1" | "/sample_upload_files/test.csv"            | "/sample_downloaded_files/test_downloaded.csv"            | "/src/test/resources/sample_downloaded_files/test_downloaded.csv"            |
      | "200"                | "/input_payloads/File_CorrectPayload.json" | "201"                 | "PRIVATE_TENANT1" | "/sample_upload_files/TestDownloadUrl.txt" | "/sample_downloaded_files/TestDownloadUrl_downloaded.txt" | "/src/test/resources/sample_downloaded_files/TestDownloadUrl_downloaded.txt" |

  #Positive scenario for FILE service
  @File
  Scenario Outline: Verify that metadata can be retrieved and is same as the time of posting
    Given I hit File service GET uploadURL API
    Then service should respond back with a valid <getReponseStatusCode> and upload input file from <inputFilePath>
    When I hit File service metadata service POST API with <inputPayload> and data-partition-id as <tenant>
    Then Service should respond back with <postReponseStatusCode>
    When I hit File service GET metadata signed API with a valid Id
    Then metadata service should respond back with a valid <getReponseStatusCode>

    Examples:
      | getReponseStatusCode | inputPayload                               | postReponseStatusCode | tenant            | inputFilePath                   |
      | "200"                | "/input_payloads/File_CorrectPayload.json" | "201"                 | "PRIVATE_TENANT1" | "/sample_upload_files/test.csv" |

  @File
  Scenario Outline: Verification for expiry time feature of downloadURL
    Given I hit File service GET uploadURL API
    Then service should respond back with a valid <getReponseStatusCode> and upload input file from <inputFilePath>
    When I hit File service metadata service POST API with <inputPayload> and data-partition-id as <tenant>
    Then Service should respond back with <postReponseStatusCode>
    When I hit File service GET download signed API with a valid Id
    Then I should be able to download the file within expiry period
    And I should not be able to download the file after expiry period

    Examples:
      | getReponseStatusCode | inputPayload                               | postReponseStatusCode | tenant            | inputFilePath                   | outputFilePath                                 | outPathToCreateFile                                               |
      | "200"                | "/input_payloads/File_CorrectPayload.json" | "201"                 | "PRIVATE_TENANT1" | "/sample_upload_files/test.csv" | "/sample_downloaded_files/test_downloaded.csv" | "/src/test/resources/sample_downloaded_files/test_downloaded.csv" |

  @File
  Scenario Outline: Verify that checksum is added to the file's metadata
    Given I hit File service GET uploadURL API
    Then service should respond back with a valid <getResponseStatusCode> and upload input file from <inputFilePath>
    When I hit File service metadata service POST API with <inputPayload> and data-partition-id as <tenant>
    Then Service should respond back with <postResponseStatusCode>
    When I hit File service GET metadata signed API with a valid Id
    Then Service should respond metadata with not null checksum <getResponseStatusCode>

    Examples:
      | getResponseStatusCode | inputPayload                                    | postResponseStatusCode | tenant            | inputFilePath                   |
      | "200"                 |"/input_payloads/File_Calculate_Checksum.json"   |"201"                   |"PRIVATE_TENANT1"  | "/sample_upload_files/test.csv" |
