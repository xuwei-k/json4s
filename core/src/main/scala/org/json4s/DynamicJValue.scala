package org.json4s

import scala.language.dynamics

class DynamicJValue(val raw: Option[JValue]) extends Dynamic {
  /**
   * Adds dynamic style to JValues. Only meaningful for JObjects
   * <p>
   * Example:<pre>
   * JObject(JField("name",JString("joe"))::Nil).name == JString("joe")
   * </pre>
   */
  def selectDynamic(name: String) = new DynamicJValue(raw.flatMap(_ \ name))
  
  override def hashCode():Int = raw.hashCode

  override def equals(p1: Any): Boolean = p1 match {
    case j: DynamicJValue => raw == j.raw
    case j: JValue => raw == Some(j)
    case _ => false
  }
}

trait DynamicJValueImplicits {
  implicit def dynamic2Jv(dynJv: DynamicJValue) = dynJv.raw
  def dyn(jv:JValue) = new DynamicJValue(Some(jv))
}

object DynamicJValue extends DynamicJValueImplicits

