import jenkins.model.Jenkins
import hudson.util.Secret
import com.cloudbees.plugins.credentials.CredentialsScope
import com.cloudbees.plugins.credentials.SystemCredentialsProvider
import com.cloudbees.plugins.credentials.domains.Domain
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl
import hudson.plugins.sonar.SonarGlobalConfiguration
import hudson.plugins.sonar.SonarInstallation
import hudson.plugins.sonar.model.TriggersConfig

def log = { msg -> println("[init.groovy] " + msg) }

def jenkins = Jenkins.get()

// ---------------------------------------------------------------
// 1. Servidor SonarQube registrado en Jenkins (para withSonarQubeEnv)
// ---------------------------------------------------------------
def sonarUrl = System.getenv('SONARQUBE_URL') ?: 'http://sonarqube:9000'
def sonarToken = System.getenv('SONARQUBE_TOKEN') ?: ''

try {
    def globalConfig = SonarGlobalConfiguration.get()
    def instalacion = new SonarInstallation('SonarQube', sonarUrl, sonarToken, '', '', new TriggersConfig(), '')
    globalConfig.setInstallations(instalacion)
    globalConfig.save()
    log("SonarQube registrado: " + sonarUrl)
} catch (Throwable t) {
    log("AVISO: SonarQube no configurado -> " + t.message)
}

// ---------------------------------------------------------------
// 2. Credenciales: webhook de Slack y token de SonarQube
// ---------------------------------------------------------------
try {
    def store = jenkins.getExtensionList(SystemCredentialsProvider)[0].getStore()
    def dominio = Domain.global()
    def existentes = store.getCredentials(dominio)*.id

    def slackUrl = System.getenv('SLACK_WEBHOOK_URL')
    if (slackUrl && !existentes.contains('slack-webhook-url')) {
        store.addCredentials(dominio,
            new StringCredentialsImpl(CredentialsScope.GLOBAL, 'slack-webhook-url',
                'Slack Incoming Webhook (Reto Pipeline de Calidad)', Secret.fromString(slackUrl)))
        log("Credencial slack-webhook-url creada")
    }

    if (sonarToken && !existentes.contains('sonar-token')) {
        store.addCredentials(dominio,
            new StringCredentialsImpl(CredentialsScope.GLOBAL, 'sonar-token',
                'Token de analisis SonarQube', Secret.fromString(sonarToken)))
        log("Credencial sonar-token creada")
    }
} catch (Throwable t) {
    log("AVISO: credenciales no creadas -> " + t.message)
}

log("Inicializacion completada")
