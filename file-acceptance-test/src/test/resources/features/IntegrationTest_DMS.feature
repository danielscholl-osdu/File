Feature: File Service DMS API integration tests

  #@FileDMS
  Scenario Outline: A scenario
    When I send request for storage instructions i should receive valid response
    Then I should be able to upload file from <inputFilePath> with provided instruction
    Then I should be able to register metadata with <metadataInputPayload> and uploaded file
    Then I should be able request for retrieval instructions for uploaded file by <datasetRegistryInputPayload>
    Then I should be able retrieve file by provided instructions, and downloaded files is same as <inputFilePath>

    Examples:
      | inputFilePath                   | metadataInputPayload                       | datasetRegistryInputPayload                              |
      | "/sample_upload_files/test.csv" | "/input_payloads/File_CorrectPayload.json" | "/input_payloads/dms_input_payload/datasetregistry.json" |
