package org.ethelred.minecraft.webhook

import io.micronaut.context.BeanContext
import io.micronaut.context.annotation.Requires
import io.micronaut.inject.qualifiers.Qualifiers
import io.micronaut.mqtt.annotation.MqttSubscriber
import io.micronaut.mqtt.annotation.Topic
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import jakarta.inject.Inject
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.FixedHostPortGenericContainer
import org.testcontainers.containers.GenericContainer
import org.testcontainers.spock.Testcontainers
import org.testcontainers.utility.DockerImageName
import spock.lang.Shared
import spock.lang.Specification

@Testcontainers
@MicronautTest(startApplication = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MqttSenderSpec extends Specification implements TestPropertyProvider {
    @Shared
    GenericContainer mosquitto = new GenericContainer("eclipse-mosquitto:2")
    .withExposedPorts(1883)
    .withClasspathResourceMapping("mosquitto.conf","/mosquitto/config/mosquitto.conf", BindMode.READ_WRITE)

    @Inject
    BeanContext beanContext
    @Inject
    TestSubscriber subscriber

    def setupSpec() {
        mosquitto.followOutput({ println(it.getUtf8String().trim()) })
    }

    def "can send a message"() {
        given:
        def senderConfiguration = new SenderConfiguration("mqtt", null, "spectopic", [:])
        def sender = beanContext.createBean(Sender, Qualifiers.byName("mqtt"), senderConfiguration)

        when:
        sender.sendMessage(new BackupEvent(EventType.BACKUP_COMPLETE, "spectest.zip"), "Not used")

        then:
        subscriber.lastMessage == '{"type":"BACKUP_COMPLETE","filename":"spectest.zip"}'
    }

    @Override
    Map<String, String> getProperties() {
        return [
                "mqtt.client.client-id":"testspec",
                "mqtt.client.server-uri":"tcp://${mosquitto.getHost()}:${mosquitto.getMappedPort(1883)}"
        ]
    }
}

@Requires(property = "mqtt.client.client-id")
@MqttSubscriber
class TestSubscriber {
    String lastMessage

    @Topic("spectopic")
    void receive(byte[] data) {
        lastMessage = new String(data)
    }
}