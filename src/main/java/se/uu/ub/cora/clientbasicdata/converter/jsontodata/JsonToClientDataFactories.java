package se.uu.ub.cora.clientbasicdata.converter.jsontodata;

import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverterFactory;

public record JsonToClientDataFactories(JsonToClientDataConverterFactory dataConverterFactory,
		JsonToBasicClientDataActionLinkConverterFactory actionLinkConverterFactory) {

}
