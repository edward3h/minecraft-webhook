package org.ethelred.minecraft.webhook

import io.micronaut.context.ApplicationContext
import org.mockserver.client.MockServerClient
import org.mockserver.matchers.MatchType
import org.mockserver.verify.VerificationTimes
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MockServerContainer
import org.testcontainers.spock.Testcontainers
import org.testcontainers.utility.DockerImageName
import spock.lang.Retry
import spock.lang.Shared
import spock.lang.Specification

import static org.mockserver.model.HttpRequest.request
import static org.mockserver.model.HttpResponse.response
import static org.mockserver.model.JsonBody.json

@Testcontainers
class MonitorSpec extends Specification {

    @Shared
    MockServerContainer mockServer = new MockServerContainer(DockerImageName.parse("mockserver/mockserver:mockserver-5.15.0"))
    @Shared
    GenericContainer mockBedrock = new GenericContainer(DockerImageName.parse("alpine"))
            .withCommand("/bin/sh", "-c", "while true; do echo \"test\" >> /proc/1/fd/1; sleep 5; done")
            .withLabel("mc-bedrock", "true")
    @Shared
    GenericContainer mockBackup = new GenericContainer(DockerImageName.parse("alpine"))
            .withCommand("/bin/sh", "-c", "while true; do echo \"test\" >> /proc/1/fd/1; sleep 5; done")
            .withLabel("mc-backup", "true")
    @Shared
    MockServerClient mockServerClient

    def writeToBedrock(String message) {
        mockBedrock.execInContainer(
                "/bin/sh", "-c", "echo \"$message\" >> /proc/1/fd/1"
        )
    }

    def writeToBackup(String message) {
        mockBackup.execInContainer(
                "/bin/sh", "-c", "echo \"$message\" >> /proc/1/fd/1"
        )
    }

    def setupSpec() {
        mockServer.followOutput({ println(it.getUtf8String().trim()) })
        mockBedrock.followOutput({ println(it.getUtf8String().trim()) })
        mockBackup.followOutput({println(it.getUtf8String().trim()) })

        writeToBedrock("Level Name: MonitorSpec")
        mockServerClient = new MockServerClient(mockServer.host, mockServer.serverPort)
        mockServerClient.when(
                request().withMethod("POST").withPath("/webhook"))
                .respond(response().withStatusCode(204))
        mockServerClient.when(
                request().withMethod("POST").withPath("/webhookjson"))
                .respond(response().withStatusCode(204))
        mockServerClient.when(
                request().withMethod("POST").withPath("/webhookmsg"))
                .respond(response().withStatusCode(204))

        ApplicationContext applicationContext = ApplicationContext.builder()
                .properties(
                        "mc-webhook.image-labels": Set.of("mc-bedrock"),
                        "mc-webhook.backup-image-labels": Set.of("mc-backup"),
                        "mc-webhook.webhook-url": "http://${mockServer.host}:${mockServer.serverPort}/webhook".toURL(),
                        "mc-webhook.webhooks.discord2.type": "discord",
                        "mc-webhook.webhooks.discord2.url": "http://${mockServer.host}:${mockServer.serverPort}/webhookmsg".toURL(),
                        "mc-webhook.webhooks.discord2.events.PLAYER_CONNECTED": 'Why hello %playerName% on %containerName%',
                        "mc-webhook.webhooks.json1.type": "json",
                        "mc-webhook.webhooks.json1.url": "http://${mockServer.host}:${mockServer.serverPort}/webhookjson".toURL()
                )
                .start()
        Monitor monitor = applicationContext.createBean(Monitor)
    }

    @Retry(delay = 1000, count = 3)
    def "player connected"() {
        when:
        writeToBedrock("Player connected: Bob, xuid: 12345")
//        println mockServerClient.retrieveRecordedRequests(null)

        then:
        mockServerClient.verify(
                request().withMethod("POST").withPath("/webhook")
                        .withBody(json("""{
                      "content" : "Bob connected to MonitorSpec"
                    }""", MatchType.ONLY_MATCHING_FIELDS)), VerificationTimes.atLeast(1))
        mockServerClient.verify(
                request().withMethod("POST").withPath("/webhookmsg")
                        .withBody(json("""{
                      "content" : "Why hello Bob on ${mockBedrock.containerName}"
                    }""", MatchType.ONLY_MATCHING_FIELDS)), VerificationTimes.atLeast(1))
        mockServerClient.verify(
                request().withMethod("POST").withPath("/webhookjson")
                        .withBody(json("""{
                      "containerName" : "${mockBedrock.containerName}"
                    }""", MatchType.ONLY_MATCHING_FIELDS)), VerificationTimes.atLeast(1))
    }

    @Retry(delay = 1000, count = 3)
    def "backup complete with space in filename"() {
        when:
        writeToBackup("[23:00:03.062][info    ] Backed up as: Hardcore FC.20220312-230000.mcworld")

        then:
        mockServerClient.verify(
                request().withMethod("POST").withPath("/webhookjson")
                        .withBody(json("""{
                      "filename" : "Hardcore FC.20220312-230000.mcworld"
                    }""", MatchType.ONLY_MATCHING_FIELDS)), VerificationTimes.atLeast(1))
    }

}
