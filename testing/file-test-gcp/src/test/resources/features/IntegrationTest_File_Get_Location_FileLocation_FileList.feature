Feature: File service Get Location API integration test

  ## Negative scenario for GET /getFileLocation and /getFileList
  @File
  Scenario Outline: Verify that File service's getFileLocation API returns bad request for non-existing file id
    Given I hit File service GetFileLocation API with non-existing file id
    Then service should respond back with <ReponseStatusCode>

    Examples: 
      | ReponseStatusCode |
      | "400"             |

  ## Negative scenario for GET /getFileLocation and /getFileList
  @File
  Scenario Outline: Verify that File service's getFileLocation and getFileList API returns unauthorized for invalid partition id
    Given I hit File service <API> with invalid partition id
    Then service should respond back with <ReponseStatusCode> or <AlternateResponseCode>

    Examples: 
      | API               | ReponseStatusCode | AlternateResponseCode |
      | "GetFileLocation" | "401"             | "403"                 |
      | "GetFileList"     | "401"             | "403"                 |

  ## Negative scenario for GET /getFileLocation and /getFileList
  @File
  Scenario Outline: Verify that File service's getFileLocation and getFileList API returns unauthorized if partition id is missing
    Given I hit File service <API> without partition id
    Then service should respond back with <ReponseStatusCode>

    Examples: 
      | API               | ReponseStatusCode |
      | "GetFileLocation" | "401"             |
      | "GetFileList"     | "401"             |

  ## Negative scenario for GET /getFileLocation and /getFileList
  @File
  Scenario Outline: Verify that File service's getFileLocation and getFileList API returns unauthorized if auth token is missing
    Given I hit File service <API> without auth token
    Then service should respond back with <ReponseStatusCode> or <AlternateResponseCode>

    Examples: 
      | API               | ReponseStatusCode | AlternateResponseCode |
      | "GetFileLocation" | "401"             | "403"                 |
      | "GetFileList"     | "401"             | "403"                 |

  ## Negative scenario for GET /getFileLocation and /getFileList
  @FailingForGCP
  Scenario Outline: Verify that File service's getFileLocation and getFileList API returns unauthorized for invalid auth token
    Given I hit File service <API> with invalid auth token
    Then service should respond back with <ReponseStatusCode> or <AlternateResponseCode>

    Examples: 
      | API               | ReponseStatusCode | AlternateResponseCode |
      | "GetFileLocation" | "401"             | "403"                 |
      | "GetFileList"     | "401"             | "403"                 |

  ## Negative scenario for GET /getFileLocation
  @File
  Scenario Outline: Verify that File service's getFileLocation API returns bad request for empty request body
    Given I hit File service GetFileLocation API with <BodyContent>
    Then service should respond back with <ReponseStatusCode> and <ReponseMessage>

    Examples: 
      | BodyContent    | ReponseStatusCode | ReponseMessage                                              |
      | "emptyReqBody" | "400"             | "ConstraintViolationException: Invalid FileLocationRequest" |

  ## Negative scenario for GET /getFileLocation
  @File
  Scenario Outline: Verify that File service's getFileLocation API returns bad request for invalid File Id
    Given I hit File service GetFileLocation API with <BodyContent>
    Then service should respond back with <ReponseStatusCode> and error message <ReponseMessage>

    Examples: 
      | BodyContent     | ReponseStatusCode | ReponseMessage                         |
      | "invalidFileId" | "400"             | "Not found location for fileID : test" |

  ## Negative scenario for GET /getLocation
  @File
  Scenario Outline: Verify that File service's getLocation API returns bad request for invalid file location
    Given I hit File service GetLocation API with <BodyContent>
    Then service should respond back with <ReponseStatusCode>

    Examples: 
      | BodyContent             | ReponseStatusCode |
      | "invalid file location" | "400"             |

  ## Negative scenario for GET /getLocation
  @File
  Scenario Outline: Verify that File service's getLocation API returns bad request if file id length exceeds limit
    Given I hit File service GetLocation API with <BodyContent>
    Then service should respond back with <ReponseStatusCode> and <ReponseMessage>

    Examples: 
      | BodyContent                    | ReponseStatusCode | ReponseMessage                                                                        |
      | "fileId legth exceeding limit" | "400"             | "The maximum filepath length is 1024 characters, but got a name with 1062 characters" |

  ## Negative scenario for GET /getLocation
  @File
  Scenario Outline: Verify that File service's getLocation API returns bad request for existing file id
    Given I hit File service GetLocation API with existing file id
    Then service should respond back with <ReponseStatusCode>

    Examples: 
      | ReponseStatusCode |
      | "400"             |

  ## Negative scenario for GET /getFileList
  @File
  Scenario Outline: Verify that File service's GetFileList API returns bad request for invalid request
    Given I hit File service GetFileList API with <InputPayload>
    Then service should respond back with <ReponseStatusCode>

    Examples: 
      | InputPayload                                                                         | ReponseStatusCode |
      | "/input_payloads/GetLocation_FileList_FileLocation/File_GetList_InvalidPayload.json" | "400"             |

  ## Negative scenario for GET /getFileList
  @File
  Scenario Outline: Verify that File service's GetFileList API returns bad request if no records are found
    Given I hit File service GetFileList API with <InputPayload>
    Then service should respond back with <ReponseStatusCode>

    Examples: 
      | InputPayload                                                                          | ReponseStatusCode |
      | "/input_payloads/GetLocation_FileList_FileLocation/File_GetList_NoRecordPayload.json" | "400"             |

  ## Negative scenario for GET /getFileList
  @File
  Scenario Outline: Verify that File service's GetFileList API returns bad request for empty request body
    Given I hit File service GetFileList API with <InputPayload>
    Then service should respond back with <ReponseStatusCode>

    Examples: 
      | InputPayload                                                                       | ReponseStatusCode |
      | "/input_payloads/GetLocation_FileList_FileLocation/File_GetList_EmptyPayload.json" | "400"             |

  ## Positive scenario for GET /getLocation
  @File
  Scenario Outline: Verify that File service's GET getFileLocation API returns File Id and SignedURL even if File Id is not provided in request
    Given I hit File service GetLocation API without File Id
    Then service should respond back with <ReponseStatusCode> , File Id and Signed URL

    Examples: 
      | ReponseStatusCode |
      | "200"             |

  ## Positive scenario for GET /getLocation
  @File
  Scenario Outline: Verify that File service's GET getFileLocation API responds back with File Id and SignedURL for File Id provided in request
    Given I hit File service GetLocation API with a File Id
    Then service should respond back with <ReponseStatusCode> , File Id and Signed URL

    Examples: 
      | ReponseStatusCode |
      | "200"             |

  ## Positive scenario for GET /getFileLocation
  @File
  Scenario Outline: Verify that File service's GET getFileLocation API responds back with File Id and UnSignedURL for File Id returned by getLocation API
    #Given I hit File service GetLocation API with a File Id and save the Id returned
    And I hit File service GetFileLocation API with same File Id
    Then service should respond back with <ReponseStatusCode> and UnSigned URL

    Examples: 
      | ReponseStatusCode |
      | "200"             |