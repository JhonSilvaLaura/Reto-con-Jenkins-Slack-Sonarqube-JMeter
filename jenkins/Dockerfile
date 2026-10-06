FROM jenkins/jenkins:lts-jdk17

USER root

RUN apt-get update \
 && apt-get install -y --no-install-recommends maven curl ca-certificates git \
 && rm -rf /var/lib/apt/lists/*

ADD https://dlcdn.apache.org/jmeter/binaries/apache-jmeter-5.6.3.tgz /tmp/jmeter.tgz
RUN mkdir -p /opt/jmeter \
 && tar xzf /tmp/jmeter.tgz -C /opt/jmeter --strip-components=1 \
 && rm /tmp/jmeter.tgz \
 && ln -sf /opt/jmeter/bin/jmeter /usr/local/bin/jmeter

RUN jenkins-plugin-cli --plugins git workflow-aggregator pipeline-stage-view slack sonar credentials-binding plain-credentials

COPY init.groovy /usr/share/jenkins/ref/init.groovy.d/init.groovy

ENV JAVA_OPTS="-Djava.awt.headless=true -Xmx512m -Djenkins.install.runSetupWizard=false"
ENV MAVEN_OPTS="-Xmx512m -Dmaven.artifact.threads=2"

USER jenkins
