package org.killercarrots.evcharge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.killercarrots.evcharge.repos.*
import spock.lang.Specification;

@SpringBootTest
class TestGeneralController extends Specification {

  private static final String IGNORED = System.setProperty("IGNORE_SSL_ERRORS", "true")

  @Autowired
  private AuthController ac

  def "Test userAccess"() {
    def gc = new GeneralController()

    expect:
    gc.userAccess() == "User Content."
  }

  def "Test GetUserStatus"() {
    given:
    def list = new ArrayList<String>()
    list.add("user")
    //def ac = new AuthController()
    //def gc = new GeneralController()

    expect:
    ac.registerUser("json", list, "bill", "password") != null

/*    when:
    List<String> list = new ArrayList<String>()
    list.add("operator")

    then:
    ac.registerUser("json", list, "bill", "password") == "does it?"*/
    //gc.GetUserStatus("json", "bill") == "User Content."
  }

/*  def "Test GetUserStatus"() {
    def get = new URL("https://localhost:8765/evcharge/api/admin/users/bill").openConnection();
    def getRC = get.getResponseCode();

    expect:
    getRc.equals("does it?")
  }*/
}
