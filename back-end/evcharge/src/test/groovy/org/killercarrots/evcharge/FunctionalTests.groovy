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
import org.killercarrots.evcharge.repos.StationRepository
import java.util.ArrayList

@SpringBootTest
@AutoConfigureMockMvc
class FunctionalTests extends Specification {

    private static final String IGNORED = System.setProperty("IGNORE_SSL_ERRORS", "true")

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StationRepository stationRepository;

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
        mvc.perform(MockMvcRequestBuilders.multipart("/evcharge/api/admin/system/sessionsupd").file(demo_file).header("X-OBSERVATORY-AUTH", "Bearer "+token)).andExpect(MockMvcResultMatchers.status().isOk())
    }

    // user MUST NOT already exist in database
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
        mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/admin/usermod/testUser/testPassword").header("X-OBSERVATORY-AUTH","Bearer "+token)).andExpect(MockMvcResultMatchers.status().isOk())
    }

    // user MUST NOT already exist in database
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
        mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/admin/usermod/testOperator/testPassword?roles=operator").header("X-OBSERVATORY-AUTH","Bearer "+token)).andExpect(MockMvcResultMatchers.status().isOk())
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
        mvc.perform(MockMvcRequestBuilders.get("/evcharge/api/SessionsPerEV/45b68c71-cd11-4bd7-a03f-fdaae259635d/20190101/20200101").header("X-OBSERVATORY-AUTH","Bearer "+token)).andExpect(MockMvcResultMatchers.status().isOk())
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
        mvc.perform(MockMvcRequestBuilders.get("/evcharge/api/SessionsPerEV/45b68c71-cd11-4bd7-a03f-fdaae259635d/20100101/20110101").header("X-OBSERVATORY-AUTH","Bearer "+token)).andExpect(MockMvcResultMatchers.status().isPaymentRequired())
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
        mvc.perform(MockMvcRequestBuilders.get("/evcharge/api/SessionsPerProvider/ESB Ecars/20100101/20210101").header("X-OBSERVATORY-AUTH","Bearer "+token)).andExpect(MockMvcResultMatchers.status().isUnauthorized())
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
        mvc.perform(MockMvcRequestBuilders.get("/evcharge/api/SessionsPerProvider/ESB Ecars/20100101/20210101").header("X-OBSERVATORY-AUTH","Bearer "+token)).andExpect(MockMvcResultMatchers.status().isOk())
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
        mvc.perform(MockMvcRequestBuilders.get("/evcharge/api/SessionsPerProvider/ESB Ecars/20100101/20210101").header("X-OBSERVATORY-AUTH","Bearer "+token)).andExpect(MockMvcResultMatchers.status().isOk())
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
        mvc.perform(MockMvcRequestBuilders.get("/evcharge/api/SessionsPerStation/5f6978bb00355e4c01059bc7/20100101/20210101").header("X-OBSERVATORY-AUTH","Bearer "+token)).andExpect(MockMvcResultMatchers.status().isOk())
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
        mvc.perform(MockMvcRequestBuilders.get("/evcharge/api/SessionsPerPoint/5f6978c000355e4c0105a3e9_33287/20100101/20210101").header("X-OBSERVATORY-AUTH","Bearer "+token)).andExpect(MockMvcResultMatchers.status().isOk())
    }

    def "Search nearby stations without login, should succeed" (){
        expect:
        mvc.perform(MockMvcRequestBuilders.get("/evcharge/api/StationsNearby/0/0/100000")).andExpect(MockMvcResultMatchers.status().isOk())
    }

    def "Try to logout after a successful login" () {
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

    def "Test pointInfo NOT supported protocol" () {
        given:
        def response = mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
                            new BasicNameValuePair("username", "testUser"),
                            new BasicNameValuePair("password", "testPassword"))))))
                        .andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
        def token = JsonPath.parse(response.getResponse().getContentAsString()).read("token")

        expect:
        def epr = mvc.perform(MockMvcRequestBuilders.get("/evcharge/api/SessionCost/a9a177bf-9ce5-4b67-b3ef-51af248b48c2/5f6978b800355e4c01059523_20101").header("X-OBSERVATORY-AUTH","Bearer "+token))
        .andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
        JsonPath.parse(epr.getResponse().getContentAsString()).read("Response")
    }

    def "Test pointInfo supported protocol" () {
        given:
        def response = mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
                            new BasicNameValuePair("username", "testUser"),
                            new BasicNameValuePair("password", "testPassword"))))))
                        .andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
        def token = JsonPath.parse(response.getResponse().getContentAsString()).read("token")

        expect:
        def epr = mvc.perform(MockMvcRequestBuilders.get("/evcharge/api/SessionCost/45b68c71-cd11-4bd7-a03f-fdaae259635d/5f6978b800355e4c0105958a_20294").header("X-OBSERVATORY-AUTH","Bearer "+token))
        .andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
        JsonPath.parse(epr.getResponse().getContentAsString()).read("protocol")
    }

    def "Test startChargingCost" () {
        given:
        def response = mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
                            new BasicNameValuePair("username", "testUser"),
                            new BasicNameValuePair("password", "testPassword"))))))
                        .andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
        def token = JsonPath.parse(response.getResponse().getContentAsString()).read("token")

        expect:
        def epr = mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/StartSessionCost/45b68c71-cd11-4bd7-a03f-fdaae259635d/5f6978b800355e4c0105958a_20294/56").header("X-OBSERVATORY-AUTH","Bearer "+token))
        .andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
        JsonPath.parse(epr.getResponse().getContentAsString()).read("session")
    }

    def "Test startChargingAmount" () {
        given:
        def response = mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
                            new BasicNameValuePair("username", "testUser"),
                            new BasicNameValuePair("password", "testPassword"))))))
                        .andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
        def token = JsonPath.parse(response.getResponse().getContentAsString()).read("token")

        expect:
        def epr = mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/StartSessionAmount/e8773876-6036-4a6c-926e-5490aae58971/5f6978b800355e4c010598e7_16161/56").header("X-OBSERVATORY-AUTH","Bearer "+token))
        .andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
        JsonPath.parse(epr.getResponse().getContentAsString()).read("session")
    }

    def "Test userActiveSessions and completeCharging" () {
        given:
        def response = mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
                            new BasicNameValuePair("username", "testUser"),
                            new BasicNameValuePair("password", "testPassword"))))))
                        .andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
        def token = JsonPath.parse(response.getResponse().getContentAsString()).read("token")

        expect:
        def epr = mvc.perform(MockMvcRequestBuilders.get("/evcharge/api/ActiveSession").header("X-OBSERVATORY-AUTH","Bearer "+token))
        .andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
        def id1 = JsonPath.parse(epr.getResponse().getContentAsString()).read("ActiveSessionsList[0].SessionID")
        def id2 = JsonPath.parse(epr.getResponse().getContentAsString()).read("ActiveSessionsList[1].SessionID")
        mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/CheckOut/"+id1).header("X-OBSERVATORY-AUTH","Bearer "+token))
        .andExpect(MockMvcResultMatchers.status().isOk())
        mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/CheckOut/"+id2).header("X-OBSERVATORY-AUTH","Bearer "+token))
        .andExpect(MockMvcResultMatchers.status().isOk())
    }

    def "Test showStations, expect No Data" () {
        given:
        def response = mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
                            new BasicNameValuePair("username", "testOperator"),
                            new BasicNameValuePair("password", "testPassword"))))))
                        .andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
        def token = JsonPath.parse(response.getResponse().getContentAsString()).read("token")

        expect:
        mvc.perform(MockMvcRequestBuilders.get("/evcharge/api/Operator/StationShow/testOperator").header("X-OBSERVATORY-AUTH","Bearer "+token))
        .andExpect(MockMvcResultMatchers.status().isPaymentRequired())
    }

    def "Test addStation" () {
        given:
        def response = mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
                            new BasicNameValuePair("username", "testOperator"),
                            new BasicNameValuePair("password", "testPassword"))))))
                        .andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
        def token = JsonPath.parse(response.getResponse().getContentAsString()).read("token")

        expect:
        mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/Operator/StationAdd?id=testStationNonExistent&cost=0.5&address=testAddress&country=Greece&lon=56.0&lat=56.0&points=404_7.0_ac_type2,101396_11.0_dc_chademo")
        .header("X-OBSERVATORY-AUTH","Bearer "+token)).andExpect(MockMvcResultMatchers.status().isOk())
    }

    def "Test showStations" () {
        given:
        def response = mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
                            new BasicNameValuePair("username", "testOperator"),
                            new BasicNameValuePair("password", "testPassword"))))))
                        .andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
        def token = JsonPath.parse(response.getResponse().getContentAsString()).read("token")

        expect:
        mvc.perform(MockMvcRequestBuilders.get("/evcharge/api/Operator/StationShow/testOperator").header("X-OBSERVATORY-AUTH","Bearer "+token))
        .andExpect(MockMvcResultMatchers.status().isOk())
    }

    def "Test removeStation" () {
        given:
        def response = mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
                            new BasicNameValuePair("username", "testOperator"),
                            new BasicNameValuePair("password", "testPassword"))))))
                        .andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
        def token = JsonPath.parse(response.getResponse().getContentAsString()).read("token")

        expect:
        mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/Operator/StationRemove/testStationNonExistent").header("X-OBSERVATORY-AUTH","Bearer "+token))
        .andExpect(MockMvcResultMatchers.status().isOk())
    }

    def "Test userVehicles" () {
        when:
        def response = mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
                            new BasicNameValuePair("username", "testUser"),
                            new BasicNameValuePair("password", "testPassword"))))))
                        .andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
        def token = JsonPath.parse(response.getResponse().getContentAsString()).read("token")

        then:
        mvc.perform(MockMvcRequestBuilders.get("/evcharge/api/evPerUser/testUser").header("X-OBSERVATORY-AUTH","Bearer "+token))
        .andExpect(MockMvcResultMatchers.status().isOk())
    }

    def "Try to reset changes from tests at the end" () {
        when:
        userRepository.deleteById("testUser")
        userRepository.deleteById("testOperator")
        stationRepository.deleteById("testStationNonExistent")

        then:
        mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/admin/resetsessions")).andExpect(MockMvcResultMatchers.status().isOk())
    }
}
