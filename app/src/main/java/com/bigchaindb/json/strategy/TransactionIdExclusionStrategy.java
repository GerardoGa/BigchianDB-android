package com.bigchaindb.json.strategy;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class TransactionIdExclusionStrategy implements ExclusionStrategy
{
	public boolean shouldSkipClass( Class<?> klass )
	{
		return false;
	}

	public boolean shouldSkipField( FieldAttributes f )
	{
		//return f.getDeclaringClass() == Transaction.class && f.getName().equals( "id" );
		return false;
	}
}
