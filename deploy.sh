#mvn clean install

API_GW_JAR_FILE=$(find ./api-gateway-lambda/target -name "api-gateway-lambda-*-aws-*.jar")

echo "Found JAR file: $API_GW_JAR_FILE"

TIMESTAMP=$(echo $API_GW_JAR_FILE | sed -E 's/.*-aws-([0-9]+)\.jar$/\1/')
echo $TIMESTAMP

BASE_API_GW_JAR_FILE=$(basename $API_GW_JAR_FILE)

if ! aws s3 ls "s3://lambda-deployment-event-bridge" 2>/dev/null; then
  aws s3 mb s3://lambda-deployment-event-bridge
  echo "Bucket created."
else
  echo "Bucket already exists."
fi

aws s3 cp "$API_GW_JAR_FILE" s3://lambda-deployment-fredericci

SAM_PARAMETERS=$(yq -r 'to_entries | map("ParameterKey=\(.key),ParameterValue=\(.value)") | join(" ")' ./cloudformation/parameters.yml)

# Replace placeholders in the parameters string using sed
SAM_PARAMETERS=$(echo "$SAM_PARAMETERS" | sed "s/ParameterValue=API_GW_JAR_FILE/ParameterValue=$BASE_API_GW_JAR_FILE/g")

echo ""
echo "Parameters:"
echo $SAM_PARAMETERS
echo ""

sam deploy --template-file ./cloudformation/api-lambda.yml --stack-name event-bridge-lambdas --parameter-overrides $SAM_PARAMETERS --capabilities CAPABILITY_IAM

#mv ./target/spring-lambda-0.0.1-SNAPSHOT-aws.jar ./target/spring-lambda.jar
#aws s3 cp ./target/spring-lambda.jar s3://lambda-deployment-fredericci
#SAM_PARAMETERS=$(yq -r 'to_entries | map("ParameterKey=\(.key),ParameterValue=\(.value)") | join(" ")' parameters.yml)
#sam deploy --template-file ./template.yaml --stack-name lambdas-benchmark --parameter-overrides $SAM_PARAMETERS --capabilities CAPABILITY_IAM