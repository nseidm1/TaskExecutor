package main.taskexecutor.persistence;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class PersistenceObject implements Parcelable {
    private String className;
    private String TAG;
    private boolean shouldRemoveFromQueueOnSuccess;
    private boolean shouldRemoveFromQueueOnException;
    private Bundle bundle = new Bundle();

    public PersistenceObject() {
    }

    public PersistenceObject(Parcel parcel) {
	className = parcel.readString();
	TAG = parcel.readString();
	switch (parcel.readInt()) {
	case 0:
	    shouldRemoveFromQueueOnSuccess = false;
	    break;
	case 1:
	    shouldRemoveFromQueueOnSuccess = true;
	    break;
	}
	switch (parcel.readInt()) {
	case 0:
	    shouldRemoveFromQueueOnException = false;
	    break;
	case 1:
	    shouldRemoveFromQueueOnException = true;
	    break;
	}
	bundle = parcel.readBundle();
    }

    public PersistenceObject(String className, Bundle bundle, String TAG,
	    boolean shouldRemoveFromQueueOnSuccess,
	    boolean shouldRemoveFromQueueOnException) {
	this.className = className;
	this.TAG = TAG;
	this.bundle = bundle;
	this.shouldRemoveFromQueueOnSuccess = shouldRemoveFromQueueOnSuccess;
	this.shouldRemoveFromQueueOnException = shouldRemoveFromQueueOnException;
    }

    public String getClassName() {
	return className;
    }

    public Bundle getBundle() {
	return bundle;
    }

    public String getTag() {
	return TAG;
    }

    public boolean getShouldRemoveFromQueueOnSuccess() {
	return shouldRemoveFromQueueOnSuccess;
    }

    public boolean getShouldRemoveFromQueueOnException() {
	return shouldRemoveFromQueueOnException;
    }

    @Override
    public int describeContents() {
	return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
	dest.writeString(className);
	dest.writeString(TAG);
	if (shouldRemoveFromQueueOnSuccess) {
	    dest.writeInt(1);
	} else {
	    dest.writeInt(0);
	}
	if (shouldRemoveFromQueueOnException) {
	    dest.writeInt(1);
	} else {
	    dest.writeInt(0);
	}
	dest.writeBundle(bundle);
    }

    public static final Parcelable.Creator<PersistenceObject> CREATOR = new Parcelable.Creator<PersistenceObject>() {
	public PersistenceObject createFromParcel(Parcel in) {
	    return new PersistenceObject(in);
	}

	public PersistenceObject[] newArray(int size) {
	    return new PersistenceObject[size];
	}
    };
}