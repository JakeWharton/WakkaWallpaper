package com.jakewharton.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * A {@link Preference} that allows for string
 * input.
 * <p>
 * It is a subclass of {@link DialogPreference} and shows the {@link EditText}
 * in a dialog. This {@link EditText} can be modified either programmatically
 * via {@link #getEditText()}, or through XML by setting any EditText
 * attributes on the EditTextPreference.
 * <p>
 * This preference will store a string into the SharedPreferences.
 * <p>
 * See {@link android.R.styleable#EditText EditText Attributes}.
 */
public class EditNumberPreference extends EditTextPreference {
    private int mValue;
    
    public EditNumberPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public EditNumberPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public EditNumberPreference(Context context) {
        super(context, null);
    }
    
    /**
     * Saves the text to the {@link SharedPreferences}.
     * 
     * @param valueString The text to save
     */
    public void setText(String valueString) {
        final boolean wasBlocking = this.shouldDisableDependents();
        
        this.mValue = Integer.parseInt(valueString);
        this.persistInt(this.mValue);
        
        final boolean isBlocking = this.shouldDisableDependents(); 
        if (isBlocking != wasBlocking) {
            this.notifyDependencyChange(isBlocking);
        }
    }

    /**
     * Saves the value to the {@link SharedPreferences}.
     * 
     * @param value The value to save
     */
    public void setNumber(int value) {
        final boolean wasBlocking = this.shouldDisableDependents();
        
        this.persistInt(value);
        
        final boolean isBlocking = this.shouldDisableDependents(); 
        if (isBlocking != wasBlocking) {
            this.notifyDependencyChange(isBlocking);
        }
    }
    
    /**
     * Gets the text from the {@link SharedPreferences}.
     * 
     * @return The current preference value.
     */
    public String getText() {
        return Integer.toString(this.mValue);
    }
    

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        this.setNumber(restoreValue ? this.getPersistedInt(this.mValue) : (Integer)defaultValue);
    }

    @Override
    public boolean shouldDisableDependents() {
        return false;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // No need to save instance state since it's persistent
            return superState;
        }
        
        final SavedState myState = new SavedState(superState);
        myState.value = this.mValue;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }
         
        final SavedState myState = (SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        this.setNumber(myState.value);
    }
    
    private static class SavedState extends BaseSavedState {
        int value;
        
        public SavedState(Parcel source) {
            super(source);
            value = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(value);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @SuppressWarnings("unused")
		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
    
}