const AWSXRay = require('aws-xray-sdk');
const aws = AWSXRay.captureAWS(require('aws-sdk'));

const stepfunctions = new aws.StepFunctions();
exports.handler = (event, context, callback) => {
    let bucket = event.Records[0].s3.bucket.name;
    let key = decodeURIComponent(event.Records[0].s3.object.key.replace(/\+/g, ' '));

    console.log(key);
    //Source video will not contain "/"
    if (key.indexOf("/") > 0)
        return;

    console.log("Start new workflow for " + key);
    let params = {
        stateMachineArn: process.env['stateMachineArn'], /* required */
        input: JSON.stringify({bucket, key})
    };
    stepfunctions.startExecution(params, function (err, data) {
        if (err) console.log(err, err.stack); // an error occurred
        else {
            console.log(data);           // successful response
            callback(null, "success");
        }
    });
};