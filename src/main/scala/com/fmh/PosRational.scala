/*
 * Copyright 2010 Roman Naumann
 *
 * This file is part of SMed.
 *
 * SMed is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SMed is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SMed.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.fmh

class PosRational(n:Int,d:Int) {
  require (d>0)
  require (n>0)
 
  val numer:Int = n/gcd(n,d)
  val denom:Int = d/gcd(n,d)

  def this(n:Int) = this(n,1)

  override def toString = if (denom==1)
                            ""+numer
                          else
                            numer +"/"+ denom

  private def gcd(a:Int, b:Int):Int =
    if (b==0) a else gcd(b, a%b)
}

object PosRational {
  def div(x:Int,y:PosRational):Double = x*y.numer/y.denom
  
  def apply(s:String):PosRational = {
    if(s exists (_=='/')) {
      val xs = s split '/'
      return new PosRational(xs(0).toInt,xs(1).toInt)
    }
    else
      return new PosRational(s.toInt,1)
  }
}
