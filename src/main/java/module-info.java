import se.uu.ub.cora.clientbasicdata.CoraDataFactory;
import se.uu.ub.cora.clientbasicdata.converter.datatojson.BasicDataToJsonConverterFactoryCreator;
import se.uu.ub.cora.clientbasicdata.converter.jsontodata.JsonToDataConverterFactoryImp;

module se.uu.ub.cora.clientbasicdata {
	requires transitive se.uu.ub.cora.json;
	requires transitive se.uu.ub.cora.clientdata;

	exports se.uu.ub.cora.clientbasicdata.converter;
	exports se.uu.ub.cora.clientbasicdata.converter.datatojson;
	exports se.uu.ub.cora.clientbasicdata.converter.jsontodata;

	provides se.uu.ub.cora.clientdata.ClientDataFactory with CoraDataFactory;

	provides se.uu.ub.cora.clientdata.converter.JsonToDataConverterFactory
			with JsonToDataConverterFactoryImp;
	provides se.uu.ub.cora.clientdata.converter.DataToJsonConverterFactoryCreator
			with BasicDataToJsonConverterFactoryCreator;
}