package org.json4s
package jackson

import org.json4s.prefs.EmptyValueStrategy
import org.specs2.mutable.Specification

class JacksonJsonMethodsSpec extends Specification {

  import org.json4s.JsonDSL._
  import JsonMethods._

  "JsonMethods.write" should {
    "produce JSON without empty fields" in {
      "from Seq(Some(1), None, None, Some(2))" in {
        val seq = Seq(Some(1), None, None, Some(2))
        val expected = JArray(List(JInt(1), JNothing, JNothing, JInt(2)))
        render(seq) must_== expected
      }

      """from Map("a" -> Some(1), "b" -> None, "c" -> None, "d" -> Some(2))""" in {
        val map = Map("a" -> Some(1), "b" -> None, "c" -> None, "d" -> Some(2))
        val expected = JObject(List(("a", JInt(1)), ("b", JNothing), ("c", JNothing), ("d", JInt(2))))
        render(map) must_== expected
      }
    }

    "produce JSON with empty fields preserved" in {
      "from Seq(Some(1), None, None, Some(2))" in {
        val seq = Seq(Some(1), None, None, Some(2))
        val expected = JArray(List(JInt(1), JNull, JNull, JInt(2)))
        render(seq, emptyValueStrategy = EmptyValueStrategy.preserve) must_== expected
      }

      """from Map("a" -> Some(1), "b" -> None, "c" -> None, "d" -> Some(2))""" in {
        val map = Map("a" -> Some(1), "b" -> None, "c" -> None, "d" -> Some(2))
        val expected = JObject(List(("a", JInt(1)), ("b", JNull), ("c", JNull), ("d", JInt(2))))
        render(map, emptyValueStrategy = EmptyValueStrategy.preserve) must_== expected
      }
    }

  }

}
