package com.example.thetoiletfindingapp.ui.PrivateToilet;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PrivateToiletViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public PrivateToiletViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is dashboard fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}