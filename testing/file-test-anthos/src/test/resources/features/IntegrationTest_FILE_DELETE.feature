Feature: File Service DELETE API integration test

  Background: 
    Given I generate user token and set request headers with "PRIVATE_TENANT1"

  @File
  Scenario Outline: Verify that metadata is deleted by delete endpoint and gives 404 for invalid ID
    Given I hit File service GET uploadURL API
    Then service should respond back with a valid <getReponseStatusCode> and upload input file from <inputFilePath>
    When I hit File service metadata service POST API with <inputPayload> and data-partition-id as <tenant>
    Then Service should respond back with <postReponseStatusCode>
    When I hit File service Delete metadata endpoint with a valid Id
    Then Delete service should respond back with <deleteReponseStatusCode>
    When I hit File service Delete metadata endpoint with a invalid Id
    Then Delete service should respond back with <invalideDeleteReponseStatusCode>

    Examples: 
      | getReponseStatusCode | inputPayload                               | postReponseStatusCode | tenant            | deleteReponseStatusCode | invalideDeleteReponseStatusCode | inputFilePath                   |
      | "200"                | "/input_payloads/File_CorrectPayload.json" | "201"                 | "PRIVATE_TENANT1" | "204"                   | "404"                           | "/sample_upload_files/test.csv" |
