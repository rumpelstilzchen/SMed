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

import scala.swing._
import scala.swing.event._
import java.awt.Dimension

object App extends SimpleSwingApplication {
  def top = new MainFrame {
    title = "SMed - Medikamenten Beilage"
    preferredSize = new Dimension(800,550)

    val menuQuit = new MenuItem("Beenden")
    val menuAbout = new MenuItem("Information")
    val buttonNewMed = new Button("Neues Medikament hinzufügen")
    val print = new Button("Druckansicht")

    val mainBox = new BoxPanel(Orientation.Vertical) {
      border = Swing.EmptyBorder(10,30,10,30)
      contents += buttonNewMed
    }

    menuBar = new MenuBar {
      contents += new Menu("Datei") {
        contents += menuQuit
      }
      contents += new Menu("Hilfe") {
        contents += menuAbout
      }
    }

    contents = mainBox

    listenTo(menuQuit)
    listenTo(menuAbout)
    listenTo(buttonNewMed)

    var meds   = List[Medicine]()
    var mBoxes = List[MedBox]()

    reactions += {
      case ButtonClicked(b) =>
        if(b==menuQuit)
          quit()
        if(b==menuAbout) {
          mainBox.contents += new Button("abc") {
            visible = true
          }
          mainBox.revalidate
        }
        if(b==buttonNewMed) {
          val med = new MedBox
          mBoxes = mBoxes :+ med
          mainBox.contents += new Separator {
            foreground = background
            maximumSize = new Dimension(800,7)
          }
          mainBox.contents += med
          mainBox.contents += new Separator {
            foreground = background
            maximumSize = new Dimension(800,7)
          }
          mainBox.revalidate
        }
    }
  }

  private def newMed() = {
    
  }
}
