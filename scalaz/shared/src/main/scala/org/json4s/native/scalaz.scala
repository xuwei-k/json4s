package org.json4s
package native

import _root_.scalaz.Show

object scalaz {

  implicit def JValueShow[A <: JValue]: Show[A] = Show.shows { x =>
    JsonMethods.compact(JsonMethods.render(x))
  }

}
