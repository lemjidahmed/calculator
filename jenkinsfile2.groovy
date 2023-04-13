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
                    junit 'target/surefire-reports/*.xml'
                        def failedTests = []
                        def results = currentBuild.rawBuild.getAction(hudson.tasks.junit.TestResultAction.class).getResult()
                        results.getFailedTests().each {
                            failedTests.add(it.getFullName())
                        }
                        echo "Failed tests: ${failedTests}"
                        echo "eeeee"
                    def bugPayloads = []
                    for (failedTest in failedTests) {
                        def bugPayload = [:]
                        bugPayload['title'] = "Bug in test ${failedTest}"
                        echo "name test: ${bugPayload['title']}"
                        bugPayload['status'] = 'Non-Resolved'
                        bugPayload['description'] = "Test failed with error message: ${failedTest}"
                        bugPayload['projectname'] = "calculator-project"
                        bugPayloads.add(bugPayload)
                    }
                    echo "Bug Payloads: ${bugPayloads}"



                    // send the JSON payloads to the bug tracker application
                    for (bugPayload in bugPayloads) {
                        echo "F:${bugPayload}"
                        sh "echo '${bugPayload}' | http POST http://localhost:8081/Bug"
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
