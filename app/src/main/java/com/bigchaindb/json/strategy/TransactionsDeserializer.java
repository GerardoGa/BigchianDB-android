package com.bigchaindb.json.strategy;

import android.util.Log;

import com.bigchaindb.model.*;
import com.bigchaindb.util.JsonUtils;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * The Class TransactionsDeserializer.
 */
public class TransactionsDeserializer implements JsonDeserializer<Transactions> {
	private static Class metaDataClass = Map.class;

	public static void setMetaDataClass( Class metaDataType )
	{
		metaDataClass = metaDataType;
	}

	/* (non-Javadoc)
	 * @see com.google.gson.JsonDeserializer#deserialize(com.google.gson.JsonElement, java.lang.reflect.Type, com.google.gson.JsonDeserializationContext)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Transactions deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		
		Transactions transactions = new Transactions();

		for( JsonElement jElement: json.getAsJsonArray() ) {
			Transaction transaction = new Transaction();

			/*from here*/
			for( JsonElement jCustomAssetElement: jElement.getAsJsonObject().get("customAsset").getAsJsonArray() ) {
				Log.d("jsonElemenent",jElement.getAsJsonObject().get("customAsset").toString());
				//transaction.addCustomAsset(JsonUtils.fromJson(jCustomAssetElement.toString(), CustomAsset.class));
			}
			/*to here*/

			transaction.setAsset(JsonUtils.fromJson(jElement.getAsJsonObject().get("asset").toString(), Asset.class));
			transaction.setMetaData( JsonUtils.fromJson( jElement.getAsJsonObject().get("metadata").toString(), metaDataClass ));
			transaction.setId(jElement.getAsJsonObject().get("id").toString());

			for( JsonElement jInputElement: jElement.getAsJsonObject().get("inputs").getAsJsonArray() ) {
				transaction.addInput(JsonUtils.fromJson(jInputElement.toString(), Input.class));
			}

			for( JsonElement jOutputElement: jElement.getAsJsonObject().get("outputs").getAsJsonArray() ) {
				transaction.addOutput(JsonUtils.fromJson(jOutputElement.toString(), Output.class));
			}
			
			transaction.setOperation(jElement.getAsJsonObject().get("operation").toString());
			transaction.setVersion(jElement.getAsJsonObject().get("version").toString());
			
			transactions.addTransaction(transaction);
			
		}
		return transactions;
	}
}
