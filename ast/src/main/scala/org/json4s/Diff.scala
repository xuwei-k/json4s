/*
 * Copyright 2009-2010 WorldWide Conferencing, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.json4s

import JsonAST._

/** A difference between two JSONs (j1 diff j2).
  *
  * @param changed what has changed from j1 to j2
 * @param added what has been added to j2
 * @param deleted what has been deleted from j1
 */
case class Diff(changed: Option[JValue], added: Option[JValue], deleted: Option[JValue]) {
  def map(f: JValue => JValue): Diff = {
    Diff(changed.map(f), added.map(f), deleted.map(f))
  }

  private[json4s] def toField(name: String): Diff = {
    def applyTo(x: Option[JValue]) = x.map(y => JObject((name, y)))
    Diff(applyTo(changed), applyTo(added), applyTo(deleted))
  }
}

/** Computes a diff between two JSONs.
 */
object Diff {

  /** Return a diff.
   * <p>
   * Example:<pre>
   * val Diff(c, a, d) = ("name", "joe") ~ ("age", 10) diff ("fname", "joe") ~ ("age", 11)
   * c = JObject(("age",JInt(11)) :: Nil)
   * a = JObject(("fname",JString("joe")) :: Nil)
   * d = JObject(("name",JString("joe")) :: Nil)
   * </pre>
   */
  def diff(val1: JValue, val2: JValue): Diff = (val1, val2) match {
    case (x, y) if x == y => Diff(None, None, None)
    case (JObject(xs), JObject(ys)) => diffFields(xs, ys)
    case (JArray(xs), JArray(ys)) => diffVals(xs, ys)
    case (JInt(x), JInt(y)) if (x != y) => Diff(Some(JInt(y)), None, None)
    case (JDouble(x), JDouble(y)) if (x != y) => Diff(Some(JDouble(y)), None, None)
    case (JDecimal(x), JDecimal(y)) if (x != y) => Diff(Some(JDecimal(y)), None, None)
    case (JString(x), JString(y)) if (x != y) => Diff(Some(JString(y)), None, None)
    case (JBool(x), JBool(y)) if (x != y) => Diff(Some(JBool(y)), None, None)
    case (x, y) => Diff(Some(y), None, None)
  }

  private def diffFields(vs1: List[JField], vs2: List[JField]) = {
    def merge(x: Option[JValue], y: Option[JValue]): Option[JValue] = x match {
      case Some(a) => y match {
        case Some(b) =>
          Some(a merge b)
        case None =>
          x
      }
      case None =>
        y
    }

    def diffRec(xleft: List[JField], yleft: List[JField]): Diff = xleft match {
      case Nil => Diff(None, if (yleft.isEmpty) None else Some(JObject(yleft)), None)
      case x :: xs => yleft find (_._1 == x._1) match {
        case Some(y) =>
          val Diff(c1, a1, d1) = diff(x._2, y._2).toField(y._1)
          val Diff(c2, a2, d2) = diffRec(xs, yleft filterNot (_ == y))
          Diff(merge(c1, c2), merge(a1, a2), merge(d1, d2))
        case None =>
          val Diff(c, a, d) = diffRec(xs, yleft)
          Diff(c, a, d.map(JObject(x :: Nil) merge _))
      }
    }

    diffRec(vs1, vs2)
  }

  private def diffVals(vs1: List[JValue], vs2: List[JValue]) = {
    def diffRec(xleft: List[JValue], yleft: List[JValue]): Diff = (xleft, yleft) match {
      case (xs, Nil) => Diff(None, None, if (xs.isEmpty) None else Some(JArray(xs)))
      case (Nil, ys) => Diff(None, if (ys.isEmpty) None else Some(JArray(ys)), None)
      case (x :: xs, y :: ys) =>
        def plusOption(a: Option[JValue], b: Option[JValue]): Option[JValue] = a match {
          case Some(aa) => b match {
            case Some(bb) =>
              Some(aa ++ bb)
            case None =>
              a
          }
          case None =>
            b
        }

        val Diff(c1, a1, d1) = diff(x, y)
        val Diff(c2, a2, d2) = diffRec(xs, ys)
        Diff(plusOption(c1, c2), plusOption(a1, a2), plusOption(d1, d2))
    }

    diffRec(vs1, vs2)
  }

  private[json4s] trait Diffable { this: org.json4s.JsonAST.JValue =>
    /** Return a diff.
      *
      * @see org.json4s.Diff#diff
     */
    def diff(other: JValue) = Diff.diff(this, other)
  }
}
