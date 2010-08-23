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
import java.awt.Dimension
import javax.swing.JOptionPane
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.PageSize
import com.itextpdf.text.FontFactory
import com.itextpdf.text.Font
import com.itextpdf.text.Image
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream
import java.io.FileInputStream
import java.io.ObjectOutputStream
import java.io.ObjectInputStream
import java.util.Calendar
import java.util.Date
import java.text.SimpleDateFormat

object AllSelector extends Reactor {
  reactions += {
    case FocusGained(c,o,t) => {
      println("FOCUS GAINED")
      c.asInstanceOf[TextComponent].selectAll
    }
  }
}

object App extends SimpleSwingApplication {
  val version = "1.1"

  val menuSave = new MenuItem("Speichern")
  val menuReset = new MenuItem("Zurücksetzen")
  val menuLoad = new MenuItem("Laden")
  val menuAdd  = new MenuItem("Neues Medikament")
  val menuPrint = new MenuItem("Druckansicht")
  val menuQuit = new MenuItem("Beenden")
  val menuAbout = new MenuItem("Information")

  val buttonNewMed = new Button("Neues Medikament hinzufügen")

  val buttonPrint = new Button("Druckansicht")

  val entryPatient = new TextField("Patientenname") {
    maximumSize = new Dimension(600,30)
  }
  AllSelector.listenTo(entryPatient)


  val mainBox = new BoxPanel(Orientation.Vertical) {
    border = Swing.EmptyBorder(10,30,10,30)
    contents += buttonNewMed
    contents += buttonPrint
    contents += entryPatient
  }

  val mBar = new MenuBar {
    contents += new Menu("Datei") {
      contents += menuAdd
      contents += menuLoad
      contents += menuPrint
      contents += menuSave
      contents += menuReset
      contents += menuQuit
    }
    contents += new Menu("Hilfe") {
      contents += menuAbout
    }
  }

  var mBoxes = List[MedBox]()

  val infoMsg = "<html><b>SMed - Medikamentenbeilage</b>\n"+
  "Copyright (c) 2010 Roman Naumann\n"+
  "Veröffentlicht unter GPLv3 Lizenz, siehe COPYING.txt\n"+
  "\n"+
  "http://www.github.com/rumpelstielzchen/SMed\n"+
  "roman_naumann at fastmail.fm"



  def top = new MainFrame {
    title = "SMed - Medikamenten Beilage"
    preferredSize = new Dimension(800,550)

    contents = mainBox
    menuBar = mBar

    listenTo(menuAdd)
    listenTo(menuAbout)
    listenTo(menuSave)
    listenTo(menuLoad)
    listenTo(menuPrint)
    listenTo(menuQuit)
    listenTo(buttonNewMed)
    listenTo(buttonPrint)
    listenTo(menuReset)

    reactions += {
      case ButtonClicked(b) => {
        if(b==menuReset) {
          clearMainBox
          mainBox.revalidate
        } 
        if(b==menuSave)
          save
        if(b==menuLoad)
          load
        if(b==menuAbout)
          JOptionPane.showMessageDialog(null
                                        ,infoMsg
                                        ,"SMed - "+version
                                        ,JOptionPane.INFORMATION_MESSAGE)
        if(b==menuQuit)
          quit()
        if(b==buttonNewMed || b==menuAdd) {
          addMed
        }
        if(b==buttonPrint || b==menuPrint) {
          createPDF
        }
      }
    }
  }

  def addMed() {
    val med = new MedBox
    mBoxes = mBoxes ++ List(med)
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


  def parseMeds() = mBoxes map (_.parseMedicine)

  def clearMainBox() = {
    var toRem:List[Component] = List()
    for (c <- mainBox.contents) {
      if (!(c==buttonNewMed || c==buttonPrint || c==entryPatient)) {
        c.enabled=false
        c.visible=false
        toRem = c::toRem
      }
    }
    for (c <- toRem) mainBox.contents -= c

    mBoxes = List()
  }

  def load(): Unit = {
    try {
      val saves = new File("saves")
      saves.mkdir
      val fileChooser = new FileChooser(saves)
      fileChooser.showOpenDialog(mainBox)
      val inFile = fileChooser.selectedFile
      if(!inFile.canRead()) {
        JOptionPane.showMessageDialog(null
                                      ,"Die gewählte Datei kann "+
                                      "nicht gelesen werden."
                                      ,"Fehler beim Laden"
                                      ,JOptionPane.ERROR_MESSAGE)
        return ()
      }
      val ois = new ObjectInputStream(new FileInputStream(inFile))
      val meds = ois.readObject.asInstanceOf[List[Medicine]]
      ois.close

      clearMainBox
      
      for(m <- meds) {
        addMed
        val box = mBoxes.last
        box.medName.text = m.name
        box.tabSize.text = m.tabletSize
        box.beginDate.text = Util.dateFormat.format(m.begin)

        box.cyclesBox.contents.clear
        box.cyclesBox.cycleBoxes = List()
        for(c <- m.cycles) {
          box.cyclesBox.addCycleBox
          box.cyclesBox.cycleBoxes.last.cDays.text = if (c.days == -1) ""
                                                     else c.days.toString
          box.cyclesBox.cycleBoxes.last.cNumTabs.text = c.num.toString
          box.cyclesBox.cycleBoxes.last.plusMinus.text = "-"
        }
        box.cyclesBox.cycleBoxes.last.plusMinus.text = "+"
      }

      mainBox.revalidate
    } catch {
      case e:Exception =>
        JOptionPane.showMessageDialog(null
                                      ,"Beim Laden der Datei ist "+
                                      "ein Fehler aufgetreten.\n"+
                                      "Bitte überprüfen sie ihre Angaben."
                                      ,"Fehler beim Laden"
                                      ,JOptionPane.ERROR_MESSAGE)
    }    
  }

  def save(): Unit = {
    try {
      val saves = new File("saves")
      saves.mkdir
      val meds = parseMeds
      val fileChooser = new FileChooser(saves)
      fileChooser.showSaveDialog(mainBox)
      var outFile = fileChooser.selectedFile
      if(!outFile.getAbsolutePath.matches(".*\\.med"))
        outFile = new File(outFile.getAbsolutePath+".med")
      outFile.createNewFile
      if(!outFile.canWrite()) {
        JOptionPane.showMessageDialog(null
                                      ,"In die gewählte Datei kann "+
                                      "nicht geschrieben werden."
                                      ,"Fehler beim Speichern"
                                      ,JOptionPane.ERROR_MESSAGE)
        return ()
      }
      val oos = new ObjectOutputStream(new FileOutputStream(outFile))
      oos.writeObject(meds)
      oos.flush
      oos.close
    } catch {
      case e:Exception =>
        JOptionPane.showMessageDialog(null
                                      ,"Beim Speichern der Datei ist "+
                                      "ein Fehler aufgetreten.\n"+
                                      "Bitte überprüfen sie ihre Angaben."
                                      ,"Fehler beim Speichern"
                                      ,JOptionPane.ERROR_MESSAGE)
    }
  }

  def createPDF() {
    val document = new Document(PageSize.A4, 50, 50, 50, 50)
    PdfWriter.getInstance(document , new FileOutputStream("beilage.pdf"))

    document.addAuthor("SMed "+version)
    document.addSubject("Medikamenten-Beilage")
    document.open();
    document.add(new Paragraph("Medikamenten-Information",fontTitle))
    document.add(new Paragraph("Bestimmt für: "+entryPatient.text,fontMedTitle))
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
