const AWSXRay = require('aws-xray-sdk');
const aws = AWSXRay.captureAWS(require('aws-sdk'));
const Q = require('q');
const path = require('path');

const execute = require('lambduh-execute');
const s3Download = require('lambduh-get-s3-object');
const jsonfile = require('jsonfile');
const fs = require('fs');
const upload = require('lambduh-put-s3-object');

exports.handler = (event, context, callback) => {

    console.log(event);

    let getJson = () => {
        //download json
        let def = Q.defer();

        let promises = [];
        for (let i in event.keys) {
            let jsonKey = event.keys[i].replace(".png", ".json");
            promises.push(s3Download({}, {
                srcBucket: event.bucket,
                srcKey: event.prefix + "/" + jsonKey,
                downloadFilepath: '/tmp/' + path.basename(jsonKey)
            }))
        }
        Q.all(promises).then((r) => {
            console.log(r);
            def.resolve(r);
        }).fail((err) => {
            console.log(err);
            def.reject(err);
        });
        return def.promise;
    };

    getJson()
        .then((result) => {
            console.log("Read All Json files.");
            let def = Q.defer();
            let promises = [];
            for (let i in event.keys) {
                let jsonKey = event.keys[i].replace(".png", ".json");
                let filePath = '/tmp/' + path.basename(jsonKey);

                let d = Q.defer();
                jsonfile.readFile(filePath, (err, obj) => {
                    d.resolve(obj);
                });
                promises.push(d.promise);
            }
            Q.all(promises).then((r) => {
                result.data = r;
                def.resolve(result);
            }).fail((err) => {
                def.reject(err);
            });
            return def.promise;
        }).then((result) => {
        console.log("Convert to CSV.");

        let def = Q.defer();
        let data = "seq,id,happy,sad,angry,confused,disgusted,surprised,calm,unknown\n";
        let jsonObjects = result.data;

        for (let i in jsonObjects) {
            for (let j in jsonObjects[i]) {
                let f = jsonObjects[i][j];
                console.log(f);
                data += `${f.seq},${f.id},${f.happy},${f.sad},${f.angry},${f.confused},${f.disgusted},${f.surprised},${f.calm},${f.unknown}\n`
            }
        }

        console.log(data);
        fs.appendFile('/tmp/report.csv', data, (err) => {
            if (err) {
                console.error("report Error" + err);
                def.reject(err);
            } else {
                console.log("report ok");
                def.resolve();
            }
        });
        return def.promise;
    }).then((result) => {
        console.log("Upload report to S3.");
        //upload csv
        return upload(result, {
            dstBucket: event.bucket,
            dstKey: event.prefix + "/report.csv",
            uploadFilepath: '/tmp/report.csv'
        });
    }).then((result) => {
        //clean up
        return execute(result, {
            shell: "rm -f /tmp/*;"
        })
    }).then((result) => {
        console.log('finished');
        console.log(result);
        callback(null, event);
    }).fail((err) => {
        console.log(JSON.stringify(err));
        callback(JSON.stringify(err));
    });

};
