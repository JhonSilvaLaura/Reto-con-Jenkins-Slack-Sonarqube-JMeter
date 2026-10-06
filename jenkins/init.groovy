import jenkins.model.Jenkins
import hudson.util.Secret
import com.cloudbees.plugins.credentials.CredentialsScope
import com.cloudbees.plugins.credentials.SystemCredentialsProvider
import com.cloudbees.plugins.credentials.domains.Domain
import com.cloudbees.plugins.credentials.impl.StringCredentialsImpl

def log = { msg -> println("[init.groovy] " + msg) }

def jenkins = Jenkins.get()

// ---------------------------------------------------------------
// 1. Servidor SonarQube registrado en Jenkins (para withSonarQubeEnv)
// ---------------------------------------------------------------
def sonarUrl = System.getenv('SONARQUBE_URL') ?: 'http://sonarqube:9000'
def sonarToken = System.getenv('SONARQUBE_TOKEN') ?: ''

try {
    def clazz = Class.forName('org.jenkinsci.plugins.sonarqube.SonarQubeInstallation')
    def descriptor = jenkins.getDescriptorByType(clazz)
    def instalacion = null

    for (args in [
        ['SonarQube', sonarUrl, sonarToken, ''],
        ['SonarQube', sonarUrl, sonarToken],
        ['SonarQube', sonarUrl, Secret.fromString(sonarToken), '']
    ]) {
        try {
            instalacion = clazz.getConstructor(args.collect { it.getClass() }.toArray() as Class[]).newInstance(*args)
            break
        } catch (Throwable ignored) {
        }
    }

    if (instalacion != null) {
        descriptor.setInstallations(instalacion as Object[])
        descriptor.save()
        log("SonarQube registrado: " + sonarUrl)
    } else {
        log("AVISO: no se pudo instanciar SonarQubeInstallation (se usaran -Dsonar.* explicitos)")
    }
} catch (Throwable t) {
    log("AVISO: SonarQube no configurado -> " + t.message)
}

// ---------------------------------------------------------------
// 2. Credenciales: webhook de Slack y token de SonarQube
// ---------------------------------------------------------------
try {
    def store = jenkins.getExtensionList(SystemCredentialsProvider)[0].getStore()
    def dominio = Domain.global()

    def slackUrl = System.getenv('SLACK_WEBHOOK_URL')
    if (slackUrl) {
        def nueva = new StringCredentialsImpl(CredentialsScope.GLOBAL, 'slack-webhook-url',
                'Slack Incoming Webhook (Reto Pipeline de Calidad)', Secret.fromString(slackUrl))
        store.addCredentials(dominio, nueva)
        log("Credencial slack-webhook-url creada")
    }

    if (sonarToken) {
        def token = new StringCredentialsImpl(CredentialsScope.GLOBAL, 'sonar-token',
                'Token de analisis SonarQube', Secret.fromString(sonarToken))
        store.addCredentials(dominio, token)
        log("Credencial sonar-token creada")
    }
} catch (Throwable t) {
    log("AVISO: credenciales no creadas -> " + t.message)
}

log("Inicializacion completada")
