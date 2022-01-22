package org.ethelred.minecraft.webhook

import io.micronaut.context.ApplicationContext
import io.micronaut.context.BeanContext
import jakarta.inject.Inject
import org.mockserver.client.MockServerClient
import org.mockserver.matchers.MatchType
import org.mockserver.mock.Expectation
import org.mockserver.model.ExpectationId
import org.mockserver.verify.VerificationTimes
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MockServerContainer
import org.testcontainers.images.builder.ImageFromDockerfile
import org.testcontainers.spock.Testcontainers
import org.testcontainers.utility.DockerImageName
import spock.lang.Retry
import spock.lang.Shared
import spock.lang.Specification

import java.util.stream.Collectors
import java.util.stream.Stream

import static org.mockserver.model.HttpRequest.request
import static org.mockserver.model.HttpResponse.response
import static org.mockserver.model.JsonBody.json

@Testcontainers
class MonitorSpec extends Specification {

    @Shared
    MockServerContainer mockServer = new MockServerContainer(DockerImageName.parse("mockserver/mockserver"))
    @Shared
    GenericContainer mockBedrock = new GenericContainer( DockerImageName.parse("alpine"))
    .withCommand("/bin/sh", "-c","while true; do echo \"test\" >> /proc/1/fd/1; sleep 5; done")
    @Shared
    MockServerClient mockServerClient
    @Shared
    Expectation expectation

    def writeToBedrock(String message) {
        mockBedrock.execInContainer(
                "/bin/sh", "-c", "echo \"$message\" >> /proc/1/fd/1"
        )
    }

    def setupSpec() {
        mockServer.followOutput({println(it.getUtf8String().trim())})
        mockBedrock.followOutput({println(it.getUtf8String().trim())})

        writeToBedrock("Level Name: MonitorSpec")
        mockServerClient = new MockServerClient(mockServer.host, mockServer.serverPort)
        expectation = mockServerClient.when(
                request().withMethod("POST").withPath("/webhook"))
                .respond(response().withStatusCode(204))[0]

        ApplicationContext applicationContext =ApplicationContext.builder()
            .properties("mc-webhook.image-name": mockBedrock.dockerImageName,
                    "mc-webhook.webhook-url": "http://${mockServer.host}:${mockServer.serverPort}/webhook".toURL())
            .start()
        Monitor monitor = applicationContext.createBean(Monitor)
    }

    @Retry(delay = 1000, count = 3)
    def "player connected"() {
        when:
        writeToBedrock("Player connected: Bob, xuid 12345")

        then:
        mockServerClient.verify(
                request().withMethod("POST").withPath("/webhook")
                        .withBody(json("""{
                      "content" : "Bob connected to MonitorSpec"
                    }""", MatchType.ONLY_MATCHING_FIELDS)), VerificationTimes.atLeast(1))
    }
}
