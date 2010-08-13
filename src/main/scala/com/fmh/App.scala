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
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.PageSize
import com.itextpdf.text.FontFactory
import com.itextpdf.text.Font
import com.itextpdf.text.Image
import com.itextpdf.text.pdf.PdfWriter
import java.io.FileOutputStream
import java.util.Calendar
import java.util.Date
import java.text.SimpleDateFormat

object App extends SimpleSwingApplication {
  def top = new MainFrame {
    title = "SMed - Medikamenten Beilage"
    preferredSize = new Dimension(800,550)

    val menuQuit = new MenuItem("Beenden")
    //val menuAbout = new MenuItem("Information")
    val buttonNewMed = new Button("Neues Medikament hinzufügen")
    val buttonPrint = new Button("Druckansicht")

    val mainBox = new BoxPanel(Orientation.Vertical) {
      border = Swing.EmptyBorder(10,30,10,30)
      contents += buttonNewMed
      contents += buttonPrint
    }

    menuBar = new MenuBar {
      contents += new Menu("Datei") {
        contents += menuQuit
      }
    }

    contents = mainBox

    listenTo(menuQuit)
    listenTo(buttonNewMed)
    listenTo(buttonPrint)

    var mBoxes = List[MedBox]()

    reactions += {
      case ButtonClicked(b) =>
        if(b==menuQuit)
          quit()
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
        if(b==buttonPrint) {
          createPDF
        }
    }


  def parseMeds() = mBoxes map (_.parseMedicine)

  def createPDF() {
    val document = new Document(PageSize.A4, 50, 50, 50, 50)
    PdfWriter.getInstance(document , new FileOutputStream("beilage.pdf"))

    document.addAuthor("SMed")
    document.addSubject("Medikamenten-Beilage")
    document.open();
    document.add(new Paragraph("Medikamenten-Information",fontTitle))
    val logo = Image.getInstance("data/logo.jpg")
    document.add(logo)
    
    for (med <- parseMeds()) {
      val eBegin = med.begin
      document.add(new Paragraph("Medikament: "+med.name,fontMedTitle))
      document.add(new Paragraph("\tTablettengröße: "+med.tabletSize,fontMed))
      document.add(new Paragraph("\tEinnahmebeginn: "+fD(eBegin),fontMed))
      val cal = Calendar.getInstance
      cal setTime eBegin      
      for (cycle <- med.cycles) {
        val tabNum = med.tabletSize filter (_.isDigit)
        val tabUnit = med.tabletSize filterNot (_.isDigit)
        if (cycle.days == -1)
          document.add(new Paragraph("\t\tNehmen sie vom "
                                     +fD(cal.getTime)+" an"
                                     +" täglich je "+cycle.num+"-Tablette(n) "
                                     +"("+PosRational.div(tabNum.toInt,cycle.num)
                                     +tabUnit+") ein."))

        else {
          val cBegin = cal.clone.asInstanceOf[Calendar]
          cal.add(Calendar.DATE,cycle.days)
          document.add(new Paragraph("\t\tNehmen sie vom "
                                     +fD(cBegin.getTime)+" bis zum "+fD(cal.getTime())
                                     +" täglich je "+cycle.num+"-Tablette(n) "
                                     +"("+PosRational.div(tabNum.toInt,cycle.num)
                                     +tabUnit+") ein."))
        }
      }
    }
    document.add(new Paragraph("\n\n\n"))
    val kontakt = io.Source.fromFile("data/kontakt.txt").mkString
    document.add(new Paragraph(kontakt))
    document.close()
    Runtime.getRuntime().exec("cmd /c start " + "beilage.pdf")
  }


  }


  def fD(d:Date):String = Util.dateFormat.format(d)

  private val fontTitle = FontFactory.getFont(FontFactory.HELVETICA
                                             ,22,Font.BOLDITALIC)
  private val fontMedTitle = FontFactory.getFont(FontFactory.COURIER
                                                ,18)
  private val fontMed = FontFactory.getFont(FontFactory.COURIER
                                           ,14)
}

object Util {
  val dateFormat = new SimpleDateFormat("dd.MM.yyyy")
}
