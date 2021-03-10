package org.killercarrots.evcharge

import org.springframework.beans.factory.annotation.Autowired
import org.killercarrots.evcharge.*
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Shared
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import spock.lang.*
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.boot.test.autoconfigure.web.servlet.*
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.*
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.springframework.mock.web.MockMultipartFile
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import com.jayway.jsonpath.JsonPath
import org.killercarrots.evcharge.repos.UserRepository
import java.util.ArrayList

@SpringBootTest
@AutoConfigureMockMvc
class FunctionalTests extends Specification {

    private static final String IGNORED = System.setProperty("IGNORE_SSL_ERRORS", "true")

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    def "Reset sessions" () {
        expect:
        mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/admin/resetsessions")).andExpect(MockMvcResultMatchers.status().isOk())
    }

    def "Login as admin" () {
        expect:
        mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/login")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
                    new BasicNameValuePair("username", "admin"),
                    new BasicNameValuePair("password", "petrol4ever"))))))
        .andExpect(MockMvcResultMatchers.status().isOk())
    }

    def "Search user status" () {
        when:
        def response = mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
                            new BasicNameValuePair("username", "admin"),
                            new BasicNameValuePair("password", "petrol4ever"))))))
                        .andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
        def token = JsonPath.parse(response.getResponse().getContentAsString()).read("token")
        then:
        mvc.perform(MockMvcRequestBuilders.get("/evcharge/api/admin/users/admin").header("X-OBSERVATORY-AUTH", "Bearer "+token)).andExpect(MockMvcResultMatchers.status().isOk())
    }

    def "Check DB status endpoint" () {
        expect:
        mvc.perform(MockMvcRequestBuilders.get("/evcharge/api/admin/healthcheck")).andExpect(MockMvcResultMatchers.status().isOk()).andReturn().getResponse().getContentAsString() == "{\"status\":\"OK\"}"
    }

    def "Upload file with some demo sessions" () {
        given:
        def demo_file = new MockMultipartFile("file","sessions.csv", "text/plain",
                                                                    ("5f6978bb00355e4c01059bc7_5096,45b68c71-cd11-4bd7-a03f-fdaae259635d,Mima,2019-09-01 16:56:18,2019-09-01 17:35:38,0.747\n"+
                                                                    "5f6978c000355e4c0105a3e9_33287,6cd5ba89-6523-4bca-9423-ea92227a8b8d,Meghann,2019-09-01 20:14:17,2019-09-01 22:01:04,11.372").getBytes());

        when:
        def response = mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
                            new BasicNameValuePair("username", "admin"),
                            new BasicNameValuePair("password", "petrol4ever"))))))
                        .andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
        def token = JsonPath.parse(response.getResponse().getContentAsString()).read("token")

        then:
        mvc.perform(MockMvcRequestBuilders.multipart("/evcharge/api/admin/system/sessionsupd").file(demo_file).header("X-OBSERVATORY-AUTH", "Bearer "+token)).andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
    }

    def "Create new demo user" () {
        when:
        def response = mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
                            new BasicNameValuePair("username", "admin"),
                            new BasicNameValuePair("password", "petrol4ever"))))))
                        .andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
        def token = JsonPath.parse(response.getResponse().getContentAsString()).read("token")

        then:
        mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/admin/usermod/testUser/testPassword").header("X-OBSERVATORY-AUTH","Bearer "+token)).andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
    }

    def "Create new demo operator" () {
        when:
        def response = mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
                            new BasicNameValuePair("username", "admin"),
                            new BasicNameValuePair("password", "petrol4ever"))))))
                        .andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
        def token = JsonPath.parse(response.getResponse().getContentAsString()).read("token")

        then:
        mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/admin/usermod/testOperator/testPassword?roles=operator").header("X-OBSERVATORY-AUTH","Bearer "+token)).andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
    }

    def "Search existing sessions by vehicle" () {
        when:
        def response = mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
                            new BasicNameValuePair("username", "testUser"),
                            new BasicNameValuePair("password", "testPassword"))))))
                        .andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
        def token = JsonPath.parse(response.getResponse().getContentAsString()).read("token")

        then:
        mvc.perform(MockMvcRequestBuilders.get("/evcharge/api/SessionsPerEV/45b68c71-cd11-4bd7-a03f-fdaae259635d/20190101/20200101").header("X-OBSERVATORY-AUTH","Bearer "+token)).andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
    }

    def "Search not existing sessions by vehicle, expecting No Data" () {
        when:
        def response = mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
                            new BasicNameValuePair("username", "testUser"),
                            new BasicNameValuePair("password", "testPassword"))))))
                        .andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
        def token = JsonPath.parse(response.getResponse().getContentAsString()).read("token")

        then:
        mvc.perform(MockMvcRequestBuilders.get("/evcharge/api/SessionsPerEV/45b68c71-cd11-4bd7-a03f-fdaae259635d/20100101/20110101").header("X-OBSERVATORY-AUTH","Bearer "+token)).andExpect(MockMvcResultMatchers.status().isPaymentRequired()).andReturn()
    }

    def "Search sessions by provider name as a user, expecting unauthorized response" () {
        when:
        def response = mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
                            new BasicNameValuePair("username", "testUser"),
                            new BasicNameValuePair("password", "testPassword"))))))
                        .andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
        def token = JsonPath.parse(response.getResponse().getContentAsString()).read("token")

        then:
        mvc.perform(MockMvcRequestBuilders.get("/evcharge/api/SessionsPerProvider/ESB Ecars/20100101/20210101").header("X-OBSERVATORY-AUTH","Bearer "+token)).andExpect(MockMvcResultMatchers.status().isUnauthorized()).andReturn()
    }

    def "Search sessions by provider name as an operator, should succeed" () {
        when:
        def response = mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
                            new BasicNameValuePair("username", "testOperator"),
                            new BasicNameValuePair("password", "testPassword"))))))
                        .andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
        def token = JsonPath.parse(response.getResponse().getContentAsString()).read("token")

        then:
        mvc.perform(MockMvcRequestBuilders.get("/evcharge/api/SessionsPerProvider/ESB Ecars/20100101/20210101").header("X-OBSERVATORY-AUTH","Bearer "+token)).andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
    }

    def "Search sessions by provider name as admin, should succeed" () {
        when:
        def response = mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
                            new BasicNameValuePair("username", "admin"),
                            new BasicNameValuePair("password", "petrol4ever"))))))
                        .andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
        def token = JsonPath.parse(response.getResponse().getContentAsString()).read("token")

        then:
        mvc.perform(MockMvcRequestBuilders.get("/evcharge/api/SessionsPerProvider/ESB Ecars/20100101/20210101").header("X-OBSERVATORY-AUTH","Bearer "+token)).andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
    }

    def "Search sessions by stationID" () {
        when:
        def response = mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
                            new BasicNameValuePair("username", "testOperator"),
                            new BasicNameValuePair("password", "testPassword"))))))
                        .andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
        def token = JsonPath.parse(response.getResponse().getContentAsString()).read("token")

        then:
        mvc.perform(MockMvcRequestBuilders.get("/evcharge/api/SessionsPerStation/5f6978bb00355e4c01059bc7/20100101/20210101").header("X-OBSERVATORY-AUTH","Bearer "+token)).andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
    }

    def "Search sessions by pointID" () {
        when:
        def response = mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
                            new BasicNameValuePair("username", "admin"),
                            new BasicNameValuePair("password", "petrol4ever"))))))
                        .andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
        def token = JsonPath.parse(response.getResponse().getContentAsString()).read("token")

        then:
        mvc.perform(MockMvcRequestBuilders.get("/evcharge/api/SessionsPerPoint/5f6978c000355e4c0105a3e9_33287/20100101/20210101").header("X-OBSERVATORY-AUTH","Bearer "+token)).andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
    }

    def "Search nearby stations without login, should succeed" (){
        expect:
        mvc.perform(MockMvcRequestBuilders.get("/evcharge/api/StationsNearby/0/0/100000")).andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
    }

    def "Try to logout after a succeful login" () {
        when:
        def response = mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
                            new BasicNameValuePair("username", "admin"),
                            new BasicNameValuePair("password", "petrol4ever"))))))
                        .andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
        def token = JsonPath.parse(response.getResponse().getContentAsString()).read("token")

        then:
        mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/logout").header("X-OBSERVATORY-AUTH","Bearer "+token)).andExpect(MockMvcResultMatchers.status().isOk())
    }

    def "Try to reset changes from tests at the end" () {
        when:
        userRepository.deleteById("testUser")
        userRepository.deleteById("testOperator")

        then:
        mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/admin/resetsessions")).andExpect(MockMvcResultMatchers.status().isOk())
    }
}