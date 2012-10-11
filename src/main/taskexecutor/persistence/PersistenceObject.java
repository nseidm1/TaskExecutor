package main.taskexecutor.persistence;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class PersistenceObject implements Parcelable
{
    private String className;
    private Bundle bundle;
    private String TAG;
    public PersistenceObject(Parcel parcel)
    {
	this.className = parcel.readString();
	this.TAG = parcel.readString();
	this.bundle = parcel.readBundle();
    }
    public PersistenceObject(String className, Bundle bundle, String TAG)
    {
	this.className = className;
	this.TAG = TAG;
	this.bundle = bundle;
    }
    public PersistenceObject()
    {
	// TODO Auto-generated constructor stub
    }
    public String getClassName()
    {
	return className;
    }
    public Bundle getBundle()
    {
	return bundle;
    }
    public String getTag()
    {
	return TAG;
    }
    @Override
    public int describeContents()
    {
	return 1;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
	dest.writeString(className);
	dest.writeString(TAG);
	dest.writeBundle(bundle);
    }
    public static final Parcelable.Creator<PersistenceObject> CREATOR =  new Parcelable.Creator<PersistenceObject>() 
    {
	public PersistenceObject createFromParcel(Parcel in) 
	{
	    return new PersistenceObject(in);
	}
	 	
	public PersistenceObject[] newArray(int size)
	{
	    return new PersistenceObject[size];
	}
    };
}