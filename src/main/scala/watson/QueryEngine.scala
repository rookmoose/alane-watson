package watson

import java.nio.file.Paths
import java.util
import java.util.Properties

import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import org.apache.lucene.analysis.core.WhitespaceAnalyzer
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser
import org.apache.lucene.queryparser.flexible.standard.QueryParserUtil
import org.apache.lucene.search.similarities.BooleanSimilarity
import org.apache.lucene.search.{IndexSearcher, TopScoreDocCollector}
import org.apache.lucene.store.FSDirectory

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer
import scala.io.Source

class QueryEngine() {

  var mrrTotal = 0.0
  val NUM_TERMS = 10
  var rank = 0
  var i = 0
  val analyzer = new WhitespaceAnalyzer
  val index: FSDirectory = FSDirectory.open(Paths.get("./indexDir"))
  val reader: DirectoryReader = DirectoryReader.open(index)
  val searcher = new IndexSearcher(reader)
  val answers = new ListBuffer[String]
  val fields = new Array[String](2)
  val boosts = new util.HashMap[String, Float]
  var parser: MultiFieldQueryParser = _
  val source: Source = Source.fromFile("./questions.txt")

  for (line <- source.getLines()) {
    i % 4 match {
      case 0 =>
        // CATEGORY
        fields(0) = line
        i += 1
      case 1 =>
        // CLUE
        fields(1) = line
        i += 1
      case 2 =>
        // ANSWER
        answers += line
        i += 1
      case 3 =>
        // NEWLINE

        boosts.put(fields(0), 1)
        boosts.put(fields(1), 1)
        parser = new MultiFieldQueryParser(Array("categories", "text"),
          analyzer,
          boosts.asInstanceOf[java.util.Map[java.lang.String, java.lang.Float]])
        val str = QueryParserUtil.escape(fields(0) + " " + fields(1))
        val collector = TopScoreDocCollector.create(NUM_TERMS)

        //searcher.setSimilarity(new BooleanSimilarity)

        searcher.search(parser.parse(str), collector)

        val hits = collector.topDocs().scoreDocs

        var j = 1
        if (hits.nonEmpty) {
          rank = 0
          for (hit <- hits) {
            val d = searcher.doc(hit.doc)
            if (answers.get(((i + 1) / 4) - 1) contains d.get("docID").mkString) {
              rank = j
            }
            j += 1
          }
        } else {
          rank = 0
        }

        if (rank != 0) mrrTotal += 1 / rank.toDouble

        boosts.clear()
        i += 1

    } //end match
  } // end loop through file
  reader.close()
  source.close()

  // results
  println(s"results = ${(mrrTotal / (i / 4)) * 100}%")

}

// end QueryEngine
