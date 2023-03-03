package org.hatke.queryfingerprint.queryhistory

import com.google.common.collect.ImmutableSet
import com.sksamuel.elastic4s.requests.mappings.MappingDefinition
import com.sksamuel.elastic4s.{Hit, HitReader, Indexable}
import org.hatke.queryfingerprint.json.JsonUtils
import org.hatke.queryfingerprint.model.{FunctionApplication, Join, JoinType, Predicate, QBType, Queryfingerprint => QFP}
import org.hatke.queryfingerprint.queryhistory.IndexableElem.{FuncAppIdxElem, JoinIdxElem, PredicateIdxElem}
import org.hatke.queryfingerprint.queryhistory.Utils.{asJava, asScala}
import org.json4s.jackson.Serialization
import org.json4s.{CustomSerializer, Formats, JString, ShortTypeHints}

import java.util.UUID
import scala.util.Try

object QueryFingerprint {

  private val SOURCE_FIELD = "qfp_source"
  private val FEATURE_VECTOR_DIM = 5

  implicit object TQFPIndexable extends Indexable[QFP] {

    override def json(qfp: QFP): String = {
      import org.json4s.DefaultWriters._
      import org.json4s.JsonDSL._
      import org.json4s.jackson.JsonMethods._

      import scala.jdk.CollectionConverters._

      val json = ("tablesReferenced" -> qfp.getTablesReferenced.asScala) ~
        ("columnsScanned" -> qfp.getColumnsScanned.asScala) ~
        ("columnsFiltered" -> qfp.getColumnsFiltered.asScala) ~
        ("predicates" -> qfp.getPredicates.asScala.flatMap(_.indexElements)) ~
        ("joins" -> qfp.getJoins.asScala.flatMap(_.indexElements)) ~
        ("functionApplications" -> qfp.getFunctionApplications.asScala.flatMap(_.indexElements)) ~
        ("groupedColumns" -> qfp.getGroupedColumns.asScala) ~
        ("orderedColumns" -> qfp.getOrderedColumns.asScala) ~
        ("id" -> qfp.getHash.toString) ~
        ("featureVector" -> asJValue(qfp.getFeatureVector)) ~
        (SOURCE_FIELD -> JsonUtils.asJson(QFPJsonConvert.fromQFP(qfp))(jsonFormat))

      compact(render(json))
    }
  }

  implicit object TQFPHitReader extends HitReader[QFP] {
    override def read(hit: Hit): Try[QFP] = {
      Try {
        val qfpI = JsonUtils.fromJson[QFPJsonConvert.QFPIndex](hit.sourceField(SOURCE_FIELD).asInstanceOf[String])
        QFPJsonConvert.toQFP(qfpI)
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
      textField("id").index(false),
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

  object JoinTypeSerializer extends CustomSerializer[JoinType](
    format => ( {
      case x => JoinType.valueOf(x.asInstanceOf[JString].s)
    }, {
      case jt: JoinType => JString(jt.name())
    }
    )
  )

  private object QFPJsonConvert {

    import scala.jdk.CollectionConverters._

    case class PredIndex(functionName: Option[String], column: String, operator: String, constantValue : Option[String])

    case class FuncAppIndex(functionName: String, column: String)

    case class JoinIndex(leftTable: String, rightTable: String, leftColumn: String, rightColumn: String, joinType: String)

    case class QFPIndex(sqlText: String,
                        isCTE: Boolean,
                        qbType: String,
                        tablesReferenced: Set[String],
                        columnsScanned: Set[String],
                        columnsFiltered: Set[String],
                        columnsScanFiltered: Set[String],
                        predicates: Set[PredIndex],
                        scanPredicates: Set[PredIndex],
                        functionApplications: Set[FuncAppIndex],
                        joins: Set[JoinIndex],
                        correlatedColumns: Set[String],
                        referencedQBlocks: Set[UUID],
                        columnsGroupBy: Set[String],
                        columnsOrderBy: Set[String],
                        hash: UUID,
                        parentQB : Option[UUID]
                       )

    def fromPred(p: Predicate): PredIndex =
      PredIndex(Utils.asScala(p.getFunctionName), p.getColumn, p.getOperator, Utils.asScala(p.getConstantValue))

    def toPred(pI: PredIndex): Predicate =
      new Predicate(asJava(pI.functionName), pI.column, pI.operator, asJava(pI.constantValue))

    def fromFA(fA: FunctionApplication): FuncAppIndex =
      FuncAppIndex(fA.getFunctionName, fA.getColumn)

    def toFA(fA: FuncAppIndex): FunctionApplication =
      new FunctionApplication(fA.functionName, fA.column)

    def fromJoin(j: Join): JoinIndex =
      JoinIndex(j.getLeftTable, j.getRightTable, j.getLeftColumn, j.getRightColumn, j.getType.name())

    def toJoin(j: JoinIndex): Join =
      new Join(j.leftTable, j.rightTable, j.leftColumn, j.rightColumn, JoinType.valueOf(j.joinType))

    def fromQFP(qfp: QFP): QFPIndex =
      QFPIndex(
        qfp.getSqlText,
        qfp.isCTE,
        qfp.getType.name(),
        qfp.getTablesReferenced.asScala.toSet,
        qfp.getColumnsScanned.asScala.toSet,
        qfp.getColumnsFiltered.asScala.toSet,
        qfp.getColumnsScanFiltered.asScala.toSet,
        qfp.getPredicates.asScala.map(fromPred).toSet,
        qfp.getScanPredicates.asScala.map(fromPred).toSet,
        qfp.getFunctionApplications.asScala.map(fromFA).toSet,
        qfp.getJoins.asScala.map(fromJoin).toSet,
        qfp.getCorrelatedColumns.asScala.toSet,
        qfp.getReferencedQBlocks.asScala.toSet,
        qfp.getGroupedColumns.asScala.toSet,
        qfp.getOrderedColumns.asScala.toSet,
        qfp.getHash,
        asScala(qfp.getParentQB)
      )

    def toQFP(qfI: QFPIndex): QFP =
      new QFP(
        qfI.sqlText,
        qfI.isCTE,
        QBType.valueOf(qfI.qbType),
        ImmutableSet.copyOf(qfI.tablesReferenced.asJava),
        ImmutableSet.copyOf(qfI.columnsScanned.asJava),
        ImmutableSet.copyOf(qfI.columnsFiltered.asJava),
        ImmutableSet.copyOf(qfI.columnsScanFiltered.asJava),
        ImmutableSet.copyOf(qfI.predicates.map(toPred).asJava),
        ImmutableSet.copyOf(qfI.scanPredicates.map(toPred).asJava),
        ImmutableSet.copyOf(qfI.functionApplications.map(toFA).asJava),
        ImmutableSet.copyOf(qfI.joins.map(toJoin).asJava),
        ImmutableSet.copyOf(qfI.correlatedColumns.asJava),
        ImmutableSet.copyOf(qfI.referencedQBlocks.asJava),
        ImmutableSet.copyOf(qfI.columnsGroupBy.asJava),
        ImmutableSet.copyOf(qfI.columnsOrderBy.asJava),
        qfI.hash,
        asJava(qfI.parentQB)
      )

    val INDEXINFO_CLASSES = List(
      classOf[PredIndex],
      classOf[FuncAppIndex],
      classOf[JoinIndex],
      classOf[QFPIndex]
    )

  }

  implicit val jsonFormat: Formats = Serialization.formats(ShortTypeHints(QFPJsonConvert.INDEXINFO_CLASSES)) ++
    org.json4s.ext.JodaTimeSerializers.all +
    JoinTypeSerializer +
    UUIDTypeSerializer

}
