pipeline {

    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    parameters {
        booleanParam(name: 'FORCE_FAILURE', defaultValue: false, description: 'Modo prueba: fuerza un fallo para evidenciar la notificacion de error en Slack')
    }

    environment {
        APP_PORT    = '8085'
        APP_PID_FILE = '/tmp/psw-app.pid'
        SONAR_HOST  = 'http://sonarqube:9000'
        SONAR_TOKEN = credentials('sonar-token')
        SONAR_KEY   = 'psw-pipeline-base'
        JMETER_HOME = '/opt/jmeter'
        JMETER_PLAN = 'jmeter/Reto_Carga_100Usuarios.jmx'
        JMETER_OUT  = 'evidencias/04-jmeter'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
                sh 'git log --oneline -1 && ls -la'
            }
        }

        stage('Build') {
            steps {
                sh '''
                    set -e
                    if [ "$FORCE_FAILURE" = "true" ]; then
                        echo "[MODO PRUEBA] Simulando fallo de compilacion para validar la notificacion de error"
                        exit 1
                    fi
                    mvn -B clean package
                '''
            }
        }

        stage('Análisis SonarQube') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh '''
                        mvn -B org.sonarsource.scanner.maven:sonar-maven-plugin:3.9.1.2184:sonar \
                          -Dsonar.host.url=$SONAR_HOST \
                          -Dsonar.login=$SONAR_TOKEN \
                          -Dsonar.projectKey=$SONAR_KEY \
                          -Dsonar.projectName="PSW Pipeline Base"
                    '''
                }
            }
        }

        stage('Levantar aplicación') {
            steps {
                sh '''
                    set -e
                    nohup java -jar target/psw-pipeline-base-0.0.1-SNAPSHOT.jar > app-runtime.log 2>&1 &
                    echo $! > $APP_PID_FILE
                    for i in $(seq 1 60); do
                        if curl -sf http://localhost:$APP_PORT/actuator/health > /dev/null 2>&1; then
                            echo "Aplicacion lista en el intento $i"
                            curl -s http://localhost:$APP_PORT/actuator/health
                            exit 0
                        fi
                        sleep 2
                    done
                    echo "La aplicacion no arranco en 120s"; tail -50 app-runtime.log; exit 1
                '''
            }
        }

        stage('Pruebas de carga JMeter') {
            steps {
                sh '''
                    set -e
                    export HEAP="-Xms256m -Xmx512m -XX:MaxMetaspaceSize=256m"
                    mkdir -p $JMETER_OUT/reporte-html
                    rm -rf $JMETER_OUT/reporte-html/*
                    $JMETER_HOME/bin/jmeter -n \
                      -t $JMETER_PLAN \
                      -l $JMETER_OUT/resultados-pipeline.jtl \
                      -j $JMETER_OUT/jmeter-pipeline.log \
                      -e -o $JMETER_OUT/reporte-html
                '''
            }
            post {
                always {
                    archiveArtifacts artifacts: "$JMETER_OUT/resultados-pipeline.jtl, $JMETER_OUT/reporte-html/index.html", allowEmptyArchive: true
                }
            }
        }

        stage('Detener aplicación') {
            steps {
                sh '''
                    if [ -f $APP_PID_FILE ]; then
                        kill $(cat $APP_PID_FILE) || true
                        rm -f $APP_PID_FILE
                        echo "Aplicacion detenida"
                    fi
                '''
            }
        }
    }

    post {
        always {
            sh '''
                if [ -f $APP_PID_FILE ]; then
                    kill $(cat $APP_PID_FILE) 2>/dev/null || true
                    rm -f $APP_PID_FILE
                fi
            '''
            script {
                def resultado = currentBuild.currentResult
                def color = resultado == 'SUCCESS' ? '#36a64f' : '#d00000'
                def estado = resultado == 'SUCCESS' ? 'EXITOSA' : 'FALLIDA'
                def payload = [
                    text: ":rocket: *Pipeline de Calidad - Reto S11 | AP5*",
                    attachments: [[
                        color: color,
                        title: "Build #${env.BUILD_NUMBER} - ${estado}",
                        fields: [
                            [title: 'Proyecto', value: 'PSW Pipeline Base', short: true],
                            [title: 'Rama', value: 'develop', short: true],
                            [title: 'Resultado', value: estado, short: true],
                            [title: 'Duracion', value: "${currentBuild.durationString}", short: true],
                            [title: 'Jenkins', value: "${env.BUILD_URL}", short: false]
                        ]
                    ]]
                ]
                writeFile file: 'slack-message.json', text: groovy.json.JsonOutput.toJson(payload)

                withCredentials([string(credentialsId: 'slack-webhook-url', variable: 'SLACK_WEBHOOK_URL')]) {
                    sh 'curl -sS -X POST -H "Content-type: application/json" --data @slack-message.json "$SLACK_WEBHOOK_URL"'
                }
                echo "Notificacion Slack enviada (${estado})"
            }
        }
    }
}
