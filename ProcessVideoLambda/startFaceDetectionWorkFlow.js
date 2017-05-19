const AWSXRay = require('aws-xray-sdk');
const aws = AWSXRay.captureAWS(require('aws-sdk'));

const stepfunctions = new aws.StepFunctions();
exports.handler = (event, context, callback) => {
    let bucket = event.Records[0].s3.bucket.name;
    let key = decodeURIComponent(event.Records[0].s3.object.key.replace(/\+/g, ' '));

    let params = {
        stateMachineArn: 'arn:aws:states:us-east-1:641280019922:stateMachine:FaceDetectionAttendance', /* required */
        input: JSON.stringify({bucket, key})
    };
    stepfunctions.startExecution(params, function (err, data) {
        if (err) console.log(err, err.stack); // an error occurred
        else {
            console.log(data);           // successful response
            context.done(data);
        }
    });
};