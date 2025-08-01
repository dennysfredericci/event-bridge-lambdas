

if aws s3 ls "s3://event-bridge-documents" 2>/dev/null; then
  aws s3 rb s3://event-bridge-documents --force
fi



sam delete --stack-name event-bridge-lambdas

if ! aws s3 ls "s3://lambda-deployment-event-bridge" 2>/dev/null; then
  echo "Bucket not found"
else
  aws s3 rb s3://lambda-deployment-event-bridge --force
  echo "Bucket removed"
fi

