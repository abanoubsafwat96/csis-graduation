package com.csis.social.app;

import android.os.Parcel;
import android.os.Parcelable;

class Question implements Parcelable {
    public String question;
    public String answer;
    public String choice1;
    public String choice2;
    public String choice3;
    public String choice4;

    public Question() {
    }

    public Question(String question, String answer, String choice1, String choice2, String choice3, String choice4) {
        this.question = question;
        this.answer=answer;
        this.choice1 = choice1;
        this.choice2 = choice2;
        this.choice3 = choice3;
        this.choice4 = choice4;
    }

    protected Question(Parcel in) {
        question = in.readString();
        answer = in.readString();
        choice1 = in.readString();
        choice2 = in.readString();
        choice3 = in.readString();
        choice4 = in.readString();
    }

    public static final Creator<Question> CREATOR = new Creator<Question>() {
        @Override
        public Question createFromParcel(Parcel in) {
            return new Question(in);
        }

        @Override
        public Question[] newArray(int size) {
            return new Question[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(question);
        parcel.writeString(answer);
        parcel.writeString(choice1);
        parcel.writeString(choice2);
        parcel.writeString(choice3);
        parcel.writeString(choice4);
    }
}
