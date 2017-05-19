//Follow https://medium.com/@SlyFireFox/micro-services-with-aws-lambda-and-api-gateway-part-1-f11aaaa5bdef
//Run
//npm install -g grunt-cli
//npm install grunt-aws-lambda grunt-pack --save-dev

let grunt = require('grunt');
grunt.loadNpmTasks('grunt-aws-lambda');

grunt.initConfig({
    lambda_invoke: {
        default: {}
    },
    lambda_deploy: {
        extractImage: {
            arn: 'arn:aws:lambda:us-east-1:641280019922:function:extractImage',
            options: {
                region: 'us-east-1',
                handler: 'extractImage.handler'
            }
        },
        cascadingFaceDetection: {
            arn: 'arn:aws:lambda:us-east-1:641280019922:function:cascadingFaceDetection',
            options: {
                region: 'us-east-1',
                handler: 'cascadingFaceDetection.handler'
            }
        },
        startFaceDetectionWorkFlow: {
            arn: 'arn:aws:lambda:us-east-1:641280019922:function:startFaceDetectionWorkFlow',
            options: {
                region: 'us-east-1',
                handler: 'startFaceDetectionWorkFlow.handler'
            }
        },
        createEmojiVideo: {
            arn: 'arn:aws:lambda:us-east-1:641280019922:function:createEmojiVideo',
            options: {
                region: 'us-east-1',
                handler: 'createEmojiVideo.handler'
            }
        },
        createEmotionReport: {
            arn: 'arn:aws:lambda:us-east-1:641280019922:function:createEmotionReport',
            options: {
                region: 'us-east-1',
                handler: 'createEmotionReport.handler'
            }
        }
        ,
        extractAudio: {
            arn: 'arn:aws:lambda:us-east-1:641280019922:function:extractAudio',
            options: {
                region: 'us-east-1',
                handler: 'extractAudio.handler'
            }
        }
    },
    lambda_package: {
        extractImage: {
            options: {
                include_time: false,
                include_version: false
            }
        },
        cascadingFaceDetection: {
            options: {
                include_time: false,
                include_version: false
            }
        },
        startFaceDetectionWorkFlow: {
            options: {
                include_time: false,
                include_version: false
            }
        },
        createEmojiVideo: {
            options: {
                include_time: false,
                include_version: false
            }
        },
        createEmotionReport: {
            options: {
                include_time: false,
                include_version: false
            }
        },
        extractAudio: {
            options: {
                include_time: false,
                include_version: false
            }
        }
    }
});

grunt.registerTask('deploy_extractImage', ['lambda_package:extractImage', 'lambda_deploy:extractImage']);
grunt.registerTask('deploy_cascadingFaceDetection', ['lambda_package:cascadingFaceDetection', 'lambda_deploy:cascadingFaceDetection']);
grunt.registerTask('deploy_startFaceDetectionWorkFlow', ['lambda_package:startFaceDetectionWorkFlow', 'lambda_deploy:startFaceDetectionWorkFlow']);
grunt.registerTask('deploy_createEmojiVideo', ['lambda_package:createEmojiVideo', 'lambda_deploy:createEmojiVideo']);
grunt.registerTask('deploy_createEmotionReport', ['lambda_package:createEmotionReport', 'lambda_deploy:createEmotionReport']);
grunt.registerTask('deploy_extractAudio', ['lambda_package:extractAudio', 'lambda_deploy:extractAudio']);

grunt.registerTask('deploy_all', ['deploy_extractImage', 'deploy_cascadingFaceDetection','deploy_startFaceDetectionWorkFlow','deploy_createEmojiVideo','deploy_createEmotionReport','deploy_extractAudio']);