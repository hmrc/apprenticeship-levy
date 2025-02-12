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

package uk.gov.hmrc.apprenticeshiplevy.data.des

import play.api.libs.json.{Json, OFormat, OWrites, Reads}

case class HodName(nameLine1: Option[String] = None, nameLine2: Option[String] = None)

case class HodTelephone(telephoneNumber: Option[String] = None, fax: Option[String] = None)

case class HodEmail(primary: Option[String] = None)

case class HodAddress(addressLine1: Option[String] = None,
                      addressLine2: Option[String] = None,
                      addressLine3: Option[String] = None,
                      addressLine4: Option[String] = None,
                      addressLine5: Option[String] = None,
                      postcode: Option[String] = None,
                      foreignCountry: Option[String] = None
                     )

case class HodContact(telephone: Option[HodTelephone] = None, email: Option[HodEmail] = None)

case class DesignatoryDetailsData(name: Option[HodName] = None, address: Option[HodAddress] = None, contact: Option[HodContact] = None)

case class DesignatoryDetails(empref:Option[String], employer: Option[DesignatoryDetailsData] = None, communication: Option[DesignatoryDetailsData] = None)

case class DesignatoryDetailsLinks(employer: Option[String], communication: Option[String])

case class HodDesignatoryDetailsLinks(links: Option[DesignatoryDetailsLinks])

object DesignatoryDetails {
  implicit val readDesignatoryDetailsFormat: Reads[DesignatoryDetails] = Json.reads[DesignatoryDetails]
  implicit val writeDesignatoryDetailsFormat: OWrites[DesignatoryDetails] = Json.writes[DesignatoryDetails]
}

object HodDesignatoryDetailsLinks {
  implicit val ddlformat: OFormat[DesignatoryDetailsLinks] = Json.format[DesignatoryDetailsLinks]

  implicit val readDesignatoryDetailsLinksFormat: Reads[HodDesignatoryDetailsLinks] = Json.reads[HodDesignatoryDetailsLinks]
  implicit val writeDesignatoryDetailsLinksFormat: OWrites[HodDesignatoryDetailsLinks] = Json.writes[HodDesignatoryDetailsLinks]
}

object DesignatoryDetailsData {
  implicit val hnformat: OFormat[HodName] = Json.format[HodName]
  implicit val haformat: OFormat[HodAddress] = Json.format[HodAddress]
  implicit val htformat: OFormat[HodTelephone] = Json.format[HodTelephone]
  implicit val heformat: OFormat[HodEmail] = Json.format[HodEmail]
  implicit val hcformat: OFormat[HodContact] = Json.format[HodContact]

  implicit val dddformat: OFormat[DesignatoryDetailsData] = Json.format[DesignatoryDetailsData]
}
