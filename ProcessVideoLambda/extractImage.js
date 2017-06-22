const Q = require('q');
const path = require('path');

const AWSXRay = require('aws-xray-sdk');
const aws = AWSXRay.captureAWS(require('aws-sdk'));
const execute = require('lambduh-execute');
const s3Download = require('lambduh-get-s3-object');
const uploadDirToS3 = require('upload-dir-to-s3');
const fs = require('fs');

process.env['PATH'] = process.env['PATH'] + ':/tmp/:' + process.env['LAMBDA_TASK_ROOT'];

exports.handler = (event, context, callback) => {
// Get the object from the event and show its content type
    console.log(JSON.stringify(event));
    const bucket = event.bucket;
    const key = event.key;

    let filename = key.split(".")[0];

    let result = {};
    s3Download(result, {
        srcBucket: bucket,
        srcKey: key,
        downloadFilepath: "/tmp/" + key
    }).then((result) => {
        console.log("Set up ffmpeg.");
        return execute(result, {
            shell: `cp /var/task/lib/ffmpeg /tmp/.; chmod 755 /tmp/ffmpeg; rm -rf /tmp/${filename}; mkdir /tmp/${filename}`, // copies an ffmpeg binary to /tmp/ and chmods permissions to run it
            logOutput: true
        });
    }).then((result) => {
        console.log("Convert MP4 to jpg.");
        return execute(result, {
            shell: `ffmpeg -i /tmp/${key} -vf fps=1 /tmp/${filename}/%4d.jpg`, //1 frame per 10 seconds
            logOutput: true
        });
    }).then((result) => {
        console.log("Upload jpg to s3");
        let def = Q.defer();

        let directory = `/tmp/${filename}/`;
        let keyPrefix = filename + "/";

        uploadDirToS3(directory, bucket, keyPrefix)
            .then((modifiedKeySet) => {
                console.log('Upload Complete, modified the following keys', modifiedKeySet);
                def.resolve(result);
            });

        return def.promise;
    }).then((result) => {
        console.log("get keys!");
        let def = Q.defer();
        fs.readdir(`/tmp/${filename}/`, (err, items) => {
            console.log(items);
            let data = {bucket: bucket, prefix: filename, keys: items};
            console.log(result);
            context.done(null, data);
            def.resolve(data);
        });
        return def.promise;
    }).then((result) => {
        //clean up
        return execute(result, {
            shell: `re -f /tmp/${filename}/*;`
        })
    }).then((result) => {
        console.log('finished');
        console.log(result);
        callback(null, result);
    }).fail((err) => {
        console.error("Error");
        console.log(err);
        callback(err);
    });
};