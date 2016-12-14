package io.buoyant.namerd.iface

import com.twitter.finagle.util.LoadService
import com.twitter.finagle.{Path, Stack}
import io.buoyant.config.Parser
import io.buoyant.namer.{InterpreterConfig, InterpreterInitializer}
import org.scalatest.FunSuite

class NamerdHttpTest extends FunSuite {
  test("sanity") {
    // ensure it doesn't totally blowup
    val _ = NamerdHttpInterpreterConfig(Some(Path.read("/whats/in/a")), Some("name"), None, None)
      .newInterpreter(Stack.Params.empty)
  }

  test("interpreter registration") {
    assert(LoadService[InterpreterInitializer]().exists(_.isInstanceOf[NamerdHttpInterpreterInitializer]))
  }

  test("parse config") {
    val yaml = s"""|kind: io.l5d.namerd.http
                   |experimental: true
                   |dst: /$$/inet/127.1/4100
                   |namespace: name
                   |""".stripMargin

    val mapper = Parser.objectMapper(yaml, Iterable(Seq(NamerdHttpInterpreterInitializer)))
    val namerd = mapper.readValue[InterpreterConfig](yaml).asInstanceOf[NamerdHttpInterpreterConfig]
    mapper.writeValueAsString(namerd) // ensure serialization doesn't blow up
    assert(namerd.dst == Some(Path.read("/$/inet/127.1/4100")))
    assert(namerd.namespace == Some("name"))
    assert(!namerd.disabled)
  }

  test("without experimental") {
    val yaml = s"""|kind: io.l5d.namerd.http
                   |dst: /$$/inet/127.1/4100
                   |namespace: name
                   |""".stripMargin

    val mapper = Parser.objectMapper(yaml, Iterable(Seq(NamerdHttpInterpreterInitializer)))
    val namerd = mapper.readValue[InterpreterConfig](yaml).asInstanceOf[NamerdHttpInterpreterConfig]
    mapper.writeValueAsString(namerd) // ensure serialization doesn't blow up
    assert(namerd.disabled)
  }
}