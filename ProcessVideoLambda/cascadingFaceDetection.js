const AWSXRay = require('aws-xray-sdk');
const aws = AWSXRay.captureAWS(require('aws-sdk'));
aws.config.update({region: 'us-east-1'});
const lambda = new aws.Lambda();

exports.handler = (event, context, callback) => {
    let bucket = event.bucket;
    let prefix = event.prefix;
    let keys = event.keys;

    console.log(event);

    let invokeLambda = (key) => new Promise((resolve, reject) => {
        let data = JSON.stringify({bucket: bucket, key: prefix + "/" + key});
        let params = {
            FunctionName: 'arn:aws:lambda:us-east-1:641280019922:function:processImage', /* required */
            Payload: data /* required */
        };
        lambda.invoke(params, (err, data) => {
            if (err) reject(err, err.stack); // an error occurred
            else     resolve(data);           // successful response
        });
    });

    let invokeLambdaPromises = keys.map(invokeLambda);
    Promise.all(invokeLambdaPromises).then(() => {
            let pngKey = keys.map(key => key.split(".")[0] + ".png");
            let data = {bucket: bucket, prefix: prefix, keys: pngKey};
            console.log("involveLambdaPromises complete!");
            context.done(null, data);
        }
    ).catch(err => {
        console.log("involveLambdaPromises failied!");
        context.done(err);
    });
};

