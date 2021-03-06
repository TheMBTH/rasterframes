/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright 2019 Astraea, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *     [http://www.apache.org/licenses/LICENSE-2.0]
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */

package astraea.spark.rasterframes.model
import astraea.spark.rasterframes.model.LazyCRS.EncodedCRS
import geotrellis.proj4.CRS
import org.locationtech.proj4j.CoordinateReferenceSystem

class LazyCRS(encoded: EncodedCRS) extends CRS {
  private lazy val delegate = LazyCRS.mapper(encoded)
  override def proj4jCrs: CoordinateReferenceSystem = delegate.proj4jCrs
  override def toProj4String: String =
    if (encoded.startsWith("+proj")) encoded
    else delegate.toProj4String
}

object LazyCRS {
  trait ValidatedCRS
  type EncodedCRS = String with ValidatedCRS

  private val mapper: PartialFunction[String, CRS] = {
    case e if e.toUpperCase().startsWith("EPSG")   => CRS.fromName(e) //not case-sensitive
    case p if p.startsWith("+proj")                => CRS.fromString(p) // case sensitive
    case w if w.toUpperCase().startsWith("GEOGCS") => CRS.fromWKT(w) //only case-sensitive inside double quotes
  }

  def apply(value: String): CRS = {
    if (mapper.isDefinedAt(value)) {
      new LazyCRS(value.asInstanceOf[EncodedCRS])
    }
    else throw new IllegalArgumentException(
      "crs string must be either EPSG code, +proj string, or OGC WKT")
  }
}
