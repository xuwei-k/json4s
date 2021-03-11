package org.json4s
package jackson

import com.fasterxml.jackson.databind.ser.Serializers
import com.fasterxml.jackson.databind.{BeanDescription, JavaType, SerializationConfig}
import org.json4s.JsonAST._

private object JValueSerializerResolver extends Serializers.Base {
  private[this] val JVALUE = classOf[JValue]
  override def findSerializer(config: SerializationConfig, theType: JavaType, beanDesc: BeanDescription) = {
    if (!JVALUE.isAssignableFrom(theType.getRawClass)) null
    else new JValueSerializer
  }

}
