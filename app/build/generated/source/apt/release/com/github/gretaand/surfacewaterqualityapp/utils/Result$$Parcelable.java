
package com.github.gretaand.surfacewaterqualityapp.utils;

import java.util.Date;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import org.parceler.Generated;
import org.parceler.IdentityCollection;
import org.parceler.ParcelWrapper;
import org.parceler.ParcelerRuntimeException;

@Generated("org.parceler.ParcelAnnotationProcessor")
@SuppressWarnings({
    "unchecked",
    "deprecation"
})
public class Result$$Parcelable
    implements Parcelable, ParcelWrapper<com.github.gretaand.surfacewaterqualityapp.utils.Result>
{

    private com.github.gretaand.surfacewaterqualityapp.utils.Result result$$0;
    @SuppressWarnings("UnusedDeclaration")
    public final static Creator<Result$$Parcelable>CREATOR = new Creator<Result$$Parcelable>() {


        @Override
        public Result$$Parcelable createFromParcel(android.os.Parcel parcel$$2) {
            return new Result$$Parcelable(read(parcel$$2, new IdentityCollection()));
        }

        @Override
        public Result$$Parcelable[] newArray(int size) {
            return new Result$$Parcelable[size] ;
        }

    }
    ;

    public Result$$Parcelable(com.github.gretaand.surfacewaterqualityapp.utils.Result result$$2) {
        result$$0 = result$$2;
    }

    @Override
    public void writeToParcel(android.os.Parcel parcel$$0, int flags) {
        write(result$$0, parcel$$0, flags, new IdentityCollection());
    }

    public static void write(com.github.gretaand.surfacewaterqualityapp.utils.Result result$$1, android.os.Parcel parcel$$1, int flags$$0, IdentityCollection identityMap$$0) {
        int identity$$0 = identityMap$$0 .getKey(result$$1);
        if (identity$$0 != -1) {
            parcel$$1 .writeInt(identity$$0);
        } else {
            parcel$$1 .writeInt(identityMap$$0 .put(result$$1));
            parcel$$1 .writeSerializable(result$$1 .activityStartDate);
            parcel$$1 .writeString(result$$1 .measureUnitCode);
            parcel$$1 .writeInt(result$$1 .stationPrimaryKey);
            parcel$$1 .writeString(result$$1 .activityMediaName);
            parcel$$1 .writeString(result$$1 .measureValueString);
            parcel$$1 .writeString(result$$1 .detectionCondition);
            parcel$$1 .writeInt(result$$1 .warningLevel);
            parcel$$1 .writeString(result$$1 .characteristicName);
            parcel$$1 .writeDouble(result$$1 .convertedMeasureValue);
            parcel$$1 .writeString(result$$1 .convertedMeasureUnitCode);
        }
    }

    @Override
    public int describeContents() {
        return  0;
    }

    @Override
    public com.github.gretaand.surfacewaterqualityapp.utils.Result getParcel() {
        return result$$0;
    }

    public static com.github.gretaand.surfacewaterqualityapp.utils.Result read(android.os.Parcel parcel$$3, IdentityCollection identityMap$$1) {
        int identity$$1 = parcel$$3 .readInt();
        if (identityMap$$1 .containsKey(identity$$1)) {
            if (identityMap$$1 .isReserved(identity$$1)) {
                throw new ParcelerRuntimeException("An instance loop was detected whild building Parcelable and deseralization cannot continue.  This error is most likely due to using @ParcelConstructor or @ParcelFactory.");
            }
            return identityMap$$1 .get(identity$$1);
        } else {
            com.github.gretaand.surfacewaterqualityapp.utils.Result result$$4;
            int reservation$$0 = identityMap$$1 .reserve();
            result$$4 = new com.github.gretaand.surfacewaterqualityapp.utils.Result();
            identityMap$$1 .put(reservation$$0, result$$4);
            result$$4 .activityStartDate = ((Date) parcel$$3 .readSerializable());
            result$$4 .measureUnitCode = parcel$$3 .readString();
            result$$4 .stationPrimaryKey = parcel$$3 .readInt();
            result$$4 .activityMediaName = parcel$$3 .readString();
            result$$4 .measureValueString = parcel$$3 .readString();
            result$$4 .detectionCondition = parcel$$3 .readString();
            result$$4 .warningLevel = parcel$$3 .readInt();
            result$$4 .characteristicName = parcel$$3 .readString();
            result$$4 .convertedMeasureValue = parcel$$3 .readDouble();
            result$$4 .convertedMeasureUnitCode = parcel$$3 .readString();
            com.github.gretaand.surfacewaterqualityapp.utils.Result result$$3 = result$$4;
            identityMap$$1 .put(identity$$1, result$$3);
            return result$$3;
        }
    }

}
