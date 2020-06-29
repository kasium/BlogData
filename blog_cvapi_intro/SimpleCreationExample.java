import java.util.Arrays;
import java.util.List;

import com.sap.hana.cvapi.basemodel.base.Cardinality;
import com.sap.hana.cvapi.basemodel.base.JoinType;
import com.sap.hana.cvapi.basemodel.base.PrivilegeType;
import com.sap.hana.cvapi.bimodel.calculcation.AttributeMapping;
import com.sap.hana.cvapi.bimodel.calculcation.CalculationScenario;
import com.sap.hana.cvapi.bimodel.calculcation.CalculationViewType;
import com.sap.hana.cvapi.bimodel.calculcation.CalculationViews;
import com.sap.hana.cvapi.bimodel.calculcation.DataSource;
import com.sap.hana.cvapi.bimodel.calculcation.DataSources;
import com.sap.hana.cvapi.bimodel.calculcation.Input;
import com.sap.hana.cvapi.bimodel.calculcation.JoinAttribute;
import com.sap.hana.cvapi.bimodel.calculcation.JoinView;
import com.sap.hana.cvapi.bimodel.calculcation.ProjectionView;
import com.sap.hana.cvapi.bimodel.calculcation.ScenarioRoot;
import com.sap.hana.cvapi.bimodel.calculcation.ViewAttribute;
import com.sap.hana.cvapi.bimodel.calculcation.ViewAttributes;
import com.sap.hana.cvapi.bimodel.cube.AggregationType;
import com.sap.hana.cvapi.bimodel.cube.BaseMeasure;
import com.sap.hana.cvapi.bimodel.cube.BaseMeasures;
import com.sap.hana.cvapi.bimodel.cube.MeasureGroup;
import com.sap.hana.cvapi.bimodel.cube.MeasureType;
import com.sap.hana.cvapi.bimodel.dataFoundation.Attribute;
import com.sap.hana.cvapi.bimodel.dataFoundation.Attributes;
import com.sap.hana.cvapi.bimodel.dataFoundation.CalculatedAttributes;
import com.sap.hana.cvapi.bimodel.dataFoundation.ColumnMapping;
import com.sap.hana.cvapi.datamodel.entity.DataCategory;
import com.sap.hana.cvapi.datamodel.type.SemanticType;

public class SimpleCreationExample {
	private static final String JOIN_PRODCT_ID = "JOIN$ID$PRODUCTID";
	private static final String SALES_TABLE = "SALES1";
	private static final String PRODUCT_TABLE = "PRODUCT";
	private static final String ID_COLUMN = "ID";
	private static final String PRICE_COLUMN = "PRICE";
	private static final String AMOUNT_COLUMN = "AMOUNT";
	private static final String DETAIL_COLUMN = "DETAIL";
	private static final String PRODUCTID_COLUMN = "PRODUCTID";
	private static final String PRODUCTNAME_COLUMN = "PRODUCTNAME";

	private static final String JOIN_VIEW_NAME = "Join_1";
	private static final String SALES_PROJ_NAME = "Projection_2";
	private static final String PRODUCT_PROJ_NAME = "Projection_1";

	public static void main(final String[] args) {
		System.out.println(createRoot().toXMLWithXSDValidation());
	}

	public static ScenarioRoot createRoot() {
		// Create the data sources of the view --> all accessed tables
		final DataSources dataSources = new DataSources();
		dataSources.addDataSource(createDataSource(PRODUCT_TABLE));
		dataSources.addDataSource(createDataSource(SALES_TABLE));

		// Create the semantic attributes
		final Attributes semanticAttributes = new Attributes();
		semanticAttributes.addAttribute(createAttribute(ID_COLUMN, 1, JOIN_VIEW_NAME));
		semanticAttributes.addAttribute(createAttribute(PRODUCTID_COLUMN, 2, JOIN_VIEW_NAME));
		semanticAttributes.addAttribute(createAttribute(DETAIL_COLUMN, 4, JOIN_VIEW_NAME));
		semanticAttributes.addAttribute(createAttribute(PRODUCTNAME_COLUMN, 5, JOIN_VIEW_NAME));

		// Create the semantic measures
		final BaseMeasures semanticMeasures = new BaseMeasures();
		semanticMeasures.addMeasure(createMeasure(AMOUNT_COLUMN, 3, JOIN_VIEW_NAME));
		semanticMeasures.addMeasure(createMeasure(PRICE_COLUMN, 6, JOIN_VIEW_NAME));

		// Semantics node
		final MeasureGroup logicalModel = new MeasureGroup();
		logicalModel.setId(JOIN_VIEW_NAME);
		logicalModel.setAttributes(semanticAttributes);
		logicalModel.setBaseMeasures(semanticMeasures);
		logicalModel.setCalculatedAttributes(new CalculatedAttributes());

		final CalculationViews calculationViews = new CalculationViews();
		calculationViews.addCalculationView(createJoinView());
		calculationViews.addCalculationView(createProductProjectionView());
		calculationViews.addCalculationView(createSalesProjectionView());

		final CalculationScenario calcScenario = new CalculationScenario();
		calcScenario.setId("SALES_VIEW");
		calcScenario.setApplyPrivilegeType(PrivilegeType.NONE);
		calcScenario.setOutputViewType(CalculationViewType.AGGREGATION);
		calcScenario.setDataCategory(DataCategory.CUBE);
		calcScenario.setLogicalModel(logicalModel);
		calcScenario.setCalculationViews(calculationViews);
		calcScenario.setDataSources(dataSources);

		final ScenarioRoot root = new ScenarioRoot();
		root.setScenario(calcScenario);
		return root;
	}

	// ================================================================================
	// Views
	// ================================================================================

	private static ProjectionView createSalesProjectionView() {
		return createProjection(SALES_PROJ_NAME, SALES_TABLE, Arrays.asList(ID_COLUMN, PRODUCTID_COLUMN, AMOUNT_COLUMN, DETAIL_COLUMN));
	}

	private static ProjectionView createProductProjectionView() {
		return createProjection(PRODUCT_PROJ_NAME, PRODUCT_TABLE, Arrays.asList(ID_COLUMN, PRODUCTNAME_COLUMN, PRICE_COLUMN));
	}

	private static ProjectionView createProjection(final String name, final String dataSource, final List<String> col) {
		final Input input = new Input();
		final ViewAttributes viewAttributes = new ViewAttributes();

		input.setNode(dataSource);
		col.forEach(colN -> {
			viewAttributes.addViewAttribute(createViewAttribute(colN));
			input.addMapping(createMapping(colN, colN));
		});

		final ProjectionView projectionView1 = new ProjectionView();
		projectionView1.setId(name);
		projectionView1.setViewAttributes(viewAttributes);
		projectionView1.addInput(input);
		return projectionView1;
	}

	private static JoinView createJoinView() {
		final ViewAttributes viewAttributes = new ViewAttributes();
		viewAttributes.addViewAttribute(createViewAttribute(ID_COLUMN, AggregationType.SUM));
		viewAttributes.addViewAttribute(createViewAttribute(PRODUCTID_COLUMN, AggregationType.SUM));
		viewAttributes.addViewAttribute(createViewAttribute(AMOUNT_COLUMN, AggregationType.SUM));
		viewAttributes.addViewAttribute(createViewAttribute(DETAIL_COLUMN));
		viewAttributes.addViewAttribute(createViewAttribute(PRODUCTNAME_COLUMN));
		viewAttributes.addViewAttribute(createViewAttribute(PRICE_COLUMN, AggregationType.SUM));

		final ViewAttribute joinViewAttribute = new ViewAttribute();
		joinViewAttribute.setId(JOIN_PRODCT_ID);
		joinViewAttribute.setHidden(Boolean.TRUE);
		viewAttributes.addViewAttribute(joinViewAttribute);

		final Input salesInput = new Input();
		salesInput.setNode(SALES_PROJ_NAME);
		salesInput.addMapping(createMapping(ID_COLUMN, ID_COLUMN));
		salesInput.addMapping(createMapping(PRODUCTID_COLUMN, PRODUCTID_COLUMN));
		salesInput.addMapping(createMapping(AMOUNT_COLUMN, AMOUNT_COLUMN));
		salesInput.addMapping(createMapping(DETAIL_COLUMN, DETAIL_COLUMN));
		salesInput.addMapping(createMapping(PRODUCTID_COLUMN, JOIN_PRODCT_ID));

		final Input productInput = new Input();
		productInput.setNode(PRODUCT_PROJ_NAME);
		productInput.addMapping(createMapping(PRODUCTNAME_COLUMN, PRODUCTNAME_COLUMN));
		productInput.addMapping(createMapping(PRICE_COLUMN, PRICE_COLUMN));
		productInput.addMapping(createMapping(ID_COLUMN, JOIN_PRODCT_ID));

		final JoinAttribute joinAttribute = new JoinAttribute();
		joinAttribute.setName(JOIN_PRODCT_ID);

		final JoinView joinView = new JoinView();
		joinView.setId(JOIN_VIEW_NAME);
		joinView.setViewAttributes(viewAttributes);
		joinView.addInput(productInput);
		joinView.addInput(salesInput);
		joinView.setJoinType(JoinType.INNER);
		joinView.setCardinality(Cardinality.CN_N);
		joinView.addJoinAttribute(joinAttribute);

		return joinView;
	}

	// ================================================================================
	// Helper
	// ================================================================================

	private static AttributeMapping createMapping(final String source, final String target) {
		final AttributeMapping mapping = new AttributeMapping();
		mapping.setSource(source);
		mapping.setTarget(target);
		return mapping;
	}

	private static ViewAttribute createViewAttribute(final String id) {
		final ViewAttribute viewAttribute = new ViewAttribute();
		viewAttribute.setId(id);
		return viewAttribute;
	}

	private static ViewAttribute createViewAttribute(final String id, final AggregationType aggregationType) {
		final ViewAttribute viewAttribute = new ViewAttribute();
		viewAttribute.setId(id);
		viewAttribute.setAggregationType(aggregationType);
		return viewAttribute;
	}

	private static BaseMeasure createMeasure(final String name, final int order, final String co) {
		final BaseMeasure measure = new BaseMeasure();
		measure.setId(name);
		measure.setOrder(Integer.valueOf(order));
		measure.setMeasureType(MeasureType.SIMPLE);
		measure.setAggregationType(AggregationType.SUM);
		measure.setMeasureMapping(createColumnMapping(name, co));
		return measure;
	}

	private static ColumnMapping createColumnMapping(final String name, final String co) {
		final ColumnMapping columnMapping = new ColumnMapping();
		columnMapping.setColumnObjectName(co);
		columnMapping.setColumnName(name);
		return columnMapping;
	}

	private static Attribute createAttribute(final String name, final int order, final String co) {
		final Attribute attribute1 = new Attribute();
		attribute1.setId(name);
		attribute1.setOrder(Integer.valueOf(order));
		attribute1.setSemanticType(SemanticType.EMPTY);
		attribute1.setKeyMapping(createColumnMapping(name, co));
		return attribute1;
	}

	private static DataSource createDataSource(final String tableName) {
		final DataSource salesDataSource = new DataSource();
		salesDataSource.setId(tableName);
		salesDataSource.setResourceUri(tableName);
		return salesDataSource;
	}
}