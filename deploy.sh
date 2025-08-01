#mvn clean install

API_GW_JAR_FILE=$(find ./api-gateway-lambda/target -name "api-gateway-lambda-*-aws-*.jar")
EVT_PROCESSOR_JAR_FILE=$(find ./event-processor-lambda/target -name "event-processor-lambda-*-aws-*.jar")
DOWNLOAD_FILE_JAR_FILE=$(find ./download-file-lambda/target -name "download-file-lambda-*-aws-*.jar")

BASE_API_GW_JAR_FILE=$(basename $API_GW_JAR_FILE)
BASE_EVT_PROCESSOR_JAR_FILE=$(basename $EVT_PROCESSOR_JAR_FILE)
BASE_DOWNLOAD_FILE_JAR_FILE=$(basename $DOWNLOAD_FILE_JAR_FILE)

if ! aws s3 ls "s3://lambda-deployment-event-bridge" 2>/dev/null; then
  aws s3 mb s3://lambda-deployment-event-bridge
  echo "Bucket created."
else
  echo "Bucket already exists."
fi

if aws s3 ls "s3://lambda-deployment-event-bridge/$BASE_DOWNLOAD_FILE_JAR_FILE" > /dev/null 2>&1; then
  echo "File already exists in S3. Skipping upload."
else
  aws s3 cp "$DOWNLOAD_FILE_JAR_FILE" s3://lambda-deployment-event-bridge
  echo "File uploaded to S3."
fi


if aws s3 ls "s3://lambda-deployment-event-bridge/$BASE_API_GW_JAR_FILE" > /dev/null 2>&1; then
  echo "File already exists in S3. Skipping upload."
else
  aws s3 cp "$API_GW_JAR_FILE" s3://lambda-deployment-event-bridge
  echo "File uploaded to S3."
fi

if aws s3 ls "s3://lambda-deployment-event-bridge/$BASE_EVT_PROCESSOR_JAR_FILE" > /dev/null 2>&1; then
  echo "File already exists in S3. Skipping upload."
else
  aws s3 cp "$EVT_PROCESSOR_JAR_FILE" s3://lambda-deployment-event-bridge
  echo "File uploaded to S3."
fi

aws s3 cp ./cloudformation/api-lambda.yml   s3://lambda-deployment-event-bridge
aws s3 cp ./cloudformation/event-bridge.yml s3://lambda-deployment-event-bridge
aws s3 cp ./cloudformation/event-processor-lambda.yml s3://lambda-deployment-event-bridge
aws s3 cp ./cloudformation/download-file-lambda.yml s3://lambda-deployment-event-bridge


SAM_PARAMETERS=$(yq -r 'to_entries | map("ParameterKey=\(.key),ParameterValue=\(.value)") | join(" ")' ./cloudformation/parameters.yml)

# Replace placeholders in the parameters string using sed
SAM_PARAMETERS=$(echo "$SAM_PARAMETERS" | sed "s/ParameterValue=API_GW_JAR_FILE/ParameterValue=$BASE_API_GW_JAR_FILE/g")
SAM_PARAMETERS=$(echo "$SAM_PARAMETERS" | sed "s/ParameterValue=EVT_PROCESSOR_JAR_FILE/ParameterValue=$BASE_EVT_PROCESSOR_JAR_FILE/g")
SAM_PARAMETERS=$(echo "$SAM_PARAMETERS" | sed "s/ParameterValue=DOWNLOAD_FILE_JAR_FILE/ParameterValue=$BASE_DOWNLOAD_FILE_JAR_FILE/g")


sam deploy --template-file ./cloudformation/template.yml --stack-name event-bridge-lambdas --parameter-overrides $SAM_PARAMETERS --capabilities CAPABILITY_IAM
