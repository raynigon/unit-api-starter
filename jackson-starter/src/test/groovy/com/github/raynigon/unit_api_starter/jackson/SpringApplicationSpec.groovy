package com.github.raynigon.unit_api_starter.jackson

import com.github.raynigon.unit_api_starter.jackson.helpers.BasicApplicationConfig
import com.github.raynigon.unit_api_starter.jackson.helpers.BasicRestController
import com.github.raynigon.unit_api_starter.jackson.helpers.BasicService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = [
                UnitApiJacksonConfiguration,
                BasicApplicationConfig,
                BasicRestController,
                BasicService
        ]
)
class SpringApplicationSpec extends Specification {

    @Autowired
    BasicService service

    @Autowired
    TestRestTemplate restTemplate

    def 'context setup works'() {
        expect:
        true
    }

    def 'entity creation works'() {

        given:
        def data = [
                "id"         : "1",
                "speed"      : 100,
                "temperature": 30
        ]

        when:
        def response = restTemplate.postForEntity("/api/basic-entity", data, Map.class)

        then:
        response.statusCode.'2xxSuccessful'

        and:
        service.getEntity("1") != null
    }
}
