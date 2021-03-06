package streaming.core

import java.io.File
import java.sql.DriverManager

import org.apache.commons.io.{FileUtils, IOUtils}
import org.apache.http.HttpVersion
import org.apache.http.client.fluent.{Form, Request}
import org.apache.http.util.EntityUtils
import org.apache.spark.sql.SparkSession
import streaming.dsl.ScriptSQLExecListener

/**
  * Created by allwefantasy on 28/4/2018.
  */
trait SpecFunctions {
  def request(url: String, params: Map[String, String]) = {
    val form = Form.form()
    params.map(f => form.add(f._1, f._2))
    val res = Request.Post(url)
      .useExpectContinue()
      .version(HttpVersion.HTTP_1_1).bodyForm(form.build())
      .execute().returnResponse()
    if (res.getStatusLine.getStatusCode != 200) {
      null
    } else {
      new String(EntityUtils.toByteArray(res.getEntity))
    }
  }

  def createSSEL(implicit spark: SparkSession) = {
    new ScriptSQLExecListener(spark, "/tmp/william", Map())
  }

  def dropTables(tables: Seq[String])(implicit spark: SparkSession) = {
    try {
      tables.foreach { table =>
        spark.sql("drop table " + table).count()
      }
    } catch {
      case e: Exception =>
    }
  }

  def jdbc(ddlStr: String) = {
    Class.forName("com.mysql.jdbc.Driver")
    val con = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/wow?characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&tinyInt1isBit=false",
      "root",
      "csdn.net")
    val stat = con.createStatement()
    stat.execute(ddlStr)
    stat.close()
    con.close()
  }

  def loadSQLScriptStr(name: String) = {
    val file = s"/test/sql/${name}.sql"
    val stream = SpecFunctions.this.getClass.getResourceAsStream(file)
    if (stream == null) throw new RuntimeException(s"load file: ${file} failed,please chech the path")
    scala.io.Source.fromInputStream(stream).getLines().mkString("\n")
  }

  def loadSQLStr(name: String) = {
    scala.io.Source.fromInputStream(SpecFunctions.this.getClass.getResourceAsStream(s"/test/sql/${name}")).getLines().mkString("\n")
  }

  def loadDataStr(name: String) = {
    scala.io.Source.fromInputStream(SpecFunctions.this.getClass.getResourceAsStream(s"/data/mllib/${name}")).getLines().mkString("\n")
  }

  def loadPythonStr(name: String) = {
    scala.io.Source.fromInputStream(SpecFunctions.this.getClass.getResourceAsStream(s"/python/${name}")).getLines().mkString("\n")
  }

  def getDirFromPath(filePath: String) = {
    filePath.stripSuffix("/").split("/").dropRight(1).mkString("/")
  }

  def delDir(file: String) = {
    require(file.stripSuffix("/").split("/").size > 1, s"delete $file  maybe too dangerous")
    FileUtils.forceDelete(new File(file))
  }

  def writeStringToFile(file: String, content: String) = {
    FileUtils.forceMkdir(new File(getDirFromPath(file)))
    FileUtils.writeStringToFile(new File(file), content)
  }

  def writeByteArrayToFile(file: String, content: Array[Byte]) = {
    FileUtils.forceMkdir(new File(getDirFromPath(file)))
    FileUtils.writeByteArrayToFile(new File(file), content)
  }

  def copySampleLibsvmData = {
    writeStringToFile("/tmp/william/sample_libsvm_data.txt", loadDataStr("sample_libsvm_data.txt"))
  }

  def copySampleMovielensRratingsData = {
    writeStringToFile("/tmp/william/sample_movielens_ratings.txt", loadDataStr("als/sample_movielens_ratings.txt"))
  }
}
