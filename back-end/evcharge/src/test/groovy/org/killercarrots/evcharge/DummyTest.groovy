package org.killercarrots.evcharge

import org.killercarrots.evcharge.*
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
class DummyTest extends Specification {

  @Autowired
  private MockMvc mvc;

  def "This should work" () {
      given:
      def response = mvc.perform(MockMvcRequestBuilders.get("/evcharge/test")).andExpect(MockMvcResultMatchers.status().isOk()).andReturn()

      expect:
      response.getResponse().getContentAsString() == "Public Content."
  }

}
