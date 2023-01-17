/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.apprenticeshiplevy.utils

import play.utils.UriEncoding.decodePath

object DecodePath {

  def decodeAnyDoubleEncoding(path:String):String = {

    if(path.contains("%25")){
      val replacedPath = path.replace("{nino}", "replace-with-nino")

      decodePath(replacedPath, "utf-8")
        .replace("replace-with-nino", "{nino}")
    }else{
      path
    }

  }

}
