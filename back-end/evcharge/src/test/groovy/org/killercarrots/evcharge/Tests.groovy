package org.killercarrots.evcharge

import org.killercarrots.evcharge.*
import org.killercarrots.evcharge.repos.*
import org.killercarrots.evcharge.models.*
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.*
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.*
import spock.lang.*
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class Tests extends Specification {

    @Autowired
    private MockMvc mvc;

    @Autowired
    UserRepository userRepository;

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

    // user MUST NOT already exist in database
    def "Register user" () {
        given:
        def response = mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
                            new BasicNameValuePair("username", "admin"),
                            new BasicNameValuePair("password", "petrol4ever"))))))
                        .andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
        def token = JsonPath.parse(response.getResponse().getContentAsString()).read("token")

        expect:
        mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/admin/usermod/testNonExistentUser/testpass")
        .param("roles", "user", "operator").header("X-OBSERVATORY-AUTH", "Bearer "+token))
        .andExpect(MockMvcResultMatchers.status().isOk())
    }

    def "Search above user status" () {
        when:
        def response = mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/login")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
            new BasicNameValuePair("username", "admin"),
            new BasicNameValuePair("password", "petrol4ever"))))))
        .andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
        def token = JsonPath.parse(response.getResponse().getContentAsString()).read("token")

        then:
        mvc.perform(MockMvcRequestBuilders.get("/evcharge/api/admin/users/testNonExistentUser")
        .header("X-OBSERVATORY-AUTH", "Bearer "+token)).andExpect(MockMvcResultMatchers.status().isOk())
    }

    def "Login and logout above user" () {
        expect:
        def response = mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
                            new BasicNameValuePair("username", "testNonExistentUser"),
                            new BasicNameValuePair("password", "testpass"))))))
                        .andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
        def token = JsonPath.parse(response.getResponse().getContentAsString()).read("token")
        mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/logout").header("X-OBSERVATORY-AUTH", "Bearer "+token))
        .andExpect(MockMvcResultMatchers.status().isOk())
    }

    def "Test pointInfo" () {
      when:
      def response = mvc.perform(MockMvcRequestBuilders.post("/evcharge/api/login")
                      .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                      .content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
                          new BasicNameValuePair("username", "testNonExistentUser"),
                          new BasicNameValuePair("password", "testpass"))))))
                      .andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
      def token = JsonPath.parse(response.getResponse().getContentAsString()).read("token")
      Vehicle vehicle = new Vehicle(
          id         : "testNonExistentVehicleID"
          brand      : "testNonExistentBrand"
          model      : "testNonExistentModel"
          variant    : "testNonExistentVariant"
          consumption: 56.0
          batterySize: 56.0
          ac         : new
          dc         :
        )

      then:
      mvc.perform(MockMvcRequestBuilders.get("/evcharge/api/SessionCost/.../...").header("X-OBSERVATORY-AUTH", "Bearer "+token))
      .andExpect(MockMvcResultMatchers.status().isOk())
    }

    def "Delete user registered above" () {
        expect:
        User user = userRepository.findById("testNonExistentUser").get()
        userRepository.delete(user)
    }

}
