package test

import java.io.PrintWriter
import java.util

import org.junit.runner.RunWith
import org.koala.sbt.SbtDistAppShell
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

@RunWith(classOf[JUnitRunner])
class TemplateSuite extends FunSuite with BeforeAndAfter {
  before {

  }
  after {

  }

  test("mustache") {
    val scopes = new util.HashMap[String, Object]()
    scopes.put("mainClass", "demo.Hello")

    val w = new PrintWriter(System.out)

    SbtDistAppShell.mustache(w)("windows.mustache", scopes)
    println("----------------------------------------------")
    SbtDistAppShell.mustache(w)("linux.mustache", scopes)
  }
}
