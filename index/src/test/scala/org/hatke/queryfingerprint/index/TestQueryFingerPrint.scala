package org.hatke.queryfingerprint.index

import com.sksamuel.elastic4s.requests.mappings.MappingDefinition
import com.sksamuel.elastic4s.requests.searches.queries.Query
import com.sksamuel.elastic4s.requests.searches.queries.compound.BoolQuery
import com.sksamuel.elastic4s.requests.searches.term.TermQuery
import com.sksamuel.elastic4s.{Hit, HitReader, Indexable}
import org.hatke.queryfingerprint.json.JsonUtils
import org.json4s.jackson.Serialization
import org.json4s.{CustomSerializer, DefaultFormats, Formats, JInt, JString, ShortTypeHints}

import java.util.UUID
import scala.util.Try


object TestJoinType extends Enumeration {

  val inner = new JoinTypeVal(true)
  val semi = new JoinTypeVal(false)
  val leftOuter = new JoinTypeVal(true)
  val rightOuter = JoinTypeVal(true, leftOuter)
  leftOuter._flipType = rightOuter
  val fullOuter = new JoinTypeVal(true)
  val cross = new JoinTypeVal(true)

  protected case class JoinTypeVal(isFlippable: Boolean, private[index] var _flipType: JoinTypeVal) extends super.Val {
    self =>
    def this(isFlippable: Boolean) = this(isFlippable, null)

    lazy val flipType: JoinTypeVal = if (isFlippable && _flipType == null) self else _flipType
  }

  import scala.language.implicitConversions

  implicit def valueToJoinTypeVal(x: Value): JoinTypeVal = x.asInstanceOf[JoinTypeVal]
}

case class TestPredicate(column: String, operator: String, functionApplication: Option[String] = None) {

  def indexElements: Seq[String] = {
    Seq(column,
      s"${column} ${operator}"
    ) ++ functionApplication.map(f => s"${column} ${operator} ${f}").toSeq
  }

  def searchQuery: Query = {
    val termQs = for (iElem <- indexElements) yield {
      TermQuery("predicates", iElem)
    }

    BoolQuery().should(termQs)
  }
}

case class TestJoin(leftTable: String,
                    leftColumn: String,
                    rightTable: String,
                    rightColumn: String,
                    joinType: TestJoinType.Value) {

  def indexElements: Seq[String] = {
    Seq(
      s"${leftTable} ${rightTable}",
      s"${leftTable} ${rightTable} ${joinType}",
      s"${leftTable} ${rightTable} ${joinType} ${leftColumn} ${rightColumn}"
    )
  }

  def searchQuery : Query = {
    val termQs = for(iElem <- indexElements) yield {
      TermQuery("joins", iElem)
    }

    BoolQuery().should(termQs)
  }
}

case class TestFunctionApplication(functionName: String, column: String) {
  def indexElements: Seq[String] = {
    Seq(s"${functionName} ${column}")
  }

  def isAggregate: Boolean = TestFunctionApplication.AGG_FUNCTIONS.contains(functionName)

  /**
   *
   * @return (query for scalar funcs, query for agg funcs)
   */
  def searchQuery: (Option[Query], Option[Query]) = {

    val termQs = for (iElem <- indexElements) yield {
      TermQuery("functionApplications", iElem)
    }

    (if (!isAggregate)  Some(BoolQuery().should(termQs)) else None,
      if (isAggregate)  Some(BoolQuery().should(termQs)) else None
      )
  }
}

object TestFunctionApplication {
  val AGG_FUNCTIONS = Set("SUM", "COUNT", "MIN", "MAX", "AVG", "AVERAGE")
}

case class TestQueryFingerPrint(uuid: String,
                                tablesReferenced: Set[String],
                                columnsScanned: Set[String],
                                columnsFiltered: Set[String],
                                predicates: Set[TestPredicate],
                                joins: Set[TestJoin],
                                functionApplications: Set[TestFunctionApplication] = Set.empty,
                                groupedColumns: Set[String] = Set.empty,
                                orderedColumns: Set[String] = Set.empty,
                                id : Option[Int] = None
                               ) {

  @transient lazy val featureVector: Array[Int] = {
    Array(
      tablesReferenced.size,
      joins.size,
      predicates.size,
      groupedColumns.size,
      functionApplications.map(_.isAggregate).size
    )
  }

  def searchTermsForTables : Set[TermQuery] = tablesReferenced.map(g => TermQuery("tablesReferenced", g))
  def searchTermsForGroupCols : Set[TermQuery] = groupedColumns.map(g => TermQuery("groupedColumns", g))
  def searchTermsForOrderCols : Set[TermQuery] = orderedColumns.map(g => TermQuery("orderedColumns", g))

}

object TestQueryFingerPrint {

  private val SOURCE_FIELD = "qfp_source"
  private val FEATURE_VECTOR_DIM = 5

  implicit object TQFPIndexable extends Indexable[TestQueryFingerPrint] {
    override def json(qfp: TestQueryFingerPrint): String = {
      import org.json4s.DefaultWriters._
      import org.json4s.JsonDSL._
      import org.json4s.jackson.JsonMethods._

      val json  = ("tablesReferenced" -> qfp.tablesReferenced) ~
        ("columnsScanned" -> qfp.columnsScanned) ~
        ("columnsFiltered" -> qfp.columnsFiltered) ~
        ("predicates" -> qfp.predicates.flatMap(_.indexElements)) ~
        ("joins" -> qfp.joins.flatMap(_.indexElements)) ~
        ("functionApplications" -> qfp.functionApplications.flatMap(_.indexElements)) ~
        ("groupedColumns" -> qfp.groupedColumns) ~
        ("orderedColumns" -> qfp.orderedColumns) ~
        ("id" -> qfp.id) ~
        ("featureVector" -> asJValue(qfp.featureVector)) ~
        (SOURCE_FIELD -> JsonUtils.asJson(qfp)(jsonFormat))

      compact(render(json))
    }
  }

  implicit object TQFPHitReader extends HitReader[TestQueryFingerPrint] {
    override def read(hit: Hit): Try[TestQueryFingerPrint] = {
      Try {
        JsonUtils.fromJson[TestQueryFingerPrint](hit.sourceField(SOURCE_FIELD).asInstanceOf[String])
      }
    }
  }

  val elasticMapping = {
    import com.sksamuel.elastic4s.ElasticDsl._
    import com.sksamuel.elastic4s.fields.DenseVectorField

    val properties = Seq(
      keywordField("tablesReferenced"),
      keywordField("columnsScanned"),
      keywordField("columnsFiltered"),
      keywordField("columnsScanFiltered"),
      keywordField("groupedColumns"),
      keywordField("orderedColumns"),
      keywordField("predicates"),
      keywordField("scannedPredicates"),
      keywordField("functionApplications"),
      keywordField("joins"),
      intField("id"),
      DenseVectorField("featureVector", FEATURE_VECTOR_DIM),
      textField(SOURCE_FIELD).index(false)
    )

    MappingDefinition(properties)
  }

  object UUIDTypeSerializer extends CustomSerializer[UUID](
    format => ( {
      case x => UUID.fromString(x.asInstanceOf[JString].s)
    }, {
      case u: UUID => JString(u.toString)
    }
    )
  )

  object TestJoinTypeSerializer extends CustomSerializer[TestJoinType.Value](
    format => ( {
      case x => TestJoinType(x.asInstanceOf[JInt].num.toInt)
    }, {
      case jt: TestJoinType.Value => JInt(jt.id)
    }
    )
  )

  val INDEXINFO_CLASSES = List(
    classOf[TestPredicate],
    classOf[TestJoin],
    classOf[TestQueryFingerPrint]
  )

  implicit val jsonFormat: Formats = Serialization.formats(ShortTypeHints(INDEXINFO_CLASSES)) ++
    org.json4s.ext.JodaTimeSerializers.all +
    TestJoinTypeSerializer +
    UUIDTypeSerializer

  val tpcdsQFP: Map[String, TestQueryFingerPrint] = Map(
    "q1" -> TestQueryFingerPrint(
      uuid = "q1",
      tablesReferenced = Set("TPCDS.STORE_RETURNS", "TPCDS.DATE_DIM", "TPCDS.STORE", "TPCDS.CUSTOMER"),
      columnsScanned = Set("TPCDS.STORE_RETURNS.SR_RETURNED_DATE_SK",
        "TPCDS.DATE_DIM.D_YEAR", "TPCDS.STORE.S_STATE", "TPCDS.STORE_RETURNS.SR_FEE", "TPCDS.DATE_DIM.D_DATE_SK",
        "TPCDS.CUSTOMER.C_CUSTOMER_SK", "TPCDS.STORE.S_STORE_SK", "TPCDS.CUSTOMER.C_CUSTOMER_ID",
        "TPCDS.STORE_RETURNS.SR_CUSTOMER_SK", "TPCDS.STORE_RETURNS.SR_STORE_SK"),
      columnsFiltered = Set("TPCDS.DATE_DIM.D_YEAR", "TPCDS.STORE.S_STATE"),
      predicates = Set(),
      joins = Set(
        TestJoin(
          leftTable = "TPCDS.CUSTOMER",
          leftColumn = "TPCDS.CUSTOMER.C_CUSTOMER_SK",
          rightTable = "TPCDS.STORE_RETURNS",
          rightColumn = "TPCDS.STORE_RETURNS.SR_CUSTOMER_SK",
          joinType = TestJoinType.inner),
        TestJoin(leftTable = "TPCDS.STORE_RETURNS", rightTable = "TPCDS.STORE_RETURNS",
          leftColumn = "TPCDS.STORE_RETURNS.SR_STORE_SK",
          rightColumn = "TPCDS.STORE_RETURNS.SR_STORE_SK", joinType = TestJoinType.inner),
        TestJoin(leftTable = "TPCDS.STORE", rightTable = "TPCDS.STORE_RETURNS",
          leftColumn = "TPCDS.STORE.S_STORE_SK",
          rightColumn = "TPCDS.STORE_RETURNS.SR_STORE_SK", joinType = TestJoinType.inner),
        TestJoin(leftTable = "TPCDS.DATE_DIM", rightTable = "TPCDS.STORE_RETURNS",
          leftColumn = "TPCDS.DATE_DIM.D_DATE_SK",
          rightColumn = "TPCDS.STORE_RETURNS.SR_RETURNED_DATE_SK", joinType = TestJoinType.inner
        ))
    ),
    "q2" -> TestQueryFingerPrint(
      uuid = "q2",
      tablesReferenced = Set("TPCDS.WEB_SALES", "TPCDS.CATALOG_SALES", "TPCDS.DATE_DIM"),
      columnsScanned = Set("TPCDS.DATE_DIM.D_YEAR", "TPCDS.WEB_SALES.WS_SOLD_DATE_SK",
        "TPCDS.WEB_SALES.WS_EXT_SALES_PRICE", "TPCDS.DATE_DIM.D_WEEK_SEQ",
        "TPCDS.DATE_DIM.D_DATE_SK", "TPCDS.CATALOG_SALES.CS_SOLD_DATE_SK", "TPCDS.CATALOG_SALES.CS_EXT_SALES_PRICE"
      ),
      columnsFiltered = Set("TPCDS.DATE_DIM.D_YEAR"),
      predicates = Set(TestPredicate(column = "TPCDS.DATE_DIM.D_YEAR", operator = "equals", functionApplication = None)),
      joins = Set(
        TestJoin(leftTable = "TPCDS.DATE_DIM", rightTable = "TPCDS.DATE_DIM",
          leftColumn = "TPCDS.DATE_DIM.D_WEEK_SEQ",
          rightColumn = "TPCDS.DATE_DIM.D_WEEK_SEQ", joinType = TestJoinType.inner),
        TestJoin(leftTable = "TPCDS.DATE_DIM", rightTable = "TPCDS.WEB_SALES",
          leftColumn = "TPCDS.DATE_DIM.D_DATE_SK", rightColumn = "TPCDS.WEB_SALES.WS_SOLD_DATE_SK",
          joinType = TestJoinType.inner)
      )
    ),
    "q3" -> TestQueryFingerPrint(
      uuid = "q3",
      tablesReferenced = Set("TPCDS.STORE_SALES", "TPCDS.ITEM", "TPCDS.DATE_DIM"),
      columnsScanned = Set("TPCDS.DATE_DIM.D_YEAR", "TPCDS.ITEM.I_BRAND_ID", "TPCDS.ITEM.I_ITEM_SK",
        "TPCDS.STORE_SALES.SS_ITEM_SK", "TPCDS.STORE_SALES.SS_EXT_SALES_PRICE",
        "TPCDS.DATE_DIM.D_DATE_SK", "TPCDS.DATE_DIM.D_MOY", "TPCDS.STORE_SALES.SS_SOLD_DATE_SK",
        "TPCDS.ITEM.I_MANUFACT_ID", "TPCDS.ITEM.I_BRAND"),
      columnsFiltered = Set("TPCDS.DATE_DIM.D_MOY", "TPCDS.ITEM.I_MANUFACT_ID"),
      predicates = Set(
        TestPredicate(column = "TPCDS.ITEM.I_MANUFACT_ID", operator = "equals", functionApplication = None),
        TestPredicate(column = "TPCDS.DATE_DIM.D_MOY", operator = "equals", functionApplication = None)
      ),
      joins = Set(
        TestJoin(leftTable = "TPCDS.DATE_DIM", rightTable = "TPCDS.STORE_SALES",
          leftColumn = "TPCDS.DATE_DIM.D_DATE_SK", rightColumn = "TPCDS.STORE_SALES.SS_SOLD_DATE_SK",
          joinType = TestJoinType.inner),
        TestJoin(leftTable = "TPCDS.ITEM", rightTable = "TPCDS.STORE_SALES",
          leftColumn = "TPCDS.ITEM.I_ITEM_SK", rightColumn = "TPCDS.STORE_SALES.SS_ITEM_SK",
          joinType = TestJoinType.inner)
      )
    ),
    "q4" -> TestQueryFingerPrint(
      uuid = "q4",
      tablesReferenced = Set("TPCDS.STORE_SALES", "TPCDS.WEB_SALES", "TPCDS.CATALOG_SALES", "TPCDS.DATE_DIM", "TPCDS.CUSTOMER"),
      columnsScanned = Set("TPCDS.WEB_SALES.WS_SOLD_DATE_SK", "TPCDS.DATE_DIM.D_DATE_SK",
        "TPCDS.CATALOG_SALES.CS_BILL_CUSTOMER_SK", "TPCDS.CUSTOMER.C_BIRTH_COUNTRY",
        "TPCDS.STORE_SALES.SS_SOLD_DATE_SK", "TPCDS.CATALOG_SALES.CS_SOLD_DATE_SK",
        "TPCDS.CUSTOMER.C_LAST_NAME", "TPCDS.STORE_SALES.SS_CUSTOMER_SK",
        "TPCDS.CUSTOMER.C_FIRST_NAME", "TPCDS.DATE_DIM.D_YEAR", "TPCDS.CUSTOMER.C_LOGIN",
        "TPCDS.CUSTOMER.C_EMAIL_ADDRESS", "TPCDS.CUSTOMER.C_CUSTOMER_SK", "TPCDS.CUSTOMER.C_CUSTOMER_ID",
        "TPCDS.CUSTOMER.C_PREFERRED_CUST_FLAG", "TPCDS.WEB_SALES.WS_BILL_CUSTOMER_SK"
      ),
      columnsFiltered = Set("TPCDS.DATE_DIM.D_YEAR"),
      predicates = Set(
        TestPredicate(column = "TPCDS.DATE_DIM.D_YEAR", operator = "equals", functionApplication = None)
      ),
      joins = Set(
        TestJoin(leftTable = "TPCDS.CUSTOMER", rightTable = "TPCDS.STORE_SALES",
          leftColumn = "TPCDS.CUSTOMER.C_CUSTOMER_SK",
          rightColumn = "TPCDS.STORE_SALES.SS_CUSTOMER_SK", joinType = TestJoinType.inner),
        TestJoin(leftTable = "TPCDS.DATE_DIM", rightTable = "TPCDS.STORE_SALES",
          leftColumn = "TPCDS.DATE_DIM.D_DATE_SK", rightColumn = "TPCDS.STORE_SALES.SS_SOLD_DATE_SK",
          joinType = TestJoinType.inner),
        TestJoin(leftTable = "TPCDS.CATALOG_SALES", rightTable = "TPCDS.DATE_DIM",
          leftColumn = "TPCDS.CATALOG_SALES.CS_SOLD_DATE_SK", rightColumn = "TPCDS.DATE_DIM.D_DATE_SK",
          joinType = TestJoinType.inner),
        TestJoin(leftTable = "TPCDS.CUSTOMER", rightTable = "TPCDS.WEB_SALES",
          leftColumn = "TPCDS.CUSTOMER.C_CUSTOMER_SK",
          rightColumn = "TPCDS.WEB_SALES.WS_BILL_CUSTOMER_SK", joinType = TestJoinType.inner),
        TestJoin(leftTable = "TPCDS.DATE_DIM", rightTable = "TPCDS.WEB_SALES",
          leftColumn = "TPCDS.DATE_DIM.D_DATE_SK", rightColumn = "TPCDS.WEB_SALES.WS_SOLD_DATE_SK",
          joinType = TestJoinType.inner),
        TestJoin(leftTable = "TPCDS.CATALOG_SALES", rightTable = "TPCDS.CUSTOMER",
          leftColumn = "TPCDS.CATALOG_SALES.CS_BILL_CUSTOMER_SK",
          rightColumn = "TPCDS.CUSTOMER.C_CUSTOMER_SK", joinType = TestJoinType.inner),
        TestJoin(leftTable = "TPCDS.CUSTOMER", rightTable = "TPCDS.CUSTOMER",
          leftColumn = "TPCDS.CUSTOMER.C_CUSTOMER_ID",
          rightColumn = "TPCDS.CUSTOMER.C_CUSTOMER_ID", joinType = TestJoinType.inner)
      )
    ),
    "q6" -> TestQueryFingerPrint(
      uuid = "q6",
      tablesReferenced = Set("TPCDS.STORE_SALES", "TPCDS.ITEM", "TPCDS.DATE_DIM", "TPCDS.CUSTOMER_ADDRESS", "TPCDS.CUSTOMER"),
      columnsScanned = Set("TPCDS.DATE_DIM.D_DATE_SK", "TPCDS.CUSTOMER_ADDRESS.CA_STATE",
        "TPCDS.STORE_SALES.SS_SOLD_DATE_SK", "TPCDS.CUSTOMER.C_CURRENT_ADDR_SK", "TPCDS.CUSTOMER_ADDRESS.CA_ADDRESS_SK",
        "TPCDS.DATE_DIM.D_MONTH_SEQ", "TPCDS.STORE_SALES.SS_CUSTOMER_SK", "TPCDS.DATE_DIM.D_YEAR",
        "TPCDS.ITEM.I_ITEM_SK", "TPCDS.STORE_SALES.SS_ITEM_SK", "TPCDS.CUSTOMER.C_CUSTOMER_SK", "TPCDS.DATE_DIM.D_MOY",
        "TPCDS.ITEM.I_CURRENT_PRICE", "TPCDS.ITEM.*"),
      columnsFiltered = Set("TPCDS.DATE_DIM.D_YEAR", "TPCDS.DATE_DIM.D_MOY"),
      predicates = Set(
        TestPredicate(column = "TPCDS.DATE_DIM.D_YEAR", operator = "equals"),
        TestPredicate(column = "TPCDS.DATE_DIM.D_MOY", operator = "equals")
      ),
      joins = Set(
        TestJoin(leftTable = "TPCDS.CUSTOMER", rightTable = "TPCDS.STORE_SALES",
          leftColumn = "TPCDS.CUSTOMER.C_CUSTOMER_SK",
          rightColumn = "TPCDS.STORE_SALES.SS_CUSTOMER_SK", joinType = TestJoinType.inner),
        TestJoin(leftTable = "TPCDS.DATE_DIM", rightTable = "TPCDS.STORE_SALES",
          leftColumn = "TPCDS.DATE_DIM.D_DATE_SK",
          rightColumn = "TPCDS.STORE_SALES.SS_SOLD_DATE_SK",
          joinType = TestJoinType.inner),
        TestJoin(leftTable = "TPCDS.ITEM", rightTable = "TPCDS.STORE_SALES",
          leftColumn = "TPCDS.ITEM.I_ITEM_SK",
          rightColumn = "TPCDS.STORE_SALES.SS_ITEM_SK",
          joinType = TestJoinType.inner),
        TestJoin(leftTable = "TPCDS.CUSTOMER", rightTable = "TPCDS.CUSTOMER_ADDRESS",
          leftColumn = "TPCDS.CUSTOMER.C_CURRENT_ADDR_SK",
          rightColumn = "TPCDS.CUSTOMER_ADDRESS.CA_ADDRESS_SK",
          joinType = TestJoinType.inner)
      )
    ),
    "q7" -> TestQueryFingerPrint(
      uuid = "q7",
      tablesReferenced = Set("TPCDS.STORE_SALES", "TPCDS.CUSTOMER_DEMOGRAPHICS", "TPCDS.ITEM", "TPCDS.DATE_DIM", "TPCDS.PROMOTION"),
      columnsScanned = Set("TPCDS.STORE_SALES.SS_QUANTITY", "TPCDS.STORE_SALES.SS_CDEMO_SK", "TPCDS.DATE_DIM.D_DATE_SK",
        "TPCDS.CUSTOMER_DEMOGRAPHICS.CD_DEMO_SK", "TPCDS.STORE_SALES.SS_COUPON_AMT",
        "TPCDS.STORE_SALES.SS_SALES_PRICE", "TPCDS.STORE_SALES.SS_SOLD_DATE_SK",
        "TPCDS.CUSTOMER_DEMOGRAPHICS.CD_EDUCATION_STATUS", "TPCDS.CUSTOMER_DEMOGRAPHICS.CD_MARITAL_STATUS",
        "TPCDS.PROMOTION.P_CHANNEL_EVENT", "TPCDS.CUSTOMER_DEMOGRAPHICS.CD_GENDER",
        "TPCDS.PROMOTION.P_CHANNEL_EMAIL", "TPCDS.DATE_DIM.D_YEAR", "TPCDS.ITEM.I_ITEM_SK",
        "TPCDS.STORE_SALES.SS_ITEM_SK", "TPCDS.ITEM.I_ITEM_ID", "TPCDS.PROMOTION.P_PROMO_SK",
        "TPCDS.STORE_SALES.SS_PROMO_SK", "TPCDS.STORE_SALES.SS_LIST_PRICE"),
      columnsFiltered = Set("TPCDS.CUSTOMER_DEMOGRAPHICS.CD_GENDER", "TPCDS.PROMOTION.P_CHANNEL_EMAIL",
        "TPCDS.DATE_DIM.D_YEAR", "TPCDS.CUSTOMER_DEMOGRAPHICS.CD_EDUCATION_STATUS",
        "TPCDS.CUSTOMER_DEMOGRAPHICS.CD_MARITAL_STATUS", "TPCDS.PROMOTION.P_CHANNEL_EVENT"),
      predicates = Set(
        TestPredicate(column = "TPCDS.DATE_DIM.D_YEAR", operator = "equals"),
        TestPredicate(column = "TPCDS.CUSTOMER_DEMOGRAPHICS.CD_MARITAL_STATUS", operator = "equals"),
        TestPredicate(column = "TPCDS.CUSTOMER_DEMOGRAPHICS.CD_EDUCATION_STATUS", operator = "equals"),
        TestPredicate(column = "TPCDS.CUSTOMER_DEMOGRAPHICS.CD_GENDER", operator = "equals")
      ),
      joins = Set(
        TestJoin(leftTable = "TPCDS.CUSTOMER_DEMOGRAPHICS", rightTable = "TPCDS.STORE_SALES",
          leftColumn = "TPCDS.CUSTOMER_DEMOGRAPHICS.CD_DEMO_SK",
          rightColumn = "TPCDS.STORE_SALES.SS_CDEMO_SK",
          joinType = TestJoinType.inner),
        TestJoin(leftTable = "TPCDS.DATE_DIM", rightTable = "TPCDS.STORE_SALES",
          leftColumn = "TPCDS.DATE_DIM.D_DATE_SK",
          rightColumn = "TPCDS.STORE_SALES.SS_SOLD_DATE_SK", joinType = TestJoinType.inner),
        TestJoin(leftTable = "TPCDS.ITEM", rightTable = "TPCDS.STORE_SALES",
          leftColumn = "TPCDS.ITEM.I_ITEM_SK",
          rightColumn = "TPCDS.STORE_SALES.SS_ITEM_SK", joinType = TestJoinType.inner),
        TestJoin(leftTable = "TPCDS.PROMOTION", rightTable = "TPCDS.STORE_SALES",
          leftColumn = "TPCDS.PROMOTION.P_PROMO_SK",
          rightColumn = "TPCDS.STORE_SALES.SS_PROMO_SK", joinType = TestJoinType.inner)
      )
    ),
    "q10" -> TestQueryFingerPrint(
      uuid = "q10",
      tablesReferenced = Set("TPCDS.CUSTOMER_DEMOGRAPHICS", "TPCDS.STORE_SALES", "TPCDS.DATE_DIM", "TPCDS.CUSTOMER_ADDRESS", "TPCDS.CUSTOMER"),
      columnsScanned = Set("TPCDS.CUSTOMER_DEMOGRAPHICS.CD_DEP_COLLEGE_COUNT",
        "TPCDS.CUSTOMER_DEMOGRAPHICS.CD_DEP_EMPLOYED_COUNT", "TPCDS.CUSTOMER_DEMOGRAPHICS.CD_DEMO_SK",
        "TPCDS.DATE_DIM.D_DATE_SK", "TPCDS.CUSTOMER_DEMOGRAPHICS.CD_CREDIT_RATING", "TPCDS.STORE_SALES.SS_SOLD_DATE_SK",
        "TPCDS.CUSTOMER.C_CURRENT_ADDR_SK", "TPCDS.CUSTOMER_DEMOGRAPHICS.CD_EDUCATION_STATUS",
        "TPCDS.CUSTOMER_ADDRESS.CA_ADDRESS_SK", "TPCDS.CUSTOMER.C_CURRENT_CDEMO_SK",
        "TPCDS.CUSTOMER_DEMOGRAPHICS.CD_MARITAL_STATUS", "TPCDS.CUSTOMER_DEMOGRAPHICS.CD_GENDER",
        "TPCDS.DATE_DIM.D_YEAR", "TPCDS.CUSTOMER.C_CUSTOMER_SK", "TPCDS.CUSTOMER_DEMOGRAPHICS.CD_DEP_COUNT",
        "TPCDS.CUSTOMER_ADDRESS.CA_COUNTY", "TPCDS.CUSTOMER_DEMOGRAPHICS.CD_PURCHASE_ESTIMATE",
        "TPCDS.CUSTOMER_DEMOGRAPHICS.*"),
      columnsFiltered = Set("TPCDS.DATE_DIM.D_YEAR", "TPCDS.CUSTOMER_ADDRESS.CA_COUNTY"),
      predicates = Set(
        TestPredicate(column = "TPCDS.DATE_DIM.D_YEAR", operator = "equals"),
        TestPredicate(column = "TPCDS.CUSTOMER_ADDRESS.CA_COUNTY", operator = "in")
      ),
      joins = Set(
        TestJoin(leftTable = "TPCDS.CUSTOMER", rightTable = "TPCDS.STORE_SALES",
          leftColumn = "TPCDS.CUSTOMER.C_CUSTOMER_SK",
          rightColumn = "TPCDS.STORE_SALES.SS_CUSTOMER_SK", joinType = TestJoinType.inner),
        TestJoin(leftTable = "TPCDS.DATE_DIM", rightTable = "TPCDS.STORE_SALES",
          leftColumn = "TPCDS.DATE_DIM.D_DATE_SK",
          rightColumn = "TPCDS.STORE_SALES.SS_SOLD_DATE_SK", joinType = TestJoinType.inner),
        TestJoin(leftTable = "TPCDS.CUSTOMER", rightTable = "TPCDS.CUSTOMER_DEMOGRAPHICS",
          leftColumn = "TPCDS.CUSTOMER.C_CURRENT_CDEMO_SK",
          rightColumn = "TPCDS.CUSTOMER_DEMOGRAPHICS.CD_DEMO_SK", joinType = TestJoinType.inner),
        TestJoin(leftTable = "TPCDS.CUSTOMER", rightTable = "TPCDS.CUSTOMER_ADDRESS",
          leftColumn = "TPCDS.CUSTOMER.C_CURRENT_ADDR_SK",
          rightColumn = "TPCDS.CUSTOMER_ADDRESS.CA_ADDRESS_SK", joinType = TestJoinType.inner)
      )
    ),
    "q11" -> TestQueryFingerPrint(
      uuid = "q11",
      tablesReferenced = Set("TPCDS.STORE_SALES", "TPCDS.WEB_SALES", "TPCDS.DATE_DIM", "TPCDS.CUSTOMER"),
      columnsScanned = Set("TPCDS.WEB_SALES.WS_SOLD_DATE_SK", "TPCDS.DATE_DIM.D_DATE_SK",
        "TPCDS.CUSTOMER.C_BIRTH_COUNTRY", "TPCDS.STORE_SALES.SS_SOLD_DATE_SK", "TPCDS.CUSTOMER.C_LAST_NAME",
        "TPCDS.STORE_SALES.SS_CUSTOMER_SK", "TPCDS.CUSTOMER.C_FIRST_NAME", "TPCDS.DATE_DIM.D_YEAR",
        "TPCDS.CUSTOMER.C_LOGIN", "TPCDS.CUSTOMER.C_EMAIL_ADDRESS", "TPCDS.CUSTOMER.C_CUSTOMER_SK",
        "TPCDS.CUSTOMER.C_CUSTOMER_ID", "TPCDS.CUSTOMER.C_PREFERRED_CUST_FLAG", "TPCDS.WEB_SALES.WS_BILL_CUSTOMER_SK"),
      columnsFiltered = Set("TPCDS.DATE_DIM.D_YEAR"),
      predicates = Set(
        TestPredicate(column = "TPCDS.DATE_DIM.D_YEAR", operator = "equals")
      ),
      joins = Set(
        TestJoin(leftTable = "TPCDS.CUSTOMER", rightTable = "TPCDS.STORE_SALES",
          leftColumn = "TPCDS.CUSTOMER.C_CUSTOMER_SK",
          rightColumn = "TPCDS.STORE_SALES.SS_CUSTOMER_SK", joinType = TestJoinType.inner),
        TestJoin(leftTable = "TPCDS.DATE_DIM", rightTable = "TPCDS.STORE_SALES",
          leftColumn = "TPCDS.DATE_DIM.D_DATE_SK",
          rightColumn = "TPCDS.STORE_SALES.SS_SOLD_DATE_SK", joinType = TestJoinType.inner),
        TestJoin(leftTable = "TPCDS.CUSTOMER", rightTable = "TPCDS.WEB_SALES",
          leftColumn = "TPCDS.CUSTOMER.C_CUSTOMER_SK",
          rightColumn = "TPCDS.WEB_SALES.WS_BILL_CUSTOMER_SK", joinType = TestJoinType.inner),
        TestJoin(leftTable = "TPCDS.DATE_DIM", rightTable = "TPCDS.WEB_SALES",
          leftColumn = "TPCDS.DATE_DIM.D_DATE_SK", rightColumn = "TPCDS.WEB_SALES.WS_SOLD_DATE_SK",
          joinType = TestJoinType.inner),
        TestJoin(leftTable = "TPCDS.CUSTOMER", rightTable = "TPCDS.CUSTOMER",
          leftColumn = "TPCDS.CUSTOMER.C_CUSTOMER_ID",
          rightColumn = "TPCDS.CUSTOMER.C_CUSTOMER_ID", joinType = TestJoinType.inner)
      )
    ),
    "q13" -> TestQueryFingerPrint(
      uuid = "q13",
      tablesReferenced = Set("TPCDS.STORE_SALES", "TPCDS.CUSTOMER_DEMOGRAPHICS", "TPCDS.HOUSEHOLD_DEMOGRAPHICS", "TPCDS.DATE_DIM", "TPCDS.CUSTOMER_ADDRESS", "TPCDS.STORE"),
      columnsScanned = Set("TPCDS.STORE_SALES.SS_QUANTITY", "TPCDS.STORE_SALES.SS_CDEMO_SK", "TPCDS.DATE_DIM.D_DATE_SK", "TPCDS.CUSTOMER_DEMOGRAPHICS.CD_DEMO_SK", "TPCDS.HOUSEHOLD_DEMOGRAPHICS.HD_DEP_COUNT", "TPCDS.CUSTOMER_ADDRESS.CA_STATE", "TPCDS.STORE_SALES.SS_SOLD_DATE_SK", "TPCDS.CUSTOMER_DEMOGRAPHICS.CD_EDUCATION_STATUS", "TPCDS.CUSTOMER_ADDRESS.CA_ADDRESS_SK", "TPCDS.CUSTOMER_DEMOGRAPHICS.CD_MARITAL_STATUS", "TPCDS.DATE_DIM.D_YEAR", "TPCDS.STORE_SALES.SS_HDEMO_SK", "TPCDS.HOUSEHOLD_DEMOGRAPHICS.HD_DEMO_SK", "TPCDS.CUSTOMER_ADDRESS.CA_COUNTRY", "TPCDS.STORE_SALES.SS_EXT_SALES_PRICE", "TPCDS.STORE_SALES.SS_EXT_WHOLESALE_COST", "TPCDS.STORE.S_STORE_SK", "TPCDS.STORE_SALES.SS_STORE_SK", "TPCDS.STORE_SALES.SS_ADDR_SK"),
      columnsFiltered = Set("TPCDS.DATE_DIM.D_YEAR", "TPCDS.CUSTOMER_ADDRESS.CA_COUNTRY", "TPCDS.HOUSEHOLD_DEMOGRAPHICS.HD_DEP_COUNT", "TPCDS.CUSTOMER_ADDRESS.CA_STATE", "TPCDS.CUSTOMER_DEMOGRAPHICS.CD_EDUCATION_STATUS", "TPCDS.CUSTOMER_DEMOGRAPHICS.CD_MARITAL_STATUS"),
      predicates = Set(
        TestPredicate(column = "TPCDS.DATE_DIM.D_YEAR", operator = "equals")
      ),
      joins = Set(
        TestJoin(leftTable = "TPCDS.STORE", rightTable = "TPCDS.STORE_SALES",
          leftColumn = "TPCDS.STORE.S_STORE_SK", rightColumn = "TPCDS.STORE_SALES.SS_STORE_SK", joinType = TestJoinType.inner),
        TestJoin(leftTable = "TPCDS.CUSTOMER_DEMOGRAPHICS", rightTable = "TPCDS.STORE_SALES", leftColumn = "TPCDS.CUSTOMER_DEMOGRAPHICS.CD_DEMO_SK", rightColumn = "TPCDS.STORE_SALES.SS_CDEMO_SK", joinType = TestJoinType.inner), TestJoin(leftTable = "TPCDS.HOUSEHOLD_DEMOGRAPHICS", rightTable = "TPCDS.STORE_SALES", leftColumn = "TPCDS.HOUSEHOLD_DEMOGRAPHICS.HD_DEMO_SK", rightColumn = "TPCDS.STORE_SALES.SS_HDEMO_SK", joinType = TestJoinType.inner), TestJoin(leftTable = "TPCDS.DATE_DIM", rightTable = "TPCDS.STORE_SALES", leftColumn = "TPCDS.DATE_DIM.D_DATE_SK", rightColumn = "TPCDS.STORE_SALES.SS_SOLD_DATE_SK", joinType = TestJoinType.inner), TestJoin(leftTable = "TPCDS.CUSTOMER_ADDRESS", rightTable = "TPCDS.STORE_SALES", leftColumn = "TPCDS.CUSTOMER_ADDRESS.CA_ADDRESS_SK", rightColumn = "TPCDS.STORE_SALES.SS_ADDR_SK", joinType = TestJoinType.inner)
      )
    ),
    "q15" -> TestQueryFingerPrint(
      uuid = "q15",
      tablesReferenced = Set("TPCDS.CATALOG_SALES", "TPCDS.DATE_DIM", "TPCDS.CUSTOMER_ADDRESS", "TPCDS.CUSTOMER"),
      columnsScanned = Set("TPCDS.DATE_DIM.D_YEAR", "TPCDS.CUSTOMER_ADDRESS.CA_ZIP", "TPCDS.DATE_DIM.D_DATE_SK", "TPCDS.CUSTOMER.C_CUSTOMER_SK", "TPCDS.CATALOG_SALES.CS_BILL_CUSTOMER_SK", "TPCDS.CUSTOMER_ADDRESS.CA_STATE", "TPCDS.DATE_DIM.D_QOY", "TPCDS.CATALOG_SALES.CS_SOLD_DATE_SK", "TPCDS.CUSTOMER.C_CURRENT_ADDR_SK", "TPCDS.CUSTOMER_ADDRESS.CA_ADDRESS_SK", "TPCDS.CATALOG_SALES.CS_SALES_PRICE"),
      columnsFiltered = Set("TPCDS.DATE_DIM.D_YEAR", "TPCDS.CUSTOMER_ADDRESS.CA_ZIP", "TPCDS.CUSTOMER_ADDRESS.CA_STATE", "TPCDS.DATE_DIM.D_QOY", "TPCDS.CATALOG_SALES.CS_SALES_PRICE"),
      predicates = Set(
        TestPredicate(column = "TPCDS.DATE_DIM.D_QOY", operator = "equals"), TestPredicate(column = "TPCDS.DATE_DIM.D_YEAR", operator = "equals")
      ),
      joins = Set(
        TestJoin(leftTable = "TPCDS.CATALOG_SALES", rightTable = "TPCDS.DATE_DIM", leftColumn = "TPCDS.CATALOG_SALES.CS_SOLD_DATE_SK", rightColumn = "TPCDS.DATE_DIM.D_DATE_SK", joinType = TestJoinType.inner), TestJoin(leftTable = "TPCDS.CATALOG_SALES", rightTable = "TPCDS.CUSTOMER", leftColumn = "TPCDS.CATALOG_SALES.CS_BILL_CUSTOMER_SK", rightColumn = "TPCDS.CUSTOMER.C_CUSTOMER_SK", joinType = TestJoinType.inner), TestJoin(leftTable = "TPCDS.CUSTOMER", rightTable = "TPCDS.CUSTOMER_ADDRESS", leftColumn = "TPCDS.CUSTOMER.C_CURRENT_ADDR_SK", rightColumn = "TPCDS.CUSTOMER_ADDRESS.CA_ADDRESS_SK", joinType = TestJoinType.inner)
      )
    ),
    "q17" -> TestQueryFingerPrint(
      uuid = "q17",
      tablesReferenced = Set(),
      columnsScanned = Set(),
      columnsFiltered = Set(),
      predicates = Set(
        TestPredicate(column = "TPCDS.DATE_DIM.D_QUARTER_NAME", operator = "in"),
        TestPredicate(column = "TPCDS.DATE_DIM.D_QUARTER_NAME", operator = "equals")
      ),
      joins = Set(
        TestJoin(leftTable = "TPCDS.STORE", rightTable = "TPCDS.STORE_SALES", leftColumn = "TPCDS.STORE.S_STORE_SK", rightColumn = "TPCDS.STORE_SALES.SS_STORE_SK", joinType = TestJoinType.inner), TestJoin(leftTable = "TPCDS.DATE_DIM", rightTable = "TPCDS.STORE_SALES", leftColumn = "TPCDS.DATE_DIM.D_DATE_SK", rightColumn = "TPCDS.STORE_SALES.SS_SOLD_DATE_SK", joinType = TestJoinType.inner), TestJoin(leftTable = "TPCDS.STORE_RETURNS", rightTable = "TPCDS.STORE_SALES", leftColumn = "TPCDS.STORE_RETURNS.SR_CUSTOMER_SK", rightColumn = "TPCDS.STORE_SALES.SS_CUSTOMER_SK", joinType = TestJoinType.inner), TestJoin(leftTable = "TPCDS.CATALOG_SALES", rightTable = "TPCDS.DATE_DIM", leftColumn = "TPCDS.CATALOG_SALES.CS_SOLD_DATE_SK", rightColumn = "TPCDS.DATE_DIM.D_DATE_SK", joinType = TestJoinType.inner), TestJoin(leftTable = "TPCDS.STORE_RETURNS", rightTable = "TPCDS.STORE_SALES", leftColumn = "TPCDS.STORE_RETURNS.SR_TICKET_NUMBER", rightColumn = "TPCDS.STORE_SALES.SS_TICKET_NUMBER", joinType = TestJoinType.inner), TestJoin(leftTable = "TPCDS.ITEM", rightTable = "TPCDS.STORE_SALES", leftColumn = "TPCDS.ITEM.I_ITEM_SK", rightColumn = "TPCDS.STORE_SALES.SS_ITEM_SK", joinType = TestJoinType.inner), TestJoin(leftTable = "TPCDS.CATALOG_SALES", rightTable = "TPCDS.STORE_RETURNS", leftColumn = "TPCDS.CATALOG_SALES.CS_BILL_CUSTOMER_SK", rightColumn = "TPCDS.STORE_RETURNS.SR_CUSTOMER_SK", joinType = TestJoinType.inner), TestJoin(leftTable = "TPCDS.STORE_RETURNS", rightTable = "TPCDS.STORE_SALES", leftColumn = "TPCDS.STORE_RETURNS.SR_ITEM_SK", rightColumn = "TPCDS.STORE_SALES.SS_ITEM_SK", joinType = TestJoinType.inner), TestJoin(leftTable = "TPCDS.CATALOG_SALES", rightTable = "TPCDS.STORE_RETURNS", leftColumn = "TPCDS.CATALOG_SALES.CS_ITEM_SK", rightColumn = "TPCDS.STORE_RETURNS.SR_ITEM_SK", joinType = TestJoinType.inner), TestJoin(leftTable = "TPCDS.DATE_DIM", rightTable = "TPCDS.STORE_RETURNS", leftColumn = "TPCDS.DATE_DIM.D_DATE_SK", rightColumn = "TPCDS.STORE_RETURNS.SR_RETURNED_DATE_SK", joinType = TestJoinType.inner)
      )
    ),
    "q18" -> TestQueryFingerPrint(
      uuid = "q18",
      tablesReferenced = Set("TPCDS.STORE_SALES", "TPCDS.ITEM", "TPCDS.CATALOG_SALES", "TPCDS.STORE_RETURNS", "TPCDS.DATE_DIM", "TPCDS.STORE"),
      columnsScanned = Set("TPCDS.STORE_SALES.SS_QUANTITY", "TPCDS.DATE_DIM.D_DATE_SK", "TPCDS.STORE_RETURNS.SR_ITEM_SK", "TPCDS.CATALOG_SALES.CS_BILL_CUSTOMER_SK", "TPCDS.STORE_SALES.SS_SOLD_DATE_SK", "TPCDS.CATALOG_SALES.CS_SOLD_DATE_SK", "TPCDS.STORE_RETURNS.SR_CUSTOMER_SK", "TPCDS.STORE_SALES.SS_CUSTOMER_SK", "TPCDS.CATALOG_SALES.CS_ITEM_SK", "TPCDS.STORE_RETURNS.SR_RETURNED_DATE_SK", "TPCDS.ITEM.I_ITEM_SK", "TPCDS.STORE.S_STATE", "TPCDS.STORE_SALES.SS_ITEM_SK", "TPCDS.STORE_RETURNS.SR_TICKET_NUMBER", "TPCDS.CATALOG_SALES.CS_QUANTITY", "TPCDS.ITEM.I_ITEM_ID", "TPCDS.DATE_DIM.D_QUARTER_NAME", "TPCDS.STORE.S_STORE_SK", "TPCDS.STORE_SALES.SS_TICKET_NUMBER", "TPCDS.ITEM.I_ITEM_DESC", "TPCDS.STORE_SALES.SS_STORE_SK", "TPCDS.STORE_RETURNS.SR_RETURN_QUANTITY"),
      columnsFiltered = Set("TPCDS.DATE_DIM.D_QUARTER_NAME"),
      predicates = Set(
        TestPredicate(column = "TPCDS.DATE_DIM.D_YEAR", operator = "equals"),
        TestPredicate(column = "TPCDS.CUSTOMER_DEMOGRAPHICS.CD_EDUCATION_STATUS", operator = "equals"),
        TestPredicate(column = "TPCDS.CUSTOMER_DEMOGRAPHICS.CD_GENDER", operator = "equals"),
        TestPredicate(column = "TPCDS.CUSTOMER.C_BIRTH_MONTH", operator = "in"),
        TestPredicate(column = "TPCDS.CUSTOMER_ADDRESS.CA_STATE", operator = "in")
      ),
      joins = Set(
        TestJoin(leftTable = "TPCDS.CATALOG_SALES", rightTable = "TPCDS.DATE_DIM", leftColumn = "TPCDS.CATALOG_SALES.CS_SOLD_DATE_SK", rightColumn = "TPCDS.DATE_DIM.D_DATE_SK", joinType = TestJoinType.inner),
        TestJoin(leftTable = "TPCDS.CUSTOMER", rightTable = "TPCDS.CUSTOMER_DEMOGRAPHICS", leftColumn = "TPCDS.CUSTOMER.C_CURRENT_CDEMO_SK", rightColumn = "TPCDS.CUSTOMER_DEMOGRAPHICS.CD_DEMO_SK", joinType = TestJoinType.inner), TestJoin(leftTable = "TPCDS.CATALOG_SALES", rightTable = "TPCDS.ITEM", leftColumn = "TPCDS.CATALOG_SALES.CS_ITEM_SK", rightColumn = "TPCDS.ITEM.I_ITEM_SK", joinType = TestJoinType.inner), TestJoin(leftTable = "TPCDS.CATALOG_SALES", rightTable = "TPCDS.CUSTOMER", leftColumn = "TPCDS.CATALOG_SALES.CS_BILL_CUSTOMER_SK", rightColumn = "TPCDS.CUSTOMER.C_CUSTOMER_SK", joinType = TestJoinType.inner), TestJoin(leftTable = "TPCDS.CATALOG_SALES", rightTable = "TPCDS.CUSTOMER_DEMOGRAPHICS", leftColumn = "TPCDS.CATALOG_SALES.CS_BILL_CDEMO_SK", rightColumn = "TPCDS.CUSTOMER_DEMOGRAPHICS.CD_DEMO_SK", joinType = TestJoinType.inner), TestJoin(leftTable = "TPCDS.CUSTOMER", rightTable = "TPCDS.CUSTOMER_ADDRESS", leftColumn = "TPCDS.CUSTOMER.C_CURRENT_ADDR_SK", rightColumn = "TPCDS.CUSTOMER_ADDRESS.CA_ADDRESS_SK", joinType = TestJoinType.inner)
      )
    ),
    "q19" -> TestQueryFingerPrint(
      uuid = "q19",
      tablesReferenced = Set("TPCDS.CUSTOMER_DEMOGRAPHICS", "TPCDS.ITEM", "TPCDS.CATALOG_SALES", "TPCDS.DATE_DIM", "TPCDS.CUSTOMER_ADDRESS", "TPCDS.CUSTOMER"),
      columnsScanned = Set("TPCDS.DATE_DIM.D_DATE_SK", "TPCDS.CUSTOMER_DEMOGRAPHICS.CD_DEMO_SK", "TPCDS.CATALOG_SALES.CS_BILL_CUSTOMER_SK", "TPCDS.CATALOG_SALES.CS_BILL_CDEMO_SK", "TPCDS.CUSTOMER_ADDRESS.CA_STATE", "TPCDS.CATALOG_SALES.CS_SOLD_DATE_SK", "TPCDS.CUSTOMER_DEMOGRAPHICS.CD_EDUCATION_STATUS", "TPCDS.CUSTOMER.C_CURRENT_ADDR_SK", "TPCDS.CUSTOMER_ADDRESS.CA_ADDRESS_SK", "TPCDS.CATALOG_SALES.CS_ITEM_SK", "TPCDS.CUSTOMER.C_CURRENT_CDEMO_SK", "TPCDS.CUSTOMER_DEMOGRAPHICS.CD_GENDER", "TPCDS.DATE_DIM.D_YEAR", "TPCDS.ITEM.I_ITEM_SK", "TPCDS.CUSTOMER_ADDRESS.CA_COUNTRY", "TPCDS.ITEM.I_ITEM_ID", "TPCDS.CUSTOMER.C_CUSTOMER_SK", "TPCDS.CUSTOMER_ADDRESS.CA_COUNTY", "TPCDS.CUSTOMER.C_BIRTH_MONTH"),
      columnsFiltered = Set("TPCDS.CUSTOMER_DEMOGRAPHICS.CD_GENDER", "TPCDS.DATE_DIM.D_YEAR", "TPCDS.CUSTOMER_ADDRESS.CA_STATE", "TPCDS.CUSTOMER_DEMOGRAPHICS.CD_EDUCATION_STATUS", "TPCDS.CUSTOMER.C_BIRTH_MONTH"),
      predicates = Set(
        TestPredicate(column = "TPCDS.ITEM.I_MANAGER_ID", operator = "equals"),
        TestPredicate(column = "TPCDS.DATE_DIM.D_YEAR", operator = "equals"),
        TestPredicate(column = "TPCDS.DATE_DIM.D_MOY", operator = "equals")
      ),
      joins = Set(
        TestJoin(leftTable = "TPCDS.CUSTOMER", rightTable = "TPCDS.STORE_SALES", leftColumn = "TPCDS.CUSTOMER.C_CUSTOMER_SK", rightColumn = "TPCDS.STORE_SALES.SS_CUSTOMER_SK", joinType = TestJoinType.inner), TestJoin(leftTable = "TPCDS.STORE", rightTable = "TPCDS.STORE_SALES", leftColumn = "TPCDS.STORE.S_STORE_SK", rightColumn = "TPCDS.STORE_SALES.SS_STORE_SK", joinType = TestJoinType.inner), TestJoin(leftTable = "TPCDS.DATE_DIM", rightTable = "TPCDS.STORE_SALES", leftColumn = "TPCDS.DATE_DIM.D_DATE_SK", rightColumn = "TPCDS.STORE_SALES.SS_SOLD_DATE_SK", joinType = TestJoinType.inner), TestJoin(leftTable = "TPCDS.ITEM", rightTable = "TPCDS.STORE_SALES", leftColumn = "TPCDS.ITEM.I_ITEM_SK", rightColumn = "TPCDS.STORE_SALES.SS_ITEM_SK", joinType = TestJoinType.inner), TestJoin(leftTable = "TPCDS.CUSTOMER", rightTable = "TPCDS.CUSTOMER_ADDRESS", leftColumn = "TPCDS.CUSTOMER.C_CURRENT_ADDR_SK", rightColumn = "TPCDS.CUSTOMER_ADDRESS.CA_ADDRESS_SK", joinType = TestJoinType.inner)
      )
    )
  )
}

object Main extends App {
  implicit val f = TestQueryFingerPrint.jsonFormat

  val j1 = JsonUtils.asJson(TestJoinType.inner)


  println(JsonUtils.fromJson[TestJoinType.Value](j1))

  println(JsonUtils.asJson(TestPredicate("A", "=", Some("SUBSTR")))(TestQueryFingerPrint.jsonFormat))

  println(JsonUtils.asJson(TestJoin(leftTable = "TPCDS.DATE_DIM", rightTable = "TPCDS.STORE_RETURNS",
    leftColumn = "TPCDS.DATE_DIM.D_DATE_SK",
    rightColumn = "TPCDS.STORE_RETURNS.SR_RETURNED_DATE_SK", joinType = TestJoinType.inner)
  )(TestQueryFingerPrint.jsonFormat))

  val json = JsonUtils.asJson(TestQueryFingerPrint.tpcdsQFP("q1"))(TestQueryFingerPrint.jsonFormat)

  println(json)


  val qfp = JsonUtils.fromJson[TestQueryFingerPrint](json)

  println(qfp)

  val x = JsonUtils.asJson(qfp.featureVector)

  println(x)
}