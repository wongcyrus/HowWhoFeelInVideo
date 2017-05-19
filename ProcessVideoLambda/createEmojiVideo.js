//Reference https://github.com/russmatney/lambda-pngs-to-mp4
const AWSXRay = require('aws-xray-sdk');
const aws = AWSXRay.captureAWS(require('aws-sdk'));

const Q = require('q');
const path = require('path');

const execute = require('lambduh-execute');
const s3Download = require('lambduh-get-s3-object');
const upload = require('lambduh-put-s3-object');

process.env['PATH'] = process.env['PATH'] + ':/tmp/:' + process.env['LAMBDA_TASK_ROOT'];

exports.handler = (event, context, callback) => {
    console.log(event);
    let result = {};
    execute(result, {
        shell: 'mkdir -p /tmp/pngs/;',
        logOutput: true
    }).then((result) => {
        console.log("Set up ffmpeg.");
        return execute(result, {
            shell: `cp /var/task/lib/ffmpeg /tmp/.; chmod 755 /tmp/ffmpeg;`, // copies an ffmpeg binary to /tmp/ and chmods permissions to run it
            logOutput: true
        });
    }).then((result) => {
        //download pngs
        let def = Q.defer();

        let promises = [];
        for (let i in event.keys) {
            promises.push(s3Download(event, {
                srcBucket: event.bucket,
                srcKey: event.prefix + "/" + event.keys[i],
                downloadFilepath: '/tmp/pngs/' + path.basename(event.keys[i])
            }))
        }

        promises.push(s3Download(event, {
            srcBucket: event.bucket,
            srcKey: event.prefix + "/audio.aac",
            downloadFilepath: '/tmp/audio.aac'
        }))

        Q.all(promises).then((r) => {
            def.resolve(result);
        }).fail((err) => {
            def.reject(err);
        });
        return def.promise;

    }).then((result) => { //convert pngs to mp4
        return execute(result, {
            shell: `#!/bin/bash            
# make the mp4
ffmpeg -r 1 \
  -i /tmp/pngs/%04d.png \
  -i /tmp/audio.aac \
  -c:v libx264 \
  -c:a aac \
  -strict -2 \
  -pix_fmt yuv420p \
  -framerate 1 \
  -y /tmp/video.mp4
  echo "Completed Video!"
  ls /tmp/*.mp4
  `,
            logOutput: true
        });
    }).then((result) => {
        //upload mp4
        return upload(result, {
            dstBucket: event.bucket,
            dstKey: event.prefix + "/video.mp4",
            uploadFilepath: '/tmp/video.mp4'
        });
    }).then((result) => {
        //clean up
        return execute(result, {
            shell: "rm -f /tmp/pngs/*;"
        })
    }).then((result) => {
        console.log('finished');
        console.log(result);
        context.done(null, event);
    }).fail((err) => {
        console.log(JSON.stringify(err));
        context.done(JSON.stringify(err));
    });
};