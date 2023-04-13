pipeline {
    agent any
    stages {
        stage('Build') {

            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    sh 'mvn package'
                }

            }

        }
        stage('Test') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    sh 'mvn test'
                }

            }
        }
        stage('Report Bugs') {
            steps {
                script {
                    sh 'mvn test'
                    junit 'target/surefire-reports/*.xml'
                    script {
                        def failedTests = []
                        def results = currentBuild.rawBuild.getAction(hudson.tasks.junit.TestResultAction.class).getResult()
                        results.getFailedTests().each {
                            failedTests.add(it.getFullName())
                        }
                        echo "Failed tests: ${failedTests}"
                    }

                    // create a JSON payload for each failed test
                    def bugPayloads = []
                    for (failedTest in failedTests) {
                        def bugPayload = [:]
                        bugPayload['id'] = null
                        bugPayload['title'] = "Bug in test ${failedTest.name}"
                        bugPayload['status'] = 'Non-Resolved'
                        bugPayload['description'] = "Test failed with error message: ${failedTest.error}"
                        bugPayload['projectname'] = projectName

                        bugPayloads.add(bugPayload)
                    }

                    // send the JSON payloads to the bug tracker application
                    for (bugPayload in bugPayloads) {
                        def bugPayloadJson = new groovy.json.JsonBuilder(bugPayload).toPrettyString()

                        sh "http.post(url: 'http://localhost:8081/Bug', contentType: 'application/json', body: bugPayloadJson)"

                    }
                }
            }
        }
        stage('Deploy') {
            steps {
                sh 'mvn deploy'
            }
        }
    }
}
