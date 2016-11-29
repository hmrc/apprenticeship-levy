import play.sbt.PlayRunHook
import sbt._

/*
  Grunt runner
*/
object DocGeneration {
  def apply(base: File): PlayRunHook = {

    object DocGenerationProcess extends PlayRunHook {

      var generateAPIDocsRun: Option[Process] = None

      override def beforeStarted(): Unit = {
        val env = "dev"
        generateAPIDocsRun = Some(generateAPIDocs(base, Seq.empty[java.io.File], env).run)
      }

      override def afterStopped(): Unit = {
        // Stop when play run stops
       generateAPIDocsRun.map(p => p.destroy())
       generateAPIDocsRun = None
      }

    }

    DocGenerationProcess
  }

  def command(base: File) = Command.command("api-docs") { state =>
    generateAPIDocs(base, Seq.empty[java.io.File]) !;
    state
  }

  def generateAPIDocs(base: File, classpath: Seq[java.io.File], args: String*) = {
    // have to use Process rather than fork because of NPE with get class loader in vnu tool
    val log = ConsoleLogger()
    log.info("Starting API Documentation Generation task...")

    log.info(base.getAbsolutePath())

    val files = new File(base, "docs/endpoints/xml").listFiles.filter(_.getName.endsWith(".xml"))

    val v1DocDir = "/public/documentation/1.0/"
    val tuples = Map("01_root.xml" -> s"${base.getAbsolutePath()}${v1DocDir}root.xml",
                     "06_employment-check.xml" -> s"${base.getAbsolutePath()}${v1DocDir}employment-check.xml",
                     "02_empref.xml" -> s"${base.getAbsolutePath()}${v1DocDir}empref.xml",
                     "03_levy-declarations.xml" -> s"${base.getAbsolutePath()}${v1DocDir}levy-declarations.xml",
                     "05_fraction-calculation-date.xml" -> s"${base.getAbsolutePath()}${v1DocDir}fraction-calculation-date.xml",
                     "04_fraction-calculations.xml" -> s"${base.getAbsolutePath()}${v1DocDir}fraction-calculations.xml")
    var processes = files.flatMap { case (file)=>
      val in = file.getAbsolutePath()

      Seq("doc-v1.xslt").flatMap { case (xslFilename) =>
        val xsl = s"./docs/endpoints/xml/xsl/${xslFilename}"
        val out = tuples(file.getName())
        log.info(out)
        val p = Process("java" :: "-jar" :: s"${classpath(0)}" :: s"-s:$in" :: s"-xsl:$xsl" :: s"-o:$out" :: args.toList, base)
        List(p)
      }
    }
    processes ++= Seq(Process("java" :: "-jar" :: s"${classpath(0)}" :: s"-it:main" :: s"-xsl:./docs/endpoints/xml/xsl/doc-v1-def.xslt" :: s"-o:${base.getAbsolutePath()}/public/api/definition.json" :: args.toList, base))

    val echo = Process("echo" :: "'Starting API documentation generation'" :: List.empty[String], base)
    val pipeline = processes.foldLeft(echo) { case (pipe,process) =>
      pipe ### process
    }
    pipeline
  }
}
