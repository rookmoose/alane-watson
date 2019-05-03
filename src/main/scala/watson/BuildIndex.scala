package watson

import java.io.File
import java.nio.file.Paths
import java.util.Properties

import org.apache.commons.lang3.StringUtils.isBlank
import edu.stanford.nlp.pipeline.{Annotation, AnnotationPipeline, StanfordCoreNLP}
import org.apache.commons.lang3.StringUtils
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.{Document, Field, StringField, TextField}
import org.apache.lucene.index.{IndexWriter, IndexWriterConfig}
import org.apache.lucene.store.FSDirectory

import scala.io.Source

class BuildIndex(var dir: Array[File], indexDir:String) {

  val stopWords = Array[String]("a", "an", "and", "are", "as", "at", "be", "but", "by",
    "for", "if", "in", "into", "is", "it",
    "no", "not", "of", "on", "or", "such",
    "that", "the", "their", "then", "there", "these",
    "they", "this", "to", "was", "will", "with", "[tpl]", "[/tpl]")

  val props = new Properties()
  props.setProperty("annotators","tokenize,ssplit,pos,lemma")
  val pipeline = new StanfordCoreNLP(props)

  val analyzer = new StandardAnalyzer()
  var index = FSDirectory.open(Paths.get(indexDir))
  val config = new IndexWriterConfig(analyzer)
  val w = new IndexWriter(index,config)

  val imagePattern = "\\[\\[Image(.+)\\]\\]".r
  val docIDPattern = "\\[\\[(.+)\\]\\]".r
  val categoriesPattern = "(CATEGORIES:)(.*)".r
  val tplPattern = "(.*)(\\[tpl\\])(.*)(\\[/tpl\\])(.*)".r
  val refPattern = "(.*)(\\[ref\\])(.*)(\\[/ref\\])(.*)".r
  val horizLinePattern = "^\\|".r
  var readingText = false
  var docID = ""
  var text:StringBuilder = new StringBuilder()
  var categories:StringBuilder = new StringBuilder()
  var fileNum = 1
  for(file <- dir) {
    readingText = false
    println(s"----FILE NAME: $file----")
    for (line <- Source.fromFile(file).getLines()) {

      if(StringUtils.isNotBlank(line)) {
        line match {
          case imagePattern(_) =>
            readingText = true
          case docIDPattern(temp) =>
            if (!readingText) {
              // save docId
              docID = temp
              readingText = false
            } else {
              // here we need to add doc to document, text will be filled. Will need to save newly read docid
              addDoc(w, docID, text.mkString, categories.mkString, pipeline)
              text.clear()
              categories.clear()
              docID = temp
              readingText = false
            }
          case horizLinePattern() =>
            readingText = true
          case categoriesPattern(_, a) =>
            categories.append(a.split(",").mkString(" "))
            readingText = true
          case refPattern(a, _, _, b) =>
            text.append(filterOutUndesirables(a + b, stopWords))
            readingText = true
          case tplPattern(a, _, _, _, b) =>
            text.append(filterOutUndesirables(a + b, stopWords))
            readingText = true
          case _ =>
            text.append(filterOutUndesirables(line, stopWords))
            readingText = true
        }
      }
    }// end read line from file

    // here we'll need to add the last in (EOF)
    addDoc(w, docID, text.mkString, categories.mkString, pipeline)
    // at this point the index will have been built for this file

   }// end read files in directory
  w.close()

  def filterOutUndesirables(line: String, stopWords: Array[String]): String = {
    var str:String = null
    val temp = line.split("[\\s]+")
      .filterNot(_.equalsIgnoreCase("#REDIRECT")).mkString(" ")
      .split("=+")
      .filterNot(_.equalsIgnoreCase("SEE ALSO"))
      .filterNot(_.equalsIgnoreCase("OTHER USES"))
      .filterNot(_.equalsIgnoreCase("EXTERNAL LINKS"))
      .filterNot(_.equalsIgnoreCase("REFERENCES"))
      .filterNot(_.equalsIgnoreCase("SECONDARY"))
      .filterNot(_.equalsIgnoreCase("FURTHER READING"))
      .mkString(" ")
    str = temp
    for(stopWord <- stopWords) {
      str = str.split(" ").filterNot(_ == stopWord).mkString(" ") + " "
    }
    return str
  }

  def addDoc(w: IndexWriter, docID: String, text: String, categories: String, pipeline: AnnotationPipeline) = {
    val doc = new Document()
    if (StringUtils.isNotBlank(text)) {
      val temp = new Annotation(text)
      doc.add(new TextField("text",temp.toString,Field.Store.YES))
    }

    if(StringUtils.isNotBlank(categories)) {
      val temp = new Annotation(text)
      doc.add(new TextField("categories",temp.toString, Field.Store.YES))
    }

    doc.add(new StringField("docID",docID,Field.Store.YES))
    w.addDocument(doc)
  }

}// end class BuildIndex
