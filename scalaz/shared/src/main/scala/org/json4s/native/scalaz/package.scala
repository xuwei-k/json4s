package org.json4s
package native

import _root_.scalaz.Show
import org.json4s.JsonAST.JValue

package object scalaz {

  implicit def JValueShow[A <: JValue]: Show[A] = Show.shows { x =>
    JsonMethods.compact(JsonMethods.render(x))
  }

}
