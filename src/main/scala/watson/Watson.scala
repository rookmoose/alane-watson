package watson

object Watson {
  def main(args: Array[String]): Unit = {

/*
    var index:BuildIndex = null
    try{
      val files = new java.io.File("./wikipages")
      index = new BuildIndex(files.listFiles(), "./indexDir")

    } catch {
      case ex: Exception => println(ex.getMessage)
    }
*/

    val query:QueryEngine = new QueryEngine()
  }//end main
}
