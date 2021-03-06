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

package astraea.spark.rasterframes.expressions.tilestats
import astraea.spark.rasterframes.expressions.{UnaryRasterOp, NullToValue}
import astraea.spark.rasterframes.model.TileContext
import geotrellis.raster._
import org.apache.spark.sql.{Column, TypedColumn}
import org.apache.spark.sql.catalyst.expressions.{Expression, ExpressionDescription}
import org.apache.spark.sql.catalyst.expressions.codegen.CodegenFallback
import org.apache.spark.sql.types.{DataType, LongType}

@ExpressionDescription(
  usage = "_FUNC_(tile) - Counts the number of non-no-data cells in a tile",
  arguments = """
  Arguments:
    * tile - tile column to analyze""",
  examples = """
  Examples:
    > SELECT _FUNC_(tile);
       357"""
)
case class DataCells(child: Expression) extends UnaryRasterOp
  with CodegenFallback with NullToValue {
  override def nodeName: String = "data_cells"
  override def dataType: DataType = LongType
  override protected def eval(tile: Tile, ctx: Option[TileContext]): Any = DataCells.op(tile)
  override def na: Any = 0L
}
object DataCells {
  import astraea.spark.rasterframes.encoders.StandardEncoders.PrimitiveEncoders.longEnc
  def apply(tile: Column): TypedColumn[Any, Long] =
    new Column(DataCells(tile.expr)).as[Long]

  val op = (tile: Tile) => {
    var count: Long = 0
    tile.dualForeach(
      z ⇒ if(isData(z)) count = count + 1
    ) (
      z ⇒ if(isData(z)) count = count + 1
    )
    count
  }
}
