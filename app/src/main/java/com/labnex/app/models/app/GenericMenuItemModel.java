package com.labnex.app.models.app;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author mmarif
 */
public class GenericMenuItemModel implements Parcelable {

	private final String id;
	private final int labelRes;
	private final int iconRes;
	private final int backgroundAttr;
	private final int contentColorAttr;
	private final String subtitle;

	public GenericMenuItemModel(
			String id, int labelRes, int iconRes, int backgroundAttr, int contentColorAttr) {
		this(id, labelRes, iconRes, backgroundAttr, contentColorAttr, null);
	}

	public GenericMenuItemModel(
			String id,
			int labelRes,
			int iconRes,
			int backgroundAttr,
			int contentColorAttr,
			String subtitle) {
		this.id = id;
		this.labelRes = labelRes;
		this.iconRes = iconRes;
		this.backgroundAttr = backgroundAttr;
		this.contentColorAttr = contentColorAttr;
		this.subtitle = subtitle;
	}

	protected GenericMenuItemModel(Parcel in) {
		id = in.readString();
		labelRes = in.readInt();
		iconRes = in.readInt();
		backgroundAttr = in.readInt();
		contentColorAttr = in.readInt();
		subtitle = in.readString();
	}

	public static final Creator<GenericMenuItemModel> CREATOR =
			new Creator<>() {
				@Override
				public GenericMenuItemModel createFromParcel(Parcel in) {
					return new GenericMenuItemModel(in);
				}

				@Override
				public GenericMenuItemModel[] newArray(int size) {
					return new GenericMenuItemModel[size];
				}
			};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeInt(labelRes);
		dest.writeInt(iconRes);
		dest.writeInt(backgroundAttr);
		dest.writeInt(contentColorAttr);
		dest.writeString(subtitle);
	}

	public String getId() {
		return id;
	}

	public int getLabelRes() {
		return labelRes;
	}

	public int getIconRes() {
		return iconRes;
	}

	public int getBackgroundAttr() {
		return backgroundAttr;
	}

	public int getContentColorAttr() {
		return contentColorAttr;
	}

	public String getSubtitle() {
		return subtitle;
	}
}
