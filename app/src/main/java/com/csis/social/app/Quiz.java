package com.csis.social.app;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

class Quiz implements Parcelable {
    public String uid;
    public String title;
    public String description;
    public String timerInMilliSec;
    public ArrayList<Question> questions_list;
    public ArrayList<Question> bonusQuestions_list;
    public String deadline;
    public String subject;

    public Quiz() {
    }

    public Quiz(String title, String description,String timerInMilliSec ,ArrayList<Question> questions_list, String deadline) {
        this.title = title;
        this.description = description;
        this.timerInMilliSec=timerInMilliSec;
        this.questions_list = questions_list;
        this.deadline = deadline;
    }

    public Quiz(String title, String description,String timerInMilliSec , ArrayList<Question> questions_list,
                ArrayList<Question> bonusQuestions_list, String deadline) {
        this.title = title;
        this.description = description;
        this.timerInMilliSec=timerInMilliSec;
        this.questions_list = questions_list;
        this.bonusQuestions_list = bonusQuestions_list;
        this.deadline = deadline;
    }

    protected Quiz(Parcel in) {
        uid = in.readString();
        title = in.readString();
        description = in.readString();
        timerInMilliSec=in.readString();
        deadline = in.readString();
        subject=in.readString();
    }

    public static final Creator<Quiz> CREATOR = new Creator<Quiz>() {
        @Override
        public Quiz createFromParcel(Parcel in) {
            return new Quiz(in);
        }

        @Override
        public Quiz[] newArray(int size) {
            return new Quiz[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(uid);
        parcel.writeString(title);
        parcel.writeString(description);
        parcel.writeString(timerInMilliSec);
        parcel.writeString(deadline);
        parcel.writeString(subject);
    }
}
