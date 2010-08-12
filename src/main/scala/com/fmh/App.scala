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
import scala.collection.mutable.LinkedList

object App extends SimpleSwingApplication {
  def top = new MainFrame {
    title = "SMed - Medikamenten Beilage"

    val menuQuit = new MenuItem("Beenden")
    val menuAbout = new MenuItem("Information")
    val buttonNewMed = new Button("Neues Medikament hinzufügen")

    menuBar = new MenuBar {
      contents += new Menu("Datei") {
        contents += menuQuit
      }
      contents += new Menu("Hilfe") {
        contents += menuAbout
      }
    }

    contents = new BoxPanel(Orientation.Vertical) {
      border = Swing.EmptyBorder(10,30,10,30)
      contents += buttonNewMed
    }

    listenTo(menuQuit)
    listenTo(menuAbout)
    listenTo(buttonNewMed)

    var meds = new LinkedList

    reactions += {
      case ButtonClicked(b) =>
        if(b==menuQuit)
          quit()
        if(b==menuAbout)
          print("about clicked")
        if(b==buttonNewMed)
          print("new med")
    }
  }
}
