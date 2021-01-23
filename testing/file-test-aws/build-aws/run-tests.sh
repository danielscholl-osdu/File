# Copyright © 2020 Amazon Web Services
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# This script prepares the dist directory for the integration tests.
# Must be run from the root of the repostiory

# This script executes the test and copies reports to the provided output directory
# To call this script from the service working directory
# ./dist/testing/integration/build-aws/run-tests.sh "./reports/"

echo "### Running File Service Integration Tests... ###"


SCRIPT_SOURCE_DIR=$(dirname "$0")
echo "Script source location"
echo "$SCRIPT_SOURCE_DIR"
(cd "$SCRIPT_SOURCE_DIR"/../bin && ./install-deps.sh)

#### ADD REQUIRED ENVIRONMENT VARIABLES HERE ###############################################
# The following variables are automatically populated from the environment during integration testing
# see os-deploy-aws/build-aws/integration-test-env-variables.py for an updated list

***REMOVED***_COGNITO_CLIENT_ID
# File Url
export INTEGRATION_TESTER=$
export AWS_COGNITO_AUTH_FLOW=USER_PASSWORD_AUTH
export AWS_COGNITO_AUTH_PARAMS_PASSWORD=$ADMIN_PASSWORD
export AWS_COGNITO_AUTH_PARAMS_USER=$ADMIN_USER
export AWS_COGNITO_AUTH_PARAMS_USER_NO_ACCESS=$USER_NO_ACCESS
export AWS_COGNITO_CLIENT_ID=$AWS_COGNITO_CLIENT_ID
export DOMAIN=testing.com
export FILE_SERVICE_HOST=${FILE_URL}
export DATA_PARTITION_ID=int-test-file
export ENVIRONMENT=$RESOURCE_PREFIX

#Delivery
export DEFAULT_DATA_PARTITION_ID_TENANT1=opendes
export DEFAULT_DATA_PARTITION_ID_TENANT2=common
export ENTITLEMENTS_DOMAIN=testing.com
export LEGAL_TAG=opendes-public-usa-dataset-1
export OTHER_RELEVANT_DATA_COUNTRIES=US
export SEARCH_HOST=$SEARCH_URL
export STORAGE_HOST=$STORAGE_URL
export LEGAL_HOST=$LEGAL_URL
export DELIVERY_INT_TEST_BUCKET_NAME="${RESOURCE_PREFIX}-osdu-delivery-integration-test-bucket"
export AWS_S3_REGION=us-east-1

#### RUN INTEGRATION TEST #########################################################################

mvn test -f "$SCRIPT_SOURCE_DIR"/../pom.xml
TEST_EXIT_CODE=$?

#### COPY TEST REPORTS #########################################################################

if [ -n "$1" ]
  then
    mkdir -p "$1"
    cp -R "$SCRIPT_SOURCE_DIR"/../target/surefire-reports "$1"
fi

echo "### File Service Integration Tests Finished ###"

exit $TEST_EXIT_CODE