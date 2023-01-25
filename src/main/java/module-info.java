import se.uu.ub.cora.clientbasicdata.BasicClientDataFactory;
import se.uu.ub.cora.clientbasicdata.converter.datatojson.BasicDataToJsonConverterFactoryCreator;
import se.uu.ub.cora.clientbasicdata.converter.jsontodata.JsonToBasicClientDataConverterFactoryImp;

module se.uu.ub.cora.clientbasicdata {
	requires transitive se.uu.ub.cora.json;
	requires transitive se.uu.ub.cora.clientdata;

	exports se.uu.ub.cora.clientbasicdata.converter;
	exports se.uu.ub.cora.clientbasicdata.converter.datatojson;
	exports se.uu.ub.cora.clientbasicdata.converter.jsontodata;

	provides se.uu.ub.cora.clientdata.ClientDataFactory with BasicClientDataFactory;

	provides se.uu.ub.cora.clientdata.converter.JsonToClientDataConverterFactory
			with JsonToBasicClientDataConverterFactoryImp;
	provides se.uu.ub.cora.clientdata.converter.ClientDataToJsonConverterFactoryCreator
			with BasicDataToJsonConverterFactoryCreator;
}