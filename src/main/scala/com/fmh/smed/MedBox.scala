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

package com.fmh.smed

import scala.swing._
import scala.swing.event._
import java.util.Date
import java.text.ParsePosition
import java.awt.Dimension

class MedBox extends BoxPanel(Orientation.Horizontal) {
  val medName = new TextField("Medikamentenname") {
    maximumSize = new Dimension(400,30)
  }
  val tabSize = new TextField("Tabl. Gr.") {
    maximumSize = new Dimension(150,30)
    inputVerifier = Verifiers.numUnit
  }
  val beginDate = new TextField(Util.dateFormat.format(new Date)) {
    maximumSize = new Dimension(100,30)
    inputVerifier = Verifiers.date
  }
  val cyclesBox = new CyclesBox

  contents += medName
  contents += tabSize
  contents += beginDate
  contents += cyclesBox
  AllSelector.listenTo(medName,tabSize,beginDate)

  def parseMedicine() = new Medicine(
    name = medName.text
   ,tabletSize = tabSize.text
   ,begin = Util.dateFormat.parse(beginDate.text,new ParsePosition(0))
   ,cycles = cyclesBox.parseCycles
  )
}

class CyclesBox extends BoxPanel(Orientation.Vertical) {
  var cycleBoxes = List[SingleCycleBox](new SingleCycleBox)
  listenTo(cycleBoxes(0).plusMinus)
  contents ++= cycleBoxes

  def parseCycles() = cycleBoxes map (_.parseCycle)

  reactions += {
    case ButtonClicked(b) =>
      if (b.text equals "+") {
        b.text = "-"
        val newCycleBox = new SingleCycleBox
        cycleBoxes = cycleBoxes :+ newCycleBox
        listenTo(newCycleBox.plusMinus)
        contents.clear
        contents ++= cycleBoxes
        revalidate
      }
      else if (b.text equals "-") {
        deafTo(b)
        cycleBoxes = cycleBoxes filterNot (b==_.plusMinus)
        contents.clear
        contents ++= cycleBoxes
        revalidate
      }
  }
}

class SingleCycleBox extends BoxPanel(Orientation.Horizontal) {
  val cDays = new TextField("Tage") {
    maximumSize = new Dimension(80,30)
    inputVerifier = Verifiers.customDate
  }
  val cNumTabs = new TextField("Tbl.") {
    maximumSize = new Dimension(80,30)
    inputVerifier = Verifiers.posRational
  }
  val plusMinus = new Button("+") {
    maximumSize = new Dimension(80,30)
  }
  AllSelector.listenTo(cDays,cNumTabs)
  contents += cDays
  contents += cNumTabs
  contents += plusMinus

  def parseCycle() = new MedCycle(if ((cDays.text equals "")
                                      || (cDays.text equals "Tage"))
                                    -1
                                  else
                                    cDays.text.toInt
                                 ,PosRational(cNumTabs.text))
}

object Verifiers {
  val num = (c:Component) => !c.asInstanceOf[TextField].text.exists(!_.isDigit)
  val posRational = (c:Component) => PosRational.isValid(c.asInstanceOf[TextField].text)
  val date = (c:Component) => isDate(c.asInstanceOf[TextField].text) 
  val customDate = (c:Component) => (isDate(c.asInstanceOf[TextField].text)
                                     || c.asInstanceOf[TextField].text == "Tage"
                                     || c.asInstanceOf[TextField].text == "")
  val numUnit = (c:Component) => c.asInstanceOf[TextField].text.matches(
    "\\d+\\p{Alpha}+"
  )

  private def isDate(s:String): Boolean = {
    try {
      Util.dateFormat.parse(s,new ParsePosition(0))
    } catch {
      case e:Exception => return false
    }
    return true
  }
}
