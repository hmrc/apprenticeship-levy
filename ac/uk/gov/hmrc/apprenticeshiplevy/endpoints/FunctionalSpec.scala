/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.apprenticeshiplevy.endpoints

import java.io.File
import org.scalatest.funspec.AnyFunSpec
import uk.gov.hmrc.apprenticeshiplevy.config.Config

import scala.io.Source
import scala.util.Using

trait FunctionalSpec extends AnyFunSpec {
  val url: String = Config.rootUrl
  val dir: String = Config.resourceDir
  val contexts: Seq[(String, String)] = Config.contexts

  def standardHeaders(implicit environment: String): Seq[(String,String)] = Seq("Accept" -> "application/vnd.hmrc.1.0+json",
                                                                              "Authorization" -> s"""Bearer ${System.getProperty("bearer.token." +
                                                                              System.getProperty("environment", "local").toLowerCase)}""")

  def fileToStr(filename: String): String = {
    Using(Source.fromFile(new File(s"$filename"))) {
      bufferedSource => bufferedSource.getLines().mkString("\n")
    }
  }.get

  def fileToStr(file: File): String = {
    Using(Source.fromFile(file)) {
      bufferedSource => bufferedSource.getLines().mkString("\n")
    }
  }.get

}