# HowWhoFeelInVideo

IAM Role for Lambda execution 

AWSXrayWriteOnlyAccess
AmazonRekognitionFullAccess
AWSLambdaExecute
AWSLambdaBasicExecutionRole

S3 Assess for the put assess for the bucket.

S3 Trigger Step Functions
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "Stmt1494399014000",
            "Effect": "Allow",
            "Action": [
                "states:StartExecution"
            ],
            "Resource": "*"
        },
        {
            "Effect": "Allow",
            "Action": [
                "lambda:InvokeFunction"
            ],
            "Resource": "arn:aws:lambda:us-east-1:XXXXXXXXXXXXXX:function:processImage"
        }
    ]
}