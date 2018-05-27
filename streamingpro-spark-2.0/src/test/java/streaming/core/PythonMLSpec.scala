package streaming.core

import org.apache.spark.streaming.BasicSparkOperation
import streaming.core.strategy.platform.SparkRuntime
import streaming.dsl.ScriptSQLExec
import streaming.dsl.template.TemplateMerge

/**
  * Created by allwefantasy on 26/5/2018.
  */
class PythonMLSpec extends BasicSparkOperation with SpecFunctions with BasicMLSQLConfig {
  copySampleLibsvmData

  "sklearn-multi-model" should "work fine" in {
    withBatchContext(setupBatchContext(batchParams, "classpath:///test/empty.json")) { runtime: SparkRuntime =>
      //执行sql
      implicit val spark = runtime.sparkSession
      val sq = createSSEL
      ScriptSQLExec.parse(loadSQLScriptStr("sklearn-multi-model-trainning"), sq)
      spark.read.parquet("/tmp/william/tmp/model/0").show()
    }
  }

  "sklearn-user-script" should "work fine" in {
    withBatchContext(setupBatchContext(batchParams, "classpath:///test/empty.json")) { runtime: SparkRuntime =>
      //执行sql
      implicit val spark = runtime.sparkSession
      val sq = createSSEL
      val pythonCode =
        """
          |import mlsql_model
          |import mlsql
          |from sklearn.naive_bayes import MultinomialNB
          |
          |clf = MultinomialNB()
          |
          |mlsql.sklearn_configure_params(clf)
          |
          |
          |def train(X, y, label_size):
          |    clf.partial_fit(X, y, classes=range(label_size))
          |
          |
          |mlsql.sklearn_batch_data(train)
          |
          |X_test, y_test = mlsql.get_validate_data()
          |print("cool------")
          |if len(X_test) > 0:
          |    testset_score = clf.score(X_test, y_test)
          |    print("mlsql_validation_score:%f" % testset_score)
          |
          |mlsql_model.sk_save_model(clf)
          |
        """.stripMargin
      writeStringToFile("/tmp/sklearn-user-script.py", pythonCode)
      ScriptSQLExec.parse(TemplateMerge.merge(loadSQLScriptStr("sklearn-user-script"), Map(
        "pythonScriptPath" -> "/tmp/sklearn-user-script.py"
      )), sq)
      spark.read.parquet("/tmp/william/tmp/model/0").show()
    }
  }

  "sklearn-multi-model-with-sample" should "work fine" in {
    withBatchContext(setupBatchContext(batchParams, "classpath:///test/empty.json")) { runtime: SparkRuntime =>
      //执行sql
      implicit val spark = runtime.sparkSession
      val sq = createSSEL
      ScriptSQLExec.parse(loadSQLScriptStr("sklearn-multi-model-trainning-with-sample"), sq)
      spark.read.parquet("/tmp/william/tmp/model/0").show()
    }
  }

  "tensorflow-cnn-model" should "work fine" in {
    withBatchContext(setupBatchContext(batchParams, "classpath:///test/empty.json")) { runtime: SparkRuntime =>
      //执行sql
      implicit val spark = runtime.sparkSession
      val sq = createSSEL
      ScriptSQLExec.parse(loadSQLScriptStr("tensorflow-cnn"), sq)

    }
  }
}
