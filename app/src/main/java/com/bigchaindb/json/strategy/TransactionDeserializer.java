package com.bigchaindb.json.strategy;

import android.util.Log;

import com.bigchaindb.model.Asset;
import com.bigchaindb.model.Input;
import com.bigchaindb.model.Output;
import com.bigchaindb.model.Transaction;
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
public class TransactionDeserializer implements JsonDeserializer<Transaction> {
	private static Class metaDataClass = Map.class;

	/**
	 * Set a custom metadata class the class that is serialized should be symmetrical with the class that is deserialized.
	 *
	 * @param metaDataType the metaData class
	 */
	public static void setMetaDataClass( Class metaDataType )
	{
		metaDataClass = metaDataType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.gson.JsonDeserializer#deserialize(com.google.gson.JsonElement,
	 * java.lang.reflect.Type, com.google.gson.JsonDeserializationContext)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Transaction deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		Transaction transaction = new Transaction();
		JsonElement jElement = json.getAsJsonObject();
/*from here*/

		for( JsonElement jCustomAssetElement: jElement.getAsJsonObject().get("customAsset").getAsJsonArray() ) {
			Log.d(getClass().toString(), jElement.getAsJsonObject().get("customAsset").getAsString());
			//transaction.addCustomAsset(JsonUtils.fromJson(jCustomAssetElement.toString(), CustomAsset.class));
		}/*to here*/

		Log.d("txdeserialize", jElement.getAsJsonObject().get("asset").toString());
//		Log.d("txdeserialize", JsonUtils.fromJson(jElement.getAsJsonObject().get("asset").getAsJsonObject().get("asset").toString(), Asset.class)    );
		transaction.setAsset( JsonUtils.fromJson(jElement.getAsJsonObject().get("asset").toString(), Asset.class));
		transaction.setMetaData( JsonUtils.fromJson( jElement.getAsJsonObject().get("metadata").toString(), metaDataClass ));
		transaction.setId(jElement.getAsJsonObject().get("id").toString().replace("\"", ""));

		for( JsonElement jInputElement: jElement.getAsJsonObject().get("inputs").getAsJsonArray() ) {
			transaction.addInput(JsonUtils.fromJson(jInputElement.toString(), Input.class));
		}

		for( JsonElement jOutputElement: jElement.getAsJsonObject().get("outputs").getAsJsonArray() ) {
			transaction.addOutput(JsonUtils.fromJson(jOutputElement.toString(), Output.class));
		}

		transaction.setOperation(jElement.getAsJsonObject().get("operation").toString());
		transaction.setVersion(jElement.getAsJsonObject().get("version").toString());
		
		return transaction;
	}
}
