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
                    def bugPayloads = []
                    for (failedTest in failedTests) {
                        def bugPayload = [:]
                        bugPayload['title'] = "Bug in test ${failedTest.name}"
                        echo "name test: ${failedTests.name}"
                        bugPayload['status'] = 'Non-Resolved'
                        bugPayload['description'] = "Test failed with error message: ${failedTest.error}"
                        bugPayload['projectname'] = "calculator-project"
                        echo ${bugPayload}
                        bugPayloads.add(bugPayload)
                    }
                    println "Bug Payloads: ${bugPayloads}"




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
